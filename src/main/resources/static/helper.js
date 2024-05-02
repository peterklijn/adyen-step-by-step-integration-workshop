/*
 This function sends a POST request to your specified URL, the `data`-parameters will be serialized as JSON in the body parameters.
 */
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