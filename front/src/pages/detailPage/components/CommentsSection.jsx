import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import CommentCard from "./ui/CommentCard";
import "./CommentsSection.css";

export default function CommentsSection({ comments = [], movieId = null }) {
  const navigate = useNavigate();
  const [visibleCount, setVisibleCount] = useState(8);

  useEffect(() => {
    if (typeof window === "undefined") {
      return undefined;
    }

    const updateVisibleCount = () => {
      const width = window.innerWidth;
      let count = 8;

      if (width < 640) {
        count = 3;
      } else if (width < 992) {
        count = 4;
      } else if (width < 1200) {
        count = 6;
      }

      setVisibleCount(count);
    };

    updateVisibleCount();
    window.addEventListener("resize", updateVisibleCount);
    return () => window.removeEventListener("resize", updateVisibleCount);
  }, []);

  if (!Array.isArray(comments) || comments.length === 0) {
    return (
      <section className="detail-comments">
        <div className="detail-comments__title">코멘트</div>
        <p className="detail-comments__empty">아직 작성된 코멘트가 없습니다.</p>
      </section>
    );
  }

  const recentComments = comments.slice(0, visibleCount);

  return (
    <section className="detail-comments">
      {/* 타이틀 + 더보기 */}
      <div className="detail-comments__header">
        <div className="detail-comments__title">코멘트</div>
        <button
          type="button"
          onClick={() => {
            const targetMovieId = movieId;
            if (targetMovieId && !isNaN(targetMovieId)) {
              navigate(`/comments?movieId=${targetMovieId}`);
            } else {
              navigate("/comments");
            }
          }}
          className="detail-comments__more"
        >
          더보기
        </button>
      </div>

      {/* 코멘트 그리드 */}
      <div className="detail-comments__grid">
        {recentComments.map((comment) => (
          <CommentCard key={comment.id} comment={comment} />
        ))}
      </div>
    </section>
  );
}
