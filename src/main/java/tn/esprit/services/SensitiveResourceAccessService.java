package tn.esprit.services;

import tn.esprit.config.LocalSecrets;
import tn.esprit.entities.User;
import tn.esprit.entities.resources;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

public class SensitiveResourceAccessService {
    private static final long DEFAULT_TTL_SECONDS = 300L;
    private static final String HMAC_ALGO = "HmacSHA256";

    private final AuditLogService auditLogService = new AuditLogService();

    public boolean canAccess(User user, resources resource) {
        if (resource == null) {
            return false;
        }
        if (!resource.isSensitive()) {
            return true;
        }
        return user != null && user.getId() > 0;
    }

    public String issueToken(resources resource, User user) {
        if (resource == null || user == null || user.getId() <= 0) {
            return "";
        }
        long expiryEpoch = Instant.now().getEpochSecond() + DEFAULT_TTL_SECONDS;
        String payload = resource.getId() + ":" + user.getId() + ":" + expiryEpoch;
        String signature = sign(payload);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(resources resource, User user, String token) {
        if (resource == null || user == null || token == null || token.isBlank()) {
            return false;
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            String all = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = all.split(":");
            if (parts.length != 4) {
                return false;
            }

            int resourceId = Integer.parseInt(parts[0]);
            int userId = Integer.parseInt(parts[1]);
            long expiry = Long.parseLong(parts[2]);
            String signature = parts[3];

            if (resourceId != resource.getId() || userId != user.getId()) {
                return false;
            }
            if (Instant.now().getEpochSecond() > expiry) {
                return false;
            }

            String payload = resourceId + ":" + userId + ":" + expiry;
            String expectedSignature = sign(payload);
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    public void logAccess(User user, resources resource, boolean success, String reason) {
        String actorEmail = user != null ? user.getEmail() : "anonymous";
        String details = "resourceId=" + (resource != null ? resource.getId() : -1)
                + ", sensitive=" + (resource != null && resource.isSensitive())
                + ", success=" + success
                + ", reason=" + (reason == null ? "" : reason);
        auditLogService.log(actorEmail, "SENSITIVE_RESOURCE_ACCESS", details);
    }

    private String sign(String payload) {
        try {
            String secret = LocalSecrets.get("RESOURCE_ACCESS_SECRET");
            if (secret == null || secret.isBlank()) {
                secret = "CHANGE_ME_RESOURCE_ACCESS_SECRET";
            }
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur de signature du token interne", e);
        }
    }
}
