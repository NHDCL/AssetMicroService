package bt.nhdcl.assetmicroservice.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRForRooms{

    private final Cloudinary cloudinary;

    public QRForRooms(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // Create QR Code Image from String
    private BufferedImage createQRCodeImage(String data) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 1); // minimal margin

        BitMatrix matrix = new MultiFormatWriter().encode(
                data, BarcodeFormat.QR_CODE, 300, 300, hints
        );

        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    // Generate and Upload QR Code to Cloudinary
    public String generateQRCode(String data, String publicId) {
        try {
            BufferedImage qrImage = createQRCodeImage(data);

            File tempFile = File.createTempFile("qr-", ".png");
            ImageIO.write(qrImage, "png", tempFile);

            Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.asMap(
                    "public_id", "qr_codes/" + publicId,
                    "overwrite", false
            ));

            // Delete temporary file
            Files.deleteIfExists(tempFile.toPath());

            return (String) uploadResult.get("secure_url");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
