package org.sopt.sopkathon.domain.match.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MatchVoiceRecordSubmitRequest(
        @NotNull(message = "최대 데시벨은 필수입니다.")
        @DecimalMin(value = "0.0", message = "최대 데시벨은 0 이상이어야 합니다.")
        @Digits(integer = 4, fraction = 3, message = "최대 데시벨은 정수 4자리, 소수 3자리까지 입력할 수 있습니다.")
        BigDecimal maxDecibel
) {
}
