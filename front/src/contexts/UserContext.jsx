import React, { createContext, useContext, useState, useEffect } from "react";
import { getMyProfile } from "../data/myPageAPI";
import { getToken } from "../utils/api";

const UserContext = createContext(null);

export const UserProvider = ({ children }) => {
  const [userProfile, setUserProfile] = useState({
    id: null,
    name: null,
    profileImage: null,
    bgImage: null,
    isLoading: true,
  });

  // 프로필 정보 로드
  const loadProfile = async () => {
    const token = getToken();
    if (!token) {
      setUserProfile({
        id: null,
        name: null,
        profileImage: null,
        bgImage: null,
        isLoading: false,
      });
      return;
    }

    try {
      const { userAPI } = await import("../utils/api");
      const profile = await userAPI.getMyProfile();
      const profileData = await getMyProfile();
      setUserProfile({
        id: profile.id,
        name: profileData.name,
        profileImage: profileData.profileImage,
        bgImage: profileData.bgImage,
        isLoading: false,
      });
    } catch (error) {
      console.error("프로필 로드 실패:", error);
      setUserProfile({
        id: null,
        name: null,
        profileImage: null,
        bgImage: null,
        isLoading: false,
      });
    }
  };

  // 초기 로드 및 토큰 변경 감지
  useEffect(() => {
    loadProfile();
    
    // 토큰 변경 감지를 위한 interval (간단한 방법)
    const interval = setInterval(() => {
      const token = getToken();
      if (token && !userProfile.name) {
        loadProfile();
      } else if (!token && userProfile.name) {
        setUserProfile({
          id: null,
          name: null,
          profileImage: null,
          bgImage: null,
          isLoading: false,
        });
      }
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  // 프로필 업데이트 함수
  const updateProfile = (newProfile) => {
    setUserProfile((prev) => ({
      ...prev,
      ...newProfile,
    }));
  };

  const value = {
    userProfile,
    updateProfile,
    reloadProfile: loadProfile,
  };

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
};

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
};

