package bio.overture.songsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Specimen {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String specimenId;

  private String submitterSpecimenId;

  private String tumourNormalDesignation;

  private String specimenTissueSource;

  private String specimenType;

  private List<Donor> donors;

  @SneakyThrows
  public static Specimen parse(@NonNull Map<String, Object> sourceMap) {
    return MAPPER.convertValue(sourceMap, Specimen.class);
  }
}
