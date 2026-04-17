package com.tfgkk.schoolcompetition.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园竞赛管理平台 - API 接口文档")
                        .version("1.0")
                        .description("基于 Spring Boot 3 和 Vue 3 的重构版校园竞赛管理系统。支持身份鉴权、竞赛管理、学生报名及数据可视化。")
                        .contact(new Contact().name("开发团队").email("admin@tfgkk.com"))
                        .license(new License().name("Apache 2.0")));
    }
}
