package com.example.demo.config;
import org.springframework.context.annotation.Configuration;
import com.example.demo.auth.SessionUser;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${APP_FRONTEND_ORIGIN:http://localhost:5173}") private String origins;
    private final UserRepository users;

    public WebConfig(UserRepository users) { this.users = users; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                var id = SessionUser.id(request);
                if (id != null && users.findById(id).map(User::isBlocked).orElse(false)) {
                    var session = request.getSession(false);
                    if (session != null) session.invalidate();
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "차단된 계정입니다.");
                }
                return true;
            }
        }).addPathPatterns("/api/**").excludePathPatterns("/api/auth/logout");
    }

    @Override public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/api/**").allowedOrigins(origins.split("\\s*,\\s*")).allowedMethods("GET","POST","PUT","DELETE","OPTIONS").allowedHeaders("Content-Type", "X-CSRF-TOKEN").allowCredentials(true);
    }
}
