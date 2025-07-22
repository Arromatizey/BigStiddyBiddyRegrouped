package com.esgi.studyBuddy.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
  // Désactiver cette configuration CORS pour éviter les conflits
  // La configuration CORS se fait maintenant uniquement dans DevSecurityConfig
  /*
  @Bean
 public WebMvcConfigurer corsConfigurer() {
      return new WebMvcConfigurer() {
         @Override
         public void addCorsMappings(CorsRegistry registry) {
              registry.addMapping("/**")
                      .allowedOrigins("http://localhost:4200") // ton front Angular
                      .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                       .allowedHeaders("*")
                       .allowCredentials(true); // si tu utilises des cookies ou JWT
           }
       };
    }
    */
}
