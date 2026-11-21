package org.sopt.sopkathon.domain.match.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.match.code.HorseGameErrorCode;
import org.sopt.sopkathon.domain.match.dto.request.HorseRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.HorseRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.HorseResultResponse;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchParticipant;
import org.sopt.sopkathon.domain.match.entity.MatchResult;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;
import org.sopt.sopkathon.domain.match.entity.MatchTeam;
import org.sopt.sopkathon.domain.match.repository.GameMatchRepository;
import org.sopt.sopkathon.domain.match.repository.MatchParticipantRepository;
import org.sopt.sopkathon.domain.match.repository.MatchTeamRepository;
import org.sopt.sopkathon.domain.member.entity.Member;
import org.sopt.sopkathon.domain.school.entity.School;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HorseGameServiceTest {

    private static final long MATCH_ID = 1019L;
    private static final long MEMBER_ID = 11101L;

    @Mock
    private GameMatchRepository gameMatchRepository;

    @Mock
    private MatchTeamRepository matchTeamRepository;

    @Mock
    private MatchParticipantRepository matchParticipantRepository;

    @InjectMocks
    private HorseGameService horseGameService;

    private GameMatch horseMatch;
    private MatchTeam myTeam;
    private MatchTeam opponentTeam;
    private MatchParticipant myParticipant;

    @BeforeEach
    void setUp() {
        horseMatch = match(GameType.HORSE_TAP, MatchStatus.IN_PROGRESS);
        SchoolClass mySchoolClass = schoolClass(111L, 1L, "성수고등학교", (byte) 1, (short) 1);
        SchoolClass opponentSchoolClass = schoolClass(233L, 2L, "휘문중학교", (byte) 3, (short) 3);
        myTeam = team(2036L, horseMatch, mySchoolClass, (byte) 1);
        opponentTeam = team(2037L, horseMatch, opponentSchoolClass, (byte) 2);
        myParticipant = participant(3121L, myTeam, member(MEMBER_ID, mySchoolClass, "김민서"));
    }

    @Test
    void 진행중인_말달리기에서_개인_완주_시간을_제출한다() {
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(horseMatch));
        given(matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(MATCH_ID, MEMBER_ID))
                .willReturn(Optional.of(myParticipant));

        HorseRecordSubmitResponse response = horseGameService.submitRecord(
                MATCH_ID,
                MEMBER_ID,
                new HorseRecordSubmitRequest(1_200L)
        );

        assertThat(response.matchId()).isEqualTo(MATCH_ID);
        assertThat(response.participantId()).isEqualTo(3121L);
        assertThat(response.finishTimeMilliseconds()).isEqualTo(1_200L);
        assertThat(myParticipant.getFinishTimeMilliseconds()).isEqualTo(1_200L);
    }

    @Test
    void 이미_제출한_말달리기_기록은_재전송해도_최초값을_유지한다() {
        myParticipant.submitHorseRecord(1_200L);
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(horseMatch));
        given(matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(MATCH_ID, MEMBER_ID))
                .willReturn(Optional.of(myParticipant));

        HorseRecordSubmitResponse response = horseGameService.submitRecord(
                MATCH_ID,
                MEMBER_ID,
                new HorseRecordSubmitRequest(9_999L)
        );

        assertThat(response.finishTimeMilliseconds()).isEqualTo(1_200L);
        assertThat(myParticipant.getFinishTimeMilliseconds()).isEqualTo(1_200L);
    }

    @Test
    void 양팀_완주_시간을_합산해_더_짧은_팀을_승리로_반환한다() {
        MatchParticipant mySecondParticipant = submittedParticipant(
                3122L, myTeam, member(11102L, myTeam.getSchoolClass(), "김지우"), 1_100L);
        myParticipant.submitHorseRecord(1_000L);
        MatchParticipant opponentFirstParticipant = submittedParticipant(
                3125L, opponentTeam, member(23301L, opponentTeam.getSchoolClass(), "박민서"), 1_200L);
        MatchParticipant opponentSecondParticipant = submittedParticipant(
                3126L, opponentTeam, member(23302L, opponentTeam.getSchoolClass(), "박지우"), 1_300L);

        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(horseMatch));
        given(matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(MATCH_ID, MEMBER_ID))
                .willReturn(Optional.of(myParticipant));
        given(matchTeamRepository.findAllByGameMatchIdOrderByTeamSlot(MATCH_ID))
                .willReturn(List.of(myTeam, opponentTeam));
        given(matchParticipantRepository.findAllByMatchTeamId(myTeam.getId()))
                .willReturn(List.of(myParticipant, mySecondParticipant));
        given(matchParticipantRepository.findAllByMatchTeamId(opponentTeam.getId()))
                .willReturn(List.of(opponentFirstParticipant, opponentSecondParticipant));

        HorseResultResponse response = horseGameService.getResult(MATCH_ID, MEMBER_ID);

        assertThat(response.winnerTeamSlot()).isEqualTo((byte) 1);
        assertThat(response.teams())
                .extracting(HorseResultResponse.TeamResult::result)
                .containsExactly(MatchResult.WIN, MatchResult.LOSE);
        assertThat(response.teams())
                .extracting(HorseResultResponse.TeamResult::totalFinishTimeMilliseconds)
                .containsExactly(2_100L, 2_500L);
    }

    @Test
    void 모든_참가자가_기록을_제출하지_않으면_결과를_조회할_수_없다() {
        myParticipant.submitHorseRecord(1_000L);
        MatchParticipant pendingOpponent = participant(
                3125L, opponentTeam, member(23301L, opponentTeam.getSchoolClass(), "박민서"));

        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(horseMatch));
        given(matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(MATCH_ID, MEMBER_ID))
                .willReturn(Optional.of(myParticipant));
        given(matchTeamRepository.findAllByGameMatchIdOrderByTeamSlot(MATCH_ID))
                .willReturn(List.of(myTeam, opponentTeam));
        given(matchParticipantRepository.findAllByMatchTeamId(myTeam.getId()))
                .willReturn(List.of(myParticipant));
        given(matchParticipantRepository.findAllByMatchTeamId(opponentTeam.getId()))
                .willReturn(List.of(pendingOpponent));

        assertThatThrownBy(() -> horseGameService.getResult(MATCH_ID, MEMBER_ID))
                .isInstanceOfSatisfying(BusinessException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(HorseGameErrorCode.HORSE_RESULT_NOT_READY));
    }

    @Test
    void 말달리기가_아닌_경기에는_기록을_제출할_수_없다() {
        GameMatch voiceMatch = match(GameType.VOICE_TUG, MatchStatus.IN_PROGRESS);
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(voiceMatch));

        assertThatThrownBy(() -> horseGameService.submitRecord(
                MATCH_ID,
                MEMBER_ID,
                new HorseRecordSubmitRequest(1_200L)
        )).isInstanceOfSatisfying(BusinessException.class,
                e -> assertThat(e.getErrorCode()).isEqualTo(HorseGameErrorCode.HORSE_GAME_TYPE_MISMATCH));
    }

    private GameMatch match(GameType gameType, MatchStatus status) {
        GameMatch gameMatch = new GameMatch(gameType, (byte) 2);
        ReflectionTestUtils.setField(gameMatch, "id", MATCH_ID);
        ReflectionTestUtils.setField(gameMatch, "status", status);
        return gameMatch;
    }

    private SchoolClass schoolClass(long schoolClassId, long schoolId, String schoolName, byte grade, short classNumber) {
        School school = new School(schoolName);
        ReflectionTestUtils.setField(school, "id", schoolId);
        SchoolClass schoolClass = new SchoolClass(school, grade, classNumber);
        ReflectionTestUtils.setField(schoolClass, "id", schoolClassId);
        return schoolClass;
    }

    private MatchTeam team(long teamId, GameMatch gameMatch, SchoolClass schoolClass, byte teamSlot) {
        MatchTeam team = new MatchTeam(gameMatch, schoolClass, teamSlot);
        ReflectionTestUtils.setField(team, "id", teamId);
        return team;
    }

    private Member member(long memberId, SchoolClass schoolClass, String displayName) {
        Member member = new Member(schoolClass, displayName);
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private MatchParticipant participant(long participantId, MatchTeam team, Member member) {
        MatchParticipant participant = new MatchParticipant(team, member);
        ReflectionTestUtils.setField(participant, "id", participantId);
        return participant;
    }

    private MatchParticipant submittedParticipant(
            long participantId,
            MatchTeam team,
            Member member,
            long finishTimeMilliseconds
    ) {
        MatchParticipant participant = participant(participantId, team, member);
        participant.submitHorseRecord(finishTimeMilliseconds);
        return participant;
    }
}
