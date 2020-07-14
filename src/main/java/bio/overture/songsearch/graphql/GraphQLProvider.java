package bio.overture.songsearch.graphql;

import bio.overture.songsearch.config.websecurity.AuthProperties;
import bio.overture.songsearch.graphql.security.VerifyAuthQueryExecutionStrategyDecorator;
import bio.overture.songsearch.model.Analysis;
import bio.overture.songsearch.model.File;
import bio.overture.songsearch.model.Run;
import com.apollographql.federation.graphqljava.Federation;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@Slf4j
@Service
public class GraphQLProvider {

  private final AnalysisDataFetcher analysisDataFetcher;
  private final FileDataFetcher fileDataFetcher;
  private final EntityDataFetcher entityDataFetcher;
  private final AuthProperties authProperties;
  private GraphQL graphQL;
  private GraphQLSchema graphQLSchema;

  @Autowired
  public GraphQLProvider(
          AnalysisDataFetcher analysisDataFetcher,
          FileDataFetcher fileDataFetcher,
          EntityDataFetcher entityDataFetcher,
          AuthProperties authProperties) {
    this.analysisDataFetcher = analysisDataFetcher;
    this.fileDataFetcher = fileDataFetcher;
    this.entityDataFetcher = entityDataFetcher;
    this.authProperties = authProperties;
  }

  @Bean
  public GraphQL graphQL() {
    return graphQL;
  }

  @PostConstruct
  public void init() throws IOException {
    URL url = Resources.getResource("schema.graphql");
    String sdl = Resources.toString(url, Charsets.UTF_8);
    graphQLSchema = buildSchema(sdl);
    this.graphQL = buildGraphql(graphQLSchema);
  }

  private GraphQL buildGraphql(GraphQLSchema graphQLSchema) {
      val graphQLBuilder = GraphQL.newGraphQL(graphQLSchema);
      if (authProperties.isEnabled()) {
          // For more info on `Execution Strategies` see: https://www.graphql-java.com/documentation/v15/execution/
          graphQLBuilder.queryExecutionStrategy(
                  new VerifyAuthQueryExecutionStrategyDecorator(new AsyncExecutionStrategy(), queryScopesToCheck())
          );
      }
      return graphQLBuilder.build();
  }

  private GraphQLSchema buildSchema(String sdl) {
    return Federation.transform(sdl, buildWiring())
        .fetchEntities(entityDataFetcher.getDataFetcher())
        .resolveEntityType(
            typeResolutionEnvironment -> {
              final Object src = typeResolutionEnvironment.getObject();
              if (src instanceof Analysis) {
                return typeResolutionEnvironment
                    .getSchema()
                    .getObjectType(EntityDataFetcher.ANALYSIS_ENTITY);
              }
              if (src instanceof File) {
                return typeResolutionEnvironment
                    .getSchema()
                    .getObjectType(EntityDataFetcher.FILE_ENTITY);
              }
              if (src instanceof Run) {
                return typeResolutionEnvironment
                    .getSchema()
                    .getObjectType(EntityDataFetcher.RUN_ENTITY);
              }
              return null;
            })
        .build();
  }

  private RuntimeWiring buildWiring() {
    return RuntimeWiring.newRuntimeWiring()
        .scalar(ExtendedScalars.Json)
        .type(
            newTypeWiring("Query")
                .dataFetcher("analyses", analysisDataFetcher.getAnalysesDataFetcher()))
        .type(
            newTypeWiring("Query")
                .dataFetcher("files", fileDataFetcher.getFilesDataFetcher()))
        .build();
  }

  private ImmutableList<String> queryScopesToCheck() {
      return ImmutableList.copyOf(
              Iterables.concat(
                      authProperties.getGraphqlScopes().getQueryOnly(),
                      authProperties.getGraphqlScopes().getQueryAndMutation()
              ));

  }
}
