package org.sopt.sopkathon.domain.match.dto.response;

import java.math.BigDecimal;
import java.util.List;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchResult;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;

public record MatchVoiceResultResponse(
        Long matchId,
        GameType gameType,
        MatchStatus status,
        Integer winnerTeamSlot,
        List<TeamResult> teams
) {

    public static MatchVoiceResultResponse of(GameMatch gameMatch, Integer winnerTeamSlot, List<TeamResult> teams) {
        return new MatchVoiceResultResponse(
                gameMatch.getId(),
                gameMatch.getGameType(),
                gameMatch.getStatus(),
                winnerTeamSlot,
                teams);
    }

    public record TeamResult(int teamSlot, BigDecimal totalMaxDecibel, MatchResult result) {
    }
}
