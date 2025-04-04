package com.ecommerce.api.service;

import com.ecommerce.api.model.Product;
import com.ecommerce.api.model.ProductImage;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}