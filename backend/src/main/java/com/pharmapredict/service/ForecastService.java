package com.pharmapredict.service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import com.pharmapredict.dto.ForecastRequest;
import com.pharmapredict.dto.MlForecastResponse;
import com.pharmapredict.entity.Medicine;
import com.pharmapredict.entity.Sale;
import com.pharmapredict.repository.SaleRepository;

@Service
public class ForecastService {
    private final MedicineService medicineService;
    private final SaleRepository saleRepository;
    private final RestClient restClient;

    public ForecastService(MedicineService medicineService, SaleRepository saleRepository,
                           RestClient.Builder builder, @Value("${app.ml-service-url}") String mlServiceUrl) {
        this.medicineService = medicineService; this.saleRepository = saleRepository;
        this.restClient = builder.baseUrl(mlServiceUrl).build();
    }

    public MlForecastResponse forecast(Long id, int days) {
        if (days < 1 || days > 30) throw new IllegalArgumentException("days must be between 1 and 30");
        Medicine medicine = medicineService.findEntity(id);
        LocalDate today = LocalDate.now(); LocalDate from = today.minusDays(29);
        Map<LocalDate, Integer> totals = saleRepository.findByMedicineIdAndSoldAtBetweenOrderBySoldAtAsc(id, from, today).stream()
                .collect(Collectors.groupingBy(Sale::getSoldAt, Collectors.summingInt(Sale::getQuantity)));
        List<Integer> history = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(today); day = day.plusDays(1)) history.add(totals.getOrDefault(day, 0));
        ForecastRequest request = new ForecastRequest(id, medicine.getName(), history, days, today.plusDays(1));
        try {
            MlForecastResponse response = restClient.post().uri("/forecast").body(request).retrieve().body(MlForecastResponse.class);
            if (response == null) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ML service returned an empty response");
            return response;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "ML forecasting service is unavailable", ex);
        }
    }
}