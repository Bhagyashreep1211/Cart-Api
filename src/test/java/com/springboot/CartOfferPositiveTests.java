package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.OfferRequest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferPositiveTests {

    private final String BASE_URL = "http://localhost:9001/api/v1";

    @Test
    public void checkFlatXForOneSegment() throws Exception {
        List<String> segments = new ArrayList<>();
        segments.add("p1");
        OfferRequest offerRequest =  OfferRequest.builder()
                .restaurant_id(1)
                .offer_type("FLATX")
                .offer_value(10)
                .customer_segment(segments)
                .build();

        boolean result = addOffer(offerRequest);
        Assert.assertTrue(result);

        ApplyOfferRequest applyOfferRequest = ApplyOfferRequest.builder().cart_value(190).restaurant_id(200).user_id(1)
                .build();

        int discountedValue = applyOffer(applyOfferRequest);
        Assert.assertEquals(190, discountedValue);
    }


    @Test
    public void checkFlatXDiscountForMatchingSegment() throws Exception {
        // Add offer with segment "p2"
        List<String> segments = new ArrayList<>();
        segments.add("p2");
        OfferRequest offerRequest = OfferRequest.builder().restaurant_id(1).offer_type("FLATX").offer_value(100).customer_segment(segments)
                .build();

        boolean result = addOffer(offerRequest);
        Assert.assertTrue(result);

        ApplyOfferRequest applyOfferRequest = ApplyOfferRequest.builder()
                .cart_value(200)
                .restaurant_id(1)
                .user_id(2)
                .build();

        int discountedValue = applyOffer(applyOfferRequest);
        Assert.assertEquals(200, discountedValue);
    }

    @Test
    public void checkPercentageDiscountForMatchingSegment() throws Exception {
        // Add offer with percentage discount "PERCENTX"
        List<String> segments = new ArrayList<>();
        segments.add("p3");
        OfferRequest offerRequest = OfferRequest.builder()
                .restaurant_id(1)
                .offer_type("PERCENTX")
                .offer_value(20)
                .customer_segment(segments)
                .build();

        boolean result = addOffer(offerRequest);
        Assert.assertTrue(result);

        ApplyOfferRequest applyOfferRequest = ApplyOfferRequest.builder()
                .cart_value(400)
                .restaurant_id(1)
                .user_id(3)
                .build();


        int discountedValue = applyOffer(applyOfferRequest);
        Assert.assertEquals(400, discountedValue);  // 500 - 20% = 400
    }

    @Test
    public void checkZeroDiscountForInvalidOffer() throws Exception {
        // Add offer with invalid discount type
        List<String> segments = new ArrayList<>();
        segments.add("p4");
        OfferRequest offerRequest = OfferRequest.builder()
                .restaurant_id(1)
                .offer_type("INVALID_TYPE")
                .offer_value(100)
                .customer_segment(segments)
                .build();


        boolean result = addOffer(offerRequest);
        Assert.assertTrue(result);

        ApplyOfferRequest applyOfferRequest = ApplyOfferRequest.builder()
                .cart_value(500)
                .restaurant_id(1)
                .user_id(4)
                .build();

        int discountedValue = applyOffer(applyOfferRequest);
        Assert.assertEquals(500, discountedValue);  // No valid offer, no discount
    }

    private boolean addOffer(OfferRequest offerRequest) throws Exception {
        String urlString = BASE_URL + "/offer";
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        ObjectMapper mapper = new ObjectMapper();
        String POST_PARAMS = mapper.writeValueAsString(offerRequest);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();
        return responseCode == HttpURLConnection.HTTP_OK;
    }

    private int applyOffer(ApplyOfferRequest applyOfferRequest) throws Exception {
        String urlString = BASE_URL + "/cart/apply_offer";
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        ObjectMapper mapper = new ObjectMapper();
        String POST_PARAMS = mapper.writeValueAsString(applyOfferRequest);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response.toString()).get("cart_value").asInt();
        } else {
            throw new RuntimeException("Failed to apply offer. HTTP Code: " + responseCode);
        }
    }

    private String fetchUserSegment(int userId) throws Exception {
        String urlString = BASE_URL + "/user_segment?user_id=" + userId;
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response.toString()).get("segment").asText();
        } else {
            throw new RuntimeException("Failed to fetch user segment. HTTP Code: " + responseCode);
        }
    }
}
