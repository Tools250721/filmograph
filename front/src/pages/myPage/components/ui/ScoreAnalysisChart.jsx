import React, { useState } from "react";

const ScoreAnalysisChart = ({ counts, onBarClick }) => {
  const maxCount = Math.max(...counts, 1);

  const [hoverIndex, setHoverIndex] = useState(null);

  return (
    <div
      style={{
        display: "flex",
        alignItems: "flex-end",
        justifyContent: "center",
        height: "150px",
        marginBottom: "20px",
        gap: "1px",
      }}
    >
      {counts.map((count, idx) => {
        let bgColor = "#A7C7AD";
        if (hoverIndex === idx) {
          bgColor = "#678D77";
        }

        return (
          <div
            key={idx}
            style={{
              width: "60px",
              height:
                maxCount > 0
                  ? `${Math.max((count / maxCount) * 100, 2)}%`
                  : "2%",
              backgroundColor: bgColor,
              borderTopLeftRadius: "3px",
              borderTopRightRadius: "3px",
              display: "flex",
              alignItems: "flex-end",
              justifyContent: "center",
              paddingBottom: "5px",
              fontSize: "12px",
              color: count > 0 ? "#fff" : "transparent",
              fontWeight: "500",
              cursor: "pointer",
              transition: "background-color 0.1s ease",
            }}
            onMouseEnter={() => setHoverIndex(idx)}
            onMouseLeave={() => setHoverIndex(null)}
            onClick={() => onBarClick(idx)}
          >
            {count > 0 && count}
          </div>
        );
      })}
    </div>
  );
};

export default ScoreAnalysisChart;
