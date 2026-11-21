package org.sopt.sopkathon.domain.match.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.sopt.sopkathon.domain.match.entity.GameType;

public record MatchGameTypeRequest(
        @NotNull(message = "종목은 필수입니다.")
        @Pattern(regexp = "HORSE_TAP|VOICE_TUG", message = "종목은 HORSE_TAP 또는 VOICE_TUG여야 합니다.")
        @Schema(implementation = GameType.class, example = "HORSE_TAP")
        String gameType
) {
}
