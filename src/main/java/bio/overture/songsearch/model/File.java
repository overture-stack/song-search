package bio.overture.songsearch.model;

import bio.overture.songsearch.model.enums.AnalysisState;
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
public class File {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String objectId;

  private String studyId;

  private String dataType;

  private String fileType;

  private String fileAccess;

  private Analysis analysis;

  private FileMeta file;

  private List<Repository> repositories;

  private List<Donor> donors;

  @SneakyThrows
  public static File parse(@NonNull Map<String, Object> sourceMap) {
    return MAPPER.convertValue(sourceMap, File.class);
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static final class Analysis {
    private String analysisId;
    private String analysisType;
    private Integer analysisVersion;
    private AnalysisState analysisState;
    private Map<String, Object> experiment;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static final class FileMeta {
    private String name;
    private String md5sum;
    private Long size;
    private String dataType;
    private IndexFile indexFile;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static final class IndexFile {
    private String objectId;
    private String name;
    private String fileType;
    private String md5sum;
    private String dataType;
    private Long size;
  }
}
