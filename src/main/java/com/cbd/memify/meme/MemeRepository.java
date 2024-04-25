package com.cbd.memify.meme;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MemeRepository extends MongoRepository<Meme, String> {

    Optional<Meme> findByName(String name);
    List<Meme> findByUserId(String id);

}
