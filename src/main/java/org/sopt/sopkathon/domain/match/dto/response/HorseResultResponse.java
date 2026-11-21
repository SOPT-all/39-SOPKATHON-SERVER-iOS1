package org.sopt.sopkathon.domain.match.dto.response;

import java.util.List;
import org.sopt.sopkathon.domain.match.entity.MatchResult;

public record HorseResultResponse(
        Long matchId,
        Byte winnerTeamSlot,
        List<TeamResult> teams
) {

    public record TeamResult(
            Byte teamSlot,
            String schoolClassName,
            MatchResult result,
            Long totalFinishTimeMilliseconds
    ) {
    }
}
