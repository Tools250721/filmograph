import React, { useState, useEffect } from "react";
import Header from "../layouts/Header_2";
import CommentCard from "./components/CommentCard";
import { ReactComponent as NoMoviesIcon } from "../../assets/icons/BookmarkIcon.svg";
import { getMyComments } from "../../data/myPageAPI";
import RatingModal from "../common/RatingModal";
import SortDropdown from "../commentsPage/components/SortDropdown";

const MyCommentsPage = () => {
  const [myComments, setMyComments] = useState([]);
  const [sortOption, setSortOption] = useState("date");

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingComment, setEditingComment] = useState(null);

  useEffect(() => {
    const loadComments = async () => {
      try {
        const data = await getMyComments();
        const processedData = data.map((c) => ({
          ...c,
          userRating: c.userRating ?? 0,
          content: c.content ?? "",
          date: c.date ?? new Date(0).toISOString(),
          likes: c.likes ?? 0,
        }));
        setMyComments(processedData);
      } catch (error) {
        setMyComments([]);
      }
    };
    loadComments();
  }, []);

  const sortedComments = [...myComments].sort((a, b) => {
    switch (sortOption) {
      case "like":
        return (b.likes ?? 0) - (a.likes ?? 0);
      case "highRating":
        return b.userRating - a.userRating;
      case "lowRating":
        return a.userRating - b.userRating;
      case "date":
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
      setMyComments((prevComments) =>
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

      await ratingAPI.upsertRating(movieId, stars, comment || null);

      // 로컬 상태 업데이트
      setMyComments((prevComments) =>
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
    <div
      style={{
        backgroundColor: "#ffffff",
        minHeight: "100vh",
        padding: "40px 360px",
        boxSizing: "border-box",
      }}
    >
      <Header />
      <div style={{ padding: "40px" }}>
        <h2 style={{ marginBottom: "20px", fontSize: "22px" }}>코멘트</h2>
        <hr
          style={{
            border: "none",
            borderTop: "2px solid #141414",
            margin: "40px 0 20px",
          }}
        />

        {/* 정렬 드롭다운 */}
        <div
          style={{
            display: "flex",
            justifyContent: "flex-end",
            marginBottom: "20px",
          }}
        >
          <SortDropdown sortOption={sortOption} setSortOption={setSortOption} />
        </div>

        {myComments.length === 0 ? (
          <div
            style={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              justifyContent: "center",
              marginTop: "100px",
              color: "#D4D4D4",
            }}
          >
            <NoMoviesIcon
              style={{ width: "62px", height: "62px", marginBottom: "20px" }}
            />
            <p style={{ fontSize: "14px", color: "#A0A0A0", marginTop: "0px" }}>
              아직 작성한 코멘트가 없어요.
            </p>
          </div>
        ) : (
          <div
            style={{ display: "flex", flexDirection: "column", gap: "20px" }}
          >
            {sortedComments.map((c) => (
              <CommentCard
                key={c.id}
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

export default MyCommentsPage;
