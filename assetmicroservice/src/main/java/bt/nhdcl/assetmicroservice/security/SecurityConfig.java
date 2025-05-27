package bt.nhdcl.assetmicroservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/assets/find").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/assets/academy/{academyID}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/assets/upload/excel").hasAuthority("Manager")
                        .requestMatchers(HttpMethod.POST, "/api/assets/update-floor-rooms").hasAuthority("Manager")
                        .requestMatchers(HttpMethod.POST, "/api/assets/{assetID}/upload-images").hasAuthority("Manager")
                        .requestMatchers(HttpMethod.POST, "/api/assets").hasAuthority("Manager")
                        .requestMatchers(HttpMethod.PUT, "/api/assets/**").hasAnyAuthority("Admin", "Manager")
                        .requestMatchers(HttpMethod.DELETE, "/api/assets/**").hasAuthority("Admin")
                        .requestMatchers(HttpMethod.POST, "/api/assets/update-status")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/assets/handle-deletion").hasAuthority("Admin")
                        .requestMatchers(HttpMethod.POST, "/api/assets/request-dispose")
                        .hasAnyAuthority("Admin", "Manager")
                        .requestMatchers(HttpMethod.GET, "/api/assets", "/api/assets/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/assets/{assetCode}").permitAll()
                        
                        .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/categories").hasAuthority("Manager")
                        .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasAuthority("Manager")
                        .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAuthority("Manager")

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
