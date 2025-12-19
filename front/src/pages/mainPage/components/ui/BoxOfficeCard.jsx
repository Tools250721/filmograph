import React from "react";
import { useNavigate } from "react-router-dom";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import styles from "./BoxOfficeCard.module.css";

const BoxOfficeCard = ({ id, rank, title, year, country, rating, poster }) => {
  const navigate = useNavigate();

  const handleClick = async () => {
    // movieId가 있으면 직접 이동
    if (id && typeof id === "number" && id > 0) {
      navigate(`/movie/${id}`);
      return;
    }

    // movieId가 없으면 영화명으로 검색
    if (!title) {
      console.warn("영화 제목이 없습니다.");
      return;
    }

    try {
      const { movieAPI } = await import("../../../../utils/api");
      // 로컬 DB에서 영화 검색
      const searchResponse = await movieAPI.searchLocalMovies(title, 0, 5);

      // 응답이 content 배열을 가진 경우 (Page 형식)
      const searchResults = searchResponse?.content || searchResponse || [];

      if (Array.isArray(searchResults) && searchResults.length > 0) {
        // 첫 번째 검색 결과의 ID 사용
        const movieId = searchResults[0].id;
        navigate(`/movie/${movieId}`);
      } else {
        // 검색 결과가 없으면 검색 페이지로 이동
        console.warn(`영화를 찾을 수 없습니다: ${title}`);
        navigate(`/search?q=${encodeURIComponent(title)}`);
      }
    } catch (error) {
      console.error("영화 검색 실패:", error);
      // 검색 실패 시 검색 페이지로 이동
      navigate(`/search?q=${encodeURIComponent(title)}`);
    }
  };

  return (
    <div onClick={handleClick} className={styles.card}>
      <div className={styles.posterContainer}>
        {/* 순위 */}
        <span className={styles.rank}>{rank}</span>

        {/* 포스터 */}
        <ImageWithFallback
          src={poster}
          alt={title}
          className={styles.poster}
          placeholder="이미지 없음"
        />
      </div>

      {/* 영화 정보 */}
      <p className={styles.title}>{title}</p>
      <p className={styles.info}>
        {year} · {country}
      </p>
      <p className={styles.rating}>
        평균 ★ {rating ? Number(rating).toFixed(1) : "0.0"}
      </p>
    </div>
  );
};

export default BoxOfficeCard;
