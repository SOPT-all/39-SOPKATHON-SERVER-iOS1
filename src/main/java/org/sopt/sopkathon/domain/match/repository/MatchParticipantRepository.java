package org.sopt.sopkathon.domain.match.repository;

import java.util.List;
import org.sopt.sopkathon.domain.match.entity.MatchParticipant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {

    @EntityGraph(attributePaths = "member")
    List<MatchParticipant> findAllByMatchTeamGameMatchId(Long matchId);
}
