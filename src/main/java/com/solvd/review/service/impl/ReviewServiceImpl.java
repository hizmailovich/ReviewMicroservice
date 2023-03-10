package com.solvd.review.service.impl;

import com.solvd.review.domain.Review;
import com.solvd.review.domain.criteria.SearchCriteria;
import com.solvd.review.domain.exception.ResourceNotFoundException;
import com.solvd.review.persistence.ReviewRepository;
import com.solvd.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final static String MOVIE_URL = "http://movie/api/v1/movies";
    private final ReviewRepository reviewRepository;
    private final WebClient.Builder webClientBuilder;

    @Override
    public Flux<Review> retrieveByCriteria(SearchCriteria searchCriteria) {
        return Objects.nonNull(searchCriteria) && Objects.nonNull(searchCriteria.getMovieId()) ?
                reviewRepository.findByMovieId(searchCriteria.getMovieId()) :
                reviewRepository.findAll();
    }

    @Override
    public Mono<Review> create(Review review) {
        Mono<Boolean> exists = webClientBuilder.build()
                .get()
                .uri(MOVIE_URL + "/exists/{movieId}", review.getMovieId())
                .retrieve()
                .bodyToMono(Boolean.class);
        return exists.flatMap(value -> {
            if (Boolean.FALSE.equals(value)) {
                return Mono.error(new ResourceNotFoundException("Movie with id = " + review.getMovieId() + " doesn't exist!"));
            } else {
                review.setDate(LocalDate.now());
                return reviewRepository.save(review);
            }
        });
    }

}
