package org.sopt.sopkathon.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.common.code.GlobalErrorCode;
import org.sopt.sopkathon.common.code.SuccessCode;
import org.sopt.sopkathon.common.dto.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 성공_응답은_CommonResponse_형식이다() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("TEST_OK"))
                .andExpect(jsonPath("$.data").value("hello"));
    }

    @Test
    void 비즈니스_예외는_에러코드_상태로_응답한다() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(GlobalErrorCode.NOT_FOUND.getMessage()));
    }

    @Test
    void 검증_실패는_400과_한글_메시지로_응답한다() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("이름은 필수입니다."));
    }

    @Test
    void 읽을_수_없는_본문은_400이다() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 필수_파라미터_누락은_400이다() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
    }

    @Test
    void 파라미터_타입_불일치는_400이다() throws Exception {
        mockMvc.perform(get("/test/param").param("count", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER_TYPE"));
    }

    @Test
    void 지원하지_않는_메서드는_405이다() throws Exception {
        mockMvc.perform(post("/test/ok"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void 예상하지_못한_예외는_내부_메시지_없이_500이다() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value(GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    enum TestSuccessCode implements SuccessCode {
        TEST_OK(HttpStatus.OK, "성공");

        private final HttpStatus status;
        private final String message;

        TestSuccessCode(HttpStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        @Override
        public HttpStatus getStatus() {
            return status;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    record TestRequest(@NotBlank(message = "이름은 필수입니다.") String name) {
    }

    @RestController
    static class TestController {

        @GetMapping("/test/ok")
        ResponseEntity<CommonResponse<String>> ok() {
            return CommonResponse.success(TestSuccessCode.TEST_OK, "hello");
        }

        @GetMapping("/test/business")
        void business() {
            throw new BusinessException(GlobalErrorCode.NOT_FOUND);
        }

        @PostMapping("/test/valid")
        void valid(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/param")
        void param(@RequestParam Integer count) {
        }

        @GetMapping("/test/boom")
        void boom() {
            throw new IllegalStateException("internal secret");
        }
    }
}
