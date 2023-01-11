package com.example.fastcampusmysql.domain.member;

import com.example.fastcampusmysql.util.MemberFixtureFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    @DisplayName("회원은 닉네임을 변경할 수 있다.")
    public void testChangeNickname() {
        var member = MemberFixtureFactory.create();

        var expected = "pnu";

        member.changeNickname(expected);

        Assertions.assertEquals(expected, member.getNickname());

//        LongStream.range(0, 10)
//                .mapToObj(MemberFixtureFactory::create)
//                .forEach(m -> {
//                    System.out.println(m.getNickname());
//                });
    }


    @Test
    @DisplayName("회원의 닉네임을 10자를 초과할 수 없다")
    public void testNicknameMaxLength() {
        var member = MemberFixtureFactory.create();

        var overMaxLengthName = "pnupnupnupnu";

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            member.changeNickname(overMaxLengthName);
        });

    }


}