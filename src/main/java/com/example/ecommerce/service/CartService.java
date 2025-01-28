package com.example.ecommerce.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.repository.CartRepository;

@Service
public class CartService {
    
    
    @Autowired
    private CartRepository cartRepository;

    public Optional<Cart> getCartByCustomerName(String customerName) {
        return cartRepository.findAll()
                .stream()
                .filter(cart -> cart.getCustomerName().equalsIgnoreCase(customerName))
                .findFirst();
    }

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }
}
