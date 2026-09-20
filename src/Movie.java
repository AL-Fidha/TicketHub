public class Movie {
    private String movieId;
    private String title;
    private String genre;
    private int duration;

    public Movie(String movieId, String title, String genre, int duration) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
    }

    public String getMovieDetails() {
        return movieId + " - " + title + " (" + genre + ", " + duration + " mins)";
    }

    public String getTitle() {
        return title;
    }
}

