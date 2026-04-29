package tn.esprit.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class QrCodeService {
    private static final int DEFAULT_SIZE = 180;

    public Image generateImage(String content, int size) {
        try {
            BufferedImage bufferedImage = generateBufferedImage(content, size);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            return null;
        }
    }

    public Path generateAndSave(String content) throws IOException {
        if (content == null || content.isBlank()) {
            throw new IOException("Contenu QR code vide");
        }

        Path qrDir = Path.of("storage", "qrcodes").toAbsolutePath().normalize();
        Files.createDirectories(qrDir);
        Path target = qrDir.resolve(hash(content) + ".png");

        if (Files.exists(target)) {
            return target;
        }

        try {
            ImageIO.write(generateBufferedImage(content, DEFAULT_SIZE), "png", target.toFile());
            return target;
        } catch (WriterException e) {
            throw new IOException("Impossible de generer le QR code", e);
        }
    }

    private BufferedImage generateBufferedImage(String content, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
        return MatrixToImageWriter.toBufferedImage(matrix);
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
}
