package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

public class SingleGetter {

    public static void main(String[] args) {
        try {
            makeCall();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makeCall() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("https://httpbin.org/get");

            // add request headers
            request.addHeader("custom-key", "mkyong");
            request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    JSONObject result = new JSONObject(EntityUtils.toString(entity));
                    if (result.getJSONObject("headers").has("Traceparent")) {
                        System.exit(0);
                    }
                }
            }
        }
        System.exit(1);
    }

}