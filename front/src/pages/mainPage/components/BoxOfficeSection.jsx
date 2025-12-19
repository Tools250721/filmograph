import React, { useRef, useState, useEffect } from "react";
import BoxOfficeCard from "./ui/BoxOfficeCard";
import { SectionSkeleton } from "../../../components/ui/Skeleton";
import styles from "./BoxOfficeSection.module.css";

const BoxOfficeSection = ({ movies }) => {
  // movies가 배열이 아니거나 없으면 빈 배열로 처리 (최상단에서 처리)
  const safeMovies = Array.isArray(movies) ? movies : [];

  const scrollRef = useRef(null);
  const autoScrollIntervalRef = useRef(null);
  const isUserScrollingRef = useRef(false);

  // 디버깅: movies 타입 확인
  useEffect(() => {
    if (movies !== undefined && !Array.isArray(movies)) {
      console.warn(
        "BoxOfficeSection: movies가 배열이 아닙니다:",
        typeof movies,
        movies
      );
    }
  }, [movies]);

  const cardWidth = 250;
  const gap = 15;
  const visibleCount = 5;
  const totalWidth = cardWidth * visibleCount + gap * (visibleCount - 1);
  const scrollByAmount = cardWidth + gap;

  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);

  const checkForScrollPosition = () => {
    if (!scrollRef.current) return;
    const { scrollLeft, scrollWidth, clientWidth } = scrollRef.current;
    setCanScrollLeft(scrollLeft > 0);
    setCanScrollRight(scrollLeft + clientWidth < scrollWidth - 1);
  };

  const scrollLeft = () => {
    if (scrollRef.current) {
      isUserScrollingRef.current = true;
      scrollRef.current.scrollBy({ left: -scrollByAmount, behavior: "smooth" });
      setTimeout(() => {
        isUserScrollingRef.current = false;
      }, 1000);
    }
  };

  const scrollRight = () => {
    if (scrollRef.current) {
      isUserScrollingRef.current = true;
      scrollRef.current.scrollBy({ left: scrollByAmount, behavior: "smooth" });
      setTimeout(() => {
        isUserScrollingRef.current = false;
      }, 1000);
    }
  };

  useEffect(() => {
    checkForScrollPosition();
  }, [safeMovies]);

  // 자동 스크롤 설정
  useEffect(() => {
    if (safeMovies.length === 0) return;

    // 자동 스크롤 시작
    const startAutoScroll = () => {
      if (autoScrollIntervalRef.current) {
        clearInterval(autoScrollIntervalRef.current);
      }

      autoScrollIntervalRef.current = setInterval(() => {
        // 사용자가 수동으로 스크롤 중이면 건너뛰기
        if (isUserScrollingRef.current) return;

        if (!scrollRef.current || safeMovies.length === 0) return;

        // 현재 스크롤 위치 확인
        const currentScrollLeft = scrollRef.current.scrollLeft;
        const scrollWidth = scrollRef.current.scrollWidth;
        const clientWidth = scrollRef.current.clientWidth;

        // 마지막에 도달했는지 확인 (약간의 오차 허용)
        const isAtEnd = currentScrollLeft + clientWidth >= scrollWidth - 10;

        // 마지막에 도달했으면 1위로 점프
        if (isAtEnd) {
          scrollRef.current.scrollTo({
            left: 0,
            behavior: "auto", // 점프 (애니메이션 없음)
          });
          setCurrentIndex(0);
        } else {
          // 다음 영화로 스크롤
          const currentCalculatedIndex = Math.round(
            currentScrollLeft / scrollByAmount
          );
          const currentIdx = Math.max(
            0,
            Math.min(currentCalculatedIndex, safeMovies.length - 1)
          );
          const nextIndex = currentIdx + 1;
          const targetScrollLeft = nextIndex * scrollByAmount;

          scrollRef.current.scrollTo({
            left: targetScrollLeft,
            behavior: "smooth",
          });
          setCurrentIndex(nextIndex);
        }
      }, 3000); // 3초마다 자동 스크롤
    };

    // 초기 지연 후 자동 스크롤 시작
    const initialDelay = setTimeout(() => {
      startAutoScroll();
    }, 3000); // 첫 번째 자동 스크롤은 3초 후 시작

    // 컴포넌트 언마운트 시 정리
    return () => {
      clearTimeout(initialDelay);
      if (autoScrollIntervalRef.current) {
        clearInterval(autoScrollIntervalRef.current);
      }
    };
  }, [safeMovies.length, scrollByAmount]);

  // 사용자 스크롤 감지 및 인덱스 업데이트
  useEffect(() => {
    const scrollContainer = scrollRef.current;
    if (!scrollContainer) return;

    let scrollTimeout;

    const handleScroll = () => {
      if (!scrollContainer) return;

      const { scrollLeft } = scrollContainer;

      // 현재 인덱스 계산
      const calculatedIndex = Math.round(scrollLeft / scrollByAmount);
      const clampedIndex = Math.max(
        0,
        Math.min(calculatedIndex, safeMovies.length - 1)
      );

      // 인덱스가 실제로 변경되었을 때만 업데이트
      setCurrentIndex((prevIndex) => {
        if (prevIndex !== clampedIndex) {
          return clampedIndex;
        }
        return prevIndex;
      });

      checkForScrollPosition();

      // 사용자가 스크롤 중임을 표시
      isUserScrollingRef.current = true;
      clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(() => {
        isUserScrollingRef.current = false;
      }, 1500); // 1.5초 후 자동 스크롤 재개
    };

    scrollContainer.addEventListener("scroll", handleScroll, { passive: true });
    return () => {
      clearTimeout(scrollTimeout);
      scrollContainer.removeEventListener("scroll", handleScroll);
    };
  }, [safeMovies.length, scrollByAmount]);

  if (!safeMovies || safeMovies.length === 0) {
    return (
      <div className={styles.sectionContainer}>
        <h3 className={styles.title}>박스오피스 순위</h3>
        <SectionSkeleton count={5} cardWidth={250} cardHeight={360} />
      </div>
    );
  }

  return (
    <div
      className={styles.sectionContainer}
      style={{ "--total-width": `${totalWidth}px` }}
    >
      <h3 className={styles.title}>박스오피스 순위</h3>

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
          <div
            key={movie.id || movie.rank || idx}
            className={styles.cardWrapper}
          >
            <BoxOfficeCard {...movie} />
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

export default BoxOfficeSection;
