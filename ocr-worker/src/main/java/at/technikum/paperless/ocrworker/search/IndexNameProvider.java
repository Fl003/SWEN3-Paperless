package at.technikum.paperless.ocrworker.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexNameProvider {
    public final String indexName;

    public IndexNameProvider(@Value("${app.search.index:documents}") String indexName) {
        this.indexName = indexName;
    }
}