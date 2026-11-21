package org.sopt.sopkathon.domain.match.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.sopt.sopkathon.domain.match.code.HorseGameSuccessCode;
import org.sopt.sopkathon.domain.match.controller.docs.HorseGameControllerDocs;
import org.sopt.sopkathon.domain.match.dto.request.HorseRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.HorseRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.HorseResultResponse;
import org.sopt.sopkathon.domain.match.service.HorseGameService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/game-matches")
public class HorseGameController implements HorseGameControllerDocs {

    private final HorseGameService horseGameService;

    @Override
    @PutMapping("/{matchId}/participants/me/horse-record")
    public ResponseEntity<CommonResponse<HorseRecordSubmitResponse>> submitRecord(
            @PathVariable @Positive(message = "경기 ID는 양수여야 합니다.") Long matchId,
            @RequestHeader("X-Demo-Member-Id") @Positive(message = "멤버 ID는 양수여야 합니다.") Long memberId,
            @RequestBody @Valid HorseRecordSubmitRequest request
    ) {
        return CommonResponse.success(HorseGameSuccessCode.HORSE_RECORD_SUBMITTED,
                horseGameService.submitRecord(matchId, memberId, request));
    }

    @Override
    @GetMapping("/{matchId}/horse-result")
    public ResponseEntity<CommonResponse<HorseResultResponse>> getResult(
            @PathVariable @Positive(message = "경기 ID는 양수여야 합니다.") Long matchId,
            @RequestHeader("X-Demo-Member-Id") @Positive(message = "멤버 ID는 양수여야 합니다.") Long memberId
    ) {
        return CommonResponse.success(HorseGameSuccessCode.HORSE_RESULT_FOUND,
                horseGameService.getResult(matchId, memberId));
    }
}
