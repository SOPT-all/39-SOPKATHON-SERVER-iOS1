package org.sopt.sopkathon.domain.match.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.sopt.sopkathon.domain.match.code.MatchSuccessCode;
import org.sopt.sopkathon.domain.match.controller.docs.MatchControllerDocs;
import org.sopt.sopkathon.domain.match.dto.MatchResultResponse;
import org.sopt.sopkathon.domain.match.dto.request.MatchGameTypeRequest;
import org.sopt.sopkathon.domain.match.dto.request.MatchReadyRequest;
import org.sopt.sopkathon.domain.match.dto.request.MatchVoiceRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.MatchGameTypeResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchGameTypeUpdateResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchReadyResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchStatusResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceResultResponse;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/game-matches/{matchId}")
@RequiredArgsConstructor
public class MatchController implements MatchControllerDocs {

    private final MatchService matchService;

    @Override
    @PatchMapping("/participants/me/ready")
    public ResponseEntity<CommonResponse<MatchReadyResponse>> updateReady(
            @PathVariable Long matchId,
            @RequestHeader("X-Demo-Member-Id") Long memberId,
            @RequestBody MatchReadyRequest request) {
        return CommonResponse.success(MatchSuccessCode.MATCH_READY_UPDATED, matchService.updateReady(matchId, memberId));
    }

    @Override
    @GetMapping("/ready")
    public ResponseEntity<CommonResponse<MatchReadyResponse>> getReady(
            @PathVariable Long matchId,
            @RequestHeader("X-Demo-Member-Id") Long memberId) {
        return CommonResponse.success(MatchSuccessCode.MATCH_READY_FETCHED, matchService.getReady(matchId, memberId));
    }

    @Override
    @PutMapping("/participants/me/voice-record")
    public ResponseEntity<CommonResponse<MatchVoiceRecordSubmitResponse>> submitVoiceRecord(
            @PathVariable Long matchId,
            @RequestHeader("X-Demo-Member-Id") Long memberId,
            @RequestBody MatchVoiceRecordSubmitRequest request) {
        return CommonResponse.success(MatchSuccessCode.VOICE_RECORD_SUBMITTED,
                matchService.submitVoiceRecord(matchId, memberId, request));
    }

    @Override
    @GetMapping("/voice-result")
    public ResponseEntity<CommonResponse<MatchVoiceResultResponse>> getVoiceResult(@PathVariable Long matchId) {
        return CommonResponse.success(MatchSuccessCode.VOICE_RESULT_FETCHED, matchService.getVoiceResult(matchId));
    }

    @Override
    @PatchMapping("/game-type")
    public ResponseEntity<CommonResponse<MatchGameTypeUpdateResponse>> updateGameType(
            @PathVariable Long matchId,
            @RequestHeader("X-Demo-Member-Id") Long memberId,
            @RequestBody MatchGameTypeRequest request) {
        return CommonResponse.success(MatchSuccessCode.GAME_TYPE_RESOLVED,
                matchService.updateGameType(matchId, memberId, GameType.valueOf(request.gameType())));
    }

    @Override
    @GetMapping("/game-type")
    public ResponseEntity<CommonResponse<MatchGameTypeResponse>> getGameType(@PathVariable Long matchId) {
        return CommonResponse.success(MatchSuccessCode.GAME_TYPE_FETCHED, matchService.getGameType(matchId));
    }

    @Override
    @GetMapping("/result")
    public ResponseEntity<CommonResponse<MatchResultResponse>> getMatchResult(
            @PathVariable Long matchId,
            @RequestHeader("X-Demo-Member-Id") Long memberId
    ) {
        MatchResultResponse response = matchService.getMatchResult(matchId, memberId);
        return CommonResponse.success(MatchSuccessCode.MATCH_RESULT_FOUND, response);
    }

    @Override
    @GetMapping("/status")
    public ResponseEntity<CommonResponse<MatchStatusResponse>> getMatchStatus(
            @PathVariable Long matchId,
            @RequestHeader("X-Demo-Member-Id") Long memberId) {
        return CommonResponse.success(MatchSuccessCode.MATCH_STATUS_FETCHED, matchService.getMatchStatus(matchId, memberId));
    }
}
