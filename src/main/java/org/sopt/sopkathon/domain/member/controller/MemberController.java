package org.sopt.sopkathon.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.sopt.sopkathon.domain.member.code.MemberSuccessCode;
import org.sopt.sopkathon.domain.member.controller.docs.MemberControllerDocs;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse;
import org.sopt.sopkathon.domain.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    @Override
    @GetMapping("/home")
    public ResponseEntity<CommonResponse<MemberHomeResponse>> getHome(
            @RequestHeader("X-Demo-Member-Id") Long memberId) {
        return CommonResponse.success(MemberSuccessCode.HOME_FETCHED, memberService.getHome(memberId));
    }
}
