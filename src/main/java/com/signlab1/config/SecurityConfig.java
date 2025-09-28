package com.signlab1.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test/**").permitAll() // 测试接口无需认证
                .requestMatchers("/api/health/**").permitAll() // 健康检查接口无需认证
                .requestMatchers("/api/student/bind-class").permitAll() // 学生绑定班级无需认证
                .requestMatchers("/api/student/photo/**").permitAll() // 照片查看接口无需认证
                .requestMatchers("/actuator/**").permitAll() // Spring Boot Actuator健康检查
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 开发环境：允许所有来源（不携带凭证）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 生产环境：建议指定具体域名（取消注释并修改）
        // configuration.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:3000",
        //     "http://localhost:8080", 
        //     "https://yourdomain.com",
        //     "https://www.yourdomain.com"
        // ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 不允许携带凭证（避免与通配符冲突）
        configuration.setAllowCredentials(false);
        
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        // 暴露给客户端的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With", 
            "accept", 
            "Origin", 
            "Access-Control-Request-Method", 
            "Access-Control-Request-Headers",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

/**
 * Web MVC 跨域配置（备用方案）
 */
@Configuration
class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}