package com.quiniela.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class ExcelExporter {

    private Workbook workbook;
    private Sheet sheet;

    public ExcelExporter(String sheetName) {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet(sheetName);
    }

    public void writeHeader(String[] headers) {

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);

        style.setFont(font);

        for (int i = 0; i < headers.length; i++) {

            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    public void writeData(List<String[]> data) {

        int rowCount = 1;

        for (String[] rowData : data) {

            Row row = sheet.createRow(rowCount++);

            for (int i = 0; i < rowData.length; i++) {

                row.createCell(i)
                   .setCellValue(rowData[i]);
            }
        }

        for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public void export(HttpServletResponse response)
            throws IOException {

        ServletOutputStream outputStream =
                response.getOutputStream();

        workbook.write(outputStream);

        workbook.close();
        outputStream.close();
    }
}
