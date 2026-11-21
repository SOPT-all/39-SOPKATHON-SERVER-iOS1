package org.sopt.sopkathon.domain.match.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MatchErrorCode implements ErrorCode {

    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경기입니다."),
    MATCH_PARTICIPANT_FORBIDDEN(HttpStatus.FORBIDDEN, "경기에 참여한 멤버만 접근할 수 있습니다."),
    MATCH_READY_UPDATE_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 경기 상태에서는 준비 완료로 변경할 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
