package org.sopt.sopkathon.domain.match.dto;

import java.math.BigDecimal;
import java.util.List;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchResult;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;

public record MatchResultResponse(
        Long matchId,
        MatchStatus status,
        GameType gameType,
        Byte winnerTeamSlot,
        List<TeamResultResponse> teams
) {

    public record TeamResultResponse(
            Byte teamSlot,
            String schoolClassName,
            MatchResult result,
            Long totalFinishTimeMilliseconds,
            BigDecimal totalMaxDecibel
    ) {
    }
}
