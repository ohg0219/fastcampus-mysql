package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import com.example.fastcampusmysql.domain.follow.service.FollowReadService;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.entity.Timeline;
import com.example.fastcampusmysql.domain.post.service.PostReadService;
import com.example.fastcampusmysql.domain.post.service.TimelineReadService;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.PageCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTimelinePostUseCase {

    private final FollowReadService followReadService;
    private final PostReadService postReadService;
    private final TimelineReadService timelineReadService;

    public PageCursor<Post> execute(Long memberId, CursorRequest request) {
        /*
        * 1. memberId -> flow 정보 조회
        * 2. 1번의 결과로 게시물 조회
        */
        var followings = followReadService.getFollowings(memberId);
        var followingMemberIds = followings.stream().map(Follow::getToMemberId).toList();
        return postReadService.getPosts(followingMemberIds, request);
    }

    public PageCursor<Post> executeByTimeline(Long memberId, CursorRequest request) {
        /*
         * 1. timeline 조회
         * 2. 1번에 해당하는 게시글을 조회한다
         */
        var pagedTimelines = timelineReadService.getTimelines(memberId, request);
        var postIds = pagedTimelines.body().stream().map(Timeline::getPostId).toList();
        var posts =  postReadService.getPosts(postIds);

        return new PageCursor(pagedTimelines.nextCursorRequest(), posts);

    }


}
