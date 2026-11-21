package org.sopt.sopkathon.domain.match.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.sopt.sopkathon.domain.match.dto.request.MatchVoiceRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceResultResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Match", description = "경기 API")
public interface MatchControllerDocs {

    @Operation(
            summary = "줄다리기 기록 제출",
            description = "줄다리기 녹음 구간이 끝난 뒤 호출합니다. 녹음 구간의 최대 데시벨을 내 기록으로 저장하고, "
                    + "모든 참가자가 제출했으면 팀별 승패를 확정해 경기를 종료합니다. "
                    + "VOICE_TUG 경기이고 IN_PROGRESS 상태일 때, 경기 참가자만 제출할 수 있습니다.")
    ResponseEntity<CommonResponse<MatchVoiceRecordSubmitResponse>> submitVoiceRecord(
            @Parameter(description = "경기 ID", example = "1020")
            @Positive(message = "경기 ID는 1 이상이어야 합니다.") Long matchId,
            @Parameter(description = "현재 기기의 시연용 멤버 ID", example = "21102")
            @Positive(message = "멤버 ID는 1 이상이어야 합니다.") Long memberId,
            @Valid MatchVoiceRecordSubmitRequest request);

    @Operation(
            summary = "줄다리기 결과 조회",
            description = "팀별 참가자의 최대 데시벨을 합산해 합계가 더 큰 팀을 승리 팀으로 반환합니다. "
                    + "합계가 같으면 무승부이고 winnerTeamSlot은 null입니다. "
                    + "아직 제출하지 않은 참가자가 있으면 409 GAME_RECORDS_NOT_COMPLETE를 반환합니다.")
    ResponseEntity<CommonResponse<MatchVoiceResultResponse>> getVoiceResult(
            @Parameter(description = "경기 ID", example = "1002")
            @Positive(message = "경기 ID는 1 이상이어야 합니다.") Long matchId);
}
