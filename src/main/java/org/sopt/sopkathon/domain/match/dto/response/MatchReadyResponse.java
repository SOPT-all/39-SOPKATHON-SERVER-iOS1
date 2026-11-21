package org.sopt.sopkathon.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;

public record MatchReadyResponse(
        @Schema(example = "1025") Long matchId,
        @Schema(description = "game_match.status 값. 전원 준비 전 SEARCHING, 전원 준비 완료 후 FOUND", example = "FOUND")
        MatchStatus status,
        @Schema(example = "true") boolean isMineReady,
        @Schema(example = "2") long readyCount,
        @Schema(example = "2") int participantCount,
        @Schema(example = "true") boolean isAllReady
) {
    public static MatchReadyResponse of(GameMatch match, boolean isMineReady, long readyCount, int participantCount) {
        return new MatchReadyResponse(match.getId(), match.getStatus(), isMineReady, readyCount, participantCount,
                readyCount == participantCount);
    }
}
