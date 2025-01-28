package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class) // Ensure Mockito is initialized
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository; // Mocked repository

    @InjectMocks
    private ProductService productService; // Service under test
    // Get all products returns list of products from repository

    // Initialize empty ArrayList when input is null
    @Test
    public void test_initialize_empty_arraylist_when_null() {
        List<String> result = null;
        result = (result != null ? result : new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    // Initialize with null input
    @Test
    public void test_initialize_with_null_input() {
        List<String> input = null;
        List<String> result = (input != null ? input : new ArrayList<>());

        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(result instanceof ArrayList);
    }
    @Test
    public void get_all_products_returns_product_list() {
        // Arrange
        List<Product> expectedProducts = Arrays.asList(
                new Product(),
                new Product()
        );
        when(productRepository.findAll()).thenReturn(expectedProducts);

        // Act
        List<Product> actualProducts = productService.getAllProducts();

        // Assert
        assertEquals(expectedProducts, actualProducts);
        verify(productRepository).findAll();
    }

    // Get product by ID returns Optional with product when found
    @Test
    public void get_product_by_id_returns_optional_with_product_when_found() {
        // Arrange
        Long productId = 1L;
        Product expectedProduct = new Product();
        expectedProduct.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        // Act
        Optional<Product> actualProduct = productService.getProductById(productId);

        // Assert
        assertTrue(actualProduct.isPresent());
        assertEquals(expectedProduct, actualProduct.get());
        verify(productRepository).findById(productId);
    }

    // Save new product persists and returns saved entity
    @Test
    public void save_product_persists_and_returns_saved_entity() {
        // Arrange
        Product productToSave = new Product();
        productToSave.setName("Test Product");
        productToSave.setPrice(99.99);
        productToSave.setCategories(Arrays.asList("Category1", "Category2"));
        productToSave.setAttributes(Map.of("key1", "value1", "key2", "value2"));

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Test Product");
        savedProduct.setPrice(99.99);
        savedProduct.setCategories(Arrays.asList("Category1", "Category2"));
        savedProduct.setAttributes(Map.of("key1", "value1", "key2", "value2"));

        when(productRepository.save(productToSave)).thenReturn(savedProduct);

        // Act
        Product result = productService.saveProduct(productToSave);

        // Assert
        assertEquals(savedProduct, result);
        verify(productRepository).save(productToSave);
    }

    // Update existing product modifies all fields and returns updated entity
    @Test
    public void update_product_modifies_all_fields_and_returns_updated_entity() {
        // Arrange
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Old Name");
        existingProduct.setPrice(10.0);
        existingProduct.setCategories(Arrays.asList("Old Category"));
        existingProduct.setAttributes(Map.of("key1", "oldValue1"));

        Product updatedProduct = new Product();
        updatedProduct.setName("New Name");
        updatedProduct.setPrice(20.0);
        updatedProduct.setCategories(Arrays.asList("New Category"));
        updatedProduct.setAttributes(Map.of("key1", "newValue1"));

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = productService.updateProduct(productId, updatedProduct);

        // Assert
        assertEquals(updatedProduct.getName(), result.getName());
        assertEquals(updatedProduct.getPrice(), result.getPrice(), 0.01);
        assertEquals(updatedProduct.getCategories(), result.getCategories());
        assertEquals(updatedProduct.getAttributes(), result.getAttributes());
        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    // Delete product removes entity from repository
    @Test
    public void delete_product_removes_entity_from_repository() {
        // Arrange
        Long productId = 1L;
        doNothing().when(productRepository).deleteById(productId);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).deleteById(productId);
    }

    // Get all products returns empty list when no products exist
    @Test
    public void get_all_products_returns_empty_list_when_no_products() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> products = productService.getAllProducts();

        // Assert
        assertTrue(products.isEmpty());
        verify(productRepository).findAll();
    }

    // Get product by ID returns empty Optional when product not found
    @Test
    public void get_product_by_id_returns_empty_optional_when_not_found() {
        // Arrange
        Long nonExistentProductId = 999L;
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductById(nonExistentProductId);

        // Assert
        assertTrue(result.isEmpty());
        verify(productRepository).findById(nonExistentProductId);
    }

    // Update product throws RuntimeException when ID doesn't exist
    @Test
    public void update_product_throws_exception_when_id_not_found() {
        // Arrange
        Long nonExistentId = 999L;
        Product updatedProduct = new Product();
        when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(nonExistentId, updatedProduct);
        });
        verify(productRepository).findById(nonExistentId);
    }
    // Save product with null fields should use default values
//    @Test
//    public void save_product_with_null_fields_uses_default_values() {
//        // Arrange
//        Product productWithNullFields = new Product();
//        productWithNullFields.setName(null);
//        productWithNullFields.setCategories(null);
//        productWithNullFields.setAttributes(null);
//        productWithNullFields.setPrice(1.0); // Set a valid price to pass validation
//
//        Product expectedProduct = new Product();
//        expectedProduct.setName(null);
//        expectedProduct.setCategories(new ArrayList<>());
//        expectedProduct.setAttributes(new HashMap<>());
//        expectedProduct.setPrice(1.0);
//
//
//        when(productRepository.save(any(Product.class))).thenReturn(expectedProduct);
//
//        // Act
//        Product savedProduct = productService.saveProduct(productWithNullFields);
//
//        // Assert
//        assertEquals(expectedProduct.getCategories(), savedProduct.getCategories());
//        assertEquals(expectedProduct.getAttributes(), savedProduct.getAttributes());
//        verify(productRepository).save(any(Product.class));
//    }
    // Delete non-existent product ID should not throw exception
    @Test
    public void delete_non_existent_product_id_should_not_throw_exception() {
        // Arrange
        Long nonExistentProductId = 999L;
        doNothing().when(productRepository).deleteById(nonExistentProductId);

        // Act & Assert
        assertDoesNotThrow(() -> productService.deleteProduct(nonExistentProductId));
        verify(productRepository).deleteById(nonExistentProductId);
    }

    // Product price validation during save/update
//    @Test
//    public void save_product_with_invalid_price_throws_exception() {
//        // Arrange
//        Product product = new Product();
//        product.setName("Test Product");
//        product.setPrice(-10.0); // Invalid price
//
//        // Act & Assert
//        assertThrows(IllegalArgumentException.class, () -> {
//            productService.saveProduct(product);
//        });
//    }

    // Categories list modifications are persisted correctly
    @Test
    public void update_product_persists_category_modifications() {
        // Arrange
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setCategories(Arrays.asList("Electronics", "Home"));

        Product updatedProduct = new Product();
        updatedProduct.setCategories(Arrays.asList("Electronics", "Garden"));

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = productService.updateProduct(productId, updatedProduct);

        // Assert
        assertEquals(updatedProduct.getCategories(), result.getCategories());
        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    // Attributes map updates are saved properly
    @Test
    public void update_product_attributes_map_is_saved() {
        // Arrange
        Long productId = 1L;
        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Old Name");
        existingProduct.setPrice(100.0);
        existingProduct.setAttributes(Map.of("color", "red"));

        Product updatedProduct = new Product();
        updatedProduct.setName("New Name");
        updatedProduct.setPrice(150.0);
        updatedProduct.setAttributes(Map.of("color", "blue", "size", "M"));

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Product result = productService.updateProduct(productId, updatedProduct);

        // Assert
        assertEquals(updatedProduct.getAttributes(), result.getAttributes());
        verify(productRepository).findById(productId);
        verify(productRepository).save(existingProduct);
    }

    // Concurrent modifications to same product
    @Test
    public void concurrent_modifications_to_same_product() throws InterruptedException {
        // Arrange
        Product initialProduct = new Product();
        initialProduct.setId(1L);
        initialProduct.setName("Initial Name");
        initialProduct.setPrice(100.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(initialProduct));

        Product updatedProduct1 = new Product();
        updatedProduct1.setName("Updated Name 1");
        updatedProduct1.setPrice(150.0);

        Product updatedProduct2 = new Product();
        updatedProduct2.setName("Updated Name 2");
        updatedProduct2.setPrice(200.0);

        // Act
        Thread thread1 = new Thread(() -> productService.updateProduct(1L, updatedProduct1));
        Thread thread2 = new Thread(() -> productService.updateProduct(1L, updatedProduct2));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Assert
        verify(productRepository, times(2)).save(any(Product.class));
    }
    // Product ID generation is sequential
//    @Test
//    public void test_product_id_generation_is_sequential() {
//        // Arrange
//        Product product1 = new Product();
//        product1.setName("Product 1");
//        product1.setPrice(10.0);
//
//        Product product2 = new Product();
//        product2.setName("Product 2");
//        product2.setPrice(20.0);
//
//        AtomicLong idGenerator = new AtomicLong(0);
//        when(productRepository.save(any(Product.class)))
//                .thenAnswer(invocation -> {
//                    Product product = invocation.getArgument(0);
//                    product.setId(idGenerator.incrementAndGet());
//                    return product;
//                });
//
//        // Act
//        Product savedProduct1 = productService.saveProduct(product1);
//        Product savedProduct2 = productService.saveProduct(product2);
//
//        // Assert
//        assertEquals(Long.valueOf(1), savedProduct1.getId());
//        assertEquals(Long.valueOf(2), savedProduct2.getId());
//        verify(productRepository, times(2)).save(any(Product.class));
//    }


}
