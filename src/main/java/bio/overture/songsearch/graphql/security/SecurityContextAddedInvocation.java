package bio.overture.songsearch.graphql.security;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.spring.web.reactive.GraphQLInvocation;
import graphql.spring.web.reactive.GraphQLInvocationData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// See original code: https://github.com/graphql-java/graphql-java-spring/blob/v1.0/graphql-java-spring-webflux/src/main/java/graphql/spring/web/reactive/components/DefaultGraphQLInvocation.java
// This class is a work around for this issue: https://github.com/graphql-java/graphql-java-spring/issues/8
// The problem at a high level is that the graphql-java engine looses the ReactiveSecurityContext when it executes async.
// This work around, adds the security context to the graphql execution context so it's not lost and can be used for auth check.
// This only occurs if auth is enabled.

@Component
@Primary
@Slf4j
@Profile("secure")
public class SecurityContextAddedInvocation implements GraphQLInvocation {

    @Autowired
    GraphQL graphQL;

    @Override
    public Mono<ExecutionResult> invoke(GraphQLInvocationData invocationData, ServerWebExchange serverWebExchange) {
        ExecutionInput.Builder executionInputBuilder = ExecutionInput.newExecutionInput()
                                                               .query(invocationData.getQuery())
                                                               .operationName(invocationData.getOperationName())
                                                               .variables(invocationData.getVariables());

        Mono<ExecutionInput> customizedExecutionInputMono = addReactiveSecurityContextToExecutionInput(executionInputBuilder);
        return customizedExecutionInputMono.flatMap(customizedExecutionInput -> Mono.fromCompletionStage(graphQL.executeAsync(customizedExecutionInput)));
    }

    @SneakyThrows
    public Mono<ExecutionInput> addReactiveSecurityContextToExecutionInput(ExecutionInput.Builder executionInputBuilder) {
        log.debug("Adding Reactive Security Context To Execution Input");
        Mono<SecurityContext> securityContextMono = ReactiveSecurityContextHolder.getContext();

        return securityContextMono
                       .map(securityContext -> executionInputBuilder.context(securityContext).build())
                       .switchIfEmpty(Mono.just(executionInputBuilder.build()));
    }
}
