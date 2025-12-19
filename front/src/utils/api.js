// API 기본 URL
const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080";

// 토큰 관리
export const getToken = () => {
  return localStorage.getItem("accessToken");
};

export const setToken = (token) => {
  localStorage.setItem("accessToken", token);
};

export const removeToken = () => {
  localStorage.removeItem("accessToken");
};

// API 호출 헬퍼 함수
const apiCall = async (endpoint, options = {}) => {
  const url = `${API_BASE_URL}${endpoint}`;
  const token = getToken();

  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(url, {
      ...options,
      headers,
    });

    let data;
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      try {
        data = await response.json();
      } catch (e) {
        data = {};
      }
    } else {
      const text = await response.text();
      try {
        data = JSON.parse(text);
      } catch {
        data = {};
      }
    }

    if (!response.ok) {
      const errorMsg =
        data.error || data.message || `서버 오류: ${response.status}`;
      if (response.status === 403 || response.status === 401) {
        const authError = new Error(errorMsg);
        authError.isAuthError = true;
        throw authError;
      }
      throw new Error(errorMsg);
    }

    return data;
  } catch (error) {
    if (error.name === "TypeError" && error.message.includes("fetch")) {
      throw new Error("서버에 연결할 수 없습니다.");
    }
    throw error;
  }
};

// 인증 API
export const authAPI = {
  register: async (name, email, password) => {
    return apiCall("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ name, email, password }),
    });
  },

  login: async (email, password) => {
    const response = await apiCall("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });

    if (response.accessToken) {
      setToken(response.accessToken);
    }

    return response;
  },

  resetRequest: async (email) => {
    return apiCall("/api/auth/reset-request", {
      method: "POST",
      body: JSON.stringify({ email }),
    });
  },

  resetConfirm: async (token, newPassword) => {
    return apiCall("/api/auth/reset/confirm", {
      method: "POST",
      body: JSON.stringify({ token, newPassword }),
    });
  },
};

export const movieAPI = {
  getMovieById: async (id) => {
    return apiCall(`/api/v1/movies/${id}`);
  },

  listMovies: async (page = 0, size = 20) => {
    return apiCall(`/api/v1/movies/list?page=${page}&size=${size}`);
  },

  searchMovies: async (keyword, year = null) => {
    const params = new URLSearchParams({ keyword });
    if (year) params.append("year", year);
    return apiCall(`/api/movies/search?${params.toString()}`);
  },

  searchLocalMovies: async (keyword, page = 0, size = 10) => {
    const params = new URLSearchParams();
    if (keyword) params.append("q", keyword);
    params.append("page", page);
    params.append("size", size);
    return apiCall(`/api/v1/movies/search?${params.toString()}`);
  },

  // OTT 제공처 조회
  getAvailability: async (movieId, region = null) => {
    const url = region
      ? `/api/v1/movies/${movieId}/availability?region=${region}`
      : `/api/v1/movies/${movieId}/availability`;
    return apiCall(url);
  },
};

// 박스오피스 API
export const boxOfficeAPI = {
  // 일일 박스오피스 Top10
  getDailyBoxOffice: async () => {
    try {
      const data = await apiCall("/boxoffice/daily");
      // 응답이 배열인지 확인
      return Array.isArray(data) ? data : [];
    } catch (error) {
      return [];
    }
  },
};

// 랭킹 API
export const rankingAPI = {
  // TMDB 글로벌 랭킹
  getGlobalRanking: async () => {
    try {
      const data = await apiCall("/api/ranking/tmdb/global");
      if (!data || !Array.isArray(data) || data.length === 0) {
        try {
          await apiCall("/api/ranking/tmdb/global/refresh", { method: "POST" });
          await new Promise((resolve) => setTimeout(resolve, 3000));
          const refreshed = await apiCall("/api/ranking/tmdb/global");
          return refreshed || [];
        } catch (refreshError) {
          return [];
        }
      }
      return data;
    } catch (error) {
      try {
        await apiCall("/api/ranking/tmdb/global/refresh", { method: "POST" });
        await new Promise((resolve) => setTimeout(resolve, 3000));
        return (await apiCall("/api/ranking/tmdb/global")) || [];
      } catch (refreshError) {
        return [];
      }
    }
  },

  // TMDB 한국 랭킹
  getKoreaRanking: async () => {
    try {
      const data = await apiCall("/api/ranking/tmdb/kr");
      if (!data || !Array.isArray(data) || data.length === 0) {
        try {
          await apiCall("/api/ranking/tmdb/kr/refresh", { method: "POST" });
          await new Promise((resolve) => setTimeout(resolve, 3000));
          const refreshed = await apiCall("/api/ranking/tmdb/kr");
          return refreshed || [];
        } catch (refreshError) {
          return [];
        }
      }
      return data;
    } catch (error) {
      try {
        await apiCall("/api/ranking/tmdb/kr/refresh", { method: "POST" });
        await new Promise((resolve) => setTimeout(resolve, 3000));
        return (await apiCall("/api/ranking/tmdb/kr")) || [];
      } catch (refreshError) {
        return [];
      }
    }
  },

  // TMDB 미국 랭킹
  getUSRanking: async () => {
    try {
      const data = await apiCall("/api/ranking/tmdb/us");
      if (!data || !Array.isArray(data) || data.length === 0) {
        try {
          await apiCall("/api/ranking/tmdb/us/refresh", { method: "POST" });
          await new Promise((resolve) => setTimeout(resolve, 3000));
          const refreshed = await apiCall("/api/ranking/tmdb/us");
          return refreshed || [];
        } catch (refreshError) {
          return [];
        }
      }
      return data;
    } catch (error) {
      try {
        await apiCall("/api/ranking/tmdb/us/refresh", { method: "POST" });
        await new Promise((resolve) => setTimeout(resolve, 3000));
        return (await apiCall("/api/ranking/tmdb/us")) || [];
      } catch (refreshError) {
        return [];
      }
    }
  },
};

// 평점/리뷰 API
export const ratingAPI = {
  // 영화별 평점 목록
  getRatingsByMovie: async (movieId, page = 0, size = 10) => {
    return apiCall(
      `/api/v1/movies/${movieId}/ratings?page=${page}&size=${size}`
    );
  },

  // 내 평점 목록
  getMyRatings: async (page = 0, size = 10) => {
    return apiCall(`/api/v1/users/me/ratings?page=${page}&size=${size}`);
  },

  // 평점 생성/수정
  upsertRating: async (movieId, stars, review = null, spoiler = false) => {
    return apiCall(`/api/v1/movies/${movieId}/ratings`, {
      method: "POST",
      body: JSON.stringify({ stars, review, spoiler }),
    });
  },

  // 평점 삭제
  deleteRating: async (movieId) => {
    return apiCall(`/api/v1/movies/${movieId}/ratings`, {
      method: "DELETE",
    });
  },

  // 최근 평점 조회 (모든 영화)
  getRecentRatings: async (page = 0, size = 10) => {
    return apiCall(`/api/v1/ratings/recent?page=${page}&size=${size}`);
  },

  getMyRatingForMovie: async (movieId) => {
    if (!getToken()) {
      return null;
    }
    try {
      return await apiCall(`/api/v1/movies/${movieId}/ratings/me`);
    } catch (error) {
      if (
        error.message?.includes("404") ||
        error.message?.includes("not found") ||
        error.isAuthError ||
        error.message?.includes("Authorization")
      ) {
        return null;
      }
      throw error;
    }
  },
};

// 사용자 영화 리스트 API
export const userListAPI = {
  getWishlist: async () => {
    if (!getToken()) {
      return [];
    }
    const response = await apiCall("/api/v1/users/me/bucket");
    if (Array.isArray(response)) {
      return response;
    }
    if (response && Array.isArray(response.content)) {
      return response.content;
    }
    return [];
  },

  // 위시리스트 추가
  addToWishlist: async (movieId) => {
    if (!getToken()) {
      throw new Error("인증이 필요합니다. 로그인해주세요.");
    }
    return apiCall(`/api/v1/users/me/bucket/${movieId}`, {
      method: "POST",
    });
  },

  // 위시리스트 삭제
  removeFromWishlist: async (movieId) => {
    if (!getToken()) {
      throw new Error("인증이 필요합니다. 로그인해주세요.");
    }
    return apiCall(`/api/v1/users/me/bucket/${movieId}`, {
      method: "DELETE",
    });
  },

  getWatched: async () => {
    if (!getToken()) {
      return [];
    }
    const response = await apiCall("/api/v1/users/me/watched");
    if (Array.isArray(response)) {
      return response;
    }
    if (response && Array.isArray(response.content)) {
      return response.content;
    }
    return [];
  },

  // 시청한 영화 추가
  addToWatched: async (movieId) => {
    if (!getToken()) {
      throw new Error("인증이 필요합니다. 로그인해주세요.");
    }
    return apiCall(`/api/v1/users/me/watched/${movieId}`, {
      method: "POST",
    });
  },

  // 시청한 영화 삭제
  removeFromWatched: async (movieId) => {
    if (!getToken()) {
      throw new Error("인증이 필요합니다. 로그인해주세요.");
    }
    return apiCall(`/api/v1/users/me/watched/${movieId}`, {
      method: "DELETE",
    });
  },

  getFavorites: async () => {
    if (!getToken()) {
      return [];
    }
    return apiCall("/api/v1/users/me/favorites");
  },

  // 즐겨찾기 추가
  addToFavorites: async (movieId) => {
    if (!getToken()) {
      throw new Error("인증이 필요합니다. 로그인해주세요.");
    }
    return apiCall(`/api/v1/users/me/favorites/${movieId}`, {
      method: "POST",
    });
  },

  // 즐겨찾기 삭제
  removeFromFavorites: async (movieId) => {
    if (!getToken()) {
      throw new Error("인증이 필요합니다. 로그인해주세요.");
    }
    return apiCall(`/api/v1/users/me/favorites/${movieId}`, {
      method: "DELETE",
    });
  },
};

// 코멘트 API
export const commentAPI = {
  // 코멘트 좋아요 토글
  toggleLike: async (commentId) => {
    if (!getToken()) {
      throw new Error("인증이 필요합니다. 로그인해주세요.");
    }
    return apiCall(`/api/comments/${commentId}/like`, {
      method: "POST",
    });
  },
};

// Rating 좋아요 API
export const ratingLikeAPI = {
  // Rating 좋아요 토글
  toggleLike: async (ratingId) => {
    if (!getToken()) {
      throw new Error("인증이 필요합니다. 로그인해주세요.");
    }
    return apiCall(`/api/v1/ratings/${ratingId}/like`, {
      method: "POST",
    });
  },
};

// 사용자 API
export const userAPI = {
  // 내 프로필 정보 조회
  getMyProfile: async () => {
    return apiCall("/api/user/me", {
      method: "GET",
    });
  },

  // 이름 수정
  updateName: async (name) => {
    return apiCall("/api/user/me/name", {
      method: "PUT",
      body: JSON.stringify({ name }),
    });
  },

  // 이미지 업로드
  uploadImage: async (type, file) => {
    const formData = new FormData();
    formData.append("type", type);
    formData.append("file", file);

    const token = getToken();
    const url = `${API_BASE_URL}/api/user/me/upload`;

    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      method: "POST",
      headers,
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(
        error.error || error.message || "이미지 업로드에 실패했습니다."
      );
    }

    return response.json();
  },

  // 평가한 영화 목록
  getRatedMovies: async (userId) => {
    return apiCall(`/api/users/${userId}/rated-movies`);
  },
};

// 추천 API
export const recommendationAPI = {
  // 장르별 추천
  getByGenre: async (genreId, limit = 20, minCount = 3) => {
    return apiCall(
      `/api/v1/recommendations/by-genre?genreId=${genreId}&limit=${limit}&minCount=${minCount}`
    );
  },
};

// 명대사 API
export const quoteAPI = {
  // 랜덤 명대사
  getRandom: async (movieId = null, lang = null) => {
    const params = new URLSearchParams();
    if (movieId) params.append("movieId", movieId);
    if (lang) params.append("lang", lang);
    const query = params.toString();
    return apiCall(`/api/v1/quotes/random${query ? "?" + query : ""}`);
  },

  // 영화별 명대사 목록
  getByMovie: async (movieId, lang = null) => {
    const url = lang
      ? `/api/v1/quotes/movie/${movieId}?lang=${lang}`
      : `/api/v1/quotes/movie/${movieId}`;
    return apiCall(url);
  },
};

export default apiCall;
