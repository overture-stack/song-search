package bio.overture.songsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Run {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String runId;

    private List<Analysis> producedAnalyses;

    private String inputAnalyses;

    @SneakyThrows
    public static Run parse(@NonNull Map<String, Object> sourceMap) {
        return MAPPER.convertValue(sourceMap, Run.class);
    }
}
