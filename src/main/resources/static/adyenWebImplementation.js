const clientKey = document.getElementById("clientKey").innerHTML;
const type = document.getElementById("type").innerHTML;

// Starts the (Adyen.Web) AdyenCheckout with your specified configuration by calling the `/paymentMethods` endpoint.
async function startCheckout() {
    // Step 8
    let paymentMethodsResponse = await sendPostRequest("/api/paymentMethods");

    const configuration = {
        paymentMethodsResponse: paymentMethodsResponse,
        clientKey,
        locale: "en_US",
        environment: "test",
        showPayButton: true,
        paymentMethodsConfiguration: {
            card: {
                hasHolderName: true,
                holderNameRequired: true,
                name: "Credit or debit card",
                amount: {
                    value: 9998,
                    currency: "EUR",
                },
            }
        },
    };
    let adyenCheckout = await new AdyenCheckout(configuration);
    adyenCheckout.create(type).mount(document.getElementById("payment"));
}

// Step 12 - Handles responses, do a simple redirect based on the result.
function handleResponse(response, component) {

}

// This function sends a POST request to your specified URL,
// the `data`-parameters will be serialized as JSON in the body parameters.
async function sendPostRequest(url, data) {
    const res = await fetch(url, {
        method: "POST",
        body: data ? JSON.stringify(data) : "",
        headers: {
            "Content-Type": "application/json",
        },
    });

    return await res.json();
}

startCheckout();
