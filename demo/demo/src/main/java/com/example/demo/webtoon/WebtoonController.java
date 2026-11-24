package com.example.demo.webtoon;
import com.example.demo.auth.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/webtoons")
public class WebtoonController {
    private final WebtoonService service;
    private final WebtoonQueryService queries;
    public WebtoonController(WebtoonService service,WebtoonQueryService queries){this.service=service;this.queries=queries;}
    @GetMapping public List<WebtoonDto> search(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String genre,@RequestParam(defaultValue="") String platform,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="24") int size,HttpServletRequest request){return queries.search(keyword,genre,platform,page,size,false,SessionUser.id(request)).items();}
    @GetMapping("/popular") public List<WebtoonDto> popular(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String genre,@RequestParam(defaultValue="") String platform,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="24") int size,HttpServletRequest request){return queries.search(keyword,genre,platform,page,size,true,SessionUser.id(request)).items();}
    @GetMapping("/page") public WebtoonPage page(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String genre,@RequestParam(defaultValue="") String platform,@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="24") int size,@RequestParam(defaultValue="false") boolean popular,HttpServletRequest request){return queries.search(keyword,genre,platform,page,size,popular,SessionUser.id(request));}
    @GetMapping("/{id}") public WebtoonDto detail(@PathVariable String id,HttpServletRequest request){return service.detail(id,SessionUser.id(request));}
}
