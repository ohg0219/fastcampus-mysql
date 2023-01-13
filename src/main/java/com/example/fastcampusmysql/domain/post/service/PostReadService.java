package com.example.fastcampusmysql.domain.post.service;

import com.example.fastcampusmysql.domain.post.dto.DailyPostCount;
import com.example.fastcampusmysql.domain.post.dto.DailyPostCountRequest;
import com.example.fastcampusmysql.domain.post.entity.Post;
import com.example.fastcampusmysql.domain.post.repository.PostRepository;
import com.example.fastcampusmysql.util.CursorRequest;
import com.example.fastcampusmysql.util.PageCursor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostReadService {

    private final PostRepository postRepository;

    public List<DailyPostCount> getDailyPostCounts(DailyPostCountRequest request) {
        /**
         *  반환 -> 리스트 [작성일자, 작성회원, 작성 게시물 갯수]
         *  SELECT createdDate memberId, COUNT(id)
         *  FROM post
         *  WHERE memberId = :memberId AND createdDate BETWEEN :firstDate and :lastDate
         *  GROUP BY createdDate memberId
         */
        return postRepository.groupByCreatedDate(request);
    }

    public Page<Post> getPosts(Long memberId, Pageable pageRequest) {
        return postRepository.findAllByMemberId(memberId, pageRequest);
    }

    public PageCursor<Post> getPosts(Long memberId, CursorRequest request) {
        var posts = findAllBy(memberId, request);
        var nextKey = getNextKey(posts);

        return new PageCursor<>(request.next(nextKey), posts);
    }


    public PageCursor<Post> getPosts(List<Long> memberIds, CursorRequest request) {
        var posts = findAllBy(memberIds, request);
        var nextKey = getNextKey(posts);

        return new PageCursor<>(request.next(nextKey), posts);
    }

    private List<Post> findAllBy(Long memberId, CursorRequest request) {
        if (request.hasKey()) {
            return postRepository.findAllByLessThanIdAndMemberIdAndOrderByIdDesc(request.key(), memberId, request.size());
        } else {
            return postRepository.findAllByMemberIdAndOrderByIdDesc(memberId, request.size());
        }
    }

    private List<Post> findAllBy(List<Long> memberIds, CursorRequest request) {
        if (request.hasKey()) {
            return postRepository.findAllByLessThanIdAndInMemberIdAndOrderByIdDesc(request.key(), memberIds, request.size());
        } else {
            return postRepository.findAllByInMemberIdAndOrderByIdDesc(memberIds, request.size());
        }
    }

    private static long getNextKey(List<Post> posts) {
        return posts.stream()
                .mapToLong(Post::getId)
                .min()
                .orElse(CursorRequest.NONE_KEY);
    }

}
