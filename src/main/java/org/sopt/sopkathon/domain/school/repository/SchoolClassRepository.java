package org.sopt.sopkathon.domain.school.repository;

import org.sopt.sopkathon.domain.school.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
}
