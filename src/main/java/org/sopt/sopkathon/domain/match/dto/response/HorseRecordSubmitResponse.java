package org.sopt.sopkathon.domain.match.dto.response;

public record HorseRecordSubmitResponse(
        Long matchId,
        Long participantId,
        Long finishTimeMilliseconds
) {
}
