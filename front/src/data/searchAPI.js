import { movieAPI } from "../utils/api";

// 검색어 정규화: 띄어쓰기 제거하여 유연한 검색 지원
const normalizeSearchQuery = (query) => {
  if (!query) return "";
  // 띄어쓰기 제거 (예: "너의 이름은" -> "너의이름은")
  return query.trim().replace(/\s+/g, "");
};

// 검색어를 여러 변형으로 생성 (띄어쓰기 포함/제거)
const generateSearchVariants = (query) => {
  const trimmed = query.trim();
  const variants = [trimmed]; // 원본 검색어

  // 띄어쓰기 제거 버전
  const noSpaces = trimmed.replace(/\s+/g, "");
  if (noSpaces !== trimmed) {
    variants.push(noSpaces);
  }

  // 띄어쓰기 추가 버전 (한글 단어 사이에 띄어쓰기 추가)
  // 간단한 휴리스틱: 2글자 이상 연속된 한글 사이에 띄어쓰기 추가
  const withSpaces = trimmed.replace(/([가-힣]{2,})([가-힣])/g, "$1 $2");
  if (withSpaces !== trimmed && !variants.includes(withSpaces)) {
    variants.push(withSpaces);
  }

  return variants;
};

export const searchMovies = async (query) => {
  if (!query || query.trim() === "") {
    return [];
  }

  const trimmedQuery = query.trim();

  // 연도 추출 (4자리 숫자)
  const yearMatch = trimmedQuery.match(/\b(19|20)\d{2}\b/);
  const extractedYear = yearMatch ? yearMatch[0] : null;
  const searchKeyword = yearMatch
    ? trimmedQuery.replace(/\b(19|20)\d{2}\b/g, "").trim()
    : trimmedQuery;

  // 검색어 변형 생성 (띄어쓰기 포함/제거)
  const searchVariants = generateSearchVariants(searchKeyword);

  if (trimmedQuery.length < 2) {
    try {
      // 검색어 변형들을 모두 시도하여 최대한 많은 결과 가져오기
      const allSearchPromises = [];
      for (const variant of searchVariants) {
        allSearchPromises.push(
          movieAPI.searchLocalMovies(variant, 0, 50),
          movieAPI.searchLocalMovies(variant, 1, 50),
          movieAPI.searchLocalMovies(variant, 2, 50)
        );
      }

      const allPages = await Promise.all(allSearchPromises);

      // 모든 페이지 결과 합치기 (중복 제거)
      const allContentMap = new Map();
      allPages.forEach((page) => {
        const content = page?.content || page || [];
        if (Array.isArray(content)) {
          content.forEach((movie) => {
            if (movie.id) {
              allContentMap.set(movie.id, movie);
            } else if (movie.title) {
              // id가 없으면 제목으로 중복 체크
              const key = `${movie.title}_${movie.releaseYear || ""}`;
              if (!allContentMap.has(key)) {
                allContentMap.set(key, movie);
              }
            }
          });
        }
      });

      const allContent = Array.from(allContentMap.values());

      const localResults = { content: allContent };
      const content = localResults?.content || localResults || [];
      if (!Array.isArray(content) || content.length === 0) {
        return [];
      }

      // 각 영화에 대해 OTT 정보와 줄거리를 가져오기
      const enrichedResults = await Promise.all(
        content.map(async (movie) => {
          let ott = [];
          let description = movie.overview || "";

          // id가 있으면 OTT 정보 가져오기
          if (movie.id) {
            try {
              const ottData = await movieAPI.getAvailability(movie.id);
              if (Array.isArray(ottData)) {
                // OttDto 전체 객체를 저장 (상세 페이지와 동일하게)
                ott = ottData.map((item) => ({
                  id: item.id,
                  name: item.name || item.providerName || "",
                  type: item.type,
                  region: item.region,
                  logoUrl: item.logoUrl,
                  linkUrl: item.linkUrl,
                }));
              }
              } catch (error) {
              // OTT 정보 가져오기 실패는 무시
            }

            // overview가 없으면 상세 정보에서 가져오기 시도
            if (!description) {
              try {
                const detail = await movieAPI.getMovieById(movie.id);
                description = detail?.overview || detail?.info?.overview || "";
              } catch (error) {
                // 상세 정보 가져오기 실패는 무시
              }
            }
          }

          // 감독 정보 추출 (여러 소스에서 시도)
          let directorName = null;
          if (
            movie.directors &&
            Array.isArray(movie.directors) &&
            movie.directors.length > 0
          ) {
            // directors 배열에서 첫 번째 감독 이름 추출
            directorName =
              movie.directors[0]?.name || movie.directors[0] || null;
          } else if (movie.director) {
            // director 필드가 문자열인 경우
            directorName =
              typeof movie.director === "string" ? movie.director : null;
          }

          return {
            id: movie.id || null,
            title: movie.title || "제목 없음",
            originalTitle: movie.originalTitle || movie.title || "",
            year: movie.releaseDate?.split("-")[0] || movie.releaseYear || null,
            poster: movie.posterUrl || null,
            backdropUrl: movie.backdropUrl || null,
            genres: movie.genres || [],
            country: movie.country || null,
            director: directorName,
            plot: description,
            rating: movie.ageRating || null,
            runtime: movie.runtimeMinutes || null,
            actors: movie.actors?.map((a) => a.name) || [],
            description: description,
            creator: directorName || "",
            ott: ott,
          };
        })
      );

      return enrichedResults.filter(
        (movie) => movie.title && movie.title !== "제목 없음"
      );
    } catch (error) {
      return [];
    }
  }

  try {
    // 먼저 로컬 DB 검색 시도 (최대한 많이 가져오기)
    let results = [];
    try {
      // 검색어 변형들을 모두 시도하여 최대한 많은 결과 가져오기
      const allSearchPromises = [];
      for (const variant of searchVariants) {
        allSearchPromises.push(
          movieAPI.searchLocalMovies(variant, 0, 50),
          movieAPI.searchLocalMovies(variant, 1, 50),
          movieAPI.searchLocalMovies(variant, 2, 50),
          movieAPI.searchLocalMovies(variant, 3, 50)
        );
      }

      const allPages = await Promise.all(allSearchPromises);

      // 모든 페이지 결과 합치기 (중복 제거)
      const allContentMap = new Map();
      allPages.forEach((page) => {
        const content = page?.content || page || [];
        if (Array.isArray(content)) {
          content.forEach((movie) => {
            if (movie.id) {
              allContentMap.set(movie.id, movie);
            } else if (movie.title) {
              // id가 없으면 제목으로 중복 체크
              const key = `${movie.title}_${movie.releaseYear || ""}`;
              if (!allContentMap.has(key)) {
                allContentMap.set(key, movie);
              }
            }
          });
        }
      });

      const allContent = Array.from(allContentMap.values());

      const localResults = { content: allContent };
      const content = localResults?.content || [];
      if (Array.isArray(content) && content.length > 0) {
        // 각 영화에 대해 OTT 정보와 줄거리를 가져오기
        results = await Promise.all(
          content.map(async (movie) => {
            let ott = [];
            let description = movie.overview || "";

            // id가 있으면 OTT 정보 가져오기
            if (movie.id) {
              try {
                const ottData = await movieAPI.getAvailability(movie.id);
                if (Array.isArray(ottData)) {
                  // OttDto 전체 객체를 저장 (상세 페이지와 동일하게)
                  ott = ottData.map((item) => ({
                    id: item.id,
                    name: item.name || item.providerName || "",
                    type: item.type,
                    region: item.region,
                    logoUrl: item.logoUrl,
                    linkUrl: item.linkUrl,
                  }));
                }
              } catch (error) {
              // OTT 정보 가져오기 실패는 무시
            }

              // overview가 없으면 상세 정보에서 가져오기 시도
              if (!description) {
                try {
                  const detail = await movieAPI.getMovieById(movie.id);
                  description =
                    detail?.overview || detail?.info?.overview || "";
                } catch (error) {
                  // 상세 정보 가져오기 실패는 무시
                }
              }
            }

            // 감독 정보 추출 (여러 소스에서 시도)
            let directorName = null;
            if (
              movie.directors &&
              Array.isArray(movie.directors) &&
              movie.directors.length > 0
            ) {
              // directors 배열에서 첫 번째 감독 이름 추출
              directorName =
                movie.directors[0]?.name || movie.directors[0] || null;
            } else if (movie.director) {
              // director 필드가 문자열인 경우
              directorName =
                typeof movie.director === "string" ? movie.director : null;
            }

            return {
              id: movie.id || null,
              title: movie.title || "제목 없음",
              originalTitle: movie.originalTitle || movie.title || "",
              year:
                movie.releaseDate?.split("-")[0] || movie.releaseYear || null,
              poster: movie.posterUrl || null,
              backdropUrl: movie.backdropUrl || null,
              genres: movie.genres || [],
              country: movie.country || null,
              director: directorName,
              plot: description,
              rating: movie.ageRating || null,
              runtime: movie.runtimeMinutes || null,
              actors: movie.actors?.map((a) => a.name) || [],
              description: description,
              creator: directorName || "",
              ott: ott,
            };
          })
        );

        results = results.filter(
          (movie) => movie.title && movie.title !== "제목 없음"
        );
      }
    } catch (localError) {
      // 로컬 DB 검색 실패는 무시하고 KMDB 검색 시도
    }

    // 로컬 DB 결과가 없거나 적으면 KMDB 검색 시도 (더 많은 결과를 위해)
    if (results.length < 20) {
      // 연도 파라미터와 함께 KMDB 검색
      const kmdbResults = await movieAPI.searchMovies(
        searchKeyword,
        extractedYear
      );

      // 백엔드 응답이 배열인지 확인
      if (Array.isArray(kmdbResults)) {
        // KMDB 결과를 프론트엔드 형식으로 변환
        const kmdbTransformed = kmdbResults
          .map((movie) => ({
            id: movie.id || null,
            title: movie.title || "제목 없음",
            originalTitle: movie.originalTitle || movie.title || "",
            year: movie.releaseDate?.split("-")[0] || movie.releaseYear || null,
            poster: movie.posterUrl || null,
            backdropUrl: movie.backdropUrl || null,
            genres: movie.genre
              ? typeof movie.genre === "string"
                ? movie.genre.split(", ")
                : movie.genre
              : [],
            country: movie.nation || null,
            director: movie.director || null,
            plot: movie.plot || null,
            rating: movie.rating || null,
            runtime: movie.runtime || null,
            actors: movie.actors
              ? typeof movie.actors === "string"
                ? movie.actors.split(", ")
                : movie.actors
              : [],
            // SearchResultCard에 필요한 필드 매핑
            description: movie.plot || "",
            creator: movie.director || "",
            ott: [], // KMDB 검색 결과에는 OTT 정보가 없음
          }))
          .filter((movie) => {
            // 검색어와 관련성이 있는 결과만 필터링
            if (!movie.title || movie.title === "제목 없음") return false;
            const titleLower = movie.title.toLowerCase();
            const queryLower = trimmedQuery.toLowerCase();
            // 제목에 검색어가 포함되어 있거나, 검색어가 2글자 이상일 때만 포함
            return titleLower.includes(queryLower) || trimmedQuery.length >= 2;
          });

        // 로컬 DB 결과와 KMDB 결과를 합치되, 중복 제거 (제목 기준)
        const existingTitles = new Set(
          results.map((r) => r.title?.toLowerCase())
        );
        const newKmdbResults = kmdbTransformed.filter(
          (m) => !existingTitles.has(m.title?.toLowerCase())
        );
        results = [...results, ...newKmdbResults];
      }
    }

    return results;
  } catch (error) {
    return [];
  }
};
