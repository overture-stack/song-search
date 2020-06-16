package bio.overture.songsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Donor {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String donorId;

  private String submitterDonorId;

  private String gender;

  private List<Specimen> specimens;

  @SneakyThrows
  public static Donor parse(@NonNull Map<String, Object> sourceMap) {
    return MAPPER.convertValue(sourceMap, Donor.class);
  }
}
