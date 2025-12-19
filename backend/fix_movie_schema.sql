-- Movie 테이블의 overview 컬럼을 TEXT 타입으로 변경
-- 이 SQL을 MySQL에서 실행하세요

USE filmograph;

-- overview 컬럼을 TEXT로 변경
ALTER TABLE movies MODIFY COLUMN overview TEXT;

-- 다른 컬럼들도 적절한 길이로 변경
ALTER TABLE movies MODIFY COLUMN title VARCHAR(500);
ALTER TABLE movies MODIFY COLUMN original_title VARCHAR(500);
ALTER TABLE movies MODIFY COLUMN poster_url VARCHAR(1000);
ALTER TABLE movies MODIFY COLUMN backdrop_url VARCHAR(1000);
ALTER TABLE movies MODIFY COLUMN director VARCHAR(200);
ALTER TABLE movies MODIFY COLUMN release_date VARCHAR(20);
ALTER TABLE movies MODIFY COLUMN country VARCHAR(10);
ALTER TABLE movies MODIFY COLUMN age_rating VARCHAR(20);

-- 변경 사항 확인
DESCRIBE movies;

