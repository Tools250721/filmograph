import ImageWithFallback from "../../../../components/ui/ImageWithFallback";
import "./PersonCard.css";

const AVATAR = 56;

export default function PersonCard({
  name,
  job,
  character,
  photo,
  withDivider = false,
}) {
  const subline = [job, character].filter(Boolean).join(" | ");

  return (
    <div className={`person-card ${withDivider ? "person-card--divider" : ""}`}>
      <ImageWithFallback
        src={photo}
        alt={name}
        width={`${AVATAR}px`}
        height={`${AVATAR}px`}
        className="person-card__avatar"
        placeholder={name ? name.charAt(0) : "?"}
        style={{
          objectFit: "cover",
        }}
      />

      <div className="person-card__info">
        <div className="person-card__name" title={name}>
          {name}
        </div>
        <div className="person-card__meta" title={subline}>
          {subline}
        </div>
      </div>
    </div>
  );
}
