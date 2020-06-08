package bio.overture.songsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Donor {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String donorId;

    private String submitterDonorId;

    private String sex;

    @SneakyThrows
    public static Donor parse(@NonNull Map<String, Object> sourceMap) {
        return MAPPER.convertValue(sourceMap, Donor.class);
    }
}
