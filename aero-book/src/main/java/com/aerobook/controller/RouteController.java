package com.aerobook.controller;


import com.aerobook.domain.dto.request.RouteRequest;
import com.aerobook.domain.dto.response.RouteResponse;
import com.aerobook.service.RouteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@AllArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<RouteResponse> getRouteByAirportCodes(
            @RequestParam String origin,
            @RequestParam String destination) {
        return ResponseEntity.ok(routeService.getRouteByAirportCodes(origin, destination));
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
