import React, { useState } from "react";
import "./StarRating.css";

const StarRating = ({ rating, onRate, readonly = false, size = 18 }) => {
  const [hover, setHover] = useState(0);

  const containerClass = `star-rating${
    readonly ? " star-rating--readonly" : ""
  }`;

  return (
    <div className={containerClass}>
      {[1, 2, 3, 4, 5].map((star) => {
        const filled = (hover || rating) >= star;
        return (
          <button
            key={star}
            type="button"
            className={`star-rating__button${
              readonly ? " star-rating__button--readonly" : ""
            }`}
            onMouseEnter={() => !readonly && setHover(star)}
            onMouseLeave={() => !readonly && setHover(0)}
            onClick={() => !readonly && onRate && onRate(star)}
          >
            <svg
              width={size}
              height={size}
              viewBox="0 0 24 24"
              fill={filled ? "#22c55e" : "none"}
              stroke={filled ? "#22c55e" : "#d1d5db"}
              strokeWidth="1"
            >
              <polygon points="12,2 15.09,8.26 22,9.27 17,14.14 18.18,21.02 12,17.77 5.82,21.02 7,14.14 2,9.27 8.91,8.26" />
            </svg>
          </button>
        );
      })}
    </div>
  );
};

export default StarRating;
