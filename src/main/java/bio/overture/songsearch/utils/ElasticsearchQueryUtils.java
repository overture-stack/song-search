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

package bio.overture.songsearch.utils;

import static bio.overture.songsearch.config.SearchFields.*;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.songsearch.model.NestedFieldPath;
import bio.overture.songsearch.model.Sort;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.val;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

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
    args.forEach(
        (key, value) ->
            bool.must(
                QUERY_RESOLVER
                    .getOrDefault(key, simpleTermQueryBuilderResolver(key))
                    .apply(value.toString())));
    return bool;
  }

  /**
   * For each sorts, find its SortBuilder and add the sort order value then collect them all in a
   * list
   *
   * @param sorts List of Sort objects
   * @return List of FiledSortBuilder
   */
  public static List<FieldSortBuilder> sortsToEsSortBuilders(
      Map<String, FieldSortBuilder> SORT_BUILDER_RESOLVER, List<Sort> sorts) {
    return sorts.stream()
        .map(
            sort -> {
              val sortBuilder = SORT_BUILDER_RESOLVER.get(sort.getFieldName());
              return sortBuilder.order(SortOrder.fromString(sort.getOrder()));
            })
        .collect(toUnmodifiableList());
  }

  public static Map<String, Function<String, AbstractQueryBuilder<?>>> createQueryResolver(
      Map<String, String> queryToEsDocPaths, Map<String, NestedFieldPath> queryToNestedEsDocPaths) {
    val immutableMap = ImmutableMap.<String, Function<String, AbstractQueryBuilder<?>>>builder();

    queryToEsDocPaths.forEach(
        (k, fieldPath) -> immutableMap.put(k, value -> new TermQueryBuilder(fieldPath, value)));

    queryToNestedEsDocPaths.forEach(
        (k, nestedFieldPath) ->
            immutableMap.put(
                k,
                value ->
                    new NestedQueryBuilder(
                        nestedFieldPath.getObjectPath(),
                        new TermQueryBuilder(nestedFieldPath.getFieldPath(), value),
                        ScoreMode.None)));

    return immutableMap.build();
  }

  public static Map<String, FieldSortBuilder> createFieldSortBuilderResolver(
      Map<String, String> sortFieldToEsDocPaths,
      Map<String, NestedFieldPath> sortFieldToNestedEsDocPaths) {
    val immutableMap = ImmutableMap.<String, FieldSortBuilder>builder();

    sortFieldToEsDocPaths.forEach((k, v) -> immutableMap.put(k, SortBuilders.fieldSort(v)));

    sortFieldToNestedEsDocPaths.forEach(
        (k, v) -> {
          val sortBuilder =
              SortBuilders.fieldSort(v.getFieldPath())
                  .setNestedSort(new NestedSortBuilder(v.getObjectPath()));
          immutableMap.put(k, sortBuilder);
        });

    return immutableMap.build();
  }

  private static Function<String, AbstractQueryBuilder<?>> simpleTermQueryBuilderResolver(
      String key) {
    return v -> new TermQueryBuilder(key, v);
  }
}
