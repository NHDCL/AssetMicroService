package bt.nhdcl.assetmicroservice.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    // This method returns the upload result as Map<String, Object>
    public Map<String, Object> uploadFile(MultipartFile file) throws IOException {
        // Get Cloudinary instance
        Cloudinary cloudinary = cloudinary();

        // Upload the file to Cloudinary
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
    }
}
