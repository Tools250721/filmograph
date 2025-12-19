import { useEffect, useRef, useState } from "react";
import StealCard from "./ui/StealCard";
import "./GallerySection.css";

export default function GallerySection({ data = [] }) {
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
    el.scrollBy({ left: dir * 570, behavior: "smooth" });
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
  }, []);

  return (
    <div className="gallery-section">
      <div className="gallery-section__title">갤러리</div>

      <div ref={scrollRef} className="gallery-section__list">
        {data.map((p, i) => (
          <div key={`${p.url || p.name || i}-${i}`} className="gallery-section__item">
            <StealCard url={p.url || p.photo} name={p.name || `스틸컷 ${i + 1}`} />
          </div>
        ))}
      </div>

      {canLeft && (
        <button
          type="button"
          className="gallery-section__nav gallery-section__nav--left"
          onClick={() => scrollByAmount(-1)}
          aria-label="이전 이미지 보기"
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
          className="gallery-section__nav gallery-section__nav--right"
          onClick={() => scrollByAmount(1)}
          aria-label="다음 이미지 보기"
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
    </div>
  );
}
