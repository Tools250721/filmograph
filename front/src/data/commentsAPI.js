import { ratingAPI } from "../utils/api";

export const getAllComments = async (movieId = null) => {
  try {
    if (movieId) {
      // 특정 영화의 평점 조회
      const response = await ratingAPI.getRatingsByMovie(movieId, 0, 100);
      const content = response?.content || response || [];
      return Array.isArray(content)
        ? content.map((r) => ({
            id: r.id,
            movieId: movieId,
            user: r.userName || "익명",
            text: r.review || "",
            rating: r.stars,
            date: r.createdAt,
            likes: r.likeCount ?? 0,
            isLiked: r.isLiked ?? false,
            spoiler: r.spoiler || false,
          }))
        : [];
    } else {
      // 전체 최근 평점 조회
      const response = await ratingAPI.getRecentRatings(0, 100);
      const content = response?.content || response || [];
      return Array.isArray(content)
        ? content.map((r) => ({
            id: r.id,
            movieId: r.movieId,
            user: r.userName || "익명",
            text: r.review || "",
            rating: r.stars,
            date: r.createdAt,
            likes: r.likeCount ?? 0,
            isLiked: r.isLiked ?? false,
            spoiler: r.spoiler || false,
          }))
        : [];
    }
  } catch (error) {
    return [];
  }
};
