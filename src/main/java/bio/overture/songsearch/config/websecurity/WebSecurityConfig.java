package bio.overture.songsearch.config.websecurity;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static java.util.stream.Collectors.toList;


@EnableWebFluxSecurity
@Slf4j
public class WebSecurityConfig {

    AuthProperties authProperties;

    public WebSecurityConfig(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http) {
        http
                .csrf().disable() // graphql endpoint resolves with 403 if csrf is enabled
                .authorizeExchange()
                .pathMatchers("/graphql/**").permitAll() // `hasAuthority` is checked at graphql layer
                .pathMatchers("/actuator/**").permitAll()
            .and()
                .oauth2ResourceServer().jwt()
                    .jwtDecoder(jwtDecoder())
                    .jwtAuthenticationConverter(grantedAuthoritiesExtractor());
        return http.build();
    }


    Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter jwtAuthenticationConverter =
                new JwtAuthenticationConverter();
        jwtAuthenticationConverter
                .setJwtGrantedAuthoritiesConverter(new GrantedAuthoritiesExtractor());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }

    static class GrantedAuthoritiesExtractor  implements Converter<Jwt, Collection<GrantedAuthority>>{
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // TODO check cast
            val context = (Map<String, Object>) jwt.getClaims().get("context");
            val scopes = (Collection<String>) context.get("scope");

            log.debug("Extracted scopes: " + scopes);

            return scopes.stream()
                           .map(SimpleGrantedAuthority::new)
                           .collect(toList());
        }
    }

    @SneakyThrows
    ReactiveJwtDecoder jwtDecoder() {
        val publicKeyStr = authProperties.getJwtPublicKeyStr();
        val publicKeyUrl = authProperties.getJwtPublicKeyUrl();

        // TODO switch entirely to uri, no str
        if (!publicKeyUrl.isBlank()) {
            return NimbusReactiveJwtDecoder.withJwkSetUri(publicKeyUrl).build();
        } else {
            val publicKeyContent = publicKeyStr
                                           .replaceAll("\\n", "")
                                           .replace("-----BEGIN PUBLIC KEY-----", "")
                                           .replace("-----END PUBLIC KEY-----", "");

            KeyFactory kf = KeyFactory.getInstance("RSA");

            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
            RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

            return NimbusReactiveJwtDecoder.withPublicKey(publicKey).build();
        }
    }
}
