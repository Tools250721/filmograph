import { ReactComponent as StarIcon } from "../../../../assets/icons/StarIcon.svg";
import "./RatingStars.css";

export default function RatingStars({ myRating, onClick }) {
  const stars = [1, 2, 3, 4, 5];
  return (
    <div className="rating-stars">
      {stars.map((i) => {
        const score = myRating / 2;
        const fillRatio = Math.min(Math.max(score - (i - 1), 0), 1);
        const fillPercent = fillRatio * 100;
        return (
          <button
            key={i}
            type="button"
            onClick={() => onClick(i)}
            className="rating-stars__button"
          >
            <svg width="0" height="0">
              <defs>
                <linearGradient
                  id={`grad-${i}`}
                  x1="0%"
                  y1="0%"
                  x2="100%"
                  y2="0%"
                >
                  <stop offset={`${fillPercent}%`} stopColor="#678d77" />
                  <stop offset={`${fillPercent}%`} stopColor="#a7c7ad" />
                </linearGradient>
              </defs>
            </svg>
            <StarIcon
              width={34}
              height={34}
              className="rating-stars__icon"
              style={{ fill: `url(#grad-${i})` }}
            />
          </button>
        );
      })}
    </div>
  );
}
