package tn.esprit.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class CloudinaryStorageService {
    // Configuration Cloudinary en dur (peut etre remplacee par vos valeurs)
    private static final String CLOUDINARY_CLOUD_NAME = "";
    private static final String CLOUDINARY_API_KEY = "426628859815675";
    private static final String CLOUDINARY_API_SECRET = "FI3inNpR6i0jw-Gvt4Pmkc8HCQ0";

    private final Cloudinary cloudinary;
    private final boolean enabled;

    public CloudinaryStorageService() {
        String cloudinaryUrl = System.getenv("CLOUDINARY_URL");
        String cloudName = firstNonBlank(CLOUDINARY_CLOUD_NAME, System.getenv("CLOUDINARY_CLOUD_NAME"));
        String apiKey = firstNonBlank(CLOUDINARY_API_KEY, System.getenv("CLOUDINARY_API_KEY"));
        String apiSecret = firstNonBlank(CLOUDINARY_API_SECRET, System.getenv("CLOUDINARY_API_SECRET"));

        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            this.cloudinary = new Cloudinary(cloudinaryUrl);
            this.cloudinary.config.secure = true;
            this.enabled = true;
            return;
        }

        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
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

    private String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }
}
