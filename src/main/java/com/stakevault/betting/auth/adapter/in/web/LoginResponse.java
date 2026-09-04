package com.stakevault.betting.auth.adapter.in.web;

public record LoginResponse(String token, boolean mustChangePassword) {
}
