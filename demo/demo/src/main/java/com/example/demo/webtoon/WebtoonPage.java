package com.example.demo.webtoon;
import java.util.List;
public record WebtoonPage(List<WebtoonDto> items,int page,int size,long totalCount,long totalPages) {}
