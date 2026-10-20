package com.example.demo.config;

import com.example.demo.adapter.web.dto.ReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class RevenueReportPdfGenerator {


    private static final DeviceRgb HEADER_COLOR =
            new DeviceRgb(41, 128, 185);

    private static final DeviceRgb LIGHT_GRAY =
            new DeviceRgb(240, 240, 240);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final RestaurantTaxProperties taxProperties;

    public byte[] generate(
            ReportDto.RevenueSummary report
    ) {

        try (ByteArrayOutputStream baos =
                     new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);

            PdfDocument pdf =
                    new PdfDocument(writer);

            Document document =
                    new Document(pdf, PageSize.A4);

            document.setMargins(30, 30, 30, 30);

            addHeader(document, report);
            addRestaurantInformation(document);
            addPeriod(document, report);
            addSalesSummary(document, report);
            addTaxSummary(document, report);
            addComplianceInformation(document);
            addFooter(document);

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate revenue report PDF",
                    e
            );
        }
    }

    private void addHeader(
            Document document,
            ReportDto.RevenueSummary report
    ) {

        Table table = new Table(
                UnitValue.createPercentArray(
                        new float[]{65, 35}
                )
        ).useAllAvailableWidth();

        Cell title = new Cell()
                .add(
                        new Paragraph(
                                "GST SALES & REVENUE REPORT"
                        )
                                .setFontSize(19)
                                .setBold()
                                .setFontColor(HEADER_COLOR)
                )
                .add(
                        new Paragraph(
                                "Management & Tax Summary"
                        )
                                .setFontSize(9)
                                .setFontColor(ColorConstants.GRAY)
                )
                .setBorder(null);

        Cell meta = new Cell()
                .add(
                        new Paragraph(
                                "Period: " + report.getPeriod()
                        )
                                .setBold()
                                .setFontSize(10)
                )
                .add(
                        new Paragraph(
                                "Generated: " +
                                        java.time.LocalDateTime.now()
                                                .format(DATE_FORMAT)
                        )
                                .setFontSize(8)
                )
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(null);

        table.addCell(title);
        table.addCell(meta);

        document.add(table);

        document.add(
                new Paragraph(" ")
                        .setMarginBottom(5)
        );
    }

    private void addRestaurantInformation(
            Document document
    ) {

        Table table = new Table(
                UnitValue.createPercentArray(
                        new float[]{50, 50}
                )
        ).useAllAvailableWidth();

        Cell left = new Cell()
                .add(sectionTitle("Supplier Information"))
                .add(
                        new Paragraph(
                                safe(taxProperties.getTradeName())
                        ).setBold()
                )
                .add(
                        new Paragraph(
                                safe(taxProperties.getLegalName())
                        )
                )
                .add(
                        new Paragraph(
                                safe(taxProperties.getAddress())
                        )
                )
                .add(
                        new Paragraph(
                                safe(taxProperties.getCity())
                                        + ", "
                                        + safe(taxProperties.getState())
                                        + " - "
                                        + safe(taxProperties.getPostalCode())
                        )
                )
                .add(
                        new Paragraph(
                                "GSTIN: "
                                        + safe(taxProperties.getGstin())
                        ).setBold()
                )
                .setPadding(10);

        Cell right = new Cell()
                .add(sectionTitle("Service Classification"))
                .add(
                        new Paragraph(
                                "SAC: "
                                        + safe(taxProperties.getSac())
                        )
                )
                .add(
                        new Paragraph(
                                "Restaurant service"
                        )
                )
                .add(
                        new Paragraph(
                                "Default GST rate: "
                                        + taxProperties.getDefaultRate()
                                        + "%"
                        )
                )
                .add(
                        new Paragraph(
                                "ITC permitted: "
                                        + (taxProperties.isInputTaxCreditAllowed()
                                        ? "Yes"
                                        : "No")
                        )
                )
                .setPadding(10);

        table.addCell(left);
        table.addCell(right);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addPeriod(
            Document document,
            ReportDto.RevenueSummary report
    ) {

        Table table = new Table(1)
                .useAllAvailableWidth();

        table.addCell(
                new Cell()
                        .add(
                                new Paragraph(
                                        "Reporting Period"
                                ).setBold()
                        )
                        .add(
                                new Paragraph(
                                        report.getFrom()
                                                .format(DATE_FORMAT)
                                                + "  →  "
                                                + report.getTo()
                                                .format(DATE_FORMAT)
                                )
                        )
                        .setBackgroundColor(LIGHT_GRAY)
                        .setPadding(8)
        );

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addSalesSummary(
            Document document,
            ReportDto.RevenueSummary report
    ) {

        document.add(
                new Paragraph("Sales Summary")
                        .setFontSize(13)
                        .setBold()
                        .setFontColor(HEADER_COLOR)
        );

        Table table = new Table(
                UnitValue.createPercentArray(
                        new float[]{65, 35}
                )
        ).useAllAvailableWidth();

        addAmountRow(
                table,
                "Gross Sales",
                report.getGrossSales()
        );

        addAmountRow(
                table,
                "Discounts",
                report.getTotalDiscount()
        );

        addAmountRow(
                table,
                "Taxable Sales",
                report.getTaxableSales()
        );

        addAmountRow(
                table,
                "GST Collected",
                report.getTotalTax()
        );

        Cell totalLabel =
                new Cell()
                        .add(
                                new Paragraph(
                                        "TOTAL INVOICE VALUE"
                                )
                                        .setBold()
                                        .setFontColor(ColorConstants.WHITE)
                        )
                        .setBackgroundColor(HEADER_COLOR)
                        .setPadding(8);

        Cell totalValue =
                new Cell()
                        .add(
                                new Paragraph(
                                        money(
                                                report.getTotalInvoiceValue()
                                        )
                                )
                                        .setBold()
                                        .setFontColor(ColorConstants.WHITE)
                        )
                        .setBackgroundColor(HEADER_COLOR)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setPadding(8);

        table.addCell(totalLabel);
        table.addCell(totalValue);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addTaxSummary(
            Document document,
            ReportDto.RevenueSummary report
    ) {

        document.add(
                new Paragraph("GST Summary")
                        .setFontSize(13)
                        .setBold()
                        .setFontColor(HEADER_COLOR)
        );

        Table table = new Table(
                UnitValue.createPercentArray(
                        new float[]{60, 20, 20}
                )
        ).useAllAvailableWidth();

        table.addHeaderCell(
                new Cell()
                        .add(new Paragraph("Tax"))
                        .setBold()
        );

        table.addHeaderCell(
                new Cell()
                        .add(new Paragraph("Amount"))
                        .setBold()
                        .setTextAlignment(TextAlignment.RIGHT)
        );

        table.addHeaderCell(
                new Cell()
                        .add(new Paragraph("Rate"))
                        .setBold()
                        .setTextAlignment(TextAlignment.RIGHT)
        );

        addTaxRow(
                table,
                "CGST",
                report.getTotalCgst()
        );

        addTaxRow(
                table,
                "SGST",
                report.getTotalSgst()
        );

        addTaxRow(
                table,
                "IGST",
                report.getTotalIgst()
        );

        addTaxRow(
                table,
                "Total GST",
                report.getTotalTax()
        );

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addComplianceInformation(
            Document document
    ) {

        Table table = new Table(1)
                .useAllAvailableWidth();

        table.addCell(
                new Cell()
                        .add(
                                new Paragraph(
                                        "Tax & Compliance Information"
                                )
                                        .setBold()
                        )
                        .add(
                                new Paragraph(
                                        "SAC: "
                                                + safe(
                                                taxProperties.getSac()
                                        )
                                )
                        )
                        .add(
                                new Paragraph(
                                        "GSTIN: "
                                                + safe(
                                                taxProperties.getGstin()
                                        )
                                )
                        )
                        .add(
                                new Paragraph(
                                        "This report is generated from "
                                                + "recorded sales transactions "
                                                + "and is intended for "
                                                + "management, accounting and "
                                                + "GST reconciliation purposes."
                                )
                                        .setFontSize(8)
                                        .setFontColor(
                                                ColorConstants.DARK_GRAY
                                        )
                        )
                        .setPadding(10)
                        .setBackgroundColor(LIGHT_GRAY)
        );

        document.add(table);
    }

    private void addFooter(
            Document document
    ) {

        document.add(
                new Paragraph(
                        "This is a computer-generated report."
                )
                        .setFontSize(8)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20)
        );
    }

    private Paragraph sectionTitle(String text) {

        return new Paragraph(text)
                .setBold()
                .setFontSize(11)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(HEADER_COLOR)
                .setPadding(5);
    }

    private void addAmountRow(
            Table table,
            String label,
            BigDecimal amount
    ) {

        table.addCell(
                new Cell()
                        .add(new Paragraph(label))
                        .setPadding(8)
        );

        table.addCell(
                new Cell()
                        .add(new Paragraph(money(amount)))
                        .setPadding(8)
                        .setTextAlignment(TextAlignment.RIGHT)
        );
    }

    private void addTaxRow(
            Table table,
            String label,
            BigDecimal amount
    ) {

        table.addCell(
                new Cell()
                        .add(new Paragraph(label))
                        .setPadding(7)
        );

        table.addCell(
                new Cell()
                        .add(new Paragraph(money(amount)))
                        .setPadding(7)
                        .setTextAlignment(TextAlignment.RIGHT)
        );

        table.addCell(
                new Cell()
                        .add(
                                new Paragraph(
                                        label.equals("Total GST")
                                                ? "-"
                                                : taxProperties
                                                  .getDefaultRate()
                                                  + "%"
                                )
                        )
                        .setPadding(7)
                        .setTextAlignment(TextAlignment.RIGHT)
        );
    }

    private String money(BigDecimal value) {

        return "₹ "
                + (value == null
                ? "0.00"
                : value.setScale(
                2,
                java.math.RoundingMode.HALF_UP
        ));
    }

    private String safe(String value) {
        return value == null || value.isBlank()
                ? "-"
                : value;
    }
}
