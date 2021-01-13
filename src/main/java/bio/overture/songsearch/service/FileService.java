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

import static bio.overture.songsearch.config.constants.SearchFields.FILE_OBJECT_ID;
import static java.util.stream.Collectors.toUnmodifiableList;

import bio.overture.songsearch.model.File;
import bio.overture.songsearch.repository.FileRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {
  private final FileRepository fileRepository;

  @Autowired
  public FileService(FileRepository fileRepository) {
    this.fileRepository = fileRepository;
  }

  private static File hitToFile(SearchHit hit) {
    val sourceMap = hit.getSourceAsMap();
    return File.parse(sourceMap);
  }

  public List<File> getFiles(Map<String, Object> filter, Map<String, Integer> page) {
    val response = fileRepository.getFiles(filter, page);
    val hitStream = Arrays.stream(response.getHits().getHits());
    return hitStream.map(FileService::hitToFile).collect(toUnmodifiableList());
  }

  public File getFileByObjectId(String fileId) {
    val response = fileRepository.getFiles(Map.of(FILE_OBJECT_ID, fileId), null);
    val runOpt =
        Arrays.stream(response.getHits().getHits()).map(FileService::hitToFile).findFirst();
    return runOpt.orElse(null);
  }
}
