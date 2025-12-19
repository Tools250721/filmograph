import React, { useState } from "react";
import ScoreAnalysisChart from "./ui/ScoreAnalysisChart";
import styles from "./ScoreAnalysis.module.css";

const ScoreAnalysis = ({ ratings = [], ratingsCount, onBarClick }) => {
  const [selectedIndex, setSelectedIndex] = useState(null);

  if (ratings.length === 0) {
    return (
      <div className={styles.card}>
        <h3 className={styles.title}>
          점수 분석
        </h3>
        <div className={styles.emptyState}>
          평가 기록이 없습니다.
        </div>
      </div>
    );
  }

  // (차트 계산 로직은 동일)
  const counts = Array(10).fill(0);
  ratings.forEach((rating) => {
    if (rating > 0 && rating <= 5.0) {
      const index = Math.ceil(rating * 2) - 1;
      if (index >= 0 && index < 10) {
        counts[index]++;
      }
    }
  });
  const averageRating = (
    ratings.reduce((sum, r) => sum + r, 0) / ratings.length
  ).toFixed(1);
  const freqMap = {};
  ratings.forEach((r) => {
    freqMap[r] = (freqMap[r] || 0) + 1;
  });
  const mostGivenRating = Object.keys(freqMap).reduce((a, b) =>
    freqMap[a] > freqMap[b] ? a : b
  );
  const handleChartBarClick = (index) => {
    setSelectedIndex(index);
    const intervalValue = (index + 1) * 0.5 - 0.5;
    onBarClick(intervalValue);
  };

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>
        점수 분석
      </h3>

      <ScoreAnalysisChart
        counts={counts}
        onBarClick={handleChartBarClick}
        selectedIndex={selectedIndex}
      />

      {/* 하단 수치 */}
      <div className={styles.statsContainer}>
        <div className={styles.statItem}>
          <p className={styles.statValue}>
            {averageRating}
          </p>
          <p className={styles.statLabel}>
            별점 평균
          </p>
        </div>
        <div className={styles.statItem}>
          <p className={styles.statValue}>
            {ratingsCount}
          </p>
          <p className={styles.statLabel}>
            별점 개수
          </p>
        </div>
        <div className={styles.statItem}>
          <p className={styles.statValue}>
            {mostGivenRating}
          </p>
          <p className={styles.statLabel}>
            많이 준 별점
          </p>
        </div>
      </div>
    </div>
  );
};

export default ScoreAnalysis;