package org.sopt.sopkathon.domain.match.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.match.code.MatchErrorCode;
import org.sopt.sopkathon.domain.match.dto.request.MatchVoiceRecordSubmitRequest;
import org.sopt.sopkathon.domain.match.dto.response.MatchGameTypeResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchGameTypeUpdateResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchReadyResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceRecordSubmitResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchVoiceResultResponse.TeamResult;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

    private static final int TEAM_COUNT = 2;

    private final GameMatchRepository gameMatchRepository;
    private final MatchParticipantRepository matchParticipantRepository;

    @Transactional
    public MatchReadyResponse updateReady(Long matchId, Long memberId) {
        // 참가자 목록을 읽기 전에 경기 행을 잠가 같은 경기의 준비 요청을 순서대로 처리한다.
        GameMatch match = gameMatchRepository.findByIdForUpdate(matchId)
                .orElseThrow(() -> new BusinessException(MatchErrorCode.MATCH_NOT_FOUND));
        List<MatchParticipant> participants = matchParticipantRepository.findAllByMatchTeamGameMatchId(matchId);
        MatchParticipant participant = getParticipant(participants, memberId);

        if (!participant.isReady() && match.getStatus() != MatchStatus.SEARCHING) {
            throw new BusinessException(MatchErrorCode.MATCH_READY_UPDATE_NOT_ALLOWED);
        }

        participant.markReady();
        long readyCount = countReadyParticipants(participants);
        if (readyCount == participants.size()) {
            match.completePreparation();
        }
        return MatchReadyResponse.of(match, participant.isReady(), readyCount, participants.size());
    }

    public MatchReadyResponse getReady(Long matchId, Long memberId) {
        GameMatch match = gameMatchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(MatchErrorCode.MATCH_NOT_FOUND));
        List<MatchParticipant> participants = matchParticipantRepository.findAllByMatchTeamGameMatchId(matchId);
        MatchParticipant participant = getParticipant(participants, memberId);
        return MatchReadyResponse.of(match, participant.isReady(), countReadyParticipants(participants), participants.size());
    }

    @Transactional
    public MatchGameTypeUpdateResponse updateGameType(Long matchId, Long memberId, GameType gameType) {
        GameMatch match = gameMatchRepository.findByIdForUpdate(matchId)
                .orElseThrow(() -> new BusinessException(MatchErrorCode.MATCH_NOT_FOUND));
        if (!matchParticipantRepository.existsByMatchTeamGameMatchIdAndMemberId(matchId, memberId)) {
            throw new BusinessException(MatchErrorCode.MATCH_PARTICIPANT_FORBIDDEN);
        }

        if (match.getStatus() == MatchStatus.SEARCHING) {
            throw new BusinessException(MatchErrorCode.MATCH_NOT_READY);
        }
        if (match.hasSelectedGameType()) {
            return MatchGameTypeUpdateResponse.of(match, false);
        }
        if (match.getStatus() != MatchStatus.FOUND || match.getGameType() != null) {
            throw new BusinessException(MatchErrorCode.MATCH_GAME_TYPE_SELECTION_NOT_ALLOWED);
        }

        match.startGame(gameType);
        return MatchGameTypeUpdateResponse.of(match, true);
    }

    public MatchGameTypeResponse getGameType(Long matchId) {
        GameMatch match = gameMatchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(MatchErrorCode.MATCH_NOT_FOUND));
        return MatchGameTypeResponse.of(match);
    }

    private MatchParticipant getParticipant(List<MatchParticipant> participants, Long memberId) {
        return participants.stream()
                .filter(participant -> participant.getMember().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(MatchErrorCode.MATCH_PARTICIPANT_FORBIDDEN));
    }

    private long countReadyParticipants(List<MatchParticipant> participants) {
        return participants.stream().filter(MatchParticipant::isReady).count();
    }

    @Transactional
    public MatchVoiceRecordSubmitResponse submitVoiceRecord(
            Long matchId, Long memberId, MatchVoiceRecordSubmitRequest request) {
        GameMatch gameMatch = getVoiceTugMatch(matchId);
        if (gameMatch.getStatus() != MatchStatus.IN_PROGRESS) {
            throw new BusinessException(MatchErrorCode.MATCH_NOT_IN_PROGRESS);
        }
        MatchParticipant participant = matchParticipantRepository
                .findByMatchTeamGameMatchIdAndMemberId(matchId, memberId)
                .orElseThrow(() -> new BusinessException(MatchErrorCode.NOT_MATCH_PARTICIPANT));

        LocalDateTime now = LocalDateTime.now();
        participant.submitVoiceScore(request.maxDecibel(), now);

        List<MatchParticipant> participants = matchParticipantRepository.findAllByMatchTeamGameMatchId(matchId);
        boolean isResultFinalized = isAllSubmitted(participants);
        if (isResultFinalized) {
            finalizeResult(gameMatch, participants, now);
        }
        return MatchVoiceRecordSubmitResponse.of(matchId, participant, isResultFinalized);
    }

    public MatchVoiceResultResponse getVoiceResult(Long matchId) {
        GameMatch gameMatch = getVoiceTugMatch(matchId);
        List<MatchParticipant> participants = matchParticipantRepository.findAllByMatchTeamGameMatchId(matchId);
        List<TeamTotal> totals = calculateTeamTotals(participants);
        List<MatchResult> results = judge(totals);

        List<TeamResult> teams = new ArrayList<>();
        Integer winnerTeamSlot = null;
        for (int i = 0; i < totals.size(); i++) {
            int teamSlot = totals.get(i).team().getTeamSlot();
            teams.add(new TeamResult(teamSlot, totals.get(i).total(), results.get(i)));
            if (results.get(i) == MatchResult.WIN) {
                winnerTeamSlot = teamSlot;
            }
        }
        return MatchVoiceResultResponse.of(gameMatch, winnerTeamSlot, teams);
    }

    private GameMatch getVoiceTugMatch(Long matchId) {
        GameMatch gameMatch = gameMatchRepository.findById(matchId)
                .orElseThrow(() -> new BusinessException(MatchErrorCode.MATCH_NOT_FOUND));
        if (gameMatch.getGameType() != GameType.VOICE_TUG) {
            throw new BusinessException(MatchErrorCode.INVALID_GAME_TYPE);
        }
        return gameMatch;
    }

    private boolean isAllSubmitted(List<MatchParticipant> participants) {
        return !participants.isEmpty() && participants.stream()
                .allMatch(participant -> participant.getRecordStatus() == RecordStatus.SUBMITTED
                        && participant.getVoiceScore() != null);
    }

    // 팀 슬롯 순서대로 참가자 최대 데시벨의 합계를 구한다. 미제출자가 있거나 두 팀이 아니면 409.
    private List<TeamTotal> calculateTeamTotals(List<MatchParticipant> participants) {
        if (!isAllSubmitted(participants)) {
            throw new BusinessException(MatchErrorCode.GAME_RECORDS_NOT_COMPLETE);
        }
        List<TeamTotal> totals = participants.stream()
                .collect(Collectors.groupingBy(
                        MatchParticipant::getMatchTeam,
                        Collectors.reducing(BigDecimal.ZERO, MatchParticipant::getVoiceScore, BigDecimal::add)))
                .entrySet().stream()
                .map(entry -> new TeamTotal(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(total -> total.team().getTeamSlot()))
                .toList();
        if (totals.size() != TEAM_COUNT) {
            throw new BusinessException(MatchErrorCode.GAME_RECORDS_NOT_COMPLETE);
        }
        return totals;
    }

    // 합계가 큰 팀이 승리하고, 합계가 같으면 무승부다.
    private List<MatchResult> judge(List<TeamTotal> totals) {
        int comparison = totals.get(0).total().compareTo(totals.get(1).total());
        if (comparison == 0) {
            return List.of(MatchResult.DRAW, MatchResult.DRAW);
        }
        return comparison > 0
                ? List.of(MatchResult.WIN, MatchResult.LOSE)
                : List.of(MatchResult.LOSE, MatchResult.WIN);
    }

    // 승패는 match_team.result에만 기록한다. 랭킹 점수는 배점 정책이 정해지지 않아 기록하지 않는다.
    private void finalizeResult(GameMatch gameMatch, List<MatchParticipant> participants, LocalDateTime now) {
        List<TeamTotal> totals = calculateTeamTotals(participants);
        List<MatchResult> results = judge(totals);
        for (int i = 0; i < totals.size(); i++) {
            totals.get(i).team().updateResult(results.get(i));
        }
        gameMatch.finish(now);
    }

    private record TeamTotal(MatchTeam team, BigDecimal total) {
    }
}
