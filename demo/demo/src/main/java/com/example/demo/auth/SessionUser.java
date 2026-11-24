package com.example.demo.auth;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
public final class SessionUser {
    private SessionUser(){}
    public static final String KEY="moodtoon.userId";
    public static UUID id(HttpServletRequest request){var s=request.getSession(false);return s==null?null:(UUID)s.getAttribute(KEY);}
    public static UUID require(HttpServletRequest request){UUID id=id(request);if(id==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"로그인이 필요합니다.");return id;}
}
