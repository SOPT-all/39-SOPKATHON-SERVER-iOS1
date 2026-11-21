package org.sopt.sopkathon.domain.match.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.sopt.sopkathon.domain.match.dto.request.HorseRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.HorseRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.HorseResultResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Horse Game", description = "말달리기 게임 API")
public interface HorseGameControllerDocs {

    @Operation(summary = "말달리기 기록 제출",
            description = "진행 중인 말달리기 경기에서 현재 시연 멤버의 완주 시간을 제출합니다. 이미 제출한 기록은 재전송해도 변경하지 않습니다.")
    ResponseEntity<CommonResponse<HorseRecordSubmitResponse>> submitRecord(
            @Parameter(description = "경기 ID", example = "1019")
            @Positive(message = "경기 ID는 양수여야 합니다.") Long matchId,
            @Parameter(description = "현재 기기의 시연용 멤버 ID", example = "11101")
            @Positive(message = "멤버 ID는 양수여야 합니다.") Long memberId,
            @Valid HorseRecordSubmitRequest request
    );

    @Operation(summary = "말달리기 결과 조회",
            description = "양 팀의 모든 참가자가 기록을 제출한 경우, 팀별 완주 시간 합계와 승패를 조회합니다.")
    ResponseEntity<CommonResponse<HorseResultResponse>> getResult(
            @Parameter(description = "경기 ID", example = "1019")
            @Positive(message = "경기 ID는 양수여야 합니다.") Long matchId,
            @Parameter(description = "현재 기기의 시연용 멤버 ID", example = "11101")
            @Positive(message = "멤버 ID는 양수여야 합니다.") Long memberId
    );
}
