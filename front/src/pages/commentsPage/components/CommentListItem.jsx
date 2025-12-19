import { useState } from "react";
import { ReactComponent as UserIcon } from "../../../assets/icons/UserIcon.svg";
import { ReactComponent as StarIcon } from "../../../assets/icons/StarIcon.svg";
import { ReactComponent as HeartIcon } from "../../../assets/icons/HeartIcon.svg";
import { ReactComponent as EditIcon } from "../../../assets/icons/EditIcon.svg";
import { ReactComponent as DeleteIcon } from "../../../assets/icons/DeleteIcon.svg";
import { useUser } from "../../../contexts/UserContext";
import "./CommentListItem.css";

export default function CommentListItem({
  comment,
  isLiked,
  onToggleLike,
  isMine = false,
  onEdit = null,
  onDelete = null,
}) {
  const [showSpoiler, setShowSpoiler] = useState(false);
  const isSpoiler = comment.spoiler === true;
  const { userProfile } = useUser();
  
  // 내 프로필인지 확인 (userName으로 비교)
  const isMyComment = userProfile?.name && comment?.user && userProfile.name === comment.user;
  // 프로필 사진 결정: 내 코멘트이고 Context에 프로필 사진이 있으면 Context 사용, 아니면 comment.photo 사용
  const profilePhoto = isMyComment && userProfile?.profileImage 
    ? (userProfile.profileImage.startsWith("http") 
        ? userProfile.profileImage 
        : `${process.env.REACT_APP_API_URL || "http://localhost:8080"}${userProfile.profileImage}`)
    : comment.photo;

  return (
    <div className="comment-list-item">
      {/* 상단 */}
      <div className="comment-list-item__header">
        {/* 유저 */}
        <div className="comment-list-item__user">
          {profilePhoto ? (
            <img
              src={profilePhoto}
              alt={comment.user}
              className="comment-list-item__avatar"
            />
          ) : (
            <UserIcon className="comment-list-item__avatar-icon" />
          )}
          <span className="comment-list-item__username">{comment.user}</span>
        </div>

        {/* 별점 */}
        <div className="comment-list-item__rating">
          <StarIcon className="comment-list-item__rating-icon" />
          <span className="comment-list-item__rating-value">
            {comment.rating.toFixed(1)}
          </span>
        </div>
      </div>

      {/* 본문 */}
      {isSpoiler && !showSpoiler ? (
        <div className="comment-list-item__spoiler-warning">
          <p className="comment-list-item__spoiler-text">
            스포일러가 포함되어있어요!
          </p>
          <button
            type="button"
            onClick={() => setShowSpoiler(true)}
            className="comment-list-item__spoiler-button"
          >
            코멘트 보기
          </button>
        </div>
      ) : (
        <p className="comment-list-item__text">{comment.text}</p>
      )}

      {/* 하단 영역 */}
      <div className="comment-list-item__meta">
        <span>좋아요 {comment.likes}</span>
      </div>

      {/* 버튼 */}
      <div className="comment-list-item__actions">
        <button
          onClick={onToggleLike}
          className={`comment-list-item__like-button ${
            isLiked ? "is-active" : ""
          }`}
        >
          <HeartIcon className="comment-list-item__like-icon" />
        </button>
        {isMine && (
          <>
            {onEdit && (
              <button
                onClick={onEdit}
                className="comment-list-item__edit-button"
                title="편집"
              >
                <EditIcon className="comment-list-item__edit-icon" />
              </button>
            )}
            {onDelete && (
              <button
                onClick={onDelete}
                className="comment-list-item__delete-button"
                title="삭제"
              >
                <DeleteIcon className="comment-list-item__delete-icon" />
              </button>
            )}
          </>
        )}
      </div>
    </div>
  );
}
