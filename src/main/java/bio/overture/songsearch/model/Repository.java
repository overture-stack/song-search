package bio.overture.songsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Repository {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String code;

  private String organization;

  private String name;

  private String type;

  private String country;

  private String url;

  @SneakyThrows
  public static Repository parse(@NonNull Map<String, Object> sourceMap) {
    return MAPPER.convertValue(sourceMap, Repository.class);
  }
}
