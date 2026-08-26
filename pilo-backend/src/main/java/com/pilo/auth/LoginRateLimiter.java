package com.pilo.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * In-memory sliding-window limiter for the login endpoint, keyed by client IP.
 * Mitigates brute-force/credential-stuffing without penalizing legitimate users on shared
 * IPs too harshly. Single-instance only: a clustered deployment should move this to a shared
 * store (e.g. Redis) instead.
 */
@Component
public class LoginRateLimiter {

	private static final int MAX_ATTEMPTS = 10;
	private static final Duration WINDOW = Duration.ofMinutes(5);

	private final Clock clock;
	private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

	public LoginRateLimiter() {
		this(Clock.systemUTC());
	}

	LoginRateLimiter(Clock clock) {
		this.clock = clock;
	}

	public boolean tryAcquire(String key) {
		Instant now = clock.instant();
		Bucket bucket = buckets.compute(key, (ignored, existing) -> {
			if (existing == null || existing.windowStart.plus(WINDOW).isBefore(now)) {
				return new Bucket(now);
			}
			return existing;
		});
		return bucket.attempts.incrementAndGet() <= MAX_ATTEMPTS;
	}

	@Scheduled(fixedRate = 10, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
	void evictExpired() {
		Instant now = clock.instant();
		buckets.entrySet().removeIf(entry -> entry.getValue().windowStart.plus(WINDOW).isBefore(now));
	}

	private static final class Bucket {
		private final Instant windowStart;
		private final AtomicInteger attempts = new AtomicInteger(0);

		private Bucket(Instant windowStart) {
			this.windowStart = windowStart;
		}
	}
}
