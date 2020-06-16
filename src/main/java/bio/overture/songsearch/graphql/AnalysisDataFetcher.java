package bio.overture.songsearch.graphql;

import bio.overture.songsearch.model.Analysis;
import bio.overture.songsearch.service.AnalysisService;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AnalysisDataFetcher {

  private final AnalysisService analysisService;

  @Autowired
  public AnalysisDataFetcher(AnalysisService analysisService) {
    this.analysisService = analysisService;
  }

  @SuppressWarnings("unchecked")
  public DataFetcher<List<Analysis>> getAnalysesDataFetcher() {
    return environment -> {
      val args = environment.getArguments();

      val filter = ImmutableMap.<String, Object>builder();
      val page = ImmutableMap.<String, Integer>builder();

      if (args != null) {
        if (args.get("filter") != null) filter.putAll((Map<String, Object>) args.get("filter"));
        if (args.get("page") != null) page.putAll((Map<String, Integer>) args.get("page"));
      }
      return analysisService.getAnalyses(filter.build(), page.build());
    };
  }
}
