package com.example.fastcampusmysql.controller;

import com.example.fastcampusmysql.application.usecase.GetTimelinePostUseCase;
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

    @PostMapping("")
    public Long create(PostCommand command) {
        return postWriteService.create(command);
    }


    @PostMapping("/daily-post-counts")
    public List<DailyPostCount> getDailyPostCount(@RequestBody DailyPostCountRequest request) {
        return postReadService.getDailyPostCounts(request);
    }

    @PostMapping("/members/{memberId}")
    public Page<Post> getPosts(
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
        return getTimelinePostUseCase.execute(memberId, request);
    }

}
