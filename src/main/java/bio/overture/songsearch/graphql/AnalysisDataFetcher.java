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

import static bio.overture.songsearch.config.SearchFields.*;
import static bio.overture.songsearch.model.enums.SpecimenType.NORMAL;
import static bio.overture.songsearch.model.enums.SpecimenType.TUMOUR;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.songsearch.model.Analysis;
import bio.overture.songsearch.model.Sample;
import bio.overture.songsearch.model.SampleMatchedAnalysisPair;
import bio.overture.songsearch.service.AnalysisService;
import com.google.common.collect.ImmutableMap;
import graphql.AssertException;
import graphql.GraphQLException;
import graphql.schema.DataFetcher;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnalysisDataFetcher {

  private final AnalysisService analysisService;

  @Autowired
  public AnalysisDataFetcher(AnalysisService analysisService) {
    this.analysisService = analysisService;
  }

  @SuppressWarnings("unchecked")
  public DataFetcher<List<Analysis>> getAnalysesDataFetcher() {
    return environment -> {
      val args = environment.getArguments();

      val filter = ImmutableMap.<String, Object>builder();
      val page = ImmutableMap.<String, Integer>builder();

      if (args != null) {
        if (args.get("filter") != null) filter.putAll((Map<String, Object>) args.get("filter"));
        if (args.get("page") != null) page.putAll((Map<String, Integer>) args.get("page"));
      }
      return analysisService.getAnalyses(filter.build(), page.build());
    };
  }

  public DataFetcher<List<SampleMatchedAnalysisPair>> getSampleMatchedAnalysisPairsFetcher() {
    return env -> {
      val analysisId = env.getArguments().get("analysisId").toString();

      val analysisFromId = analysisService.getAnalysisById(analysisId);
      val samples = getFlattenedSamplesFromAnalysis(analysisFromId);

      validateAnalysisAndSamplesValidForQuery(analysisFromId, samples);

      val flattenedSampleOfInterest = (FlatDonorSample) samples.get(0);
      val analysisType = analysisFromId.getAnalysisType();
      val experimentalStrategy = analysisFromId.getExperiment().get("experimental_strategy");
      val tnDesignation = flattenedSampleOfInterest.getTumourNormalDesignation();

      val filter = ImmutableMap.<String, Object>builder();
      filter.putAll(createTumourOrNormalSubmitterIdFilter(flattenedSampleOfInterest));
      filter.put(ANALYSIS_TYPE, analysisType);
      filter.put("experiment.experimental_strategy", experimentalStrategy);

      val matchedAnalyses = analysisService.getAnalyses(filter.build(), null);

      return matchedAnalyses.stream()
                     .map(a -> tnDesignation.equalsIgnoreCase(TUMOUR.toString()) ?
                                       new SampleMatchedAnalysisPair(a, analysisFromId) :
                                       new SampleMatchedAnalysisPair(analysisFromId, a))
                     .collect(Collectors.toUnmodifiableList());
    };
  }

  @SneakyThrows
  private void validateAnalysisAndSamplesValidForQuery(Analysis analysis, List<FlatDonorSample> flatSamples) {
    if (analysis.getExperiment().get("experimental_strategy") == null) {
      throw new GraphQLException("Can't find matched T/N analyses for this analysis because it has no experimental_strategy!");
    }
    if (flatSamples.size() != 1) {
      throw new GraphQLException("Can't find matched T/N analyses for this analysis because it has multiple or no samples!");
    }
  }

  private Map<String, String> createTumourOrNormalSubmitterIdFilter(FlatDonorSample flatDonorSample) {
    if (flatDonorSample.getTumourNormalDesignation().equalsIgnoreCase(TUMOUR.toString())) {
      return Map.of(SUBMITTER_SAMPLE_ID, flatDonorSample.getMatchedNormalSubmitterSampleId());
    } else if (flatDonorSample.getTumourNormalDesignation().equalsIgnoreCase(NORMAL.toString())) {
      return Map.of(MATCHED_NORMAL_SUBMITTER_SAMPLE_ID, flatDonorSample.getSubmitterSampleId());
    }
    return Map.of();
  }

  private List<FlatDonorSample> getFlattenedSamplesFromAnalysis(Analysis analysis) {
    return analysis.getDonors().stream().flatMap(
            d -> d.getSpecimens().stream().flatMap(
                    sp -> {
                      val designation = sp.getTumourNormalDesignation();
                      return sp.getSamples().stream().map(sam -> new FlatDonorSample(sam, designation));
                    }))
                   .collect(toUnmodifiableList());
  }

  @Value
  static class FlatDonorSample {
    String tumourNormalDesignation;
    String submitterSampleId;
    String matchedNormalSubmitterSampleId;

    FlatDonorSample(Sample sample, String tumourNormalDesignation) {
      this.tumourNormalDesignation = tumourNormalDesignation;
      this.submitterSampleId = sample.getSubmitterSampleId();
      this.matchedNormalSubmitterSampleId = sample.getMatchedNormalSubmitterSampleId();
    }
  }
}
