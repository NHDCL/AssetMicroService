package bt.nhdcl.assetmicroservice.controller;

import bt.nhdcl.assetmicroservice.entity.Asset;
import bt.nhdcl.assetmicroservice.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            return ResponseEntity.noContent().build();  // 204 No Content if no assets are found
        }
        return ResponseEntity.ok(assets);  // 200 OK with the assets list
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

    @PostMapping("/{assetID}/upload")
    public ResponseEntity<?> uploadFile(
            @PathVariable int assetID,
            @RequestParam("file") MultipartFile file) {
        try {
            Asset updatedAsset = assetService.uploadFileToAsset(assetID, file);
            return ResponseEntity.ok(updatedAsset);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Asset not found or file upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/upload/excel")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            assetService.processExcelFile(file);
            return ResponseEntity.ok("Excel file processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing Excel file: " + e.getMessage());
        }
    }

}
