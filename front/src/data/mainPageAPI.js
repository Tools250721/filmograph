import { boxOfficeAPI, rankingAPI, ratingAPI } from "../utils/api";

// 백엔드 응답을 프론트엔드 형식으로 변환
const transformMovie = (movie) => {
  if (!movie) return null;

  return {
    id: movie.id,
    title: movie.title || movie.originalTitle,
    originalTitle: movie.originalTitle,
    year: movie.releaseYear || movie.releaseDate?.split("-")[0],
    poster: movie.posterUrl,
    backdropUrl: movie.backdropUrl,
    rating: movie.averageRating || 0,
    voteCount: movie.totalRatings || 0,
    genres:
      movie.genres?.map((g) => (typeof g === "string" ? g : g.name)) || [],
    country: movie.country || movie.nation,
    duration: movie.runtimeMinutes,
    ageRating: movie.ageRating,
  };
};

// TmdbItem을 프론트엔드 형식으로 변환 (랭킹 API용)
const transformTmdbItem = async (item) => {
  if (!item) {
    return null;
  }

  // movieId가 있으면 실제 영화 ID 사용, 없으면 null
  const movieId = item.movieId || null;

  // posterUrl 확인 - null이거나 빈 문자열이면 빈 문자열로 처리
  const posterUrl = item.posterUrl || "";

  // movieId가 null이면 id도 null로 설정 (잘못된 ID 사용 방지)
  const actualMovieId = item.movieId || movieId || null;
  const actualId = actualMovieId || null; // movieId가 null이면 id도 null

  // movieId가 있으면 줄거리와 OTT 정보 가져오기
  let overview = "";
  let ott = [];
  if (actualMovieId) {
    try {
      const { movieAPI } = await import("../utils/api");
      const detail = await movieAPI.getMovieById(actualMovieId);
      if (detail) {
        overview = detail.overview || detail.info?.overview || "";
        // OTT 정보 가져오기
        try {
          const ottData = await movieAPI.getAvailability(actualMovieId);
          if (Array.isArray(ottData)) {
            ott = ottData
              .map((item) => item.name || item.providerName || "")
              .filter((name) => name);
          }
        } catch (ottError) {
          // OTT 정보 가져오기 실패는 무시
        }
      }
    } catch (error) {
      // 상세 정보 가져오기 실패는 무시
    }
  }

  const result = {
    // id는 movieId와 동일하게 설정 (movieId가 null이면 id도 null)
    id: actualId,
    title: item.title || "",
    originalTitle: item.title || "",
    year: null, // TmdbItem에는 연도 정보가 없음
    poster: posterUrl || null, // posterUrl이 빈 문자열이면 null로 처리
    backdropUrl: null,
    rating: 0,
    voteCount: 0,
    genres: [],
    country: null,
    duration: null,
    ageRating: null,
    rank: item.rank, // 순위 정보 추가
    movieId: actualMovieId, // 실제 영화 ID (있으면 사용, 없으면 null)
    overview: overview, // 줄거리
    ott: ott, // OTT 정보
  };

  return result;
};

export const getBoxOffice = async () => {
  const maxRetries = 3;
  let lastError = null;

  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      const data = await boxOfficeAPI.getDailyBoxOffice();

      if (!data || !Array.isArray(data)) {
        if (attempt < maxRetries - 1) {
          await new Promise((resolve) =>
            setTimeout(resolve, (attempt + 1) * 2000)
          );
          continue;
        }
        return [];
      }

      // BoxOfficeDto는 다른 구조를 가지고 있으므로 별도 변환
      const result = await Promise.all(
        data.map(async (item, index) => {
          if (!item) return null;

          // 백엔드 응답에서 posterUrl과 movieId 확인 (null일 수 있음)
          let poster = item.posterUrl || null;
          let movieId = item.movieId || null;

          // posterUrl이 없고 movieId가 있으면 상세페이지 API로 포스터 가져오기
          if (!poster && movieId) {
            try {
              const { getMovieById } = await import("./movieAPI");
              const movieDetail = await getMovieById(movieId);
              if (movieDetail && movieDetail.info && movieDetail.info.poster) {
                poster = movieDetail.info.poster;
              }
            } catch (error) {
              // 상세페이지 API 호출 실패는 무시
            }
          }

          // posterUrl과 movieId가 모두 없으면 영화 제목으로 검색해서 포스터 가져오기
          if (!poster && item.movieNm) {
            try {
              const { movieAPI } = await import("../utils/api");
              const searchResponse = await movieAPI.searchLocalMovies(
                item.movieNm,
                0,
                3
              );
              const searchResults =
                searchResponse?.content || searchResponse || [];

              if (Array.isArray(searchResults) && searchResults.length > 0) {
                // 첫 번째 검색 결과 사용
                const foundMovie = searchResults[0];
                if (foundMovie.posterUrl) {
                  poster = foundMovie.posterUrl;
                  // movieId도 업데이트
                  if (!movieId && foundMovie.id) {
                    movieId = foundMovie.id;
                  }
                }
              }
            } catch (error) {
              // 검색 실패는 무시
            }
          }

          // movieId가 있으면 줄거리, OTT 정보, 평균 별점 가져오기
          let overview = "";
          let ott = [];
          let rating = 0;
          if (movieId) {
            try {
              const { movieAPI } = await import("../utils/api");
              const detail = await movieAPI.getMovieById(movieId);
              if (detail) {
                overview = detail.overview || detail.info?.overview || "";
                // 평균 별점 가져오기
                rating = detail.stats?.avgRating || detail.info?.rating || 0;
                // OTT 정보 가져오기
                try {
                  const ottData = await movieAPI.getAvailability(movieId);
                  if (Array.isArray(ottData)) {
                    ott = ottData
                      .map((item) => item.name || item.providerName || "")
                      .filter((name) => name);
                  }
                } catch (ottError) {
                  // OTT 정보 가져오기 실패는 무시
                }
              }
            } catch (error) {
              // 상세 정보 가져오기 실패는 무시
            }
          }

          return {
            id: movieId || null, // movieId가 없으면 null (클릭 시 검색으로 처리)
            rank: item.rank || index + 1,
            title: item.movieNm || "제목 없음",
            year: item.openDt ? item.openDt.split("-")[0] : null,
            country: "KR",
            rating: rating,
            poster: poster, // 백엔드, 상세페이지, 또는 검색에서 가져온 posterUrl 사용
            salesAcc: item.salesAcc,
            audiAcc: item.audiAcc,
            overview: overview,
            ott: ott,
          };
        })
      );

      return Array.isArray(result) ? result.filter(Boolean) : [];
    } catch (error) {
      lastError = error;
      if (attempt < maxRetries - 1) {
        await new Promise((resolve) =>
          setTimeout(resolve, (attempt + 1) * 2000)
        );
      }
    }
  }

  return [];
};

// 백엔드 API 매핑:
// - 넷플릭스 = global (TMDB 글로벌 트렌딩)
// - 왓챠 = kr (TMDB 한국 인기)
// - TVING = us (TMDB 미국 인기)

export const getNetflixRanking = async () => {
  const maxRetries = 3;
  let lastError = null;

  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      let data = await rankingAPI.getGlobalRanking();

      if (Array.isArray(data) && data.length > 0) {
        const transformed = await Promise.all(
          data.map((item) => transformTmdbItem(item))
        );
        return transformed.filter(Boolean).slice(0, 10);
      }

      if (attempt < maxRetries - 1) {
        await new Promise((resolve) =>
          setTimeout(resolve, (attempt + 1) * 2000)
        );
      }
    } catch (error) {
      lastError = error;
      if (attempt < maxRetries - 1) {
        await new Promise((resolve) =>
          setTimeout(resolve, (attempt + 1) * 2000)
        );
      }
    }
  }

  return [];
};

export const getWatchaRanking = async () => {
  const maxRetries = 3;
  let lastError = null;

  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      let data = await rankingAPI.getKoreaRanking();

      if (Array.isArray(data) && data.length > 0) {
        const transformed = await Promise.all(
          data.map((item) => transformTmdbItem(item))
        );
        return transformed.filter(Boolean).slice(0, 10);
      }

      if (attempt < maxRetries - 1) {
        await new Promise((resolve) =>
          setTimeout(resolve, (attempt + 1) * 2000)
        );
      }
    } catch (error) {
      lastError = error;
      if (attempt < maxRetries - 1) {
        await new Promise((resolve) =>
          setTimeout(resolve, (attempt + 1) * 2000)
        );
      }
    }
  }

  return [];
};

export const getTvingRanking = async () => {
  const maxRetries = 3;
  let lastError = null;

  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      let data = await rankingAPI.getUSRanking();

      if (Array.isArray(data) && data.length > 0) {
        const transformed = await Promise.all(
          data.map((item) => transformTmdbItem(item))
        );
        return transformed.filter(Boolean).slice(0, 10);
      }

      if (attempt < maxRetries - 1) {
        await new Promise((resolve) =>
          setTimeout(resolve, (attempt + 1) * 2000)
        );
      }
    } catch (error) {
      lastError = error;
      if (attempt < maxRetries - 1) {
        await new Promise((resolve) =>
          setTimeout(resolve, (attempt + 1) * 2000)
        );
      }
    }
  }

  return [];
};


export const getWishlist = async () => {
  try {
    const { userListAPI, getToken } = await import("../utils/api");
    // 로그인하지 않은 경우 빈 배열 반환
    if (!getToken()) {
      return [];
    }

    const data = await userListAPI.getWishlist();

    if (!Array.isArray(data) || data.length === 0) {
      return [];
    }

    return data.map((movie) => transformMovie(movie)).filter(Boolean);
  } catch (error) {
    // 인증 오류는 무시하고 빈 배열 반환 (로그인하지 않은 사용자)
    if (error.message && error.message.includes("인증")) {
      return [];
    }
    console.error("위시리스트 조회 실패:", error);
    return [];
  }
};

export const getMainComments = async () => {
  try {
    // 내 코멘트만 가져오기 (최근 10개)
    const response = await ratingAPI.getMyRatings(0, 10);
    let rawList = [];
    if (response && Array.isArray(response.content)) {
      rawList = response.content;
    } else if (Array.isArray(response)) {
      rawList = response;
    }

    if (rawList.length === 0) return [];

    // 영화 정보 결합
    const commentsWithMovieInfo = await Promise.all(
      rawList.map(async (r) => {
        try {
          const { movieAPI } = await import("../utils/api");
          const movie = await movieAPI.getMovieById(r.movieId);
          return {
            id: r.id,
            user: r.userName || "익명",
            userName: r.userName || "익명",
            userId: r.userId, // userId 추가
            text: r.review || "",
            content: r.review || "",
            rating: r.stars,
            date: r.createdAt,
            likes: r.likeCount ?? 0,
            movieId: r.movieId,
            movieTitle: movie?.title || "영화 정보 없음",
            poster: movie?.posterUrl || null,
          };
        } catch (error) {
          return {
            id: r.id,
            user: r.userName || "익명",
            userName: r.userName || "익명",
            userId: r.userId, // userId 추가
            text: r.review || "",
            content: r.review || "",
            rating: r.stars,
            date: r.createdAt,
            likes: r.likeCount ?? 0,
            movieId: r.movieId,
            movieTitle: "영화 정보 로드 실패",
            poster: null,
          };
        }
      })
    );

    return commentsWithMovieInfo;
  } catch (error) {
    // 인증 에러 등은 빈 배열 반환
    if (error.isAuthError || error.message?.includes("401") || error.message?.includes("403")) {
      return [];
    }
    throw error;
  }
};
