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
                .description("Thống Lý Pá Tra")
                .version("0.0.0 with GET login")
                .contact(new Contact("Group", "https://www.patra.com", "thonglypatra@yahoo.an"))
                .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(
                        Arrays.asList(new ApiKey(
                                "JWT Token Here",
                                HttpHeaders.AUTHORIZATION,
                                In.HEADER.name())))
//                .securityContexts(Arrays.asList(securityContext())
                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.prc391.patra"))
                .build()
                .apiInfo(apiInfo())
                //use this to pass JWT, will remove when OAuth2 is implemented
                .globalOperationParameters(Arrays.asList(parameterBuilder()))

                ;
    }

    private Parameter parameterBuilder() {
        return new ParameterBuilder()
                .name("Authorization")
                .description("Put your JWT here")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build();
    }

    //will be used in OAuth2
//    private SecurityContext securityContext() {
//
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//
//        List<SecurityReference> securityReferences = new ArrayList<>();
//        securityReferences.add(new SecurityReference("swaggerApiKey", authorizationScopes));
//
//        return SecurityContext.builder()
//                .securityReferences(securityReferences)
//                .build();
//    }

    //will be used in OAuth2
//    @Bean
//    SecurityConfiguration security() {
//        return new SecurityConfiguration(
//                "test-app-client-id",
//                "test-app-client-secret",
//                "test-app-reich",
//                "test-app",
//                "Authorization",
//                ApiKeyVehicle.HEADER,
//                "Basic",
//                ","
//        );
//    }
}
