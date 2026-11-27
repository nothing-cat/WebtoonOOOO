package com.example.demo.auth;
import com.example.demo.user.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
    public AuthController(UserRepository users){this.users=users;}
    public record Credentials(String username,String password){}
    public record UserDto(UUID id,String username,UserRole role){}
    private UserDto dto(User u){return new UserDto(u.getId(),u.getUsername(),u.getRole());}
    @PostMapping("/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> signup(@RequestBody Credentials c){
        if(c.username()==null||!c.username().matches("[a-z0-9_]{4,20}")||c.password()==null||c.password().length()<8||c.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"아이디 4~20자, 비밀번호 8자 이상(72바이트 이하)을 입력하세요.");
        if(users.existsByUsername(c.username()))throw new ResponseStatusException(HttpStatus.CONFLICT,"이미 존재하는 아이디입니다.");
        User u=new User();u.setUsername(c.username());u.setPasswordHash(encoder.encode(c.password()));users.save(u);return Map.of("message","가입 완료");
    }
    @PostMapping("/auth/login")
    public UserDto login(@RequestBody Credentials c,HttpServletRequest request){
        User u=c.username()==null?null:users.findByUsername(c.username()).orElse(null);
        if(u==null||c.password()==null||c.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72||!encoder.matches(c.password(),u.getPasswordHash()))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"아이디 또는 비밀번호가 올바르지 않습니다.");
        if (u.isBlocked()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "차단된 계정입니다.");
        var previous=request.getSession(false);if(previous!=null)previous.invalidate();
        request.getSession(true).setAttribute(SessionUser.KEY,u.getId());return dto(u);
    }
    @PostMapping("/auth/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request){var session=request.getSession(false);if(session!=null)session.invalidate();}
    @GetMapping("/users/me")
    public UserDto me(HttpServletRequest request){return dto(users.findById(SessionUser.require(request)).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"로그인이 필요합니다.")));}
}
