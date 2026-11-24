package com.example.demo.common;
import com.example.demo.user.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.UUID;
@Service
public class AccessService {
    private final UserRepository users;
    public AccessService(UserRepository users){this.users=users;}
    public User require(UUID id){if(id==null)throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"로그인이 필요합니다.");return users.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"로그인이 필요합니다."));}
    public void owner(User actor,User owner,boolean allowAdmin){if(!actor.getId().equals(owner.getId())&&!(allowAdmin&&actor.getRole()==UserRole.ADMIN))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"권한이 없습니다.");}
    public static String nonblank(String s){if(s==null||s.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"필수 항목을 입력하세요.");return s.trim();}
}
