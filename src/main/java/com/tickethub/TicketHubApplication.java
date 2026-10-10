package com.tickethub;

import com.tickethub.model.*;
import com.tickethub.repository.*;
import com.tickethub.service.TmdbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@Slf4j
public class TicketHubApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(TicketHubApplication.class, args);
        String port = context.getEnvironment().getProperty("local.server.port",
            context.getEnvironment().getProperty("server.port", "8080"));
        System.out.println("""

            ╔══════════════════════════════════════════╗
            ║   🎬  TICKET HUB is running!              ║
            ║   Open: http://localhost:%s             ║
            ╚══════════════════════════════════════════╝
            """.formatted(port));
    }

    @Bean
    CommandLineRunner seedData(
            UserRepository    userRepo,
            MovieRepository   movieRepo,
            TheatreRepository theatreRepo,
            ScreenRepository  screenRepo,
            ShowRepository    showRepo,
            PasswordEncoder   encoder,
            TmdbService       tmdb,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {

        return args -> {
            // ── 1. Optional administrator bootstrap ────────
            if (adminEmail != null && !adminEmail.isBlank()
                    && adminPassword != null && !adminPassword.isBlank()) {
                String normalizedAdminEmail = adminEmail.trim().toLowerCase(java.util.Locale.ROOT);
                if (!userRepo.existsByEmail(normalizedAdminEmail)) {
                    userRepo.save(User.builder()
                        .name("Administrator")
                        .email(normalizedAdminEmail)
                        .password(encoder.encode(adminPassword))
                        .role(User.Role.admin)
                        .build());
                    log.info("Administrator account created.");
                }
            } else {
                log.warn("No administrator bootstrapped. Set app.admin.email and app.admin.password to create one.");
            }

            // ── 2. Movies ──────────────────────────────────
            if (movieRepo.count() == 0) {
                List<Movie> movies = List.of(
                    // ── MALAYALAM ──
                    movie("Manjummel Boys", "Adventure, Thriller, Survival",
                          Movie.Language.Malayalam, 135, 8.6, 1205371L,
                          "https://image.tmdb.org/t/p/w500/4ux5z0Bl7NrtKpj08YLzJEB4HO3.jpg",
                          "A group of close friends from Kochi embark on a trip to Kodaikanal where a harrowing cave accident tests their friendship and resilience.",
                          Movie.Status.NOW_SHOWING),
                    movie("Aavesham", "Action, Comedy",
                          Movie.Language.Malayalam, 158, 8.4, 1084812L,
                          "https://image.tmdb.org/t/p/w500/k5RWPaNjgRcNvGoawYaQHQwyctI.jpg",
                          "Three engineering freshmen in Bangalore befriend an eccentric local gangster named Ranga to stand up against college bullies.",
                          Movie.Status.NOW_SHOWING),
                    movie("Premalu", "Romance, Comedy",
                          Movie.Language.Malayalam, 156, 8.2, 1196422L,
                          "https://upload.wikimedia.org/wikipedia/en/c/c5/Premalu_film_poster.jpg",
                          "Sachin moves to Hyderabad with dreams of higher education and finds his life turned upside down when he falls for the confident Reenu.",
                          Movie.Status.NOW_SHOWING),
                    movie("Bramayugam", "Horror, Mystery, Thriller",
                          Movie.Language.Malayalam, 139, 8.5, 1160164L,
                          "https://upload.wikimedia.org/wikipedia/en/0/09/Bramayugam_poster.jpg",
                          "A fleeing folk singer in 17th-century Malabar seeks shelter in a shadowy mana, only to discover a sinister game of ancient sorcery.",
                          Movie.Status.NOW_SHOWING),
                    movie("The Goat Life (Aadujeevitham)", "Adventure, Drama, Survival",
                          Movie.Language.Malayalam, 172, 8.7, 507241L,
                          "https://upload.wikimedia.org/wikipedia/en/f/fb/The_Goat_Life_poster.jpg",
                          "Based on the acclaimed true story, Najeeb endures years of forced isolation and desert labor in a desperate quest for freedom.",
                          Movie.Status.NOW_SHOWING),

                    // ── TAMIL ──
                    movie("Amaran", "Action, Biography, Drama",
                          Movie.Language.Tamil, 169, 8.8, 927342L,
                          "https://image.tmdb.org/t/p/w500/7cNE2qydew1c8fqnlhWjkE3DHc2.jpg",
                          "The inspiring and courageous journey of Major Mukund Varadarajan and his Rajput Regiment brothers in counter-insurgency operations.",
                          Movie.Status.NOW_SHOWING),
                    movie("Leo", "Action, Crime, Thriller",
                          Movie.Language.Tamil, 164, 8.0, 1072790L,
                          "https://image.tmdb.org/t/p/w500/9Rj8l6gElLpRL7Kj17iZhrT5Zuw.jpg",
                          "Parthiban lives a quiet life in Theog until a violent incident exposes him to an international cartel seeking a long-lost operative.",
                          Movie.Status.NOW_SHOWING),
                    movie("The Greatest of All Time (GOAT)", "Action, Sci-Fi, Thriller",
                          Movie.Language.Tamil, 179, 8.1, 1184918L,
                          "https://image.tmdb.org/t/p/w500/4ux5z0Bl7NrtKpj08YLzJEB4HO3.jpg",
                          "A veteran hostage negotiator and counter-terrorism specialist is pulled back into action when an old nemesis reappears with deadly technology.",
                          Movie.Status.NOW_SHOWING),

                    // ── HINDI ──
                    movie("Stree 2", "Comedy, Horror",
                          Movie.Language.Hindi, 149, 8.4, 1114513L,
                          "https://upload.wikimedia.org/wikipedia/en/a/a1/Stree_2.jpg",
                          "The residents of Chanderi confront a terrifying headless demon known as Sarkata, requiring an unlikely union of old friends.",
                          Movie.Status.NOW_SHOWING),
                    movie("Jawan", "Action, Thriller",
                          Movie.Language.Hindi, 169, 8.3, 872585L,
                          "https://upload.wikimedia.org/wikipedia/en/3/39/Jawan_film_poster.jpg",
                          "An enigmatic vigilante orchestrates high-stakes operations across the country to expose corruption and bring justice to ordinary citizens.",
                          Movie.Status.NOW_SHOWING),
                    movie("Animal", "Action, Crime, Drama",
                          Movie.Language.Hindi, 201, 8.0, 781732L,
                          "https://upload.wikimedia.org/wikipedia/en/9/90/Animal_%282023_film%29_poster.jpg",
                          "An all-consuming devotion between father and son spirals into a brutal underworld conflict when an assassination attempt occurs.",
                          Movie.Status.NOW_SHOWING),
                    movie("12th Fail", "Biography, Drama",
                          Movie.Language.Hindi, 147, 9.1, 1181548L,
                          "https://upload.wikimedia.org/wikipedia/en/f/f2/12th_Fail_poster.jpeg",
                          "Based on true events, Manoj Kumar Sharma battles extreme poverty and setbacks to restart his journey towards clearing the UPSC exam.",
                          Movie.Status.NOW_SHOWING),

                    // ── ENGLISH (HOLLYWOOD) ──
                    movie("Deadpool & Wolverine", "Action, Comedy, Sci-Fi",
                          Movie.Language.English, 128, 8.5, 533535L,
                          "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
                          "Wade Wilson's quiet retirement is shattered when the TVA pulls him into an interdimensional mission requiring the claws of Wolverine.",
                          Movie.Status.NOW_SHOWING),
                    movie("Dune: Part Two", "Action, Adventure, Sci-Fi",
                          Movie.Language.English, 166, 8.9, 693134L,
                          "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                          "Paul Atreides unites with Chani and the Fremen while seeking vengeance against those who destroyed his house, shaping the universe's fate.",
                          Movie.Status.NOW_SHOWING),
                    movie("Gladiator II", "Action, Adventure, Drama",
                          Movie.Language.English, 148, 8.4, 558449L,
                          "https://upload.wikimedia.org/wikipedia/en/0/04/Gladiator_II_%282024%29_poster.jpg",
                          "Decades after Maximus, Lucius enters the Colosseum to fight for the future of Rome and restore glory to its people.",
                          Movie.Status.NOW_SHOWING),
                    movie("Oppenheimer", "Biography, Drama, History",
                          Movie.Language.English, 180, 8.9, 872585L,
                          "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                          "The compelling story of theoretical physicist J. Robert Oppenheimer, whose leadership of the Manhattan Project forever altered world history.",
                          Movie.Status.NOW_SHOWING),
                    movie("Inside Out 2", "Animation, Adventure, Comedy",
                          Movie.Language.English, 96, 8.3, 1022789L,
                          "https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg",
                          "Joy and the gang meet new teenage emotions including Anxiety, Envy, Ennui, and Embarrassment inside Riley's evolving mind.",
                          Movie.Status.NOW_SHOWING)
                );
                movieRepo.saveAll(movies);
                log.info("17 blockbuster movies with real posters seeded.");
            }

            // ── 3. Theatres + Screens ──────────────────────
            if (theatreRepo.count() == 0) {
                List<Object[]> theatreData = List.of(
                    new Object[]{"PVR Cinemas - LuLu Mall",        "LuLu Mall, NH Bypass, Edapally",          "Kochi"},
                    new Object[]{"INOX - Oberon Mall",             "Oberon Mall, NH 66, Edapally",             "Kochi"},
                    new Object[]{"Cinepolis - Centre Square Mall", "Centre Square Mall, MG Road",              "Kochi"},
                    new Object[]{"PVR - Gold Souk Grande Mall",    "Gold Souk Grande Mall, Kalamassery",       "Kochi"},
                    new Object[]{"Carnival Cinemas - Kochi",       "Vyttila Hub, Vyttila",                     "Kochi"},
                    new Object[]{"INOX - Trivandrum",              "Bhavani Centre, Pattom",                   "Thiruvananthapuram"},
                    new Object[]{"PVR - Trivandrum",               "Oberon Mall, Kowdiar",                     "Thiruvananthapuram"},
                    new Object[]{"Carnival Cinemas - East Fort",   "East Fort, Thiruvananthapuram",            "Thiruvananthapuram"},
                    new Object[]{"Miraj Cinemas - Kozhikode",      "Focus Mall, Mavoor Road",                  "Kozhikode"},
                    new Object[]{"PVR - Kozhikode",                "HiLite Mall, NH 66, Kozhikode",            "Kozhikode"},
                    new Object[]{"Cinepolis - Calicut",            "City Centre Mall, Mavoor Road",            "Kozhikode"},
                    new Object[]{"Carnival Cinemas - Thrissur",    "Sobha City Mall, Thrissur",                "Thrissur"},
                    new Object[]{"INOX - Thrissur",                "Pearl Shopping Mall, Thrissur",            "Thrissur"},
                    new Object[]{"PVR - Kollam",                   "KSRTC Complex, Kollam",                    "Kollam"},
                    new Object[]{"Carnival Cinemas - Kannur",      "Focus Mall, Kannur",                       "Kannur"},
                    new Object[]{"Gold Theater - Palakkad",        "GB Road, Palakkad",                        "Palakkad"},
                    new Object[]{"Cinepolis - Kottayam",           "Meenachil Tower, Baker Junction",          "Kottayam"},
                    new Object[]{"PVR - Malappuram",               "Eureka Junction, Malappuram",              "Malappuram"},
                    new Object[]{"SRM Cinemas - Alappuzha",        "Mullakkal, Alappuzha",                     "Alappuzha"},
                    new Object[]{"Miraj Cinemas - Kasaragod",      "City Mall, Kasaragod",                     "Kasaragod"}
                );

                for (Object[] td : theatreData) {
                    Theatre t = theatreRepo.save(Theatre.builder()
                        .name((String) td[0])
                        .location((String) td[1])
                        .city((String) td[2])
                        .build());

                    // 2 screens per theatre
                    for (int s = 1; s <= 2; s++) {
                        screenRepo.save(Screen.builder()
                            .theatre(t)
                            .name("Screen " + s)
                            .totalSeats(80)
                            .rows(8)
                            .cols(10)
                            .build());
                    }
                }
                log.info("20 Kerala theatres + 40 screens seeded.");
            } else if (screenRepo.count() == 0) {
                for (Theatre t : theatreRepo.findAll()) {
                    for (int s = 1; s <= 2; s++) {
                        screenRepo.save(Screen.builder()
                            .theatre(t)
                            .name("Screen " + s)
                            .totalSeats(80)
                            .rows(8)
                            .cols(10)
                            .build());
                    }
                }
                log.info("40 screens seeded for existing theatres.");
            }

            // ── 4. Shows — next 5 days for each movie ──────
            if (showRepo.count() == 0) {
                List<Movie>  movies  = movieRepo.findAll();
                List<Screen> screens = screenRepo.findAll();
                if (!screens.isEmpty() && !movies.isEmpty()) {
                    LocalTime[]  times   = {
                        LocalTime.of(10, 0), LocalTime.of(14, 0),
                        LocalTime.of(18, 0), LocalTime.of(21, 0)
                    };

                    int screenIdx = 0;
                    for (Movie m : movies) {
                        Screen sc = screens.get(screenIdx % screens.size());
                        for (int d = 0; d < 5; d++) {
                            LocalDate date = LocalDate.now().plusDays(d);
                            for (LocalTime t : times) {
                                showRepo.save(Show.builder()
                                    .movie(m)
                                    .screen(sc)
                                    .showDate(date)
                                    .showTime(t)
                                    .priceStd(BigDecimal.valueOf(220))
                                    .pricePrem(BigDecimal.valueOf(380))
                                    .build());
                            }
                        }
                        screenIdx++;
                    }
                    log.info("Shows seeded for all movies.");
                }
            }
        };
    }

    private Movie movie(String title, String genre, Movie.Language lang,
                        int dur, double rating, Long tmdbId, String posterPath, String desc, Movie.Status status) {
        return Movie.builder()
            .title(title).genre(genre).language(lang)
            .durationMin(dur).rating(rating)
            .tmdbId(tmdbId).posterPath(posterPath).description(desc).status(status)
            .build();
    }
}
