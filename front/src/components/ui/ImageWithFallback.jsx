import React, { useState } from "react";
import "./ImageWithFallback.css";

/**
 * 이미지 로딩 실패 시 '이미지 없음' 텍스트를 표시하는 컴포넌트
 */
export default function ImageWithFallback({
  src,
  alt = "",
  className = "",
  width,
  height,
  style = {},
  placeholder = "이미지 없음",
}) {
  const [imageError, setImageError] = useState(false);
  const [imageLoaded, setImageLoaded] = useState(false);

  if (!src || imageError) {
    return (
      <div
        className={`image-fallback ${className}`}
        style={{
          ...(width && { width }),
          ...(height && { height }),
          ...style,
        }}
      >
        <span className="image-fallback-text">{placeholder}</span>
      </div>
    );
  }

  return (
    <>
      {!imageLoaded && !imageError && (
        <div
          className={`image-fallback image-fallback--loading ${className}`}
          style={{
            ...(width && { width }),
            ...(height && { height }),
            ...style,
          }}
        >
          <span className="image-fallback-text">로딩 중...</span>
        </div>
      )}
      <img
        src={src}
        alt={alt}
        className={className}
        style={{
          display: imageLoaded && !imageError ? "block" : "none",
          ...(width && { width }),
          ...(height && { height }),
          ...style,
        }}
        onLoad={() => setImageLoaded(true)}
        onError={() => {
          setImageError(true);
          setImageLoaded(false);
        }}
      />
    </>
  );
}
