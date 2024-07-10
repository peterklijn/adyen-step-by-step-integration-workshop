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

    private final String origin = "https://orange-space-waddle-g57v47wr742r74-8080.app.github.dev";

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

        var billingAddress = new BillingAddress()
            .street("Rokin")
            .houseNumberOrName("49")
            .postalCode("1012KK")
            .city("Amsterdam")
            .country("NL");
        var authenticationData = new AuthenticationData()
            .attemptAuthentication(AuthenticationData.AttemptAuthenticationEnum.ALWAYS);

        var paymentRequest = new PaymentRequest()
            .merchantAccount(applicationConfiguration.getAdyenMerchantAccount())
            .channel(PaymentRequest.ChannelEnum.WEB)
            .amount(new Amount().currency("EUR").value(9998L))
            .reference(orderRef)
            .paymentMethod(body.getPaymentMethod())
            // .returnUrl(request.getScheme() + "://" + host + "/api/handleShopperRedirect?orderRef=" + orderRef)
            .returnUrl(origin + "/api/handleShopperRedirect?orderRef=" + orderRef)
            // Step 12
            .authenticationData(authenticationData)
            .browserInfo(body.getBrowserInfo())
            .billingAddress(billingAddress)
            // .origin(request.getScheme() + "://" + host)
            .origin(origin)
            .shopperIP(body.getShopperIP())
            .shopperInteraction(PaymentRequest.ShopperInteractionEnum.ECOMMERCE)
            .shopperEmail("s.hopper@example.com");
        
        log.info("payment request {}", paymentRequest);
        
        var requestOptions = new RequestOptions().idempotencyKey(UUID.randomUUID().toString());
        var paymentResponse = paymentsApi.payments(paymentRequest, requestOptions);
        log.info("payment response {}", paymentResponse);

        return ResponseEntity.ok(paymentResponse);
    }

    @PostMapping("/api/payments/details")
    public ResponseEntity<PaymentDetailsResponse> paymentsDetails(@RequestBody PaymentDetailsRequest detailsRequest) throws IOException, ApiException {
        // Step 13
        log.info("PaymentDetailsRequest {}", detailsRequest);
        var response = paymentsApi.paymentsDetails(detailsRequest);
        log.info("PaymentDetailsResponse {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/handleShopperRedirect")
    public RedirectView redirect(@RequestParam(required = false) String payload, @RequestParam(required = false) String redirectResult) throws IOException, ApiException {
        // Step 14
        var paymentDetailsRequest = new PaymentDetailsRequest();
        var paymentCompletionDetails = new PaymentCompletionDetails();

        if (redirectResult != null && !redirectResult.isEmpty()) {
            // For redirect, you are redirected to an Adyen domain to complete the 3DS2 challenge
            // After completing the 3DS2 challenge, you get the redirect result from Adyen in the returnUrl
            // We then pass on the redirectResult
            paymentCompletionDetails.redirectResult(redirectResult);
        } else if (payload != null && !payload.isEmpty()) {
            paymentCompletionDetails.payload(payload);
        }

        paymentDetailsRequest.setDetails(paymentCompletionDetails);

        var paymentsDetailsResponse = paymentsApi.paymentsDetails(paymentDetailsRequest);
        log.info("PaymentsDetailsResponse {}", paymentsDetailsResponse);


        // Handle response and redirect user accordingly
        var redirectURL = "/result/";
        switch (paymentsDetailsResponse.getResultCode()) {
            case AUTHORISED:
                redirectURL += "success";
                break;
            case PENDING:
            case RECEIVED:
                redirectURL += "pending";
                break;
            case REFUSED:
                redirectURL += "failed";
                break;
            default:
                redirectURL += "error";
                break;
        }
        log.info("redirectURL: {}", redirectURL);
        return new RedirectView(origin + redirectURL + "?reason=" + paymentsDetailsResponse.getResultCode());
    }
}