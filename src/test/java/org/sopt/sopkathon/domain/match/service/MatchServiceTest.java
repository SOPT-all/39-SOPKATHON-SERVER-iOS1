package org.sopt.sopkathon.domain.match.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.match.dto.MatchResultResponse;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchParticipant;
import org.sopt.sopkathon.domain.match.entity.MatchResult;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;
import org.sopt.sopkathon.domain.match.entity.MatchTeam;
import org.sopt.sopkathon.domain.match.entity.RecordStatus;
import org.sopt.sopkathon.domain.match.repository.GameMatchRepository;
import org.sopt.sopkathon.domain.match.repository.MatchParticipantRepository;
import org.sopt.sopkathon.domain.match.repository.MatchTeamRepository;
import org.sopt.sopkathon.domain.member.entity.Member;
import org.sopt.sopkathon.domain.school.entity.School;
import org.sopt.sopkathon.domain.school.entity.SchoolClass;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    private static final long MATCH_ID = 101L;
    private static final long MEMBER_ID = 1L;

    @Mock
    private GameMatchRepository gameMatchRepository;

    @Mock
    private MatchTeamRepository matchTeamRepository;

    @Mock
    private MatchParticipantRepository matchParticipantRepository;

    @InjectMocks
    private MatchService matchService;

    private MatchTeam myTeam;
    private MatchTeam opponentTeam;
    private MatchParticipant myHorseRecord;
    private MatchParticipant mySecondHorseRecord;
    private MatchParticipant opponentHorseRecord;
    private MatchParticipant opponentSecondHorseRecord;

    @BeforeEach
    void setUp() {
        GameMatch finishedHorseMatch = match(GameType.HORSE_TAP, MatchStatus.FINISHED);
        SchoolClass mySchoolClass = schoolClass(10L, 1L, "솝고등학교", (byte) 2, (short) 3);
        SchoolClass opponentSchoolClass = schoolClass(20L, 2L, "성수고등학교", (byte) 3, (short) 2);

        myTeam = team(1001L, finishedHorseMatch, mySchoolClass, (byte) 1);
        opponentTeam = team(1002L, finishedHorseMatch, opponentSchoolClass, (byte) 2);
        myHorseRecord = participant(5001L, myTeam, member(MEMBER_ID, mySchoolClass, "김민수"),
                12_875L, null);
        mySecondHorseRecord = participant(5002L, myTeam, member(2L, mySchoolClass, "박지민"),
                13_000L, null);
        opponentHorseRecord = participant(5003L, opponentTeam, member(3L, opponentSchoolClass, "이서준"),
                13_420L, null);
        opponentSecondHorseRecord = participant(5004L, opponentTeam, member(4L, opponentSchoolClass, "최윤아"),
                13_500L, null);
    }

    @Test
    void 양팀_경마_개인기록을_합산해_더_짧은_팀을_승리로_반환한다() {
        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(myTeam.getGameMatch()));
        given(matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(MATCH_ID, MEMBER_ID))
                .willReturn(Optional.of(myHorseRecord));
        given(matchTeamRepository.findAllByGameMatchIdOrderByTeamSlot(MATCH_ID))
                .willReturn(List.of(myTeam, opponentTeam));
        given(matchParticipantRepository.findAllByMatchTeamId(myTeam.getId()))
                .willReturn(List.of(myHorseRecord, mySecondHorseRecord));
        given(matchParticipantRepository.findAllByMatchTeamId(opponentTeam.getId()))
                .willReturn(List.of(opponentHorseRecord, opponentSecondHorseRecord));

        MatchResultResponse response = matchService.getMatchResult(MATCH_ID, MEMBER_ID);

        assertThat(response.winnerTeamSlot()).isEqualTo((byte) 1);
        assertThat(response.teams())
                .extracting(MatchResultResponse.TeamResultResponse::result)
                .containsExactly(MatchResult.WIN, MatchResult.LOSE);
        assertThat(response.teams())
                .extracting(MatchResultResponse.TeamResultResponse::totalFinishTimeMilliseconds)
                .containsExactly(25_875L, 26_920L);
    }

    @Test
    void 양팀_줄다리기_개인최대데시벨을_합산해_더_큰_팀을_승리로_반환한다() {
        GameMatch finishedVoiceMatch = match(GameType.VOICE_TUG, MatchStatus.FINISHED);
        SchoolClass mySchoolClass = schoolClass(10L, 1L, "솝고등학교", (byte) 2, (short) 3);
        SchoolClass opponentSchoolClass = schoolClass(20L, 2L, "성수고등학교", (byte) 3, (short) 2);
        MatchTeam voiceMyTeam = team(1001L, finishedVoiceMatch, mySchoolClass, (byte) 1);
        MatchTeam voiceOpponentTeam = team(1002L, finishedVoiceMatch, opponentSchoolClass, (byte) 2);
        MatchParticipant voiceMyRecord = participant(5001L, voiceMyTeam, member(MEMBER_ID, mySchoolClass, "김민수"),
                null, new BigDecimal("82.375"));
        MatchParticipant voiceOpponentRecord = participant(5003L, voiceOpponentTeam, member(3L, opponentSchoolClass, "이서준"),
                null, new BigDecimal("80.125"));

        given(gameMatchRepository.findById(MATCH_ID)).willReturn(Optional.of(finishedVoiceMatch));
        given(matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(MATCH_ID, MEMBER_ID))
                .willReturn(Optional.of(voiceMyRecord));
        given(matchTeamRepository.findAllByGameMatchIdOrderByTeamSlot(MATCH_ID))
                .willReturn(List.of(voiceMyTeam, voiceOpponentTeam));
        given(matchParticipantRepository.findAllByMatchTeamId(voiceMyTeam.getId()))
                .willReturn(List.of(voiceMyRecord));
        given(matchParticipantRepository.findAllByMatchTeamId(voiceOpponentTeam.getId()))
                .willReturn(List.of(voiceOpponentRecord));

        MatchResultResponse response = matchService.getMatchResult(MATCH_ID, MEMBER_ID);

        assertThat(response.winnerTeamSlot()).isEqualTo((byte) 1);
        assertThat(response.teams())
                .extracting(MatchResultResponse.TeamResultResponse::totalMaxDecibel)
                .containsExactly(new BigDecimal("82.375"), new BigDecimal("80.125"));
    }

    @Test
    void 종료되지_않은_경기는_결과를_조회할_수_없다() {
        given(gameMatchRepository.findById(MATCH_ID))
                .willReturn(Optional.of(match(GameType.HORSE_TAP, MatchStatus.IN_PROGRESS)));

        assertThatThrownBy(() -> matchService.getMatchResult(MATCH_ID, MEMBER_ID))
                .isInstanceOf(BusinessException.class);
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

    private Member member(long memberId, SchoolClass schoolClass, String displayName) {
        Member member = new Member(schoolClass, displayName);
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }

    private MatchTeam team(long teamId, GameMatch gameMatch, SchoolClass schoolClass, byte teamSlot) {
        MatchTeam team = new MatchTeam(gameMatch, schoolClass, teamSlot);
        ReflectionTestUtils.setField(team, "id", teamId);
        return team;
    }

    private MatchParticipant participant(
            long participantId,
            MatchTeam team,
            Member member,
            Long finishTimeMilliseconds,
            BigDecimal voiceScore
    ) {
        MatchParticipant participant = new MatchParticipant(team, member);
        ReflectionTestUtils.setField(participant, "id", participantId);
        ReflectionTestUtils.setField(participant, "recordStatus", RecordStatus.SUBMITTED);
        ReflectionTestUtils.setField(participant, "finishTimeMilliseconds", finishTimeMilliseconds);
        ReflectionTestUtils.setField(participant, "voiceScore", voiceScore);
        return participant;
    }
}
