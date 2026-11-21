package org.sopt.sopkathon.domain.school.repository;

import java.util.Optional;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    Optional<SchoolClass> findBySchoolIdAndGradeAndClassNumber(Long schoolId, byte grade, short classNumber);
}
