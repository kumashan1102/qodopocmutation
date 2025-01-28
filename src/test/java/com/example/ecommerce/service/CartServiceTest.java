package com.example.ecommerce.service;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.repository.CartRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @InjectMocks

    private CartService cartService; // Mocked repository

    @Mock
    private CartRepository cartRepository; // Service under test
    // Successfully retrieve cart by existing customer name
    @Test
    public void test_get_cart_by_existing_customer_name() {
        // Arrange
        Cart cart = new Cart();
        cart.setCustomerName("John");
        List<Cart> carts = Arrays.asList(cart);

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        Optional<Cart> result = cartService.getCartByCustomerName("John");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getCustomerName());
        verify(cartRepository).findAll();
    }

    // Successfully save new cart with valid customer name and empty categories
    @Test
    public void test_save_cart_with_valid_customer_name_and_empty_categories() {
        // Arrange
        Cart cart = new Cart();
        cart.setCustomerName("Alice");
        cart.setCategories(new ArrayList<>());

        when(cartRepository.save(cart)).thenReturn(cart);

        // Act
        Cart savedCart = cartService.saveCart(cart);

        // Assert
        assertNotNull(savedCart);
        assertEquals("Alice", savedCart.getCustomerName());
        assertTrue(savedCart.getCategories().isEmpty());
        verify(cartRepository).save(cart);
    }

    // Successfully save cart with valid customer name and multiple categories
    @Test
    public void test_save_cart_with_valid_customer_name_and_multiple_categories() {
        // Arrange
        Cart cart = new Cart();
        cart.setCustomerName("Alice");
        Category category1 = new Category();
        Category category2 = new Category();
        cart.setCategories(Arrays.asList(category1, category2));

        when(cartRepository.save(cart)).thenReturn(cart);

        // Act
        Cart savedCart = cartService.saveCart(cart);

        // Assert
        assertNotNull(savedCart);
        assertEquals("Alice", savedCart.getCustomerName());
        assertEquals(2, savedCart.getCategories().size());
        verify(cartRepository).save(cart);
    }

    // Return empty Optional when searching for non-existent customer name
    @Test
    public void test_get_cart_by_non_existent_customer_name() {
        // Arrange
        List<Cart> carts = new ArrayList<>();

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        Optional<Cart> result = cartService.getCartByCustomerName("NonExistentName");

        // Assert
        assertFalse(result.isPresent());
        verify(cartRepository).findAll();
    }
    // Handle case-insensitive customer name search
    @Test
    public void test_get_cart_case_insensitive_customer_name() {
        // Arrange
        Cart cart = new Cart();
        cart.setCustomerName("John");
        List<Cart> carts = Arrays.asList(cart);

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        Optional<Cart> result = cartService.getCartByCustomerName("JOHN");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getCustomerName());
        verify(cartRepository).findAll();
    }

    // Handle null customer name in search
//    @Test
//    public void test_get_cart_by_null_customer_name() {
//        // Arrange
//        List<Cart> carts = Arrays.asList(new Cart(), new Cart());
//
//        when(cartRepository.findAll()).thenReturn(carts);
//
//        // Act
//        Optional<Cart> result = cartService.getCartByCustomerName(null);
//
//        // Assert
//        assertFalse(result.isPresent());
//        verify(cartRepository).findAll();
//    }

    // Handle empty customer name in search
//    @Test
//    public void test_get_cart_by_empty_customer_name() {
//        // Arrange
//        List<Cart> carts = Arrays.asList(new Cart(), new Cart());
//
//        when(cartRepository.findAll()).thenReturn(carts);
//
//        // Act
//        Optional<Cart> result = cartService.getCartByCustomerName("");
//
//        // Assert
//        assertFalse(result.isPresent());
//        verify(cartRepository).findAll();
//    }

    // Handle special characters in customer name
    @Test
    public void test_get_cart_by_customer_name_with_special_characters() {
        // Arrange
        Cart cart = new Cart();
        cart.setCustomerName("John@Doe");
        List<Cart> carts = Arrays.asList(cart);

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        Optional<Cart> result = cartService.getCartByCustomerName("John@Doe");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John@Doe", result.get().getCustomerName());
        verify(cartRepository).findAll();
    }

    // Handle very long customer name
    @Test
    public void test_get_cart_by_very_long_customer_name() {
        // Arrange
        Cart cart = new Cart();
        String longCustomerName = "A".repeat(1000); // Very long customer name
        cart.setCustomerName(longCustomerName);
        List<Cart> carts = Arrays.asList(cart);

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        Optional<Cart> result = cartService.getCartByCustomerName(longCustomerName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(longCustomerName, result.get().getCustomerName());
        verify(cartRepository).findAll();
    }

    // Handle duplicate customer names returning first match
    @Test
    public void test_get_cart_by_duplicate_customer_name() {
        // Arrange
        Cart cart1 = new Cart();
        cart1.setCustomerName("Alice");
        Cart cart2 = new Cart();
        cart2.setCustomerName("Alice");
        List<Cart> carts = Arrays.asList(cart1, cart2);

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        Optional<Cart> result = cartService.getCartByCustomerName("Alice");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Alice", result.get().getCustomerName());
        assertEquals(cart1, result.get());
        verify(cartRepository).findAll();
    }

    // Handle concurrent cart saves for same customer
    @Test
    public void test_concurrent_cart_saves_for_same_customer() throws InterruptedException {
        // Arrange
        Cart cart1 = new Cart();
        cart1.setCustomerName("Alice");
        Cart cart2 = new Cart();
        cart2.setCustomerName("Alice");

        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Thread thread1 = new Thread(() -> cartService.saveCart(cart1));
        Thread thread2 = new Thread(() -> cartService.saveCart(cart2));

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Assert
        verify(cartRepository, times(2)).save(any(Cart.class));
    }

    // Handle lazy loading of categories when retrieving cart
    @Test
    public void test_lazy_loading_of_categories() {
        // Arrange
        Cart cart = new Cart();
        cart.setCustomerName("John");
        Category category = new Category();
        cart.setCategories(Arrays.asList(category));
        List<Cart> carts = Arrays.asList(cart);

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        Optional<Cart> result = cartService.getCartByCustomerName("John");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John", result.get().getCustomerName());
        assertNotNull(result.get().getCategories());
        verify(cartRepository).findAll();
    }
    // Verify cascade operations when saving cart with categories
    @Test
    public void test_save_cart_with_categories_cascade() {
        // Arrange
        Cart cart = new Cart();
        cart.setCustomerName("Alice");
        Category category1 = new Category();
        Category category2 = new Category();
        cart.setCategories(Arrays.asList(category1, category2));

        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        Cart savedCart = cartService.saveCart(cart);

        // Assert
        assertNotNull(savedCart);
        assertEquals(2, savedCart.getCategories().size());
        verify(cartRepository).save(cart);
    }

    // Handle large number of carts when searching
    @Test
    public void test_get_cart_by_customer_name_with_large_number_of_carts() {
        // Arrange
        List<Cart> carts = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Cart cart = new Cart();
            cart.setCustomerName("Customer" + i);
            carts.add(cart);
        }
        Cart targetCart = new Cart();
        targetCart.setCustomerName("TargetCustomer");
        carts.add(targetCart);

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        Optional<Cart> result = cartService.getCartByCustomerName("TargetCustomer");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("TargetCustomer", result.get().getCustomerName());
        verify(cartRepository).findAll();
    }

    // Validate cart entity constraints before saving
    @Test
    public void test_save_cart_with_valid_entity() {
        // Arrange
        Cart cart = new Cart();
        cart.setCustomerName("Alice");
        when(cartRepository.save(cart)).thenReturn(cart);

        // Act
        Cart savedCart = cartService.saveCart(cart);

        // Assert
        assertNotNull(savedCart);
        assertEquals("Alice", savedCart.getCustomerName());
        verify(cartRepository).save(cart);
    }

    // Performance testing for cart search operation
    @Test
    public void test_performance_of_get_cart_by_customer_name() {
        // Arrange
        List<Cart> carts = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Cart cart = new Cart();
            cart.setCustomerName("Customer" + i);
            carts.add(cart);
        }
        Cart targetCart = new Cart();
        targetCart.setCustomerName("TargetCustomer");
        carts.add(targetCart);

        when(cartRepository.findAll()).thenReturn(carts);

        // Act
        long startTime = System.currentTimeMillis();
        Optional<Cart> result = cartService.getCartByCustomerName("TargetCustomer");
        long endTime = System.currentTimeMillis();

        // Assert
        assertTrue(result.isPresent());
        assertEquals("TargetCustomer", result.get().getCustomerName());
        verify(cartRepository).findAll();
        assertTrue((endTime - startTime) < 100, "Performance issue: search took too long");
    }
}
