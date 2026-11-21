package org.sopt.sopkathon.domain.match.repository;

import java.util.List;
import org.sopt.sopkathon.domain.match.entity.MatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {

    List<MatchTeam> findAllByGameMatchIdOrderByTeamSlot(Long gameMatchId);
}
