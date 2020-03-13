package com.prc391.patra.config.security;

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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final DatabaseAuthProvider databaseAuthProvider;

    @Autowired
    public SecurityConfig(DatabaseAuthProvider databaseAuthProvider) {
        this.databaseAuthProvider = databaseAuthProvider;
    }

    //Spring Security se su dung encoder nay cho viec check password
    //cau truc password luu trong database: {encodingAlgorithm}password
    //ex: {bcrypt}$2a$10$MmwsXsyLmOD5FtCVrJDqd.J0waJsS.wIunYGC0MrnasU7YWodR2O2
    //dung co dung MD4 voi MD5, no encode bua loi ra
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(databaseAuthProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //stateless: khong tao bat ky session nao va khong dung bat ky session co san nao
                //session va cookie luon
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .enableSessionUrlRewriting(false)
                .and()
                .authorizeRequests()
                //can exclude url nao thi cho vao day (vi du login)
                //.antMatchers("/login").permitAll()

                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                ;
    }

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
