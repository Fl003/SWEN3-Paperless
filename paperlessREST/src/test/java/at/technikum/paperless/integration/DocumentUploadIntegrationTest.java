package at.technikum.paperless.integration;

import at.technikum.paperless.PaperlessApplication;
import at.technikum.paperless.domain.User;
import at.technikum.paperless.messaging.DocumentEventsProducer;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Parameter;
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
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.data.elasticsearch.repositories.enabled=false"
        }
)
@AutoConfigureMockMvc
@Import(DocumentUploadIntegrationTest.TestConfig.class)
class DocumentUploadIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("paperless_test")
                    .withUsername("paperless")
                    .withPassword("paperless");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private RequestMappingHandlerMapping handlerMapping;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private UserRepository userRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    // replace real storage bean
    @MockitoBean
    private FileStorageService fileStorageService;

    // replace Kafka publisher so no producer is created at all
    @MockitoBean
    private DocumentEventsProducer documentEventsProducer;

    @BeforeEach
    void cleanDb() {
        documentRepository.deleteAll();
        userRepository.deleteAll();

        Mockito.when(fileStorageService.store(Mockito.any()))
                .thenReturn("test-storage-key");

        Mockito.doNothing()
                .when(documentEventsProducer)
                .publish(Mockito.any());
    }

    @Test
    @WithMockUser(username = "it-test")
    void uploadDocument_persistsDocument() throws Exception {
        insertUser("it-test");

        UploadEndpoint endpoint = resolveUploadEndpoint()
                .orElseThrow(() -> new IllegalStateException(
                        "No multipart POST endpoint found in DocumentController"
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

        assertThat(documentRepository.count()).isEqualTo(1);
    }

    private Optional<UploadEndpoint> resolveUploadEndpoint() {
        Map<RequestMappingInfo, HandlerMethod> methods = handlerMapping.getHandlerMethods();

        return methods.entrySet().stream()
                .filter(e -> e.getValue().getBeanType().getSimpleName().equals("DocumentController"))
                .filter(e -> {
                    RequestMappingInfo info = e.getKey();
                    boolean isPost = info.getMethodsCondition().getMethods().contains(RequestMethod.POST);
                    boolean multipart = info.getConsumesCondition().getConsumableMediaTypes()
                            .stream()
                            .anyMatch(mt -> mt.isCompatibleWith(MediaType.MULTIPART_FORM_DATA));
                    return isPost && multipart;
                })
                .map(e -> {
                    String path = e.getKey().getPatternValues().iterator().next();
                    String field = extractMultipartFieldName(e.getValue()).orElse("file");
                    return new UploadEndpoint(path, field);
                })
                .findFirst();
    }

    private Optional<String> extractMultipartFieldName(HandlerMethod hm) {
        for (MethodParameter mp : hm.getMethodParameters()) {
            Parameter p = mp.getParameter();
            if (p.getType().getSimpleName().equals("MultipartFile")) {
                RequestParam rp = p.getAnnotation(RequestParam.class);
                if (rp != null && !rp.value().isBlank()) return Optional.of(rp.value());
                return Optional.of("file");
            }
        }
        return Optional.empty();
    }

    record UploadEndpoint(String path, String multipartFieldName) {}

    static class TestConfig {
        @Bean
        SecurityFilterChain testSecurity(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    private void insertUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordDigest("test-digest");
        userRepository.save(user);
    }
}