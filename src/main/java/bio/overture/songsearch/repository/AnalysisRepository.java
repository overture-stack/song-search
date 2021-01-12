/*
 * Copyright (c) 2020 The Ontario Institute for Cancer Research. All rights reserved
 *
 * This program and the accompanying materials are made available under the terms of the GNU Affero General Public License v3.0.
 * You should have received a copy of the GNU Affero General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package bio.overture.songsearch.repository;

import static bio.overture.songsearch.config.SearchFields.*;
import static bio.overture.songsearch.utils.ElasticsearchQueryUtils.queryFromArgs;
import static bio.overture.songsearch.utils.ElasticsearchQueryUtils.sortsToEsSortBuilders;
import static java.util.Collections.emptyList;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import bio.overture.songsearch.config.ElasticsearchProperties;
import bio.overture.songsearch.model.Sort;
import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnalysisRepository {
  private static final Map<String, Function<String, AbstractQueryBuilder<?>>> QUERY_RESOLVER =
      argumentPathMap();

  private static final Map<String, FieldSortBuilder> SORT_BUILDER_RESOLVERS = sortBuilderMap();

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
    val immutableMap = ImmutableMap.<String, Function<String, AbstractQueryBuilder<?>>>builder();

    ANALYSIS_QUERY_TO_ES_DOC_PATHS.forEach(
        (k, fieldPath) -> {
          immutableMap.put(k, value -> new TermQueryBuilder(fieldPath, value));
        });

    ANALYSIS_QUERY_TO_ES_NESTED_DOC_PATHS.forEach(
        (k, nestedFieldPath) -> {
          immutableMap.put(
              k,
              value ->
                  new NestedQueryBuilder(
                      nestedFieldPath.getObjectPath(),
                      new TermQueryBuilder(nestedFieldPath.getFieldPath(), value),
                      ScoreMode.None));
        });

    return immutableMap.build();
  }

  private static Map<String, FieldSortBuilder> sortBuilderMap() {
    val immutableMap = ImmutableMap.<String, FieldSortBuilder>builder();

    ANALYSIS_QUERY_TO_ES_DOC_PATHS.forEach(
        (k, v) -> immutableMap.put(k, SortBuilders.fieldSort(v)));

    ANALYSIS_QUERY_TO_ES_NESTED_DOC_PATHS.forEach(
        (k, v) -> {
          val sortBuilder =
              SortBuilders.fieldSort(v.getFieldPath())
                  .setNestedSort(new NestedSortBuilder(v.getObjectPath()));
          immutableMap.put(k, sortBuilder);
        });

    return immutableMap.build();
  }

  public SearchResponse getAnalyses(Map<String, Object> filter, Map<String, Integer> page) {
    return getAnalyses(filter, page, emptyList());
  }

  public SearchResponse getAnalyses(
      Map<String, Object> filter, Map<String, Integer> page, List<Sort> sorts) {
    final AbstractQueryBuilder<?> query =
        (filter == null || filter.size() == 0)
            ? matchAllQuery()
            : queryFromArgs(QUERY_RESOLVER, filter);

    val searchSourceBuilder = createSearchSourceBuilder(query, page, sorts);

    return execute(searchSourceBuilder);
  }

  public MultiSearchResponse getAnalyses(
      List<Map<String, Object>> multipleFilters, Map<String, Integer> page) {
    List<SearchSourceBuilder> searchSourceBuilders =
        multipleFilters.stream()
            .filter(f -> f != null && f.size() != 0)
            .map(f -> createSearchSourceBuilder(queryFromArgs(QUERY_RESOLVER, f), page))
            .collect(Collectors.toList());

    if (searchSourceBuilders.isEmpty()) {
      searchSourceBuilders.add(createSearchSourceBuilder(matchAllQuery(), page));
    }

    return execute(searchSourceBuilders);
  }

  private SearchSourceBuilder createSearchSourceBuilder(
      AbstractQueryBuilder<?> query, Map<String, Integer> page) {
    return createSearchSourceBuilder(query, page, emptyList());
  }

  private SearchSourceBuilder createSearchSourceBuilder(
      AbstractQueryBuilder<?> query, Map<String, Integer> page, List<Sort> sorts) {
    val searchSourceBuilder = new SearchSourceBuilder();

    if (sorts.isEmpty()) {
      searchSourceBuilder.sort(ANALYSIS_QUERY_TO_ES_DOC_PATHS.get(ANALYSIS_ID), SortOrder.ASC);
    } else {
      val sortBuilders = sortsToEsSortBuilders(SORT_BUILDER_RESOLVERS, sorts);
      sortBuilders.forEach(searchSourceBuilder::sort);
    }

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
