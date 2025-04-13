package bt.nhdcl.assetmicroservice.controller;

import bt.nhdcl.assetmicroservice.entity.Asset;
import bt.nhdcl.assetmicroservice.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @PostMapping
    public Asset createAsset(@RequestBody Asset asset) {
        assetService.generateQRCodeForAsset(asset);
        return assetService.saveAsset(asset);
    }

    @GetMapping("/{assetCode}")
    public ResponseEntity<Asset> getAsset(@PathVariable String assetCode) {
        Asset asset = assetService.getAssetByAssetCode(assetCode);
        return asset != null ? ResponseEntity.ok(asset) : ResponseEntity.notFound().build();
    }

    @GetMapping("/academy/{academyID}")
    public ResponseEntity<List<Asset>> getAssetsByAcademyID(@PathVariable String academyID) {
        List<Asset> assets = assetService.getAssetsByAcademyID(academyID);
        if (assets.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content if no assets are found
        }
        return ResponseEntity.ok(assets); // 200 OK with the assets list
    }

    @GetMapping
    public List<Asset> getAllAssets() {
        return assetService.getAllAssets();
    }

    @PutMapping("/{assetCode}")
    public ResponseEntity<Asset> updateAsset(@PathVariable String assetCode, @RequestBody Asset asset) {
        asset.setAssetCode(assetCode); // Ensure asset code is set before updating
        Asset updatedAsset = assetService.updateAsset(asset);

        return updatedAsset != null ? ResponseEntity.ok(updatedAsset) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{assetCode}")
    public ResponseEntity<Void> deleteAsset(@PathVariable String assetCode) {
        assetService.deleteAsset(assetCode);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PostMapping("/{assetID}/upload-images")
    public ResponseEntity<?> uploadImagesAsAttributes(
            @PathVariable int assetID,
            @RequestParam("images") MultipartFile[] images) {
        try {
            Asset updatedAsset = assetService.uploadAssetImagesToAttributes(assetID, images);
            return ResponseEntity.ok(updatedAsset);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading images: " + e.getMessage());
        }
    }

    @PostMapping("/upload/excel")
    public ResponseEntity<Map<String, String>> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            assetService.processExcelFile(file);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Excel file processed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error processing Excel file: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/find")
    public ResponseEntity<?> findByAssetCode(@RequestBody Map<String, String> request) {
        String assetCode = request.get("assetCode");

        if (assetCode != null) {
            try {
                Asset asset = assetService.getAssetByCode(assetCode);
                if (asset != null) {
                    return ResponseEntity.ok(asset);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Asset not found");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("AssetCode is required");
        }
    }

}
