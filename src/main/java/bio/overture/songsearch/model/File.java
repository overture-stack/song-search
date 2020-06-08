package bio.overture.songsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class File {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String fileId;

    private String objectId;

    private String name;

    private Integer size;

    private String md5sum;

    private String fileType;

    private String fileAccess;

    private String dataType;

    private Analysis analysis;

    @SneakyThrows
    public static File parse(@NonNull Map<String, Object> sourceMap) {
        return MAPPER.convertValue(sourceMap, File.class);
    }
}
