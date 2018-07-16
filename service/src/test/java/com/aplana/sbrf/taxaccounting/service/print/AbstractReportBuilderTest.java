package com.aplana.sbrf.taxaccounting.service.print;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Objects.firstNonNull;
import static java.util.Arrays.asList;

public class AbstractReportBuilderTest {

    protected List<List<String>> toList(String[][] rows) {
        List<List<String>> result = new ArrayList<>();
        for (String[] row : rows) {
            result.add(Arrays.asList(row));
        }
        return result;
    }

    protected List<List<String>> readExcelFile(String filePath) throws Exception {
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = wb.getSheetAt(0);
            List<List<String>> rows = new ArrayList<>();
            for (int rowNum = 0; rowNum <= sheet.getPhysicalNumberOfRows(); rowNum++) {
                List<String> row = new ArrayList<>();
                rows.add(row);

                XSSFRow xssfRow = sheet.getRow(rowNum);
                if (xssfRow != null) {
                    for (int cellNum = 0; cellNum < xssfRow.getLastCellNum(); cellNum++) {
                        String cell = "";

                        XSSFCell xssfCell = xssfRow.getCell(cellNum);
                        if (xssfCell != null) {
                            cell = xssfCell.toString();
                        }
                        row.add(cell);
                    }
                }
            }
            return rows;
        }
    }

    protected List<List<String>> readCsvFile(String filePath, String encoding) throws Exception {
        List<List<String>> result = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream, firstNonNull(encoding, Charset.defaultCharset().name())));
            for (String[] row = reader.readNext(); row != null; row = reader.readNext()) {
                result.add(asList(row[0].trim().split("\"?;\"?")));
            }
        }
        return result;
    }
}
