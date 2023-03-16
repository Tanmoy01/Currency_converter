package com.example.Currency_Exchanage_demo.Service;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {

    @Value("${currency_rate.url}")
    private String resourceUrl;

    @Value("${currency_rate.api_key}")
    private String api_key;
    private String g10Currencies = "EUR,USD,CAD,GBP,JPY,CHF,SEK,NOK,AUD,NZD";
    private List<String> g10CurrencyList = Arrays.asList("EUR","USD","CAD","GBP","JPY","CHF","SEK","NOK","AUD","NZD");

    public Map<String,String> convert(String base, String destination){
        Map<String, String> result = new HashMap<>();
        try {
            String[] baseSplitted = base.split(" ");
            Double baseValue = Double.valueOf(baseSplitted[0]);
            String baseCode = baseSplitted[1];
            if (g10CurrencyList.contains(baseCode) && g10CurrencyList.contains(destination)) {
                JSONObject rates = getRates(g10Currencies);
                Double baseRate = rates.getDouble(baseCode);
                Double destinationRate = rates.getDouble(destination);
                Double ratio = destinationRate / baseRate;
                Double destinationValue = baseValue * ratio;
                result.put(base, destinationValue + " " + destination);
                return result;
            } else {
                result.put("error", "Only G10 currency exchange is allowed");
                return result;
            }
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
            result.put("error", "Error while converting");
            return result;
        }
    }

    JSONObject getRates(String currencies){
        String formatted_URL = MessageFormat.format(resourceUrl, currencies, "USD");

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("apikey", api_key);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<String> response = restTemplate.exchange(formatted_URL, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
        JSONObject responseJson = new JSONObject(response.getBody());
        return responseJson.getJSONObject("rates");
    }
}
