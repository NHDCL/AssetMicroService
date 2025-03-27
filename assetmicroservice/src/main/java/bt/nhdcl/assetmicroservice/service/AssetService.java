package bt.nhdcl.assetmicroservice.service;

import bt.nhdcl.assetmicroservice.entity.Asset;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface AssetService {
    Asset saveAsset(Asset asset);
    Asset getAssetByAssetCode(String assetCode);
    List<Asset> getAllAssets();
    Asset updateAsset(Asset asset);
    void deleteAsset(String assetCode);
    Asset uploadFileToAsset(int assetID, MultipartFile file);
}
