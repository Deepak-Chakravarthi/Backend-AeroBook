package com.aerobook.controller;

import com.aerobook.domain.dto.request.RouteGetRequest;
import com.aerobook.domain.dto.request.RouteRequest;
import com.aerobook.domain.dto.response.RouteResponse;
import com.aerobook.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    /**
     * GET /api/v1/routes?id=1
     * GET /api/v1/routes?originCode=DEL
     * GET /api/v1/routes?destinationCode=BOM
     * GET /api/v1/routes?status=ACTIVE
     *
     * Exactly one param must be passed.
     */
    @GetMapping
    public ResponseEntity<?> getRoute(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String originCode,
            @RequestParam(required = false) String destinationCode,
            @RequestParam(required = false) String status) {

        RouteGetRequest request = RouteGetRequest.builder()
                .id(id)
                .originCode(originCode)
                .destinationCode(destinationCode)
                .status(status)
                .build();

        request.validate();

        return ResponseEntity.ok(routeService.getRoute(request));
    }

    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id,
                                                     @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}
