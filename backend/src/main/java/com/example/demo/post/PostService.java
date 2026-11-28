package com.example.demo.post;
import com.example.demo.common.AccessService;
import com.example.demo.user.*;
import com.example.demo.webtoon.WebtoonService;
import com.example.demo.postcomment.PostCommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
@Service
@Transactional
public class PostService {
    private final PostRepository posts;
    private final PostLikeRepository likes;
    private final PostCommentRepository comments;
    private final AccessService access;
    private final WebtoonService webtoons;
    private final JdbcTemplate jdbc;

    public PostService(PostRepository posts, PostLikeRepository likes, PostCommentRepository comments,
                       AccessService access, WebtoonService webtoons, JdbcTemplate jdbc) {
        this.posts = posts;
        this.likes = likes;
        this.comments = comments;
        this.access = access;
        this.webtoons = webtoons;
        this.jdbc = jdbc;
    }

    public record PostDto(Long id, UUID userId, String username, String webtoonId, PostCategory category,
                          String title, String content, long viewCount, long likeCount, long commentCount,
                          boolean isLiked, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
    public record Input(PostCategory category, String title, String content, String webtoonId) {}
    public record PostPage(List<PostDto> items, int page, boolean hasNext) {}

    public Post require(Long id) {
        return posts.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 없습니다."));
    }

    public PostDto dto(Post post, UUID viewer) {
        Long id = post.getId();
        return new PostDto(id, post.getUser().getId(), post.getUser().getDisplayName(),
            post.getWebtoon() == null ? null : post.getWebtoon().getId(), post.getCategory(),
            post.getTitle(), post.getContent(), post.getViewCount(), likes.countByPost_Id(id),
            comments.countByPost_Id(id), viewer != null && likes.existsByPost_IdAndUser_Id(id, viewer),
            post.getCreatedAt(), post.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public PostDto get(Long id, UUID viewer) {
        return dto(require(id), viewer);
    }

    @Transactional(readOnly = true)
    public List<PostDto> list(PostCategory category, boolean popular, UUID viewer) {
        return queryList(category, popular, false, viewer, null, 0);
    }

    @Transactional(readOnly = true)
    public PostPage page(PostCategory category, boolean popular, boolean excludeNotices,
                         UUID viewer, int page, int size) {
        if (page < 1 || size < 1 || size > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "페이지 또는 글 개수를 확인해 주세요.");
        }
        // Read one extra row to decide whether the next page exists without a separate COUNT query.
        List<PostDto> rows = queryList(category, popular, excludeNotices, viewer, size + 1,
            (page - 1L) * size);
        boolean hasNext = rows.size() > size;
        return new PostPage(hasNext ? List.copyOf(rows.subList(0, size)) : rows, page, hasNext);
    }

    private List<PostDto> queryList(PostCategory category, boolean popular, boolean excludeNotices,
                                    UUID viewer, Integer limit, long offset) {
        String categoryName = category == null ? null : category.name();
        // Only popular ordering needs like counts before selecting a page.
        String rank = popular
            ? "(select count(*) from public.post_likes where post_id = p.id)"
            : "case when p.category = 'NOTICE' then 1 else 0 end";
        String likesColumn = popular ? "p.rank" :
            "(select count(*) from public.post_likes where post_id = p.id)";
        return jdbc.query("""
            with selected as (
                select p.*, %s as rank
                from public.posts p
                where (?::text is null or p.category = ?)
                  and (not ? or p.category <> 'NOTICE')
                order by rank desc, p.id desc
                limit ? offset ?
            )
            select p.id, p.user_id, coalesce(u.nickname, u.username) as author_name,
                   p.webtoon_id, p.category, p.title, p.content, p.view_count,
                   %s as like_count,
                   (select count(*) from public.post_comments where post_id = p.id) as comment_count,
                   exists(select 1 from public.post_likes where post_id = p.id and user_id = ?) as liked,
                   p.created_at, p.updated_at
            from selected p join public.users u on u.id = p.user_id
            order by p.rank desc, p.id desc
            """.formatted(rank, likesColumn), POST_ROW, categoryName, categoryName, excludeNotices,
            limit, offset, viewer);
    }

    private static final RowMapper<PostDto> POST_ROW = (row, index) -> new PostDto(
        row.getLong("id"), row.getObject("user_id", UUID.class), row.getString("author_name"),
        row.getString("webtoon_id"), PostCategory.valueOf(row.getString("category")),
        row.getString("title"), row.getString("content"), row.getLong("view_count"),
        row.getLong("like_count"), row.getLong("comment_count"), row.getBoolean("liked"),
        row.getObject("created_at", OffsetDateTime.class), row.getObject("updated_at", OffsetDateTime.class));

    @Transactional(readOnly = true)
    public List<PostDto> reviews(String webtoonId, UUID viewer) {
        return posts.findByWebtoon_IdAndCategoryOrderByCreatedAtDesc(webtoonId, PostCategory.REVIEW)
            .stream().map(post -> dto(post, viewer)).toList();
    }

    @Transactional(readOnly = true)
    public List<PostDto> mine(UUID viewer) {
        access.require(viewer);
        return posts.findByUser_IdOrderByCreatedAtDesc(viewer, Pageable.unpaged()).stream()
            .map(post -> dto(post, viewer)).toList();
    }

    private void apply(Post post, Input input, User user) {
        if (input.category() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "게시판을 선택하세요.");
        }
        if (input.category() == PostCategory.NOTICE && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 공지를 작성할 수 있습니다.");
        }
        post.setCategory(input.category());
        post.setTitle(AccessService.nonblank(input.title()));
        post.setContent(AccessService.nonblank(input.content()));
        String webtoonId = input.webtoonId();
        post.setWebtoon(input.category() == PostCategory.REVIEW && webtoonId != null && !webtoonId.isBlank()
            ? webtoons.require(webtoonId.trim())
            : null);
    }

    public PostDto create(Input input, UUID actor) {
        User user = access.require(actor);
        Post post = new Post();
        post.setUser(user);
        apply(post, input, user);
        return dto(posts.saveAndFlush(post), actor);
    }

    public PostDto update(Long id, Input input, UUID actor) {
        User user = access.require(actor);
        Post post = require(id);
        access.owner(user, post.getUser(), false);
        apply(post, input, user);
        return dto(posts.saveAndFlush(post), actor);
    }

    public void delete(Long id, UUID actor) {
        Post post = require(id);
        access.owner(access.require(actor), post.getUser(), true);
        posts.delete(post);
    }

    public void like(Long id, UUID actor, boolean add) {
        access.require(actor);
        require(id);
        if (add) {
            jdbc.update("insert into public.post_likes(post_id,user_id) values (?,?) on conflict do nothing", id, actor);
        } else {
            jdbc.update("delete from public.post_likes where post_id=? and user_id=?", id, actor);
        }
    }

    public void visit(Long id) {
        if (posts.incrementViewCount(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글이 없습니다.");
        }
    }
}
