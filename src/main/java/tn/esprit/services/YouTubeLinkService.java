package tn.esprit.services;

import java.net.URI;
import java.net.URISyntaxException;

public class YouTubeLinkService {

    public boolean isYoutubeUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(url.trim());
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            String normalizedHost = host.toLowerCase();
            return normalizedHost.contains("youtube.com") || normalizedHost.contains("youtu.be");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public String normalizeForOpen(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return "";
        }
        String url = rawUrl.trim();
        if (!isYoutubeUrl(url)) {
            return url;
        }

        String videoId = extractVideoId(url);
        if (videoId == null || videoId.isBlank()) {
            return url;
        }
        return "https://www.youtube.com/watch?v=" + videoId + "&autoplay=1";
    }

    private String extractVideoId(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            String path = uri.getPath() == null ? "" : uri.getPath();

            if (host.contains("youtu.be")) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                int slash = path.indexOf('/');
                return slash >= 0 ? path.substring(0, slash) : path;
            }

            String query = uri.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=", 2);
                    if (kv.length == 2 && "v".equalsIgnoreCase(kv[0]) && !kv[1].isBlank()) {
                        return kv[1];
                    }
                }
            }

            if (path.startsWith("/shorts/")) {
                return path.replace("/shorts/", "").split("/")[0];
            }
            if (path.startsWith("/embed/")) {
                return path.replace("/embed/", "").split("/")[0];
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
