import React, { useState, useEffect } from "react";
import { ReactComponent as HeartIcon } from "../../../../assets/icons/HeartIcon.svg";
import { ReactComponent as UserIcon } from "../../../../assets/icons/UserIcon.svg";
import { ReactComponent as StarIcon } from "../../../../assets/icons/StarIcon.svg";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import { ratingLikeAPI, getToken } from "../../../../utils/api";
import "./CommentCard.css";

export default function CommentCard({ comment }) {
  const [showSpoiler, setShowSpoiler] = useState(false);
  const [isLiked, setIsLiked] = useState(comment?.isLiked ?? false);
  const [likesCount, setLikesCount] = useState(comment?.likes ?? 0);

  const profilePhoto = comment.photo;

  React.useEffect(() => {
    setIsLiked(comment?.isLiked ?? false);
    setLikesCount(comment?.likes ?? 0);
  }, [comment?.isLiked, comment?.likes]);

  if (!comment) {
    return null;
  }

  const isSpoiler = comment.spoiler === true;

  const handleLikeClick = async () => {
    if (!getToken()) {
      alert("로그인이 필요합니다.");
      return;
    }
    try {
      await ratingLikeAPI.toggleLike(comment.id);
      const wasLiked = isLiked;
      setIsLiked(!wasLiked);
      setLikesCount((prev) => (wasLiked ? Math.max(0, prev - 1) : prev + 1));
    } catch (error) {
      alert(
        "좋아요 처리에 실패했습니다: " + (error.message || "알 수 없는 오류")
      );
    }
  };

  return (
    <div className="detail-comment-card">
      {/* 상단 유저/별점 */}
      <div className="detail-comment-card__header">
        <div className="detail-comment-card__user">
          {/* 프로필 */}
          <ImageWithFallback
            src={comment.photo}
            alt={comment.user}
            className="detail-comment-card__avatar"
            width="40px"
            height="40px"
            placeholder=""
            fallbackIcon={UserIcon}
            style={{ borderRadius: "50%" }}
          />
          <div className="detail-comment-card__username">
            {comment.user || "익명"}
          </div>
        </div>

        {/* 별점 */}
        <div className="detail-comment-card__rating">
          <StarIcon className="detail-comment-card__rating-icon" />
          {typeof comment.rating === "number"
            ? comment.rating.toFixed(1)
            : "0.0"}
        </div>
      </div>

      {/* 본문 */}
      <div className="detail-comment-card__body">
        {isSpoiler && !showSpoiler ? (
          <div className="detail-comment-card__spoiler-warning">
            <p className="detail-comment-card__spoiler-text">
              스포일러가 포함되어있어요!
            </p>
            <button
              type="button"
              onClick={() => setShowSpoiler(true)}
              className="detail-comment-card__spoiler-button"
            >
              코멘트 보기
            </button>
          </div>
        ) : (
          <div>{comment.text || "코멘트 내용이 없습니다."}</div>
        )}
      </div>

      {/* 하단 */}
      <div className="detail-comment-card__footer">
        <button
          type="button"
          onClick={handleLikeClick}
          className={`detail-comment-card__like-button ${
            isLiked ? "is-active" : ""
          }`}
        >
          <HeartIcon
            className="detail-comment-card__heart-icon"
            style={{ fill: isLiked ? "#678d77" : "#A0A0A0" }}
          />
        </button>
        <span>{likesCount}</span>
      </div>
    </div>
  );
}
