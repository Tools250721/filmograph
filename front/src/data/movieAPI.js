import { movieAPI, ratingAPI, quoteAPI } from "../utils/api";

export const getMovieById = async (id) => {
  try {
    // 영화 상세 정보 조회
    const movieDetail = await movieAPI.getMovieById(id);

    if (!movieDetail) {
      return null;
    }

    // 평점 목록 조회 (히스토그램 계산을 위해 더 많이 가져옴)
    let ratings = [];
    try {
      const ratingsResponse = await ratingAPI.getRatingsByMovie(id, 0, 1000);
      ratings = ratingsResponse?.content || [];
    } catch (err) {
      // 평점 조회 실패는 무시
    }

    // 히스토그램 계산 (0.5점 단위로 10개 구간: 0.0~0.5, 0.5~1.0, ..., 4.5~5.0)
    const calculateHistogram = (ratings) => {
      const histogram = new Array(10).fill(0);
      ratings.forEach((rating) => {
        let stars = rating.stars || 0;
        // 각 구간은 [min, max) 형태로 계산
        // 0.0~0.5: idx 0, 0.5~1.0: idx 1, ..., 4.5~5.0: idx 9
        // Math.floor를 사용하여 해당 구간에 배치
        const index = Math.min(Math.floor(stars * 2), 9);
        if (index >= 0 && index < 10) {
          histogram[index]++;
        }
      });
      return histogram;
    };
    const histogram = calculateHistogram(ratings);

    // 명대사 조회
    let quotes = [];
    try {
      const quotesResponse = await quoteAPI.getByMovie(id);
      quotes = quotesResponse?.quotes || [];
    } catch (err) {
      // 명대사 조회 실패는 무시
    }

    // 백엔드 응답을 프론트엔드 형식으로 변환
    return {
      hero: {
        id: movieDetail.id,
        title: movieDetail.title || "제목 없음",
        originalTitle: movieDetail.originalTitle || "",
        year: movieDetail.releaseYear,
        genres:
          movieDetail.genres?.map((g) =>
            typeof g === "string" ? g : g.name
          ) || [],
        country: movieDetail.country || "",
        duration: movieDetail.runtimeMinutes,
        ageRating: movieDetail.ageRating || "",
        imageUrl: movieDetail.backdropUrl || movieDetail.posterUrl || "",
        backdropUrl: movieDetail.backdropUrl || "",
      },
      info: {
        id: movieDetail.id,
        title: movieDetail.title,
        originalTitle: movieDetail.originalTitle,
        overview: movieDetail.overview,
        year: movieDetail.releaseYear,
        runtimeMinutes: movieDetail.runtimeMinutes,
        country: movieDetail.country,
        ageRating: movieDetail.ageRating,
        poster: movieDetail.posterUrl,
        backdropUrl: movieDetail.backdropUrl,
        genres: movieDetail.genres?.map((g) => g.name) || [],
        rating: movieDetail.stats?.avgRating || 0,
        voteCount: movieDetail.stats?.ratingCount || 0,
        histogram: histogram, // 실제 히스토그램 데이터
      },
      ott: movieDetail.ott || [],
      cast: (() => {
        const directors = (movieDetail.directors || []).map((d) => ({
          id: d.id,
          name: d.name || "",
          role: "감독",
          job: "감독",
          photo: d.profileUrl || null,
          profileUrl: d.profileUrl || null,
        }));
        const actors = (movieDetail.actors || []).map((a) => ({
          id: a.id,
          name: a.name || "",
          character: a.character || null,
          role: "배우",
          photo: a.profileUrl || null,
          profileUrl: a.profileUrl || null,
        }));
        return [...directors, ...actors];
      })(),
      directors: movieDetail.directors || [],
      comments: ratings.map((r) => ({
        id: r.id,
        user: r.userName || "익명",
        text: r.review || "",
        rating: r.stars,
        date: r.createdAt,
        likes: r.likeCount ?? 0,
        isLiked: r.isLiked ?? false,
        spoiler: r.spoiler || false,
      })),
      gallery: (movieDetail.stills || []).map((still) => ({
        name: still.url || "",
        url: still.url || "",
        width: still.width || 0,
        height: still.height || 0,
        aspectRatio: still.aspectRatio || 16 / 9,
      })),
      quotes: quotes,
    };
  } catch (error) {
    return null;
  }
};
