import React from "react";
import ImageWithFallback from "../../../components/ui/ImageWithFallback";
import "./HeroSection.css";

const HeroSection = ({ movie }) => {
  // movie가 없거나 undefined인 경우 처리
  if (!movie || typeof movie !== 'object') {
    return (
      <div className="hero-section">
        <div style={{ padding: '40px', textAlign: 'center' }}>
          영화 정보를 불러오는 중...
        </div>
      </div>
    );
  }
  
  // 안전하게 속성 추출
  const imageUrl = movie.imageUrl || movie.backdropUrl || '';
  const title = movie.title || '제목 없음';
  const originalTitle = movie.originalTitle || '';
  const year = movie.year || movie.releaseYear || null;
  const genres = Array.isArray(movie.genres) ? movie.genres : [];
  const country = movie.country || '';
  const duration = movie.duration || movie.runtimeMinutes || null;
  const ageRating = movie.ageRating || '';
  
  return (
    <div className="hero-section">
      {/* 배경 이미지 */}
      <ImageWithFallback
        src={imageUrl}
        alt={`${title} 히어로 이미지`}
        className="hero-section__background"
        width="100%"
        height="500px"
        placeholder="이미지 없음"
        style={{ objectFit: "cover" }}
      />

      {/* shadow 효과 */}
      <div className="hero-section__overlay" />

      {/* 텍스트 영역 */}
      <div className="hero-section__content">
        <div>
          <h1 className="hero-section__title">{title}</h1>
          <div className="hero-section__meta">
            {originalTitle && `${originalTitle} `}
            <br />
            {year && `${year}년`} {genres.length > 0 && `• ${genres.join("/")}`} {country && `• ${country}`}
            <br />
            {duration && `${duration}분`} {ageRating && `• ${ageRating}`}
          </div>
        </div>
      </div>
    </div>
  );
};

export default HeroSection;
