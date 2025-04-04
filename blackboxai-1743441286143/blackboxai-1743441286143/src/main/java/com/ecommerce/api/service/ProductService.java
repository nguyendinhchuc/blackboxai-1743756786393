package com.ecommerce.api.service;

import com.ecommerce.api.model.Product;
import com.ecommerce.api.model.ProductImage;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional
    public Product createProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        
        if (product.getImages() != null) {
            for (ProductImage image : product.getImages()) {
                image.setProduct(savedProduct);
                productImageRepository.save(image);
            }
        }
        
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setCategory(productDetails.getCategory());

        // Update images
//        productImageRepository.deleteByProductId(id);
        if (productDetails.getImages() != null) {
            for (ProductImage image : productDetails.getImages()) {
                image.setProduct(product);
                productImageRepository.save(image);
            }
        }

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public ProductImage uploadProductImage(Long productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Create uploads directory if it doesn't exist
        String uploadDir = "uploads/products";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
        
        // Save file to filesystem
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create and save ProductImage entity
        ProductImage productImage = new ProductImage();
        productImage.setImageUrl(uploadDir + "/" + filename);
        productImage.setProduct(product);
        
        return productImageRepository.save(productImage);
    }
}