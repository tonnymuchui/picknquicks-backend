package com.picknquicks.service;

import com.picknquicks.util.IdempotencyKey;
import com.picknquicks.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    @Transactional
    public Optional<IdempotencyKey> findByKey(String key) {
        return repository.findByKey(key)
                .filter(ik -> ik.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Transactional
    public IdempotencyKey save(String key, String requestHash, String responseBody, Integer status) {
        IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                .key(key)
                .requestHash(requestHash)
                .responseBody(responseBody)
                .responseStatus(status)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        return repository.save(idempotencyKey);
    }

    @Transactional
    public void deleteExpired() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}