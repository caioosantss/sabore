package com.projeto_final.receitas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                // allowedOriginPatterns, e nao allowedOrigins: aceita curinga
                // (https://*.vercel.app), o que cobre as URLs de preview, que
                // mudam a cada deploy. allowedOrigins exige igualdade exata e
                // recusa qualquer curinga.
                //
                // O curinga tambem e a unica forma de combinar origem dinamica
                // com allowCredentials(true) — o navegador proibe responder
                // "Access-Control-Allow-Origin: *" quando ha credenciais, e o
                // Spring resolve o padrao para a origem concreta da chamada.
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
