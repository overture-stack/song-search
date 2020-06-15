package bio.overture.songsearch.graphql;

import bio.overture.songsearch.model.Run;
import bio.overture.songsearch.service.AnalysisService;
import bio.overture.songsearch.service.FileService;
import com.apollographql.federation.graphqljava._Entity;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class EntityDataFetcher {
    public static final String ANALYSIS_ENTITY = "Analysis";
    public static final String FILE_ENTITY = "File";
    public static final String RUN_ENTITY = "Run";

    private final AnalysisService analysisService;
    private final FileService fileService;

    @Autowired
    public EntityDataFetcher(AnalysisService analysisService, FileService fileService) {
        this.analysisService = analysisService;
        this.fileService = fileService;
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
                            final Object runName = values.get("runName");
                            if (runName instanceof String) {
                                return new Run((String) runName, analysisService.getAnalysisByRunName((String) runName));
                            }
                        }
                        return null;
                    })
                .collect(toList());
    }
}
