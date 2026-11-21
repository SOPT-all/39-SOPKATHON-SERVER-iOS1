package org.sopt.sopkathon.domain.match.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.match.code.HorseGameErrorCode;
import org.sopt.sopkathon.domain.match.dto.request.HorseRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.HorseRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.HorseResultResponse;
import org.sopt.sopkathon.domain.match.dto.response.HorseResultResponse.TeamResult;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HorseGameService {

    private final GameMatchRepository gameMatchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    @Transactional
    public HorseRecordSubmitResponse submitRecord(
            Long matchId,
            Long memberId,
            HorseRecordSubmitRequest request
    ) {
        GameMatch gameMatch = getHorseMatch(matchId);
        if (gameMatch.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new BusinessException(HorseGameErrorCode.HORSE_GAME_NOT_IN_PROGRESS);
        }

        MatchParticipant participant = getParticipant(matchId, memberId);
        if (participant.getRecordStatus() != RecordStatus.SUBMITTED) {
            participant.submitHorseRecord(request.elapsedMilliseconds());
        }

        Long finishTimeMilliseconds = participant.getFinishTimeMilliseconds();
        if (finishTimeMilliseconds == null) {
            throw new BusinessException(HorseGameErrorCode.HORSE_RESULT_DATA_INVALID);
        }
        return new HorseRecordSubmitResponse(gameMatch.getId(), participant.getId(), finishTimeMilliseconds);
    }

    public HorseResultResponse getResult(Long matchId, Long memberId) {
        GameMatch gameMatch = getHorseMatch(matchId);
        getParticipant(matchId, memberId);

        List<MatchTeam> teams = matchTeamRepository.findAllByGameMatchIdOrderByTeamSlot(matchId);
        if (teams.size() != 2) {
            throw new BusinessException(HorseGameErrorCode.HORSE_RESULT_DATA_INVALID);
        }

        TeamScore firstTeamScore = calculateTeamScore(teams.get(0));
        TeamScore secondTeamScore = calculateTeamScore(teams.get(1));
        MatchResult firstTeamResult = decideResult(firstTeamScore.totalFinishTimeMilliseconds(),
                secondTeamScore.totalFinishTimeMilliseconds());
        MatchResult secondTeamResult = oppositeOf(firstTeamResult);

        return new HorseResultResponse(
                gameMatch.getId(),
                firstTeamResult == MatchResult.DRAW
                        ? null
                        : (firstTeamResult == MatchResult.WIN
                                ? teams.get(0).getTeamSlot()
                                : teams.get(1).getTeamSlot()),
                List.of(
                        toTeamResult(teams.get(0), firstTeamScore, firstTeamResult),
                        toTeamResult(teams.get(1), secondTeamScore, secondTeamResult)
                )
        );
    }

    private GameMatch getHorseMatch(Long matchId) {
        GameMatch gameMatch = gameMatchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(HorseGameErrorCode.MATCH_NOT_FOUND));
        if (gameMatch.getGameType() != GameType.HORSE_TAP) {
            throw new BusinessException(HorseGameErrorCode.HORSE_GAME_TYPE_MISMATCH);
        }
        return gameMatch;
    }

    private MatchParticipant getParticipant(Long matchId, Long memberId) {
        return matchParticipantRepository.findByMatchTeamGameMatchIdAndMemberId(matchId, memberId)
                .orElseThrow(() -> new BusinessException(HorseGameErrorCode.MATCH_PARTICIPANT_FORBIDDEN));
    }

    private TeamScore calculateTeamScore(MatchTeam team) {
        List<MatchParticipant> participants = matchParticipantRepository.findAllByMatchTeamId(team.getId());
        if (participants.isEmpty() || participants.stream()
                .anyMatch(participant -> participant.getRecordStatus() != RecordStatus.SUBMITTED
                        || participant.getFinishTimeMilliseconds() == null)) {
            throw new BusinessException(HorseGameErrorCode.HORSE_RESULT_NOT_READY);
        }

        long totalFinishTimeMilliseconds = participants.stream()
                .mapToLong(MatchParticipant::getFinishTimeMilliseconds)
                .sum();
        return new TeamScore(totalFinishTimeMilliseconds);
    }

    private MatchResult decideResult(long firstTeamTotal, long secondTeamTotal) {
        if (firstTeamTotal == secondTeamTotal) {
            return MatchResult.DRAW;
        }
        return firstTeamTotal < secondTeamTotal ? MatchResult.WIN : MatchResult.LOSE;
    }

    private MatchResult oppositeOf(MatchResult result) {
        return switch (result) {
            case WIN -> MatchResult.LOSE;
            case LOSE -> MatchResult.WIN;
            case DRAW -> MatchResult.DRAW;
        };
    }

    private TeamResult toTeamResult(MatchTeam team, TeamScore score, MatchResult result) {
        return new TeamResult(
                team.getTeamSlot(),
                team.getSchoolClass().getSchool().getName()
                        + " " + team.getSchoolClass().getGrade()
                        + "학년 " + team.getSchoolClass().getClassNumber() + "반",
                result,
                score.totalFinishTimeMilliseconds()
        );
    }

    private record TeamScore(long totalFinishTimeMilliseconds) {
    }
}
