package org.sopt.sopkathon.domain.match.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.sopt.sopkathon.domain.match.code.MatchSuccessCode;
import org.sopt.sopkathon.domain.match.controller.docs.MatchControllerDocs;
import org.sopt.sopkathon.domain.match.dto.request.MatchVoiceRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceResultResponse;
import org.sopt.sopkathon.domain.match.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/game-matches")
@RequiredArgsConstructor
public class MatchController implements MatchControllerDocs {

    private final MatchService matchService;

    @Override
    @PutMapping("/{matchId}/participants/me/voice-record")
    public ResponseEntity<CommonResponse<MatchVoiceRecordSubmitResponse>> submitVoiceRecord(
            @PathVariable Long matchId,
            @RequestHeader("X-Demo-Member-Id") Long memberId,
            @RequestBody MatchVoiceRecordSubmitRequest request) {
        return CommonResponse.success(MatchSuccessCode.VOICE_RECORD_SUBMITTED,
                matchService.submitVoiceRecord(matchId, memberId, request));
    }

    @Override
    @GetMapping("/{matchId}/voice-result")
    public ResponseEntity<CommonResponse<MatchVoiceResultResponse>> getVoiceResult(@PathVariable Long matchId) {
        return CommonResponse.success(MatchSuccessCode.VOICE_RESULT_FETCHED, matchService.getVoiceResult(matchId));
    }
}
