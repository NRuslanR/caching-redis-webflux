package org.example.redis_cache;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class IntegrationTestsConfig 
{
    private static MongoDBContainer mongodb =
        new MongoDBContainer(
            DockerImageName.parse("mongo:4.0.10")
        );

    private static GenericContainer<?> redis =
        new GenericContainer<>(
                DockerImageName.parse("redis:7.0")
        )
        .withExposedPorts(6379);

    static
    {
        mongodb.start();

        System.setProperty("spring.data.mongodb.uri", mongodb.getReplicaSetUrl());

        redis.start();
    }

}
