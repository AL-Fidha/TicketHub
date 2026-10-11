package com.tickethub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tickethub.model.Movie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TmdbService {

    @Value("${app.tmdb.api-key:}")
    private String apiKey;

    @Value("${app.tmdb.base-url}")
    private String baseUrl;

    @Value("${app.tmdb.image-base}")
    private String imageBase;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper       = new ObjectMapper();

    public List<Map<String, Object>> discoverMovies(Movie.Language language) {
        requireApiKey();
        LocalDate today = LocalDate.now();
        String url = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/discover/movie")
            .queryParam("api_key", apiKey)
            .queryParam("region", "IN")
            .queryParam("with_original_language", languageCode(language))
            .queryParam("primary_release_date.gte", today.minusDays(90))
            .queryParam("primary_release_date.lte", today.plusDays(180))
            .queryParam("sort_by", "primary_release_date.desc")
            .queryParam("include_adult", false)
            .queryParam("page", 1)
            .build().encode().toUriString();

        try {
            JsonNode results = mapper.readTree(restTemplate.getForObject(url, String.class)).path("results");
            List<Map<String, Object>> movies = new ArrayList<>();
            for (JsonNode result : results) {
                Map<String, Object> movie = new LinkedHashMap<>();
                movie.put("tmdb_id", result.path("id").asLong());
                movie.put("title", result.path("title").asText(""));
                movie.put("overview", result.path("overview").asText(""));
                movie.put("release_date", result.path("release_date").asText(""));
                movie.put("rating", result.path("vote_average").asDouble());
                movie.put("poster_url", buildUrl(result.path("poster_path").asText(null)));
                movies.add(movie);
            }
            return movies;
        } catch (Exception e) {
            log.warn("TMDB discovery failed: {}", e.getClass().getSimpleName());
            throw new IllegalStateException("TMDB could not return movie listings. Check the API key and network connection.");
        }
    }

    public JsonNode getMovieDetails(Long tmdbId) {
        requireApiKey();
        String url = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/movie/{id}")
            .queryParam("api_key", apiKey)
            .queryParam("append_to_response", "credits")
            .buildAndExpand(tmdbId).encode().toUriString();
        try {
            return mapper.readTree(restTemplate.getForObject(url, String.class));
        } catch (Exception e) {
            log.warn("TMDB movie detail lookup failed: {}", e.getClass().getSimpleName());
            throw new IllegalStateException("TMDB could not load this movie. Check the API key and network connection.");
        }
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("YOUR_TMDB_API_KEY"))
            throw new IllegalStateException("Set app.tmdb.api-key in your local application.properties first.");
    }

    private String languageCode(Movie.Language language) {
        return switch (language) {
            case Malayalam -> "ml";
            case Tamil -> "ta";
            case Hindi -> "hi";
            case English -> "en";
            case Telugu -> "te";
        };
    }

    /** Returns the full poster URL for a movie, or null if not found */
    public String fetchPosterUrl(String title, Long tmdbId) {
        if (apiKey == null || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) return null;
        try {
            String posterPath = null;

            if (tmdbId != null) {
                String url  = baseUrl + "/movie/" + tmdbId + "?api_key=" + apiKey;
                String body = restTemplate.getForObject(url, String.class);
                JsonNode node = mapper.readTree(body);
                posterPath = node.path("poster_path").asText(null);
            }

            if (posterPath == null) {
                String q    = java.net.URLEncoder.encode(title, "UTF-8");
                String url  = baseUrl + "/search/movie?api_key=" + apiKey + "&query=" + q;
                String body = restTemplate.getForObject(url, String.class);
                JsonNode root = mapper.readTree(body);
                JsonNode results = root.path("results");
                if (results.isArray() && results.size() > 0)
                    posterPath = results.get(0).path("poster_path").asText(null);
            }

            return posterPath != null ? imageBase + posterPath : null;

        } catch (Exception e) {
            log.warn("TMDB poster fetch failed for '{}': {}", title, e.getClass().getSimpleName());
            return null;
        }
    }

    public String getPosterPath(String title, Long tmdbId) {
        if (apiKey == null || apiKey.equals("YOUR_TMDB_API_KEY_HERE")) return null;
        try {
            if (tmdbId != null) {
                String url  = baseUrl + "/movie/" + tmdbId + "?api_key=" + apiKey;
                String body = restTemplate.getForObject(url, String.class);
                JsonNode node = mapper.readTree(body);
                String p = node.path("poster_path").asText(null);
                if (p != null) return p;
            }
            String q    = java.net.URLEncoder.encode(title, "UTF-8");
            String url  = baseUrl + "/search/movie?api_key=" + apiKey + "&query=" + q;
            String body = restTemplate.getForObject(url, String.class);
            JsonNode root = mapper.readTree(body);
            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0)
                return results.get(0).path("poster_path").asText(null);
        } catch (Exception e) {
            log.warn("TMDB poster lookup failed for '{}': {}", title, e.getClass().getSimpleName());
        }
        return null;
    }

    public String buildUrl(String posterPath) {
        if (posterPath == null || posterPath.isBlank()) return null;
        if (posterPath.startsWith("http://") || posterPath.startsWith("https://")) {
            return posterPath;
        }
        return imageBase + (posterPath.startsWith("/") ? posterPath : "/" + posterPath);
    }
}
