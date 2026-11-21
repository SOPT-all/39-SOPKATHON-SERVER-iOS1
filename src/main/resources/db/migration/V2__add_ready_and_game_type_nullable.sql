-- V2: 준비 상태 컬럼, 종목 NULL 허용, 진행 중 시연 경기 더미 데이터
--
-- 변경
--   - match_participant.is_ready 추가. 기존 참가자는 모두 종료된 경기의 참가자라 TRUE로 채운다.
--   - game_match.game_type을 NULL 허용으로 변경. 종목 선택(S06) 전에는 종목이 없다.
--   - 진행 중 경기 1건 추가. 대기·준비·종목 선택·게임 제출 API를 시연·테스트하는 데 쓴다.
-- 경기 상태는 새로 추가하지 않고 기존 값을 쓴다.
--   SEARCHING 준비 대기 / FOUND 전체 준비 완료 / IN_PROGRESS 게임 진행 / FINISHED 결과 확정
-- 시연용 member_id: 1 (김민서, 우리팀 성수고등학교 1-1). 상대 목 데이터 사용자: 7 (윤지호, 성수고등학교 1-2)

SET time_zone = '+09:00';
SET @base_at = TIMESTAMP(DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:00'));

-- 스키마

ALTER TABLE match_participant
    ADD COLUMN is_ready BOOLEAN NOT NULL DEFAULT FALSE COMMENT '준비 완료 여부' AFTER member_id;

UPDATE match_participant SET is_ready = TRUE;

ALTER TABLE game_match
    MODIFY COLUMN game_type VARCHAR(20) NULL COMMENT 'HORSE_TAP 말 달리기 / VOICE_TUG 줄다리기. 종목 선택 전에는 NULL';

-- 목 데이터: 진행 중인 시연 경기 1건

-- 경기. 종목은 아직 선택 전이다.
INSERT INTO game_match (id, game_type, team_size, status, created_at, starts_at, ends_at, finished_at) VALUES
    (11, NULL, 1, 'SEARCHING', @base_at, NULL, NULL, NULL);

-- 경기 팀. 우리팀(성수고등학교 1-1)과 상대팀(성수고등학교 1-2)
INSERT INTO match_team (id, game_match_id, school_class_id, team_slot, result, average_finish_time_milliseconds, average_voice_score, ranking_points) VALUES
    (21, 11, 1, 1, NULL, NULL, NULL, NULL),
    (22, 11, 2, 2, NULL, NULL, NULL, NULL);

-- 경기 참가자. 우리 사용자는 준비 전, 상대 사용자는 말달리기 시간과 줄다리기 dB를 미리 넣어 둔 목 데이터다.
INSERT INTO match_participant (id, match_team_id, member_id, is_ready, record_status, finish_time_milliseconds, voice_score, submitted_at) VALUES
    (61, 21, 1, FALSE, 'PENDING', NULL, NULL, NULL),
    (62, 22, 7, TRUE, 'SUBMITTED', 11800, 72.400, @base_at);
