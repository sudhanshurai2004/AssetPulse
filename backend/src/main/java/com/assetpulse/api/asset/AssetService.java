package com.assetpulse.api.asset;

import com.assetpulse.api.common.ResourceNotFoundException;
import com.assetpulse.api.location.Location;
import com.assetpulse.api.location.LocationRepository;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetService {
    private final AssetRepository assets;
    private final LocationRepository locations;

    public AssetService(AssetRepository assets, LocationRepository locations) {
        this.assets = assets;
        this.locations = locations;
    }

    @Transactional(readOnly = true)
    public Page<AssetResponse> list(String query, AssetStatus status, Pageable pageable) {
        Specification<Asset> specification = Specification.where(null);
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, criteria, builder) -> builder.or(
                    builder.like(builder.lower(root.get("name")), pattern),
                    builder.like(builder.lower(root.get("assetTag")), pattern),
                    builder.like(builder.lower(root.join("location").get("name")), pattern)));
        }
        if (status != null) {
            specification = specification.and((root, criteria, builder) -> builder.equal(root.get("status"), status));
        }
        return assets.findAll(specification, pageable).map(AssetResponse::from);
    }

    @Transactional(readOnly = true)
    public Asset getEntity(Long id) {
        return assets.findById(id).orElseThrow(() -> new ResourceNotFoundException("Asset", id));
    }

    @Transactional(readOnly = true)
    public AssetResponse get(Long id) {
        return AssetResponse.from(getEntity(id));
    }

    @Transactional
    public AssetResponse create(AssetRequest request) {
        return AssetResponse.from(assets.save(new Asset(
                request.name().trim(), request.assetTag().trim(), request.type().trim(),
                locationFor(request.location()), request.status(), request.threshold())));
    }

    @Transactional
    public AssetResponse update(Long id, AssetRequest request) {
        Asset asset = getEntity(id);
        if (!asset.getAssetTag().equals(request.assetTag().trim())
                && assets.existsByAssetTag(request.assetTag().trim())) {
            throw new IllegalArgumentException("Asset tag is already in use");
        }
        asset.update(request.name().trim(), request.assetTag().trim(), request.type().trim(),
                locationFor(request.location()),
                request.status(), request.threshold());
        return AssetResponse.from(asset);
    }

    @Transactional
    public void delete(Long id) {
        assets.delete(getEntity(id));
    }

    private Location locationFor(String name) {
        String normalized = name.trim();
        return locations.findByNameIgnoreCase(normalized)
                .orElseGet(() -> locations.save(new Location(normalized)));
    }
}
