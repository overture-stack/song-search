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

import static bio.overture.songsearch.config.constants.SearchFields.*;
import static bio.overture.songsearch.utils.ElasticsearchQueryUtils.queryFromArgs;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import bio.overture.songsearch.config.ElasticsearchProperties;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.lucene.search.join.ScoreMode;
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
        .put(FILE_OBJECT_ID, value -> new TermQueryBuilder("object_id", value))
        .put(FILE_NAME, value -> new TermQueryBuilder("file.name", value))
        .put(FILE_ACCESS, value -> new TermQueryBuilder("file_access", value))
        .put(FILE_DATA_TYPE, value -> new TermQueryBuilder("data_type", value))
        .put(STUDY_ID, value -> new TermQueryBuilder("study_id", value))
        .put(ANALYSIS_ID, value -> new TermQueryBuilder("analysis.analysis_id", value))
        .put(
            DONOR_ID,
            value ->
                new NestedQueryBuilder(
                    "donors", new TermQueryBuilder("donors.donor_id", value), ScoreMode.None))
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
