package bio.overture.songsearch.model;

import lombok.Value;

@Value
public class NestedFieldPath {
  String objectPath;
  String fieldPath;
}
