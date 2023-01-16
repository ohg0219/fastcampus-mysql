package com.example.fastcampusmysql.domain.post.service;

import com.example.fastcampusmysql.domain.post.entity.Timeline;
import com.example.fastcampusmysql.domain.post.repository.TimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineWriteService {

    private final TimelineRepository timelineRepository;

    public void deliveryToTimeline(Long postId, List<Long> toMemberIds) {
        var timelines = toMemberIds.stream()
                .map(memberId -> tiTimeline(postId, memberId))
                .toList();

        timelineRepository.bulkInsert(timelines);
    }

    private static Timeline tiTimeline(Long postId, Long memberId) {
        return Timeline.builder().memberId(memberId).postId(postId).build();
    }

}
