package org.sopt.sopkathon.domain.match.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.common.exception.GlobalExceptionHandler;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.MatchParticipant;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;
import org.sopt.sopkathon.domain.match.entity.MatchTeam;
import org.sopt.sopkathon.domain.match.repository.GameMatchRepository;
import org.sopt.sopkathon.domain.match.repository.MatchParticipantRepository;
import org.sopt.sopkathon.domain.match.service.MatchService;
import org.sopt.sopkathon.domain.member.entity.Member;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class MatchControllerTest {

    private static final long MATCH_ID = 1025L;
    private static final long FIRST_MEMBER_ID = 11101L;
    private static final long SECOND_MEMBER_ID = 12201L;
    private static final String DEMO_MEMBER_HEADER = "X-Demo-Member-Id";
    private static final String MATCH_URL = "/api/v1/game-matches/{matchId}";
    private static final String READY_BODY = "{\"isReady\":true}";

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
    void 마지막_참가자가_준비하면_FOUND가_되고_두_기기의_폴링에_반영된다() throws Exception {
        given(gameMatchRepository.findByIdForUpdate(MATCH_ID)).willReturn(Optional.of(match));
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(match));
        given(matchParticipantRepository.findAllByMatchTeamGameMatchId(MATCH_ID)).willReturn(List.of(
                createParticipant(FIRST_MEMBER_ID, (byte) 1), createParticipant(SECOND_MEMBER_ID, (byte) 2)));

        updateReady(FIRST_MEMBER_ID, READY_BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MATCH_READY_UPDATED"))
                .andExpect(jsonPath("$.data.status").value("SEARCHING"))
                .andExpect(jsonPath("$.data.isMineReady").value(true))
                .andExpect(jsonPath("$.data.readyCount").value(1))
                .andExpect(jsonPath("$.data.participantCount").value(2))
                .andExpect(jsonPath("$.data.isAllReady").value(false));

        updateReady(FIRST_MEMBER_ID, READY_BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readyCount").value(1));

        getReady(SECOND_MEMBER_ID)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MATCH_READY_FETCHED"))
                .andExpect(jsonPath("$.data.matchId").value((int) MATCH_ID))
                .andExpect(jsonPath("$.data.status").value("SEARCHING"))
                .andExpect(jsonPath("$.data.isMineReady").value(false))
                .andExpect(jsonPath("$.data.readyCount").value(1));

        updateReady(SECOND_MEMBER_ID, READY_BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FOUND"))
                .andExpect(jsonPath("$.data.isAllReady").value(true));

        for (long memberId : List.of(FIRST_MEMBER_ID, SECOND_MEMBER_ID)) {
            getReady(memberId)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("FOUND"))
                    .andExpect(jsonPath("$.data.isMineReady").value(true))
                    .andExpect(jsonPath("$.data.readyCount").value(2))
                    .andExpect(jsonPath("$.data.isAllReady").value(true));
        }
    }

    @Test
    void 비참가자는_준비를_바꾸거나_조회할_수_없다() throws Exception {
        given(gameMatchRepository.findByIdForUpdate(MATCH_ID)).willReturn(Optional.of(match));
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(match));
        given(matchParticipantRepository.findAllByMatchTeamGameMatchId(MATCH_ID))
                .willReturn(List.of(createParticipant(FIRST_MEMBER_ID, (byte) 1)));

        updateReady(SECOND_MEMBER_ID, READY_BODY)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MATCH_PARTICIPANT_FORBIDDEN"));
        getReady(SECOND_MEMBER_ID)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MATCH_PARTICIPANT_FORBIDDEN"));
    }

    @Test
    void 준비값이_false이거나_누락되면_400으로_응답한다() throws Exception {
        updateReady(FIRST_MEMBER_ID, "{\"isReady\":false}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("준비 여부는 true여야 합니다."));
        updateReady(FIRST_MEMBER_ID, "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("준비 여부는 필수입니다."));
    }

    @Test
    void 경기_ID와_멤버_헤더를_검증한다() throws Exception {
        mockMvc.perform(get(MATCH_URL + "/ready", MATCH_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MISSING_PARAMETER"));
        mockMvc.perform(get(MATCH_URL + "/ready", 0).header(DEMO_MEMBER_HEADER, FIRST_MEMBER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        getReady(0)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 없는_경기의_준비_상태는_404로_응답한다() throws Exception {
        getReady(FIRST_MEMBER_ID)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MATCH_NOT_FOUND"));
    }

    @Test
    void 이미_시작된_경기는_준비완료_재요청을_허용하되_새로운_준비변경을_거절한다() throws Exception {
        ReflectionTestUtils.setField(match, "status", MatchStatus.IN_PROGRESS);
        MatchParticipant readyParticipant = createParticipant(FIRST_MEMBER_ID, (byte) 1);
        readyParticipant.markReady();
        given(gameMatchRepository.findByIdForUpdate(MATCH_ID)).willReturn(Optional.of(match));
        given(matchParticipantRepository.findAllByMatchTeamGameMatchId(MATCH_ID)).willReturn(List.of(
                readyParticipant, createParticipant(SECOND_MEMBER_ID, (byte) 2)));

        updateReady(FIRST_MEMBER_ID, READY_BODY)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.readyCount").value(1));
        updateReady(SECOND_MEMBER_ID, READY_BODY)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MATCH_READY_UPDATE_NOT_ALLOWED"));
    }

    private MatchParticipant createParticipant(long memberId, byte teamSlot) {
        Member member = new Member(null, "시연 멤버");
        ReflectionTestUtils.setField(member, "id", memberId);
        return new MatchParticipant(new MatchTeam(match, null, teamSlot), member);
    }

    private ResultActions updateReady(long memberId, String body) throws Exception {
        return mockMvc.perform(patch(MATCH_URL + "/participants/me/ready", MATCH_ID)
                .header(DEMO_MEMBER_HEADER, memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions getReady(long memberId) throws Exception {
        return mockMvc.perform(get(MATCH_URL + "/ready", MATCH_ID).header(DEMO_MEMBER_HEADER, memberId));
    }
}
