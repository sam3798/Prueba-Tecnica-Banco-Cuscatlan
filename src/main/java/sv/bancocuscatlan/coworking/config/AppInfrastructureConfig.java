package sv.bancocuscatlan.coworking.config;

import java.time.Duration;
import java.util.concurrent.Executor;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
@EnableAsync
@EnableConfigurationProperties(PaymentProperties.class)
public class AppInfrastructureConfig {

    public static final String OCCUPANCY_CACHE = "ocupacion";

    @Bean(name = "notificationExecutor")
    Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("notify-");
        executor.initialize();
        return executor;
    }

    @Bean
    CacheManagerCustomizer<CaffeineCacheManager> caffeineCacheCustomizer() {
        return cacheManager -> {
            cacheManager.setCacheNames(java.util.List.of(OCCUPANCY_CACHE));
            cacheManager.setCaffeine(Caffeine.newBuilder()
                    .maximumSize(200)
                    .expireAfterWrite(Duration.ofMinutes(10)));
        };
    }
}
