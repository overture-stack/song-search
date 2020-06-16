package bio.overture.songsearch.graphql;

import bio.overture.songsearch.model.File;
import bio.overture.songsearch.service.FileService;
import com.google.common.collect.ImmutableMap;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FileDataFetcher {
  private final FileService fileService;

  @Autowired
  public FileDataFetcher(FileService fileService) {
    this.fileService = fileService;
  }

  @SuppressWarnings("unchecked")
  public DataFetcher<List<File>> getFilesDataFetcher() {
    return environment -> {
      val args = environment.getArguments();

      val filter = ImmutableMap.<String, Object>builder();
      val page = ImmutableMap.<String, Integer>builder();

      if (args != null) {
        if (args.get("filter") != null) filter.putAll((Map<String, Object>) args.get("filter"));
        if (args.get("page") != null) page.putAll((Map<String, Integer>) args.get("page"));
      }
      return fileService.getFiles(filter.build(), page.build());
    };
  }
}
