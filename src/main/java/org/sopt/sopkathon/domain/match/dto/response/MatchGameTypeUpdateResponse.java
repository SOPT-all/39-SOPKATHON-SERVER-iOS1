package org.sopt.sopkathon.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;

public record MatchGameTypeUpdateResponse(
        @Schema(example = "1025") Long matchId,
        @Schema(example = "HORSE_TAP") GameType gameType,
        @Schema(example = "IN_PROGRESS") MatchStatus status,
        @Schema(example = "2026-11-22T14:00:00+09:00") OffsetDateTime startsAt,
        @Schema(description = "이 요청이 종목을 최초 확정했으면 true", example = "true") boolean selectedByCurrentRequest
) {
    public static MatchGameTypeUpdateResponse of(GameMatch match, boolean selectedByCurrentRequest) {
        return new MatchGameTypeUpdateResponse(match.getId(), match.getGameType(), match.getStatus(),
                match.getStartsAtWithOffset(), selectedByCurrentRequest);
    }
}
