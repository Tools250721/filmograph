package com.filmograph.auth_server.movie.web;

import com.filmograph.auth_server.movie.domain.Movie;
import com.filmograph.auth_server.movie.domain.Quote;
import com.filmograph.auth_server.movie.repo.MovieRepository;
import com.filmograph.auth_server.movie.repo.QuoteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteRepository repo;
    private final MovieRepository movieRepository;

    public QuoteController(QuoteRepository repo, MovieRepository movieRepository) {
        this.repo = repo;
        this.movieRepository = movieRepository;
    }

    /**
     * 랜덤 명대사 조회
     */
    @GetMapping("/random")
    public ResponseEntity<Map<String, Object>> random(@RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String lang) {
        try {
            var list = repo.randomOne(movieId, lang, PageRequest.of(0, 1));
            if (list.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "id", 0L,
                        "movieId", movieId != null ? movieId : 0L,
                        "text", "",
                        "speaker", "",
                        "lang", ""));
            }
            Quote q = list.get(0);

            // LazyInitializationException 방지를 위해 movieId를 먼저 가져옴
            Long movieIdValue;
            try {
                movieIdValue = q.getMovie().getId();
            } catch (Exception e) {
                // 지연 로딩 실패 시 movieId 파라미터 사용
                movieIdValue = movieId != null ? movieId : 0L;
            }

            return ResponseEntity.ok(Map.of(
                    "id", q.getId(),
                    "movieId", movieIdValue,
                    "text", q.getText() != null ? q.getText() : "",
                    "speaker", q.getSpeaker() != null ? q.getSpeaker() : "",
                    "lang", q.getLang() != null ? q.getLang() : ""));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("명대사 조회 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 영화별 명대사 목록 조회
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getQuotesByMovie(@PathVariable Long movieId,
            @RequestParam(required = false) String lang) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("movie not found: " + movieId));

        List<Quote> quotes = repo.findByMovie(movie);

        // 언어 필터링
        if (lang != null && !lang.isBlank()) {
            quotes = quotes.stream()
                    .filter(q -> lang.equals(q.getLang()))
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> result = quotes.stream()
                .map(q -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", q.getId());
                    map.put("text", q.getText());
                    map.put("speaker", q.getSpeaker() != null ? q.getSpeaker() : "");
                    map.put("lang", q.getLang() != null ? q.getLang() : "");
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "movieId", movieId,
                "count", result.size(),
                "quotes", result));
    }

    /**
     * 명대사 추가 (관리자용)
     */
    @PostMapping("/movie/{movieId}")
    public ResponseEntity<?> addQuote(@PathVariable Long movieId,
            @RequestBody Map<String, String> request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("요청 바디가 필요합니다.");
            }

            Movie movie = movieRepository.findById(movieId)
                    .orElseThrow(() -> new IllegalArgumentException("movie not found: " + movieId));

            String text = request.get("text");
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("명대사 텍스트는 필수입니다.");
            }

            Quote quote = new Quote();
            quote.setMovie(movie);
            quote.setText(text);

            String speaker = request.get("speaker");
            if (speaker != null && !speaker.isBlank()) {
                quote.setSpeaker(speaker);
            }

            String lang = request.get("lang");
            if (lang != null && !lang.isBlank()) {
                if (lang.length() > 8) {
                    throw new IllegalArgumentException("lang 값은 최대 8자까지 허용됩니다. (현재: " + lang.length() + "자)");
                }
                quote.setLang(lang);
            } else {
                quote.setLang("ko");
            }

            Quote saved = saveQuote(quote);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", saved.getId());
            response.put("movieId", movieId);
            response.put("text", saved.getText());
            response.put("speaker", saved.getSpeaker() != null ? saved.getSpeaker() : "");
            response.put("lang", saved.getLang() != null ? saved.getLang() : "");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("명대사 추가 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 트랜잭션 내에서 Quote 저장 (롤백 문제 방지)
     */
    @Transactional
    private Quote saveQuote(Quote quote) {
        return repo.save(quote);
    }
}
