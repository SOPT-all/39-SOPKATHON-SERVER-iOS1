package org.sopt.sopkathon.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;

public record MatchGameTypeResponse(
        @Schema(example = "1025") Long matchId,
        @Schema(description = "확정된 종목. 확정 전에는 null", example = "HORSE_TAP") GameType gameType,
        @Schema(description = "game_match.status 값", example = "IN_PROGRESS") MatchStatus status,
        @Schema(description = "경기 시작 시각. 확정 전에는 null", example = "2026-11-22T14:00:00+09:00")
        OffsetDateTime startsAt
) {
    public static MatchGameTypeResponse of(GameMatch match) {
        if (!match.hasSelectedGameType()) {
            return new MatchGameTypeResponse(match.getId(), null, match.getStatus(), null);
        }
        return new MatchGameTypeResponse(match.getId(), match.getGameType(), match.getStatus(), match.getStartsAtWithOffset());
    }
}
