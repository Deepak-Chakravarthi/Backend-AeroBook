package com.aerobook.service;

import com.aerobook.domain.dto.response.BoardingPassResponse;
import com.aerobook.domain.enums.BoardingPassStatus;
import com.aerobook.domain.enums.CheckInStatus;
import com.aerobook.enitity.BoardingPass;
import com.aerobook.enitity.Booking;
import com.aerobook.enitity.CheckIn;
import com.aerobook.enitity.Flight;
import com.aerobook.exception.AeroBookException;
import com.aerobook.exception.ResourceNotFoundException;
import com.aerobook.mapper.CheckInMapper;
import com.aerobook.repository.BoardingPassRepository;
import com.aerobook.repository.CheckInRepository;
import com.aerobook.util.BoardingPassNumberGenerator;
import com.aerobook.util.pdf.BoardingPassPdfGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardingPassService {

    private final BoardingPassRepository      boardingPassRepository;
    private final CheckInRepository           checkInRepository;
    private final CheckInMapper               checkInMapper;
    private final BoardingPassNumberGenerator bpNumberGenerator;
    private final BoardingPassPdfGenerator    pdfGenerator;

    private static final int BOARDING_BEFORE_DEPARTURE_MINS = 45;

    // ----------------------------------------------------------------
    // Get boarding pass by id
    // ----------------------------------------------------------------
    public BoardingPassResponse getBoardingPassById(Long id) {
        return checkInMapper.toBoardingPassResponse(findBoardingPassById(id));
    }

    // ----------------------------------------------------------------
    // Get boarding pass by check-in
    // ----------------------------------------------------------------
    public BoardingPassResponse getBoardingPassByCheckIn(Long checkInId) {
        BoardingPass bp = boardingPassRepository.findByCheckInId(checkInId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BoardingPass", "checkInId", String.valueOf(checkInId)));
        return checkInMapper.toBoardingPassResponse(bp);
    }

    // ----------------------------------------------------------------
    // Get boarding passes by flight
    // ----------------------------------------------------------------
    public List<BoardingPassResponse> getBoardingPassesByFlight(Long flightId) {
        return boardingPassRepository.findAllByFlightId(flightId)
                .stream()
                .map(checkInMapper::toBoardingPassResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // Issue boarding pass — after check-in
    // ----------------------------------------------------------------
    @Transactional
    public BoardingPassResponse issueBoardingPass(Long checkInId) {
        CheckIn checkIn = checkInRepository.findByIdWithDetails(checkInId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CheckIn", checkInId));

        // Validate check-in status
        if (checkIn.getStatus() != CheckInStatus.CHECKED_IN) {
            throw new AeroBookException(
                    "Boarding pass can only be issued after check-in. " +
                            "Current status: " + checkIn.getStatus(),
                    HttpStatus.CONFLICT,
                    "CHECKIN_NOT_COMPLETE"
            );
        }

        // Check if already issued
        if (boardingPassRepository.findByCheckInId(checkInId).isPresent()) {
            throw new AeroBookException(
                    "Boarding pass already issued for this check-in",
                    HttpStatus.CONFLICT,
                    "BOARDING_PASS_ALREADY_ISSUED"
            );
        }

        Flight flight    = checkIn.getFlight();
        Booking booking   = checkIn.getBooking();
        String  passengerName = checkIn.getPassenger().getFirstName()
                + " " + checkIn.getPassenger().getLastName();

        String bpNumber  = generateUniqueBoardingPassNumber(
                booking.getPnr(), checkIn.getSeatNumber());
        String barcode   = bpNumberGenerator.generateBarcode(bpNumber);
        LocalDateTime boardingTime = flight.getDepartureTime()
                .minusMinutes(BOARDING_BEFORE_DEPARTURE_MINS);

        BoardingPass boardingPass = BoardingPass.builder()
                .boardingPassNumber(bpNumber)
                .checkIn(checkIn)
                .ticket(checkIn.getTicket())
                .passenger(checkIn.getPassenger())
                .flight(flight)
                .passengerName(passengerName)
                .flightNumber(flight.getFlightNumber())
                .originCode(flight.getRoute().getOrigin().getIataCode())
                .destinationCode(flight.getRoute().getDestination().getIataCode())
                .departureTime(flight.getDepartureTime())
                .seatNumber(checkIn.getSeatNumber())
                .seatClass(checkIn.getSeatClass())
                .gate(flight.getGate())
                .terminal(flight.getTerminal())
                .boardingGroup(checkIn.getBoardingGroup())
                .boardingTime(boardingTime)
                .barcode(barcode)
                .status(BoardingPassStatus.ISSUED)
                .createdAt(LocalDateTime.now())
                .build();

        BoardingPass saved = boardingPassRepository.save(boardingPass);

        // Update check-in status
        checkIn.markBoardingPassIssued();
        checkInRepository.save(checkIn);

        log.info("Boarding pass issued — {}, seat: {}, flight: {}",
                bpNumber, checkIn.getSeatNumber(), flight.getFlightNumber());

        return checkInMapper.toBoardingPassResponse(saved);
    }

    // ----------------------------------------------------------------
    // Download boarding pass as PDF
    // ----------------------------------------------------------------
    public byte[] downloadBoardingPassPdf(Long boardingPassId) {
        BoardingPass boardingPass = findBoardingPassById(boardingPassId);

        if (boardingPass.getStatus() == BoardingPassStatus.CANCELLED
                || boardingPass.getStatus() == BoardingPassStatus.EXPIRED) {
            throw new AeroBookException(
                    "Boarding pass is " + boardingPass.getStatus()
                            + " and cannot be downloaded",
                    HttpStatus.CONFLICT,
                    "BOARDING_PASS_NOT_VALID"
            );
        }

        log.info("Generating PDF for boarding pass: {}",
                boardingPass.getBoardingPassNumber());

        return pdfGenerator.generate(boardingPass);
    }

    // ----------------------------------------------------------------
    // Cancel boarding pass
    // ----------------------------------------------------------------
    @Transactional
    public void cancelBoardingPass(Long boardingPassId) {
        BoardingPass boardingPass = findBoardingPassById(boardingPassId);
        boardingPass.cancel();
        boardingPassRepository.save(boardingPass);
        log.info("Boarding pass cancelled: {}", boardingPass.getBoardingPassNumber());
    }

    // ----------------------------------------------------------------
    // Internal
    // ----------------------------------------------------------------
    public BoardingPass findBoardingPassById(Long id) {
        return boardingPassRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "BoardingPass", id));
    }

    // ----------------------------------------------------------------
    // Private
    // ----------------------------------------------------------------
    private String generateUniqueBoardingPassNumber(String pnr,
                                                    String seatNumber) {
        String number;
        do {
            number = bpNumberGenerator.generate(pnr, seatNumber);
        } while (boardingPassRepository.existsByBoardingPassNumber(number));
        return number;
    }
}