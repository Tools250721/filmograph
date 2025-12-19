import React, { useState, useEffect } from "react";
import { ReactComponent as UserIcon } from "../../../assets/icons/UserIcon.svg";
import { ReactComponent as CameraIcon } from "../../../assets/icons/CameraIcon.svg";
import styles from "./ProfileEdit.module.css";

const ProfileEdit = ({ 
  isOpen, 
  onClose, 
  currentName, 
  currentProfileImage,
  currentBgImage,
  onSave, 
  onDelete 
}) => {
  const [name, setName] = useState(currentName || "");
  const [profileImage, setProfileImage] = useState(null);
  const [bgImage, setBgImage] = useState(null);
  const [profileImageFile, setProfileImageFile] = useState(null);
  const [bgImageFile, setBgImageFile] = useState(null);

  // URL 처리 함수 (백엔드 URL과 결합)
  const getImageUrl = (imagePath) => {
    if (!imagePath) return null;
    // 이미 전체 URL인 경우
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
      return imagePath;
    }
    // 상대 경로인 경우 백엔드 URL과 결합
    const apiBaseUrl = process.env.REACT_APP_API_URL || "http://localhost:8080";
    return `${apiBaseUrl}${imagePath.startsWith("/") ? imagePath : "/" + imagePath}`;
  };

  // currentName이 변경되면 name 상태 업데이트
  useEffect(() => {
    if (currentName) {
      setName(currentName);
    }
  }, [currentName]);

  // 모달이 열릴 때 현재 이미지 초기화 (새 파일을 선택하지 않은 경우)
  useEffect(() => {
    if (isOpen) {
      // 새로 선택한 파일이 없을 때만 현재 프로필 이미지로 초기화
      if (!profileImageFile) {
        if (currentProfileImage) {
          setProfileImage(getImageUrl(currentProfileImage));
        } else {
          setProfileImage(null);
        }
      }
      // 새로 선택한 파일이 없을 때만 현재 배경 이미지로 초기화
      if (!bgImageFile) {
        if (currentBgImage) {
          setBgImage(getImageUrl(currentBgImage));
        } else {
          setBgImage(null);
        }
      }
    } else {
      // 모달이 닫힐 때 파일 선택 초기화 (다음에 열 때를 위해)
      setProfileImageFile(null);
      setBgImageFile(null);
    }
  }, [isOpen, currentProfileImage, currentBgImage]);

  if (!isOpen) return null;

  // 프로필 이미지 변경
  const handleProfileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setProfileImageFile(file);
      // 새 파일의 미리보기 URL 생성
      const previewUrl = URL.createObjectURL(file);
      setProfileImage(previewUrl);
    } else {
      // 파일 선택 취소 시 현재 프로필 이미지로 되돌림
      setProfileImageFile(null);
      if (currentProfileImage) {
        setProfileImage(getImageUrl(currentProfileImage));
      } else {
        setProfileImage(null);
      }
    }
  };

  // 배경 이미지 변경
  const handleBgChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setBgImageFile(file);
      // 새 파일의 미리보기 URL 생성
      const previewUrl = URL.createObjectURL(file);
      setBgImage(previewUrl);
    } else {
      // 파일 선택 취소 시 현재 배경 이미지로 되돌림
      setBgImageFile(null);
      if (currentBgImage) {
        setBgImage(getImageUrl(currentBgImage));
      } else {
        setBgImage(null);
      }
    }
  };

  // 저장
  const handleSave = () => {
    onSave({
      name,
      profileImage,
      bgImage,
      profileImageFile,
      bgImageFile,
    });
  };

  return (
    <div className={styles.overlay}>
      <div className={styles.modalContent}>
        {/* 상단 바 */}
        <div className={styles.header}>
          <button onClick={onClose} className={styles.closeButton}>
            ✕
          </button>
          <h3 className={styles.title}>프로필 수정</h3>
          <div className={styles.headerSpacer} />
        </div>

        {/* 배경 이미지 */}
        <div
          className={styles.bgImageContainer}
          style={{
            backgroundImage: bgImage
              ? `url(${bgImage})`
              : currentBgImage
              ? `url(${getImageUrl(currentBgImage)})`
              : "url('/images/profile-bg.png')",
          }}
        >
          {/* 배경 카메라 버튼 */}
          <label htmlFor="bgFileInput" className={styles.cameraLabelBg}>
            <CameraIcon
              style={{ width: "22px", height: "22px", fill: "#678D77" }}
            />
          </label>
          <input
            id="bgFileInput"
            type="file"
            accept="image/*"
            className={styles.fileInput}
            onChange={handleBgChange}
          />

          {/* 프로필 이미지 */}
          <div className={styles.profileImageContainer}>
            {profileImage || (currentProfileImage && !profileImageFile) ? (
              <img
                src={profileImage || getImageUrl(currentProfileImage)}
                alt="profile"
                className={styles.profileImage}
              />
            ) : (
              <UserIcon
                style={{ width: "100%", height: "100%", objectFit: "cover" }}
              />
            )}

            {/* 프로필 카메라 버튼 */}
            <label
              htmlFor="profileFileInput"
              className={styles.cameraLabelProfile}
            >
              <CameraIcon
                style={{ width: "22px", height: "22px", fill: "#fff" }}
              />
            </label>
            <input
              id="profileFileInput"
              type="file"
              accept="image/*"
              className={styles.fileInput}
              onChange={handleProfileChange}
            />
          </div>
        </div>

        {/* 이름 입력 */}
        <div className={styles.inputWrapper}>
          <label className={styles.inputLabel}>이름</label>
          <input
            type="text"
            value={name}
            maxLength={20}
            onChange={(e) => setName(e.target.value)}
            className={styles.nameInput}
          />
          <div className={styles.charCount}>{name.length}/20</div>
        </div>

        {/* 회원 탈퇴 버튼 */}
        <div className={styles.deleteButtonWrapper}>
          <button
            onClick={onDelete}
            className={`${styles.button} ${styles.deleteButton}`}
          >
            회원 탈퇴
          </button>
        </div>

        {/* 저장 버튼 */}
        <div className={styles.saveButtonWrapper}>
          <button onClick={handleSave} className={styles.button}>
            확인
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProfileEdit;
