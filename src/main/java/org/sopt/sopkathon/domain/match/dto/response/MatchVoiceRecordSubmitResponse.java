package org.sopt.sopkathon.domain.match.dto.response;

import java.math.BigDecimal;
import org.sopt.sopkathon.domain.match.entity.MatchParticipant;
import org.sopt.sopkathon.domain.match.entity.RecordStatus;

public record MatchVoiceRecordSubmitResponse(
        Long matchId,
        Long memberId,
        BigDecimal maxDecibel,
        RecordStatus recordStatus,
        boolean isResultFinalized
) {

    public static MatchVoiceRecordSubmitResponse of(Long matchId, MatchParticipant participant, boolean isResultFinalized) {
        return new MatchVoiceRecordSubmitResponse(
                matchId,
                participant.getMember().getId(),
                participant.getVoiceScore(),
                participant.getRecordStatus(),
                isResultFinalized);
    }
}
