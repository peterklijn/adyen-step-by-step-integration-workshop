package com.adyen.workshop.controllers;

import com.adyen.model.notification.NotificationRequest;
import com.adyen.model.notification.NotificationRequestItem;
import com.adyen.util.HMACValidator;
import com.adyen.workshop.configurations.ApplicationConfiguration;
import org.apache.coyote.Response;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.SignatureException;

/**
 * REST controller for receiving Adyen webhook notifications
 */
@RestController
public class WebhookController {
    private final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final ApplicationConfiguration applicationConfiguration;

    private final HMACValidator hmacValidator;

    @Autowired
    public WebhookController(ApplicationConfiguration applicationConfiguration, HMACValidator hmacValidator) {
        this.applicationConfiguration = applicationConfiguration;
        this.hmacValidator = hmacValidator;
    }

    @PostMapping("/webhooks")
    public ResponseEntity<String> webhooks(@RequestBody String json) throws Exception {
        // Step 16
        log.info("Received webhook: {}", json);
        try {
            var notificationRequest = NotificationRequest.fromJson(json);
            var notificationRequestItem = notificationRequest.getNotificationItems().stream().findFirst();

            var item = notificationRequestItem.get();
            if (!hmacValidator.validateHMAC(item, applicationConfiguration.getAdyenHmacKey())) {
                log.warn("Could not validate HMAC signature for incoming webhook message: {}", item);
                return ResponseEntity.unprocessableEntity().build();
            }

            // Success, log it for now
            log.info("""
                    Received webhook with event {} :\s
                    Merchant Reference: {}
                    Alias : {}
                    PSP reference : {}""",
            item.getEventCode(),
            item.getMerchantReference(),
            item.getAdditionalData().get("alias"),
            item.getPspReference());

        } catch (Exception e) {
            log.error("Exception while handling webhook", e);
            ResponseEntity.internalServerError();
        }

        return ResponseEntity.ok("");
    }
}