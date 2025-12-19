import { useState, useEffect } from "react";
import { useSearchParams, Link } from "react-router-dom";
import Header from "../layouts/Header_2";
import { searchMovies } from "../../data/searchAPI";
import SearchResultItem from "./components/SearchResultCard";
import { SearchCardSkeleton } from "../../components/ui/Skeleton";
import { ReactComponent as MoreIcon } from "../../assets/icons/MoreIcon.svg";
import "./SearchPage.css";

export default function SearchPage() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get("q");

  const [searchResults, setSearchResults] = useState([]);
  const [visibleCount, setVisibleCount] = useState(10);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const loadSearchResults = async () => {
      if (!query || query.trim() === "") {
        setSearchResults([]);
        setVisibleCount(10);
        return;
      }

      setIsLoading(true);
      try {
        const results = await searchMovies(query);
        setSearchResults(Array.isArray(results) ? results : []);
        setVisibleCount(10);
      } catch (error) {
        console.error("검색 실패:", error);
        setSearchResults([]);
      } finally {
        setIsLoading(false);
      }
    };

    loadSearchResults();
  }, [query]);

  const handleLoadMore = () => {
    setVisibleCount((prev) => prev + 10);
  };

  return (
    <div className="search-page">
      {/* 상단 헤더 */}
      <Header />
      <div className="search-page__body">
        {/* 검색 결과 */}
        <div className="search-page__results">
          <h2 className="search-page__title">"{query}"의 검색 결과</h2>

          {isLoading ? (
            <div className="search-page__grid">
              {Array.from({ length: 6 }).map((_, i) => (
                <SearchCardSkeleton key={i} />
              ))}
            </div>
          ) : Array.isArray(searchResults) && searchResults.length > 0 ? (
            <>
              <div className="search-page__grid">
                {searchResults.slice(0, visibleCount).map((item, index) => {
                  // id가 없으면 제목으로 검색 페이지로 이동
                  const linkTo = item.id
                    ? `/movie/${item.id}`
                    : `/search?q=${encodeURIComponent(item.title || "")}`;
                  return (
                    <Link
                      key={item.id || `search-${index}`}
                      to={linkTo}
                      className="search-page__grid-link"
                    >
                      <SearchResultItem {...item} />
                    </Link>
                  );
                })}
              </div>

              {/* 더보기 버튼 */}
              {visibleCount < searchResults.length && (
                <div className="search-page__load-more">
                  <button
                    onClick={handleLoadMore}
                    className="search-page__load-more-button"
                  >
                    더보기 <MoreIcon />
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="search-page__no-results">검색 결과가 없습니다.</div>
          )}
        </div>
      </div>
    </div>
  );
}
