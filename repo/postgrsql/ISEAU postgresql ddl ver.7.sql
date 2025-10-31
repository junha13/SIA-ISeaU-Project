-- 처음 실행때만 주석풀고 실행
-- CREATE EXTENSION postgis;

-- 원하는 스키마 접속
--SET search_path TO public;

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
--   tb_fcm_token, -- 실시간 통신
--   tb_user_settings, -- 회원설정사항
--   tb_jellyfish, -- 해파리
--   tb_jellyfish_report, -- 해파리제보
--   tb_rip_current_info, -- 이안류정보
--   tb_weather_info, -- 기상정보
--   tb_role, -- 역할
--   tb_user, -- 회원
--   tb_post_recommend, -- 게시글추천
--   tb_post_comment_recommend, -- 게시글댓글추천
--   tb_beach, -- 해수욕장
--   tb_beach_tag, -- 해수욕장 태그
--   tb_beach_tag_list, -- 해수욕장별 태그목록록
--   tb_first_aid, -- 응급처치
--   tb_beach_weather_info,
--   tb_uv_info
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
CREATE TABLE tb_beach_tag (
    tag_number        		SERIAL PRIMARY KEY,
    tag_name      		VARCHAR(50) NOT NULL UNIQUE   -- 예: '서핑', '가족', '안전'
);

CREATE TABLE tb_beach_tag_list (
    beach_number  integer NOT NULL,
    tag_number    integer NOT NULL,

    -- 중복 방지 (같은 해수욕장에 같은 태그 두 번 못 넣게)
    PRIMARY KEY (beach_number, tag_number)
);

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
     weather_number integer  NOT NULL,
	 beach_weather_number integer not null,
	 uv_number integer not null
    ) 
;

CREATE TABLE tb_board 
    ( 
     board_number serial PRIMARY KEY ,
     board_name   varchar (50)  NOT NULL
    ) 
;

CREATE TABLE tb_first_aid
	(
	first_aid_number SERIAL PRIMARY KEY , -- 직접 넣는 값이라 SERIAL PRIMARY KEY 안함 (사실 했음)
	first_aid_case_num int ,
	first_aid_case_name text ,
	first_aid_content text ,
	first_aid_step int ,
	first_aid_image text ,
	first_aid_description text
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

CREATE TABLE tb_fcm_token (
    user_id VARCHAR(255) PRIMARY KEY, 
    fcm_token TEXT NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

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
	 image_url 		text ,
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
     id		     varchar (50)  NOT NULL , 
     password    varchar (250)  NOT NULL , 
     user_name   varchar (50)  NOT NULL , 
     mobile      varchar (20)  NOT NULL , 
     email       varchar (100)  NOT NULL , 
     birth_date  DATE  NOT NULL , 
     location    geography(Point, 4326) , 
     role_number integer  NOT NULL,
	 beach_number integer
    )
;
Create table tb_post_recommend
	(
	user_number integer not null,
	post_number integer not null,
	PRIMARY KEY (user_number, post_number)
	)
;

Create table tb_post_comment_recommend
	(
	user_number integer not null,
	comment_number integer not null,
	PRIMARY KEY (user_number, comment_number)
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
     humidity          integer , 
     rain              double precision , 
     wind_gusts         double precision , 
     wind_speed         double precision , 
     wind_direction     integer , 
     forecast_time     timestamp NOT null , 
     observed_location geography(Point, 4326) NOT null 
    ) 
;
CREATE TABLE tb_beach_weather_info 
    ( 
     beach_weather_number    SERIAL PRIMARY KEY , 
	 wave_height             double precision,
	 sea_surface_temperature double precision,
     forecast_time           timestamp NOT null , 
     observed_location geography(Point, 4326) NOT null 
    ) 
;
CREATE TABLE tb_uv_info 
    ( 
     uv_number              SERIAL PRIMARY KEY ,
     rain_probability       integer , 
     uv_index               double precision , 
     uv_index_clear_sky     double precision , 
     forecast_time          timestamp NOT null , 
     observed_location geography(Point, 4326) NOT null 
    ) 
;

--(위치, 시각) 기준으로 upsert 가능하도록 유니크 인덱스
CREATE UNIQUE INDEX uq_weather_time_xy1
  ON tb_weather_info (
    (ST_Y(observed_location::geometry)),
    (ST_X(observed_location::geometry)),
    forecast_time
  );
--(위치, 시각) 기준으로 upsert 가능하도록 유니크 인덱스
CREATE UNIQUE INDEX uq_weather_time_xy2
  ON tb_beach_weather_info (
    (ST_Y(observed_location::geometry)),
    (ST_X(observed_location::geometry)),
    forecast_time
  );
--(위치, 시각) 기준으로 upsert 가능하도록 유니크 인덱스
CREATE UNIQUE INDEX uq_weather_time_xy3
  ON tb_uv_info (
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

ALTER TABLE tb_user
	ADD CONSTRAINT tb_beach_FKv5 FOREIGN KEY
	(
	beach_number
	)
	REFERENCES tb_beach
	(
	beach_number
	)

;
ALTER TABLE tb_beach_tag_list
    ADD CONSTRAINT tb_beach_FKv3 FOREIGN KEY
    (
     beach_number
    )
    REFERENCES tb_beach
    (
     beach_number
    )
;
ALTER TABLE tb_beach_tag_list
    ADD CONSTRAINT tb_beach_tag_FKv1 FOREIGN KEY
    (
     tag_number
    )
    REFERENCES tb_beach_tag
    (
     tag_number
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
ALTER TABLE tb_post_recommend
    ADD CONSTRAINT tb_post_FKv1 FOREIGN KEY
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
ALTER TABLE tb_post_recommend
    ADD CONSTRAINT tb_user_FKv8 FOREIGN KEY
    (
     user_number
    )
    REFERENCES tb_user
    (
     user_number
    )
;

ALTER TABLE tb_post_comment_recommend
    ADD CONSTRAINT tb_user_FKv9 FOREIGN KEY
    (
     user_number
    )
    REFERENCES tb_user
    (
     user_number
    )
;
ALTER TABLE tb_post_comment_recommend
    ADD CONSTRAINT tb_post_comment_FK FOREIGN KEY
    (
     comment_number
    )
    REFERENCES tb_post_comment
    (
     comment_number
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
ALTER TABLE tb_beach_daily_forcast 
    ADD CONSTRAINT tb_beach_weather_info_FK FOREIGN KEY 
    ( 
     beach_weather_number
    ) 
    REFERENCES tb_beach_weather_info
    ( 
     beach_weather_number
    ) 
;
ALTER TABLE tb_beach_daily_forcast 
    ADD CONSTRAINT tb_uv_info_FK FOREIGN KEY 
    ( 
     uv_number
    ) 
    REFERENCES tb_uv_info
    ( 
     uv_number
    ) 
;

------------------------------------------------------------------------------------------------
-- 데이터 연결

-- 해수욕장 태그목록 넣기
insert into tb_beach_tag (tag_name)
values ('산책'), ('수영'), ('레저'), ('서핑'), ('가족'), ('핫플'), ('한적'), ('반려동물 동반');
;
-- 게시판 목록 넣기
insert into tb_board (board_name)
values ('공지'), ('정보'), ('친목')
;
-- 해수욕장 데이터 넣기 (주의 : lon - lat 순서임)
INSERT INTO tb_beach (
    beach_name, 
    beach_image, 
    beach_information, 
    location, 
    approved_by_ministry, 
    rating, 
    address, 
    mobile, 
    open_date, 
    close_date
)
VALUES 
    (
        '해운대해수욕장', 
        'https://www.visitbusan.net/uploadImgs/files/cntnts/20191229153531987_oen',
        '해운대 짱짱짱', 
        ST_GeomFromText('POINT(129.1611 35.15981)', 4326)::geography, -- 콤마(,) 추가 및 ::geography 추가
        'Y', 
        4.5, 
        '부산광역시 해운대구 해운대해변로 264 (우동)', 
        '051-749-7611', 
        DATE '2025-06-21', 
        DATE '2025-09-14'
    ),
    (
        '속초해수욕장', 
        'https://cdn.visitkorea.or.kr/img/call?cmd=VIEW&id=3336c139-f3f8-4985-ab2e-813e007e2706', 
        '속초 굿굿굿', 
        ST_GeomFromText('POINT(128.6033 38.19146)', 4326)::geography, -- 콤마(,) 추가 및 ::geography 추가
        'Y', 
        4.3, 
        '강원특별자치도 속초시 해오름로 190 (조양동)', 
        '033-639-2027', 
        DATE '2025-07-04', 
        DATE '2025-08-24'
    ),
    (
        '강문해수욕장', 
        'https://buly.kr/DaPWfhf', 
        '강문 킹킹킹', 
        ST_GeomFromText('POINT(128.9194 37.79558)', 4326)::geography, -- 콤마(,) 추가 및 ::geography 추가
        'Y', 
        4.1, 
        '강원특별자치도 강릉시 강문동', 
        '033-640-4920', -- 콤마(,) 추가
        DATE '2025-07-04', 
        DATE '2025-08-17'
    ),
    (
        '광안리해수욕장', 
        'https://buly.kr/3COonhV', 
        '광안리 킹왕킹', 
        ST_GeomFromText('POINT(129.1190949 35.1546410)', 4326)::geography, -- 콤마(,) 추가 및 ::geography 추가
        'Y', 
        4.4, 
        '부산광역시 수영구 광안해변로 219 (광안동)', 
        '051-622-4251', -- 콤마(,) 추가
        DATE '2025-06-21', 
        DATE '2025-08-31'
    ),
    (
        '중문해수욕장', 
        'https://buly.kr/G3E8PKY', 
        '제주도 짱짱짱', 
        ST_GeomFromText('POINT(126.411 33.245)', 4326)::geography, -- 콤마(,) 추가 및 ::geography 추가
        'Y', 
        3.8, 
        '제주특별자치도 서귀포시 색달동', 
        '064-739-4993', -- 콤마(,) 추가
        DATE '2025-07-01', 
        DATE '2025-08-31'),
		(
    '이호테우해수욕장',
    'https://buly.kr/8emJacU',
    '상당히 재밌다',
    ST_GeomFromText('POINT(126.453120 33.498035)', 4326)::geography,
    'Y',
    4.3,
    '제주특별자치도 제주시 이호일동 1665-13',
    '064-728-3994',
    DATE '2025-07-15',
    DATE '2025-08-15'
    ),
	(
    '경포대해수욕장',
    'https://buly.kr/8ph4YvI',
    '무지 재밌다',
    ST_GeomFromText('POINT(128.908479 37.805874)', 4326)::geography,
    'Y',
    4.4,
    '강원특별자치도 강릉시 강문동 산1',
    '0507-1320-4901',
    DATE '2025-06-28',
    DATE '2025-08-17'
	),
	(
    '협재해수욕장',
    'https://buly.kr/Gvnvh5N',
    '정말 재밌다',
    ST_GeomFromText('POINT(126.239878 33.394301)', 4326)::geography,
    'Y',
    4.6,
    '제주특별자치도 제주시 한림읍 한림로 329-10',
    '064-728-3981',
    DATE '2025-06-24',
    DATE '2025-08-31'
),
(
    '함덕해수욕장',
    'https://buly.kr/8emJacU',
    '진짜 재밌다',
    ST_GeomFromText('POINT(126.669806 33.543507)', 4326)::geography,
    'Y',
    4.5,
    '제주특별자치도 제주시 조천읍 신북로',
    '064-728-3989',
    DATE '2025-06-24',
    DATE '2025-08-31'
),
(
    '을왕리해수욕장',
    'https://buly.kr/HSYCdrr',
    '매우 재밌다',
    ST_GeomFromText('POINT(126.372517 37.447692)', 4326)::geography,
    'Y',
    4.2,
    '인천광역시 중구 을왕동 717-25',
    '032-760-6482',
    DATE '2025-06-21',
    DATE '2025-09-07'
),
(
    '방아머리해변',
    'https://buly.kr/A46NS7O',
    '너무 재밌다',
    ST_GeomFromText('POINT(126.3755 37.1325)', 4326)::geography,
    'Y',
    4.2,
    '경기도 안산시 단원구 대부북동',
    '031-481-2357',
    DATE '2025-06-22',
    DATE '2025-08-25'
),
(
    '궁평리해수욕장',
    'https://buly.kr/D3fG84l',
    '완전 재밌다',
    ST_GeomFromText('POINT(126.684918 37.121496)', 4326)::geography,
    'Y',
    4.3,
    '경기도 화성시 서신면 궁평항로 104-9',
    '031-356-7339',
    DATE '2025-07-01',
    DATE '2025-08-31'
),
( '제부도해수욕장',
    'https://buly.kr/HSYCeyZ',
    '제부도 짱짱짱',
    ST_GeomFromText('POINT(126.617329 37.165585)', 4326)::geography,
    'Y',
    4.5,
    '경기도 화성시 서신면 해안길 260',
    '031-5189-6018',
    DATE '2025-07-01',
    DATE '2025-08-31'),

('구룡포해수욕장',
    'https://buly.kr/2Jp434m',
    '구룡포 짱짱짱',
    ST_GeomFromText('POINT(129.56661 35.99741)', 4326)::geography,
    'Y',
    3.5,
    '경상북도 포항시 남구 구룡포읍 호미로426번길 6',
    ' 054-270-6561',
    DATE '2025-07-12',
    DATE '2025-08-24'),
   ('대천해수욕장',
   'https://buly.kr/1n4n8sW',
   '서해 대표 해수욕장',
   ST_GeomFromText('POINT(126.51131 36.31125)', 4326)::geography,
   'Y',
   4.5,
   '충청남도 보령시 신흑동',
   '041-930-3520',
   DATE '2025-07-05', DATE '2025-08-24'),

('꽃지해수욕장',
'https://buly.kr/DwF4pIV',
'할미·할아비바위 노을',
ST_GeomFromText('POINT(126.33421 36.48765)', 4326)::geography,
'Y', 4.4,
'충청남도 태안군 안면읍 승언리 산29-100번지',
'041-670-2543',
DATE '2025-07-05', DATE '2025-08-17'),
('만리포해수욕장',
'https://buly.kr/5UIhkAO',
'태안의 대표 백사장',
ST_GeomFromText('POINT(126.13957 36.7884)', 4326)::geography,
'Y', 4.3,
'충청남도 태안군 모항리 1325-52번지',
'041-672-9662',
DATE '2025-07-05', DATE '2025-08-24'),
('신지명사십리해수욕장',
'https://buly.kr/CWv0yuO',
'직선 4km 백사장',
ST_GeomFromText('POINT(126.81214 34.3272)', 4326)::geography,
'Y', 4.5,
'전라남도 완도군 신지면',
'061-550-5427',
DATE '2025-07-12', DATE '2025-08-17'),

('만성리검은모래해변', 'https://buly.kr/2fea3Gq', '검은모래와 해상레일바이크로 유명한 여수 대표 해변',
 ST_GeomFromText('POINT(127.74508 34.77675)', 4326)::geography, 'Y', 4.3,
 '전라남도 여수시 만흥동 1-3',
'061-651-4525', DATE '2025-07-05', DATE '2025-08-17'),

('율포솔밭해수욕장', 'https://buly.kr/7mCYUzR', '소나무 숲 캠핑장과 해수녹차탕으로 유명한 보성 명소',
 ST_GeomFromText('POINT(127.09065 34.66981)', 4326)::geography, 'Y', 4.2,
 '전라남도 보성군 회천면 우암길 24', '061-850-5448', DATE '2025-07-05', DATE '2025-08-17'),

('우전해수욕장', 'https://buly.kr/1RFHBef', '증도의 얕은 바다와 모래톱으로 가족 단위 인기',
 ST_GeomFromText('POINT(126.11585 34.99616)', 4326)::geography, 'Y', 4.3,
 '전라남도 신안군 증도면 우전리 200', '061-240-4003', DATE '2025-07-05', DATE '2025-08-17'),

('송호해수욕장', 'https://buly.kr/2qZL2ES', '완만한 경사와 얕은 수심으로 어린이 동반에 적합',
 ST_GeomFromText('POINT(126.51966 34.31322)', 4326)::geography, 'Y', 4.1,
 '전라남도 해남군 송지면 땅끝해안로 1827','061- 530- 5917', DATE '2025-07-18', DATE '2025-08-17'),

('가마미해수욕장', 'https://buly.kr/7x7JU6h', '서해안 노을 명소, 넓은 백사장과 야영장',
 ST_GeomFromText('POINT(126.40867 35.39878)', 4326)::geography, 'Y', 4.3,
 '전라남도 영광군 염산면 가마미해수욕장길 23', '061-356-1020', DATE '2025-07-18', DATE '2025-08-24'),

('변산해수욕장',    'https://buly.kr/1RFHCN5', '부안의 대표 관광지, 변산반도 국립공원 내 위치',
 ST_GeomFromText('POINT(126.53113 35.68032)', 4326)::geography, 'Y', 4.5,
 '전라북도 부안군 변산면 대항리 603', '063-582-7808', DATE '2025-07-04', DATE '2025-08-17'),

('격포해수욕장', 'https://buly.kr/74XWaRX', '채석강과 함께 즐기는 해변',
 ST_GeomFromText('POINT(126.4696 35.62906)', 4326)::geography, 'Y', 4.4,
 '전라북도 부안군  격포리 산 47-1', '063-580-4493', DATE '2025-07-04', DATE '2025-08-17'),

('고사포해수욕장', 'https://buly.kr/8824TK2', '가족 단위 캠핑 명소',
 ST_GeomFromText('POINT(126.50869 35.66277)', 4326)::geography, 'Y', 4.3,
 '전라북도 부안군  변산면 운산리 441-4', '063-580-4493', DATE '2025-07-04', DATE '2025-08-17'),

('모항해수욕장', 'https://buly.kr/6Bxjg26', '조용하고 프라이빗한 소규모 해변',
 ST_GeomFromText('POINT(126.50756 35.5822)', 4326)::geography, 'Y', 4.2,
 '전라북도 부안군 변산면 도청리 165',   '063-583-6941', DATE '2025-07-04', DATE '2025-08-17'),

('위도해수욕장', 'https://buly.kr/5fDSjWo', '도서여행 명소, 맑은 물과 고운 모래',
 ST_GeomFromText('POINT(126.28325 35.60463)', 4326)::geography, 'Y', 4.4,
 '전라북도 부안군 위도면 진리 575', '063-580-3762', DATE '2025-07-04', DATE '2025-08-17'),

('무창포해수욕장', 'https://buly.kr/CB5VF1U', '신비의 바닷길로 유명',
 ST_GeomFromText('POINT(126.53606 36.24478)', 4326)::geography,
 'Y', 4.4, '충청남도 보령시 웅천읍 열린바다1길 10', '041-930-0800', DATE '2025-07-12', DATE '2025-08-24'),


('천리포해수욕장', 'https://buly.kr/5UIhy0U', '천리포수목원 인근 잔잔한 해변',
 ST_GeomFromText('POINT(126.15062 36.80234)', 4326)::geography,
 'Y', 4.3, '충남 태안군 소원면 의항리 978-2번지', '041-670-2695', DATE '2025-07-05', DATE '2025-08-17'),

('학암포해수욕장', 'https://buly.kr/FhOerJU', '완만한 수심과 솔숲 야영지',
 ST_GeomFromText('POINT(126.20323 36.89768)', 4326)::geography,
 'Y', 4.3, '충청남도 태안군 원북면 방갈리 515-121번지', '041-670-2695', DATE '2025-07-05', DATE '2025-08-17'),


('삼봉해수욕장', 'https://buly.kr/ChpmBQz', '곰섬·삼봉·기지포 연계 드라이브 코스',
 ST_GeomFromText('POINT(126.31733 36.56799)', 4326)::geography,
 'Y', 4.2, '충남 태안군 안면읍 창기리 1304-3번지', '041-670-2695', DATE '2025-07-05', DATE '2025-08-17'),

('신두리해수욕장', 'https://buly.kr/6XnFqrP', '신두리 사구 인접 자연경관',
 ST_GeomFromText('POINT(126.19112 36.84078)', 4326)::geography,
 'Y', 4.2, '충청남도 태안군 원북면 신두해변길 199', '041-670-2695', DATE '2025-07-05', DATE '2025-08-17'),

('영일대해수욕장', 'https://buly.kr/C0AkG8P', '포항 도심 접근성 최고, 영일대 누각',
 ST_GeomFromText('POINT(129.37919 36.05653)', 4326)::geography,
 'Y', 4.5, '경상북도 포항시 북구  항구동 17-228',    '054-246-0041', DATE '2025-07-12', DATE '2025-08-24'),

('송도해변(포항)', 'https://buly.kr/7mCYiXU', '2025년 재개장(보도자료)',
 ST_GeomFromText('POINT(129.3799 36.03556)', 4326)::geography,
 'Y', 4.2, '경상북도 포항시 남구 송도동 1171', '054-240-7314', DATE '2025-07-12', DATE '2025-08-24'),

('칠포해수욕장', 'https://buly.kr/YfUV4v', '서퍼·캠핑 인기 스팟',
 ST_GeomFromText('POINT(129.4001 36.13196)', 4326)::geography,
 'Y', 4.3, '경상북도 포항시 북구 흥해읍 칠포리 197-31', '054-261-3001', DATE '2025-07-12', DATE '2025-08-24'),

('월포해수욕장', 'https://buly.kr/CpyXQr', '완만한 수심과 긴 백사장',
 ST_GeomFromText('POINT(129.37137 36.2015)', 4326)::geography,
 'Y', 4.2, '경상북도 포항시 북구 청하면 월포리 294-21', '054-232-9770', DATE '2025-07-12', DATE '2025-08-24'),


('구조라해수욕장', 'https://buly.kr/8TradjK', '외도·해금강 접근 용이, 가족 인기',
 ST_GeomFromText('POINT(128.68958 34.8081)', 4326)::geography,
 'Y', 4.5, '경상남도 거제시 일운면 거제대로 2056', '055-639-3000', DATE '2025-07-05', DATE '2025-08-24'),

('학동몽돌해수욕장', 'https://buly.kr/3u3t8V8', '자갈 파도소리로 유명한 몽돌 해변',
 ST_GeomFromText('POINT(128.64153 34.77328)', 4326)::geography,
 'Y', 4.4, '경상남도 거제시 동부면 학동6길 ','055-635-5421', DATE '2025-07-05', DATE '2025-08-24'),

('와현모래숲해변', 'https://buly.kr/G3EAoib', '거제 동부의 잔잔한 모래사장',
 ST_GeomFromText('POINT(128.70738 34.81279)', 4326)::geography,
 'Y', 4.3, '경남 거제시 일운면 와현리', '055-639-4243', DATE '2025-07-05', DATE '2025-08-24'),

('상주 은모래비치', 'https://buly.kr/90bra4a', '남해 대표 백사장(에메랄드 수색)',
 ST_GeomFromText('POINT(127.98892 34.72021)', 4326)::geography,
 'Y', 4.2, '경남 남해군 상주면 상주리', '055-860-3073', DATE '2025-07-11', DATE '2025-08-24'),

('광암해수욕장(창원)', 'https://buly.kr/8IwpenU', '창원 유일의 도심형 해수욕장',
 ST_GeomFromText('POINT(128.5006 35.10274)', 4326)::geography,
 'Y', 4.0, '경상남도 창원시 마산합포구 진동면 광암해안길', '055-225-6861', DATE '2025-07-05', DATE '2025-08-24'),

('송정해수욕장', 'https://buly.kr/8emLcPx', '서핑 포인트', ST_GeomFromText('POINT(129.20024 35.17915)', 4326)::geography, 'Y', 4.6,
 '부산광역시 해운대구 송정동 712-8', '051-749-5800', DATE '2025-06-21', DATE '2025-08-31'), -- 좌표: 송정역 인근 위키(동해선) :contentReference[oaicite:5]{index=5}

('다대포해수욕장', 'https://buly.kr/GZyRl2h', '낙조 명소, 모래사장', ST_GeomFromText('POINT(128.96353 35.04661)', 4326)::geography, 'Y', 4.2,
 '부산광역시 사하구 다대동 482-3번지', '051-220-4912', DATE '2025-07-01', DATE '2025-08-31'),

('일광해수욕장', 'https://buly.kr/8TradRO', '가족 휴양에 적합', ST_GeomFromText('POINT(129.23498 35.26107)', 4326)::geography, 'Y', 4.5,
 '부산광역시 기장군 일광읍 삼성3길 17', '051-709-5446', DATE '2025-07-01', DATE '2025-08-31'),

('송도해수욕장', 'https://buly.kr/DEa37Qr', '대한민국 최초 공설 해수욕장(1913)', ST_GeomFromText('POINT(129.01881 35.07564)', 4326)::geography, 'Y', 4.5,
 '부산광역시 서구 암남동 135-5', '051-240-4000', DATE '2025-07-01', DATE '2025-08-31'),

('안목해변', 'https://buly.kr/FWTtrsg', '커피거리로 유명', ST_GeomFromText('POINT(128.94464 37.77433)', 4326)::geography, 'Y', 4.0,
 '강원특별자치도 강릉시 견소동 286', '033-660-3887', DATE '2025-07-04', DATE '2025-08-17'),

('주문진해수욕장', 'https://buly.kr/8emLc2N', '북강릉 대표, BTS 버스정류장 인근', ST_GeomFromText('POINT(128.82121 37.90942)', 4326)::geography, 'Y', 3.7,
 '강원특별자치도 강릉시 주문진읍 향호리 8-35', '033-640-4535', DATE '2025-07-04', DATE '2025-08-17'),

('낙산해수욕장', 'https://buly.kr/612ytlv', '양양 대표 해변', ST_GeomFromText('POINT(128.63751 38.11548)', 4326)::geography, 'Y', 4.1,
 '강원특별자치도 양양군 강현면 해맞이길 55', '033-670-2518', DATE '2025-07-11', DATE '2025-08-24'),

('망상해수욕장', 'https://buly.kr/CM0GD6S', '동해시 대표 해변(오토캠핑장)', ST_GeomFromText('POINT(129.09002 37.59425)', 4326)::geography, 'Y', 4.5,
 '강원특별자치도 동해시 망상동 393-36',    '033-530-2800', DATE '2025-07-09', DATE '2025-08-17'),

('추암해변', 'https://buly.kr/GZyRkfT', '촛대바위 일출 명소', ST_GeomFromText('POINT(129.1596 37.47815)', 4326)::geography, 'Y', 3.8,
 '강원특별자치도 동해시 추암동 1', '033-530-2801', DATE '2025-07-09', DATE '2025-08-17'),
('삼척해수욕장', 'https://buly.kr/EoorwSD', '삼척 시내 접근 용이', ST_GeomFromText('POINT(129.17950 37.44030)', 4326)::geography, 'Y', 4.5,
 '   강원특별자치도 삼척시 테마타운길 76 ',    '033-570-3074', DATE '2025-07-09', DATE '2025-08-17'),

('맹방해수욕장', 'https://buly.kr/8IwpeEM', '완만한 경사, 가족형', ST_GeomFromText('POINT(129.23489 37.38894)', 4326)::geography, 'Y', 4.5,
 '강원특별자치도 삼척시 근덕면 하맹방리', '033-570-3074', DATE '2025-07-09', DATE '2025-08-17'),

-- ===================== 인천 (지자체·관광공사 공지/기사 기준) =====================
('왕산해수욕장', 'https://buly.kr/ChpmAc2', '을왕리 북측 조용한 백사장', ST_GeomFromText('POINT(126.3683442 37.4564569)', 4326)::geography, 'Y', 4.4,
 '인천광역시 중구 을왕동 810-97', '032-760-8874', DATE '2025-06-21', DATE '2025-09-07'),

('마시안해변', 'https://buly.kr/1RFHONA', '용유도 카페거리·노을', ST_GeomFromText('POINT(126.41819 37.47630)', 4326)::geography, 'Y', 4.4,
 '인천광역시 중구 마시란로 118', '032-746-3093', DATE '2025-06-21', DATE '2025-09-07'),

('하나개해수욕장', 'https://buly.kr/90brZRX', '무의도 대표 해변(갯벌체험)', ST_GeomFromText('POINT(126.40889 37.38584)', 4326)::geography, 'Y', 4.5,
 '인천광역시 중구 무의동 산189', '032-751-8833', DATE '2025-06-21', DATE '2025-09-07'),

('서포리해수욕장','https://buly.kr/9MRNX13', '3km 백사장·소나무숲', ST_GeomFromText('POINT(126.11405 37.2201)', 4326)::geography, 'Y', 4.1,
 '인천광역시 옹진군 덕적면 덕적남로 582', '   032-831-6623', DATE '2025-07-21', DATE '2025-08-17'),
('실미해수욕장', 'https://buly.kr/BIViK1M', '잔잔한 갯벌·모래밭', ST_GeomFromText('POINT(126.40262 37.40347)', 4326)::geography, 'Y', 4.3,
 '인천광역시 중구 큰무리로 99', '032-752-4466', DATE '2025-01-01', DATE '2025-12-31');

ALTER TABLE tb_beach ADD COLUMN region varchar(20);
UPDATE tb_beach SET region='부산' WHERE address LIKE '부산%';
UPDATE tb_beach SET region='강원' WHERE address LIKE '강원%';
UPDATE tb_beach SET region='제주' WHERE address LIKE '제주%';
UPDATE tb_beach SET region='인천' WHERE address LIKE '인천%';
UPDATE tb_beach SET region='경기' WHERE address LIKE '경기%';
UPDATE tb_beach SET region='경남' WHERE address LIKE '경상남도%';
UPDATE tb_beach SET region='경북' WHERE address LIKE '경상북도%';
UPDATE tb_beach SET region='울산' WHERE address LIKE '울산%';
UPDATE tb_beach SET region='전남' WHERE address LIKE '전라남도%';
UPDATE tb_beach SET region='전북' WHERE address LIKE '전라북도%';
UPDATE tb_beach SET region='충남' WHERE address LIKE '충청남도%';

-- role 데이터 넣기
INSERT INTO tb_role (role_number, role_name)
VALUES 
(1, '관리자'),
(2, '일반 사용자');

-- 회원정보 넣기
INSERT INTO tb_user (user_name, email, mobile, id, password, birth_date, role_number, location)
VALUES ('사람일', 'person1@lxmail.com', '010-1111-1111', 'imperson1', 'person1234', '2003-10-28', 2, ST_SetSRID(ST_MakePoint(126.8795001, 37.4915885),4326)),
		('1', '2@2', '1', '1', '1', '2003-10-28', 2, ST_SetSRID(ST_MakePoint(126.8795001, 37.4915885),4326)),
		('13', '2@2', '2', '2', '2', '2003-10-28', 2, ST_SetSRID(ST_MakePoint(126.8795001, 37.4915885),4326));

-- 응급처치 데이터 넣기
insert into tb_first_aid (first_aid_case_num, first_aid_case_name,
first_aid_step, first_aid_content, first_aid_image,
first_aid_description) values
(1, '해파리 쏘임 응급 처치', 1, '바다에서 나오기', 'images/fa/jellyfish1.mp4', '침착하게 바다에서 나와 안전한 곳으로 이동하세요.'),
(1, '해파리 쏘임 응급 처치', 2, '촉수 제거하기', 'images/fa/jellyfish1.mp4', '핀셋이나 카드로 조심스럽게 제거, 맨손으로 만지지 마세요!'),
(1, '해파리 쏘임 응급 처치', 3, '바닷물로 씻기', 'images/fa/jellyfish1.mp4', '바닷물로 부드럽게 씻어내기, 민물이나 알코올 사용 금지!'),
(1, '해파리 쏘임 응급 처치', 4, '냉찜질하기', 'images/fa/jellyfish1.mp4', '깨끗한 물수건이나 얼음팩을 수건에 감싸 이용해 피부 온도를 낮춰주세요., 얼음을 직접 피부에 대지 마세요!'),

(2, '저체온증 응급 처치', 1, '바다에서 나오기', 'images/fa/jellyfish1.mp4', '침착하게 바다에서 나와 안전한 곳으로 이동하세요.'),
(2, '저체온증 응급 처치', 2, '젖은 옷 벗기', 'images/fa/jellyfish1.mp4', '젖은 옷은 체온을 빼앗으므로 모두 벗깁니다.'),
(2, '저체온증 응급 처치', 3, '따뜻하게 하기', 'images/fa/jellyfish1.mp4', '따뜻한 음료를 마시게 하고 옷을 껴입게 합니다., 겨드랑이나 배 목 뒤 등 큰 혈관이 있는 곳에 따뜻한 물주머니를 둡니다. (화상 주의 !)'),
(2, '저체온증 응급 처치', 4, '움직임 최소화', 'images/fa/jellyfish1.mp4', '과도한 움직임은 심근에 부담을 주므로 최소화합니다.'),
