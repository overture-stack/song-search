package bio.overture.songsearch.model;

import lombok.Value;

@Value
public class SampleMatchedAnalysisPair {
  Analysis normalSampleAnalysis;
  Analysis tumourSampleAnalysis;
}
