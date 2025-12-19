import React, { useState, useEffect } from "react";
import RatingModal from "../../common/RatingModal";
import RatingStars from "./ui/RatingStars";
import RatingSummary from "./ui/RatingSummary";
import RatingHistogram from "./ui/RatingHistogram";
import ActionButtons from "./ui/ActionButtons";
import UserCommentCard from "./ui/UserCommentCard";
import ImageWithFallback from "../../../components/ui/ImageWithFallback";
import { MovieInfoSkeleton } from "../../../components/ui/Skeleton";
import "./MovieInfoSection.css";

export default function MovieInfoSection({
  movieInfo,
  movieId,
  userRating,
  userComment = "",
  userSpoiler = false,
  onRatingChange,
  onToggleWishlist,
  onToggleWatching,
  isLoggedIn = false,
  isWishlisted = false,
  isWatching = false,
  isRatingUpdating = false,
  onAuthRequired,
}) {
  const formatVoteCount = (voteCount) => {
    if (voteCount == null) return "-";

    // 1만 이상
    if (voteCount >= 10000) {
      return `${(voteCount / 10000).toFixed(1)}만명`;
    }

    return `${voteCount.toLocaleString("ko-KR")}명`;
  };

  const avgRating = movieInfo?.rating ?? 0;
  const avgCountLabel = formatVoteCount(movieInfo?.voteCount);
  const overview = movieInfo?.overview || "";
  const synopsis = movieInfo?.synopsis || [];
  // 히스토그램 데이터 (실제 데이터 또는 빈 배열)
  const histogram = movieInfo?.histogram || new Array(10).fill(0);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [startStep, setStartStep] = useState("rating");
  const [myComment, setMyComment] = useState(userComment || "");
  const [mySpoiler, setMySpoiler] = useState(false);

  // userComment prop이 변경되면 state 업데이트
  useEffect(() => {
    setMyComment(userComment || "");
    setMySpoiler(userSpoiler || false);
  }, [userComment, userSpoiler]);

  useEffect(() => {
    if (userRating === 0) {
      setMyComment("");
      setMySpoiler(false);
    }
  }, [userRating]);

  const handleSubmitRating = async ({ rating, comment, spoiler }) => {
    if (!isLoggedIn) {
      onAuthRequired?.();
      return;
    }

    const normalizedRating = rating / 2;
    await onRatingChange(normalizedRating, comment || null, spoiler || false);
    setMyComment(comment || "");
    setMySpoiler(spoiler || false);
  };

  const handleDeleteComment = async () => {
    if (!isLoggedIn) {
      onAuthRequired?.();
      return;
    }
    await onRatingChange(0);
    setMyComment("");
    setMySpoiler(false);
  };

  if (!movieInfo) {
    return <MovieInfoSkeleton />;
  }

  return (
    <section className="movie-info">
      <div className="movie-info__main">
        <div className="movie-info__hero">
          <div className="movie-info__poster-column">
            <ImageWithFallback
              src={movieInfo.poster}
              alt={`${movieInfo.title} 포스터`}
              className="movie-info__poster"
              placeholder="이미지 없음"
            />
            <div className="movie-info__histogram">
              <RatingHistogram
                histogram={histogram}
                avgRating={avgRating}
                avgCountLabel={avgCountLabel}
              />
            </div>
          </div>

          <div className="movie-info__summary-column">
            <div className="movie-info__summary-row">
              <div className="movie-info__rating-star">
                <RatingStars
                  myRating={userRating * 2}
                  onClick={() => {
                    if (!isLoggedIn) {
                      onAuthRequired?.();
                      return;
                    }
                    setIsModalOpen(true);
                    setStartStep("rating");
                  }}
                />
                <div className="movie-info__rating-label">평가하기</div>
              </div>

              {isRatingUpdating ? (
                <div style={{ 
                  display: "flex", 
                  alignItems: "center", 
                  justifyContent: "center",
                  minHeight: "60px"
                }}>
                  <div style={{
                    width: "20px",
                    height: "20px",
                    border: "2px solid #f3f3f3",
                    borderTop: "2px solid #678d77",
                    borderRadius: "50%",
                    animation: "spin 1s linear infinite"
                  }}></div>
                  <style>{`
                    @keyframes spin {
                      0% { transform: rotate(0deg); }
                      100% { transform: rotate(360deg); }
                    }
                  `}</style>
                </div>
              ) : (
                <RatingSummary
                  myRating={userRating * 2}
                  avgRating={avgRating}
                  avgCountLabel={avgCountLabel}
                />
              )}

              <div className="movie-info__actions-row">
                <ActionButtons
                  onToggleWishlist={onToggleWishlist}
                  onToggleWatching={onToggleWatching}
                  isLoggedIn={isLoggedIn}
                  isWishlisted={isWishlisted}
                  isWatching={isWatching}
                  onAuthRequired={onAuthRequired}
                />
              </div>
            </div>

            <div className="movie-info__body">
              {userRating > 0 ? (
                <UserCommentCard
                  myRating={userRating * 2}
                  myComment={myComment}
                  onEdit={() => {
                    setStartStep("comment");
                    setIsModalOpen(true);
                  }}
                  onDelete={handleDeleteComment}
                />
              ) : (
                <div className="movie-info__empty-comment">
                  아직 작성한 코멘트가 없습니다. 별점을 남겨보세요!
                </div>
              )}

              <div className="movie-info__synopsis">
                {overview ? (
                  <p className="movie-info__synopsis-paragraph">{overview}</p>
                ) : synopsis.length > 0 ? (
                  synopsis.map((p, idx) => (
                    <p key={idx} className="movie-info__synopsis-paragraph">
                      {p}
                    </p>
                  ))
                ) : (
                  <p
                    className="movie-info__synopsis-paragraph"
                    style={{ color: "#999", fontSize: "14px" }}
                  >
                    줄거리 정보가 없습니다.
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Rating Modal */}
      <RatingModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleSubmitRating}
        movieTitle={movieInfo?.title}
        movieId={movieId}
        onAddToWishlist={async () => {
          if (onToggleWishlist) {
            await onToggleWishlist();
          }
        }}
        initialRating={userRating * 2}
        initialComment={myComment}
        initialSpoiler={userSpoiler || mySpoiler}
        startStep={startStep}
      />
    </section>
  );
}
