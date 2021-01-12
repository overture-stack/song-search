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

package bio.overture.songsearch.config;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.Value;

@NoArgsConstructor(access = PRIVATE)
public class SearchFields {
  public static final String ANALYSIS_ID = "analysisId";
  public static final String ANALYSIS_TYPE = "analysisType";
  public static final String ANALYSIS_VERSION = "analysisVersion";
  public static final String ANALYSIS_STATE = "analysisState";
  public static final String FILE_OBJECT_ID = "objectId";
  public static final String FILE_NAME = "name";
  public static final String FILE_ACCESS = "fileAccess";
  public static final String FILE_DATA_TYPE = "dataType";
  public static final String STUDY_ID = "studyId";
  public static final String DONOR_ID = "donorId";
  public static final String SPECIMEN_ID = "specimenId";
  public static final String SAMPLE_ID = "sampleId";
  public static final String SUBMITTER_SAMPLE_ID = "submitterSampleId";
  public static final String MATCHED_NORMAL_SUBMITTER_SAMPLE_ID = "matchedNormalSubmitterSampleId";
  public static final String RUN_ID = "runId";

  public static final Map<String, String> ANALYSIS_QUERY_TO_ES_DOC_PATHS =
      ImmutableMap.copyOf(
          ofEntries(
              entry(ANALYSIS_ID, "analysis_id"),
              entry(ANALYSIS_TYPE, "analysis_type"),
              entry(ANALYSIS_VERSION, "analysis_version"),
              entry(ANALYSIS_STATE, "analysis_state"),
              entry(STUDY_ID, "study_id"),
              entry(RUN_ID, "workflow.run_id")));

  public static final Map<String, NestedFieldPath> ANALYSIS_QUERY_TO_ES_NESTED_DOC_PATHS =
      ImmutableMap.copyOf(
          ofEntries(
              entry(DONOR_ID, new NestedFieldPath("donors", "donors.donor_id")),
              entry(
                  SPECIMEN_ID,
                  new NestedFieldPath("donors.specimens", "donors.specimens.specimen_id")),
              entry(
                  SAMPLE_ID,
                  new NestedFieldPath(
                      "donors.specimens.samples", "donors.specimens.samples.sample_id")),
              entry(
                  MATCHED_NORMAL_SUBMITTER_SAMPLE_ID,
                  new NestedFieldPath(
                      "donors.specimens.samples",
                      "donors.specimens.samples.matched_normal_submitter_sample_id")),
              entry(
                  SUBMITTER_SAMPLE_ID,
                  new NestedFieldPath(
                      "donors.specimens.samples",
                      "donors.specimens.samples.submitter_sample_id"))));

  @Value
  public static class NestedFieldPath {
    String objectPath;
    String fieldPath;
  }
}
