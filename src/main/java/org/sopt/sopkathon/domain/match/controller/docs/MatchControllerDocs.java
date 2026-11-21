package org.sopt.sopkathon.domain.match.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.sopt.sopkathon.domain.match.dto.request.MatchGameTypeRequest;
import org.sopt.sopkathon.domain.match.dto.request.MatchReadyRequest;
import org.sopt.sopkathon.domain.match.dto.response.MatchGameTypeResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchGameTypeUpdateResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchReadyResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Match", description = "경기 API")
public interface MatchControllerDocs {

    @Operation(summary = "내 준비 완료 처리",
            description = "시연용 멤버 헤더로 식별한 참가자를 준비 완료로 변경합니다. 전원이 준비되면 경기 상태가 FOUND가 됩니다.")
    @ApiResponse(responseCode = "200", description = "MATCH_READY_UPDATED")
    @ApiResponse(responseCode = "400", description = "헤더, 경로 또는 준비 요청 값이 올바르지 않음")
    @ApiResponse(responseCode = "403", description = "MATCH_PARTICIPANT_FORBIDDEN")
    @ApiResponse(responseCode = "404", description = "MATCH_NOT_FOUND")
    @ApiResponse(responseCode = "409", description = "MATCH_READY_UPDATE_NOT_ALLOWED")
    ResponseEntity<CommonResponse<MatchReadyResponse>> updateReady(
            @Parameter(description = "경기 ID", example = "1025")
            @Positive(message = "경기 ID는 1 이상이어야 합니다.") Long matchId,
            @Parameter(description = "현재 기기의 시연용 멤버 ID", example = "11101")
            @Positive(message = "멤버 ID는 1 이상이어야 합니다.") Long memberId,
            @Valid MatchReadyRequest request);

    @Operation(summary = "경기 준비 상태 조회",
            description = "준비 화면에서 3초마다 조회합니다. SEARCHING이면 화면을 유지하고 FOUND이면 종목 선택 화면으로 이동합니다.")
    @ApiResponse(responseCode = "200", description = "MATCH_READY_FETCHED")
    @ApiResponse(responseCode = "400", description = "헤더 또는 경기 ID가 올바르지 않음")
    @ApiResponse(responseCode = "403", description = "MATCH_PARTICIPANT_FORBIDDEN")
    @ApiResponse(responseCode = "404", description = "MATCH_NOT_FOUND")
    ResponseEntity<CommonResponse<MatchReadyResponse>> getReady(
            @Parameter(description = "경기 ID", example = "1025")
            @Positive(message = "경기 ID는 1 이상이어야 합니다.") Long matchId,
            @Parameter(description = "현재 기기의 시연용 멤버 ID", example = "11101")
            @Positive(message = "멤버 ID는 1 이상이어야 합니다.") Long memberId);

    @Operation(summary = "선착순 종목 확정",
            description = "전원 준비된 경기에서 처음 확정한 종목으로 경기를 시작합니다. 이후 요청에는 같은 종목과 시작 시각을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "GAME_TYPE_RESOLVED")
    @ApiResponse(responseCode = "400", description = "헤더, 경기 ID 또는 종목 값이 올바르지 않음")
    @ApiResponse(responseCode = "403", description = "MATCH_PARTICIPANT_FORBIDDEN")
    @ApiResponse(responseCode = "404", description = "MATCH_NOT_FOUND")
    @ApiResponse(responseCode = "409", description = "MATCH_NOT_READY 또는 MATCH_GAME_TYPE_SELECTION_NOT_ALLOWED")
    ResponseEntity<CommonResponse<MatchGameTypeUpdateResponse>> updateGameType(
            @Parameter(description = "경기 ID", example = "1025")
            @Positive(message = "경기 ID는 1 이상이어야 합니다.") Long matchId,
            @Parameter(description = "현재 기기의 시연용 멤버 ID", example = "11101")
            @Positive(message = "멤버 ID는 1 이상이어야 합니다.") Long memberId,
            @Valid MatchGameTypeRequest request);

    @Operation(summary = "확정 종목 조회",
            description = "종목 선택 화면에서 3초마다 조회합니다. IN_PROGRESS와 확정 종목을 받으면 해당 게임 화면으로 이동합니다.")
    @ApiResponse(responseCode = "200", description = "GAME_TYPE_FETCHED")
    @ApiResponse(responseCode = "400", description = "경기 ID가 올바르지 않음")
    @ApiResponse(responseCode = "404", description = "MATCH_NOT_FOUND")
    ResponseEntity<CommonResponse<MatchGameTypeResponse>> getGameType(
            @Parameter(description = "경기 ID", example = "1025")
            @Positive(message = "경기 ID는 1 이상이어야 합니다.") Long matchId);
}
