package org.sopt.sopkathon.domain.school.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SchoolErrorCode implements ErrorCode {

    SCHOOL_CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 학급입니다.");

    private final HttpStatus status;
    private final String message;
}
