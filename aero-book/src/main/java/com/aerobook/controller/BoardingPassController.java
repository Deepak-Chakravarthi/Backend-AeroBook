package com.aerobook.controller;


import com.aerobook.domain.dto.response.BoardingPassResponse;
import com.aerobook.service.BoardingPassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The type Boarding pass controller.
 */
@RestController
@RequestMapping("/boarding-passes")
@RequiredArgsConstructor
public class BoardingPassController {

    private final BoardingPassService boardingPassService;

    /**
     * Gets boarding pass by id.
     *
     * @param id the id
     * @return the boarding pass by id
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BoardingPassResponse> getBoardingPassById(
            @PathVariable Long id) {
        return ResponseEntity.ok(boardingPassService.getBoardingPassById(id));
    }

    /**
     * Gets boarding passes by flight.
     *
     * @param flightId the flight id
     * @return the boarding passes by flight
     */
    @GetMapping("/flight/{flightId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT', 'AIRLINE_ADMIN')")
    public ResponseEntity<List<BoardingPassResponse>> getBoardingPassesByFlight(
            @PathVariable Long flightId) {
        return ResponseEntity.ok(
                boardingPassService.getBoardingPassesByFlight(flightId));
    }

    /**
     * Download boarding pass pdf response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadBoardingPassPdf(@PathVariable Long id) {
        byte[] pdf = boardingPassService.downloadBoardingPassPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "boarding-pass-" + id + ".pdf");
        headers.setContentLength(pdf.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    /**
     * Cancel boarding pass response entity.
     *
     * @param id the id
     * @return the response entity
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AGENT')")
    public ResponseEntity<Void> cancelBoardingPass(@PathVariable Long id) {
        boardingPassService.cancelBoardingPass(id);
        return ResponseEntity.noContent().build();
    }
}