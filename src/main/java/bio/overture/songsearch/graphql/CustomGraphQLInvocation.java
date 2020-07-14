package bio.overture.songsearch.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.spring.web.reactive.GraphQLInvocation;
import graphql.spring.web.reactive.GraphQLInvocationData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


// This is a work around for this issue: https://github.com/graphql-java/graphql-java-spring/issues/8
// See original code: https://github.com/graphql-java/graphql-java-spring/blob/v1.0/graphql-java-spring-webflux/src/main/java/graphql/spring/web/reactive/components/DefaultGraphQLInvocation.java


@Component
@Primary
@Slf4j
public class CustomGraphQLInvocation implements GraphQLInvocation {

    @Autowired
    GraphQL graphQL;

    @Override
    public Mono<ExecutionResult> invoke(GraphQLInvocationData invocationData, ServerWebExchange serverWebExchange) {
        ExecutionInput.Builder executionInputBuilder = ExecutionInput.newExecutionInput()
                                                               .query(invocationData.getQuery())
                                                               .operationName(invocationData.getOperationName())
                                                               .variables(invocationData.getVariables());

        Mono<ExecutionInput> customizedExecutionInputMono = customizeExecutionInput(executionInputBuilder);

        return customizedExecutionInputMono.flatMap(customizedExecutionInput -> Mono.fromCompletionStage(graphQL.executeAsync(customizedExecutionInput)));
    }

    @SneakyThrows
    public Mono<ExecutionInput> customizeExecutionInput(ExecutionInput.Builder executionInputBuilder) {
        log.info("Customizing Execution Input");
        Mono<SecurityContext> securityContextMono = ReactiveSecurityContextHolder.getContext();

        return securityContextMono
                       .log()
                       .map(securityContext -> {
                                    log.info(securityContext.toString());
                                    return executionInputBuilder.context(securityContext).build();
                       })
                       .switchIfEmpty(Mono.just(executionInputBuilder.build()));
    }
}
