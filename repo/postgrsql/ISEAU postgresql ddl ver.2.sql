-- 처음 실행때만 주석풀고 실행
-- CREATE EXTENSION postgis;

-- 원하는 스키마 접속
-- SET search_path TO public;

-- 드랍문 드래그하고 ctrl + /
-- DROP TABLE IF EXISTS
--   tb_beach_cannot_forecast, -- 해수욕장비예측정보
--   tb_beach_saved, -- 해수욕장저장목록
--   tb_beach_comment_list, -- 해수욕장댓글목록
--   tb_beach_daily_forcast, -- 해수욕장예측정보
--   tb_post_comment, -- 게시글댓글목록
--   tb_post, -- 게시글
--   tb_board, -- 게시판
--   tb_group, -- 그룹
--   tb_user_settings, -- 회원설정사항
--   tb_jellyfish, -- 해파리
--   tb_jellyfish_report, -- 해파리제보
--   tb_rip_current_info, -- 이안류정보
--   tb_weather_info, -- 기상정보
--   tb_role, -- 역할
--   tb_user, -- 회원
--   tb_beach, -- 해수욕장
--   tb_first_aid -- 응급처치
-- CASCADE;

CREATE TABLE tb_beach 
    ( 
     beach_number         SERIAL PRIMARY KEY , 
     beach_name           varchar (50)  NOT NULL , 
     beach_image          text , 
     beach_information    text , 
     approved_by_ministry CHAR (1)  NOT NULL , 
     rating               DECIMAL (2,1)  NOT NULL , 
     address              text  NOT NULL , 
     location             geography(Point, 4326) , 
     -- available_time       DATE  NOT NULL , 
     mobile               varchar (20)  NOT NULL , 
     open_date            DATE  NOT NULL , 
     close_date           DATE  NOT NULL 
    ) 
;

CREATE TABLE tb_beach_cannot_forecast 
    ( 
     beach_number       integer  NOT NULL , 
     rip_current_number integer  NOT NULL
    ) 
;

ALTER TABLE tb_beach_cannot_forecast
    ADD CONSTRAINT tb_beach_cannot_forecast_PK PRIMARY KEY ( beach_number, rip_current_number ) ;

CREATE TABLE tb_beach_saved 
    ( 
     user_number  integer  NOT NULL , 
     beach_number integer  NOT NULL
    ) 
;

ALTER TABLE tb_beach_saved 
    ADD CONSTRAINT tb_beach_saved_PK PRIMARY KEY ( beach_number, user_number ) ;

CREATE TABLE tb_beach_comment_list 
    ( 
     beach_comment_number SERIAL PRIMARY KEY , 
     comment_content      text  NOT NULL , 
     created_time         TIMESTAMP  NOT NULL , 
     rating               INTEGER  NOT NULL , 
     beach_number         integer  NOT NULL , 
     user_number          integer  NOT NULL 
    ) 
;


CREATE TABLE tb_beach_daily_forcast 
    ( 
     beach_number   integer PRIMARY KEY , 
     forecast_time  DATE , 
     weather_number integer  NOT NULL 
    ) 
;

CREATE TABLE tb_board 
    ( 
     board_number integer  PRIMARY KEY , 
     board_name   varchar (50)  NOT NULL 
    ) 
;

CREATE TABLE tb_first_aid 
    ( 
     first_aid_number  SERIAL PRIMARY KEY , -- 직접 넣는 값이라 SERIAL PRIMARY KEY 안함 (사실 했음)
     first_aid_case    text , 
     first_aid_content text 
    ) 
;


CREATE TABLE tb_group 
    (
     group_number   SERIAL PRIMARY KEY,
     group_leader   integer  NOT NULL , 
     group_member   integer  NOT NULL , 
     group_name     varchar (50) , 
     group_accepted CHAR (1) , 
     marker_color   text 
    ) 
;

CREATE TABLE tb_jellyfish 
    ( 
     year                    DATE  NOT NULL , 
     month                   DATE  NOT NULL , 
     week                    INTEGER  NOT NULL , 
     region                  text  NOT NULL , 
     nomura_jellyfish        INTEGER  NOT NULL , 
     moon_jellyfish          INTEGER  NOT NULL , 
     blue_umbrella_jellyfish INTEGER  NOT NULL 
    ) 
;

ALTER TABLE tb_jellyfish
    ADD CONSTRAINT tb_jellyfish_PK PRIMARY KEY ( year, month, week, region ) ;

CREATE TABLE tb_jellyfish_report 
    ( 
     report_number  SERIAL PRIMARY KEY , 
     location       geography(Point, 4326)  NOT NULL , 
     mobile         varchar (20) , 
     details        text , 
     admin_approval CHAR (1) 
    ) 
;

CREATE TABLE tb_post 
    ( 
     post_number  SERIAL PRIMARY KEY , 
     post_title   varchar (50) , 
     post_content text , 
     view_count   INTEGER , 
     like_count   INTEGER , 
     created_at   TIMESTAMP , 
     board_number integer  NOT NULL , 
     user_number  integer  NOT NULL 
    ) 
;

CREATE TABLE tb_post_comment 
    ( 
     comment_number  SERIAL PRIMARY KEY , 
     comment_content text  NOT NULL , 
     like_count      INTEGER , 
     created_at      TIMESTAMP  NOT NULL , 
     post_number     integer  NOT NULL , 
     user_number     integer  NOT NULL 
    ) 
;

CREATE TABLE tb_rip_current_info 
    ( 
     rip_current_number SERIAL PRIMARY KEY , 
     rip_current_level  varchar (10) , 
     rip_current_index  integer , 
     observed_location  geography(Point, 4326) , 
     recorded_time      DATE 
    ) 
;

CREATE TABLE tb_role 
    ( 
     role_number integer  PRIMARY KEY , -- 지정이니까 SERIAL PRIMARY KEY 안함 이건 진짜로
     role_name   varchar (50)  NOT NULL 
    ) 
;

CREATE TABLE tb_user 
    ( 
     user_number SERIAL PRIMARY KEY , 
     username    varchar (50)  NOT NULL , 
     password    varchar (250)  NOT NULL , 
     user_name   varchar (50)  NOT NULL , 
     mobile      varchar (20)  NOT NULL , 
     email       varchar (100)  NOT NULL , 
     birth_date  DATE  NOT NULL , 
     location    geography(Point, 4326) , 
     role_number integer  NOT NULL 
    ) 
;

CREATE TABLE tb_user_settings 
    ( 
     user_number                integer  PRIMARY KEY , 
     group_no_update_alert      CHAR (1) , 
     tide_alert                 CHAR (1) , 
     group_exit_level1_alert    CHAR (1) , 
     group_exit_level1_distance INTEGER , 
     group_exit_level2_alert    CHAR (1) , 
     group_exit_level2_distance INTEGER , 
     group_exit_level3_alert    CHAR (1) , 
     group_exit_level3_distance INTEGER 
    ) 
;

CREATE TABLE tb_weather_info 
    ( 
     weather_number    SERIAL PRIMARY KEY , 
     temperature       double precision , 
     wind_speed        double precision , 
     wind_direction    integer , 
     strong_wind       double precision , 
     rainfall_percent  double precision , 
     rainfall          double precision , 
     uv                integer , 
     wave_height       double precision , 
     forecast_time     timestamp NOT null , 
     observed_location geography(Point, 4326) NOT null 
    ) 
;

--(위치, 시각) 기준으로 upsert 가능하도록 유니크 인덱스
CREATE UNIQUE INDEX uq_weather_time_xy
  ON tb_weather_info (
    (ST_Y(observed_location::geometry)),
    (ST_X(observed_location::geometry)),
    forecast_time
  );

ALTER TABLE tb_beach_daily_forcast 
    ADD CONSTRAINT tb_beach_FK FOREIGN KEY 
    ( 
     beach_number
    ) 
    REFERENCES tb_beach 
    ( 
     beach_number
    ) 
;

ALTER TABLE tb_beach_saved
    ADD CONSTRAINT tb_beach_FKv1 FOREIGN KEY 
    ( 
     beach_number
    ) 
    REFERENCES tb_beach 
    ( 
     beach_number
    ) 
;

ALTER TABLE tb_beach_cannot_forecast 
    ADD CONSTRAINT tb_beach_FKv2 FOREIGN KEY 
    ( 
     beach_number
    ) 
    REFERENCES tb_beach 
    ( 
     beach_number
    ) 
;

ALTER TABLE tb_beach_comment_list 
    ADD CONSTRAINT tb_beach_FKv4 FOREIGN KEY 
    ( 
     beach_number
    ) 
    REFERENCES tb_beach 
    ( 
     beach_number
    ) 
;

ALTER TABLE tb_post 
    ADD CONSTRAINT tb_board_FK FOREIGN KEY 
    ( 
     board_number
    ) 
    REFERENCES tb_board 
    ( 
     board_number
    ) 
;

ALTER TABLE tb_post_comment 
    ADD CONSTRAINT tb_post_FK FOREIGN KEY 
    ( 
     post_number
    ) 
    REFERENCES tb_post 
    ( 
     post_number
    ) 
;

ALTER TABLE tb_beach_cannot_forecast 
    ADD CONSTRAINT tb_rip_current_info_FK FOREIGN KEY 
    ( 
     rip_current_number
    ) 
    REFERENCES tb_rip_current_info 
    ( 
     rip_current_number
    ) 
;

ALTER TABLE tb_user 
    ADD CONSTRAINT tb_role_FK FOREIGN KEY 
    ( 
     role_number
    ) 
    REFERENCES tb_role 
    ( 
     role_number
    ) 
;

ALTER TABLE tb_beach_comment_list 
    ADD CONSTRAINT tb_user_FK FOREIGN KEY 
    ( 
     user_number
    ) 
    REFERENCES tb_user 
    ( 
     user_number
    ) 
;

ALTER TABLE tb_user_settings 
    ADD CONSTRAINT tb_user_FKv1 FOREIGN KEY 
    ( 
     user_number
    ) 
    REFERENCES tb_user 
    ( 
     user_number
    ) 
;

ALTER TABLE tb_beach_saved
    ADD CONSTRAINT tb_user_FKv2 FOREIGN KEY 
    ( 
     user_number
    ) 
    REFERENCES tb_user 
    ( 
     user_number
    ) 
;

ALTER TABLE tb_group 
    ADD CONSTRAINT tb_user_FKv3 FOREIGN KEY 
    ( 
     group_leader
    ) 
    REFERENCES tb_user 
    ( 
     user_number
    ) 
;

ALTER TABLE tb_group 
    ADD CONSTRAINT tb_user_FKv4 FOREIGN KEY 
    ( 
     group_member
    ) 
    REFERENCES tb_user 
    ( 
     user_number
    ) 
;

ALTER TABLE tb_post 
    ADD CONSTRAINT tb_user_FKv5 FOREIGN KEY 
    ( 
     user_number
    ) 
    REFERENCES tb_user 
    ( 
     user_number
    ) 
;

ALTER TABLE tb_post_comment 
    ADD CONSTRAINT tb_user_FKv7 FOREIGN KEY 
    ( 
     user_number
    ) 
    REFERENCES tb_user 
    ( 
     user_number
    ) 
;

ALTER TABLE tb_beach_daily_forcast 
    ADD CONSTRAINT tb_weather_info_FK FOREIGN KEY 
    ( 
     weather_number
    ) 
    REFERENCES tb_weather_info 
    ( 
     weather_number
    ) 
;

------------------------------------------------------------------------------------------------
-- 데이터 연결

-- 해수욕장 데이터 넣기 (주의 : lon - lat 순서임) 
insert into tb_beach (beach_name, location, approved_by_ministry, rating, address, mobile, open_date, close_date)
values 
	('해운대', ST_GeomFromText('POINT(129.1604 35.1587)', 4326), 1, 1, 1, 1, date '2025-06-01', date '2025-06-01'),
	('경포대', ST_GeomFromText('POINT(128.9487 37.7960)', 4326), 1, 1, 1, 1, date '2025-06-01', date '2025-06-01')
;
