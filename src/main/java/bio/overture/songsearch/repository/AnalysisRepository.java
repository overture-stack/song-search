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
import static bio.overture.songsearch.utils.ElasticsearchQueryUtils.*;
import static java.util.Collections.emptyList;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.search.sort.SortOrder.ASC;

import bio.overture.songsearch.config.ElasticsearchProperties;
import bio.overture.songsearch.model.NestedFieldPath;
import bio.overture.songsearch.model.Sort;
import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnalysisRepository {
  private static final Map<String, String> ANALYSIS_QUERY_TO_ES_DOC_PATHS =
      ImmutableMap.copyOf(
          Map.of(
              ANALYSIS_ID, "analysis_id",
              ANALYSIS_TYPE, "analysis_type",
              ANALYSIS_VERSION,"analysis_version",
              ANALYSIS_STATE,"analysis_state",
              STUDY_ID,"study_id",
              RUN_ID,"workflow.run_id"));

  private static final Map<String, NestedFieldPath> ANALYSIS_QUERY_TO_NESTED_ES_DOC_PATHS =
      ImmutableMap.of(
          DONOR_ID, new NestedFieldPath("donors", "donors.donor_id"),
          SPECIMEN_ID, new NestedFieldPath("donors.specimens", "donors.specimens.specimen_id"),
          SAMPLE_ID,
          new NestedFieldPath("donors.specimens.samples", "donors.specimens.samples.sample_id"),
          MATCHED_NORMAL_SUBMITTER_SAMPLE_ID,
          new NestedFieldPath(
          "donors.specimens.samples",
          "donors.specimens.samples.matched_normal_submitter_sample_id"),
          SUBMITTER_SAMPLE_ID,
          new NestedFieldPath(
              "donors.specimens.samples", "donors.specimens.samples.submitter_sample_id"));

  private static final Map<String, String> ANALYSIS_SORT_TO_ES_DOC_PATHS =
      ImmutableMap.of(
          ANALYSIS_ID, "analysis_id",
          ANALYSIS_STATE, "analysis_state",
          PUBLISHED_AT, "published_at",
          UPDATED_AT, "updated_at",
          FIRST_PUBLISHED_AT, "first_published_at");

  private static final Map<String, Function<String, AbstractQueryBuilder<?>>> QUERY_RESOLVER =
      createQueryResolver(ANALYSIS_QUERY_TO_ES_DOC_PATHS, ANALYSIS_QUERY_TO_NESTED_ES_DOC_PATHS);

  private static final Map<String, FieldSortBuilder> SORT_BUILDER_RESOLVER =
      createFieldSortBuilderResolver(ANALYSIS_SORT_TO_ES_DOC_PATHS, Map.of());

  private final RestHighLevelClient client;
  private final String analysisCentricIndex;

  @Autowired
  public AnalysisRepository(
      @NonNull RestHighLevelClient client,
      @NonNull ElasticsearchProperties elasticSearchProperties) {
    this.client = client;
    this.analysisCentricIndex = elasticSearchProperties.getAnalysisCentricIndex();
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
      searchSourceBuilder.sort(SORT_BUILDER_RESOLVER.get(ANALYSIS_ID).order(ASC));
    } else {
      val sortBuilders = sortsToEsSortBuilders(SORT_BUILDER_RESOLVER, sorts);
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
