package com.aerobook.util.pdf;


import com.aerobook.entity.BoardingPass;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class BoardingPassPdfGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private static final DeviceRgb HEADER_COLOR =
            new DeviceRgb(30, 64, 175);     // AeroBook blue

    private static final DeviceRgb LIGHT_GRAY =
            new DeviceRgb(243, 244, 246);

    public byte[] generate(BoardingPass boardingPass) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer   = new PdfWriter(baos);
            PdfDocument pdf    = new PdfDocument(writer);
            Document document  = new Document(pdf, PageSize.A5.rotate());

            PdfFont boldFont    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ── Header ───────────────────────────────────────────────
            Paragraph header = new Paragraph("✈  AEROBOOK — BOARDING PASS")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(HEADER_COLOR)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(10);
            document.add(header);

            // ── Route + Flight info ───────────────────────────────────
            Table routeTable = new Table(UnitValue.createPercentArray(
                    new float[]{40, 20, 40}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(10);

            routeTable.addCell(originCell(
                    boardingPass.getOriginCode(), boldFont, regularFont));
            routeTable.addCell(arrowCell(boldFont));
            routeTable.addCell(destinationCell(
                    boardingPass.getDestinationCode(), boldFont, regularFont));

            document.add(routeTable);

            // ── Details table ─────────────────────────────────────────
            Table detailsTable = new Table(UnitValue.createPercentArray(
                    new float[]{25, 25, 25, 25}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(15);

            addDetailCell(detailsTable, "PASSENGER",
                    boardingPass.getPassengerName(),
                    boldFont, regularFont);
            addDetailCell(detailsTable, "FLIGHT",
                    boardingPass.getFlightNumber(),
                    boldFont, regularFont);
            addDetailCell(detailsTable, "DEPARTURE",
                    boardingPass.getDepartureTime().format(DATE_TIME_FORMAT),
                    boldFont, regularFont);
            addDetailCell(detailsTable, "BOARDING",
                    boardingPass.getBoardingTime().format(DATE_TIME_FORMAT),
                    boldFont, regularFont);
            addDetailCell(detailsTable, "SEAT",
                    boardingPass.getSeatNumber(),
                    boldFont, regularFont);
            addDetailCell(detailsTable, "CLASS",
                    boardingPass.getSeatClass().name(),
                    boldFont, regularFont);
            addDetailCell(detailsTable, "GATE",
                    boardingPass.getGate() != null ? boardingPass.getGate() : "TBA",
                    boldFont, regularFont);
            addDetailCell(detailsTable, "TERMINAL",
                    boardingPass.getTerminal() != null ? boardingPass.getTerminal() : "TBA",
                    boldFont, regularFont);
            addDetailCell(detailsTable, "BOARDING GROUP",
                    boardingPass.getBoardingGroup(),
                    boldFont, regularFont);
            addDetailCell(detailsTable, "PNR",
                    boardingPass.getCheckIn().getBooking().getPnr(),
                    boldFont, regularFont);

            document.add(detailsTable);

            // ── Barcode section ───────────────────────────────────────
            Paragraph barcode = new Paragraph(boardingPass.getBarcode())
                    .setFont(regularFont)
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(LIGHT_GRAY)
                    .setPadding(8)
                    .setMarginTop(15);
            document.add(barcode);

            // ── Footer ────────────────────────────────────────────────
            Paragraph footer = new Paragraph(
                    "Please arrive at the gate at least 30 minutes before boarding time. "
                            + "This boarding pass is non-transferable.")
                    .setFont(regularFont)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10);
            document.add(footer);

            document.close();
            log.info("PDF generated for boarding pass: {}",
                    boardingPass.getBoardingPassNumber());

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate boarding pass PDF: {}", e.getMessage());
            throw new com.aerobook.exception.AeroBookException(
                    "Failed to generate boarding pass PDF",
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "PDF_GENERATION_FAILED"
            );
        }
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private Cell originCell(String code, PdfFont bold, PdfFont regular) {
        return new Cell()
                .add(new Paragraph(code).setFont(bold).setFontSize(28)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("ORIGIN").setFont(regular).setFontSize(9)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
    }

    private Cell arrowCell(PdfFont bold) {
        return new Cell()
                .add(new Paragraph("→").setFont(bold).setFontSize(24)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(new DeviceRgb(30, 64, 175)))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
    }

    private Cell destinationCell(String code, PdfFont bold, PdfFont regular) {
        return new Cell()
                .add(new Paragraph(code).setFont(bold).setFontSize(28)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("DESTINATION").setFont(regular).setFontSize(9)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
    }

    private void addDetailCell(Table table, String label, String value,
                               PdfFont bold, PdfFont regular) {
        Cell cell = new Cell()
                .add(new Paragraph(label).setFont(regular).setFontSize(8)
                        .setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(value).setFont(bold).setFontSize(12))
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(8)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setMargin(2);
        table.addCell(cell);
    }
}