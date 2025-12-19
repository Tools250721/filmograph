import { ratingAPI, userListAPI } from "../utils/api";
import { movieAPI } from "../utils/api";

// 영화 정보 변환
const transformMovieForList = (movie) => {
  if (!movie) return null;

  return {
    id: movie.id,
    title: movie.title || movie.originalTitle,
    poster: movie.posterUrl || movie.poster,
    year: movie.releaseYear || movie.releaseDate?.split("-")[0],
    country: movie.country || movie.nation,
  };
};

/* 내가 쓴 코멘트 목록 */
export const getMyComments = async () => {
  try {
    const response = await ratingAPI.getMyRatings(0, 100);
    const ratings = response.content || [];

    // 각 평점에 대한 영화 정보 조회
    const commentsWithMovieInfo = await Promise.all(
      ratings.map(async (rating) => {
        try {
          const movie = await movieAPI.getMovieById(rating.movieId);
          return {
            id: rating.id,
            movieId: rating.movieId,
            userName: rating.userName || "익명",
            movieTitle: movie?.title || "알 수 없음",
            year: movie?.releaseYear,
            avgRating: movie?.stats?.avgRating || 0,
            userRating: rating.stars,
            poster: movie?.posterUrl,
            content: rating.review || "",
            date: rating.createdAt,
            likes: rating.likeCount ?? 0,
            isLiked: rating.isLiked ?? false,
            spoiler: rating.spoiler || false,
          };
        } catch (error) {
          return null;
        }
      })
    );

    return commentsWithMovieInfo.filter(Boolean);
  } catch (error) {
    return [];
  }
};

/* 내가 평가한 영화 목록 */
export const getMyRatedMovies = async () => {
  try {
    const response = await ratingAPI.getMyRatings(0, 100);
    const ratings = response.content || [];

    const ratedMovies = await Promise.all(
      ratings.map(async (rating) => {
        try {
          const movie = await movieAPI.getMovieById(rating.movieId);
          return {
            id: rating.movieId,
            title: movie?.title || "알 수 없음",
            poster: movie?.posterUrl,
            rating: rating.stars,
            dateRated: rating.createdAt,
          };
        } catch (error) {
          return null;
        }
      })
    );

    // 중복 제거
    const uniqueRatedMovies = Array.from(
      new Map(
        ratedMovies.filter(Boolean).map((movie) => [movie.id, movie])
      ).values()
    );

    return uniqueRatedMovies;
  } catch (error) {
    return [];
  }
};

/* 보고싶어요한 영화 목록 */
export const getMyWatchlist = async () => {
  try {
    const data = await userListAPI.getWishlist();

    if (!Array.isArray(data)) {
      return [];
    }

        return data
      .map((movie) => {
        const transformed = transformMovieForList(movie);
        if (!transformed) return null;
        return {
          ...transformed,
          dateAdded: movie.createdAt || movie.dateAdded || new Date().toISOString(),
        };
      })
      .filter(Boolean);
  } catch (error) {
    return [];
  }
};

/* 보는 중 목록 */
export const getMyWatchingList = async () => {
  try {
    const data = await userListAPI.getWatched();

    if (!Array.isArray(data)) {
      return [];
    }

        return data
      .map((movie) => {
        const transformed = transformMovieForList(movie);
        if (!transformed) return null;
        return {
          ...transformed,
          dateAdded: movie.createdAt || movie.dateAdded || new Date().toISOString(),
        };
      })
      .filter(Boolean);
  } catch (error) {
    return [];
  }
};

/* 마이페이지 사용자 정보 */
export const getMyProfile = async () => {
  try {
    const { userAPI, getToken } = await import("../utils/api");
    // 로그인하지 않은 경우 기본값 반환
    if (!getToken()) {
      return {
        name: "사용자",
        profileImage: null,
        bgImage: null,
      };
    }

    const response = await userAPI.getMyProfile();
    return {
      name: response.name || "",
      profileImage: response.profileImageUrl || null,
      bgImage: response.backgroundImageUrl || null,
    };
  } catch (error) {
    return {
      name: "사용자",
      profileImage: null,
      bgImage: null,
    };
  }
};
