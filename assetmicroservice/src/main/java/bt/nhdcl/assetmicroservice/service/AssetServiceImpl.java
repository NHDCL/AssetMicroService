package bt.nhdcl.assetmicroservice.service;

import bt.nhdcl.assetmicroservice.entity.Asset;
import bt.nhdcl.assetmicroservice.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.multipart.MultipartFile;


@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public Asset saveAsset(Asset asset) {
        // Get the next asset ID and set it automatically
        int nextAssetID = getNextAssetID();
        asset.setAssetIDAuto(nextAssetID); // Pass nextAssetID here
        return assetRepository.save(asset);
    }

    @Override
    public Asset getAssetByAssetCode(String assetCode) {
        return assetRepository.findByAssetCode(assetCode);
    }

    @Override
    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    @Override
    public Asset updateAsset(Asset asset) {
        Optional<Asset> existingAsset = assetRepository.findById(asset.getAssetCode());
        if (existingAsset.isPresent()) {
            return assetRepository.save(asset);
        }
        return null; // Or throw an exception if asset not found
    }

    @Override
    public void deleteAsset(String assetCode) {
        assetRepository.deleteById(assetCode);
    }

    public int getNextAssetID() {
        Query query = new Query().with(Sort.by(Sort.Order.desc("assetID"))).limit(1);
        Asset latestAsset = mongoTemplate.findOne(query, Asset.class);
        return latestAsset == null ? 1 : latestAsset.getAssetID() + 1;
    }

    public Asset uploadFileToAsset(int assetID, MultipartFile file) {
        Optional<Asset> assetOptional = assetRepository.findByAssetID(assetID);
        if (assetOptional.isEmpty()) {
            throw new RuntimeException("Asset not found");
        }
    
        Asset asset = assetOptional.get();
        String fileUrl = cloudinaryService.uploadFile(file); // Upload file to Cloudinary
        asset.addFileAttribute(fileUrl); // Add file URL as an attribute
    
        return assetRepository.save(asset); // Save updated asset
    }
}
