package at.technikum.paperless.mapper;

import at.technikum.paperless.domain.Document;
import at.technikum.paperless.domain.Tag;
import at.technikum.paperless.dto.DocumentDTO;
import org.mapstruct.*;

@Mapper(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class DocumentMapper {
    //from object to data  (for GET)
    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "tags", source = "tags")
    public abstract DocumentDTO map(Document document);
        protected String map(Tag tag) {
            return tag.getName();
        }
}
