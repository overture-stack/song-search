package bio.overture.songsearch.config.constants;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class EsDefaults {
  // Default values from ES pagination:
  // https://www.elastic.co/guide/en/elasticsearch/reference/7.x/paginate-search-results.html
  public static final Integer ES_PAGE_DEFAULT_SIZE = 10;
  public static final Integer ES_PAGE_DEFAULT_FROM = 0;
}
