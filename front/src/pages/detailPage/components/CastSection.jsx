import { useEffect, useRef, useState } from "react";
import PersonCard from "./ui/PersonCard";
import "./CastSection.css";

export default function CastSection({ data = [] }) {
  const scrollRef = useRef(null);
  const [canLeft, setCanLeft] = useState(false);
  const [canRight, setCanRight] = useState(false);

  const checkScroll = () => {
    const el = scrollRef.current;
    if (!el) return;
    setCanLeft(el.scrollLeft > 0);
    setCanRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 1);
  };

  const scrollByAmount = (dir) => {
    const el = scrollRef.current;
    if (!el) return;
    el.scrollBy({ left: dir * 320, behavior: "smooth" });
  };

  useEffect(() => {
    checkScroll();
    const el = scrollRef.current;
    if (!el) return;
    el.addEventListener("scroll", checkScroll);
    window.addEventListener("resize", checkScroll);
    return () => {
      el.removeEventListener("scroll", checkScroll);
      window.removeEventListener("resize", checkScroll);
    };
  }, [data]);

  const hasData = data && Array.isArray(data) && data.length > 0;

  return (
    <div className="cast-section">
      <div className="cast-section__title">출연/제작</div>

      {hasData ? (
        <>
          {/* 가로 스크롤 */}
          <div ref={scrollRef} className="cast-section__list">
            {data.map((p, i) => {
              const rowIndex = i % 3;
              const withDivider = rowIndex !== 2;

              const personData = {
                name: p.name || "",
                job: p.role === "감독" || p.job === "감독" ? "감독" : null,
                character: p.character || null,
                photo: p.profileUrl || p.photo || null,
                withDivider: withDivider,
              };

              return (
                <div
                  key={`${p.name || p.id || i}-${i}`}
                  className="cast-section__item"
                >
                  <PersonCard {...personData} />
                </div>
              );
            })}
          </div>

          {/* 좌/우 화살표 */}
          {canLeft && (
            <button
              type="button"
              className="cast-section__nav cast-section__nav--left"
              onClick={() => scrollByAmount(-1)}
              aria-label="이전 인물 보기"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path
                  d="M9 5l7 7-7 7"
                  color="#7e7e7e"
                  stroke="currentColor"
                  strokeWidth="1"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </button>
          )}

          {canRight && (
            <button
              type="button"
              className="cast-section__nav cast-section__nav--right"
              onClick={() => scrollByAmount(1)}
              aria-label="다음 인물 보기"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path
                  d="M9 5l7 7-7 7"
                  color="#7e7e7e"
                  stroke="currentColor"
                  strokeWidth="1"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </button>
          )}
        </>
      ) : (
        <div className="cast-section__empty">출연/제작 정보가 없습니다.</div>
      )}
    </div>
  );
}
