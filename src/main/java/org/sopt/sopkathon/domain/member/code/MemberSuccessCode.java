package org.sopt.sopkathon.domain.member.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.code.SuccessCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements SuccessCode {

    HOME_FETCHED(HttpStatus.OK, "홈 데이터를 조회했습니다.");

    private final HttpStatus status;
    private final String message;
}
