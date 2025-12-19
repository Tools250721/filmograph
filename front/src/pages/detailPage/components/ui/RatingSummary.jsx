import "./RatingSummary.css";

export default function RatingSummary({ myRating, avgRating, avgCountLabel }) {
  return (
    <div className="rating-summary">
      <div className="rating-summary__column rating-summary__column--mine">
        <div className="rating-summary__score">{(myRating / 2).toFixed(1)}</div>
        <div className="rating-summary__label">내 별점</div>
      </div>

      <div className="rating-summary__column rating-summary__column--average">
        <div className="rating-summary__score">{avgRating.toFixed(1)}</div>
        <div className="rating-summary__label">평균 별점({avgCountLabel})</div>
      </div>
    </div>
  );
}
