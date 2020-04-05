package com.prc391.patra.config;

import io.swagger.models.auth.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Patra Project's Activites Tracking System")
                .description("")
                .version("0.0.1")
                .contact(new Contact("Group", "https://www.patra.com", "thonglypatra@yahoo.an"))
                .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(
                        Arrays.asList(new ApiKey(
                                "Bearer token",
                                HttpHeaders.AUTHORIZATION,
                                In.HEADER.name())))
                .select()
                .build()
                .apiInfo(apiInfo())
                .globalOperationParameters(Arrays.asList(parameterBuilder()))

                ;
    }

    private Parameter parameterBuilder() {
        return new ParameterBuilder()
                .name("Authorization")
                .description("Bearer token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();
    }
}
