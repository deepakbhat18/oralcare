package com.dental.clinic.service;

import com.dental.clinic.model.Bill;
import com.dental.clinic.model.Payment;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfInvoiceService {

    public byte[] generateInvoicePdf(Bill bill) throws IOException {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();


            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Font.BOLD);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Font.BOLD);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.BOLD);


            Paragraph title = new Paragraph("ORALCARE DENTAL CLINIC INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);


            if (bill.getClinic() != null) {
                Paragraph clinicInfo = new Paragraph(
                    bill.getClinic().getName() + "\n" +
                    bill.getClinic().getAddress() + "\n" +
                    "Contact: " + bill.getClinic().getContact() + "\n",
                    normalFont
                );
                clinicInfo.setAlignment(Element.ALIGN_CENTER);
                document.add(clinicInfo);
            } else {
                Paragraph clinicInfo = new Paragraph("OralCare Dental Services\nMain Branch\nContact: +1-234-567-8900\n", normalFont);
                clinicInfo.setAlignment(Element.ALIGN_CENTER);
                document.add(clinicInfo);
            }
            document.add(Chunk.NEWLINE);


            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = bill.getCreatedAt() != null ? bill.getCreatedAt().format(formatter) : "N/A";


            PdfPCell c1 = new PdfPCell();
            c1.setBorder(Rectangle.NO_BORDER);
            c1.addElement(new Paragraph("INVOICE DETAILS", sectionFont));
            c1.addElement(new Paragraph("Invoice Number: " + bill.getInvoiceNumber(), boldFont));
            c1.addElement(new Paragraph("Date: " + formattedDate, normalFont));
            c1.addElement(new Paragraph("Due Date: " + (bill.getDueDate() != null ? bill.getDueDate().format(formatter) : "N/A"), normalFont));
            c1.addElement(new Paragraph("Invoice Status: " + bill.getStatus(), boldFont));


            PdfPCell c2 = new PdfPCell();
            c2.setBorder(Rectangle.NO_BORDER);
            c2.addElement(new Paragraph("PATIENT DETAILS", sectionFont));
            c2.addElement(new Paragraph("Name: " + bill.getPatient().getName(), normalFont));
            if (bill.getPatient().getContact() != null) {
                c2.addElement(new Paragraph("Contact: " + bill.getPatient().getContact(), normalFont));
            }
            if (bill.getPatient().getUser() != null) {
                c2.addElement(new Paragraph("Email: " + bill.getPatient().getUser().getEmail(), normalFont));
            }

            infoTable.addCell(c1);
            infoTable.addCell(c2);
            document.add(infoTable);
            document.add(Chunk.NEWLINE);


            document.add(new Paragraph("TREATMENT PROCEDURES", sectionFont));
            document.add(Chunk.NEWLINE);

            PdfPTable itemsTable = new PdfPTable(2);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{4.0f, 1.0f});


            itemsTable.addCell(new PdfPCell(new Paragraph("Description", boldFont)));
            itemsTable.addCell(new PdfPCell(new Paragraph("Amount", boldFont)));


            if (bill.getTreatment() != null) {
                itemsTable.addCell(new Paragraph("Plan: " + bill.getTreatment().getPlanName() + "\nDescription: " + bill.getTreatment().getDescription(), normalFont));
            } else {
                itemsTable.addCell(new Paragraph("Dental Care Checkup & General Consultation", normalFont));
            }
            itemsTable.addCell(new Paragraph("₹" + bill.getAmount(), normalFont));


            itemsTable.addCell(new Paragraph("Subtotal", normalFont));
            itemsTable.addCell(new Paragraph("₹" + bill.getAmount(), normalFont));

            itemsTable.addCell(new Paragraph("Discount", normalFont));
            itemsTable.addCell(new Paragraph("-₹" + bill.getDiscount(), normalFont));

            itemsTable.addCell(new Paragraph("Tax", normalFont));
            itemsTable.addCell(new Paragraph("+₹" + bill.getTax(), normalFont));

            itemsTable.addCell(new Paragraph("Final Total Amount", boldFont));
            itemsTable.addCell(new Paragraph("₹" + bill.getTotalAmount(), boldFont));

            document.add(itemsTable);
            document.add(Chunk.NEWLINE);


            document.add(new Paragraph("PAYMENT HISTORY", sectionFont));
            document.add(Chunk.NEWLINE);

            PdfPTable paymentTable = new PdfPTable(4);
            paymentTable.setWidthPercentage(100);
            paymentTable.setWidths(new float[]{2.0f, 2.0f, 2.0f, 2.0f});

            paymentTable.addCell(new PdfPCell(new Paragraph("Date / Time", boldFont)));
            paymentTable.addCell(new PdfPCell(new Paragraph("Method", boldFont)));
            paymentTable.addCell(new PdfPCell(new Paragraph("Reference / Notes", boldFont)));
            paymentTable.addCell(new PdfPCell(new Paragraph("Amount", boldFont)));

            if (bill.getPayments() != null && !bill.getPayments().isEmpty()) {
                for (Payment payment : bill.getPayments()) {
                    String paymentDate = payment.getPaidAt() != null ? payment.getPaidAt().format(formatter) : "N/A";
                    String amountText = payment.isRefund() ? "-₹" + payment.getAmount() + " (REFUND)" : "₹" + payment.getAmount();

                    paymentTable.addCell(new Paragraph(paymentDate, normalFont));
                    paymentTable.addCell(new Paragraph(payment.getPaymentMethod(), normalFont));
                    paymentTable.addCell(new Paragraph(
                            (payment.getTransactionReference() != null ? payment.getTransactionReference() : "N/A") +
                            (payment.getNotes() != null ? " (" + payment.getNotes() + ")" : ""), normalFont));
                    paymentTable.addCell(new Paragraph(amountText, boldFont));
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Paragraph("No payments recorded yet", normalFont));
                emptyCell.setColspan(4);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                paymentTable.addCell(emptyCell);
            }
            document.add(paymentTable);
            document.add(Chunk.NEWLINE);


            double balanceDue = bill.getTotalAmount() - bill.getPaidAmount();
            Paragraph balancePara = new Paragraph("TOTAL PAID: ₹" + bill.getPaidAmount() + " | BALANCE DUE: ₹" + Math.max(0.0, balanceDue), boldFont);
            balancePara.setAlignment(Element.ALIGN_RIGHT);
            document.add(balancePara);


            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Thank you for choosing OralCare Dental Clinic! Keep smiling!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

        } catch (DocumentException e) {
            throw new IOException("Error generating PDF document: " + e.getMessage());
        } finally {
            document.close();
        }

        return out.toByteArray();
    }
}

/*
 * This file contains the PdfInvoiceService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
