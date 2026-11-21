package org.sopt.sopkathon.domain.match.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.common.exception.GlobalExceptionHandler;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;
import org.sopt.sopkathon.domain.match.repository.GameMatchRepository;
import org.sopt.sopkathon.domain.match.repository.MatchParticipantRepository;
import org.sopt.sopkathon.domain.match.service.MatchService;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MatchGameTypeControllerTest {

    private static final long MATCH_ID = 1025L;
    private static final long FIRST_MEMBER_ID = 11101L;
    private static final long SECOND_MEMBER_ID = 12201L;
    private static final String GAME_TYPE_URL = "/api/v1/game-matches/{matchId}/game-type";
    private static final String DEMO_MEMBER_HEADER = "X-Demo-Member-Id";
    private static final String HORSE_TAP_REQUEST = "{\"gameType\":\"HORSE_TAP\"}";
    private static final String VOICE_TUG_REQUEST = "{\"gameType\":\"VOICE_TUG\"}";

    @Mock
    private GameMatchRepository gameMatchRepository;

    @Mock
    private MatchParticipantRepository matchParticipantRepository;

    private MockMvc mockMvc;
    private GameMatch match;

    @BeforeEach
    void setUp() {
        match = new GameMatch(null, (byte) 1);
        ReflectionTestUtils.setField(match, "id", MATCH_ID);
        mockMvc = MockMvcBuilders.standaloneSetup(new MatchController(
                        new MatchService(gameMatchRepository, matchParticipantRepository)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 처음_선택된_종목과_시작_시각을_이후_선택과_폴링에도_반환한다() throws Exception {
        match.completePreparation();
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(match));
        given(gameMatchRepository.findByIdForUpdate(MATCH_ID)).willReturn(Optional.of(match));
        given(matchParticipantRepository.existsByMatchTeamGameMatchIdAndMemberId(MATCH_ID, FIRST_MEMBER_ID))
                .willReturn(true);
        given(matchParticipantRepository.existsByMatchTeamGameMatchIdAndMemberId(MATCH_ID, SECOND_MEMBER_ID))
                .willReturn(true);

        mockMvc.perform(get(GAME_TYPE_URL, MATCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GAME_TYPE_FETCHED"))
                .andExpect(jsonPath("$.data.status").value("FOUND"))
                .andExpect(jsonPath("$.data.gameType").value(nullValue()))
                .andExpect(jsonPath("$.data.startsAt").value(nullValue()));

        String firstResponse = updateGameType(FIRST_MEMBER_ID, HORSE_TAP_REQUEST)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GAME_TYPE_RESOLVED"))
                .andExpect(jsonPath("$.data.gameType").value("HORSE_TAP"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.selectedByCurrentRequest").value(true))
                .andExpect(jsonPath("$.data.startsAt").value(endsWith("+09:00")))
                .andReturn().getResponse().getContentAsString();
        String startsAt = JsonPath.read(firstResponse, "$.data.startsAt");

        updateGameType(SECOND_MEMBER_ID, VOICE_TUG_REQUEST)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.gameType").value("HORSE_TAP"))
                .andExpect(jsonPath("$.data.startsAt").value(startsAt))
                .andExpect(jsonPath("$.data.selectedByCurrentRequest").value(false));

        mockMvc.perform(get(GAME_TYPE_URL, MATCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matchId").value((int) MATCH_ID))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.gameType").value("HORSE_TAP"))
                .andExpect(jsonPath("$.data.startsAt").value(startsAt))
                .andExpect(jsonPath("$.data.selectedByCurrentRequest").doesNotHaveJsonPath());
    }

    @Test
    void 준비_전이거나_이미_종목이_채워진_경기의_종목은_변경할_수_없다() throws Exception {
        ReflectionTestUtils.setField(match, "gameType", GameType.VOICE_TUG);
        given(gameMatchRepository.findByIdForUpdate(MATCH_ID)).willReturn(Optional.of(match));
        given(matchParticipantRepository.existsByMatchTeamGameMatchIdAndMemberId(MATCH_ID, FIRST_MEMBER_ID))
                .willReturn(true);

        updateGameType(FIRST_MEMBER_ID, HORSE_TAP_REQUEST)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MATCH_NOT_READY"));

        match.completePreparation();
        updateGameType(FIRST_MEMBER_ID, HORSE_TAP_REQUEST)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MATCH_GAME_TYPE_SELECTION_NOT_ALLOWED"));
    }

    @Test
    void 비참가자의_종목_선택을_거절한다() throws Exception {
        match.completePreparation();
        given(gameMatchRepository.findByIdForUpdate(MATCH_ID)).willReturn(Optional.of(match));

        updateGameType(FIRST_MEMBER_ID, HORSE_TAP_REQUEST)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MATCH_PARTICIPANT_FORBIDDEN"));
    }

    @Test
    void 헤더와_종목_값을_검증한다() throws Exception {
        mockMvc.perform(patch(GAME_TYPE_URL, MATCH_ID)
                        .contentType(MediaType.APPLICATION_JSON).content(HORSE_TAP_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
        updateGameType(FIRST_MEMBER_ID, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("종목은 필수입니다."));
        updateGameType(FIRST_MEMBER_ID, "{\"gameType\":\"SWIMMING\"}")
                .andExpect(status().isBadRequest());
        updateGameType(FIRST_MEMBER_ID, "{\"gameType\":0}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void 없는_경기의_종목을_조회하면_404를_반환한다() throws Exception {
        mockMvc.perform(get(GAME_TYPE_URL, MATCH_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MATCH_NOT_FOUND"));
    }

    private ResultActions updateGameType(long memberId, String body) throws Exception {
        return mockMvc.perform(patch(GAME_TYPE_URL, MATCH_ID)
                .header(DEMO_MEMBER_HEADER, memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
