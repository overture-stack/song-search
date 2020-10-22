package bio.overture.songsearch.utils;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public final class CommonUtils {

  private CommonUtils() {}

  public static <K, V> ImmutableMap<K, V> asImmutableMap(Object obj) {
    val newMap = ImmutableMap.<K, V>builder();
    if (obj instanceof Map) {
      try {
        newMap.putAll((Map<? extends K, ? extends V>) obj);
      } catch (ClassCastException e) {
        log.error("Failed to cast obj to Map<K,V>");
      }
    }
    return newMap.build();
  }
}
