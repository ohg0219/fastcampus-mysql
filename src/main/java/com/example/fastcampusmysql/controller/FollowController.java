package com.example.fastcampusmysql.controller;

import com.example.fastcampusmysql.application.usecase.CreateFollowMemberUseCase;
import com.example.fastcampusmysql.application.usecase.GetFollowingMembersUsecase;
import com.example.fastcampusmysql.domain.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final CreateFollowMemberUseCase createFollowMemberUseCase;

    private final GetFollowingMembersUsecase getFollowingMembersUsecase;

    @PostMapping("/{fromId}/{toId}")
    public void create(@PathVariable Long fromId, @PathVariable Long toId) {
        createFollowMemberUseCase.execute(fromId, toId);
    }
    
    @PostMapping("/members/{fromId}")
    public List<MemberDto> create(@PathVariable Long fromId) {
        return getFollowingMembersUsecase.execute(fromId);
    }


}
