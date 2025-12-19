import React from "react";
import { ottPlatforms } from "../../../data/ottPlatforms";
import ImageWithFallback from "../../../components/ui/ImageWithFallback";
import "./SearchResultCard.css";

// OTT 이름을 정규화하는 함수
const normalizeOttName = (name) => {
  if (!name) return "";
  return name.toLowerCase().trim();
};

// OTT 이름을 플랫폼 이름으로 매핑
const mapOttNameToPlatform = (ottName) => {
  if (!ottName) return null;

  const normalized = normalizeOttName(ottName);

  // 제외할 플랫폼 (먼저 체크)
  if (
    normalized.includes("apple") ||
    normalized.includes("prime") ||
    normalized.includes("amazon") ||
    normalized.includes(" for ad") ||
    normalized.includes("for ad") ||
    (normalized.includes("netflix") && normalized.includes("ad"))
  ) {
    return null;
  }

  // 정확한 매핑 규칙 (백엔드에서 오는 이름 기준)
  if (normalized === "netflix" || normalized.includes("netflix"))
    return "Netflix";
  if (
    normalized === "disney+" ||
    normalized === "disney plus" ||
    normalized.includes("disney")
  )
    return "Disney+";
  if (normalized === "watcha" || normalized.includes("watcha")) return "Watcha";
  if (
    normalized === "tving" ||
    normalized === "t ving" ||
    normalized.includes("tving")
  )
    return "Tving";
  if (normalized.includes("coupang")) return "Coupang Play";
  if (normalized === "wavve" || normalized.includes("wavve")) return "Wavve";

  return null;
};

export default function SearchResultItem({
  poster,
  title,
  description,
  creator,
  ott = [],
  withDivider = true,
}) {
  // OTT 이름을 플랫폼 이름으로 매핑하고 중복 제거
  // ott는 문자열 배열 또는 OttDto 객체 배열일 수 있음
  const matchedOTTs = React.useMemo(() => {
    const seenPlatforms = new Set();
    const platformMap = new Map();

    ott.forEach((ottItem) => {
      // ottItem이 문자열인지 객체인지 확인
      const ottName =
        typeof ottItem === "string"
          ? ottItem
          : ottItem.name || ottItem.providerName || "";

      const platformName = mapOttNameToPlatform(ottName);
      if (platformName && !seenPlatforms.has(platformName)) {
        seenPlatforms.add(platformName);
        const platform = ottPlatforms.find((p) => p.name === platformName);
        if (platform) {
          platformMap.set(platformName, platform);
        }
      }
    });

    return Array.from(platformMap.values());
  }, [ott]);

  return (
    <div
      className={`search-result-card ${
        withDivider ? "search-result-card--with-divider" : ""
      }`}
    >
      {/* 포스터 */}
      <ImageWithFallback
        src={poster}
        alt={title}
        className="search-result-card__poster"
        placeholder="이미지 없음"
      />

      {/* 텍스트 + OTT */}
      <div className="search-result-card__content">
        <div className="search-result-card__title">{title}</div>
        {/* 검색 페이지에서는 줄거리 표시 안 함 */}
        {creator && (
          <div className="search-result-card__creator">{creator}</div>
        )}

        {/* OTT 미니 아이콘 */}
        <div className="search-result-card__ott-list">
          {matchedOTTs.map((platform) => (
            <div key={platform.name} className="search-result-card__ott-item">
              <img
                src={platform.miniLogo}
                alt={platform.name}
                title={platform.name}
                className="search-result-card__ott-icon"
              />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
