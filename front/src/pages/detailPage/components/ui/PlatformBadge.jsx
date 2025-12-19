import "./PlatformBadge.css";

const PlatformBadge = ({ name, color }) => (
  <div className="platform-badge">
    <div
      className="platform-badge__dot"
      style={{ backgroundColor: color }}
      aria-hidden="true"
    ></div>
    {name}
  </div>
);

export default PlatformBadge;
