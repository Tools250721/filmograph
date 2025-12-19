package com.filmograph.auth_server.movie.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieSearchRequest {
    private String keyword;
    private String year;
    private int page = 0;
    private int size = 10;
}
