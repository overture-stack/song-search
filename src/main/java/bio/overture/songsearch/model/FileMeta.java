package bio.overture.songsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileMeta {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String name;

  private String md5sum;

  private Integer size;

  private String dataType;

  private String indexFile;

  @SneakyThrows
  public static FileMeta parse(@NonNull Map<String, Object> sourceMap) {
    return MAPPER.convertValue(sourceMap, FileMeta.class);
  }
}
