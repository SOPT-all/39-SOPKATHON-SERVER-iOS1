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
import org.sopt.sopkathon.domain.school.code.SchoolErrorCode;
import org.sopt.sopkathon.domain.school.entity.School;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;
import org.sopt.sopkathon.domain.school.service.SchoolService;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SchoolService schoolService;

    @InjectMocks
    private MemberService memberService;

    @Test
    void 홈_조회는_내_정보와_요청한_학급의_멤버_목록을_반환한다() {
        School school = createSchool(1L, "성수고등학교");
        SchoolClass myClass = createSchoolClass(10L, school, 1, 1);
        SchoolClass otherClass = createSchoolClass(11L, school, 2, 3);
        Member me = createMember(myClass, 1L, "김민서");
        Member classmate = createMember(otherClass, 7L, "윤지호");
        given(memberRepository.findWithSchoolClassAndSchoolById(1L)).willReturn(Optional.of(me));
        given(schoolService.getSchoolClass(1L, (byte) 2, (short) 3)).willReturn(otherClass);
        given(memberRepository.findAllBySchoolClassIdOrderByIdAsc(11L)).willReturn(List.of(classmate));

        MemberHomeResponse response = memberService.getHome(1L, (byte) 2, (short) 3);

        assertThat(response.me().memberId()).isEqualTo(1L);
        assertThat(response.me().displayName()).isEqualTo("김민서");
        assertThat(response.school().name()).isEqualTo("성수고등학교");
        assertThat(response.schoolClass().schoolClassId()).isEqualTo(11L);
        assertThat(response.schoolClass().grade()).isEqualTo(2);
        assertThat(response.schoolClass().classNumber()).isEqualTo(3);
        assertThat(response.members()).extracting("memberId", "isMe")
                .containsExactly(tuple(7L, false));
    }

    @Test
    void 내_학급을_조회하면_isMe가_내_멤버에만_true다() {
        School school = createSchool(1L, "성수고등학교");
        SchoolClass myClass = createSchoolClass(10L, school, 1, 1);
        Member me = createMember(myClass, 1L, "김민서");
        Member friend = createMember(myClass, 2L, "이서준");
        given(memberRepository.findWithSchoolClassAndSchoolById(1L)).willReturn(Optional.of(me));
        given(schoolService.getSchoolClass(1L, (byte) 1, (short) 1)).willReturn(myClass);
        given(memberRepository.findAllBySchoolClassIdOrderByIdAsc(10L)).willReturn(List.of(me, friend));

        MemberHomeResponse response = memberService.getHome(1L, (byte) 1, (short) 1);

        assertThat(response.members()).extracting("memberId", "isMe")
                .containsExactly(tuple(1L, true), tuple(2L, false));
    }

    @Test
    void 존재하지_않는_멤버면_MEMBER_NOT_FOUND_예외가_발생한다() {
        given(memberRepository.findWithSchoolClassAndSchoolById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getHome(99L, (byte) 1, (short) 1))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    void 존재하지_않는_학급이면_SCHOOL_CLASS_NOT_FOUND_예외가_전파된다() {
        School school = createSchool(1L, "성수고등학교");
        SchoolClass myClass = createSchoolClass(10L, school, 1, 1);
        Member me = createMember(myClass, 1L, "김민서");
        given(memberRepository.findWithSchoolClassAndSchoolById(1L)).willReturn(Optional.of(me));
        given(schoolService.getSchoolClass(1L, (byte) 9, (short) 9))
                .willThrow(new BusinessException(SchoolErrorCode.SCHOOL_CLASS_NOT_FOUND));

        assertThatThrownBy(() -> memberService.getHome(1L, (byte) 9, (short) 9))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(SchoolErrorCode.SCHOOL_CLASS_NOT_FOUND));
    }

    private School createSchool(Long id, String name) {
        School school = new School(name);
        ReflectionTestUtils.setField(school, "id", id);
        return school;
    }

    private SchoolClass createSchoolClass(Long id, School school, int grade, int classNumber) {
        SchoolClass schoolClass = new SchoolClass(school, (byte) grade, (short) classNumber);
        ReflectionTestUtils.setField(schoolClass, "id", id);
        return schoolClass;
    }

    private Member createMember(SchoolClass schoolClass, Long id, String displayName) {
        Member member = new Member(schoolClass, displayName);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
