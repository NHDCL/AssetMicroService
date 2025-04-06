package bt.nhdcl.assetmicroservice.repository;

import bt.nhdcl.assetmicroservice.entity.Asset;

import java.util.Optional;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends MongoRepository<Asset, String> {
    Asset findByAssetCode(String assetCode);
    Optional<Asset> findByAssetID(int assetID);
    List<Asset> findByAcademyID(String academyID);
}
