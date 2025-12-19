import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";

import Header1 from "../layouts/Header_1";
import Header2 from "../layouts/Header_2";
import HeroSection from "./components/HeroSection";
import MovieInfoSection from "./components/MovieInfoSection";
import OTTSection from "./components/OTTSection";
import CastSection from "./components/CastSection";
import CommentsSection from "./components/CommentsSection";
import GallerySection from "./components/GallerySection";
import { HeroSkeleton, MovieInfoSkeleton } from "../../components/ui/Skeleton";
import AuthModal from "../../components/AuthModal";

import { getMovieById } from "../../data/movieAPI";
import { getToken, ratingAPI, userListAPI } from "../../utils/api";
import "./DetailPage.css";

export default function DetailPage() {
  const { movieId } = useParams();
  const [movieData, setMovieData] = useState(null);
  const [userRating, setUserRating] = useState(0);
  const [userComment, setUserComment] = useState("");
  const [userSpoiler, setUserSpoiler] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [isWishlisted, setIsWishlisted] = useState(false);
  const [isWatching, setIsWatching] = useState(false);
  const [isRatingUpdating, setIsRatingUpdating] = useState(false);

  // 로그인 상태 확인
  useEffect(() => {
    const token = getToken();
    setIsLoggedIn(!!token);
  }, []);

  // 위시리스트/보는중 초기 상태 조회 (독립적인 useEffect)
  useEffect(() => {
    const fetchInitialStatus = async () => {
      if (!isLoggedIn) {
        setIsWishlisted(false);
        setIsWatching(false);
        return;
      }

      try {
        const [wishlist, watched] = await Promise.all([
          userListAPI.getWishlist(),
          userListAPI.getWatched(),
        ]);

        const currentMovieId = parseInt(movieId);

        const isInWishlist =
          Array.isArray(wishlist) &&
          wishlist.length > 0 &&
          wishlist.some((m) => {
            const movieIdValue = m.id || m.movieId;
            return parseInt(movieIdValue) === currentMovieId;
          });

        const isInWatched =
          Array.isArray(watched) &&
          watched.length > 0 &&
          watched.some((m) => {
            const movieIdValue = m.id || m.movieId;
            return parseInt(movieIdValue) === currentMovieId;
          });

        setIsWishlisted(isInWishlist);
        setIsWatching(isInWatched);
      } catch (error) {
        setIsWishlisted(false);
        setIsWatching(false);
      }
    };

    fetchInitialStatus();
  }, [movieId, isLoggedIn]);

  // 영화 데이터 로드
  useEffect(() => {
    const loadMovie = async () => {
      try {
        const data = await getMovieById(movieId);
        setMovieData(data);

        if (isLoggedIn && data?.info) {
          try {
            const myRating = await ratingAPI.getMyRatingForMovie(movieId);
            if (myRating) {
              setUserRating(myRating.stars || 0);
              setUserComment(myRating.review || "");
              setUserSpoiler(myRating.spoiler || false);
            } else {
              setUserRating(0);
              setUserComment("");
              setUserSpoiler(false);
            }
          } catch (error) {
            setUserRating(0);
            setUserComment("");
            setUserSpoiler(false);
          }
        }
      } catch (error) {
        console.error("영화 데이터 로딩 실패:", error);
        setMovieData(null);
      }
    };

    loadMovie();
  }, [movieId, isLoggedIn]);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 15);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // 로딩 중이거나 데이터가 없을 때
  if (!movieData) {
    return (
      <div className="detail-page">
        <Header2 />
        <HeroSkeleton />
        <div className="detail-page__section detail-page__section--info">
          <MovieInfoSkeleton />
        </div>
      </div>
    );
  }

  const refreshMovieData = async () => {
    try {
      const data = await getMovieById(movieId);
      setMovieData(data);
    } catch (error) {
      // 새로고침 실패는 무시
    }
  };

  const handleRatingChange = async (
    rating,
    comment = null,
    spoiler = false
  ) => {
    if (!isLoggedIn) {
      setIsAuthModalOpen(true);
      return;
    }

    setIsRatingUpdating(true);
    try {
      if (rating > 0) {
        const stars = Math.max(1.0, Math.min(5.0, rating));
        await ratingAPI.upsertRating(
          parseInt(movieId),
          stars,
          comment || null,
          spoiler
        );
        setUserRating(rating);
        setUserComment(comment || "");
        setUserSpoiler(spoiler);
      } else {
        await ratingAPI.deleteRating(parseInt(movieId));
        setUserRating(0);
        setUserComment("");
        setUserSpoiler(false);
      }
      await refreshMovieData();
    } catch (error) {
      console.error("평점 저장 실패:", error);
      if (error.message?.includes("인증")) {
        setIsAuthModalOpen(true);
      }
    } finally {
      setIsRatingUpdating(false);
    }
  };

  const handleToggleWishlist = async () => {
    if (!isLoggedIn) {
      setIsAuthModalOpen(true);
      return;
    }

    const currentMovieId = parseInt(movieId);
    const previousState = isWishlisted;

    try {
      setIsWishlisted(!previousState);

      if (previousState) {
        await userListAPI.removeFromWishlist(currentMovieId);
      } else {
        await userListAPI.addToWishlist(currentMovieId);
      }

      const wishlist = await userListAPI.getWishlist();
      const isInWishlist =
        Array.isArray(wishlist) &&
        wishlist.length > 0 &&
        wishlist.some((m) => {
          const movieIdValue = m.id || m.movieId;
          return parseInt(movieIdValue) === currentMovieId;
        });

      setIsWishlisted(isInWishlist);
    } catch (error) {
      console.error("위시리스트 업데이트 실패:", error);
      setIsWishlisted(previousState);

      if (error.message?.includes("인증")) {
        setIsAuthModalOpen(true);
      }

      try {
        const wishlist = await userListAPI.getWishlist();
        const isInWishlist =
          Array.isArray(wishlist) &&
          wishlist.length > 0 &&
          wishlist.some((m) => {
            const movieIdValue = m.id || m.movieId;
            return parseInt(movieIdValue) === currentMovieId;
          });
        setIsWishlisted(isInWishlist);
      } catch (recheckError) {
        // 재확인 실패는 무시
      }
    }
  };

  const handleToggleWatching = async () => {
    if (!isLoggedIn) {
      setIsAuthModalOpen(true);
      return;
    }

    const currentMovieId = parseInt(movieId);
    const previousState = isWatching;

    try {
      setIsWatching(!previousState);

      if (previousState) {
        await userListAPI.removeFromWatched(currentMovieId);
      } else {
        await userListAPI.addToWatched(currentMovieId);
      }

      const watched = await userListAPI.getWatched();
      const isInWatched =
        Array.isArray(watched) &&
        watched.length > 0 &&
        watched.some((m) => {
          const movieIdValue = m.id || m.movieId;
          return parseInt(movieIdValue) === currentMovieId;
        });

      setIsWatching(isInWatched);
    } catch (error) {
      console.error("보는중 업데이트 실패:", error);
      setIsWatching(previousState);

      if (error.message?.includes("인증")) {
        setIsAuthModalOpen(true);
      }

      try {
        const watched = await userListAPI.getWatched();
        const isInWatched =
          Array.isArray(watched) &&
          watched.length > 0 &&
          watched.some((m) => {
            const movieIdValue = m.id || m.movieId;
            return parseInt(movieIdValue) === currentMovieId;
          });
        setIsWatching(isInWatched);
      } catch (recheckError) {
        // 재확인 실패는 무시
      }
    }
  };

  const handleLoginSuccess = async () => {
    setIsLoggedIn(true);
    setIsAuthModalOpen(false);
    try {
      const data = await getMovieById(movieId);
      setMovieData(data);

      try {
        const myRatingsResponse = await ratingAPI.getMyRatings(0, 100);
        const myRating = myRatingsResponse?.content?.find(
          (r) => r.movieId === parseInt(movieId)
        );
        if (myRating) {
          setUserRating(myRating.stars || 0);
          setUserComment(myRating.review || "");
          setUserSpoiler(myRating.spoiler || false);
        }
      } catch (error) {
        // 평점 조회 실패는 무시
      }

      try {
        const [wishlist, watched] = await Promise.all([
          userListAPI.getWishlist(),
          userListAPI.getWatched(),
        ]);

        const currentMovieId = parseInt(movieId);
        const isInWishlist =
          Array.isArray(wishlist) &&
          wishlist.some((m) => {
            const movieIdValue = m.id || m.movieId;
            return parseInt(movieIdValue) === currentMovieId;
          });
        const isInWatched =
          Array.isArray(watched) &&
          watched.some((m) => {
            const movieIdValue = m.id || m.movieId;
            return parseInt(movieIdValue) === currentMovieId;
          });

        setIsWishlisted(isInWishlist);
        setIsWatching(isInWatched);
      } catch (error) {
        // 상태 조회 실패는 무시
      }
    } catch (error) {
      // 영화 데이터 로딩 실패는 무시
    }
  };

  // hero 데이터가 없을 때도 처리
  const heroData = movieData?.hero || {};
  const infoData = movieData?.info || {};
  const ottData = movieData?.ott || [];
  const castData = movieData?.cast || [];
  const commentsData = movieData?.comments || [];
  const galleryData = movieData?.gallery || [];

  return (
    <div className="detail-page">
      {/* 헤더 */}
      <div className="detail-page__header">
        <div
          className={`detail-page__header-layer detail-page__header-layer--primary ${
            scrolled ? "" : "is-visible"
          }`}
        >
          <Header1 />
        </div>
        <div
          className={`detail-page__header-layer detail-page__header-layer--secondary ${
            scrolled ? "is-visible" : ""
          }`}
        >
          <Header2 />
        </div>
      </div>

      {/* Hero */}
      <HeroSection movie={heroData} />

      <div className="detail-page__section detail-page__section--info">
        <MovieInfoSection
          movieInfo={infoData}
          movieId={parseInt(movieId)}
          userRating={userRating}
          userComment={userComment}
          userSpoiler={userSpoiler}
          onRatingChange={handleRatingChange}
          onToggleWishlist={handleToggleWishlist}
          onToggleWatching={handleToggleWatching}
          isLoggedIn={isLoggedIn}
          isWishlisted={isWishlisted}
          isWatching={isWatching}
          isRatingUpdating={isRatingUpdating}
          onAuthRequired={() => setIsAuthModalOpen(true)}
        />
      </div>

      <div className="detail-page__section detail-page__section--content">
        <OTTSection data={ottData} />
        <CastSection data={castData} />
        <CommentsSection
          comments={commentsData}
          movieId={movieId ? parseInt(movieId, 10) : null}
        />
        {galleryData.length > 0 && <GallerySection data={galleryData} />}
      </div>

      {/* 로그인 모달 */}
      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onLoginSuccess={handleLoginSuccess}
        initialMode="login"
      />
    </div>
  );
}
