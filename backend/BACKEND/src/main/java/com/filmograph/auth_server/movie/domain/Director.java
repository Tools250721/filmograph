package com.filmograph.auth_server.movie.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="directors")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    //  mappedBy 제거 해서 단방향 매핑으로 변경
    @ManyToMany
    @Builder.Default
    private List<Movie> movies = new ArrayList<>();
}
