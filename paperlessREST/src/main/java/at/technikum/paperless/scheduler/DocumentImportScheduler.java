package at.technikum.paperless.scheduler;

import at.technikum.paperless.service.DocumentImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DocumentImportScheduler {
    private final DocumentImportService importService;

    @Value("${paperless.import.folder}")
    private String folderPath;

    public DocumentImportScheduler(DocumentImportService importService) {
        this.importService = importService;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Europe/Berlin")
    public void runImportTask() {
        try {
            importService.importAndSaveDocuments(folderPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
