package bio.overture.songsearch.service;

import bio.overture.songsearch.model.Analysis;
import bio.overture.songsearch.repository.AnalysisRepository;
import lombok.val;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static bio.overture.songsearch.config.SearchFields.ANALYSIS_ID;
import static java.util.stream.Collectors.toUnmodifiableList;

@Service
public class AnalysisService {

  private final AnalysisRepository analysisRepository;

  public AnalysisService(AnalysisRepository analysisRepository) {
    this.analysisRepository = analysisRepository;
  }

  private static Analysis hitToRun(SearchHit hit) {
    val sourceMap = hit.getSourceAsMap();
    return Analysis.parse(sourceMap);
  }

  public List<Analysis> getAnalyses(Map<String, Object> filter, Map<String, Integer> page) {
    val response = analysisRepository.getAnalyses(filter, page);
    val hitStream = Arrays.stream(response.getHits().getHits());
    return hitStream.map(AnalysisService::hitToRun).collect(toUnmodifiableList());
  }

  public Analysis getAnalysisById(String analysisId) {
      val response = analysisRepository.getAnalyses(Map.of(ANALYSIS_ID, analysisId), null);
      val runOpt = Arrays.stream(response.getHits().getHits()).map(AnalysisService::hitToRun).findFirst();
      return runOpt.orElse(null);
  }
}
