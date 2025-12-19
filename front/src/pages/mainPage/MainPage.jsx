import React, { useState, useEffect } from "react";
import Header2 from "../layouts/Header_2";
import BoxOfficeSection from "./components/BoxOfficeSection";
import CommentSection from "./components/CommentSection";
import RankingSection from "./components/RankingSection";
import WishlistSection from "./components/WishlistSection";
import { SectionSkeleton } from "../../components/ui/Skeleton";

import {
  getBoxOffice,
  getNetflixRanking,
  getWatchaRanking,
  getWishlist,
  getMainComments,
  getTvingRanking,
} from "../../data/mainPageAPI";

import styles from "./MainPage.module.css";

const MainPage = () => {
  const [boxOfficeMovies, setBoxOfficeMovies] = useState([]);
  const [netflixMovies, setNetflixMovies] = useState([]);
  const [watchaMovies, setWatchaMovies] = useState([]);
  const [tvingMovies, setTvingMovies] = useState([]);
  const [wishlistMovies, setWishlistMovies] = useState([]);
  const [mainComments, setMainComments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const loadData = async () => {
    try {
      setIsLoading(true);

      const token = localStorage.getItem("accessToken");
      const loggedIn = !!token;
      setIsLoggedIn(loggedIn);

      const fetchOrEmpty = async (fn) => {
        try {
          return await fn();
        } catch (error) {
          return [];
        }
      };

      const [boxOffice, netflix, watcha, tving, wishlist, comments] =
        await Promise.all([
          fetchOrEmpty(getBoxOffice),
          fetchOrEmpty(getNetflixRanking),
          fetchOrEmpty(getWatchaRanking),
          fetchOrEmpty(getTvingRanking),
          fetchOrEmpty(getWishlist),
          fetchOrEmpty(getMainComments), // 여기서 에러나도 []로 처리됨
        ]);

      // 배열이 아닌 경우 빈 배열로 설정
      setBoxOfficeMovies(Array.isArray(boxOffice) ? boxOffice : []);
      setNetflixMovies(Array.isArray(netflix) ? netflix : []);
      setWatchaMovies(Array.isArray(watcha) ? watcha : []);
      setTvingMovies(Array.isArray(tving) ? tving : []);
      setWishlistMovies(Array.isArray(wishlist) ? wishlist : []);
      setMainComments(Array.isArray(comments) ? comments : []);
    } catch (error) {
      console.error("데이터 로딩 중 치명적 오류:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 페이지가 다시 포커스될 때 데이터 새로고침 (별점 입력 후 MainPage로 돌아왔을 때)
  useEffect(() => {
    const handleFocus = () => {
      loadData();
    };

    window.addEventListener("focus", handleFocus);
    return () => {
      window.removeEventListener("focus", handleFocus);
    };
  }, []);

  if (isLoading) {
    return (
      <div className={styles.pageWrapper}>
        <Header2 />
        <div className={styles.mainContainer}>
          <SectionSkeleton count={5} cardWidth={250} cardHeight={360} />
          <SectionSkeleton count={5} cardWidth={250} cardHeight={360} />
          <SectionSkeleton count={5} cardWidth={250} cardHeight={360} />
        </div>
      </div>
    );
  }

  return (
    <div className={styles.pageWrapper}>
      <Header2 />
      <div className={styles.mainContainer}>
        <BoxOfficeSection movies={boxOfficeMovies} />
        <CommentSection comments={mainComments} isLoggedIn={isLoggedIn} />
        <RankingSection sectionTitle="글로벌 순위" movies={netflixMovies} />
        <RankingSection sectionTitle="한국 순위" movies={watchaMovies} />

        <WishlistSection movies={wishlistMovies} isLoggedIn={isLoggedIn} />

        <RankingSection sectionTitle="US 순위" movies={tvingMovies} />
      </div>
    </div>
  );
};

export default MainPage;
