-- V4: 같은 반 두 명(11101 김민서, 11102 김지우)이 줄다리기와 말달리기를 함께 시연할 수 있는 경기 추가
--
-- 스키마 변경은 없고 데이터만 추가한다. V1~V3와 기존 경기(1025 포함)는 건드리지 않는다.
-- 시연 경기 2건 (모두 IN_PROGRESS, 2 vs 2)
--   1) VOICE_TUG(줄다리기), 2) HORSE_TAP(말달리기)
--   우리 팀(성수고등학교 1-1): 11101 김민서, 11102 김지우 - 제출 전
--   상대 팀(성수고등학교 2-2): 12201 이민서, 12202 이지우 - 제출 완료(목 데이터)
-- 두 사람이 모두 제출하면 팀 기록으로 승패가 확정되고 경기가 종료된다.
--   줄다리기는 팀 최대 데시벨 합계가 큰 팀, 말달리기는 완주 시간이 짧은 팀이 이긴다.
--
-- PK는 고정하지 않고 AUTO_INCREMENT가 발급한다. 고정 ID를 쓰면 배포 DB에 같은 ID의 경기가
-- 이미 있을 때 Duplicate entry로 마이그레이션이 실패해 서버가 뜨지 않기 때문이다.
-- V3 직후 DB라면 줄다리기 경기 ID는 1026, 말달리기 경기 ID는 1027이다. 다른 경기가 이미 있으면
-- 달라질 수 있으며, 아래 쿼리로 찾을 수 있다.
--   SELECT gm.id, gm.game_type FROM game_match gm
--     JOIN match_team mt ON mt.game_match_id = gm.id
--     JOIN match_participant mp ON mp.match_team_id = mt.id
--    WHERE gm.status = 'IN_PROGRESS' AND mp.member_id = 11101 AND gm.team_size = 2;
-- 시간 값은 마이그레이션 실행 시각(@base_at) 기준 상대값이다. (Asia/Seoul)

SET NAMES utf8mb4;
SET time_zone = '+09:00';
SET @base_at = TIMESTAMP(DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:00'));

INSERT INTO game_match (game_type, team_size, status, created_at, starts_at, ends_at, finished_at) VALUES
    ('VOICE_TUG', 2, 'IN_PROGRESS', @base_at - INTERVAL 30 SECOND, @base_at - INTERVAL 2 SECOND, @base_at + INTERVAL 1 DAY, NULL);
SET @match_id = LAST_INSERT_ID();

INSERT INTO match_team (game_match_id, school_class_id, team_slot, result, average_finish_time_milliseconds, average_voice_score, ranking_points) VALUES
    (@match_id, 111, 1, NULL, NULL, NULL, NULL);
SET @our_team_id = LAST_INSERT_ID();

INSERT INTO match_team (game_match_id, school_class_id, team_slot, result, average_finish_time_milliseconds, average_voice_score, ranking_points) VALUES
    (@match_id, 122, 2, NULL, NULL, NULL, NULL);
SET @opponent_team_id = LAST_INSERT_ID();

INSERT INTO match_participant (match_team_id, member_id, is_ready, record_status, finish_time_milliseconds, voice_score, submitted_at) VALUES
    (@our_team_id, 11101, TRUE, 'PENDING', NULL, NULL, NULL),
    (@our_team_id, 11102, TRUE, 'PENDING', NULL, NULL, NULL),
    (@opponent_team_id, 12201, TRUE, 'SUBMITTED', NULL, 72.400, @base_at - INTERVAL 1 SECOND),
    (@opponent_team_id, 12202, TRUE, 'SUBMITTED', NULL, 68.100, @base_at - INTERVAL 1 SECOND);

-- 말달리기 시연 경기: 상대 팀은 완주 시간(밀리초)이 들어 있는 목 데이터다. 평균 12.8초.
INSERT INTO game_match (game_type, team_size, status, created_at, starts_at, ends_at, finished_at) VALUES
    ('HORSE_TAP', 2, 'IN_PROGRESS', @base_at - INTERVAL 30 SECOND, @base_at - INTERVAL 2 SECOND, @base_at + INTERVAL 1 DAY, NULL);
SET @horse_match_id = LAST_INSERT_ID();

INSERT INTO match_team (game_match_id, school_class_id, team_slot, result, average_finish_time_milliseconds, average_voice_score, ranking_points) VALUES
    (@horse_match_id, 111, 1, NULL, NULL, NULL, NULL);
SET @horse_our_team_id = LAST_INSERT_ID();

INSERT INTO match_team (game_match_id, school_class_id, team_slot, result, average_finish_time_milliseconds, average_voice_score, ranking_points) VALUES
    (@horse_match_id, 122, 2, NULL, NULL, NULL, NULL);
SET @horse_opponent_team_id = LAST_INSERT_ID();

INSERT INTO match_participant (match_team_id, member_id, is_ready, record_status, finish_time_milliseconds, voice_score, submitted_at) VALUES
    (@horse_our_team_id, 11101, TRUE, 'PENDING', NULL, NULL, NULL),
    (@horse_our_team_id, 11102, TRUE, 'PENDING', NULL, NULL, NULL),
    (@horse_opponent_team_id, 12201, TRUE, 'SUBMITTED', 12500, NULL, @base_at - INTERVAL 1 SECOND),
    (@horse_opponent_team_id, 12202, TRUE, 'SUBMITTED', 13100, NULL, @base_at - INTERVAL 1 SECOND);
