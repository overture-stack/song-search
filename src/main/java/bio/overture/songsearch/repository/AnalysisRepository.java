package bio.overture.songsearch.repository;

import bio.overture.songsearch.config.ElasticsearchProperties;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bio.overture.songsearch.config.SearchFields.*;
import static bio.overture.songsearch.utils.ElasticsearchQueryUtils.queryFromArgs;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

@Slf4j
@Component
public class AnalysisRepository {
  private static final Map<String, Function<String, AbstractQueryBuilder<?>>> QUERY_RESOLVER =
      argumentPathMap();

  private final RestHighLevelClient client;
  private final String analysisCentricIndex;

  @Autowired
  public AnalysisRepository(
      @NonNull RestHighLevelClient client,
      @NonNull ElasticsearchProperties elasticSearchProperties) {
    this.client = client;
    this.analysisCentricIndex = elasticSearchProperties.getAnalysisCentricIndex();
  }

  private static Map<String, Function<String, AbstractQueryBuilder<?>>> argumentPathMap() {
    return ImmutableMap.<String, Function<String, AbstractQueryBuilder<?>>>builder()
        .put(ANALYSIS_ID, value -> new TermQueryBuilder("analysis_id", value))
        .put(ANALYSIS_TYPE, value -> new TermQueryBuilder("analysis_type", value))
        .put(ANALYSIS_VERSION, value -> new TermQueryBuilder("analysis_version", value))
        .put(ANALYSIS_STATE, value -> new TermQueryBuilder("analysis_state", value))
        .put(STUDY_ID, value -> new TermQueryBuilder("study_id", value))
        .put(RUN_ID, value -> new TermQueryBuilder("workflow.run_id", value))
        .put(
            DONOR_ID,
            value ->
                new NestedQueryBuilder(
                    "donors", new TermQueryBuilder("donors.donor_id", value), ScoreMode.None))
        .put(
            SPECIMEN_ID,
            value ->
                new NestedQueryBuilder(
                    "donors.specimens", new TermQueryBuilder("donors.specimens.specimen_id", value), ScoreMode.None))
        .put(
            SAMPLE_ID,
            value ->
                new NestedQueryBuilder(
                    "donors.specimens.samples", new TermQueryBuilder("donors.specimens.samples.sample_id", value), ScoreMode.None))
        .build();
  }

  public SearchResponse getAnalyses(Map<String, Object> filter, Map<String, Integer> page) {
    final AbstractQueryBuilder<?> query =
        (filter == null || filter.size() == 0)
            ? matchAllQuery()
            : queryFromArgs(QUERY_RESOLVER, filter);

    val searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(query);

    if (page != null && page.size() != 0) {
      searchSourceBuilder.size(page.get("size"));
      searchSourceBuilder.from(page.get("from"));
    }

    return execute(searchSourceBuilder);
  }

  public List<SearchResponse> getAnalyses(List<Map<String, Object>> multiFilters, Map<String, Integer> page) {
    if (multiFilters.isEmpty()) {
      return List.of(getAnalyses(Map.of(), page));
    }

    List<SearchSourceBuilder> builders = new ArrayList<>();

    multiFilters.stream()
            .filter(f -> f != null && f.size() != 0)
            .forEach(f -> builders.add(createSearchSourceBuilder(f, page)));

    return Arrays.stream(execute(builders).getResponses()).map(MultiSearchResponse.Item::getResponse).collect(Collectors.toList());
  }

  private SearchSourceBuilder createSearchSourceBuilder(Map<String, Object> filter, Map<String, Integer> page) {
    final AbstractQueryBuilder<?> query =
            (filter == null || filter.size() == 0)
                    ? matchAllQuery()
                    : queryFromArgs(QUERY_RESOLVER, filter);

    val searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(query);

    if (page != null && page.size() != 0) {
      searchSourceBuilder.size(page.get("size"));
      searchSourceBuilder.from(page.get("from"));
    }

    return searchSourceBuilder;
  }

  @SneakyThrows
  private SearchResponse execute(@NonNull SearchSourceBuilder builder) {
    val searchRequest = new SearchRequest(analysisCentricIndex);
    searchRequest.source(builder);
    return client.search(searchRequest, RequestOptions.DEFAULT);
  }

  @SneakyThrows
  private MultiSearchResponse execute(@NonNull List<SearchSourceBuilder> builders) {
    MultiSearchRequest mSearchRequest = new MultiSearchRequest();
    builders.forEach(b -> mSearchRequest.add(new SearchRequest().source(b)));
    return client.msearch(mSearchRequest, RequestOptions.DEFAULT);
  }
}
