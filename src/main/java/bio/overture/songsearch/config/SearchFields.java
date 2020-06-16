package bio.overture.songsearch.config;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class SearchFields {
  public static final String ANALYSIS_ID = "analysisId";
  public static final String ANALYSIS_TYPE = "analysisType";
  public static final String ANALYSIS_VERSION = "analysisVersion";
  public static final String ANALYSIS_STATE = "analysisState";
  public static final String FILE_OBJECT_ID = "objectId";
  public static final String FILE_NAME = "name";
  public static final String FILE_ACCESS = "fileAccess";
  public static final String FILE_DATA_TYPE = "dataType";
  public static final String STUDY_ID = "studyId";
  public static final String DONOR_ID = "donorId";
  public static final String SPECIMEN_ID = "specimenId";
  public static final String SAMPLE_ID = "sampleId";
  public static final String RUN_NAME = "runName";
}
