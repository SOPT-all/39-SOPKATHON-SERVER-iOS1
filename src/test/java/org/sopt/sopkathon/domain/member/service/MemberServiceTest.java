package org.sopt.sopkathon.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.member.code.MemberErrorCode;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse;
import org.sopt.sopkathon.domain.member.entity.Member;
import org.sopt.sopkathon.domain.member.repository.MemberRepository;
import org.sopt.sopkathon.domain.school.entity.School;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void 홈_조회는_내_정보와_학급_멤버_목록을_반환한다() {
        School school = new School("성수고등학교");
        ReflectionTestUtils.setField(school, "id", 1L);
        SchoolClass schoolClass = new SchoolClass(school, (byte) 1, (short) 1);
        ReflectionTestUtils.setField(schoolClass, "id", 10L);
        Member me = createMember(schoolClass, 1L, "김민서");
        Member friend = createMember(schoolClass, 2L, "이서준");
        given(memberRepository.findWithSchoolClassAndSchoolById(1L)).willReturn(Optional.of(me));
        given(memberRepository.findAllBySchoolClassIdOrderByIdAsc(10L)).willReturn(List.of(me, friend));

        MemberHomeResponse response = memberService.getHome(1L);

        assertThat(response.me().memberId()).isEqualTo(1L);
        assertThat(response.me().displayName()).isEqualTo("김민서");
        assertThat(response.school().name()).isEqualTo("성수고등학교");
        assertThat(response.schoolClass().schoolClassId()).isEqualTo(10L);
        assertThat(response.schoolClass().grade()).isEqualTo(1);
        assertThat(response.schoolClass().classNumber()).isEqualTo(1);
        assertThat(response.members()).extracting("memberId", "isMe")
                .containsExactly(
                        tuple(1L, true),
                        tuple(2L, false));
    }

    @Test
    void 존재하지_않는_멤버면_MEMBER_NOT_FOUND_예외가_발생한다() {
        given(memberRepository.findWithSchoolClassAndSchoolById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getHome(99L))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private Member createMember(SchoolClass schoolClass, Long id, String displayName) {
        Member member = new Member(schoolClass, displayName);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
