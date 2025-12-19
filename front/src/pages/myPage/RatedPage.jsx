import React, { useState, useEffect } from "react";
import Header from "../layouts/Header_2";
import MovieRatedCard from "./components/ui/MovieRatedCard";
import { ReactComponent as NoMoviesIcon } from "../../assets/icons/StarIcon.svg";
import { getMyRatedMovies } from "../../data/myPageAPI";
import styles from "./RatedPage.module.css";

const RatedPage = () => {
  const [ratedMovies, setRatedMovies] = useState([]);
  const [selected, setSelected] = useState("담은 순");
  const [open, setOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const data = getMyRatedMovies();
    setRatedMovies(data);
    setIsLoading(false);
  }, []);

  const options = [
    "담은 순",
    "담은 역순",
    "나의 별점 높은 순",
    "나의 별점 낮은 순",
    "가나다 순",
    "가나다 역순",
  ];

  const sortedMovies = [...ratedMovies].sort((a, b) => {
    switch (selected) {
      case "나의 별점 높은 순":
        return b.rating - a.rating;
      case "나의 별점 낮은 순":
        return a.rating - b.rating;
      case "가나다 순":
        return a.title.localeCompare(b.title);
      case "가나다 역순":
        return b.title.localeCompare(a.title);
      case "담은 역순":
        try {
          return new Date(b.dateRated) - new Date(a.dateRated);
        } catch (e) {
          return 0;
        }
      case "담은 순":
      default:
        try {
          return new Date(a.dateRated) - new Date(b.dateRated);
        } catch (e) {
          return 0;
        }
    }
  });

  if (isLoading) {
    return (
      <div className={styles.pageWrapper}>
        <Header />
        <div className={styles.loadingMessage}>
          평가한 작품 목록을 불러오는 중...
        </div>
      </div>
    );
  }

  return (
    <div className={styles.pageWrapper}>
      <Header />
      <div className={styles.contentWrapper}>
        <h2 className={styles.title}>
          평가한 작품들 ({ratedMovies.length})
        </h2>{" "}
        <hr className={styles.divider} />
        {/* 정렬 드롭다운 */}
        <div className={styles.dropdownContainer}>
          <div className={styles.dropdownWrapper}>
            <button
              onClick={() => setOpen(!open)}
              className={`${styles.dropdownButton} ${
                open ? styles.isOpen : ""
              }`}
            >
              {selected}
              <span className={styles.dropdownArrow}>▼</span>
            </button>
            {open && (
              <div className={styles.optionsList}>
                {options.map((opt, idx) => (
                  <div
                    key={idx}
                    onClick={() => {
                      setSelected(opt);
                      setOpen(false);
                    }}
                    className={styles.optionItem}
                  >
                    {opt}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
        {/* 영화 목록 또는 빈 상태 */}
        {ratedMovies.length === 0 ? (
          <div className={styles.emptyStateContainer}>
            <NoMoviesIcon
              style={{ width: "62px", height: "62px", marginBottom: "20px" }}
            />
            <p className={styles.emptyStateMessage}>
              아직 평가한 작품이 없어요.
            </p>
          </div>
        ) : (
          <div className={styles.movieGrid}>
            {/* 정렬된 목록 사용 */}
            {sortedMovies.map((m) => (
              <MovieRatedCard
                key={m.id}
                id={m.id}
                poster={m.poster}
                title={m.title}
                rating={m.rating}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default RatedPage;