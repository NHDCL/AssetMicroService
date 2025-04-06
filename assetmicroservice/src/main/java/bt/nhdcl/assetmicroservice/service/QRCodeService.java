package bt.nhdcl.assetmicroservice.service;

import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    private final Cloudinary cloudinary;

    public QRCodeService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @SuppressWarnings("unchecked")
    public String generateQRCode(String text) {
        try {
            // QR code generation logic
            int width = 200;
            int height = 200;
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = new com.google.zxing.MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Create a temporary file for the QR image
            File tempFile = File.createTempFile("qr_code_", ".png");
            ImageIO.write(qrImage, "png", tempFile);

            // Create a Map<String, Object> for upload options
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("public_id", "qr_code");  // Optional: Set public_id for Cloudinary

            // Upload the file to Cloudinary with the correct type
            Map<String, Object> uploadResult = cloudinary.uploader().upload(tempFile, uploadOptions);
            tempFile.delete();  // Delete the temporary file after upload

            return (String) uploadResult.get("secure_url"); // Return the Cloudinary URL
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
