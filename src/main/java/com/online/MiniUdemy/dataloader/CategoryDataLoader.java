package com.online.MiniUdemy.dataloader; // Make sure this matches your actual package name!

import com.online.MiniUdemy.entity.Category;
import com.online.MiniUdemy.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CategoryDataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public CategoryDataLoader(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if the database is completely empty of categories
        if (categoryRepository.count() == 0) {
            
            // Create and save default categories using setters
            Category cat1 = new Category();
            cat1.setName("Web Development");
            categoryRepository.save(cat1);

            Category cat2 = new Category();
            cat2.setName("Data Science");
            categoryRepository.save(cat2);

            Category cat3 = new Category();
            cat3.setName("Business & Finance");
            categoryRepository.save(cat3);

            Category cat4 = new Category();
            cat4.setName("Graphic Design");
            categoryRepository.save(cat4);
            
            System.out.println("✅ INITIALIZATION SUCCESS: Default Categories loaded into the database!");
        } else {
            System.out.println("⚡ INITIALIZATION SKIPPED: Categories already exist.");
        }
    }
}