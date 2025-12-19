import React from "react";
import { useNavigate } from "react-router-dom";
import { ReactComponent as BookmarkIcon } from "../../../assets/icons/BookmarkIcon.svg";
import { ReactComponent as EyeIcon } from "../../../assets/icons/EyeIcon.svg";
import styles from "./WatchlistSection.module.css";

const WatchlistSection = () => {
  const navigate = useNavigate();

  const goToWatchlist = () => navigate("/watchlist"); // 위시리스트 페이지로 이동
  const goToWatching = () => navigate("/watching"); // 보는중 페이지로 이동

  return (
    <div className={styles.card}>
      <h3 className={styles.title}>
        보관함
      </h3>

      <div className={styles.linksContainer}>
        {/* 위시리스트 */}
        <div onClick={goToWatchlist} className={styles.linkItem}>
          <div className={styles.iconWrapper}>
            <BookmarkIcon width={38} height={48} />
          </div>
          <p className={styles.linkText}>
            위시리스트
          </p>
        </div>

        {/* 보는중 */}
        <div onClick={goToWatching} className={styles.linkItem}>
          <div className={styles.iconWrapper}>
            <EyeIcon width={68} height={45} />
          </div>
          <p className={styles.linkText}>
            보는중
          </p>
        </div>
      </div>
    </div>
  );
};

export default WatchlistSection;