package com.tickethub.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 120)
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Language language;

    @com.fasterxml.jackson.annotation.JsonAlias({"duration_min", "durationMin"})
    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(columnDefinition = "DECIMAL(3,1)")
    private Double rating;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Column(name = "poster_path", length = 500)
    private String posterPath;   // TMDB path /abc.jpg or full image URL

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = Status.NOW_SHOWING;
    }

    public enum Language { Malayalam, English, Tamil, Hindi, Telugu }
    public enum Status   { NOW_SHOWING, NEW, COMING_SOON }

    // Convenience: full poster URL (populated in service layer or fallback)
    @Transient
    private String posterUrl;

    @com.fasterxml.jackson.annotation.JsonProperty("poster_url")
    public String getPosterUrl() {
        if (posterUrl != null && !posterUrl.isBlank()) return posterUrl;
        if (posterPath != null && !posterPath.isBlank()) {
            if (posterPath.startsWith("http://") || posterPath.startsWith("https://")) {
                return posterPath;
            }
            return "https://image.tmdb.org/t/p/w500" + posterPath;
        }
        return null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("duration_min")
    public Integer getDurationMinSnake() {
        return durationMin;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("duration_min")
    public void setDurationMinSnake(Integer duration) {
        if (duration != null) this.durationMin = duration;
    }
}
