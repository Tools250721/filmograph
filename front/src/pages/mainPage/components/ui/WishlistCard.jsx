import React from "react";
import { useNavigate } from "react-router-dom";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import styles from "./WishlistCard.module.css";

const WishlistCard = ({ id, title, year, country, poster }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/movie/${id}`);
  };

  return (
    <div
      onClick={handleClick}
      className={styles.card}
    >
      {/* 포스터 */}
      <ImageWithFallback
        src={poster}
        alt={title}
        className={styles.poster}
        placeholder="이미지 없음"
      />

      {/* 영화 제목 */}
      <p className={styles.title}>
        {title}
      </p>

      {/* 연도, 국가 */}
      <p className={styles.info}>
        {year} · {country}
      </p>
    </div>
  );
};

export default WishlistCard;