package org.sopt.sopkathon.domain.match.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HorseRecordSubmitRequest(
        @NotNull(message = "완주 시간은 필수입니다.")
        @Positive(message = "완주 시간은 0보다 커야 합니다.")
        Long elapsedMilliseconds
) {
}
