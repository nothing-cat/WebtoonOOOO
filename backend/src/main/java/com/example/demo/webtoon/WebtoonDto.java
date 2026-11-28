package com.example.demo.webtoon;
import java.util.List;
public record WebtoonDto(String id,String title,String pictureWriter,String storyWriter,String genre,String genreId,List<String> platforms,List<String> platformIds,String description,String thumbnailUrl,String ageRating,long likeCount,boolean isFavorite) {}
