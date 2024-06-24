package org.example.redis_cache;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import reactor.core.publisher.Mono;

@Validated
public interface ItemService 
{
    public Mono<Item> getItem(@NotBlank String itemId);
}
