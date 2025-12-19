import React from "react";
import styles from "./ProfileShare.module.css";

const ProfileShare = ({ isOpen, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className={styles.overlay}>
      <div className={styles.modalBox}>
        <h3 className={styles.title}>
          알림
        </h3>
        <p className={styles.message}>
          프로필 링크가 복사됐습니다
        </p>
        
        <hr className={styles.divider} />
        
        <button
          onClick={onClose}
          className={styles.closeButton}
        >
          확인
        </button>
      </div>
    </div>
  );
};

export default ProfileShare;