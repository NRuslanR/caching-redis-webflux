package org.example.redis_cache;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Testcontainers
@Import({IntegrationTestsConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
@Tag("IntegrationTest")
public class ItemServiceTests 
{
    @Autowired
    @MockBean
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Test
    public void should_ReturnItemOnlyOnce_When_CacheUsing()
    {
        var itemId = "Item#1";

        when(itemRepository.findById(anyString()))
            .thenAnswer(a -> Mono.just(Item.of(a.getArgument(0), a.getArgument(0))));

        var itemFuture = itemService.getItem(itemId);

        StepVerifier
            .create(itemFuture)
            .assertNext(item -> {

                var sameItemFuture = itemService.getItem(itemId);

                StepVerifier
                    .create(sameItemFuture)
                    .assertNext(sameItem -> {

                        verify(itemRepository, times(1)).findById(anyString());

                        assertEquals(item, sameItem);   

                    })
                    .verifyComplete();

            })
            .verifyComplete();
    }
}
