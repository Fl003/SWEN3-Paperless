package at.technikum.paperless.integration;

import at.technikum.paperless.PaperlessApplication;
import at.technikum.paperless.repository.DocumentRepository;
import at.technikum.paperless.repository.user.UserRepository;
import at.technikum.paperless.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Parameter;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
@SpringBootTest(
        classes = PaperlessApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
@Import(DocumentUploadIntegrationTest.TestConfig.class)
class DocumentUploadIntegrationTest {
    // containers
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("paperless_test")
            .withUsername("paperless")
            .withPassword("paperless");

    @Container
    static final ElasticsearchContainer elasticsearch = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.18.5")
    )
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        // DB
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        // Kafka
        //r.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        // Elasticsearch
        r.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);

        r.add("spring.data.elasticsearch.repositories.enabled", () -> "true");

        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    //Spring beans
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RequestMappingHandlerMapping handlerMapping;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void cleanDb() {
        documentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void uploadDocument_persistsDocument() throws Exception {
        // create test user
        var user = TestDataFactory.insertUser(userRepository);

        // auto-discover real upload mapping + multipart field name
        UploadEndpoint endpoint = resolveUploadEndpoint()
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find an upload endpoint. " +
                                "Expected a POST mapping in DocumentController consuming multipart/form-data."
                ));

        MockMultipartFile file = new MockMultipartFile(
                endpoint.multipartFieldName(),
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "hello from integration test".getBytes()
        );

        mockMvc.perform(
                        multipart(endpoint.path())
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().is2xxSuccessful());

        assertThat(documentRepository.count())
                .as("Document should be stored in DB after upload")
                .isGreaterThan(0);
    }

    /**
     * Finds the real upload endpoint by scanning Spring MVC mappings at runtime.
     */
    private Optional<UploadEndpoint> resolveUploadEndpoint() {
        Map<RequestMappingInfo, HandlerMethod> methods = handlerMapping.getHandlerMethods();
        System.out.println("=== REGISTERED HANDLERS ===");
        methods.forEach((info, method) -> {
            System.out.println(method.getBeanType().getName() + " -> " + info);
        });
        System.out.println("=== END HANDLERS ===");
        methods.forEach((k, v) ->
                System.out.println(v.getBeanType().getName())
        );
        return methods.entrySet().stream()
                .filter(e -> {
                    HandlerMethod hm = e.getValue();
                    return hm.getBeanType().getSimpleName().equals("DocumentController");
                })
                .filter(e -> {
                    RequestMappingInfo info = e.getKey();
                    boolean isPost = info.getMethodsCondition()
                            .getMethods()
                            .stream()
                            .anyMatch(m -> m == RequestMethod.POST);
                    boolean consumesMultipart = info.getConsumesCondition().getConsumableMediaTypes()
                            .stream()
                            .anyMatch(mt -> mt.isCompatibleWith(MediaType.MULTIPART_FORM_DATA));
                    return isPost && consumesMultipart;
                })
                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                .map(e -> {
                    String path = e.getKey().getPatternValues().stream().findFirst().orElse(null);
                    String multipartField = extractMultipartFieldName(e.getValue()).orElse("file");
                    return path == null ? null : new UploadEndpoint(path, multipartField);
                })
                .filter(x -> x != null)
                .findFirst();
    }

    private Optional<String> extractMultipartFieldName(HandlerMethod hm) {
        for (MethodParameter mp : hm.getMethodParameters()) {
            Parameter p = mp.getParameter();
            if (p.getType().getSimpleName().equals("MultipartFile")) {
                RequestParam rp = p.getAnnotation(RequestParam.class);
                if (rp != null && !rp.value().isBlank()) return Optional.of(rp.value());
                if (rp != null && !rp.name().isBlank()) return Optional.of(rp.name());
                return Optional.of("file");
            }
        }
        return Optional.empty();
    }

    record UploadEndpoint(String path, String multipartFieldName) {}

    /**
     * Test-only config:
     * - Mocks FileStorageService so MinIO/S3 is not required for the integration test.
     * - Permits all HTTP requests so we don’t fight JWT/CSRF while validating upload flow.
     */
    static class TestConfig {

        @Bean
        FileStorageService fileStorageService() {
            FileStorageService mock = Mockito.mock(FileStorageService.class);

            // Adjust this stub to match the service API!
            // Example patterns:
            // Mockito.when(mock.store(Mockito.any())).thenReturn("test-key");
            // Mockito.when(mock.upload(Mockito.any(), Mockito.anyString())).thenReturn("test-key");

            return mock;
        }

        @Bean
        SecurityFilterChain testSecurity(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    /**
     * Tiny helper so the test compiles even if the User entity differs.
     * Replace with your real User builder/constructor once, and you’re done.
     */
    static class TestDataFactory {
        static Object insertUser(UserRepository userRepository) {
            // If you have a User entity like at.technikum.paperless.model.User,
            // create + save it here.
            //
            // Example (adapt to your fields):
            // User u = new User();
            // u.setUsername("it-test");
            // u.setPassword("x");
            // return userRepository.save(u);

            return null;
        }
    }
}
