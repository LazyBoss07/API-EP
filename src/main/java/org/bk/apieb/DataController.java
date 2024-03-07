package org.bk.apieb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@RequestMapping("/api")
public class DataController {

    @PostMapping("/receive-data")
    public ResponseEntity<String> receiveDataFromESP32(@RequestBody DataModel requestData) {
        // Process the received data from ESP32
        System.out.println("Received data from ESP32: " + requestData);

        sendData(requestData);
        return ResponseEntity.ok("Data received successfully");
    }

    @PostMapping("/send-data")
    public ResponseEntity<String> sendData(@RequestBody DataModel dataModel) {
        String targetUrl = "https://eb-server.vercel.app/receive-data";

        try {
            // Convert DataModel to JSON stringbbb
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(dataModel);

            // Create connection
            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send JSON data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get response code
            int responseCode = connection.getResponseCode();
            String responseBody;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                responseBody = "JSON sent successfully";
            } else {
                responseBody = "Failed to send JSON. Response code: " + responseCode;
            }

            connection.disconnect();

            return ResponseEntity.ok(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while sending JSON.");
        }
    }
}
