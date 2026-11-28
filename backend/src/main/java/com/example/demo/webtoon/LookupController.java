package com.example.demo.webtoon;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api")
public class LookupController {
    private final GenreRepository genres;
    private final PlatformRepository platforms;
    public LookupController(GenreRepository genres,PlatformRepository platforms){this.genres=genres;this.platforms=platforms;}
    public record LookupDto(String id,String name){}
    @GetMapping("/genres") public List<LookupDto> genres(){return genres.findAll().stream().map(g->new LookupDto(g.getId(),g.getName())).toList();}
    @GetMapping("/platforms") public List<LookupDto> platforms(){return platforms.findAll().stream().map(p->new LookupDto(p.getId(),p.getName())).toList();}
}
