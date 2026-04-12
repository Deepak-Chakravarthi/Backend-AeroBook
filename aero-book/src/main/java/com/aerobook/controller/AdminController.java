package com.aerobook.controller;


import com.aerobook.domain.dto.request.*;
import com.aerobook.domain.dto.response.*;
import com.aerobook.service.AdminFlightService;
import com.aerobook.service.AdminReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminFlightService adminFlightService;
    private final AdminReportService adminReportService;

    // ── Bulk flight operations ────────────────────────────────────────

    @PostMapping("/flights/bulk-status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BulkOperationResponse> bulkUpdateStatus(
            @Valid @RequestBody BulkFlightStatusRequest request) {
        return ResponseEntity.ok(
                adminFlightService.bulkUpdateStatus(request));
    }

    @PostMapping("/flights/bulk-cancel")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<BulkOperationResponse> bulkCancel(
            @Valid @RequestBody BulkFlightCancelRequest request) {
        return ResponseEntity.ok(
                adminFlightService.bulkCancel(request));
    }

    // ── Revenue report ────────────────────────────────────────────────

    @PostMapping("/reports/revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AIRLINE_ADMIN')")
    public ResponseEntity<RevenueReportResponse> revenueReport(
            @Valid @RequestBody RevenueReportRequest request) {
        return ResponseEntity.ok(
                adminReportService.generateRevenueReport(request));
    }

    // ── Occupancy report ──────────────────────────────────────────────

    @PostMapping("/reports/occupancy")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AIRLINE_ADMIN')")
    public ResponseEntity<OccupancyReportResponse> occupancyReport(
            @Valid @RequestBody OccupancyReportRequest request) {
        return ResponseEntity.ok(
                adminReportService.generateOccupancyReport(request));
    }

    // ── Flight performance report ─────────────────────────────────────

    @GetMapping("/reports/performance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AIRLINE_ADMIN')")
    public ResponseEntity<FlightPerformanceResponse> performanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {
        return ResponseEntity.ok(
                adminReportService.generatePerformanceReport(from, to));
    }
}