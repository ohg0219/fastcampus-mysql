package com.example.fastcampusmysql.domain.member.service;

import com.example.fastcampusmysql.domain.member.dto.RegisterMemberCommand;
import com.example.fastcampusmysql.domain.member.entity.Member;
import com.example.fastcampusmysql.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberWriteService {

    private final MemberRepository memberRepository;

    public Member create(RegisterMemberCommand command) {
        /**
         * 목표 - 회원 정보(이메일, 닉네임, 생년월일)를 등록한다.
         *     - 닉네임은 최대 10자
         * 파라미터 - memberRegisterCommand
         *
         * memberRepository.save()
         */
        Member member = Member.builder()
                .nickname(command.nickname())
                .birthday(command.birthday())
                .email(command.email())
                .build();

        return memberRepository.save(member);
    }

}