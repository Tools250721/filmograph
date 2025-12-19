import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { movieAPI } from "../../../../utils/api";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import styles from "./RankingCard.module.css";

const RankingCard = ({ id, title, poster, movieId }) => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const handleClick = async () => {
    // movieId가 있으면 확인 없이 바로 상세 페이지로 이동
    const targetId = movieId || id;
    if (targetId && typeof targetId === "number" && targetId > 0) {
      navigate(`/movie/${targetId}`);
      return;
    }

    // movieId가 없으면 영화명으로 검색
    if (!title) {
      console.warn("영화 제목이 없습니다.");
      navigate(`/search`);
      return;
    }

    setIsLoading(true);
    try {
      // 로컬 DB에서 영화 검색
      const searchResponse = await movieAPI.searchLocalMovies(title, 0, 5);

      // 응답이 content 배열을 가진 경우 (Page 형식)
      const searchResults = searchResponse?.content || searchResponse || [];

      if (Array.isArray(searchResults) && searchResults.length > 0) {
        // 첫 번째 검색 결과의 ID 사용
        const foundMovieId = searchResults[0].id;
        navigate(`/movie/${foundMovieId}`);
      } else {
        // 검색 결과가 없으면 검색 페이지로 이동
        console.warn(`영화를 찾을 수 없습니다: ${title}`);
        navigate(`/search?q=${encodeURIComponent(title)}`);
      }
    } catch (error) {
      console.error("영화 검색 실패:", error);
      // 검색 실패 시 검색 페이지로 이동
      if (title) {
        navigate(`/search?q=${encodeURIComponent(title)}`);
      } else {
        navigate(`/search`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div onClick={handleClick} className={styles.card}>
      {/* 포스터 */}
      <ImageWithFallback
        src={poster}
        alt={title}
        className={styles.poster}
        placeholder="이미지 없음"
      />

      {/* 영화 제목 */}
      <p className={styles.title}>{title}</p>
    </div>
  );
};

export default RankingCard;
