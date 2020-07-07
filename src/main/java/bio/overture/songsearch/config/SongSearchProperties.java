package bio.overture.songsearch.config;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "song-search")
public class SongSearchProperties {
    WorkflowRunParameterKeys workflowRunParameterKeys;

    @Value
    @ConstructorBinding
    public static class WorkflowRunParameterKeys {
       ImmutableList<String> analysisId;

        public WorkflowRunParameterKeys(List<String> analysisId) {
            this.analysisId = ImmutableList.copyOf(analysisId);
        }
    }
}
