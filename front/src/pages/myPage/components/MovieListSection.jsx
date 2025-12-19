import React from "react";
import { ReactComponent as StarIcon } from "../../../assets/icons/StarIcon.svg";
import MovieListCard from "./ui/MovieListCard";
import styles from "./MovieListSection.module.css";

const MovieListSection = ({ selectedInterval, movies = [] }) => {
  if (selectedInterval === null) {
    return (
      <div className={styles.card}>
        <p className={styles.promptMessage}>점수 구간을 선택해주세요.</p>
      </div>
    );
  }

  const start = Number(selectedInterval);
  const end = start + 0.5;
  const filtered = movies
    .filter((m) => m.rating > start && m.rating <= end)
    .slice(0, 10);

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>
        <StarIcon className={styles.starIcon} /> {start.toFixed(1)} ~{" "}
        {end.toFixed(1)}
      </h3>

      <div className={styles.movieGrid}>
        {filtered.length > 0 ? (
          filtered.map((movie) => (
            <MovieListCard
              key={movie.id}
              id={movie.id}
              title={movie.title}
              rating={movie.rating}
              poster={movie.poster}
            />
          ))
        ) : (
          <p className={styles.emptyMessage}>
            이 점수 구간에 평가한 영화가 없습니다.
          </p>
        )}
      </div>
    </div>
  );
};

export default MovieListSection;