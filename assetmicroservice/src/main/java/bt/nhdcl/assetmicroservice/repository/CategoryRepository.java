package bt.nhdcl.assetmicroservice.repository;

import bt.nhdcl.assetmicroservice.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    
    // Custom query methods can be added here
    Category findByName(String name);
}
