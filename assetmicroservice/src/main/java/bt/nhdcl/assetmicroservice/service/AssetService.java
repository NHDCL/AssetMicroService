package bt.nhdcl.assetmicroservice.service;

import bt.nhdcl.assetmicroservice.entity.Asset;
import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface AssetService {
    Asset saveAsset(Asset asset);
    void generateQRCodeForAsset(Asset asset);
    Asset getAssetByAssetCode(String assetCode);
    List<Asset> getAssetsByAcademyID(String academyID);
    List<Asset> getAllAssets();
    Asset updateAsset(Asset asset);
    void deleteAsset(String assetCode);
    Asset uploadAssetImagesToAttributes(int assetID, MultipartFile[] files) throws IOException;
    void processExcelFile(MultipartFile file) throws IOException;
    Asset getAssetByCode(String assetCode);
}
