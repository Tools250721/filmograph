import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../layouts/Header_2";
import UserProfileCard from "./components/UserProfileCard";
import WatchlistSection from "./components/WatchlistSection";
import ScoreAnalysis from "./components/ScoreAnalysis";
import MovieListSection from "./components/MovieListSection";
import ProfileShare from "./components/ProfileShare";
import ProfileEdit from "./components/ProfileEdit";
import AuthModal from "../../components/AuthModal";
import {
  getMyProfile,
  getMyRatedMovies,
  getMyComments,
} from "../../data/myPageAPI";
import { getToken, removeToken, userAPI } from "../../utils/api";
import { useUser } from "../../contexts/UserContext";
import styles from "./MyPage.module.css";

export default function MyPage() {
  const navigate = useNavigate();
  const { userProfile: contextProfile, updateProfile, reloadProfile } = useUser();
  const [userProfile, setUserProfile] = useState({
    name: "...",
    profileImage: null,
    bgImage: null,
  });
  const [myRatedMovies, setMyRatedMovies] = useState([]);
  const [myComments, setMyComments] = useState([]);
  const [selectedInterval, setSelectedInterval] = useState(4.5);
  const [isSharePopupOpen, setSharePopupOpen] = useState(false);
  const [isEditPopupOpen, setEditPopupOpen] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const checkAuthAndLoadData = async () => {
      const token = getToken();
      if (!token) {
        setIsAuthModalOpen(true);
        setIsAuthenticated(false);
        setIsLoading(false);
        return;
      }

      setIsAuthenticated(true);
      try {
        setIsLoading(true);
        const [profileData, ratedMoviesData, commentsData] = await Promise.all([
          getMyProfile(),
          getMyRatedMovies(),
          getMyComments(),
        ]);

        setUserProfile(profileData);
        setMyRatedMovies(ratedMoviesData);
        setMyComments(commentsData);
      } catch (error) {
        console.error("데이터 로딩 실패:", error);
        if (
          error.isAuthError ||
          error.message?.includes("인증이 필요합니다") ||
          error.message?.includes("JWT")
        ) {
          setIsAuthModalOpen(true);
          setIsAuthenticated(false);
          // 토큰이 만료되었거나 유효하지 않으면 삭제
          localStorage.removeItem("accessToken");
        }
      } finally {
        setIsLoading(false);
      }
    };

    checkAuthAndLoadData();
  }, []);

  const handleLoginSuccess = () => {
    setIsAuthModalOpen(false);
    setIsAuthenticated(true);
    // 페이지 새로고침하여 데이터 다시 로드
    window.location.reload();
  };

  const handleLogout = () => {
    if (window.confirm("로그아웃 하시겠습니까?")) {
      removeToken();
      setIsAuthenticated(false);
      setIsAuthModalOpen(true);
      setUserProfile({
        name: "...",
        profileImage: null,
        bgImage: null,
      });
      setMyRatedMovies([]);
      setMyComments([]);
    }
  };

  const handleEditProfile = () => setEditPopupOpen(true);
  const handleShareProfile = () => {
    navigator.clipboard.writeText(window.location.href);
    setSharePopupOpen(true);
  };
  const handleSaveProfile = async (newData) => {
    try {
      // 이름 업데이트
      if (
        newData.name &&
        newData.name.trim() !== "" &&
        newData.name !== userProfile.name
      ) {
        await userAPI.updateName(newData.name);
      }

      // 프로필 이미지 업로드
      if (newData.profileImageFile) {
        const response = await userAPI.uploadImage(
          "profile",
          newData.profileImageFile
        );
        newData.profileImage = response.imageUrl;
      }

      // 배경 이미지 업로드
      if (newData.bgImageFile) {
        const response = await userAPI.uploadImage(
          "background",
          newData.bgImageFile
        );
        newData.bgImage = response.imageUrl;
      }

      // 프로필 정보 다시 로드
      const updatedProfile = await getMyProfile();
      setUserProfile(updatedProfile);
      // Context 업데이트 (모든 곳에 반영)
      const fullProfile = await userAPI.getMyProfile();
      updateProfile({
        id: fullProfile.id,
        name: updatedProfile.name,
        profileImage: updatedProfile.profileImage,
        bgImage: updatedProfile.bgImage,
      });
      setEditPopupOpen(false);
    } catch (error) {
      alert(
        "프로필 저장에 실패했습니다: " + (error.message || "알 수 없는 오류")
      );
    }
  };
  const handleDeleteProfile = () => {
    if (
      window.confirm("정말로 회원 탈퇴를 하시겠습니까? 모든 정보가 삭제됩니다.")
    ) {
      alert("회원 탈퇴 처리되었습니다.");
      setEditPopupOpen(false);
    }
  };

  const ratings = myRatedMovies.map((movie) => movie.rating);
  const ratingsCount = ratings.length;

  if (isLoading) {
    return (
      <div className={styles.loadingWrapper}>
        <Header />
        <div className={styles.loadingMessage}>
          <div className={styles.spinner}></div>
          <p>마이페이지 로딩 중...</p>
        </div>
        <AuthModal
          isOpen={isAuthModalOpen}
          onClose={() => {
            setIsAuthModalOpen(false);
            navigate("/");
          }}
          onLoginSuccess={handleLoginSuccess}
          initialMode="login"
        />
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className={styles.pageWrapper}>
        <Header />
        <AuthModal
          isOpen={isAuthModalOpen}
          onClose={() => {
            setIsAuthModalOpen(false);
            navigate("/");
          }}
          onLoginSuccess={handleLoginSuccess}
          initialMode="login"
        />
      </div>
    );
  }

  return (
    <div className={styles.pageWrapper}>
      <Header />
      <div className={styles.container}>
        {/* 왼쪽 영역 */}
        <div className={styles.leftColumn}>
          <UserProfileCard
            userName={userProfile.name}
            profileImage={userProfile.profileImage}
            bgImage={userProfile.bgImage}
            ratingsCount={ratingsCount}
            commentsCount={myComments.length}
            onEditProfile={handleEditProfile}
            onShareProfile={handleShareProfile}
            onLogout={handleLogout}
          />
          <WatchlistSection />
        </div>

        {/* 오른쪽 영역 */}
        <div className={styles.rightColumn}>
          <ScoreAnalysis
            ratings={ratings}
            ratingsCount={ratingsCount}
            onBarClick={(interval) => setSelectedInterval(interval)}
          />
          <MovieListSection
            selectedInterval={selectedInterval}
            movies={myRatedMovies}
          />
        </div>
      </div>

      {/* 팝업들 */}
      <ProfileShare
        isOpen={isSharePopupOpen}
        onClose={() => setSharePopupOpen(false)}
      />
      <ProfileEdit
        isOpen={isEditPopupOpen}
        onClose={() => setEditPopupOpen(false)}
        currentName={userProfile.name}
        currentProfileImage={userProfile.profileImage}
        currentBgImage={userProfile.bgImage}
        onSave={handleSaveProfile}
        onDelete={handleDeleteProfile}
      />
    </div>
  );
}
