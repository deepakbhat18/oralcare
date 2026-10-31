package com.dental.clinic.service;

import com.dental.clinic.model.Appointment;
import com.dental.clinic.model.Bill;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    public byte[] exportAppointmentsToExcel(List<Appointment> appointments) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Appointments");


            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Patient Name", "Dentist Name", "Treatment Type", "Date/Time", "Status", "Room"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            int rowIdx = 1;
            for (Appointment apt : appointments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(apt.getId());
                row.createCell(1).setCellValue(apt.getPatient() != null ? apt.getPatient().getName() : "N/A");
                row.createCell(2).setCellValue(apt.getDentist() != null ? apt.getDentist().getName() : "N/A");
                row.createCell(3).setCellValue(apt.getTreatmentType());
                row.createCell(4).setCellValue(apt.getDateTime() != null ? apt.getDateTime().format(formatter) : "N/A");
                row.createCell(5).setCellValue(apt.getStatus().name());
                row.createCell(6).setCellValue(apt.getRoomNumber() != null ? apt.getRoomNumber() : "N/A");
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportRevenueToExcel(List<Bill> bills) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Revenue Report");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Invoice Number", "Patient Name", "Amount", "Discount", "Tax", "Total Amount", "Status", "Paid At", "Payment Method"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            int rowIdx = 1;
            for (Bill bill : bills) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(bill.getInvoiceNumber());
                row.createCell(1).setCellValue(bill.getPatient() != null ? bill.getPatient().getName() : "N/A");
                row.createCell(2).setCellValue(bill.getAmount());
                row.createCell(3).setCellValue(bill.getDiscount());
                row.createCell(4).setCellValue(bill.getTax());
                row.createCell(5).setCellValue(bill.getTotalAmount());
                row.createCell(6).setCellValue(bill.getStatus().name());
                row.createCell(7).setCellValue(bill.getPaidAt() != null ? bill.getPaidAt().format(formatter) : "N/A");
                row.createCell(8).setCellValue(bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "N/A");
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public String exportAppointmentsToCsv(List<Appointment> appointments) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Patient Name,Dentist Name,Treatment Type,Date/Time,Status,Room\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Appointment apt : appointments) {
            csv.append(apt.getId()).append(",")
               .append(escapeCsvField(apt.getPatient() != null ? apt.getPatient().getName() : "N/A")).append(",")
               .append(escapeCsvField(apt.getDentist() != null ? apt.getDentist().getName() : "N/A")).append(",")
               .append(escapeCsvField(apt.getTreatmentType())).append(",")
               .append(apt.getDateTime() != null ? apt.getDateTime().format(formatter) : "N/A").append(",")
               .append(apt.getStatus().name()).append(",")
               .append(escapeCsvField(apt.getRoomNumber() != null ? apt.getRoomNumber() : "N/A")).append("\n");
        }
        return csv.toString();
    }

    public String exportRevenueToCsv(List<Bill> bills) {
        StringBuilder csv = new StringBuilder();
        csv.append("Invoice Number,Patient Name,Amount,Discount,Tax,Total Amount,Status,Paid At,Payment Method\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Bill bill : bills) {
            csv.append(bill.getInvoiceNumber()).append(",")
               .append(escapeCsvField(bill.getPatient() != null ? bill.getPatient().getName() : "N/A")).append(",")
               .append(bill.getAmount()).append(",")
               .append(bill.getDiscount()).append(",")
               .append(bill.getTax()).append(",")
               .append(bill.getTotalAmount()).append(",")
               .append(bill.getStatus().name()).append(",")
               .append(bill.getPaidAt() != null ? bill.getPaidAt().format(formatter) : "N/A").append(",")
               .append(escapeCsvField(bill.getPaymentMethod() != null ? bill.getPaymentMethod() : "N/A")).append("\n");
        }
        return csv.toString();
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}

/*
 * This file contains the ExportService class which manages abstract contracts for clinical operations and decoupling controllers from repository databases.
 * Concepts used: Java interfaces, abstraction boundaries, and enterprise service patterns.
 */
