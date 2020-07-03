package bio.overture.songsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "song-search")
public class SongSearchProperties {
    Map<String, List<String>> runParameterKeys;
}
