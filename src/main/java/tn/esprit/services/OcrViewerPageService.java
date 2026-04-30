package tn.esprit.services;

import tn.esprit.config.LocalSecrets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OcrViewerPageService {

    private static final String DEFAULT_OCR_KEY = "helloworld";

    private final CloudinaryStorageService cloudinaryStorageService;

    public OcrViewerPageService(CloudinaryStorageService cloudinaryStorageService) {
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    public String createViewerUrl(String title, String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IOException("URL image invalide pour la page OCR.");
        }

        Path htmlPath = writeViewerPage(title, imageUrl);
        if (cloudinaryStorageService != null && cloudinaryStorageService.isEnabled()) {
            return cloudinaryStorageService.uploadRaw(htmlPath);
        }
        return htmlPath.toUri().toString();
    }

    private Path writeViewerPage(String title, String imageUrl) throws IOException {
        Path dir = Path.of("storage", "ocr-pages").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path target = dir.resolve(hash(imageUrl) + ".html");
        Files.writeString(target, html(title, imageUrl), StandardCharsets.UTF_8);
        return target;
    }

    private String html(String title, String imageUrl) {
        String safeTitle = escapeHtml(title == null || title.isBlank() ? "Ressource OCR" : title);
        String jsImageUrl = escapeJs(imageUrl);
        String jsOcrKey = escapeJs(ocrApiKey());

        return """
                <!doctype html>
                <html lang="fr">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <title>%s</title>
                  <style>
                    *{box-sizing:border-box}body{margin:0;font-family:Arial,sans-serif;background:#eef1ff;color:#251a45}
                    main{max-width:980px;margin:auto;padding:18px}.hero{display:grid;grid-template-columns:minmax(0,1fr);gap:14px}
                    img{width:100%%;max-height:62vh;object-fit:contain;background:#111;border-radius:10px}
                    .panel{background:#fffaff;border:1px solid #dfd4f2;border-radius:10px;padding:14px;box-shadow:0 8px 24px rgba(79,56,128,.12)}
                    h1{font-size:22px;margin:0 0 12px}.bar{display:flex;gap:8px;flex-wrap:wrap;margin:10px 0}
                    button,select{border:0;border-radius:9px;padding:10px 13px;font-weight:700}
                    button{background:#6f5cc2;color:white;cursor:pointer}button.secondary{background:#e9e2fb;color:#493286}
                    button.copy{background:#e4f8ec;color:#17643a}button.warn{background:#fff0c9;color:#855b00}
                    textarea{width:100%%;min-height:180px;border:1px solid #d9cdee;border-radius:10px;padding:12px;font-size:15px;line-height:1.45;background:#fffaff;color:#251a45}
                    textarea.hidden{display:none}
                    #status{color:#756b8e;font-weight:700;margin:8px 0}
                    @media (min-width:860px){.hero{grid-template-columns:1fr 1fr;align-items:start}}
                  </style>
                </head>
                <body>
                <main>
                  <h1>%s</h1>
                  <section class="hero">
                    <img src="%s" alt="Ressource">
                    <div class="panel">
                      <div id="status">Image Cloudinary prete. Lance le scan OCR.</div>
                      <textarea id="text" class="hidden" placeholder="Le texte extrait apparait ici."></textarea>
                      <div class="bar">
                        <button id="scan">Scanner l'image</button>
                        <button class="copy" id="copy">Copy</button>
                        <select id="lang">
                          <option value="fr">Francais</option>
                          <option value="ar">Arabe</option>
                          <option value="en">Anglais</option>
                        </select>
                        <button class="warn" id="translate">Traduire</button>
                        <button class="secondary" id="search">Rechercher</button>
                      </div>
                    </div>
                  </section>
                </main>
                <script>
                  const IMAGE_URL = "%s";
                  const OCR_KEY = "%s";
                  const text = document.getElementById('text');
                  const status = document.getElementById('status');
                  const setStatus = (value) => status.textContent = value;
                  document.getElementById('scan').onclick = async () => {
                    setStatus('Analyse OCR en cours...');
                    const form = new FormData();
                    form.append('apikey', OCR_KEY);
                    form.append('url', IMAGE_URL);
                    form.append('language', 'fre');
                    form.append('OCREngine', '2');
                    form.append('scale', 'true');
                    form.append('detectOrientation', 'true');
                    form.append('isOverlayRequired', 'false');
                    try {
                      const res = await fetch('https://api.ocr.space/parse/image', {method:'POST', body:form});
                      const data = await res.json();
                      if (data.IsErroredOnProcessing) throw new Error(String(data.ErrorMessage || data.ErrorDetails || 'OCR indisponible'));
                      const extracted = (data.ParsedResults || []).map(r => r.ParsedText || '').join('\\n').trim();
                      text.value = extracted || 'Aucun texte detecte dans cette image.';
                      text.classList.remove('hidden');
                      setStatus(extracted ? 'OCR termine: texte extrait.' : 'OCR termine: aucun texte detecte.');
                    } catch (e) {
                      setStatus('OCR indisponible: ' + e.message);
                    }
                  };
                  document.getElementById('copy').onclick = async () => {
                    if (!text.value.trim()) return setStatus('Aucun texte a copier.');
                    await navigator.clipboard.writeText(text.value);
                    setStatus('Texte copie.');
                  };
                  document.getElementById('translate').onclick = async () => {
                    if (!text.value.trim()) return setStatus('Aucun texte a traduire.');
                    setStatus('Traduction en cours...');
                    const lang = document.getElementById('lang').value;
                    const url = 'https://api.mymemory.translated.net/get?q=' + encodeURIComponent(text.value) + '&langpair=auto|' + lang;
                    try {
                      const res = await fetch(url);
                      const data = await res.json();
                      text.value = data?.responseData?.translatedText || 'Traduction indisponible.';
                      setStatus('Traduction terminee.');
                    } catch (e) {
                      setStatus('Traduction indisponible: ' + e.message);
                    }
                  };
                  document.getElementById('search').onclick = () => {
                    if (!text.value.trim()) return setStatus('Aucun texte a rechercher.');
                    window.open('https://www.google.com/search?q=' + encodeURIComponent(text.value.slice(0, 220)), '_blank');
                    setStatus('Recherche ouverte.');
                  };
                </script>
                </body>
                </html>
                """.formatted(safeTitle, safeTitle, escapeHtmlAttribute(imageUrl), jsImageUrl, jsOcrKey);
    }

    private String ocrApiKey() {
        String configured = LocalSecrets.get("OCR_SPACE_API_KEY");
        if (configured == null || configured.isBlank() || configured.contains("your-")) {
            return DEFAULT_OCR_KEY;
        }
        return configured.trim();
    }

    private String hash(String value) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : encoded) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 indisponible", e);
        }
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String escapeHtmlAttribute(String value) {
        return escapeHtml(value).replace("\"", "&quot;");
    }

    private String escapeJs(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
