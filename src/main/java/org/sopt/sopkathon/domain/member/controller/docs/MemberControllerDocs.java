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
            description = "시연용 멤버 헤더의 멤버가 속한 학교에서 학년·반으로 학급을 찾아, 학교·학급 정보와 그 학급 멤버 목록을 한 번에 조회합니다.")
    ResponseEntity<CommonResponse<MemberHomeResponse>> getHome(
            @Parameter(description = "현재 기기의 시연용 멤버 ID", example = "1")
            @Positive(message = "멤버 ID는 1 이상이어야 합니다.") Long memberId,
            @Parameter(description = "학년", example = "1")
            @Positive(message = "학년은 1 이상이어야 합니다.") Byte grade,
            @Parameter(description = "반 번호", example = "1")
            @Positive(message = "반 번호는 1 이상이어야 합니다.") Short classNumber);
}
