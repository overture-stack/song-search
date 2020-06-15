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
public class Workflow {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String runName;

    private String runId;

    private String workflowName;

    private String workflowVersion;

    private List<Map<String, Object>> inputs;

    private String genomeBuild;

    private List<String> analysisTools;

    @SneakyThrows
    public static Workflow parse(@NonNull Map<String, Object> sourceMap) {
        return MAPPER.convertValue(sourceMap, Workflow.class);
    }
}
