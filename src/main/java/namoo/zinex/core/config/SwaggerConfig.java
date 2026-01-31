package namoo.zinex.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info =
        @Info(
            title = "zinex API",
            description = "zinex(주식거래소) 백엔드 API 문서",
            version = "v1.0"
        )
)
@Configuration
public class SwaggerConfig {
        @Bean
        public OpenAPI openAPI(){

                /// 보안 스키마(API 인증 방식) 정의 -> HTTP 인증 방식, Bearer 토큰, JWT 형식
                SecurityScheme securityScheme = new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");

                /// API의 인증 요구 사항 -> bearerAuth 이름으로 securityScheme 참조
                SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

                return new OpenAPI()
                        .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                        .addSecurityItem(securityRequirement);
        }
}

