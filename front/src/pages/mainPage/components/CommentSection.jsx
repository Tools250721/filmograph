import React, { useRef, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import CommentCard from "./ui/CommentCard";
import { Skeleton } from "../../../components/ui/Skeleton";
import { useUser } from "../../../contexts/UserContext";
import styles from "./CommentSection.module.css";

const CommentSection = ({ comments, isLoggedIn: propIsLoggedIn }) => {
  const navigate = useNavigate();
  const scrollRef = useRef(null);
  const { userProfile } = useUser();

  const cardWidth = 650;
  const gap = 20;
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

  // 스크롤 및 리사이즈 이벤트 감지
  useEffect(() => {
    const currentRef = scrollRef.current;
    if (currentRef) {
      checkForScrollPosition();
      currentRef.addEventListener("scroll", checkForScrollPosition);
      window.addEventListener("resize", checkForScrollPosition);

      return () => {
        currentRef.removeEventListener("scroll", checkForScrollPosition);
        window.removeEventListener("resize", checkForScrollPosition);
      };
    }
  }, [comments]);

  // 로그인 상태 확인 (props가 없으면 직접 확인)
  const [isLoggedIn, setIsLoggedIn] = React.useState(propIsLoggedIn ?? false);
  const [isLoading, setIsLoading] = React.useState(
    propIsLoggedIn === undefined
  );

  React.useEffect(() => {
    if (propIsLoggedIn !== undefined) {
      setIsLoggedIn(propIsLoggedIn);
      setIsLoading(false);
    } else {
      const checkAuth = async () => {
        try {
          const { getToken } = await import("../../../../utils/api");
          setIsLoggedIn(!!getToken());
        } catch (error) {
          setIsLoggedIn(false);
        } finally {
          setIsLoading(false);
        }
      };
      checkAuth();
    }
  }, [propIsLoggedIn]);

  const userName = userProfile?.name || "사용자";

  if (isLoading) {
    return (
      <div className={styles.sectionContainer}>
        <h3 className={styles.title}>{userName}님의 코멘트</h3>
        <div className={styles.scrollBox}>
          <div className={styles.scrollContainer}>
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className={styles.cardWrapper}>
                <Skeleton width="650px" height="200px" borderRadius="8px" />
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (!isLoggedIn) {
    return (
      <div className={styles.sectionContainer}>
        <h3 className={styles.title}>{userName}님의 코멘트</h3>
        <div style={{ padding: "20px", textAlign: "center", color: "#999" }}>
          로그인 후 코멘트를 확인할 수 있습니다.
        </div>
      </div>
    );
  }

  if (!comments || comments.length === 0) {
    return (
      <div className={styles.sectionContainer}>
        <h3 className={styles.title}>{userName}님의 코멘트</h3>
        <div style={{ padding: "20px", textAlign: "center", color: "#999" }}>
          코멘트를 남겨보세요!
        </div>
      </div>
    );
  }

  return (
    <div className={styles.sectionContainer}>
      <div className={styles.titleHeader}>
        <h3 className={styles.title}>{userName}님의 코멘트</h3>
        <button
          onClick={() => navigate("/commented")}
          className={styles.viewAllButton}
        >
          모두 보기 〉
        </button>
      </div>

      <div className={styles.scrollBox} style={{ boxSizing: "border-box" }}>
        <div
          ref={scrollRef}
          className={styles.scrollContainer}
          style={{ "--gap": `${gap}px` }}
        >
          {comments &&
            Array.isArray(comments) &&
            comments.map((comment, i) => (
              <div key={comment.id || i} className={styles.cardWrapper}>
                <CommentCard {...comment} />
              </div>
            ))}
        </div>
      </div>

      {/* 왼쪽/오른쪽 버튼 */}
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

export default CommentSection;
