package com.aerobook.controller;

import com.aerobook.domain.dto.request.RouteRequest;
import com.aerobook.domain.dto.request.get.RouteGetRequest;
import com.aerobook.domain.dto.response.RouteResponse;
import com.aerobook.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * The type Route controller.
 */
@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    /**
     * Method to getRoute
     *
     * @param id              the id
     * @param originCode      the origin code
     * @param destinationCode the destination code
     * @param status          the status
     * @param pageable        the pageable
     * @return the route
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getRoute(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String originCode,
            @RequestParam(required = false) String destinationCode,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {

        RouteGetRequest request = RouteGetRequest.builder()
                .id(id)
                .originCode(originCode)
                .destinationCode(destinationCode)
                .status(status)
                .build();

        return ResponseEntity.ok(routeService.getRoutes(request, pageable));
    }

    /**
     * Create route response entity.
     *
     * @param request the request
     * @return the response entity
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(routeService.createRoute(request));
    }

    /**
     * Update route response entity.
     *
     * @param id      the id
     * @param request the request
     * @return the response entity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AIRLINE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long id,
                                                     @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    /**
     * Delete route response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }
}
