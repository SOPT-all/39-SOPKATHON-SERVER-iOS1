package org.sopt.sopkathon.domain.match.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MatchErrorCode implements ErrorCode {

    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경기입니다."),
    INVALID_GAME_TYPE(HttpStatus.BAD_REQUEST, "줄다리기 경기가 아닙니다."),
    MATCH_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "진행 중인 경기가 아닙니다."),
    NOT_MATCH_PARTICIPANT(HttpStatus.FORBIDDEN, "경기 참가자가 아닙니다."),
    GAME_RECORDS_NOT_COMPLETE(HttpStatus.CONFLICT, "아직 기록을 제출하지 않은 참가자가 있습니다.");

    private final HttpStatus status;
    private final String message;
}
