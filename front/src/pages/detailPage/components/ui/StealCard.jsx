import "./StealCard.css";
import ImageWithFallback from "../../../../components/ui/ImageWithFallback";

export default function StealCard({ name, photo, url }) {
  const imageUrl = url || photo || "";
  return (
    <div className="steal-card">
      <ImageWithFallback
        src={imageUrl}
        alt={name || "스틸컷"}
        className="steal-card__image"
        placeholder="이미지 없음"
      />
    </div>
  );
}
