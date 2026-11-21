package org.sopt.sopkathon.domain.match.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.common.code.ErrorCode;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.match.code.MatchErrorCode;
import org.sopt.sopkathon.domain.match.dto.request.MatchVoiceRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceResultResponse;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchParticipant;
import org.sopt.sopkathon.domain.match.entity.MatchResult;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;
import org.sopt.sopkathon.domain.match.entity.MatchTeam;
import org.sopt.sopkathon.domain.match.entity.RecordStatus;
import org.sopt.sopkathon.domain.match.repository.GameMatchRepository;
import org.sopt.sopkathon.domain.match.repository.MatchParticipantRepository;
import org.sopt.sopkathon.domain.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MatchVoiceServiceTest {

    private static final Long MATCH_ID = 1020L;

    @Mock
    private GameMatchRepository gameMatchRepository;

    @Mock
    private MatchParticipantRepository matchParticipantRepository;

    @InjectMocks
    private MatchService matchService;

    private GameMatch gameMatch;
    private MatchTeam team1;
    private MatchTeam team2;

    @BeforeEach
    void setUp() {
        gameMatch = createMatch(GameType.VOICE_TUG, MatchStatus.IN_PROGRESS);
        team1 = createTeam(gameMatch, 1);
        team2 = createTeam(gameMatch, 2);
    }

    @Test
    void 기록을_제출하면_점수와_SUBMITTED가_저장되고_미제출자가_있으면_확정되지_않는다() {
        MatchParticipant me = createParticipant(team1, 1L, RecordStatus.PENDING, null);
        MatchParticipant other = createParticipant(team2, 2L, RecordStatus.PENDING, null);
        givenMatchAndParticipant(me, List.of(me, other));

        MatchVoiceRecordSubmitResponse response = matchService.submitVoiceRecord(MATCH_ID, 1L, request("82.375"));

        assertThat(response.matchId()).isEqualTo(MATCH_ID);
        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.maxDecibel()).isEqualByComparingTo("82.375");
        assertThat(response.recordStatus()).isEqualTo(RecordStatus.SUBMITTED);
        assertThat(response.isResultFinalized()).isFalse();
        assertThat(me.getSubmittedAt()).isNotNull();
        assertThat(gameMatch.getStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
        assertThat(team1.getResult()).isNull();
    }

    @Test
    void 마지막_참가자가_제출하면_합계가_큰_팀이_WIN으로_확정되고_경기가_종료된다() {
        MatchParticipant me = createParticipant(team2, 1L, RecordStatus.PENDING, null);
        MatchParticipant mate = createParticipant(team2, 3L, RecordStatus.SUBMITTED, "75.500");
        MatchParticipant rival = createParticipant(team1, 2L, RecordStatus.SUBMITTED, "149.200");
        givenMatchAndParticipant(me, List.of(rival, me, mate));

        MatchVoiceRecordSubmitResponse response = matchService.submitVoiceRecord(MATCH_ID, 1L, request("82.375"));

        assertThat(response.isResultFinalized()).isTrue();
        assertThat(team1.getResult()).isEqualTo(MatchResult.LOSE);
        assertThat(team2.getResult()).isEqualTo(MatchResult.WIN);
        assertThat(gameMatch.getStatus()).isEqualTo(MatchStatus.FINISHED);
        assertThat(gameMatch.getFinishedAt()).isNotNull();
        assertThat(team2.getRankingPoints()).isNull();
    }

    @Test
    void 합계가_같으면_양쪽_모두_DRAW로_확정된다() {
        MatchParticipant me = createParticipant(team1, 1L, RecordStatus.PENDING, null);
        MatchParticipant rival = createParticipant(team2, 2L, RecordStatus.SUBMITTED, "70.000");
        givenMatchAndParticipant(me, List.of(me, rival));

        matchService.submitVoiceRecord(MATCH_ID, 1L, request("70"));

        assertThat(team1.getResult()).isEqualTo(MatchResult.DRAW);
        assertThat(team2.getResult()).isEqualTo(MatchResult.DRAW);
    }

    @Test
    void 존재하지_않는_경기면_MATCH_NOT_FOUND() {
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.empty());

        assertBusinessError(() -> matchService.submitVoiceRecord(MATCH_ID, 1L, request("80")), MatchErrorCode.MATCH_NOT_FOUND);
        assertBusinessError(() -> matchService.getVoiceResult(MATCH_ID), MatchErrorCode.MATCH_NOT_FOUND);
    }

    @Test
    void 줄다리기_경기가_아니면_INVALID_GAME_TYPE() {
        GameMatch horse = createMatch(GameType.HORSE_TAP, MatchStatus.IN_PROGRESS);
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(horse));

        assertBusinessError(() -> matchService.submitVoiceRecord(MATCH_ID, 1L, request("80")), MatchErrorCode.INVALID_GAME_TYPE);
        assertBusinessError(() -> matchService.getVoiceResult(MATCH_ID), MatchErrorCode.INVALID_GAME_TYPE);
    }

    @Test
    void 종목이_선택되지_않은_경기도_INVALID_GAME_TYPE() {
        GameMatch noType = createMatch(null, MatchStatus.IN_PROGRESS);
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(noType));

        assertBusinessError(() -> matchService.submitVoiceRecord(MATCH_ID, 1L, request("80")), MatchErrorCode.INVALID_GAME_TYPE);
    }

    @Test
    void 진행_중이_아닌_경기면_MATCH_NOT_IN_PROGRESS() {
        gameMatch.finish(java.time.LocalDateTime.now());
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(gameMatch));

        assertBusinessError(() -> matchService.submitVoiceRecord(MATCH_ID, 1L, request("80")), MatchErrorCode.MATCH_NOT_IN_PROGRESS);
    }

    @Test
    void 경기_참가자가_아니면_NOT_MATCH_PARTICIPANT() {
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(gameMatch));
        given(matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(MATCH_ID, 99L))
                .willReturn(Optional.empty());

        assertBusinessError(() -> matchService.submitVoiceRecord(MATCH_ID, 99L, request("80")), MatchErrorCode.NOT_MATCH_PARTICIPANT);
    }

    @Test
    void 결과_조회는_팀별_합계와_승리_팀을_반환한다() {
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(gameMatch));
        given(matchParticipantRepository.findAllByMatchTeamGameMatchId(MATCH_ID)).willReturn(List.of(
                createParticipant(team2, 3L, RecordStatus.SUBMITTED, "75.500"),
                createParticipant(team1, 1L, RecordStatus.SUBMITTED, "70.000"),
                createParticipant(team2, 4L, RecordStatus.SUBMITTED, "82.375"),
                createParticipant(team1, 2L, RecordStatus.SUBMITTED, "79.200")));

        MatchVoiceResultResponse response = matchService.getVoiceResult(MATCH_ID);

        assertThat(response.gameType()).isEqualTo(GameType.VOICE_TUG);
        assertThat(response.winnerTeamSlot()).isEqualTo(2);
        assertThat(response.teams()).extracting("teamSlot", "result")
                .containsExactly(tuple(1, MatchResult.LOSE), tuple(2, MatchResult.WIN));
        assertThat(response.teams().get(0).totalMaxDecibel()).isEqualByComparingTo("149.200");
        assertThat(response.teams().get(1).totalMaxDecibel()).isEqualByComparingTo("157.875");
    }

    @Test
    void 결과_조회에서_합계가_같으면_무승부이고_승리_팀은_없다() {
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(gameMatch));
        given(matchParticipantRepository.findAllByMatchTeamGameMatchId(MATCH_ID)).willReturn(List.of(
                createParticipant(team1, 1L, RecordStatus.SUBMITTED, "70.000"),
                createParticipant(team2, 2L, RecordStatus.SUBMITTED, "70.000")));

        MatchVoiceResultResponse response = matchService.getVoiceResult(MATCH_ID);

        assertThat(response.winnerTeamSlot()).isNull();
        assertThat(response.teams()).extracting("result").containsExactly(MatchResult.DRAW, MatchResult.DRAW);
    }

    @Test
    void 결과_조회에서_미제출자가_있으면_GAME_RECORDS_NOT_COMPLETE() {
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(gameMatch));
        given(matchParticipantRepository.findAllByMatchTeamGameMatchId(MATCH_ID)).willReturn(List.of(
                createParticipant(team1, 1L, RecordStatus.SUBMITTED, "70.000"),
                createParticipant(team2, 2L, RecordStatus.PENDING, null)));

        assertBusinessError(() -> matchService.getVoiceResult(MATCH_ID), MatchErrorCode.GAME_RECORDS_NOT_COMPLETE);
    }

    private void givenMatchAndParticipant(MatchParticipant me, List<MatchParticipant> all) {
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(gameMatch));
        given(matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(MATCH_ID, me.getMember().getId()))
                .willReturn(Optional.of(me));
        given(matchParticipantRepository.findAllByMatchTeamGameMatchId(MATCH_ID)).willReturn(all);
    }

    private void assertBusinessError(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable, ErrorCode expected) {
        assertThatThrownBy(callable).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    private MatchVoiceRecordSubmitRequest request(String maxDecibel) {
        return new MatchVoiceRecordSubmitRequest(new BigDecimal(maxDecibel));
    }

    private GameMatch createMatch(GameType gameType, MatchStatus status) {
        GameMatch match = new GameMatch(gameType, (byte) 2);
        ReflectionTestUtils.setField(match, "id", MATCH_ID);
        ReflectionTestUtils.setField(match, "status", status);
        return match;
    }

    private MatchTeam createTeam(GameMatch match, int teamSlot) {
        return new MatchTeam(match, null, (byte) teamSlot);
    }

    private MatchParticipant createParticipant(MatchTeam team, Long memberId, RecordStatus status, String score) {
        Member member = new Member(null, "member" + memberId);
        ReflectionTestUtils.setField(member, "id", memberId);
        MatchParticipant participant = new MatchParticipant(team, member);
        ReflectionTestUtils.setField(participant, "recordStatus", status);
        if (score != null) {
            ReflectionTestUtils.setField(participant, "voiceScore", new BigDecimal(score));
        }
        return participant;
    }
}
