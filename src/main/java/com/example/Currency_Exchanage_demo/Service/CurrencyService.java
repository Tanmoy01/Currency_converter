package com.example.Currency_Exchanage_demo.Service;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CurrencyService {

    private final String g10Currencies = "EUR,USD,CAD,GBP,JPY,CHF,SEK,NOK,AUD,NZD";
    private final List<String> g10CurrencyList = Arrays.asList("EUR", "USD", "CAD", "GBP", "JPY", "CHF", "SEK", "NOK", "AUD", "NZD");
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${currency_rate.latest_url}")
    private String latestRatesUrl;
    @Value("${currency_rate.historical_url}")
    private String historicalRatesUrl;
    @Value("${currency_rate.api_key}")
    private String api_key;

    public Map<String, String> convert(String base, String destination) {
        Map<String, String> result = new HashMap<>();
        try {
            String[] baseSplitted = base.split(" ");
            Double baseValue = Double.valueOf(baseSplitted[0]);
            String baseCode = baseSplitted[1];
            if (g10CurrencyList.contains(baseCode) && g10CurrencyList.contains(destination)) {
                JSONObject rates = getLatestRates(g10Currencies);
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
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            result.put("error", "Error while converting");
            return result;
        }
    }

    public Map<String, String> predict(String currency, String date) {
        Map<String, String> result = new HashMap<>();
        LocalDate endDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            try {
                endDate = LocalDate.parse(date);
            } catch (Exception e) {
                result.put("error", "Date format should be yyyy-MM-dd");
                return result;
            }
            LocalDate startDate = endDate.minusDays(30);
            String endDateStr = formatter.format(endDate);
            String startDateStr = formatter.format(startDate);
            JSONObject rates = getHistoricalRates(currency, startDateStr, endDateStr);
            Map<String, Object> ratesMap = rates.toMap();

            List<Map<String, BigDecimal>> currencyRates = new ArrayList<>();
            ratesMap.values().forEach(val -> {
                currencyRates.add((Map<String, BigDecimal>) val);

            });
            System.out.println(currencyRates);
            BigDecimal sumVal = BigDecimal.ZERO;
            for (Map<String, BigDecimal> rate : currencyRates) {
                sumVal = sumVal.add(rate.get(currency));
            }
            BigDecimal predictedValue = sumVal.divide(new BigDecimal(currencyRates.size()), 2, RoundingMode.HALF_EVEN);
            result.put("predictedValue", predictedValue + " " + currency);
            return result;
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            result.put("error", "Unable to predict. The date is too far from current date or currency code is Invalid");
            return result;
        }
    }

    private JSONObject getLatestRates(String currencies) {
        String formatted_URL = MessageFormat.format(latestRatesUrl, currencies, "USD");
        HttpHeaders headers = new HttpHeaders();
        headers.add("apikey", api_key);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<String> response = restTemplate.exchange(formatted_URL, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
        JSONObject responseJson = new JSONObject(response.getBody());
        return responseJson.getJSONObject("rates");
    }

    private JSONObject getHistoricalRates(String currency, String startDate, String endDate) {
        String formatted_URL = MessageFormat.format(historicalRatesUrl, currency, "USD", startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.add("apikey", api_key);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<String> response = restTemplate.exchange(formatted_URL, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
        JSONObject responseJson = new JSONObject(response.getBody());
        return responseJson.getJSONObject("rates");
    }


}
