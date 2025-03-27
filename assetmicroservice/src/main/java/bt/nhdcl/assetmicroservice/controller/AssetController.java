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
        return assetService.saveAsset(asset);
    }

    @GetMapping("/{assetCode}")
    public Asset getAsset(@PathVariable String assetCode) {
        return assetService.getAssetByAssetCode(assetCode);
    }

    @GetMapping
    public List<Asset> getAllAssets() {
        return assetService.getAllAssets();
    }

    @PutMapping("/{assetCode}")
    public Asset updateAsset(@PathVariable String assetCode, @RequestBody Asset asset) {
        asset.setAssetCode(assetCode); // Ensure the asset code is set before updating
        return assetService.updateAsset(asset);
    }

    @DeleteMapping("/{assetCode}")
    public void deleteAsset(@PathVariable String assetCode) {
        assetService.deleteAsset(assetCode);
    }

    @PostMapping("/{assetID}/upload")
    public ResponseEntity<Asset> uploadFile(
            @PathVariable int assetID,
            @RequestParam("file") MultipartFile file) {
        // Handle the file upload
        Asset updatedAsset = assetService.uploadFileToAsset(assetID, file);

        return ResponseEntity.ok(updatedAsset);
    }

}
