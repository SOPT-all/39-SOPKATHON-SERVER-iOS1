package org.sopt.sopkathon.domain.member.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.common.exception.GlobalExceptionHandler;
import org.sopt.sopkathon.domain.member.code.MemberErrorCode;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse.MeInfo;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse.MemberInfo;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse.SchoolClassInfo;
import org.sopt.sopkathon.domain.member.dto.response.MemberHomeResponse.SchoolInfo;
import org.sopt.sopkathon.domain.member.service.MemberService;
import org.sopt.sopkathon.domain.school.code.SchoolErrorCode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    private static final String DEMO_MEMBER_HEADER = "X-Demo-Member-Id";

    @Mock
    private MemberService memberService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MemberController(memberService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 홈_조회_성공시_200과_HOME_FETCHED를_반환한다() throws Exception {
        MemberHomeResponse response = new MemberHomeResponse(
                new MeInfo(1L, "김민서"),
                new SchoolInfo(1L, "성수고등학교"),
                new SchoolClassInfo(1L, 1, 1),
                List.of(new MemberInfo(1L, "김민서", true), new MemberInfo(2L, "이서준", false)));
        given(memberService.getHome(1L, (byte) 1, (short) 1)).willReturn(response);

        mockMvc.perform(get("/api/v1/home")
                        .header(DEMO_MEMBER_HEADER, 1)
                        .param("grade", "1")
                        .param("classNumber", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("HOME_FETCHED"))
                .andExpect(jsonPath("$.data.me.memberId").value(1))
                .andExpect(jsonPath("$.data.school.name").value("성수고등학교"))
                .andExpect(jsonPath("$.data.schoolClass.grade").value(1))
                .andExpect(jsonPath("$.data.members[0].isMe").value(true))
                .andExpect(jsonPath("$.data.members[1].isMe").value(false));
    }

    @Test
    void 헤더가_없으면_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/home").param("grade", "1").param("classNumber", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("'X-Demo-Member-Id' 헤더는 필수입니다."));
    }

    @Test
    void 학년이_없으면_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/home").header(DEMO_MEMBER_HEADER, 1).param("classNumber", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("'grade' 파라미터는 필수입니다."));
    }

    @Test
    void 반_번호가_없으면_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/home").header(DEMO_MEMBER_HEADER, 1).param("grade", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("'classNumber' 파라미터는 필수입니다."));
    }

    @Test
    void 헤더가_숫자가_아니면_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/home")
                        .header(DEMO_MEMBER_HEADER, "abc")
                        .param("grade", "1")
                        .param("classNumber", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"));
    }

    @Test
    void 학년이_숫자가_아니거나_범위를_넘으면_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/home")
                        .header(DEMO_MEMBER_HEADER, 1)
                        .param("grade", "abc")
                        .param("classNumber", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"));
        mockMvc.perform(get("/api/v1/home")
                        .header(DEMO_MEMBER_HEADER, 1)
                        .param("grade", "1000")
                        .param("classNumber", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"));
    }

    @Test
    void 헤더가_1_미만이면_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/home")
                        .header(DEMO_MEMBER_HEADER, 0)
                        .param("grade", "1")
                        .param("classNumber", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("멤버 ID는 1 이상이어야 합니다."));
    }

    @Test
    void 학년이_1_미만이면_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/home")
                        .header(DEMO_MEMBER_HEADER, 1)
                        .param("grade", "0")
                        .param("classNumber", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("학년은 1 이상이어야 합니다."));
    }

    @Test
    void 존재하지_않는_멤버면_404로_응답한다() throws Exception {
        given(memberService.getHome(99L, (byte) 1, (short) 1))
                .willThrow(new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/home")
                        .header(DEMO_MEMBER_HEADER, 99)
                        .param("grade", "1")
                        .param("classNumber", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("MEMBER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 멤버입니다."));
    }

    @Test
    void 존재하지_않는_학급이면_404로_응답한다() throws Exception {
        given(memberService.getHome(1L, (byte) 9, (short) 9))
                .willThrow(new BusinessException(SchoolErrorCode.SCHOOL_CLASS_NOT_FOUND));

        mockMvc.perform(get("/api/v1/home")
                        .header(DEMO_MEMBER_HEADER, 1)
                        .param("grade", "9")
                        .param("classNumber", "9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHOOL_CLASS_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 학급입니다."));
    }
}
