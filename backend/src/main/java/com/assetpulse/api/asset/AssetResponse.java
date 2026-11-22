package com.assetpulse.api.asset;

import java.time.Instant;

public record AssetResponse(
        Long id,
        String name,
        String assetTag,
        String type,
        String location,
        AssetStatus status,
        double threshold,
        Instant createdAt) {
    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(), asset.getName(), asset.getAssetTag(), asset.getType(),
                asset.getLocation().getName(), asset.getStatus(), asset.getThreshold(), asset.getCreatedAt());
    }
}
