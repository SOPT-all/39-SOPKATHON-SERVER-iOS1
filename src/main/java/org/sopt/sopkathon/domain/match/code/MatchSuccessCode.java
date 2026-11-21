package org.sopt.sopkathon.domain.match.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MatchSuccessCode implements SuccessCode {

    VOICE_RECORD_SUBMITTED(HttpStatus.OK, "줄다리기 기록을 제출했습니다."),
    VOICE_RESULT_FETCHED(HttpStatus.OK, "줄다리기 결과를 조회했습니다."),
    MATCH_READY_UPDATED(HttpStatus.OK, "준비 완료 상태를 반영했습니다."),
    MATCH_READY_FETCHED(HttpStatus.OK, "경기의 준비 상태를 조회했습니다."),
    GAME_TYPE_RESOLVED(HttpStatus.OK, "경기의 확정 종목을 반환했습니다."),
    GAME_TYPE_FETCHED(HttpStatus.OK, "경기의 종목 상태를 조회했습니다."),
    MATCH_STATUS_FETCHED(HttpStatus.OK, "경기 상태를 조회했습니다."),
    MATCH_RESULT_FOUND(HttpStatus.OK, "경기 결과를 조회했습니다.");

    private final HttpStatus status;
    private final String message;
}
