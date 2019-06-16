package blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
public class App {
  // defined in application.properties
  @Value("${kame.enable_cors}")
  private boolean enable_cors;
  @Value("${kame.cors_origins:*}")
  private String[] cors_origins;

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        if (enable_cors) {
          registry.addMapping("/**").allowedOrigins(cors_origins);
        }
      }
    };
  }
}
