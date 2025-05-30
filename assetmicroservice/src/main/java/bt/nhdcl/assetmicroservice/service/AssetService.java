package bt.nhdcl.assetmicroservice.service;

import bt.nhdcl.assetmicroservice.entity.Asset;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    void updateStatusOrHandleAction(String assetCode, String status, String email, String action);
    void handleAssetDeletion(String assetCode, String email, String action);
    void softDeleteAsset(String assetCode, String email);
    String updateFloorAndRoomsAttribute(Map<String, Object> payload);
}
