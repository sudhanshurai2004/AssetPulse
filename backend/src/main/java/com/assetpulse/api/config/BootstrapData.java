package com.assetpulse.api.config;

import com.assetpulse.api.asset.Asset;
import com.assetpulse.api.asset.AssetRepository;
import com.assetpulse.api.asset.AssetStatus;
import com.assetpulse.api.auth.AppUser;
import com.assetpulse.api.auth.AppUserRepository;
import com.assetpulse.api.auth.UserRole;
import com.assetpulse.api.location.Location;
import com.assetpulse.api.location.LocationRepository;
import com.assetpulse.api.technician.Technician;
import com.assetpulse.api.technician.TechnicianRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapData {
    @Bean
    CommandLineRunner seedDemoData(
            AppUserRepository users,
            AssetRepository assets,
            TechnicianRepository technicians,
            LocationRepository locations,
            PasswordEncoder encoder,
            @Value("${app.bootstrap.username}") String username,
            @Value("${app.bootstrap.password}") String password) {
        return args -> {
            if (!users.existsByUsername(username)) {
                users.save(new AppUser(username, encoder.encode(password), UserRole.ADMIN));
            }
            if (assets.count() == 0) {
                Location northPlant = location(locations, "North Plant");
                Location officeBlock = location(locations, "Office Block");
                Location utilityBuilding = location(locations, "Utility Building");
                assets.save(new Asset("Cooling Water Pump", "PMP-001", "Pump", northPlant, AssetStatus.ACTIVE, 80));
                assets.save(new Asset("Air Handling Unit", "HVAC-001", "HVAC", officeBlock, AssetStatus.ACTIVE, 70));
                assets.save(new Asset("Backup Generator", "GEN-001", "Generator", utilityBuilding, AssetStatus.ACTIVE, 90));
            }
            if (technicians.count() == 0) {
                technicians.save(new Technician("Avery Morgan", "avery.morgan@example.test", true));
                technicians.save(new Technician("Jordan Lee", "jordan.lee@example.test", true));
            }
        };
    }

    private Location location(LocationRepository locations, String name) {
        return locations.findByNameIgnoreCase(name).orElseGet(() -> locations.save(new Location(name)));
    }
}
