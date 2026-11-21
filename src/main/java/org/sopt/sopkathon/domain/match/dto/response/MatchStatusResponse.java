package org.sopt.sopkathon.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;

public record MatchStatusResponse(
        @Schema(example = "1025") Long matchId,
        @Schema(description = "저장된 경기 상태. FINISHED는 결과가 확정된 상태", example = "FINISHED")
        MatchStatus status
) {
}
