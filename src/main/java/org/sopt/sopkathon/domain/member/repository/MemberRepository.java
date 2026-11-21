package org.sopt.sopkathon.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.sopt.sopkathon.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("""
            select m from Member m
            join fetch m.schoolClass sc
            join fetch sc.school
            where m.id = :memberId
            """)
    Optional<Member> findWithSchoolClassAndSchoolById(@Param("memberId") Long memberId);

    List<Member> findAllBySchoolClassIdOrderByIdAsc(Long schoolClassId);
}
