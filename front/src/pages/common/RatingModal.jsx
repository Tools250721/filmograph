import React, { useState, useEffect } from "react";
import Modal from "./Modal";
import { ReactComponent as StarIcon } from "../../assets/icons/StarIcon.svg";
import "./RatingModal.css";

export default function RatingModal({
  isOpen,
  onClose,
  onSubmit,
  initialRating = 0,
  initialComment = "",
  initialSpoiler = false,
  startStep = "rating",
  movieTitle = "", // 영화 제목 prop 추가
  movieId = null, // 영화 ID prop 추가
  onAddToWishlist = null, // 위시리스트 추가 콜백
}) {
  const [step, setStep] = useState(startStep);
  const [ratingInt, setRatingInt] = useState(null);
  const [ratingDec, setRatingDec] = useState(null);
  const [comment, setComment] = useState("");
  const [spoiler, setSpoiler] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const stars = [1, 2, 3, 4, 5];

  // 최종 평점
  const ratingValue = (() => {
    if (ratingInt === null) return 0;
    return ratingInt + (ratingDec !== null ? ratingDec / 10 : 0);
  })();

  // 별점 상태 반영
  useEffect(() => {
    if (isOpen) {
      setStep(startStep);

      if (initialRating > 0) {
        const val = initialRating / 2;
        const intPart = Math.floor(val);
        const decPart = Math.round((val - intPart) * 10);

        setRatingInt(intPart);
        setRatingDec(decPart);
      } else {
        setRatingInt(null);
        setRatingDec(null);
      }

      setComment(initialComment);
      setSpoiler(initialSpoiler);
      setErrorMsg("");
    }
  }, [isOpen, startStep, initialRating, initialComment, initialSpoiler]);

  // 평점 (키보드 입력)
  const handleKey = (e) => {
    if (/^[0-9]$/.test(e.key)) {
      const digit = parseInt(e.key, 10);

      if (ratingInt === null) {
        if (digit === 0) return;
        // 5점 이상 평점 제한 (경고)
        if (digit > 5) {
          setErrorMsg("5점 이상의 평점을 줄 수 없어요");
          setTimeout(() => setErrorMsg(""), 1000);
          return;
        }
        setRatingInt(digit);
        setRatingDec(null);
      } else if (ratingDec === null) {
        if (ratingInt === 5 && digit > 0) {
          setErrorMsg("5점 이상의 평점을 줄 수 없어요");
          setTimeout(() => setErrorMsg(""), 1000);
          return;
        }
        setRatingDec(digit);
      }
    } else if (e.key === "Backspace") {
      if (ratingDec !== null) {
        setRatingDec(null);
      } else if (ratingInt !== null) {
        setRatingInt(null);
      }
    }
  };

  // 평점 -> 코멘트
  const handleNext = () => {
    if (step === "rating") {
      setStep("comment");
    } else {
      onSubmit?.({
        rating: ratingValue * 2,
        comment,
        spoiler,
      });
      onClose();
    }
  };

  // 평가 건너뛰기
  const handleSkip = () => {
    onSubmit?.({
      rating: ratingValue * 2,
      comment: "",
      spoiler: false,
    });
    onClose();
  };

  // 위시리스트 추가
  const handleAddToWishlist = async () => {
    if (!movieId || !onAddToWishlist) {
      return;
    }
    try {
      await onAddToWishlist();
      // 성공 피드백은 부모 컴포넌트에서 처리
    } catch (error) {
      alert(
        "위시리스트 추가에 실패했습니다: " +
          (error.message || "알 수 없는 오류")
      );
    }
  };

  // 버튼 활성화 조건
  const isNextDisabled =
    step === "rating"
      ? ratingInt === null || ratingDec === null || ratingValue <= 0
      : comment.trim() === "";

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <div
        className={`rating-modal ${
          step === "rating" ? "rating-modal--rating" : "rating-modal--comment"
        }`}
      >
        {step === "rating" ? (
          <div className="rating-modal__section">
            <div className="rating-modal__title-wrap">
              <h3 className="rating-modal__title">{movieTitle || "영화"}</h3>
            </div>

            {/* 입력기 */}
            <div
              tabIndex={0}
              onKeyDown={handleKey}
              className="rating-modal__score-input"
            >
              <span
                className={`rating-modal__digit ${
                  ratingInt !== null ? "is-filled" : ""
                }`}
              >
                {ratingInt ?? 0}
              </span>
              <span className="rating-modal__dot">.</span>
              <span
                className={`rating-modal__digit ${
                  ratingDec !== null ? "is-filled" : ""
                }`}
              >
                {ratingDec ?? 0}
              </span>
            </div>

            {/* 에러 메시지 */}
            {errorMsg && <div className="rating-modal__error">{errorMsg}</div>}

            {/* 별 */}
            <div className="rating-modal__stars">
              {stars.map((i) => {
                const fillRatio = Math.min(
                  Math.max(ratingValue - (i - 1), 0),
                  1
                );
                const fillPercent = fillRatio * 100;
                return (
                  <svg key={i} width={40} height={40} viewBox="0 0 39 37">
                    <defs>
                      <linearGradient
                        id={`grad-rate-${i}`}
                        x1="0%"
                        y1="0%"
                        x2="100%"
                        y2="0%"
                      >
                        <stop offset={`${fillPercent}%`} stopColor="#678d77" />
                        <stop offset={`${fillPercent}%`} stopColor="#a7c7ad" />
                      </linearGradient>
                    </defs>
                    <StarIcon
                      width={40}
                      height={40}
                      fill={`url(#grad-rate-${i})`}
                    />
                  </svg>
                );
              })}
            </div>

            {/* '평가 건너뛰기/위시리스트에 추가' 버튼 */}
            <div className="rating-modal__secondary-actions">
              <button
                type="button"
                onClick={handleSkip}
                className="rating-modal__skip-button"
              >
                평가 건너뛰기
              </button>
              <button
                type="button"
                onClick={handleAddToWishlist}
                className="rating-modal__skip-button rating-modal__skip-button--ghost"
                disabled={!movieId || !onAddToWishlist}
              >
                위시리스트에 추가
              </button>
            </div>

            {/* '다음' 버튼 */}
            <div className="rating-modal__footer">
              <button
                type="button"
                onClick={handleNext}
                disabled={isNextDisabled}
                className="rating-modal__primary-button"
              >
                다음
              </button>
            </div>
          </div>
        ) : (
          <div className="rating-modal__section">
            <div className="rating-modal__title-wrap rating-modal__title-wrap--compact">
              <h3 className="rating-modal__title">{movieTitle || "영화"}</h3>
            </div>

            <textarea
              className="rating-modal__textarea"
              placeholder="이 작품에 대한 생각을 자유롭게 표현해주세요."
              value={comment}
              onChange={(e) => setComment(e.target.value)}
            />

            <div className="rating-modal__comment-footer">
              <div className="rating-modal__spoiler">
                <span className="rating-modal__spoiler-label">스포일러</span>
                <button
                  type="button"
                  onClick={() => setSpoiler(!spoiler)}
                  className={`rating-modal__toggle ${
                    spoiler ? "is-active" : ""
                  }`}
                >
                  <span className="rating-modal__toggle-knob" />
                </button>
              </div>
              <button
                type="button"
                onClick={handleNext}
                disabled={isNextDisabled}
                className="rating-modal__primary-button"
              >
                저장
              </button>
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
}
