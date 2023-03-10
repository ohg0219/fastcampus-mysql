package com.example.fastcampusmysql.controller;

import com.example.fastcampusmysql.application.usecase.CreatePostLikeUseCase;
import com.example.fastcampusmysql.application.usecase.CreatePostUseCase;
import com.example.fastcampusmysql.application.usecase.GetTimelinePostUseCase;
import com.example.fastcampusmysql.domain.member.dto.PostDto;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.dto.PostCommand;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.service.PostReadService;
import com.example.fastcampusmysql.domain.post.service.PostWriteService;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.PageCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostWriteService postWriteService;
    private final PostReadService postReadService;

    private final GetTimelinePostUseCase getTimelinePostUseCase;
    private final CreatePostUseCase createPostUseCase;
    private final CreatePostLikeUseCase createPostLikeUseCase;

    @PostMapping("")
    public Long create(PostCommand command) {
        return createPostUseCase.execute(command);
    }


    @PostMapping("/daily-post-counts")
    public List<DailyPostCount> getDailyPostCount(@RequestBody DailyPostCountRequest request) {
        return postReadService.getDailyPostCounts(request);
    }

    @PostMapping("/members/{memberId}")
    public Page<PostDto> getPosts(
            @PathVariable Long memberId,
            Pageable pageable
    ) {
        return postReadService.getPosts(memberId, pageable);
    }

    @PostMapping("/members/{memberId}/by-cursor")
    public PageCursor<Post> getPostsByCursor(
            @PathVariable Long memberId,
            CursorRequest request
    ) {
        return postReadService.getPosts(memberId, request);
    }

    @GetMapping("/members/{memberId}/timeline")
    public PageCursor<Post> getTimeLine(
            @PathVariable Long memberId,
            CursorRequest request
    ) {
        return getTimelinePostUseCase.executeByTimeline(memberId, request);
    }

    @PostMapping("/{postId}/like/v1")
    public void likePost(@PathVariable Long postId) {
//        postWriteService.listPost(postId);
        postWriteService.listPostByOptimisticLock(postId);
    }

    @PostMapping("/{postId}/like/v2")
    public void likePostV2(@PathVariable Long postId, @RequestParam Long memberId) {
        createPostLikeUseCase.execute(postId, memberId);
    }

}
