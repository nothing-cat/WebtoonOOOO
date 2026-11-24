package com.example.demo.webtoon;
import com.example.demo.auth.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/webtoons")
public class WebtoonController {
    private final WebtoonService service;
    public WebtoonController(WebtoonService service){this.service=service;}
    @GetMapping public List<WebtoonDto> search(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String genre,@RequestParam(defaultValue="") String platform,HttpServletRequest request){return service.search(keyword,genre,platform,SessionUser.id(request));}
    @GetMapping("/popular") public List<WebtoonDto> popular(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String genre,@RequestParam(defaultValue="") String platform,HttpServletRequest request){return service.popular(keyword,genre,platform,SessionUser.id(request));}
    @GetMapping("/{id}") public WebtoonDto detail(@PathVariable String id,HttpServletRequest request){return service.detail(id,SessionUser.id(request));}
}
