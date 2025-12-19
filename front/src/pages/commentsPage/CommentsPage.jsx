import React, { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import Header from "../layouts/Header_2";
import CommentListItem from "./components/CommentListItem";
import SortDropdown from "./components/SortDropdown";
import RatingModal from "../common/RatingModal";
import { getAllComments } from "../../data/commentsAPI";
import { getToken, userAPI, ratingAPI, ratingLikeAPI } from "../../utils/api";
import "./CommentsPage.css";

export default function CommentsPage() {
  const [searchParams] = useSearchParams();
  const movieId = searchParams.get("movieId");
  const [allComments, setAllComments] = useState([]);
  const [sortOption, setSortOption] = useState("like");
  const [currentUser, setCurrentUser] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingComment, setEditingComment] = useState(null);

  useEffect(() => {
    const loadCurrentUser = async () => {
      try {
        if (getToken()) {
          const profile = await userAPI.getMyProfile();
          setCurrentUser(profile);
        }
      } catch (error) {
        setCurrentUser(null);
      }
    };
    loadCurrentUser();
  }, []);

  useEffect(() => {
    const loadComments = async () => {
      try {
        const movieIdParam = movieId ? parseInt(movieId, 10) : null;
        const response = await getAllComments(movieIdParam);

        const data = response?.content || response || [];

        const commentsWithLikes = Array.isArray(data)
          ? data.map((c) => ({
              ...c,
              likes: c.likes ?? 0,
              isLiked: c.isLiked ?? false,
            }))
          : [];
        setAllComments(commentsWithLikes);

        // 초기 좋아요 상태 설정
        const initialLikedSet = new Set();
        commentsWithLikes.forEach((c) => {
          if (c.isLiked) {
            initialLikedSet.add(c.id);
          }
        });
        setLikedComments(initialLikedSet);
      } catch (error) {
        setAllComments([]);
      }
    };
    loadComments();
  }, [movieId]);

  const [likedComments, setLikedComments] = useState(new Set());
  const handleToggleLike = async (id) => {
    try {
      await ratingLikeAPI.toggleLike(id);

      // 좋아요 상태 토글
      setLikedComments((prev) => {
        const newSet = new Set(prev);
        if (newSet.has(id)) {
          newSet.delete(id);
        } else {
          newSet.add(id);
        }
        return newSet;
      });

      // 좋아요 카운트 업데이트
      setAllComments((prevComments) =>
        prevComments.map((c) => {
          if (c.id === id) {
            const wasLiked = likedComments.has(id);
            return {
              ...c,
              likes: wasLiked
                ? Math.max(0, (c.likes ?? 0) - 1)
                : (c.likes ?? 0) + 1,
            };
          }
          return c;
        })
      );
    } catch (error) {
      alert(
        "좋아요 처리에 실패했습니다: " + (error.message || "알 수 없는 오류")
      );
    }
  };

  const handleEdit = (comment) => {
    setEditingComment(comment);
    setIsModalOpen(true);
  };

  const handleDelete = async (comment) => {
    if (!window.confirm("정말로 이 코멘트를 삭제하시겠습니까?")) {
      return;
    }

    try {
      await ratingAPI.deleteRating(comment.movieId);
      setAllComments((prevComments) =>
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
      const movieIdParam = editingComment.movieId;
      const stars = Math.max(1.0, Math.min(5.0, rating / 2));

      await ratingAPI.upsertRating(
        movieIdParam,
        stars,
        comment || null,
        spoiler || false
      );

      setAllComments((prevComments) =>
        prevComments.map((c) =>
          c.id === editingComment.id
            ? {
                ...c,
                rating: stars,
                text: comment || "",
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

  const sortedComments = [...allComments].sort((a, b) => {
    switch (sortOption) {
      case "like":
        return (b.likes ?? 0) - (a.likes ?? 0);
      case "highRating":
        return (b.rating ?? 0) - (a.rating ?? 0);
      case "lowRating":
        return (a.rating ?? 0) - (b.rating ?? 0);
      case "date":
        const dateA = a.date ? new Date(a.date) : new Date(0);
        const dateB = b.date ? new Date(b.date) : new Date(0);
        if (isNaN(dateA.getTime())) return 1;
        if (isNaN(dateB.getTime())) return -1;
        return dateB - dateA;
      default:
        return 0;
    }
  });

  return (
    <section className="comments-page">
      {/* 헤더 */}
      <Header />

      <div className="comments-page__inner">
        <div className="comments-page__header-row">
          <h2 className="comments-page__title">코멘트</h2>
        </div>

        {/* 정렬 옵션 드롭다운 */}
        <div className="comments-page__controls">
          <SortDropdown sortOption={sortOption} setSortOption={setSortOption} />
        </div>
      </div>

      {/* 리스트 */}
      <div className="comments-page__list">
        {sortedComments.length === 0 ? (
          <div className="comments-page__empty">
            {allComments.length > 0
              ? "정렬 결과가 없습니다."
              : "가장 먼저 코멘트를 남겨보세요!"}
          </div>
        ) : (
          sortedComments.map((c) => {
            const isMine = currentUser && c.user === currentUser.name;
            return (
              <CommentListItem
                key={c.id}
                comment={c}
                isLiked={likedComments.has(c.id)}
                onToggleLike={() => handleToggleLike(c.id)}
                isMine={isMine}
                onEdit={isMine ? () => handleEdit(c) : null}
                onDelete={isMine ? () => handleDelete(c) : null}
              />
            );
          })
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
          movieTitle={editingComment.movieTitle || "영화"}
          initialRating={editingComment.rating * 2}
          initialComment={editingComment.text}
          initialSpoiler={editingComment.spoiler || false}
          startStep="comment"
        />
      )}
    </section>
  );
}
