package bio.overture.songsearch.graphql;

import com.apollographql.federation.graphqljava.Federation;
import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

@Slf4j
@Service
public class GraphQLProvider {

  private GraphQL graphQL;
  private GraphQLSchema graphQLSchema;

  public GraphQLProvider() {}

  @Bean
  public GraphQL graphQL() {
    return graphQL;
  }

  @PostConstruct
  public void init() throws IOException {
    URL url = Resources.getResource("schema.graphql");
    String sdl = Resources.toString(url, Charsets.UTF_8);
    graphQLSchema = buildSchema(sdl);
    this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
  }

  private GraphQLSchema buildSchema(String sdl) {
    return Federation.transform(sdl, buildWiring())
        .build();
  }

  private RuntimeWiring buildWiring() {
    return RuntimeWiring.newRuntimeWiring().scalar(ExtendedScalars.Json).build();
  }
}
