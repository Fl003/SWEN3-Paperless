package at.technikum.paperless.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchResultDTO {

    private Long documentId;
    private String name;
    private String contentType;
    private String status;
    private List<String> tags;

    // search related
    private Double score;
    private String snippet;
}