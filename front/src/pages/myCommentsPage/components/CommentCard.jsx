import React from "react";
import { useNavigate } from "react-router-dom";
import { ReactComponent as UserIcon } from "../../../assets/icons/UserIcon.svg";
import { ReactComponent as HeartIcon } from "../../../assets/icons/HeartIcon.svg";
import { ReactComponent as MessageIcon } from "../../../assets/icons/MessageIcon.svg";
import { ReactComponent as EditIcon } from "../../../assets/icons/EditIcon.svg";
import { ReactComponent as DeleteIcon } from "../../../assets/icons/DeleteIcon.svg";
import ImageWithFallback from "../../../components/ui/ImageWithFallback";

const CommentCard = ({
  movieId,
  userName,
  movieTitle,
  year,
  avgRating,
  userRating,
  poster,
  content,
  onEdit,
  onDelete,
}) => {
  const navigate = useNavigate();

  const handleMovieClick = () => {
    navigate(`/movie/${movieId}`);
  };

  return (
    <div
      style={{
        backgroundColor: "#f6f6f6",
        borderRadius: "8px",
        padding: "20px",
        display: "flex",
        flexDirection: "column",
      }}
    >
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "10px",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <UserIcon style={{ width: "30px", height: "30px" }} />
          <span
            style={{ fontSize: "15px", fontWeight: "600", color: "#7E7E7E" }}
          >
            {userName}
          </span>
        </div>
        <span style={{ fontSize: "14px", color: "#7E7E7E", fontWeight: "500" }}>
          ★ {userRating.toFixed(1)}
        </span>
      </div>

      <div
        style={{
          borderTop: "1px solid #EDEDED",
          width: "100%",
          marginBottom: "15px",
        }}
      />

      <div
        onClick={handleMovieClick}
        style={{ display: "flex", gap: "12px", cursor: "pointer" }}
      >
        <ImageWithFallback
          src={poster}
          alt={movieTitle}
          placeholder="이미지 없음"
          style={{
            width: "60px",
            height: "85px",
            borderRadius: "5px",
            objectFit: "cover",
          }}
        />
        <div
          style={{
            display: "flex",
            flexDirection: "column",
            justifyContent: "flex-start",
          }}
        >
          <p style={{ fontSize: "14px", fontWeight: "600", margin: 0 }}>
            {movieTitle}
          </p>
          <p style={{ fontSize: "14px", color: "#777", margin: "2px 0" }}>
            영화 · {year}
          </p>
          <p style={{ fontSize: "12px", color: "#777", margin: 0 }}>
            평균 {avgRating.toFixed(1)}
          </p>
        </div>
      </div>

      <p style={{ fontSize: "14px", margin: "20px 0", color: "#333" }}>
        {content}
      </p>

      <div
        style={{
          borderTop: "1px solid #EDEDED",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          padding: "15px 0 5px 0",
        }}
      >
        <div style={{ display: "flex", gap: "16px" }}>
          <HeartIcon
            style={{ width: "22px", height: "22px", cursor: "pointer" }}
          />
          <MessageIcon
            style={{ width: "22px", height: "22px", cursor: "pointer" }}
          />
        </div>
        <div style={{ display: "flex", gap: "12px" }}>
          <EditIcon
            onClick={onEdit}
            style={{ width: "20px", height: "20px", cursor: "pointer" }}
          />
          <DeleteIcon
            onClick={onDelete}
            style={{ width: "22px", height: "22px", cursor: "pointer" }}
          />
        </div>
      </div>
    </div>
  );
};

export default CommentCard;
