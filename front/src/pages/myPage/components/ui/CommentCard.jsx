import React, { useState, useEffect } from "react";
import { ReactComponent as UserIcon } from "../../../../assets/icons/UserIcon.svg";
import { ReactComponent as StarIcon } from "../../../../assets/icons/StarIcon.svg";
import { ReactComponent as HeartIcon } from "../../../../assets/icons/HeartIcon.svg";
import { ReactComponent as EditIcon } from "../../../../assets/icons/EditIcon.svg";
import { ReactComponent as DeleteIcon } from "../../../../assets/icons/DeleteIcon.svg";
import { useNavigate } from "react-router-dom";
import CommentDelete from "../CommentDelete";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import { ratingLikeAPI, getToken } from "../../../../utils/api";
import styles from "./CommentCard.module.css";

const CommentCard = ({
  id, // Rating ID
  movieId,
  commentId, // 호환성을 위해 유지
  userName,
  movieTitle,
  year,
  avgRating,
  userRating,
  poster,
  content,
  likes: initialLikes = 0,
  isLiked: initialIsLiked = false,
  spoiler = false,
  onEdit,
  onDelete,
}) => {
  const navigate = useNavigate();
  const [liked, setLiked] = useState(initialIsLiked);
  const [deletePopupOpen, setDeletePopupOpen] = useState(false);
  const [likesCount, setLikesCount] = useState(initialLikes);
  const [showSpoiler, setShowSpoiler] = useState(false);

  useEffect(() => {
    setLikesCount(initialLikes);
    setLiked(initialIsLiked);
  }, [initialLikes, initialIsLiked]);

  const handlePosterClick = () => {
    navigate(`/movie/${movieId}`);
  };

  const handleUserClick = () => {
    navigate("/my");
  };

  const handleLikeClick = async () => {
    const ratingId = id || commentId; // Rating ID 우선 사용
    if (!ratingId) {
      return;
    }

    if (!getToken()) {
      alert("로그인이 필요합니다.");
      return;
    }

    try {
      await ratingLikeAPI.toggleLike(ratingId);
      const wasLiked = liked;
      setLiked(!wasLiked);
      setLikesCount((prev) => (wasLiked ? Math.max(0, prev - 1) : prev + 1));
    } catch (error) {
      alert(
        "좋아요 처리에 실패했습니다: " + (error.message || "알 수 없는 오류")
      );
    }
  };

  const handleDeleteClick = () => {
    setDeletePopupOpen(true);
  };

  const handleDeleteConfirm = () => {
    setDeletePopupOpen(false);
    if (onDelete) onDelete();
  };

  const handleDeleteCancel = () => {
    setDeletePopupOpen(false);
  };

  const getHeartColor = () => {
    if (liked) return "#678d77"; // 초록계열 색상
    return "#A0A0A0";
  };

  return (
    <>
      <div className={styles.cardContainer}>
        <div className={styles.header}>
          <div className={styles.userInfo} onClick={handleUserClick}>
            <UserIcon className={styles.userIcon} />
            <span className={styles.userName}>{userName}</span>
          </div>
          <span className={styles.userRating}>
            <StarIcon className={styles.starIcon} /> {userRating.toFixed(1)}
          </span>
        </div>

        <div className={styles.divider} />

        <div className={styles.movieInfo} onClick={handlePosterClick}>
          <ImageWithFallback
            src={poster}
            alt={movieTitle}
            className={styles.poster}
            placeholder="이미지 없음"
          />
          <div className={styles.movieText}>
            <p className={styles.movieTitle}>{movieTitle}</p>
            <p className={styles.movieMeta}>영화 · {year}</p>
            <p className={styles.movieAvgRating}>평균 {avgRating.toFixed(1)}</p>
          </div>
        </div>

        {spoiler && !showSpoiler ? (
          <div className={styles.spoilerWarning}>
            <p className={styles.spoilerText}>스포일러가 포함되어있어요!</p>
            <button
              type="button"
              onClick={() => setShowSpoiler(true)}
              className={styles.spoilerButton}
            >
              코멘트 보기
            </button>
          </div>
        ) : (
          <p className={styles.content}>{content}</p>
        )}

        <div className={styles.footer}>
          <div className={styles.iconGroupLeft}>
            <button
              type="button"
              onClick={handleLikeClick}
              className={`${styles.likeButton} ${liked ? styles.isLiked : ""}`}
            >
              <HeartIcon
                className={styles.heartIcon}
                style={{ fill: getHeartColor() }}
              />
            </button>
            <span className={styles.likesCount}>{likesCount}</span>
          </div>
          <div className={styles.iconGroupRight}>
            <EditIcon className={styles.editIcon} onClick={onEdit} />
            <DeleteIcon
              className={styles.deleteIcon}
              onClick={handleDeleteClick}
            />
          </div>
        </div>
      </div>

      <CommentDelete
        isOpen={deletePopupOpen}
        onClose={handleDeleteCancel}
        onConfirm={handleDeleteConfirm}
      />
    </>
  );
};

export default CommentCard;
