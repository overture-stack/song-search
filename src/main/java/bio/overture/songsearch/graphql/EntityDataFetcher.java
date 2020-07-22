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

package bio.overture.songsearch.graphql;

import bio.overture.songsearch.config.SongSearchProperties;
import bio.overture.songsearch.model.Run;
import bio.overture.songsearch.service.AnalysisService;
import bio.overture.songsearch.service.FileService;
import com.apollographql.federation.graphqljava._Entity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class EntityDataFetcher {
  public static final String ANALYSIS_ENTITY = "Analysis";
  public static final String FILE_ENTITY = "File";
  public static final String RUN_ENTITY = "Run";

  private final AnalysisService analysisService;
  private final FileService fileService;
  private final SongSearchProperties songSearchProperties;

  @Autowired
  public EntityDataFetcher(AnalysisService analysisService, FileService fileService, SongSearchProperties songSearchProperties) {
    this.analysisService = analysisService;
    this.fileService = fileService;
    this.songSearchProperties = songSearchProperties;
  }

  public DataFetcher getDataFetcher() {
    return environment ->
        environment.<List<Map<String, Object>>>getArgument(_Entity.argumentName).stream()
            .map(
                values -> {
                  if (ANALYSIS_ENTITY.equals(values.get("__typename"))) {
                    final Object analysisId = values.get("analysisId");
                    if (analysisId instanceof String) {
                      return analysisService.getAnalysisById((String) analysisId);
                    }
                  }
                  if (FILE_ENTITY.equals(values.get("__typename"))) {
                    final Object fileObjectId = values.get("objectId");
                    if (fileObjectId instanceof String) {
                      return fileService.getFileByObjectId((String) fileObjectId);
                    }
                  }
                  if (RUN_ENTITY.equals(values.get("__typename"))) {
                    final Object runId = values.get("runId");
                    val ids = getRelevantAnalysisIdsFromRunParameters(values.get("parameters"));

                    if (runId instanceof String) {
                      return new Run(
                          (String) runId,
                          analysisService.getAnalysesByRunId((String) runId),
                          analysisService.getAnalysesById(ids)
                          );
                    }
                  }
                  return null;
                })
            .collect(toList());
  }

    private List<String> getRelevantAnalysisIdsFromRunParameters(Object parametersObj) {
      val parametersBuilder = ImmutableMap.<String, Object>builder();
      if (parametersObj instanceof Map) {
          try {
              val parametersMap = (Map<String, Object>) parametersObj;
              parametersBuilder.putAll(parametersMap);
          } catch (ClassCastException e) {
              log.error("Failed to cast parametersObj to Map<String, Object>");
          }
      }
      val parameters = parametersBuilder.build();

      ImmutableList<String> analysisIdKeysToLookFor = songSearchProperties.getWorkflowRunParameterKeys().getAnalysisId();

      return analysisIdKeysToLookFor.stream()
                                .map(parameters::get)
                                .filter(Objects::nonNull)
                                .map(Objects::toString)
                                .collect(toList());
  }
}
