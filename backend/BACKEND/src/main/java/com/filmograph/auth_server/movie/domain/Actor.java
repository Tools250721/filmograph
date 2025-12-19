package com.filmograph.auth_server.movie.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 배우 이름
    //@Column(nullable = false)
    private String name;

    // 출연한 영화 목록
    @ManyToMany(mappedBy = "actors")
    @Builder.Default
    private List<Movie> movies = new ArrayList<>();

    public Actor(String name) {
        this.name=name;
    }
}
