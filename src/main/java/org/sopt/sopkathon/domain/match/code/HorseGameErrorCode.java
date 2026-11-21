package org.sopt.sopkathon.domain.match.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HorseGameErrorCode implements ErrorCode {

    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "경기를 찾을 수 없습니다."),
    MATCH_PARTICIPANT_FORBIDDEN(HttpStatus.FORBIDDEN, "경기 참가자만 기록을 제출하거나 결과를 조회할 수 있습니다."),
    HORSE_GAME_TYPE_MISMATCH(HttpStatus.CONFLICT, "말달리기 경기가 아닙니다."),
    HORSE_GAME_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "진행 중인 말달리기 경기에서만 기록을 제출할 수 있습니다."),
    HORSE_RESULT_NOT_READY(HttpStatus.CONFLICT, "모든 참가자의 말달리기 기록이 제출되지 않았습니다."),
    HORSE_RESULT_DATA_INVALID(HttpStatus.CONFLICT, "말달리기 결과를 계산할 데이터가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;
}
