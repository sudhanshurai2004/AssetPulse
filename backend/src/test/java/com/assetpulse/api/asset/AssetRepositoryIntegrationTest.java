package com.assetpulse.api.asset;

import static org.assertj.core.api.Assertions.assertThat;

import com.assetpulse.api.location.Location;
import com.assetpulse.api.location.LocationRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
class AssetRepositoryIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private AssetRepository assets;

    @Autowired
    private LocationRepository locations;

    @Test
    void persistsAssetFieldsToPostgres() {
        Location location = locations.saveAndFlush(new Location("North Plant"));
        Asset saved = assets.saveAndFlush(new Asset(
                "Cooling Pump", "PMP-001", "Pump", location, AssetStatus.ACTIVE, 80));

        Asset loaded = assets.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getAssetTag()).isEqualTo("PMP-001");
        assertThat(loaded.getThreshold()).isEqualTo(80);
        assertThat(loaded.getCreatedAt()).isNotNull();
    }
}
