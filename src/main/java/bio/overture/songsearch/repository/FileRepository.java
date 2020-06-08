package bio.overture.songsearch.repository;

import bio.overture.songsearch.config.ElasticsearchProperties;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

import static bio.overture.songsearch.config.SearchFields.*;
import static bio.overture.songsearch.utils.ElasticsearchQueryUtils.queryFromArgs;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

@Slf4j
@Component
public class FileRepository {
  private static final Map<String, Function<String, AbstractQueryBuilder<?>>> QUERY_RESOLVER =
      argumentPathMap();

  private final RestHighLevelClient client;
  private final String fileCentricIndex;

  @Autowired
  public FileRepository(
      @NonNull RestHighLevelClient client,
      @NonNull ElasticsearchProperties elasticSearchProperties) {
    this.client = client;
    this.fileCentricIndex = elasticSearchProperties.getFileCentricIndex();
  }

  private static Map<String, Function<String, AbstractQueryBuilder<?>>> argumentPathMap() {
    return ImmutableMap.<String, Function<String, AbstractQueryBuilder<?>>>builder()
        .put(FILE_ID, value -> new TermQueryBuilder(FILE_ID, value))
        .put(FILE_OBJECT_ID, value -> new TermQueryBuilder(FILE_OBJECT_ID, value))
        .put(FILE_NAME, value -> new TermQueryBuilder(FILE_NAME, value))
        .build();
  }

  public SearchResponse getFiles(Map<String, Object> filter, Map<String, Integer> page) {
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

  @SneakyThrows
  private SearchResponse execute(@NonNull SearchSourceBuilder builder) {
    val searchRequest = new SearchRequest(fileCentricIndex);
    searchRequest.source(builder);
    return client.search(searchRequest, RequestOptions.DEFAULT);
  }
}
