package bt.nhdcl.assetmicroservice.service;

import bt.nhdcl.assetmicroservice.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(String id);
    Category getCategoryByName(String name);
    Category saveCategory(Category category);
    void deleteCategory(String id);
}
