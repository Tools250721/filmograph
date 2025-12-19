import React, { useState, useEffect } from "react";
import Header from "../layouts/Header_2";
import CommentCard from "./components/ui/CommentCard";
import { ReactComponent as NoMoviesIcon } from "../../assets/icons/NoMoviesIcon.svg";
import { getMyComments } from "../../data/myPageAPI";
import RatingModal from "../common/RatingModal";
import styles from "./CommentedPage.module.css";

const CommentedPage = () => {
  const [selected, setSelected] = useState("작성 순");
  const [open, setOpen] = useState(false);
  const [comments, setComments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingComment, setEditingComment] = useState(null);

  useEffect(() => {
    const loadComments = async () => {
      try {
        setIsLoading(true);
        const data = await getMyComments();
        const processedData = data.map((c) => ({
          ...c,
          userRating: c.userRating ?? 0,
          content: c.content ?? "",
          date: c.date ?? new Date(0).toISOString(),
          likes: c.likes ?? 0,
          spoiler: c.spoiler ?? false,
        }));
        setComments(processedData);
      } catch (error) {
        setComments([]);
      } finally {
        setIsLoading(false);
      }
    };
    loadComments();
  }, []);

  const options = [
    "작성 순",
    "작성 역순",
    "나의 별점 높은 순",
    "나의 별점 낮은 순",
    "가나다 순",
    "가나다 역순",
  ];

  const sortedComments = [...comments].sort((a, b) => {
    switch (selected) {
      case "작성 역순":
        try {
          return new Date(a.date) - new Date(b.date);
        } catch (e) {
          return 0;
        }
      case "나의 별점 높은 순":
        return b.userRating - a.userRating;
      case "나의 별점 낮은 순":
        return a.userRating - b.userRating;
      case "가나다 순":
        return (a.movieTitle || "").localeCompare(b.movieTitle || "");
      case "가나다 역순":
        return (b.movieTitle || "").localeCompare(a.movieTitle || "");
      case "작성 순":
      default:
        try {
          return new Date(b.date) - new Date(a.date);
        } catch (e) {
          return 0;
        }
    }
  });

  const handleEdit = (comment) => {
    setEditingComment(comment);
    setIsModalOpen(true);
  };

  const handleDelete = async (comment) => {
    if (!window.confirm("정말로 이 코멘트를 삭제하시겠습니까?")) {
      return;
    }

    try {
      const { ratingAPI } = await import("../../utils/api");
      const movieId = comment.movieId;

      await ratingAPI.deleteRating(movieId);

      // 로컬 상태 업데이트
      setComments((prevComments) =>
        prevComments.filter((c) => c.id !== comment.id)
      );
    } catch (error) {
      alert(
        "코멘트 삭제에 실패했습니다: " + (error.message || "알 수 없는 오류")
      );
    }
  };

  const handleUpdateComment = async ({ rating, comment, spoiler }) => {
    if (!editingComment) return;

    try {
      const { ratingAPI } = await import("../../utils/api");
      const movieId = editingComment.movieId;
      const stars = Math.max(1, Math.min(5, Math.round(rating / 2)));

      await ratingAPI.upsertRating(
        movieId,
        stars,
        comment || null,
        spoiler || false
      );

      // 로컬 상태 업데이트
      setComments((prevComments) =>
        prevComments.map((c) =>
          c.id === editingComment.id
            ? {
                ...c,
                userRating: rating / 2,
                content: comment || "",
                spoiler: spoiler || false,
              }
            : c
        )
      );

      setIsModalOpen(false);
      setEditingComment(null);
    } catch (error) {
      alert(
        "코멘트 업데이트에 실패했습니다: " +
          (error.message || "알 수 없는 오류")
      );
    }
  };

  return (
    <div className={styles.pageContainer}>
      <Header />

      <div className={styles.contentArea}>
        <h2 className={styles.title}>코멘트</h2>

        {/* 구분선 */}
        <hr className={styles.divider} />

        {/* 드롭다운 */}
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
              <div className={styles.dropdownMenu}>
                {options.map((opt, idx) => (
                  <div
                    key={idx}
                    onClick={() => {
                      setSelected(opt);
                      setOpen(false);
                    }}
                    className={styles.dropdownOption}
                  >
                    {opt}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {isLoading ? (
          <div className={styles.noCommentsContainer}>
            <p className={styles.noCommentsText}>로딩 중...</p>
          </div>
        ) : sortedComments.length === 0 ? (
          <div className={styles.noCommentsContainer}>
            <NoMoviesIcon className={styles.noCommentsIcon} />
            <p className={styles.noCommentsText}>
              아직 작성한 코멘트가 없어요.
            </p>
          </div>
        ) : (
          <div className={styles.commentsList}>
            {sortedComments.map((c) => (
              <CommentCard
                key={c.id || c.movieId}
                {...c}
                onEdit={() => handleEdit(c)}
                onDelete={() => handleDelete(c)}
              />
            ))}
          </div>
        )}
      </div>

      {/* RatingModal */}
      {editingComment && (
        <RatingModal
          isOpen={isModalOpen}
          onClose={() => {
            setIsModalOpen(false);
            setEditingComment(null);
          }}
          onSubmit={handleUpdateComment}
          movieTitle={editingComment.movieTitle}
          initialRating={editingComment.userRating * 2}
          initialComment={editingComment.content}
          initialSpoiler={editingComment.spoiler || false}
          startStep="comment"
        />
      )}
    </div>
  );
};

export default CommentedPage;
