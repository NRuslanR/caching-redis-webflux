package org.example.redis_cache;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Validated
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "ItemCache")
public class ItemServiceImpl implements ItemService 
{
    private final ItemRepository itemRepository;

    @Override
    @Cacheable(key = "#itemId")
    public Mono<Item> getItem(@NotBlank String itemId) 
    {
       return 
            itemRepository
                .findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException()));
    }
}
