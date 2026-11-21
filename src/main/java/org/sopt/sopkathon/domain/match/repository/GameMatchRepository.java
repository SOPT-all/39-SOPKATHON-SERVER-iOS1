package org.sopt.sopkathon.domain.match.repository;

import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameMatchRepository extends JpaRepository<GameMatch, Long> {
}
