package com.filmograph.auth_server.movie.dto;

public class RedirectResponse {
    private final boolean ok;
    private final String list;     // "bucket" | "favorites" | "watched"
    private final Long movieId;
    private final String redirect; // 프론트 페이지 URL

    public RedirectResponse(boolean ok, String list, Long movieId, String redirect) {
        this.ok = ok;
        this.list = list;
        this.movieId = movieId;
        this.redirect = redirect;
    }

    public boolean isOk() { return ok; }
    public String getList() { return list; }
    public Long getMovieId() { return movieId; }
    public String getRedirect() { return redirect; }
}
