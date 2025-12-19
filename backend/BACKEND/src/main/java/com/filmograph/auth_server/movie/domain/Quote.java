package com.filmograph.auth_server.movie.domain;

import jakarta.persistence.*;

@Entity @Table(name="quote")
public class Quote {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="movie_id", nullable=false)
    private Movie movie;

    @Column(nullable=false, columnDefinition="TEXT")
    private String text;

    @Column(length=128)
    private String speaker;

    @Column(length=8)
    private String lang;

    public Long getId(){ return id; }
    public Movie getMovie(){ return movie; }
    public String getText(){ return text; }
    public String getSpeaker(){ return speaker; }
    public String getLang(){ return lang; }
    
    public void setMovie(Movie movie) { this.movie = movie; }
    public void setText(String text) { this.text = text; }
    public void setSpeaker(String speaker) { this.speaker = speaker; }
    public void setLang(String lang) { this.lang = lang; }
}

