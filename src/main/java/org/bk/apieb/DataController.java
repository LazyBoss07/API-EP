package org.bk.apieb;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@RequestMapping("/demo")
public class DataController {

    @PostMapping("/receive-data")
    public ResponseEntity<String> receiveDataFromESP32(@RequestBody String requestData) {
        // Process the received data from ESP32
        System.out.println("Received data from ESP32: " + requestData);

        // You can process the data here or return a response to ESP32
        // For now, just acknowledge the receipt of data
        return ResponseEntity.ok("Data received successfully");
    }

    @PostMapping("/send-data")
    public ResponseEntity<String> sendData(@RequestBody DataModel dataToSend) {
        String externalUrl = "http://example.com/external-endpoint";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            URL url = new URL(externalUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{\"current\": \"" + dataToSend.getCurrent() + "\", \"voltage\": \"" + dataToSend.getVoltage() + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            String responseBody;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    responseBody = response.toString();
                }
            } else {
                responseBody = "Failed to send data. Response code: " + responseCode;
            }

            connection.disconnect();

            return ResponseEntity.ok(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while sending data.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
        }
    }
}
