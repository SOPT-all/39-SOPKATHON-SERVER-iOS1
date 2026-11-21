package org.sopt.sopkathon.domain.match.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HorseGameSuccessCode implements SuccessCode {

    HORSE_RECORD_SUBMITTED(HttpStatus.OK, "말달리기 기록을 제출했습니다."),
    HORSE_RESULT_FOUND(HttpStatus.OK, "말달리기 결과를 조회했습니다.");

    private final HttpStatus status;
    private final String message;
}
