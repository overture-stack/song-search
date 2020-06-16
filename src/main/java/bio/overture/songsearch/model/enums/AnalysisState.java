package bio.overture.songsearch.model.enums;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AnalysisState {
    PUBLISHED("PUBLISHED"),
    UNPUBLISHED("UNPUBLISHED"),
    SUPPRESSED("SUPPRESSED");

    @NonNull
    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
