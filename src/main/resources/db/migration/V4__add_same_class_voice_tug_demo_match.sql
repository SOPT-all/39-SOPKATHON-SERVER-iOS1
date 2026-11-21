-- V4: 같은 반 두 명(11101 김민서, 11102 김지우)이 함께 줄다리기를 시연할 수 있는 경기 추가
--
-- 스키마 변경은 없고 데이터만 추가한다. V1~V3와 기존 경기(1025 포함)는 건드리지 않는다.
-- 경기 1026: VOICE_TUG, IN_PROGRESS, 2 vs 2
--   우리 팀(성수고등학교 1-1, team 2048): 11101 김민서, 11102 김지우 - 제출 전
--   상대 팀(성수고등학교 2-2, team 2049): 12201 이민서, 12202 이지우 - 제출 완료(목 데이터)
-- 두 사람이 모두 제출하면 팀 합계로 승패가 확정되고 경기가 종료된다.
-- 시간 값은 마이그레이션 실행 시각(@base_at) 기준 상대값이다. (Asia/Seoul)

SET NAMES utf8mb4;
SET time_zone = '+09:00';
SET @base_at = TIMESTAMP(DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:00'));

INSERT INTO game_match (id, game_type, team_size, status, created_at, starts_at, ends_at, finished_at) VALUES
    (1026, 'VOICE_TUG', 2, 'IN_PROGRESS', @base_at - INTERVAL 30 SECOND, @base_at - INTERVAL 2 SECOND, @base_at + INTERVAL 1 DAY, NULL);

INSERT INTO match_team (id, game_match_id, school_class_id, team_slot, result, average_finish_time_milliseconds, average_voice_score, ranking_points) VALUES
    (2048, 1026, 111, 1, NULL, NULL, NULL, NULL),
    (2049, 1026, 122, 2, NULL, NULL, NULL, NULL);

INSERT INTO match_participant (id, match_team_id, member_id, is_ready, record_status, finish_time_milliseconds, voice_score, submitted_at) VALUES
    (3164, 2048, 11101, TRUE, 'PENDING', NULL, NULL, NULL),
    (3165, 2048, 11102, TRUE, 'PENDING', NULL, NULL, NULL),
    (3166, 2049, 12201, TRUE, 'SUBMITTED', NULL, 72.400, @base_at - INTERVAL 1 SECOND),
    (3167, 2049, 12202, TRUE, 'SUBMITTED', NULL, 68.100, @base_at - INTERVAL 1 SECOND);
