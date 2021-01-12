package bio.overture.songsearch.model;

import java.util.List;
import lombok.Value;

@Value
public class ProbeResult<T> {
  List<T> content;
  Info info;

  public ProbeResult(List<T> content, Boolean hasNextFrom, Long totalHits) {
    this.content = content;
    this.info = new Info(hasNextFrom, totalHits, content.size());
  }

  @Value
  public static class Info {
    Boolean hasNextFrom;
    Long totalHits;
    Integer contentCount;
  }
}
