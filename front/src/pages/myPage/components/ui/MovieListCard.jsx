import React from "react";
import { useNavigate } from "react-router-dom";
import { ReactComponent as StarIcon } from "../../../../assets/icons/StarIcon.svg";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import styles from "./MovieListCard.module.css";

const MovieListCard = ({ id, title, rating, poster }) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/movie/${id}`);
  };

  return (
    <div onClick={handleClick} className={styles.card}>
      <ImageWithFallback
        src={poster}
        alt={title}
        className={styles.poster}
        placeholder="이미지 없음"
      />
      <p className={styles.title}>{title}</p>
      <p className={styles.rating}>
        평가함 <StarIcon className={styles.starIcon} /> {rating}
      </p>
    </div>
  );
};

export default MovieListCard;