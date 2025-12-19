import { ReactComponent as StarIcon } from "../../../../assets/icons/StarIcon.svg";
import { ReactComponent as EditIcon } from "../../../../assets/icons/EditIcon.svg";
import { ReactComponent as DeleteIcon } from "../../../../assets/icons/DeleteIcon.svg";
import "./UserCommentCard.css";

export default function UserCommentCard({
  myRating,
  myComment,
  onEdit,
  onDelete,
}) {
  if (!myComment) return null;
  return (
    <div className="user-comment-card">
      {/* 헤더 */}
      <div className="user-comment-card__header">
        <span className="user-comment-card__label">내 코멘트</span>
        <span className="user-comment-card__rating">
          <StarIcon className="user-comment-card__rating-icon" />
          {(myRating / 2).toFixed(1)}
        </span>
      </div>

      {/* 구분선 */}
      <div className="user-comment-card__divider" />

      {/* 본문 */}
      <p className="user-comment-card__body">{myComment}</p>

      {/* 수정/삭제 버튼 */}
      <div className="user-comment-card__actions">
        <button
          type="button"
          onClick={onEdit}
          className="user-comment-card__action-button"
        >
          <EditIcon /> 수정하기
        </button>
        <button
          type="button"
          onClick={onDelete}
          className="user-comment-card__action-button"
        >
          <DeleteIcon /> 삭제하기
        </button>
      </div>
    </div>
  );
}
