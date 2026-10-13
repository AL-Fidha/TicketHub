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
            @Value("${app.admin.password:}") String adminPassword,
            @Value("${app.seed.demo-data:true}") boolean seedDemoData) {

        return args -> {
            // ── 1. Optional administrator bootstrap ────────
            if (adminEmail != null && !adminEmail.isBlank()
                    && adminPassword != null && !adminPassword.isBlank()) {
                String normalizedAdminEmail = adminEmail.trim().toLowerCase(java.util.Locale.ROOT);
                User adminUser = userRepo.findByEmail(normalizedAdminEmail).orElse(null);
                if (adminUser == null) {
                    userRepo.save(User.builder()
                        .name("Administrator")
                        .email(normalizedAdminEmail)
                        .password(encoder.encode(adminPassword))
                        .role(User.Role.admin)
                        .build());
                    log.info("Administrator account created.");
                } else {
                    adminUser.setPassword(encoder.encode(adminPassword));
                    adminUser.setRole(User.Role.admin);
                    userRepo.save(adminUser);
                    log.info("Administrator account credentials synchronized.");
                }
            } else {
                log.warn("No administrator bootstrapped. Set app.admin.email and app.admin.password to create one.");
            }

            if (!seedDemoData) {
                log.info("Sample movie, theatre, and show seeding is disabled.");
                return;
            }

            // ── 2. Movies ──────────────────────────────────
            List<Movie> defaultMovies = List.of(
                // ── MALAYALAM ──
                movie("Manjummel Boys", "Adventure, Thriller, Survival",
                      Movie.Language.Malayalam, 135, 8.6, 1069945L,
                      "https://image.tmdb.org/t/p/w500/bswrtewwthpsh6nABiqKevU4UBI.jpg",
                      "A group of close friends from Kochi embark on a trip to Kodaikanal where a harrowing cave accident tests their friendship and resilience.",
                      Movie.Status.NOW_SHOWING),
                movie("Aavesham", "Action, Comedy",
                      Movie.Language.Malayalam, 158, 8.4, 1084812L,
                      "https://image.tmdb.org/t/p/w500/k5RWPaNjgRcNvGoawYaQHQwyctI.jpg",
                      "Three engineering freshmen in Bangalore befriend an eccentric local gangster named Ranga to stand up against college bullies.",
                      Movie.Status.NOW_SHOWING),
                movie("Premalu", "Romance, Comedy",
                      Movie.Language.Malayalam, 156, 8.2, 1196422L,
                      "https://image.tmdb.org/t/p/w500/uPpmBjY3znUqGY8kYwI5xvOrSc0.jpg",
                      "Sachin moves to Hyderabad with dreams of higher education and finds his life turned upside down when he falls for the confident Reenu.",
                      Movie.Status.NOW_SHOWING),
                movie("Bramayugam", "Horror, Mystery, Thriller",
                      Movie.Language.Malayalam, 139, 8.5, 1160164L,
                      "https://image.tmdb.org/t/p/w500/snQLwRrfQAl5YFKVefZq9Lbscki.jpg",
                      "A fleeing folk singer in 17th-century Malabar seeks shelter in a shadowy mana, only to discover a sinister game of ancient sorcery.",
                      Movie.Status.NOW_SHOWING),
                movie("The Goat Life (Aadujeevitham)", "Adventure, Drama, Survival",
                      Movie.Language.Malayalam, 172, 8.7, 507241L,
                      "https://image.tmdb.org/t/p/w500/l7z3Skv1xLASnUt0whC4IInaXt0.jpg",
                      "Based on the acclaimed true story, Najeeb endures years of forced isolation and desert labor in a desperate quest for freedom.",
                      Movie.Status.NOW_SHOWING),
                movie("Kishkindha Kaandam", "Mystery, Thriller",
                      Movie.Language.Malayalam, 125, 8.5, 1344445L,
                      "https://image.tmdb.org/t/p/w500/rniBYHDRs6e8qMkPr4D4ZE5uMhF.jpg",
                      "Strange occurrences in a monkey-inhabited reserve forest unravel deep family secrets and missing firearms in an intricate puzzle.",
                      Movie.Status.NOW_SHOWING),
                movie("ARM (Ajayante Randam Moshanam)", "Action, Adventure, Fantasy",
                      Movie.Language.Malayalam, 147, 8.1, 1038234L,
                      "https://image.tmdb.org/t/p/w500/6OgA9x6jvwBIj8e7x8LANTbVvR2.jpg",
                      "Set in Northern Kerala across three generations, three heroes battle to protect a celestial treasure entrusted to their land.",
                      Movie.Status.NOW_SHOWING),
                movie("Turbo", "Action, Comedy",
                      Movie.Language.Malayalam, 152, 7.8, 1205371L,
                      "https://image.tmdb.org/t/p/w500/inTKQni4YW8syrfgnXHwzmNeSo4.jpg",
                      "Turbo Jose, an unyielding man from Idukki, lands in Chennai and gets entangled in a massive corporate and underworld conspiracy.",
                      Movie.Status.NOW_SHOWING),

                // ── ENGLISH ──
                movie("Dune: Part Two", "Sci-Fi, Adventure, Action",
                      Movie.Language.English, 166, 8.9, 693134L,
                      "https://image.tmdb.org/t/p/w500/6izwz7rsy95ARzTR3poZ8H6c5pp.jpg",
                      "Paul Atreides unites with Chani and the Fremen while seeking vengeance against the conspirators who destroyed his house.",
                      Movie.Status.NOW_SHOWING),
                movie("Oppenheimer", "Biography, Drama, History",
                      Movie.Language.English, 180, 8.9, 872585L,
                      "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                      "The compelling story of theoretical physicist J. Robert Oppenheimer, whose leadership of the Manhattan Project forever altered world history.",
                      Movie.Status.NOW_SHOWING),
                movie("Deadpool & Wolverine", "Action, Comedy, Sci-Fi",
                      Movie.Language.English, 128, 8.5, 533535L,
                      "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
                      "Wade Wilson's quiet retirement is shattered when the TVA pulls him into an interdimensional mission requiring the claws of Wolverine.",
                      Movie.Status.NOW_SHOWING),
                movie("Interstellar", "Sci-Fi, Adventure, Drama",
                      Movie.Language.English, 169, 8.7, 157336L,
                      "https://image.tmdb.org/t/p/w500/yQvGrMoipbRoddT0ZR8tPoR7NfX.jpg",
                      "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
                      Movie.Status.NOW_SHOWING),
                movie("The Batman", "Action, Crime, Drama",
                      Movie.Language.English, 176, 8.3, 414906L,
                      "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg",
                      "When a sadistic serial killer begins murdering key political figures in Gotham, Batman is forced to investigate the city's hidden corruption.",
                      Movie.Status.NOW_SHOWING),
                movie("Gladiator II", "Action, Adventure, Drama",
                      Movie.Language.English, 148, 8.4, 558449L,
                      "https://image.tmdb.org/t/p/w500/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg",
                      "Decades after Maximus, Lucius enters the Colosseum to fight for the future of Rome and restore glory to its people.",
                      Movie.Status.NOW_SHOWING),
                movie("Avatar: The Way of Water", "Sci-Fi, Adventure, Action",
                      Movie.Language.English, 192, 8.6, 76600L,
                      "https://image.tmdb.org/t/p/w500/t6HIqrRAclMCA60NsSmeqe9RmNV.jpg",
                      "Jake Sully lives with his newfound family formed on the extrasolar moon Pandora once more facing a familiar threat.",
                      Movie.Status.NOW_SHOWING),

                // ── TAMIL ──
                movie("Leo", "Action, Crime, Thriller",
                      Movie.Language.Tamil, 164, 8.0, 1072790L,
                      "https://image.tmdb.org/t/p/w500/gSOVog7ydsaF1YpgAqBqnKYFGY.jpg",
                      "Parthiban lives a quiet life in Theog until a violent incident exposes him to an international cartel seeking a long-lost operative.",
                      Movie.Status.NOW_SHOWING),
                movie("Jailer", "Action, Comedy, Crime",
                      Movie.Language.Tamil, 168, 8.2, 946310L,
                      "https://image.tmdb.org/t/p/w500/upwdEyhhYZ7pT2hD535fr2MGfpx.jpg",
                      "A retired jailer goes on a ruthless manhunt to track down the gangsters who kidnapped his son.",
                      Movie.Status.NOW_SHOWING),
                movie("Amaran", "Action, Biography, Drama",
                      Movie.Language.Tamil, 169, 8.8, 927342L,
                      "https://image.tmdb.org/t/p/w500/eCB06m1KUGilEOlIzb40nkQhVY0.jpg",
                      "The inspiring and courageous journey of Major Mukund Varadarajan and his Rajput Regiment brothers in counter-insurgency operations.",
                      Movie.Status.NOW_SHOWING),
                movie("The Greatest of All Time (GOAT)", "Action, Sci-Fi, Thriller",
                      Movie.Language.Tamil, 179, 8.1, 1184918L,
                      "https://image.tmdb.org/t/p/w500/uYzHkzZPp84B7nHOKaJZWjyGFUL.jpg",
                      "A veteran hostage negotiator and counter-terrorism specialist is pulled back into action when an old nemesis reappears.",
                      Movie.Status.NOW_SHOWING),
                movie("Maharaja", "Action, Crime, Thriller",
                      Movie.Language.Tamil, 140, 8.5, 1118224L,
                      "https://image.tmdb.org/t/p/w500/s0m4TM1XRAftQStgKpw024RvkJo.jpg",
                      "A quiet barber reports his dustbin stolen to the police, sparking a gripping investigation that conceals an intensely personal crusade.",
                      Movie.Status.NOW_SHOWING),
                movie("Vikram", "Action, Thriller, Crime",
                      Movie.Language.Tamil, 175, 8.6, 764835L,
                      "https://image.tmdb.org/t/p/w500/774UV1aCURb4s4JfEFg3IEMu5Zj.jpg",
                      "A black-ops commander investigates a series of murders committed by a masked vigilante group targeting a brutal drug kingpin.",
                      Movie.Status.NOW_SHOWING),

                // ── HINDI ──
                movie("Stree 2", "Comedy, Horror",
                      Movie.Language.Hindi, 149, 8.4, 1114513L,
                      "https://image.tmdb.org/t/p/w500/nfnhwfUEFuSOxxf4jDdBlY6Lccw.jpg",
                      "The residents of Chanderi confront a terrifying headless demon known as Sarkata, requiring an unlikely union of old friends.",
                      Movie.Status.NOW_SHOWING),
                movie("Jawan", "Action, Thriller",
                      Movie.Language.Hindi, 169, 8.3, 872585L,
                      "https://image.tmdb.org/t/p/w500/jFt1gS4BGHlK8xt76Y81Alp4dbt.jpg",
                      "An enigmatic vigilante orchestrates high-stakes operations across the country to expose corruption and bring justice to ordinary citizens.",
                      Movie.Status.NOW_SHOWING),
                movie("12th Fail", "Biography, Drama",
                      Movie.Language.Hindi, 147, 9.1, 1181548L,
                      "https://image.tmdb.org/t/p/w500/eebUPRI4Z5e1Z7Hev4JZAwMIFkX.jpg",
                      "Based on true events, Manoj Kumar Sharma battles extreme poverty and setbacks to restart his journey towards clearing the UPSC exam.",
                      Movie.Status.NOW_SHOWING),
                movie("Animal", "Action, Crime, Drama",
                      Movie.Language.Hindi, 201, 8.0, 781732L,
                      "https://image.tmdb.org/t/p/w500/6pzI1DIlxwAFffw9OZcG6cr031U.jpg",
                      "An all-consuming devotion between father and son spirals into a brutal underworld conflict when an assassination attempt occurs.",
                      Movie.Status.NOW_SHOWING),
                movie("Fighter", "Action, Thriller, War",
                      Movie.Language.Hindi, 166, 8.1, 850165L,
                      "https://image.tmdb.org/t/p/w500/dJPT2Uf12X6aFg8i5N67NTpIZ9t.jpg",
                      "Top Indian Air Force aviators come together to form Air Dragons, an elite aerial combat unit responding to militant threats.",
                      Movie.Status.NOW_SHOWING),
                movie("Bhool Bhulaiyaa 3", "Comedy, Horror",
                      Movie.Language.Hindi, 158, 8.0, 1132644L,
                      "https://image.tmdb.org/t/p/w500/3AfHD1HoaQpQwKH8kxRdBKVmzeU.jpg",
                      "Rooh Baba travels to the royal kingdom of Raktaghat in West Bengal and comes face-to-face with two vengeful spirits.",
                      Movie.Status.NOW_SHOWING)
            );

            if (movieRepo.count() == 0) {
                movieRepo.saveAll(defaultMovies);
                log.info("27 blockbuster movies with real posters seeded.");
            } else {
                for (Movie dm : defaultMovies) {
                    Movie existing = movieRepo.findAll().stream()
                        .filter(m -> m.getTitle().equalsIgnoreCase(dm.getTitle()))
                        .findFirst().orElse(null);
                    if (existing == null) {
                        movieRepo.save(dm);
                    } else {
                        if (existing.getPosterPath() == null || !existing.getPosterPath().equals(dm.getPosterPath())) {
                            existing.setPosterPath(dm.getPosterPath());
                        }
                        existing.setDescription(dm.getDescription());
                        existing.setGenre(dm.getGenre());
                        existing.setLanguage(dm.getLanguage());
                        existing.setRating(dm.getRating());
                        existing.setDurationMin(dm.getDurationMin());
                        if (existing.getCast() == null || existing.getCast().isBlank()) existing.setCast(dm.getCast());
                        if (existing.getProducers() == null || existing.getProducers().isBlank()) existing.setProducers(dm.getProducers());
                        movieRepo.save(existing);
                    }
                }
            }

            // ── 3. Theatres + Screens ──────────────────────
            if (theatreRepo.count() < 10) {
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

                List<Theatre> existingTheatres = theatreRepo.findAll();
                for (Object[] td : theatreData) {
                    String name = (String) td[0];
                    if (existingTheatres.stream().noneMatch(t -> t.getName().equalsIgnoreCase(name))) {
                        Theatre t = theatreRepo.save(Theatre.builder()
                            .name(name)
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
                }
                log.info("Kerala theatres + screens seeded.");
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

            // ── 4. Shows — every movie plays at every Kerala theatre for today + upcoming days ──────
            List<Movie> movies = movieRepo.findAll();
            List<Theatre> allTheatres = theatreRepo.findAll();
            if (!allTheatres.isEmpty() && !movies.isEmpty()) {
                List<Screen> allScreens = screenRepo.findAll();
                java.util.Map<Long, List<Screen>> screensByTheatre = new java.util.LinkedHashMap<>();
                for (Screen screen : allScreens) {
                    screensByTheatre.computeIfAbsent(screen.getTheatre().getId(), k -> new java.util.ArrayList<>()).add(screen);
                }

                List<Show> existingShows = showRepo.findAllWithDetails();
                java.util.Set<String> showKeys = new java.util.HashSet<>();
                for (Show existing : existingShows) {
                    showKeys.add(existing.getMovie().getId() + "|" + existing.getScreen().getId() + "|"
                        + existing.getShowDate() + "|" + existing.getShowTime());
                }

                LocalTime[] times = {
                    LocalTime.of(10, 30),
                    LocalTime.of(14, 0),
                    LocalTime.of(18, 30),
                    LocalTime.of(21, 30)
                };

                java.util.List<Show> batch = new java.util.ArrayList<>();
                LocalDate today = LocalDate.now();

                for (Movie movie : movies) {
                    for (Theatre theatre : allTheatres) {
                        List<Screen> tScreens = screensByTheatre.get(theatre.getId());
                        if (tScreens == null || tScreens.isEmpty()) continue;
                        Screen screen = tScreens.get((int) (movie.getId() % tScreens.size()));

                        for (int d = 0; d < 7; d++) {
                            LocalDate date = today.plusDays(d);
                            // Schedule 2 distinct showtimes per movie per theatre
                            int t1 = (int) ((movie.getId() + d) % times.length);
                            int t2 = (int) ((movie.getId() + d + 2) % times.length);
                            LocalTime[] chosenTimes = { times[t1], times[t2] };

                            for (LocalTime time : chosenTimes) {
                                String key = movie.getId() + "|" + screen.getId() + "|" + date + "|" + time;
                                if (showKeys.contains(key)) continue;

                                batch.add(Show.builder()
                                    .movie(movie)
                                    .screen(screen)
                                    .showDate(date)
                                    .showTime(time)
                                    .priceStd(BigDecimal.valueOf(220))
                                    .pricePrem(BigDecimal.valueOf(380))
                                    .build());
                                showKeys.add(key);
                            }
                        }
                    }
                }
                if (!batch.isEmpty()) {
                    showRepo.saveAll(batch);
                    log.info("{} shows added across all Kerala theatres for today and upcoming 7 days.", batch.size());
                } else {
                    log.info("Upcoming shows already exist across all theatres.");
                }
            }
        };
    }

    private Movie movie(String title, String genre, Movie.Language lang,
                        int dur, double rating, Long tmdbId, String posterPath, String desc, Movie.Status status) {
        String cast = switch (title) {
            case "Bramayugam" -> "Mammootty, Arjun Ashokan, Sidharth Bharathan, Amalda Liz";
            case "The Goat Life (Aadujeevitham)" -> "Prithviraj Sukumaran, Jimmy Jean-Louis, KR Gokul, Amala Paul";
            case "Manjummel Boys" -> "Soubin Shahir, Sreenath Bhasi, Balu Varghese, Ganapathi";
            case "Premalu" -> "Naslen K. Gafoor, Mamitha Baiju, Sangeeth Prathap, Shyam Mohan";
            case "Dune: Part Two" -> "Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem";
            case "Oppenheimer" -> "Cillian Murphy, Emily Blunt, Matt Damon, Robert Downey Jr.";
            case "Deadpool & Wolverine" -> "Ryan Reynolds, Hugh Jackman, Emma Corrin, Morena Baccarin";
            case "Leo" -> "Vijay, Sanjay Dutt, Trisha Krishnan, Arjun Sarja";
            case "Jailer" -> "Rajinikanth, Mohanlal, Shiva Rajkumar, Ramya Krishnan";
            case "Stree 2" -> "Shraddha Kapoor, Rajkummar Rao, Pankaj Tripathi, Abhishek Banerjee";
            case "12th Fail" -> "Vikrant Massey, Medha Shankr, Anant V Joshi, Anshumaan Pushkar";
            default -> "Ensemble Cast, Lead Actors";
        };
        String producers = switch (title) {
            case "Bramayugam" -> "Chakravarthy Ramachandra, S. Sashikanth (Night Shift Studios)";
            case "The Goat Life (Aadujeevitham)" -> "Blessy, Jimmy Jean-Louis, Steven Adams (Visual Romance)";
            case "Manjummel Boys" -> "Babu Shahir, Soubin Shahir, Shawn Antony (Parava Films)";
            case "Premalu" -> "Fahadh Faasil, Dileesh Pothan, Syam Pushkaran (Bhavana Studios)";
            case "Dune: Part Two" -> "Mary Parent, Cale Boyter, Denis Villeneuve, Tanya Lapointe (Legendary Pictures)";
            case "Oppenheimer" -> "Emma Thomas, Charles Roven, Christopher Nolan (Syncopy)";
            case "Leo" -> "S. S. Lalit Kumar, Jagadish Palanisamy (Seven Screen Studio)";
            default -> "TicketHub Premiere Studios & Productions";
        };

        return Movie.builder()
            .title(title).genre(genre).language(lang)
            .durationMin(dur).rating(rating)
            .tmdbId(tmdbId).posterPath(posterPath).description(desc).status(status)
            .cast(cast).producers(producers)
            .build();
    }
}
