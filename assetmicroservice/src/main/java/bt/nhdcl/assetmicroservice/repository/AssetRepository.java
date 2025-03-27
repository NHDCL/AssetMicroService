package bt.nhdcl.assetmicroservice.repository;

import bt.nhdcl.assetmicroservice.entity.Asset;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends MongoRepository<Asset, String> {
    // You can define custom query methods here if needed
    
    // Example: Find by assetCode
    Asset findByAssetCode(String assetCode);
    Optional<Asset> findByAssetID(int assetID);
}
