import React from "react";
import "./Skeleton.css";

// 기본 스켈레톤 (직사각형)
export const Skeleton = ({ width, height, borderRadius = "4px", className = "" }) => {
  return (
    <div
      className={`skeleton ${className}`}
      style={{
        width: width || "100%",
        height: height || "20px",
        borderRadius,
      }}
    />
  );
};

// 포스터 스켈레톤
export const PosterSkeleton = ({ width = 250, height = 360 }) => {
  return (
    <div className="skeleton-poster" style={{ width, height }}>
      <Skeleton width="100%" height="100%" borderRadius="4px" />
    </div>
  );
};

// 카드 스켈레톤 (포스터 + 제목)
export const CardSkeleton = ({ width = 250, height = 360 }) => {
  return (
    <div className="skeleton-card" style={{ width }}>
      <PosterSkeleton width={width} height={height} />
      <Skeleton width="80%" height="16px" className="skeleton-title" />
    </div>
  );
};

// 검색 결과 카드 스켈레톤
export const SearchCardSkeleton = () => {
  return (
    <div className="skeleton-search-card">
      <Skeleton width="70px" height="105px" borderRadius="3px" />
      <div className="skeleton-search-content">
        <Skeleton width="60%" height="18px" className="skeleton-search-title" />
        <Skeleton width="40%" height="14px" className="skeleton-search-creator" />
        <div className="skeleton-search-ott">
          <Skeleton width="40px" height="18px" borderRadius="5px" />
          <Skeleton width="40px" height="18px" borderRadius="5px" />
        </div>
      </div>
    </div>
  );
};

// 섹션 스켈레톤 (여러 카드)
export const SectionSkeleton = ({ count = 5, cardWidth = 250, cardHeight = 360 }) => {
  return (
    <div className="skeleton-section">
      <Skeleton width="200px" height="24px" className="skeleton-section-title" />
      <div className="skeleton-section-grid">
        {Array.from({ length: count }).map((_, i) => (
          <CardSkeleton key={i} width={cardWidth} height={cardHeight} />
        ))}
      </div>
    </div>
  );
};

// 히어로 섹션 스켈레톤
export const HeroSkeleton = () => {
  return (
    <div className="skeleton-hero">
      <Skeleton width="100%" height="500px" borderRadius="0" />
    </div>
  );
};

// 영화 정보 섹션 스켈레톤
export const MovieInfoSkeleton = () => {
  return (
    <div className="skeleton-movie-info">
      <div className="skeleton-movie-info-left">
        <Skeleton width="200px" height="300px" borderRadius="4px" />
      </div>
      <div className="skeleton-movie-info-right">
        <Skeleton width="60%" height="32px" className="skeleton-movie-title" />
        <Skeleton width="40%" height="20px" className="skeleton-movie-meta" />
        <Skeleton width="100%" height="100px" className="skeleton-movie-overview" />
      </div>
    </div>
  );
};

