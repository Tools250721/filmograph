import React, { useState } from "react";
import { ReactComponent as UserIcon } from "../../../assets/icons/UserIcon.svg";
import { useNavigate } from "react-router-dom";
import styles from "./UserProfileCard.module.css";

const UserProfileCard = ({
  userName,
  profileImage,
  bgImage,
  ratingsCount,
  commentsCount,
  onEditProfile,
  onShareProfile,
  onLogout,
}) => {
  const navigate = useNavigate();

  // 프로필 이미지 URL 처리 (백엔드 URL과 결합)
  const getProfileImageUrl = () => {
    if (!profileImage) return null;
    // 이미 전체 URL인 경우
    if (
      profileImage.startsWith("http://") ||
      profileImage.startsWith("https://")
    ) {
      return profileImage;
    }
    // 상대 경로인 경우 백엔드 URL과 결합
    const apiBaseUrl = process.env.REACT_APP_API_URL || "http://localhost:8080";
    return `${apiBaseUrl}${
      profileImage.startsWith("/") ? profileImage : "/" + profileImage
    }`;
  };

  const profileImageUrl = getProfileImageUrl();
  const [imageError, setImageError] = useState(false);

  return (
    <div className={styles.card}>
      {/* 프로필 이미지 */}
      {profileImageUrl && !imageError ? (
        <img
          src={profileImageUrl}
          alt={userName || "프로필"}
          className={styles.userIcon}
          onError={() => {
            // 이미지 로드 실패 시 기본 아이콘으로 대체
            setImageError(true);
          }}
        />
      ) : (
        <UserIcon className={styles.userIcon} />
      )}

      {/* 사용자 이름 */}
      <p className={styles.username}>{userName}</p>

      {/* 평가/코멘트 수 */}
      <div className={styles.statsContainer}>
        {/* 평가 */}
        <div onClick={() => navigate("/rated")} className={styles.statItem}>
          <span className={styles.statCount}>{ratingsCount}</span>
          <span className={styles.statLabel}>평가</span>
        </div>

        {/* 세로 구분선 */}
        <div className={styles.dividerVertical}></div>

        {/* 코멘트 */}
        <div onClick={() => navigate("/commented")} className={styles.statItem}>
          <span className={styles.statCount}>{commentsCount}</span>
          <span className={styles.statLabel}>코멘트</span>
        </div>
      </div>

      {/* 구분선 */}
      <div className={styles.dividerHorizontal} />

      {/* 버튼 영역 */}
      <button
        onClick={onEditProfile}
        className={`${styles.button} ${styles.editButton}`}
      >
        <span className={styles.buttonText}>프로필 수정</span>
      </button>

      <button
        onClick={onShareProfile}
        className={`${styles.button} ${styles.shareButton}`}
      >
        <span className={styles.buttonText}>프로필 공유</span>
      </button>

      {onLogout && (
        <button
          onClick={onLogout}
          className={`${styles.button} ${styles.logoutButton}`}
        >
          <span className={styles.buttonText}>로그아웃</span>
        </button>
      )}
    </div>
  );
};

export default UserProfileCard;
