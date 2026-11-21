package org.sopt.sopkathon.domain.member.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Member", description = "멤버·홈 API")
public interface MemberControllerDocs {

    @Operation(
            summary = "홈 데이터 조회",
            description = "시연용 멤버 헤더로 내 정보를 정하고, 내 학교·학급과 같은 학급 멤버 목록을 한 번에 조회합니다.")
    ResponseEntity<CommonResponse<MemberHomeResponse>> getHome(
            @Parameter(description = "현재 기기의 시연용 멤버 ID", example = "1")
            @Positive(message = "멤버 ID는 1 이상이어야 합니다.") Long memberId);
}
