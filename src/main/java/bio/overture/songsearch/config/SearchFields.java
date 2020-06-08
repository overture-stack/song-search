package bio.overture.songsearch.config;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class SearchFields {
  public static final String ANALYSIS_ID = "analysisId";
  public static final String ANALYSIS_TYPE = "analysisType";
  public static final String ANALYSIS_VERSION = "analysisVersion";
  public static final String ANALYSIS_STATE = "analysisState";
  public static final String FILE_ID = "fileId";
  public static final String FILE_OBJECT_ID = "objectId";
  public static final String FILE_NAME = "fileName";
}
