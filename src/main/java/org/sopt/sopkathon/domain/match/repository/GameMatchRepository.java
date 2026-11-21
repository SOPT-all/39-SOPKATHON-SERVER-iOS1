package org.sopt.sopkathon.domain.match.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameMatchRepository extends JpaRepository<GameMatch, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select match from GameMatch match where match.id = :matchId")
    Optional<GameMatch> findByIdForUpdate(@Param("matchId") Long matchId);
}
