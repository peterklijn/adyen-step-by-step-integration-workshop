package com.adyen.workshop.controllers;

import com.adyen.model.RequestOptions;
import com.adyen.model.checkout.*;
import com.adyen.workshop.configurations.ApplicationConfiguration;
import com.adyen.service.checkout.PaymentsApi;
import com.adyen.service.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * REST controller for using the Adyen payments API.
 */
@RestController
public class ApiController {
    private final Logger log = LoggerFactory.getLogger(ApiController.class);

    private final ApplicationConfiguration applicationConfiguration;
    private final PaymentsApi paymentsApi;

    public ApiController(ApplicationConfiguration applicationConfiguration, PaymentsApi paymentsApi) {
        this.applicationConfiguration = applicationConfiguration;
        this.paymentsApi = paymentsApi;
    }

    @GetMapping("/hello-world")
    public ResponseEntity<String> helloWorld() throws Exception {
        // Step 0
        return ResponseEntity.ok()
                .body("This is the 'Hello World' from the workshop - You've successfully finished step 0!");
    }

    @PostMapping("/api/paymentMethods")
    public ResponseEntity<PaymentMethodsResponse> paymentMethods() throws IOException, ApiException {
        var request = new PaymentMethodsRequest()
            .merchantAccount(applicationConfiguration.getAdyenMerchantAccount());
        log.info("retrieving payment methods from adyen {}", request);

        var response = paymentsApi.paymentMethods(request);
        log.info("payment methods response from adyen {}", response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/payments")
    public ResponseEntity<PaymentResponse> payments(@RequestHeader String host, @RequestBody PaymentRequest body, HttpServletRequest request) throws IOException, ApiException {
        var orderRef = UUID.randomUUID().toString();

        var paymentRequest = new PaymentRequest()
            .merchantAccount(applicationConfiguration.getAdyenMerchantAccount())
            .channel(PaymentRequest.ChannelEnum.WEB)
            .amount(new Amount().currency("EUR").value(9998L))
            .reference(orderRef)
            .paymentMethod(body.getPaymentMethod())
            .returnUrl(request.getScheme() + "://" + host + "/api/handleShopperRedirect?orderRef=" + orderRef);
        
        log.info("payment request {}", paymentRequest);
        
        var requestOptions = new RequestOptions().idempotencyKey(UUID.randomUUID().toString());
        var paymentResponse = paymentsApi.payments(paymentRequest, requestOptions);
        log.info("payment response {}", paymentResponse);

        return ResponseEntity.ok(paymentResponse);
    }

    @PostMapping("/api/payments/details")
    public ResponseEntity<PaymentDetailsResponse> paymentsDetails(@RequestBody PaymentDetailsRequest detailsRequest) throws IOException, ApiException {
        // Step 12
        var pay = new PaymentRequest();
        pay.setShopperInteraction(PaymentRequest.ShopperInteractionEnum.ECOMMERCE);
        return null;
    }

    @GetMapping("/api/handleShopperRedirect")
    public RedirectView redirect(@RequestParam(required = false) String payload, @RequestParam(required = false) String redirectResult) throws IOException, ApiException {
        // Step 13
        return null;
    }
}