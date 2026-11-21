package org.sopt.sopkathon.domain.match.repository;

import org.sopt.sopkathon.domain.match.entity.MatchParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchParticipantRepository extends JpaRepository<MatchParticipant, Long> {
}
