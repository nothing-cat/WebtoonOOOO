package com.example.demo.post;
import com.example.demo.auth.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.*;
@RestController
@RequestMapping("/api")
public class PostController {
    private final PostService service;
    public PostController(PostService service){this.service=service;}
    @GetMapping("/posts") public List<PostService.PostDto> list(@RequestParam(required=false) PostCategory category,HttpServletRequest r){return service.list(category,false,SessionUser.id(r));}
    @GetMapping("/posts/popular") public List<PostService.PostDto> popular(@RequestParam(required=false) PostCategory category,HttpServletRequest r){return service.list(category,true,SessionUser.id(r));}
    @GetMapping("/posts/page")
    public PostService.PostPage page(
        @RequestParam(required = false) PostCategory category,
        @RequestParam(defaultValue = "false") boolean popular,
        @RequestParam(defaultValue = "false") boolean excludeNotices,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        HttpServletRequest request
    ) {
        return service.page(category, popular, excludeNotices, SessionUser.id(request), page, size);
    }
    @GetMapping("/posts/{id}") public PostService.PostDto get(@PathVariable Long id,HttpServletRequest r){return service.get(id,SessionUser.id(r));}
    @GetMapping("/webtoons/{id}/reviews") public List<PostService.PostDto> reviews(@PathVariable String id,HttpServletRequest r){return service.reviews(id,SessionUser.id(r));}
    @GetMapping("/users/me/posts") public List<PostService.PostDto> mine(HttpServletRequest r){return service.mine(SessionUser.require(r));}
    @PostMapping("/posts") @ResponseStatus(HttpStatus.CREATED) public PostService.PostDto create(@RequestBody PostService.Input in,HttpServletRequest r){return service.create(in,SessionUser.require(r));}
    @PutMapping("/posts/{id}") public PostService.PostDto update(@PathVariable Long id,@RequestBody PostService.Input in,HttpServletRequest r){return service.update(id,in,SessionUser.require(r));}
    @DeleteMapping("/posts/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id,HttpServletRequest r){service.delete(id,SessionUser.require(r));}
    @PostMapping("/posts/{id}/like") @ResponseStatus(HttpStatus.NO_CONTENT) public void like(@PathVariable Long id,HttpServletRequest r){service.like(id,SessionUser.require(r),true);}
    @DeleteMapping("/posts/{id}/like") @ResponseStatus(HttpStatus.NO_CONTENT) public void unlike(@PathVariable Long id,HttpServletRequest r){service.like(id,SessionUser.require(r),false);}
    @PostMapping("/posts/{id}/views") @ResponseStatus(HttpStatus.NO_CONTENT) public void view(@PathVariable Long id){service.visit(id);}
}
