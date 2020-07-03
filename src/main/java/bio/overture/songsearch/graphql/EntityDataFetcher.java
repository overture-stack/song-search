package bio.overture.songsearch.graphql;

import bio.overture.songsearch.config.SongSearchProperties;
import bio.overture.songsearch.model.Run;
import bio.overture.songsearch.service.AnalysisService;
import bio.overture.songsearch.service.FileService;
import com.apollographql.federation.graphqljava._Entity;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static bio.overture.songsearch.config.SearchFields.ANALYSIS_ID;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class EntityDataFetcher {
  public static final String ANALYSIS_ENTITY = "Analysis";
  public static final String FILE_ENTITY = "File";
  public static final String RUN_ENTITY = "Run";

  private final AnalysisService analysisService;
  private final FileService fileService;
  private final SongSearchProperties songSearchProperties;

  @Autowired
  public EntityDataFetcher(AnalysisService analysisService, FileService fileService, SongSearchProperties songSearchProperties) {
    this.analysisService = analysisService;
    this.fileService = fileService;
    this.songSearchProperties = songSearchProperties;
  }

  public DataFetcher getDataFetcher() {
    return environment ->
        environment.<List<Map<String, Object>>>getArgument(_Entity.argumentName).stream()
            .map(
                values -> {
                  if (ANALYSIS_ENTITY.equals(values.get("__typename"))) {
                    final Object analysisId = values.get("analysisId");
                    if (analysisId instanceof String) {
                      return analysisService.getAnalysisById((String) analysisId);
                    }
                  }
                  if (FILE_ENTITY.equals(values.get("__typename"))) {
                    final Object fileObjectId = values.get("objectId");
                    if (fileObjectId instanceof String) {
                      return fileService.getFileByObjectId((String) fileObjectId);
                    }
                  }
                  if (RUN_ENTITY.equals(values.get("__typename"))) {
                    final Object runId = values.get("runId");
                    val ids = getRelevantAnalysisIdsFromRunParameters(values.get("parameters"));

                    if (runId instanceof String) {
                      return new Run(
                          (String) runId,
                          analysisService.getAnalysesByRunId((String) runId),
                          analysisService.getAnalysesById(ids)
                          );
                    }
                  }
                  return null;
                })
            .collect(toList());
  }

  @SuppressWarnings("unchecked")
  private List<String> getRelevantAnalysisIdsFromRunParameters(Object parametersObj) {
      // convert parameters to map
      val parametersBuilder = ImmutableMap.<String, Object>builder();
      if (parametersObj != null) {
          parametersBuilder.putAll((Map<String, Object>) parametersObj);
      }
      val parameters = parametersBuilder.build();

      List<String> lookFor = songSearchProperties.getRunParameterKeys().get(ANALYSIS_ID);

      val analysisIds = lookFor.stream().map(parameters::get).filter(Objects::nonNull).map(Objects::toString).collect(toList());
      analysisIds.add( "6406af31-7567-4611-86af-317567c611c6"); // TODO remove
      return analysisIds;
  }
}
