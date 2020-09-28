package bio.overture.songsearch.model.enums;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SpecimenType {
  NORMAL("Normal"),
  TUMOUR("Tumour");

  @NonNull private final String value;

  @Override
  public String toString() {
    return value;
  }
}
