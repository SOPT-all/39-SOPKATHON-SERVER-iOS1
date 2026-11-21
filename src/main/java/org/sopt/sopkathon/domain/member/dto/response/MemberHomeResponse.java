package org.sopt.sopkathon.domain.member.dto.response;

import java.util.List;
import org.sopt.sopkathon.domain.member.entity.Member;
import org.sopt.sopkathon.domain.school.entity.School;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;

public record MemberHomeResponse(
        MeInfo me,
        SchoolInfo school,
        SchoolClassInfo schoolClass,
        List<MemberInfo> members
) {

    public static MemberHomeResponse of(Member me, List<Member> classMembers) {
        SchoolClass schoolClass = me.getSchoolClass();
        return new MemberHomeResponse(
                MeInfo.from(me),
                SchoolInfo.from(schoolClass.getSchool()),
                SchoolClassInfo.from(schoolClass),
                classMembers.stream()
                        .map(member -> MemberInfo.of(member, me))
                        .toList());
    }

    public record MeInfo(Long memberId, String displayName) {

        static MeInfo from(Member member) {
            return new MeInfo(member.getId(), member.getDisplayName());
        }
    }

    public record SchoolInfo(Long schoolId, String name) {

        static SchoolInfo from(School school) {
            return new SchoolInfo(school.getId(), school.getName());
        }
    }

    public record SchoolClassInfo(Long schoolClassId, int grade, int classNumber) {

        static SchoolClassInfo from(SchoolClass schoolClass) {
            return new SchoolClassInfo(schoolClass.getId(), schoolClass.getGrade(), schoolClass.getClassNumber());
        }
    }

    public record MemberInfo(Long memberId, String displayName, boolean isMe) {

        static MemberInfo of(Member member, Member me) {
            return new MemberInfo(member.getId(), member.getDisplayName(), member.getId().equals(me.getId()));
        }
    }
}
