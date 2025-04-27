package bt.nhdcl.assetmicroservice.service;

import bt.nhdcl.assetmicroservice.entity.Asset;
import bt.nhdcl.assetmicroservice.repository.AssetRepository;
import bt.nhdcl.assetmicroservice.repository.CategoryRepository;
import bt.nhdcl.assetmicroservice.config.CloudinaryConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import bt.nhdcl.assetmicroservice.entity.Attribute;
import bt.nhdcl.assetmicroservice.entity.Category;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private QRForRooms qrForRooms;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    public void AssetService(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public void generateQRCodeForAsset(Asset asset) {
        // Fetch category name using assetCategoryID
        String categoryName = categoryRepository.findById(asset.getAssetCategoryID())
                .map(c -> c.getName()) // Assuming your Category class has a `getName()` method
                .orElse("Unknown");

        if ("Building".equalsIgnoreCase(categoryName)) {
            generateQRCodeForBuilding(asset);
        } else if (!isExcludedCategory(categoryName)) {
            generateQRCodeForOtherCategories(asset, categoryName);
        }
    }

    private boolean isExcludedCategory(String category) {
        return "Infrastructure".equalsIgnoreCase(category) || "Facility".equalsIgnoreCase(category);
    }

    private void generateQRCodeForBuilding(Asset asset) {
        // Iterate through the attributes of the asset
        for (Attribute attr : asset.getAttributes()) {
            // Check if the attribute name is "Floor and Rooms"
            if ("Floor and Rooms".equalsIgnoreCase(attr.getName())) {
                try {
                    // Create an ObjectMapper to parse the JSON string into a Map
                    ObjectMapper objectMapper = new ObjectMapper();
                    var floorsAndRooms = objectMapper.readValue(attr.getValue(),
                            new TypeReference<Map<String, List<String>>>() {
                            });
    
                    // Loop through each floor and its rooms
                    for (Map.Entry<String, List<String>> entry : floorsAndRooms.entrySet()) {
                        String floor = entry.getKey(); // The floor name (e.g., "Ground Floor")
                        List<String> rooms = entry.getValue(); // List of rooms on that floor
    
                        // Iterate through the rooms on this floor
                        for (String room : rooms) {
                            // Construct the QR code data (URL)
                            String qrData = "http://localhost:3000/roomqrdetail/" + asset.getAssetCode() +
                                    "?floor=" + URLEncoder.encode(floor, StandardCharsets.UTF_8) +
                                    "&room=" + URLEncoder.encode(room, StandardCharsets.UTF_8);
    
                            // Create a unique name for the QR code image
                            String uniqueName = asset.getAssetCode() + "-room-" + room + "-"
                                    + System.currentTimeMillis();
    
                            // Generate QR code with the unique name
                            String qrUrl = qrForRooms.generateQRCode(qrData, uniqueName);
    
                            // If QR URL is generated successfully, add it to the asset's attributes
                            if (qrUrl != null) {
                                asset.addQRCodeAttribute("QR Code - Room " + room, qrUrl);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break; // Exit the loop after processing the "Floor and Rooms" attribute
            }
        }
    }    

    private void generateQRCodeForOtherCategories(Asset asset, String categoryName) {
        String baseUrl = "http://localhost:3000/qrdetail/";
        String qrData = baseUrl + asset.getAssetCode(); // Only pass assetCode

        String qrUrl = qrCodeService.generateQRCode(qrData);
        if (qrUrl != null) {
            asset.addQRCodeAttribute("QR Code", qrUrl);
        }
    }

    @Override
    public Asset saveAsset(Asset asset) {
        // Get the next asset ID and set it automatically
        int nextAssetID = getNextAssetID();
        asset.setAssetIDAuto(nextAssetID); // Pass nextAssetID here
        return assetRepository.save(asset);
    }

    @Override
    public Asset getAssetByAssetCode(String assetCode) {
        // 1. Find the asset using the assetCode
        Query query = new Query(Criteria.where("assetCode").is(assetCode));
        Asset asset = mongoTemplate.findOne(query, Asset.class, "assets");

        if (asset == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with assetCode: " + assetCode);
        }

        // 2. Fetch category details using assetCategoryID
        Object assetCategoryId = asset.getAssetCategoryID();
        if (assetCategoryId != null) {
            Category category = mongoTemplate.findById(assetCategoryId, Category.class, "categories");
            asset.setCategoryDetails(category); // Make sure Asset has a setCategoryDetails() method
        }

        return asset;
    }

    @Override
    public List<Asset> getAssetsByAcademyID(String academyID) {
        AggregationOperation lookupOperation = context -> new Document("$lookup",
                new Document("from", "categories")
                        .append("let", new Document("catId", new Document("$toObjectId", "$assetCategoryID")))
                        .append("pipeline", List.of(
                                new Document("$match", new Document("$expr",
                                        new Document("$eq", List.of("$_id", "$$catId"))))))
                        .append("as", "categoryDetails"));

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("academyID").is(academyID)),
                lookupOperation,
                Aggregation.unwind("categoryDetails", true));

        return mongoTemplate.aggregate(aggregation, "assets", Asset.class).getMappedResults();
    }

    @Override
    public List<Asset> getAllAssets() {
        AggregationOperation lookupOperation = context -> new Document("$lookup",
                new Document("from", "categories")
                        .append("let", new Document("catId", new Document("$toObjectId", "$assetCategoryID")))
                        .append("pipeline", List.of(
                                new Document("$match", new Document("$expr",
                                        new Document("$eq", List.of("$_id", "$$catId"))))))
                        .append("as", "categoryDetails"));

        Aggregation aggregation = Aggregation.newAggregation(
                lookupOperation,
                Aggregation.unwind("categoryDetails", true));

        return mongoTemplate.aggregate(aggregation, "assets", Asset.class).getMappedResults();
    }

    @Override
    public Asset updateAsset(Asset asset) {
        Optional<Asset> existingAssetOpt = assetRepository.findById(asset.getAssetCode());
        if (existingAssetOpt.isPresent()) {
            Asset existingAsset = existingAssetOpt.get();

            if (asset.getTitle() != null)
                existingAsset.setTitle(asset.getTitle());
            if (asset.getCost() != 0)
                existingAsset.setCost(asset.getCost());
            if (asset.getAcquireDate() != null)
                existingAsset.setAcquireDate(asset.getAcquireDate());
            if (asset.getLifespan() != null)
                existingAsset.setLifespan(asset.getLifespan());
            if (asset.getAssetArea() != null)
                existingAsset.setAssetArea(asset.getAssetArea());
            if (asset.getDescription() != null)
                existingAsset.setDescription(asset.getDescription());
            if (asset.getStatus() != null)
                existingAsset.setStatus(asset.getStatus());
            if (asset.getCreatedBy() != null)
                existingAsset.setCreatedBy(asset.getCreatedBy());
            if (asset.getAcademyID() != null)
                existingAsset.setAcademyID(asset.getAcademyID());
            if (asset.getAssetCategoryID() != null)
                existingAsset.setAssetCategoryID(asset.getAssetCategoryID());
            if (asset.getAttributes() != null && !asset.getAttributes().isEmpty())
                existingAsset.setAttributes(asset.getAttributes());

            return assetRepository.save(existingAsset);
        }
        return null;
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

    public Asset uploadAssetImagesToAttributes(int assetID, MultipartFile[] files) throws IOException {
        Optional<Asset> optionalAsset = assetRepository.findByAssetID(assetID);
        if (optionalAsset.isEmpty()) {
            throw new RuntimeException("Asset not found with assetID: " + assetID);
        }

        Asset asset = optionalAsset.get();
        List<Attribute> attributes = asset.getAttributes() != null ? asset.getAttributes() : new ArrayList<>();

        // Find max index for imageN
        int maxImageIndex = attributes.stream()
                .filter(attr -> attr.getName().startsWith("image"))
                .map(attr -> attr.getName().replace("image", ""))
                .filter(s -> s.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            // Call the uploadFile method to get the upload result as Map
            Map<String, Object> uploadResult = cloudinaryConfig.uploadFile(file);

            // Retrieve the image URL from the result
            String imageUrl = (String) uploadResult.get("secure_url");

            // Add the new image attribute with the URL
            attributes.add(new Attribute("image" + (maxImageIndex + i + 1), imageUrl));
        }

        // Set the updated attributes to the asset
        asset.setAttributes(attributes);

        // Save and return the updated asset
        return assetRepository.save(asset);
    }

    @Override
    public void processExcelFile(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext())
                return;

            // Step 1: Read header row and map headers to their column index
            Row headerRow = rowIterator.next();
            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (Cell cell : headerRow) {
                headerIndexMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
            }

            // Step 2: Process each row
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row == null)
                    continue;

                Asset asset = new Asset();

                // Step 3: Auto-generate Asset ID
                Integer assetID = getNextAssetID(); // Auto-generate the Asset ID
                asset.setAssetIDAuto(assetID); // Set the auto-generated Asset ID

                // Set other fields from the row (from the Excel file)
                asset.setAssetCode(getCellValueAsString(row.getCell(headerIndexMap.get("assetCode"))));
                asset.setTitle(getCellValueAsString(row.getCell(headerIndexMap.get("title"))));
                asset.setCost((int) getCellValueAsDouble(row.getCell(headerIndexMap.get("cost"))));
                asset.setAcquireDate(getCellValueAsString(row.getCell(headerIndexMap.get("acquireDate"))));
                asset.setLifespan(getCellValueAsString(row.getCell(headerIndexMap.get("lifespan"))));
                asset.setAssetArea(getCellValueAsString(row.getCell(headerIndexMap.get("assetArea"))));
                asset.setDescription(getCellValueAsString(row.getCell(headerIndexMap.get("description"))));
                asset.setStatus("Pending");
                asset.setCreatedBy(getCellValueAsString(row.getCell(headerIndexMap.get("createdBy"))));
                asset.setAcademyID(getCellValueAsString(row.getCell(headerIndexMap.get("academyID"))));
                asset.setAssetCategoryID(getCellValueAsString(row.getCell(headerIndexMap.get("assetCategoryID"))));

                // Step 4: Collect dynamic attributes only if they have a value
                List<Attribute> attributes = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : headerIndexMap.entrySet()) {
                    String key = entry.getKey();
                    int colIndex = entry.getValue();

                    // Skip fixed fields
                    if (List.of("assetCode", "title", "cost", "acquireDate", "lifespan",
                            "assetArea", "description", "status", "createdBy",
                            "academyID", "assetCategoryID").contains(key)) {
                        continue;
                    }

                    String value = getCellValueAsString(row.getCell(colIndex));
                    if (value != null && !value.trim().isEmpty()) {
                        // Only add attribute if it has a value
                        attributes.add(new Attribute(key, value));
                    }
                }

                asset.setAttributes(attributes);

                // Step 5: Generate QR Code for the asset (if needed)
                generateQRCodeForAsset(asset);

                // Step 6: Save the asset to the repository
                assetRepository.save(asset);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error processing Excel file", e);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Format the date explicitly
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // or "dd/MM/yyyy"
                    return sdf.format(date);
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return null;
        }
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null)
            return 0.0;

        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    @Override
    public Asset getAssetByCode(String assetCode) {
        return assetRepository.findByAssetCode(assetCode)
                .orElseThrow(() -> new RuntimeException("Asset not found for assetCode: " + assetCode));
    }

    public void updateStatusOrHandleAction(String assetCode, String status, String email, String action) {
        Optional<Asset> assetOpt = assetRepository.findById(assetCode);

        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();

            if ("decline".equalsIgnoreCase(action)) {
                assetRepository.deleteById(assetCode);
                if (email != null) {
                    emailService.sendEmail(email, "Asset Registration Declined",
                            "We regret to inform you that your asset registration request has been declined and the asset has been removed from the system.");
                }
                return; // 👈 stop further execution to avoid saving the deleted asset
            }

            if (status != null && !status.isEmpty()) {
                asset.setStatus(status);
            }

            if ("accept".equalsIgnoreCase(action)) {
                if (email != null) {
                    emailService.sendEmail(email, "Asset Registration Approved",
                            "Your asset registration request has been approved and the asset is now active in the system.");
                }
            }

            assetRepository.save(asset);
        } else {
            throw new RuntimeException("Asset with code " + assetCode + " not found.");
        }
    }

    public void handleAssetDeletion(String assetCode, String email, String action) {
        Optional<Asset> assetOpt = assetRepository.findById(assetCode);

        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();

            // Case 1: Only assetCode is given (soft delete)
            if (email == null && action == null) {
                asset.setStatus("Disposed");
                assetRepository.save(asset);
                return;
            }

            // Case 2: Email and action are provided
            if ("accept".equalsIgnoreCase(action)) {
                asset.setStatus("Disposed");
                asset.setDeleted(false);
                assetRepository.save(asset);
                emailService.sendEmail(
                        email,
                        "Asset Disposal Request Approved",
                        "Your request to dispose of the asset has been approved. The asset has now been marked as disposed in the system.");
            } else if ("decline".equalsIgnoreCase(action)) {
                asset.setStatus("In Usage");
                asset.setDeleted(false);
                asset.setDeletedBy(null);
                assetRepository.save(asset);
                // No change to deleted status
                emailService.sendEmail(
                        email,
                        "Asset Disposal Request Declined",
                        "Your request to dispose of the asset has been declined. The asset will remain active in the system.");
            }

        } else {
            throw new RuntimeException("Asset with code " + assetCode + " not found.");
        }
    }

    public void softDeleteAsset(String assetCode, String email) {
        if (assetCode == null || assetCode.isEmpty()) {
            throw new IllegalArgumentException("Asset code must be provided.");
        }

        Optional<Asset> optionalAsset = assetRepository.findById(assetCode);
        if (optionalAsset.isEmpty()) {
            throw new IllegalArgumentException("Asset not found.");
        }

        Asset asset = optionalAsset.get();
        asset.setStatus("Pending");
        asset.setDeleted(true);
        asset.setDeletedBy(email);
        assetRepository.save(asset);
    }

    public String updateFloorAndRoomsAttribute(Map<String, Object> payload) {
        String assetCode = (String) payload.get("assetCode");
        String name = (String) payload.get("name");
        String value = (String) payload.get("value");
    
        if (assetCode == null || name == null || value == null) {
            return "Missing assetCode, name, or value.";
        }
    
        Optional<Asset> optionalAsset = assetRepository.findByAssetCode(assetCode);
    
        if (optionalAsset.isEmpty()) {
            return "Asset not found with code: " + assetCode;
        }
    
        Asset asset = optionalAsset.get();
    
        List<Attribute> attributes = asset.getAttributes();
        if (attributes != null && attributes.stream().anyMatch(attr -> "Floor and Rooms".equals(attr.getName()))) {
            return "Asset already contains Floor and Rooms.";
        }
    
        if (attributes == null) {
            attributes = new ArrayList<>();
        }
    
        attributes.add(new Attribute(name, value));
        asset.setAttributes(attributes);
    
        // ✅ Generate QR codes for the new Floor and Rooms
        generateQRCodeForBuilding(asset);
    
        assetRepository.save(asset);
    
        return "Floor and Rooms attribute added and QR codes generated successfully.";
    }
}