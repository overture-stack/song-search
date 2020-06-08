package bio.overture.songsearch.utils;

import lombok.val;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Map;
import java.util.function.Function;

public class ElasticsearchQueryUtils {
  /**
   * For each argument, find its query producer function and apply the argument value ANDing it in a
   * bool query
   *
   * @param args Argument Map from GraphQL
   * @return Elasticsearch Bool Query containing ANDed (MUSTed) term queries
   */
  public static BoolQueryBuilder queryFromArgs(
      Map<String, Function<String, AbstractQueryBuilder<?>>> QUERY_RESOLVER,
      Map<String, Object> args) {
    val bool = QueryBuilders.boolQuery();
    args.forEach((key, value) -> bool.must(QUERY_RESOLVER.get(key).apply(value.toString())));
    return bool;
  }
}
