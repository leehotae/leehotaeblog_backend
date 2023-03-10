package com.example.demo.Web.Security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.example.demo.Web.Host.HostEnviroment;

@Configuration
public class CorsConfig {

   @Bean
   public CorsFilter corsFilter() {
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowCredentials(true);
      config.addAllowedOrigin(HostEnviroment.CLIENT_HOST); 
      config.addAllowedHeader("*");
      config.addAllowedMethod("*");

      config.addExposedHeader("AccessToken");

      source.registerCorsConfiguration("/api/**", config);
      return new CorsFilter(source);
   }

}