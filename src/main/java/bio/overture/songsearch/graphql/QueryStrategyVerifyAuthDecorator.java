package bio.overture.songsearch.graphql;

import graphql.*;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategy;
import graphql.execution.ExecutionStrategyParameters;
import graphql.language.Field;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class QueryStrategyVerifyAuthDecorator extends ExecutionStrategy {

    private final List<String> readOnly;
    private final ExecutionStrategy decoratedStrategy;

    public QueryStrategyVerifyAuthDecorator(ExecutionStrategy decoratedStrategy, List<String> readOnly) {
        this.decoratedStrategy = decoratedStrategy;
        this.readOnly = readOnly;
    }

    @Override
    @SneakyThrows
    public CompletableFuture<ExecutionResult> execute(
            ExecutionContext executionContext,
            ExecutionStrategyParameters executionStrategyParameters)
    {
        if (isApolloFederationServiceCapabilityQuery(executionContext)) {
            // the apollo federation requests are queries but they don't need to be authorized so no need to check
            return decoratedStrategy.execute(executionContext, executionStrategyParameters);
        }

        Object contextObject = executionContext.getContext();
        List<GrantedAuthority> scopes = new java.util.ArrayList<>(Collections.emptyList());

        if (contextObject instanceof SecurityContext) {
            // TODO Try cast
            SecurityContext securityContext = (SecurityContext) contextObject;
            scopes.addAll(securityContext.getAuthentication().getAuthorities());
        }

        val foundScopes = scopes.stream()
                                  .map(GrantedAuthority::getAuthority)
                                  .filter(readOnly::contains)
                                  .collect(Collectors.toUnmodifiableList());

        log.debug("Found scopes in decorator: " + foundScopes.toString());

        if (foundScopes.size() <= 0) {
            val deniedError = GraphqlErrorBuilder.newError()
                                      .message("Permission Denied")
                                      .extensions(Map.of("status", "403"))
                                      .build();
            ExecutionResult result = new ExecutionResultImpl(deniedError);
            return CompletableFuture.completedFuture(result);
        }

        return decoratedStrategy.execute(executionContext, executionStrategyParameters);
    }

    // As per the apollo federation spec (https://www.apollographql.com/docs/apollo-server/federation/federation-spec/#fetch-service-capabilities),
    // each service in a federation needs to provide service capability information represented as `SDL` via the `_Service` query.
    // This function will check if operation is from Apollo and if the query in the executionContext matches:
    //    query {
    //          _Service {
    //              sdl
    //          }
    //     }
    private boolean isApolloFederationServiceCapabilityQuery(ExecutionContext executionContext) {
        final String federationExpectedOperationName = "__ApolloGetServiceDefinition__";
        final String federationExpectedFirstLevelQuery = "_service";
        final String federationExpectedSecondLevelQuery = "sdl";

        // TODO check casting & lower case string compare
        val operationName = executionContext.getOperationDefinition().getName();
        if (operationName == null ||
                    !operationName.equals(federationExpectedOperationName)) {
            return false;
        }

        val firstLevelQueryFields = executionContext.getOperationDefinition().getSelectionSet().getChildren();
        Field firstLevelQueryField = (Field) firstLevelQueryFields.stream().findFirst().get();
        if (firstLevelQueryFields.size() > 1 ||
                    !firstLevelQueryField.getName().equals(federationExpectedFirstLevelQuery)) {
            return false;
        }

        val secondLevelQueryFields = firstLevelQueryField.getSelectionSet().getChildren();
        Field secondLevelQueryField = (Field) secondLevelQueryFields.stream().findFirst().get();
        if (secondLevelQueryFields.size() > 1 ||
                    !secondLevelQueryField.getName().equals(federationExpectedSecondLevelQuery)) {
            return false;
        }

        return true;
    }
}
