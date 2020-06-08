package bio.overture.songsearch.service;

import bio.overture.songsearch.model.File;
import bio.overture.songsearch.repository.FileRepository;
import lombok.val;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static bio.overture.songsearch.config.SearchFields.FILE_ID;
import static java.util.stream.Collectors.toUnmodifiableList;

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

  public File getFileById(String fileId) {
    val response = fileRepository.getFiles(Map.of(FILE_ID, fileId), null);
    val runOpt =
        Arrays.stream(response.getHits().getHits()).map(FileService::hitToFile).findFirst();
    return runOpt.orElse(null);
  }
}
