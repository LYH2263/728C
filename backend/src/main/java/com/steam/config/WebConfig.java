package com.steam.config;

import com.steam.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtil))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/games/**",
                        "/categories/**",
                        "/reviews/game/**",
                        "/error"
                );
    }
    
    /**
     * 认证拦截器
     */
    @RequiredArgsConstructor
    public static class AuthInterceptor implements HandlerInterceptor {
        
        private final JwtUtil jwtUtil;
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            // 放行OPTIONS请求
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                return true;
            }
            
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            
            token = token.substring(7);
            if (!jwtUtil.validateToken(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            
            // 将用户信息放入请求属性
            request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
            request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
            request.setAttribute("role", jwtUtil.getRoleFromToken(token));
            
            return true;
        }
    }
}
