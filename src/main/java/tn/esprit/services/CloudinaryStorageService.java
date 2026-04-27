package tn.esprit.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import tn.esprit.config.LocalSecrets;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class CloudinaryStorageService {
    private final Cloudinary cloudinary;
    private final boolean enabled;

    public CloudinaryStorageService() {
        String cloudinaryUrl = LocalSecrets.get("CLOUDINARY_URL");
        String cloudName = LocalSecrets.get("CLOUDINARY_CLOUD_NAME");
        String apiKey = LocalSecrets.get("CLOUDINARY_API_KEY");
        String apiSecret = LocalSecrets.get("CLOUDINARY_API_SECRET");

        if (!isBlank(cloudinaryUrl) && !isPlaceholder(cloudinaryUrl)) {
            this.cloudinary = new Cloudinary(cloudinaryUrl);
            this.cloudinary.config.secure = true;
            this.enabled = true;
            return;
        }

        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)
                || isPlaceholder(cloudName) || isPlaceholder(apiKey) || isPlaceholder(apiSecret)) {
            this.cloudinary = null;
            this.enabled = false;
            return;
        }

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String upload(Path filePath, String resourceType) throws IOException {
        String mapped = mapResourceType(resourceType);
        if ("image".equals(mapped)) {
            return uploadImage(filePath);
        }
        if ("video".equals(mapped)) {
            return uploadVideo(filePath);
        }

        if (!enabled || cloudinary == null) {
            throw new IllegalStateException("Cloudinary n'est pas configure");
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                filePath.toFile(),
                ObjectUtils.asMap(
                        "resource_type", mapResourceType(resourceType),
                        "folder", "eduflex/resources"
                )
        );

        Object secureUrl = result.get("secure_url");
        if (secureUrl == null) {
            throw new IOException("Cloudinary n'a pas retourne d'URL securisee");
        }
        return secureUrl.toString();
    }

    public String uploadImage(Path filePath) throws IOException {
        return uploadByType(filePath, "image");
    }

    public String uploadVideo(Path filePath) throws IOException {
        return uploadByType(filePath, "video");
    }

    private String uploadByType(Path filePath, String type) throws IOException {
        if (!enabled || cloudinary == null) {
            throw new IllegalStateException("Cloudinary n'est pas configure");
        }
        if (filePath == null || !filePath.toFile().exists()) {
            throw new IOException("Fichier introuvable pour l'upload Cloudinary");
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                filePath.toFile(),
                ObjectUtils.asMap(
                        "resource_type", type,
                        "folder", "eduflex/resources"
                )
        );

        Object secureUrl = result.get("secure_url");
        if (secureUrl == null) {
            throw new IOException("Cloudinary n'a pas retourne d'URL securisee");
        }
        return secureUrl.toString();
    }

    private String mapResourceType(String type) {
        if (type == null) {
            return "raw";
        }

        switch (type.toLowerCase()) {
            case "image":
                return "image";
            case "video":
                return "video";
            case "audio":
            case "pdf":
            default:
                return "raw";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isPlaceholder(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.contains("your-cloud")
                || normalized.contains("your-cloudinary")
                || normalized.contains("<api_key>")
                || normalized.contains("<api_secret>")
                || normalized.contains("<cloud_name>");
    }

}
