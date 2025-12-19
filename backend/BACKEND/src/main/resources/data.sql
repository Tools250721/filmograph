-- ===== 안전 모드 (로컬 개발용) =====
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- 🎬 영화 샘플 (이미 있으면 무시)
-- 테이블 이름이 movies (복수형)로 변경됨
INSERT IGNORE INTO movies (title, original_title, overview, release_year, runtime_minutes, country, age_rating, poster_url, backdrop_url)
VALUES ('너의 이름은.', 'Your Name.', '소년과 소녀의 몸이 바뀌는 신비한 경험...', 2016, 106, 'JP', '12',
        'https://image.tmdb.org/t/p/w500/q719jXXEzOoYaps6babgKONaQfw.jpg',
        'https://image.tmdb.org/t/p/original/7OMAfDJicBfhI3TQe0H8f83ZECq.jpg');

-- 방금(혹은 기존에) 들어가 있던 영화 id 가져오기
SET @movie_id := (SELECT id FROM movies WHERE title = '너의 이름은.' LIMIT 1);

-- 🎤 명대사 (중복 방지)
INSERT INTO quote (movie_id, text, speaker, lang)
SELECT @movie_id, '잃어버린 시간을 찾아서.', '나레이션', 'ko'
WHERE NOT EXISTS (
  SELECT 1 FROM quote
  WHERE movie_id = @movie_id AND text = '잃어버린 시간을 찾아서.' AND lang = 'ko'
);

-- 📺 OTT 제공처 (이름 UNIQUE 가정)
INSERT IGNORE INTO ott_provider (name, type, logo_url) VALUES
('Netflix',    'SUBSCRIPTION', 'https://logo.clearbit.com/netflix.com'),
('Wavve',      'SUBSCRIPTION', 'https://logo.clearbit.com/wavve.com'),
('NaverStore', 'BUY',          'https://logo.clearbit.com/naver.com');

-- provider id 조회
SET @pid_netflix := (SELECT id FROM ott_provider WHERE name = 'Netflix' LIMIT 1);
SET @pid_wavve   := (SELECT id FROM ott_provider WHERE name = 'Wavve'   LIMIT 1);
SET @pid_naver   := (SELECT id FROM ott_provider WHERE name = 'NaverStore' LIMIT 1);

-- 🎬 영화 ↔ OTT 매핑 (중복 방지)
INSERT INTO movie_ott (movie_id, provider_id, region, link_url)
SELECT @movie_id, @pid_netflix, 'KR', 'https://www.netflix.com/title/80092865'
WHERE NOT EXISTS (
  SELECT 1 FROM movie_ott
  WHERE movie_id = @movie_id AND provider_id = @pid_netflix AND region = 'KR'
);

INSERT INTO movie_ott (movie_id, provider_id, region, link_url)
SELECT @movie_id, @pid_wavve, 'KR', 'https://www.wavve.com/'
WHERE NOT EXISTS (
  SELECT 1 FROM movie_ott
  WHERE movie_id = @movie_id AND provider_id = @pid_wavve AND region = 'KR'
);

INSERT INTO movie_ott (movie_id, provider_id, region, link_url)
SELECT @movie_id, @pid_naver, 'KR', 'https://serieson.naver.com/'
WHERE NOT EXISTS (
  SELECT 1 FROM movie_ott
  WHERE movie_id = @movie_id AND provider_id = @pid_naver AND region = 'KR'
);

-- ===== 원복 =====
SET FOREIGN_KEY_CHECKS = 1;
