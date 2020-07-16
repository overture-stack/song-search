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

package bio.overture.songsearch.service;

import bio.overture.songsearch.model.Analysis;
import bio.overture.songsearch.repository.AnalysisRepository;
import lombok.val;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bio.overture.songsearch.config.SearchFields.ANALYSIS_ID;
import static bio.overture.songsearch.config.SearchFields.RUN_ID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

@Service
public class AnalysisService {

  private final AnalysisRepository analysisRepository;

  @Autowired
  public AnalysisService(AnalysisRepository analysisRepository) {
    this.analysisRepository = analysisRepository;
  }

  private static Analysis hitToAnalysis(SearchHit hit) {
    val sourceMap = hit.getSourceAsMap();
    return Analysis.parse(sourceMap);
  }

  public List<Analysis> getAnalyses(Map<String, Object> filter, Map<String, Integer> page) {
    val response = analysisRepository.getAnalyses(filter, page);
    val hitStream = Arrays.stream(response.getHits().getHits());
    return hitStream.map(AnalysisService::hitToAnalysis).collect(toUnmodifiableList());
  }

  public Analysis getAnalysisById(String analysisId) {
    val response = analysisRepository.getAnalyses(Map.of(ANALYSIS_ID, analysisId), null);
    val runOpt =
        Arrays.stream(response.getHits().getHits()).map(AnalysisService::hitToAnalysis).findFirst();
    return runOpt.orElse(null);
  }

  public List<Analysis> getAnalysesByRunId(String runId) {
    val response = analysisRepository.getAnalyses(Map.of(RUN_ID, runId), null);
    val hitStream = Arrays.stream(response.getHits().getHits());
    return hitStream.map(AnalysisService::hitToAnalysis).collect(toUnmodifiableList());
  }

 public List<Analysis> getAnalysesById(List<String> analysisIds) {
    val multipleFilters = analysisIds.stream().map(id -> Map.of(ANALYSIS_ID, (Object) id)).collect(toList());
    val multiSearchResponse = analysisRepository.getAnalyses(multipleFilters, null);
    return Arrays.stream(multiSearchResponse.getResponses())
                            .map(MultiSearchResponse.Item::getResponse)
                            .map(res -> Arrays.stream(res.getHits().getHits())
                                              .map(AnalysisService::hitToAnalysis)
                                              .findFirst())
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(toUnmodifiableList());
 }
}
