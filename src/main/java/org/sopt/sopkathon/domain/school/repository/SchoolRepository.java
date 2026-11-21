package org.sopt.sopkathon.domain.school.repository;

import org.sopt.sopkathon.domain.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
}
