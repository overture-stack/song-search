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
public class Analysis {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String analysisId;

  private String analysisType;

  private Integer analysisVersion;

  private String analysisState;

  private String studyId;

  private List<Donor> donors;

  private List<AnalysisFile> files;

  @SneakyThrows
  public static Analysis parse(@NonNull Map<String, Object> sourceMap) {
    return MAPPER.convertValue(sourceMap, Analysis.class);
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static final class AnalysisFile {
    private String objectId;
    private String name;
    private Integer size;
    private String fileType;
    private String md5Sum;
    private String fileAccess;
    private String dataType;
  }
}
