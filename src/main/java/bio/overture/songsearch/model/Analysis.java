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
public class Analysis {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String analysisId;

    private String analysisType;

    private Integer analysisVersion;

    private String analysisState;

    private String program;

    private List<Donor> samples;

    private List<File> files;

    @SneakyThrows
    public static Analysis parse(@NonNull Map<String, Object> sourceMap) {
        return MAPPER.convertValue(sourceMap, Analysis.class);
    }
}
