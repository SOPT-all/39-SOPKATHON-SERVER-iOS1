package org.sopt.sopkathon.domain.match.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.common.exception.GlobalExceptionHandler;
import org.sopt.sopkathon.domain.match.code.MatchErrorCode;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceResultResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceResultResponse.TeamResult;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchResult;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;
import org.sopt.sopkathon.domain.match.entity.RecordStatus;
import org.sopt.sopkathon.domain.match.service.MatchService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    private static final String HEADER = "X-Demo-Member-Id";
    private static final String SUBMIT_URL = "/api/v1/game-matches/102/participants/me/voice-record";
    private static final String RESULT_URL = "/api/v1/game-matches/102/voice-result";

    @Mock
    private MatchService matchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MatchController(matchService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 기록_제출_성공시_200과_VOICE_RECORD_SUBMITTED를_반환한다() throws Exception {
        given(matchService.submitVoiceRecord(eq(102L), eq(1L), any())).willReturn(
                new MatchVoiceRecordSubmitResponse(102L, 1L, new BigDecimal("82.375"), RecordStatus.SUBMITTED, false));

        mockMvc.perform(put(SUBMIT_URL).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxDecibel\": 82.375}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("VOICE_RECORD_SUBMITTED"))
                .andExpect(jsonPath("$.data.matchId").value(102))
                .andExpect(jsonPath("$.data.memberId").value(1))
                .andExpect(jsonPath("$.data.maxDecibel").value(82.375))
                .andExpect(jsonPath("$.data.recordStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.isResultFinalized").value(false));
    }

    @Test
    void 음수_데시벨은_400으로_응답한다() throws Exception {
        mockMvc.perform(put(SUBMIT_URL).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxDecibel\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("최대 데시벨은 0 이상이어야 합니다."));
    }

    @Test
    void 데시벨이_없으면_400으로_응답한다() throws Exception {
        mockMvc.perform(put(SUBMIT_URL).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("최대 데시벨은 필수입니다."));
    }

    @Test
    void 소수_4자리_데시벨은_400으로_응답한다() throws Exception {
        mockMvc.perform(put(SUBMIT_URL).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxDecibel\": 82.3751}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 헤더가_없으면_400으로_응답한다() throws Exception {
        mockMvc.perform(put(SUBMIT_URL).contentType(MediaType.APPLICATION_JSON).content("{\"maxDecibel\": 80}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
    }

    @Test
    void 요청_본문이_숫자가_아니면_400으로_응답한다() throws Exception {
        mockMvc.perform(put(SUBMIT_URL).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxDecibel\": \"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 경기_참가자가_아니면_403으로_응답한다() throws Exception {
        given(matchService.submitVoiceRecord(eq(102L), eq(9L), any()))
                .willThrow(new BusinessException(MatchErrorCode.NOT_MATCH_PARTICIPANT));

        mockMvc.perform(put(SUBMIT_URL).header(HEADER, 9).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxDecibel\": 80}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_MATCH_PARTICIPANT"));
    }

    @Test
    void 진행_중이_아닌_경기는_409로_응답한다() throws Exception {
        given(matchService.submitVoiceRecord(eq(102L), eq(1L), any()))
                .willThrow(new BusinessException(MatchErrorCode.MATCH_NOT_IN_PROGRESS));

        mockMvc.perform(put(SUBMIT_URL).header(HEADER, 1).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxDecibel\": 80}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MATCH_NOT_IN_PROGRESS"));
    }

    @Test
    void 결과_조회_성공시_팀별_합계와_승리_팀을_반환한다() throws Exception {
        given(matchService.getVoiceResult(102L)).willReturn(new MatchVoiceResultResponse(
                102L, GameType.VOICE_TUG, MatchStatus.FINISHED, 2,
                List.of(new TeamResult(1, new BigDecimal("149.200"), MatchResult.LOSE),
                        new TeamResult(2, new BigDecimal("157.875"), MatchResult.WIN))));

        mockMvc.perform(get(RESULT_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("VOICE_RESULT_FETCHED"))
                .andExpect(jsonPath("$.data.gameType").value("VOICE_TUG"))
                .andExpect(jsonPath("$.data.status").value("FINISHED"))
                .andExpect(jsonPath("$.data.winnerTeamSlot").value(2))
                .andExpect(jsonPath("$.data.teams[0].teamSlot").value(1))
                .andExpect(jsonPath("$.data.teams[0].totalMaxDecibel").value(149.2))
                .andExpect(jsonPath("$.data.teams[0].result").value("LOSE"))
                .andExpect(jsonPath("$.data.teams[1].result").value("WIN"));
    }

    @Test
    void 미제출자가_있으면_결과_조회는_409로_응답한다() throws Exception {
        given(matchService.getVoiceResult(102L))
                .willThrow(new BusinessException(MatchErrorCode.GAME_RECORDS_NOT_COMPLETE));

        mockMvc.perform(get(RESULT_URL))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("GAME_RECORDS_NOT_COMPLETE"));
    }

    @Test
    void 경기_ID가_1_미만이면_400으로_응답한다() throws Exception {
        mockMvc.perform(get("/api/v1/game-matches/0/voice-result"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("경기 ID는 1 이상이어야 합니다."));
    }
}
