package org.sopt.sopkathon.domain.match.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record MatchReadyRequest(
        @NotNull(message = "준비 여부는 필수입니다.")
        @AssertTrue(message = "준비 여부는 true여야 합니다.")
        @Schema(example = "true", allowableValues = "true")
        Boolean isReady
) {
}
