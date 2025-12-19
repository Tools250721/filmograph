import { ReactComponent as StarIcon } from "../../../../assets/icons/StarIcon.svg";
import "./RatingHistogram.css";

export default function RatingHistogram({
  histogram,
  avgRating,
  avgCountLabel,
}) {
  const max = Math.max(...histogram, 1);

  return (
    <div className="rating-histogram">
      <div className="rating-histogram__summary">
        <div className="rating-histogram__label">별점 그래프</div>
        <div className="rating-histogram__average">
          <span className="rating-histogram__average-text">평균</span>
          <span className="rating-histogram__average-value">
            <span className="rating-histogram__average-score">
              <StarIcon className="rating-histogram__average-icon" />
              {avgRating.toFixed(1)}
            </span>
            <span className="rating-histogram__average-count">
              ({avgCountLabel})
            </span>
          </span>
        </div>
      </div>

      <div className="rating-histogram__bars">
        {histogram.map((v, idx) => {
          const isMax = v === max;
          const barColor = isMax ? "#678d77" : "#a7c7ad";

          // 각 bin의 범위: idx 0 = 0.0~0.5, idx 1 = 0.5~1.0, ..., idx 9 = 4.5~5.0
          const minValue = idx * 0.5;
          const maxValue = (idx + 1) * 0.5;
          const binRange = `${minValue.toFixed(1)}~${maxValue.toFixed(1)}`;

          return (
            <div key={idx} className="rating-histogram__bar-wrap">
              {idx === 0 && (
                <div className="rating-histogram__bar-label">0.5</div>
              )}
              {idx === histogram.length - 1 && (
                <div className="rating-histogram__bar-label">5</div>
              )}

              {/* 막대 */}
              <div
                title={binRange}
                className="rating-histogram__bar"
                style={{
                  height: Math.max((v / max) * 60, 4),
                  background: barColor,
                }}
              />
            </div>
          );
        })}
      </div>
    </div>
  );
}
