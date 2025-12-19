import { useState, useEffect } from "react";
import { ReactComponent as EyeIcon } from "../../../../assets/icons/EyeIcon.svg";
import { ReactComponent as PlusIcon } from "../../../../assets/icons/PlusIcon.svg";
import { ReactComponent as PlusIconSelected } from "../../../../assets/icons/PlusIconSelected.svg";
import "./ActionButtons.css";

export default function ActionButtons({
  onToggleWishlist,
  onToggleWatching,
  isLoggedIn = false,
  isWishlisted: propIsWishlisted = false,
  isWatching: propIsWatching = false,
  onAuthRequired,
}) {
  const [isWishlisted, setIsWishlisted] = useState(propIsWishlisted);
  const [isWatching, setIsWatching] = useState(propIsWatching);

  // props 변경 시 state 업데이트
  useEffect(() => {
    setIsWishlisted(propIsWishlisted);
  }, [propIsWishlisted]);

  useEffect(() => {
    setIsWatching(propIsWatching);
  }, [propIsWatching]);

  const handleWishlist = async () => {
    if (!isLoggedIn) {
      onAuthRequired?.();
      return;
    }

    try {
      await onToggleWishlist?.();
    } catch (error) {
      // 에러는 부모 컴포넌트에서 처리
    }
  };

  const handleWatching = async () => {
    if (!isLoggedIn) {
      onAuthRequired?.();
      return;
    }

    try {
      await onToggleWatching?.();
    } catch (error) {
      // 에러는 부모 컴포넌트에서 처리
    }
  };

  return (
    <div className="action-buttons">
      <button
        type="button"
        title="위시리스트"
        aria-label="위시리스트"
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          handleWishlist();
        }}
        className={`action-buttons__button ${isWishlisted ? "is-active" : ""}`}
      >
        <span className="action-buttons__icon">
          {isWishlisted ? (
            <PlusIconSelected className="action-buttons__icon-svg" />
          ) : (
            <PlusIcon className="action-buttons__icon-svg" />
          )}
        </span>
        <span className="action-buttons__label">위시리스트</span>
      </button>

      <button
        type="button"
        title="보는중"
        aria-label="보는중"
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation();
          handleWatching();
        }}
        className={`action-buttons__button ${isWatching ? "is-active" : ""}`}
      >
        <span className="action-buttons__icon">
          <EyeIcon
            className={`action-buttons__icon-svg action-buttons__icon-svg--eye ${
              isWatching ? "is-active" : ""
            }`}
          />
        </span>
        <span className="action-buttons__label">보는중</span>
      </button>
    </div>
  );
}
