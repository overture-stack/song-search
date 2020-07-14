package bio.overture.songsearch.config.websecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@EnableWebFluxSecurity
@Profile("!secure")
public class AuthDisabledConfiguration {
    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http) {
        http
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers("/graphql/**").permitAll()
                .pathMatchers("/actuator/**").permitAll();
        return http.build();
    }
}
