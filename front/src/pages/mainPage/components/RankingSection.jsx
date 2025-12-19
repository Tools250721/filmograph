import React, { useRef, useState, useEffect } from "react";
import RankingCard from "./ui/RankingCard";
import { SectionSkeleton } from "../../../components/ui/Skeleton";
import styles from "./RankingSection.module.css";

const RankingSection = ({ sectionTitle, movies }) => {
  const scrollRef = useRef(null);

  const cardWidth = 205;
  const gap = 16;
  const visibleCount = 6;
  const totalWidth = cardWidth * visibleCount + gap * (visibleCount - 1);
  const scrollByAmount = cardWidth + gap;

  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);

  const checkForScrollPosition = () => {
    if (!scrollRef.current) return;
    const { scrollLeft, scrollWidth, clientWidth } = scrollRef.current;
    setCanScrollLeft(scrollLeft > 0);
    setCanScrollRight(scrollLeft + clientWidth < scrollWidth - 1);
  };

  const scrollLeft = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: -scrollByAmount, behavior: "smooth" });
    }
  };

  const scrollRight = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollBy({ left: scrollByAmount, behavior: "smooth" });
    }
  };

  useEffect(() => {
    checkForScrollPosition();
  }, [movies]);

  const safeMovies = Array.isArray(movies) ? movies : [];

  // 데이터가 없어도 섹션은 표시 (로딩 중일 수 있음)
  if (safeMovies.length === 0) {
    return (
      <div className={styles.sectionContainer}>
        <h3 className={styles.title}>{sectionTitle}</h3>
        <SectionSkeleton count={6} cardWidth={205} cardHeight={290} />
      </div>
    );
  }

  return (
    <div
      className={styles.sectionContainer}
      style={{ "--total-width": `${totalWidth}px` }}
    >
      <h3 className={styles.title}>{sectionTitle}</h3>

      <div
        ref={scrollRef}
        onScroll={checkForScrollPosition}
        className={styles.scrollContainer}
        style={{
          "--total-width": `${totalWidth}px`,
          "--gap": `${gap}px`,
        }}
      >
        {safeMovies.map((movie, idx) => (
          <div key={movie.id || idx} className={styles.cardWrapper}>
            <RankingCard {...movie} />
          </div>
        ))}
      </div>

      {/* 왼쪽 버튼 */}
      {canScrollLeft && (
        <button
          type="button"
          onClick={scrollLeft}
          className={`${styles.navButton} ${styles.navButtonLeft}`}
          aria-label="이전 항목 보기"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path
              d="M9 5l7 7-7 7"
              color="#7e7e7e"
              stroke="currentColor"
              strokeWidth="1"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
      )}

      {/* 오른쪽 버튼 */}
      {canScrollRight && (
        <button
          type="button"
          onClick={scrollRight}
          className={`${styles.navButton} ${styles.navButtonRight}`}
          aria-label="다음 항목 보기"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
            <path
              d="M9 5l7 7-7 7"
              color="#7e7e7e"
              stroke="currentColor"
              strokeWidth="1"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </button>
      )}
    </div>
  );
};

export default RankingSection;
