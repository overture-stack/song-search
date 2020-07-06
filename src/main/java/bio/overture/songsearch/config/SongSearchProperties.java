package bio.overture.songsearch.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "song-search")
public class SongSearchProperties {
    WorkflowRunParameterKeys workflowRunParameterKeys;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowRunParameterKeys {
        List<String> analysisId;
    }
}
