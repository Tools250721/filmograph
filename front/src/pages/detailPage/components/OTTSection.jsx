import React from "react";
import "./OTTSection.css";
import { ottPlatforms } from "../../../data/ottPlatforms";
import ImageWithFallback from "../../../components/ui/ImageWithFallback";

// 허용된 OTT 플랫폼 목록 (소문자로 정규화)
const ALLOWED_OTTS = [
  "netflix",
  "disney+",
  "disney plus",
  "watcha",
  "tving",
  "coupang play",
  "coupangplay",
  "wavve",
];

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
    normalized.includes(" for ad") || // "Netflix for ad" 제외
    normalized.includes("for ad") || // "Netflix for ad" 제외
    (normalized.includes("netflix") && normalized.includes("ad")) // Netflix ad 버전 제외
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

export default function OTTSection({ data = [] }) {
  // 허용된 OTT만 필터링하고 로컬 로고로 매핑
  const filteredOtts = React.useMemo(() => {
    const seenPlatforms = new Set(); // 중복 제거를 위한 Set

    return data
      .filter((ott) => {
        const name = normalizeOttName(ott.name || ott.providerName || "");
        // "for ad" 또는 "ad"가 포함된 경우 제외
        if (
          name.includes(" for ad") ||
          name.includes("for ad") ||
          (name.includes("netflix") && name.includes("ad"))
        ) {
          return false;
        }
        return ALLOWED_OTTS.some((allowed) => name.includes(allowed));
      })
      .map((ott) => {
        const platformName = mapOttNameToPlatform(
          ott.name || ott.providerName || ""
        );
        const platform = ottPlatforms.find((p) => p.name === platformName);

        return {
          ...ott,
          platformName,
          localLogo: platform?.logo || null,
        };
      })
      .filter((ott) => {
        // 로고가 있고, 중복되지 않은 플랫폼만
        if (!ott.localLogo || !ott.platformName) return false;
        if (seenPlatforms.has(ott.platformName)) return false;
        seenPlatforms.add(ott.platformName);
        return true;
      });
  }, [data]);

  const hasData = filteredOtts && filteredOtts.length > 0;

  return (
    <section className="ott-section">
      {/* 타이틀 */}
      <div className="ott-section__title">감상 가능한 곳</div>

      {/* OTT 아이콘 리스트 또는 없음 메시지 */}
      {hasData ? (
        <div className="ott-section__list">
          {filteredOtts.map((ott, idx) => {
            const name = ott.platformName || ott.name || "";

            return (
              <div
                key={`${ott.platformName}-${idx}`}
                className="ott-section__item"
                title={name}
              >
                <ImageWithFallback
                  src={ott.localLogo}
                  alt={name}
                  className="ott-section__logo"
                  width="60px"
                  height="60px"
                  placeholder={name || "OTT"}
                  style={{
                    borderRadius: "50%",
                    objectFit: "contain",
                  }}
                />
              </div>
            );
          })}
        </div>
      ) : (
        <div className="ott-section__empty">OTT 정보가 없습니다.</div>
      )}
    </section>
  );
}
