package com.example.fastcampusmysql.application.usecase;

import com.example.fastcampusmysql.domain.follow.entity.Follow;
import com.example.fastcampusmysql.domain.follow.service.FollowReadService;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.service.PostReadService;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.PageCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTimelinePostUseCase {

    private final FollowReadService followReadService;
    private final PostReadService postReadService;

    public PageCursor<Post> execute(Long memberId, CursorRequest request) {
        /*
        * 1. memberId -> flow 정보 조회
        * 2. 1번의 결과로 게시물 조회
        */
        var followings = followReadService.getFollowings(memberId);
        var followingMemberIds = followings.stream().map(Follow::getToMemberId).toList();
        return postReadService.getPosts(followingMemberIds, request);
    }

}
