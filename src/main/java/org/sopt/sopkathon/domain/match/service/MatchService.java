package org.sopt.sopkathon.domain.match.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.common.exception.BusinessException;
import org.sopt.sopkathon.domain.match.code.MatchErrorCode;
import org.sopt.sopkathon.domain.match.dto.response.MatchGameTypeResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchGameTypeUpdateResponse;
import org.sopt.sopkathon.domain.match.dto.response.MatchReadyResponse;
import org.sopt.sopkathon.domain.match.entity.GameMatch;
import org.sopt.sopkathon.domain.match.entity.GameType;
import org.sopt.sopkathon.domain.match.entity.MatchParticipant;
import org.sopt.sopkathon.domain.match.entity.MatchStatus;
import org.sopt.sopkathon.domain.match.repository.GameMatchRepository;
import org.sopt.sopkathon.domain.match.repository.MatchParticipantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

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
}
