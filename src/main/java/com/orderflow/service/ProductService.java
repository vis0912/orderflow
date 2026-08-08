package com.orderflow.service;

import com.orderflow.dto.ProductRequest;
import com.orderflow.dto.ProductResponse;
import com.orderflow.entity.Product;
import com.orderflow.exception.ProductNotFoundException;
import com.orderflow.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request) {

        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .description(request.description())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        return toResponse(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getDescription(),
                product.getActive()
        );
    }
}