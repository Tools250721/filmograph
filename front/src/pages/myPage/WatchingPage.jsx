import React, { useState, useEffect } from "react";
import Header_2 from "../layouts/Header_2";
import MovieWatchCard from "./components/ui/MovieWatchCard";
import { ReactComponent as NoMoviesIcon } from "../../assets/icons/NoMoviesIcon.svg";
import { getMyWatchingList } from "../../data/myPageAPI";
import styles from "./WatchingPage.module.css";

const WatchingPage = () => {
  const [watchingMovies, setWatchingMovies] = useState([]);
  const [selected, setSelected] = useState("담은 순");
  const [open, setOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        const data = await getMyWatchingList();
        setWatchingMovies(Array.isArray(data) ? data : []);
      } catch (error) {
        console.error("보는 중 목록 로딩 실패:", error);
        setWatchingMovies([]);
      } finally {
        setIsLoading(false);
      }
    };
    loadData();
  }, []);

  const options = ["담은 순", "담은 역순", "가나다 순", "가나다 역순"];

  const sortedMovies = [...watchingMovies].sort((a, b) => {
    switch (selected) {
      case "가나다 순":
        return a.title.localeCompare(b.title);
      case "가나다 역순":
        return b.title.localeCompare(a.title);
      case "담은 역순":
        try {
          const dateA = a.dateAdded ? new Date(a.dateAdded).getTime() : 0;
          const dateB = b.dateAdded ? new Date(b.dateAdded).getTime() : 0;
          return dateB - dateA;
        } catch (e) {
          return 0;
        }
      case "담은 순":
      default:
        try {
          const dateA = a.dateAdded ? new Date(a.dateAdded).getTime() : 0;
          const dateB = b.dateAdded ? new Date(b.dateAdded).getTime() : 0;
          return dateA - dateB;
        } catch (e) {
          return 0;
        }
    }
  });

  if (isLoading) {
    return (
      <div className={styles.pageWrapper}>
        <Header_2 />
        <div className={styles.loadingMessage}>
          보는 중인 작품 목록을 불러오는 중...
        </div>
      </div>
    );
  }

  return (
    <div className={styles.pageWrapper}>
      <Header_2 />
      <div className={styles.contentWrapper}>
        <h2 className={styles.title}>
          보는 중인 영화 ({watchingMovies.length})
        </h2>
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
        {watchingMovies.length === 0 ? (
          <div className={styles.emptyStateContainer}>
            <NoMoviesIcon
              style={{ width: "62px", height: "62px", marginBottom: "20px" }}
            />
            <p className={styles.emptyStateMessage}>보는 중인 작품이 없어요.</p>
          </div>
        ) : (
          <div className={styles.movieGrid}>
            {sortedMovies.map((m) => (
              <MovieWatchCard
                key={m.id}
                id={m.id}
                poster={m.poster}
                title={m.title}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default WatchingPage;
