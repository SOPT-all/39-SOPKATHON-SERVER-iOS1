package org.sopt.sopkathon.domain.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.member.code.MemberErrorCode;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse;
import org.sopt.sopkathon.domain.member.entity.Member;
import org.sopt.sopkathon.domain.member.repository.MemberRepository;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;
import org.sopt.sopkathon.domain.school.service.SchoolService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final SchoolService schoolService;

    public MemberHomeResponse getHome(Long memberId, byte grade, short classNumber) {
        Member me = memberRepository.findWithSchoolClassAndSchoolById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        Long schoolId = me.getSchoolClass().getSchool().getId();
        SchoolClass schoolClass = schoolService.getSchoolClass(schoolId, grade, classNumber);
        List<Member> classMembers = memberRepository.findAllBySchoolClassIdOrderByIdAsc(schoolClass.getId());
        return MemberHomeResponse.of(me, schoolClass, classMembers);
    }
}
