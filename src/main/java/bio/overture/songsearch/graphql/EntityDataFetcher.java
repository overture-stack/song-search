package bio.overture.songsearch.graphql;

import bio.overture.songsearch.model.Run;
import bio.overture.songsearch.service.AnalysisService;
import bio.overture.songsearch.service.FileService;
import com.apollographql.federation.graphqljava._Entity;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class EntityDataFetcher {
  public static final String ANALYSIS_ENTITY = "Analysis";
  public static final String FILE_ENTITY = "File";
  public static final String RUN_ENTITY = "Run";

  private final AnalysisService analysisService;
  private final FileService fileService;

  @Autowired
  public EntityDataFetcher(AnalysisService analysisService, FileService fileService) {
    this.analysisService = analysisService;
    this.fileService = fileService;
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
                    val filters = createAnalysisFiltersFromRunParameters(values.get("parameters"));

                    if (runId instanceof String) {
                      return new Run(
                          (String) runId,
                          analysisService.getAnalysesByRunId((String) runId),
                          analysisService.getAnalyses(filters, null)
                          );
                    }
                  }
                  return null;
                })
            .collect(toList());
  }

  @SuppressWarnings("unchecked")
  private ImmutableMap<String, Object> createAnalysisFiltersFromRunParameters(Object parametersObj) {
      // conver parameters to map
      val parametersBuilder = ImmutableMap.<String, Object>builder();
      if (parametersObj != null) {
          parametersBuilder.putAll((Map<String, Object>) parametersObj);
      }
      val parameters = parametersBuilder.build();

      List<String> lookFor = List.of("analysis_id", "normal_aln_analysis_id", "tumour_aln_analysis_id"); // TODO put in more common place?

      val filtersBuilder = ImmutableMap.<String, Object>builder();
      lookFor.forEach(k -> {
          if (parameters.get(k) != null) {
              val formattedKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k.toLowerCase());  // TODO check if this is needed
              filtersBuilder.put(formattedKey, parameters.get(k));
          }
      });
      return filtersBuilder.build();
  }
}
