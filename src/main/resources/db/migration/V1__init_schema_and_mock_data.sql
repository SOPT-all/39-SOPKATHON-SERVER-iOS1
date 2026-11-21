-- V1: 반림픽 초기 스키마와 시연용 목 데이터
--
-- 테이블: school, school_class, member, game_match, match_team, match_participant
-- 규칙
--   - 우리팀은 성수고등학교 1학년 1반(school_class.id = 1)이다. 그 외 학급은 모두 상대팀 목 데이터다.
--   - 집계는 우리팀만 한다. 상대팀 기록은 경기 결과를 완성하기 위한 목 데이터다.
--   - 시연용 member_id: 1 (김민서, 성수고등학교 1-1)
--   - 시간 값은 고정 날짜 대신 마이그레이션 실행 시각(@base_at)에서의 상대값이다. (Asia/Seoul 기준)
--   - 랭킹 점수(WIN 300 / DRAW 100 / LOSE 0)는 확정 정책이 아닌 가정값이다.
--   - 진행 중인 경기(SEARCHING, FOUND, IN_PROGRESS)는 넣지 않는다. 상대 찾기 때 오래된 대기 경기가 섞이지 않게 하기 위해서다.

SET time_zone = '+09:00';
SET @base_at = TIMESTAMP(DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:00'));

-- 스키마

CREATE TABLE school (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE school_class (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    school_id    BIGINT   NOT NULL,
    grade        TINYINT  NOT NULL,
    class_number SMALLINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_school_class_school_grade_class_number UNIQUE (school_id, grade, class_number),
    CONSTRAINT fk_school_class_school FOREIGN KEY (school_id) REFERENCES school (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE member (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    school_class_id BIGINT      NOT NULL,
    display_name    VARCHAR(50) NOT NULL,
    last_seen_at    DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_member_school_class FOREIGN KEY (school_class_id) REFERENCES school_class (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE game_match (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    game_type   VARCHAR(20) NOT NULL COMMENT 'HORSE_TAP 말 달리기 / VOICE_TUG 줄다리기',
    team_size   TINYINT     NOT NULL COMMENT '팀당 참가 인원',
    status      VARCHAR(20) NOT NULL COMMENT 'SEARCHING, FOUND, IN_PROGRESS, FINISHED, TIMEOUT, CANCELLED, INVALID',
    created_at  DATETIME(6) NOT NULL,
    starts_at   DATETIME(6) NULL,
    ends_at     DATETIME(6) NULL,
    finished_at DATETIME(6) NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE match_team (
    id                               BIGINT         NOT NULL AUTO_INCREMENT,
    game_match_id                    BIGINT         NOT NULL,
    school_class_id                  BIGINT         NOT NULL,
    team_slot                        TINYINT        NOT NULL COMMENT '경기 내 팀 구분 번호 (1 또는 2)',
    result                           VARCHAR(10)    NULL COMMENT 'WIN, LOSE, DRAW',
    average_finish_time_milliseconds DECIMAL(12, 3) NULL COMMENT '말 달리기 팀 평균 완주 시간',
    average_voice_score              DECIMAL(7, 3)  NULL COMMENT '줄다리기 팀 평균 음량 점수',
    ranking_points                   INT            NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_match_team_game_match_team_slot UNIQUE (game_match_id, team_slot),
    CONSTRAINT ck_match_team_team_slot CHECK (team_slot IN (1, 2)),
    CONSTRAINT fk_match_team_game_match FOREIGN KEY (game_match_id) REFERENCES game_match (id),
    CONSTRAINT fk_match_team_school_class FOREIGN KEY (school_class_id) REFERENCES school_class (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE match_participant (
    id                       BIGINT        NOT NULL AUTO_INCREMENT,
    match_team_id            BIGINT        NOT NULL,
    member_id                BIGINT        NOT NULL,
    record_status            VARCHAR(16)   NOT NULL COMMENT 'PENDING 기록 대기 / SUBMITTED 제출 / DNF 미완주',
    finish_time_milliseconds BIGINT        NULL COMMENT '말 달리기 개인 완주 시간',
    voice_score              DECIMAL(7, 3) NULL COMMENT '줄다리기 개인 최종 음량 점수',
    submitted_at             DATETIME(6)   NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_match_participant_match_team_member UNIQUE (match_team_id, member_id),
    CONSTRAINT fk_match_participant_match_team FOREIGN KEY (match_team_id) REFERENCES match_team (id),
    CONSTRAINT fk_match_participant_member FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 목 데이터
-- 학교 4개
INSERT INTO school (id, name) VALUES
    (1, '성수고등학교'),
    (2, '서울고등학교'),
    (3, '경복고등학교'),
    (4, '대원고등학교');

-- 학급 8개. id 1이 우리팀(성수고등학교 1-1)
INSERT INTO school_class (id, school_id, grade, class_number) VALUES
    (1, 1, 1, 1),
    (2, 1, 1, 2),
    (3, 1, 2, 1),
    (4, 2, 1, 1),
    (5, 2, 1, 3),
    (6, 3, 1, 2),
    (7, 3, 2, 1),
    (8, 4, 1, 1);

-- 멤버 34명. 기준 시각 5분 이내 last_seen_at은 온라인, 그 외는 오프라인, NULL은 미접속
INSERT INTO member (id, school_class_id, display_name, last_seen_at) VALUES
    (1, 1, '김민서', @base_at - INTERVAL 20 SECOND),
    (2, 1, '이서준', @base_at - INTERVAL 40 SECOND),
    (3, 1, '박지우', @base_at - INTERVAL 60 SECOND),
    (4, 1, '최하은', @base_at - INTERVAL 14400 SECOND),
    (5, 1, '정도윤', @base_at - INTERVAL 18000 SECOND),
    (6, 1, '강서연', NULL),
    (7, 2, '윤지호', @base_at - INTERVAL 20 SECOND),
    (8, 2, '임서아', @base_at - INTERVAL 90 SECOND),
    (9, 2, '한도현', @base_at - INTERVAL 14400 SECOND),
    (10, 2, '오유진', NULL),
    (11, 3, '송하준', @base_at - INTERVAL 20 SECOND),
    (12, 3, '조예은', @base_at - INTERVAL 90 SECOND),
    (13, 3, '배시우', @base_at - INTERVAL 14400 SECOND),
    (14, 3, '류나연', NULL),
    (15, 4, '권태양', @base_at - INTERVAL 20 SECOND),
    (16, 4, '문소율', @base_at - INTERVAL 90 SECOND),
    (17, 4, '장우진', @base_at - INTERVAL 14400 SECOND),
    (18, 4, '노채원', NULL),
    (19, 5, '안재민', @base_at - INTERVAL 20 SECOND),
    (20, 5, '홍다은', @base_at - INTERVAL 90 SECOND),
    (21, 5, '서민재', @base_at - INTERVAL 14400 SECOND),
    (22, 5, '진하람', NULL),
    (23, 6, '남건우', @base_at - INTERVAL 20 SECOND),
    (24, 6, '심아린', @base_at - INTERVAL 90 SECOND),
    (25, 6, '유주원', @base_at - INTERVAL 14400 SECOND),
    (26, 6, '곽지안', NULL),
    (27, 7, '백승현', @base_at - INTERVAL 20 SECOND),
    (28, 7, '마서윤', @base_at - INTERVAL 90 SECOND),
    (29, 7, '천우빈', @base_at - INTERVAL 14400 SECOND),
    (30, 7, '도서하', NULL),
    (31, 8, '변지후', @base_at - INTERVAL 20 SECOND),
    (32, 8, '채은서', @base_at - INTERVAL 90 SECOND),
    (33, 8, '엄도영', @base_at - INTERVAL 14400 SECOND),
    (34, 8, '탁예지', NULL);

-- 경기 10개. 모두 우리팀이 참가한 종료(FINISHED) 경기
INSERT INTO game_match (id, game_type, team_size, status, created_at, starts_at, ends_at, finished_at) VALUES
    (1, 'HORSE_TAP', 2, 'FINISHED', @base_at - INTERVAL 37800 SECOND, @base_at - INTERVAL 37795 SECOND, @base_at - INTERVAL 37735 SECOND, @base_at - INTERVAL 37735 SECOND),
    (2, 'VOICE_TUG', 3, 'FINISHED', @base_at - INTERVAL 34200 SECOND, @base_at - INTERVAL 34195 SECOND, @base_at - INTERVAL 34175 SECOND, @base_at - INTERVAL 34175 SECOND),
    (3, 'HORSE_TAP', 3, 'FINISHED', @base_at - INTERVAL 30600 SECOND, @base_at - INTERVAL 30595 SECOND, @base_at - INTERVAL 30535 SECOND, @base_at - INTERVAL 30535 SECOND),
    (4, 'VOICE_TUG', 2, 'FINISHED', @base_at - INTERVAL 27000 SECOND, @base_at - INTERVAL 26995 SECOND, @base_at - INTERVAL 26975 SECOND, @base_at - INTERVAL 26975 SECOND),
    (5, 'HORSE_TAP', 4, 'FINISHED', @base_at - INTERVAL 23400 SECOND, @base_at - INTERVAL 23395 SECOND, @base_at - INTERVAL 23335 SECOND, @base_at - INTERVAL 23335 SECOND),
    (6, 'VOICE_TUG', 4, 'FINISHED', @base_at - INTERVAL 19800 SECOND, @base_at - INTERVAL 19795 SECOND, @base_at - INTERVAL 19775 SECOND, @base_at - INTERVAL 19775 SECOND),
    (7, 'HORSE_TAP', 2, 'FINISHED', @base_at - INTERVAL 16200 SECOND, @base_at - INTERVAL 16195 SECOND, @base_at - INTERVAL 16135 SECOND, @base_at - INTERVAL 16135 SECOND),
    (8, 'VOICE_TUG', 3, 'FINISHED', @base_at - INTERVAL 12600 SECOND, @base_at - INTERVAL 12595 SECOND, @base_at - INTERVAL 12575 SECOND, @base_at - INTERVAL 12575 SECOND),
    (9, 'HORSE_TAP', 3, 'FINISHED', @base_at - INTERVAL 9000 SECOND, @base_at - INTERVAL 8995 SECOND, @base_at - INTERVAL 8935 SECOND, @base_at - INTERVAL 8935 SECOND),
    (10, 'HORSE_TAP', 4, 'FINISHED', @base_at - INTERVAL 5400 SECOND, @base_at - INTERVAL 5395 SECOND, @base_at - INTERVAL 5335 SECOND, @base_at - INTERVAL 5335 SECOND);

-- 경기 팀 20개. 한 경기에서 team_slot 1·2 중 한쪽이 우리팀(school_class_id = 1)
INSERT INTO match_team (id, game_match_id, school_class_id, team_slot, result, average_finish_time_milliseconds, average_voice_score, ranking_points) VALUES
    (1, 1, 1, 1, 'WIN', 10600, NULL, 300),
    (2, 1, 4, 2, 'LOSE', 12700, NULL, 0),
    (3, 2, 2, 1, 'WIN', NULL, 71.5, 300),
    (4, 2, 1, 2, 'LOSE', NULL, 64.2, 0),
    (5, 3, 1, 1, 'WIN', 11300, NULL, 300),
    (6, 3, 6, 2, 'LOSE', 13100, NULL, 0),
    (7, 4, 8, 1, 'DRAW', NULL, 70, 100),
    (8, 4, 1, 2, 'DRAW', NULL, 70, 100),
    (9, 5, 1, 1, 'LOSE', 14200, NULL, 0),
    (10, 5, 3, 2, 'WIN', 12800, NULL, 300),
    (11, 6, 5, 1, 'LOSE', NULL, 68.8, 0),
    (12, 6, 1, 2, 'WIN', NULL, 76.4, 300),
    (13, 7, 1, 1, 'WIN', 10900, NULL, 300),
    (14, 7, 7, 2, 'LOSE', 11800, NULL, 0),
    (15, 8, 1, 1, 'WIN', NULL, 78.3, 300),
    (16, 8, 4, 2, 'LOSE', NULL, 72.1, 0),
    (17, 9, 2, 1, 'DRAW', 12500, NULL, 100),
    (18, 9, 1, 2, 'DRAW', 12500, NULL, 100),
    (19, 10, 8, 1, 'LOSE', 13400, NULL, 0),
    (20, 10, 1, 2, 'WIN', 10200, NULL, 300);

-- 경기 참가자 60명
INSERT INTO match_participant (id, match_team_id, member_id, record_status, finish_time_milliseconds, voice_score, submitted_at) VALUES
    (1, 1, 1, 'SUBMITTED', 10500, NULL, @base_at - INTERVAL 37784.5 SECOND),
    (2, 1, 2, 'SUBMITTED', 10700, NULL, @base_at - INTERVAL 37784.3 SECOND),
    (3, 2, 15, 'SUBMITTED', 12600, NULL, @base_at - INTERVAL 37782.4 SECOND),
    (4, 2, 16, 'SUBMITTED', 12800, NULL, @base_at - INTERVAL 37782.2 SECOND),
    (5, 3, 7, 'SUBMITTED', NULL, 70.3, @base_at - INTERVAL 34175 SECOND),
    (6, 3, 8, 'SUBMITTED', NULL, 71.5, @base_at - INTERVAL 34175 SECOND),
    (7, 3, 9, 'SUBMITTED', NULL, 72.7, @base_at - INTERVAL 34175 SECOND),
    (8, 4, 2, 'SUBMITTED', NULL, 63, @base_at - INTERVAL 34175 SECOND),
    (9, 4, 3, 'SUBMITTED', NULL, 64.2, @base_at - INTERVAL 34175 SECOND),
    (10, 4, 4, 'SUBMITTED', NULL, 65.4, @base_at - INTERVAL 34175 SECOND),
    (11, 5, 3, 'SUBMITTED', 11150, NULL, @base_at - INTERVAL 30583.85 SECOND),
    (12, 5, 4, 'SUBMITTED', 11300, NULL, @base_at - INTERVAL 30583.7 SECOND),
    (13, 5, 5, 'SUBMITTED', 11450, NULL, @base_at - INTERVAL 30583.55 SECOND),
    (14, 6, 23, 'SUBMITTED', 12950, NULL, @base_at - INTERVAL 30582.05 SECOND),
    (15, 6, 24, 'SUBMITTED', 13100, NULL, @base_at - INTERVAL 30581.9 SECOND),
    (16, 6, 25, 'SUBMITTED', 13250, NULL, @base_at - INTERVAL 30581.75 SECOND),
    (17, 7, 31, 'SUBMITTED', NULL, 69.2, @base_at - INTERVAL 26975 SECOND),
    (18, 7, 32, 'SUBMITTED', NULL, 70.8, @base_at - INTERVAL 26975 SECOND),
    (19, 8, 4, 'SUBMITTED', NULL, 69.2, @base_at - INTERVAL 26975 SECOND),
    (20, 8, 5, 'SUBMITTED', NULL, 70.8, @base_at - INTERVAL 26975 SECOND),
    (21, 9, 5, 'SUBMITTED', 13900, NULL, @base_at - INTERVAL 23381.1 SECOND),
    (22, 9, 6, 'SUBMITTED', 14100, NULL, @base_at - INTERVAL 23380.9 SECOND),
    (23, 9, 1, 'SUBMITTED', 14300, NULL, @base_at - INTERVAL 23380.7 SECOND),
    (24, 9, 2, 'SUBMITTED', 14500, NULL, @base_at - INTERVAL 23380.5 SECOND),
    (25, 10, 11, 'SUBMITTED', 12500, NULL, @base_at - INTERVAL 23382.5 SECOND),
    (26, 10, 12, 'SUBMITTED', 12700, NULL, @base_at - INTERVAL 23382.3 SECOND),
    (27, 10, 13, 'SUBMITTED', 12900, NULL, @base_at - INTERVAL 23382.1 SECOND),
    (28, 10, 14, 'SUBMITTED', 13100, NULL, @base_at - INTERVAL 23381.9 SECOND),
    (29, 11, 19, 'SUBMITTED', NULL, 67, @base_at - INTERVAL 19775 SECOND),
    (30, 11, 20, 'SUBMITTED', NULL, 68.2, @base_at - INTERVAL 19775 SECOND),
    (31, 11, 21, 'SUBMITTED', NULL, 69.4, @base_at - INTERVAL 19775 SECOND),
    (32, 11, 22, 'SUBMITTED', NULL, 70.6, @base_at - INTERVAL 19775 SECOND),
    (33, 12, 6, 'SUBMITTED', NULL, 74.6, @base_at - INTERVAL 19775 SECOND),
    (34, 12, 1, 'SUBMITTED', NULL, 75.8, @base_at - INTERVAL 19775 SECOND),
    (35, 12, 2, 'SUBMITTED', NULL, 77, @base_at - INTERVAL 19775 SECOND),
    (36, 12, 3, 'SUBMITTED', NULL, 78.2, @base_at - INTERVAL 19775 SECOND),
    (37, 13, 1, 'SUBMITTED', 10800, NULL, @base_at - INTERVAL 16184.2 SECOND),
    (38, 13, 2, 'SUBMITTED', 11000, NULL, @base_at - INTERVAL 16184 SECOND),
    (39, 14, 27, 'SUBMITTED', 11700, NULL, @base_at - INTERVAL 16183.3 SECOND),
    (40, 14, 28, 'SUBMITTED', 11900, NULL, @base_at - INTERVAL 16183.1 SECOND),
    (41, 15, 2, 'SUBMITTED', NULL, 77.1, @base_at - INTERVAL 12575 SECOND),
    (42, 15, 3, 'SUBMITTED', NULL, 78.3, @base_at - INTERVAL 12575 SECOND),
    (43, 15, 4, 'SUBMITTED', NULL, 79.5, @base_at - INTERVAL 12575 SECOND),
    (44, 16, 15, 'SUBMITTED', NULL, 70.9, @base_at - INTERVAL 12575 SECOND),
    (45, 16, 16, 'SUBMITTED', NULL, 72.1, @base_at - INTERVAL 12575 SECOND),
    (46, 16, 17, 'SUBMITTED', NULL, 73.3, @base_at - INTERVAL 12575 SECOND),
    (47, 17, 7, 'SUBMITTED', 12350, NULL, @base_at - INTERVAL 8982.65 SECOND),
    (48, 17, 8, 'SUBMITTED', 12500, NULL, @base_at - INTERVAL 8982.5 SECOND),
    (49, 17, 9, 'SUBMITTED', 12650, NULL, @base_at - INTERVAL 8982.35 SECOND),
    (50, 18, 3, 'SUBMITTED', 12350, NULL, @base_at - INTERVAL 8982.65 SECOND),
    (51, 18, 4, 'SUBMITTED', 12500, NULL, @base_at - INTERVAL 8982.5 SECOND),
    (52, 18, 5, 'SUBMITTED', 12650, NULL, @base_at - INTERVAL 8982.35 SECOND),
    (53, 19, 31, 'SUBMITTED', 13100, NULL, @base_at - INTERVAL 5381.9 SECOND),
    (54, 19, 32, 'SUBMITTED', 13300, NULL, @base_at - INTERVAL 5381.7 SECOND),
    (55, 19, 33, 'SUBMITTED', 13500, NULL, @base_at - INTERVAL 5381.5 SECOND),
    (56, 19, 34, 'SUBMITTED', 13700, NULL, @base_at - INTERVAL 5381.3 SECOND),
    (57, 20, 4, 'SUBMITTED', 9900, NULL, @base_at - INTERVAL 5385.1 SECOND),
    (58, 20, 5, 'SUBMITTED', 10100, NULL, @base_at - INTERVAL 5384.9 SECOND),
    (59, 20, 6, 'SUBMITTED', 10300, NULL, @base_at - INTERVAL 5384.7 SECOND),
    (60, 20, 1, 'SUBMITTED', 10500, NULL, @base_at - INTERVAL 5384.5 SECOND);
