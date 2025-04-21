package com.ecommerce.api.converter;

import com.ecommerce.api.model.ProductImage;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public class ProductImageConverter implements Converter<MultipartFile, ProductImage> {

    @Override
    public ProductImage convert(MultipartFile source) {
        if (source.isEmpty()) {
            return null;
        }

        try {
            ProductImage productImage = new ProductImage();
            productImage.setFileName(source.getOriginalFilename());
            productImage.setContentType(source.getContentType());
            productImage.setData(source.getBytes());
            return productImage;
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert MultipartFile to ProductImage", e);
        }
    }
}
