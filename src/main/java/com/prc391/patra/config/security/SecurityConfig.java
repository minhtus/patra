package com.prc391.patra.config.security;

import com.prc391.patra.filter.JWTAuthenticationFilter;
import com.prc391.patra.filter.JWTLoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final DatabaseAuthProvider databaseAuthProvider;

    @Autowired
    public SecurityConfig(DatabaseAuthProvider databaseAuthProvider) {
        this.databaseAuthProvider = databaseAuthProvider;
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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers(HttpMethod.POST, "/login").permitAll()
                .antMatchers(HttpMethod.GET, "/login").denyAll();

        http
                //stateless: khong tao bat ky session nao va khong dung bat ky session co san nao
                //session va cookie luon
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
                .anyRequest().authenticated()
//                .and()
//                .httpBasic()
        ;

        http
                .addFilterBefore(new JWTLoginFilter("/login", authenticationManager()), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JWTAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        ;
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
