package com.example.Currency_Exchanage_demo.Controller;

import com.example.Currency_Exchanage_demo.Service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/currency")
public class CurrencyController {

    @Autowired
    CurrencyService service;

    @GetMapping("/exchange")
    ResponseEntity<Object> convert(@RequestHeader("base") String base,
                                   @RequestHeader("destination") String destination) {
        Map<String, String> result = service.convert(base, destination);
            if (!result.containsKey("error")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
    }

    @GetMapping("/predict")
    ResponseEntity<Object> predict(@RequestHeader("baseCurrency") String baseCurrency,
                                   @RequestHeader("date") String date) {

        Map<String, String> result = service.predict(baseCurrency, date);
        if (!result.containsKey("error")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }

    }
}
