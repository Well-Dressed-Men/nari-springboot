package welldressedmen.narispringboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
   @Bean
   public CorsFilter corsFilter() {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowCredentials(true);
      config.addAllowedOrigin("http://localhost:3000"); // 값요청 허용하고자하는 도메인의 정확한 원본URL - Access-Control-Allow-Origin  (Response에 자동으로 추가해줌)
      config.addAllowedHeader("*");  // Access-Control-Request-Headers  
      config.addAllowedMethod("*"); // Access-Control-Request-Method
      
      source.registerCorsConfiguration("/**", config);
      return new CorsFilter(source);
   }

}
