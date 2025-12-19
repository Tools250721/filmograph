package com.filmograph.auth_server.movie.domain;

/**
 * 유저의 영화 상태 Enum
 * WATCHED : 이미 본 영화
 * WISHLIST : 보고싶은 영화 (위시리스트)
 * WATCHING : 현재 보고 있는 영화
 * FAVORITE : 즐겨찾기 등록한 영화
 */
public enum UserMovieStatus {
    WATCHED,
    WISHLIST,
    WATCHING,
    FAVORITE
}
