package com.minimarket.security.monitor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class SuspiciousActivityService {

    private static final Logger logger = LoggerFactory.getLogger(SuspiciousActivityService.class);

    private final ConcurrentHashMap<String, List<Long>> failedLoginTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Long>> requestTimestampsByIp = new ConcurrentHashMap<>();
    private final int FAILED_LOGIN_THRESHOLD = 5;
    private final int REQUEST_THRESHOLD = 200;
    private final long WINDOW_MS = 15 * 60 * 1000L;

    private String clientIp(HttpServletRequest request) {
        String xf = request.getHeader("x-forwarded-for");
        return (xf != null && !xf.isBlank() ? xf.split(",")[0].trim() : request.getRemoteAddr());
    }

    private void pruneOld(List<Long> list) {
        long cutOff = Instant.now().toEpochMilli() - WINDOW_MS;
        list.removeIf(t -> t < cutOff);
    }

    public void recordFailedLogin(HttpServletRequest req, String username) {
        String key = "FAILED_LOGIN: " + (username == null ? clientIp(req) : username + "e" + clientIp(req));
        List<Long> list = failedLoginTimestamps.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (list) {
            list.add(Instant.now().toEpochMilli());
            pruneOld(list);
            int count = list.size();
            logger.warn("Suspicious activity detected: failed login (user={}, ip={}, count={})", username,
                    clientIp(req), count);
            if (count >= FAILED_LOGIN_THRESHOLD) {
                logger.warn("Suspicious activity detected: treshold for failed login (user/ip={}): {}", key, count);
            }
        }
    }

    public void recordInvalidJwt(HttpServletRequest req, String token, Exception ex) {
        String ip = clientIp(req);
        logger.warn("Suspicious activity detected: Invalid JWT from ip={} path={} reason={}", ip, req.getRequestURI(),
                ex == null ? "invalid/expired" : ex.getMessage());
    }

    public void recordRequest(HttpServletRequest req) {
        String ip = clientIp(req);
        List<Long> list = requestTimestampsByIp.computeIfAbsent(ip, k -> new ArrayList<>());
        synchronized (list) {
            list.add(Instant.now().toEpochMilli());
            pruneOld(list);
            int count = list.size();
            if (count % 50 == 0) {
                logger.info("Suspicious activity detected: request rate ip={} count_last_15_min={}", ip, count);
            }
            if (count >= REQUEST_THRESHOLD) {
                logger.warn("Suspicious activity detected: high request rate ip={} count_last_15_min={}", ip, count);
            }
        }
    }

    public void recordUnauthorizedAccess(HttpServletRequest req, String resource) {
        String ip = clientIp(req);
        logger.warn("Suspicious activity detected: unauthorized access attempt ip={} path={} resource={}", ip,
                req.getRequestURI(), resource);
    }

    public void recordCrudOperation(HttpServletRequest req, String operation, String resource) {
        String ip = clientIp(req);
        logger.info("Suspicious activity detected: CRUD op={} resource={} by ip={} path={}", operation, resource, ip,
                req.getRequestURI());
    }

}
