package com.adyen.checkout.controllers;

import com.adyen.checkout.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;


/**
 * REST controller for managing your shopping cart.
 */
@RestController
public class CartApiController {

    @Autowired
    private CartService cartService;

    public CartApiController() {
    }

    @GetMapping("/cart-api/add")
    public RedirectView add(@RequestParam(required = true) String name, @RequestParam(required = true) String type) {
        getCartService().addItemToCart(name);
        return new RedirectView("/preview?type=" + type);
    }

    public CartService getCartService() {
        return cartService;
    }

    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }
}
