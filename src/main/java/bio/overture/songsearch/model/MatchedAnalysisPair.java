package bio.overture.songsearch.model;

import lombok.Value;

@Value
public class MatchedAnalysisPair {
  Analysis analysis;
  Analysis matchedAnalysis;
}
