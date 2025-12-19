import React from "react";
import styles from "./CommentDelete.module.css";

const CommentDelete = ({ isOpen, onClose, onConfirm }) => {
  if (!isOpen) return null;

  return (
    <div className={styles.modalBackdrop}>
      <div className={styles.modalContent}>
        <h3 className={styles.title}>알림</h3>
        <p className={styles.message}>코멘트를 삭제하시겠어요?</p>
        <hr className={styles.divider} />
        <div className={styles.buttonContainer}>
          <button
            onClick={onClose}
            className={`${styles.button} ${styles.cancelButton}`}
          >
            취소
          </button>
          <div className={styles.buttonSeparator} />
          <button
            onClick={onConfirm}
            className={`${styles.button} ${styles.confirmButton}`}
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
};

export default CommentDelete;