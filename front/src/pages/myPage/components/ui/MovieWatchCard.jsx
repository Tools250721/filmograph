import React from "react";
import { useNavigate } from "react-router-dom";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import styles from "./MovieWatchCard.module.css";

const MovieWatchCard = ({ id, poster, title }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/movie/${id}`);
  };

  return (
    <div
      onClick={handleClick}
      className={styles.card}
    >
      <ImageWithFallback
        src={poster}
        alt={title}
        className={styles.poster}
        placeholder="이미지 없음"
      />
      <p className={styles.title}>
        {title}
      </p>
    </div>
  );
};

export default MovieWatchCard;