package com.example.demo.common;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Map;
@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",e.getReason()==null?"요청 실패":e.getReason()));}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> invalid(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("message","입력값을 확인하세요."));}
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> conflict(DataIntegrityViolationException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","중복되거나 유효하지 않은 데이터입니다."));}
}
