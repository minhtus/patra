package com.prc391.patra.config.security;

import com.prc391.patra.filter.JWTAuthenticationFilter;
import com.prc391.patra.filter.JWTLoginFilter;
import com.prc391.patra.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final DatabaseAuthProvider databaseAuthProvider;
    private final UserRepository userRepository;

    @Autowired
    public SecurityConfig(DatabaseAuthProvider databaseAuthProvider, UserRepository userRepository) {
        this.databaseAuthProvider = databaseAuthProvider;
        this.userRepository = userRepository;
    }

    /**
     * Spring Security will use this PasswordEncoder for encoding and checking password
     * Password saved in database must have the following format: {encodingAlgorithm}password
     * ex: {bcrypt}$2a$10$MmwsXsyLmOD5FtCVrJDqd.J0waJsS.wIunYGC0MrnasU7YWodR2O2
     * Note: try NOT to use MD5, I don't understand how Spring encode using MD5...
     *
     * @return the password encoder that Spring would use
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(databaseAuthProvider);
    }

    //allow preflight request
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080","http://iq30.co"));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH"
        ));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Cache-Control", "Content-type"
        ));
        configuration.addExposedHeader("Authorization");
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // put "/**"  in "path" param to allow all,
        // when auth is fully implemented and used, use "/login" only to allow pre-flight request
        // when login
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new PatraLogoutSuccessHandler();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //temporary disable this to let anonymous use POST methods
        //re-enable it (delete this line) before going "production"
        http.csrf().disable();

        http.cors();

        http.authorizeRequests()

                .antMatchers("/login").permitAll()

        ;

        http
                //stateless: won't create cookie and won't use cookie
                //applies for both session and cookie
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .enableSessionUrlRewriting(false)
                .and()
                .authorizeRequests()
                //enter what url you want to exclude here, and it won't be authenticated by Spring security
                //ex: login
//                .antMatchers(HttpMethod.POST,"/api/login").permitAll()
                //hope this will prevent login with GET
//                .antMatchers(HttpMethod.GET,"/login").denyAll()

                //let anonymous use API for easier developing
                .anyRequest()
                .permitAll()
        //uncomment the .authenticated and comment .permitAll to use security
        //.authenticated()

//                .and()
//                .httpBasic()
        ;

        http
                .addFilterBefore(new JWTLoginFilter("/login", authenticationManager()), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTAuthenticationFilter(userRepository), UsernamePasswordAuthenticationFilter.class)
        ;

        http.logout()
//                .logoutSuccessUrl("/logout")
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler());
    }

    /**
     * Ignore all swagger
     *
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**")
                .and()
                .ignoring()
                .antMatchers();

    }
}
