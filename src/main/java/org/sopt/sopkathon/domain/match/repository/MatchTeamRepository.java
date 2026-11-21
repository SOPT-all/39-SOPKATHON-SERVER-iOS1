package org.sopt.sopkathon.domain.match.repository;

import org.sopt.sopkathon.domain.match.entity.MatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {
}
