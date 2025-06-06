package bt.nhdcl.assetmicroservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadFile(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    
            return uploadResult.get("url").toString(); // Return the uploaded file URL
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
}
