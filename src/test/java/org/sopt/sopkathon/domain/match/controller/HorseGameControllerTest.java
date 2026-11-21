package org.sopt.sopkathon.domain.match.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.common.exception.GlobalExceptionHandler;
import org.sopt.sopkathon.domain.match.dto.request.HorseRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.HorseRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.HorseResultResponse;
import org.sopt.sopkathon.domain.match.dto.response.HorseResultResponse.TeamResult;
import org.sopt.sopkathon.domain.match.entity.MatchResult;
import org.sopt.sopkathon.domain.match.service.HorseGameService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class HorseGameControllerTest {

    private static final String DEMO_MEMBER_HEADER = "X-Demo-Member-Id";

    @Mock
    private HorseGameService horseGameService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HorseGameController(horseGameService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 말달리기_기록_제출에_성공하면_200을_반환한다() throws Exception {
        HorseRecordSubmitResponse response = new HorseRecordSubmitResponse(1019L, 3121L, 1_200L);
        given(horseGameService.submitRecord(1019L, 11101L, new HorseRecordSubmitRequest(1_200L)))
                .willReturn(response);

        mockMvc.perform(put("/api/v1/game-matches/1019/participants/me/horse-record")
                        .header(DEMO_MEMBER_HEADER, 11101)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"elapsedMilliseconds":1200}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("HORSE_RECORD_SUBMITTED"))
                .andExpect(jsonPath("$.data.matchId").value(1019))
                .andExpect(jsonPath("$.data.finishTimeMilliseconds").value(1200));
    }

    @Test
    void 말달리기_결과_조회에_성공하면_200과_팀별_합계를_반환한다() throws Exception {
        HorseResultResponse response = new HorseResultResponse(
                1019L,
                (byte) 1,
                List.of(
                        new TeamResult((byte) 1, "성수고등학교 1학년 1반", MatchResult.WIN, 2_100L),
                        new TeamResult((byte) 2, "휘문중학교 3학년 3반", MatchResult.LOSE, 2_500L)
                )
        );
        given(horseGameService.getResult(1019L, 11101L)).willReturn(response);

        mockMvc.perform(get("/api/v1/game-matches/1019/horse-result")
                        .header(DEMO_MEMBER_HEADER, 11101))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("HORSE_RESULT_FOUND"))
                .andExpect(jsonPath("$.data.winnerTeamSlot").value(1))
                .andExpect(jsonPath("$.data.teams[0].result").value("WIN"))
                .andExpect(jsonPath("$.data.teams[0].totalFinishTimeMilliseconds").value(2100))
                .andExpect(jsonPath("$.data.teams[1].result").value("LOSE"));
    }
}
