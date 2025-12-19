import React from "react";
import { useNavigate } from "react-router-dom";
import { ReactComponent as UserIcon } from "../../../../assets/icons/UserIcon.svg";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import { useUser } from "../../../../contexts/UserContext";
import styles from "./CommentCard.module.css";

const Star = ({ fillPercent }) => {
  const id = Math.random().toString(36).slice(2, 9);

  return (
    <svg
      width="23"
      height="23"
      viewBox="0 0 24 25"
      xmlns="http://www.w3.org/2000/svg"
    >
      <defs>
        <linearGradient id={`grad-${id}`} x1="0%" y1="0" x2="100%" y2="0">
          <stop offset={`${fillPercent}%`} stopColor="#678D77" />
          <stop offset={`${fillPercent}%`} stopColor="#D4E5CE" />
        </linearGradient>
      </defs>
      <path
        d="M5.825 22.5372L7.45 15.5122L2 10.7872L9.2 10.1622L12 3.53722L14.8 10.1622L22 10.7872L16.55 15.5122L18.175 22.5372L12 18.8122L5.825 22.5372Z"
        fill={`url(#grad-${id})`}
      />
    </svg>
  );
};

const StarRating = ({ rating }) => {
  return (
    <div className={styles.starRatingContainer}>
      {Array.from({ length: 5 }).map((_, i) => {
        const raw = rating - i;
        const fillLevel = raw >= 1 ? 1 : raw <= 0 ? 0 : raw;
        return (
          <div key={i}>
            <Star fillPercent={fillLevel * 100} />
          </div>
        );
      })}
    </div>
  );
};

const CommentCard = ({
  id,
  movieId,
  user,
  userName,
  movieTitle,
  poster,
  content,
  text,
  rating,
  photo,
}) => {
  const navigate = useNavigate();
  const { userProfile } = useUser();

  // movieId가 있으면 사용, 없으면 id 사용
  const actualMovieId = movieId || id;
  const displayContent = content || text || "";
  const displayUserName = userName || user || "익명";
  
  // 내 프로필인지 확인 (userName으로 비교)
  const isMyComment = userProfile?.name && displayUserName && userProfile.name === displayUserName;
  // 프로필 사진 결정: 내 코멘트이고 Context에 프로필 사진이 있으면 Context 사용, 아니면 photo 사용
  const profilePhoto = isMyComment && userProfile?.profileImage 
    ? (userProfile.profileImage.startsWith("http") 
        ? userProfile.profileImage 
        : `${process.env.REACT_APP_API_URL || "http://localhost:8080"}${userProfile.profileImage}`)
    : photo;

  const handlePosterClick = () => {
    if (actualMovieId && typeof actualMovieId === "number" && actualMovieId > 0) {
      navigate(`/movie/${actualMovieId}`);
    } else {
      console.warn("영화 ID가 없습니다.");
    }
  };

  return (
    <div className={styles.card}>
      {/* 유저 정보 */}
      <div className={styles.userInfo}>
        {profilePhoto ? (
          <img
            src={profilePhoto}
            alt={displayUserName}
            style={{
              width: "24px",
              height: "24px",
              borderRadius: "50%",
              objectFit: "cover",
            }}
          />
        ) : (
          <UserIcon style={{ width: 24, height: 24 }} />
        )}
        <span className={styles.username}>
          {displayUserName}
        </span>
      </div>

      {/* 영화 포스터 + 코멘트 */}
      <div className={styles.contentBody}>
        <div onClick={handlePosterClick} style={{ cursor: 'pointer' }}>
          <ImageWithFallback
            src={poster}
            alt={movieTitle || "영화"}
            className={styles.poster}
            placeholder="이미지 없음"
          />
        </div>
        <p className={styles.commentText}>
          {displayContent}
        </p>
      </div>

      {/* 구분선 */}
      <div className={styles.divider} />

      {/* 평점 */}
      <StarRating rating={rating} />
    </div>
  );
};

export default CommentCard;