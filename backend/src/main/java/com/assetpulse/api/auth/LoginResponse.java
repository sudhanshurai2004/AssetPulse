package com.assetpulse.api.auth;

public record LoginResponse(String token, String username, UserRole role) {
}
