package com.aplana.generators;

import com.google.common.collect.Range;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.streaming.SheetDataWriter;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.aplana.generators.Dictionary.*;
import static com.aplana.generators.Main.printStream;
import static com.aplana.generators.Utils.*;

/**
 * Класс для генерации ТФ (Excel) РНУ
 */
public class GeneratorRnuNdflXlsx {

    //Номера столбцов в таблице
    private static final int COL_N = 1;

    private static final int COL_INP = 2;

    private static final int COL_SURNAME = 3;

    private static final int COL_NAME = 4;

    private static final int COL_NAME_2 = 5;

    private static final int COL_BIRTHDAY = 6;

    private static final int COL_INN_RF = 8;

    private static final int COL_DUL_CODE = 10;

    private static final int COL_DUL_N = 11;

    private static final int COL_SNILS = 22;

    private static final int COL_OPER_ID = 23;

    private static final int SHEET_DATA = 1;

    private static final int ROW_START = 2;

    private Random random = new Random(System.currentTimeMillis());

    private DataFormatter formatter = new DataFormatter();

    void generateXlsx(File sourceFile, int personCount) throws Exception {
        XSSFWorkbook sourceWorkbook;
        SXSSFWorkbook workBook = null;
        XSSFSheet sourceSheet;
        XSSFSheet templateSheet;
        File outFile = new File(sourceFile.getAbsoluteFile().getParent() + "\\out.xlsx");

        try (OutputStream out = new FileOutputStream(outFile)) {
            sourceWorkbook = new XSSFWorkbook(new FileInputStream(sourceFile));
            workBook = new SXSSFWorkbook(sourceWorkbook, 1000);
            sourceSheet = sourceWorkbook.cloneSheet(SHEET_DATA);
            templateSheet = sourceWorkbook.getSheetAt(SHEET_DATA);

            // Идем по строкам с конца, потому что сначала нужно удалить пустые строки с обоих страниц,
            // а потом удалить не пустые строки с шаблонной страницы, так как один итератор на 2 страницы.
            for (int i = sourceSheet.getLastRowNum(); i >= 0; i--) {
                if (i >= ROW_START && !isRowEmpty(sourceSheet.getRow(i))){
                    templateSheet.removeRow(templateSheet.getRow(i));
                }
                if (isRowEmpty(sourceSheet.getRow(i))) {
                    sourceSheet.removeRow(sourceSheet.getRow(i));
                    templateSheet.removeRow(templateSheet.getRow(i));
                }
            }

            SXSSFSheet sheet = (SXSSFSheet) workBook.getSheetAt(SHEET_DATA);

            List<Integer> personsPositions = new ArrayList<>();
            Row lastRow = null;
            for (int i = ROW_START; i < sourceSheet.getLastRowNum(); i++) {
                if (lastRow == null || !isRowsEqualByFL(lastRow, sourceSheet.getRow(i))) {
                    personsPositions.add(i);
                }
                lastRow = sourceSheet.getRow(i);
            }

            int personNum = 1;
            int rowNum = templateSheet.getLastRowNum();
            for (int i = 0; i < personCount; i++) {
                int personIndex = random.nextInt(personsPositions.size());
                int personRowIndex = personsPositions.get(personIndex);
                // копируем физика со всеми операциями
                {
                    int startIndex = Math.max(sheet.getLastRowNum() + 1, templateSheet.getLastRowNum() + 1);
                    Row row = sheet.createRow(startIndex);
                    copyRow(sourceSheet.getRow(personRowIndex), row, rowNum++);
                    mutateRow(row, sourceSheet.getRow(personRowIndex));
                    personRowIndex++;

                    while (personIndex == personsPositions.size() - 1 && personRowIndex <= sourceSheet.getLastRowNum() ||
                            personIndex < personsPositions.size() - 1 && personRowIndex < personsPositions.get(personIndex + 1)) {
                        row = sheet.createRow(Math.max(sheet.getLastRowNum() + 1, templateSheet.getLastRowNum() + 1));
                        copyRow(sourceSheet.getRow(personRowIndex), row, rowNum);
                        copyRow(sheet.getRow(startIndex), row, Range.closed(COL_N, COL_OPER_ID), rowNum);
                        personRowIndex++;
                        rowNum++;
                    }
                }
                if (personNum % 1000 == 0) {
                    printStream.println(String.format("%s физиков сгенерировано", personNum));
                }
                personNum++;
            }

            printStream.println("Идет сохранение...");
            workBook.removeSheetAt(SHEET_DATA + 1);
            workBook.write(out);
            printStream.println(String.format("Данные сохранены в файл %s", outFile.getAbsolutePath()));
        } finally {
            if (workBook != null) {
                deleteSXSSFTempFiles(workBook);
            }
        }
    }

    private void mutateRow(Row row, Row rowStyle) {
        //генерация ИНП
        String inp = String.valueOf(1000000000 + random.nextInt(2000000000));
        row.createCell(COL_INP).setCellValue(inp.length() == 10 ? inp : inp.substring(inp.length() - 10, inp.length()));
        row.getCell(COL_INP).setCellStyle(rowStyle.getCell(COL_INP).getCellStyle());

        //генерация СНИЛС
        row.createCell(COL_SNILS).setCellValue(generateSnils(random));
        row.getCell(COL_SNILS).setCellStyle(rowStyle.getCell(COL_SNILS).getCellStyle());

        //генерация ФИО
        row.createCell(COL_SURNAME).setCellValue(lastnameDictionary.get(random.nextInt(lastnameDictionary.size())));
        row.getCell(COL_SURNAME).setCellStyle(rowStyle.getCell(COL_SURNAME).getCellStyle());
        row.createCell(COL_NAME).setCellValue(firstnameDictionary.get(random.nextInt(firstnameDictionary.size())));
        row.getCell(COL_NAME).setCellStyle(rowStyle.getCell(COL_NAME).getCellStyle());
        row.createCell(COL_NAME_2).setCellValue(middlenameDictionary.get(random.nextInt(middlenameDictionary.size())));
        row.getCell(COL_NAME_2).setCellStyle(rowStyle.getCell(COL_NAME_2).getCellStyle());

        //генерация номера уд.лич.
        if (formatter.formatCellValue(row.getCell(COL_DUL_CODE)).equals("21")) {
            row.createCell(COL_DUL_N).setCellValue(generateNumberDul(random));
            row.getCell(COL_DUL_N).setCellStyle(rowStyle.getCell(COL_DUL_N).getCellStyle());
        }

        // генерация даты рождения
        row.createCell(COL_BIRTHDAY).setCellValue(generateDate(random));
        row.getCell(COL_BIRTHDAY).setCellStyle(rowStyle.getCell(COL_BIRTHDAY).getCellStyle());

        // генерация ИНН
        row.createCell(COL_INN_RF).setCellValue(generateInn(random));
        row.getCell(COL_INN_RF).setCellStyle(rowStyle.getCell(COL_INN_RF).getCellStyle());
    }

    private boolean isRowsEqualByFL(Row row1, Row row2) {
        if (row1.getFirstCellNum() != row2.getFirstCellNum() || row1.getLastCellNum() != row2.getLastCellNum()) {
            return false;
        }
        for (int i = COL_INP; i < COL_OPER_ID; i++) {
            Cell cell = row1.getCell(i);
            if (!Objects.equals(formatter.formatCellValue(cell), formatter.formatCellValue(row2.getCell(cell.getColumnIndex())))) {
                return false;
            }
        }
        return true;
    }

    private boolean isRowEmpty(XSSFRow row) {
        for (int i = 0; i <= row.getLastCellNum(); i++) {
            if (row.getCell(i) != null && row.getCell(i).getRawValue() != null) {
                return false;
            }
        }
        return true;
    }

    private void copyRow(Row fromRow, Row toRow, int rowNum) {
        copyRow(fromRow, toRow, null, rowNum);
    }

    private void copyRow(Row fromRow, Row toRow, Range<Integer> range, int rowNum) {
        int fromIndex = range == null ? 0 : range.lowerEndpoint();
        int toIndex = range == null ? fromRow.getLastCellNum() : range.upperEndpoint();
        for (int i = fromIndex; i < toIndex; i++) {
            Cell cell = fromRow.getCell(i);
            Cell newCell;
            if (cell == null) {
                continue;
            }
            newCell = toRow.createCell(cell.getColumnIndex());
            newCell.setCellStyle(cell.getCellStyle());

            if (i < COL_N) {
                newCell.setCellValue("");
            } else if (i == COL_N) {
                newCell.setCellValue(rowNum);
            } else {
                newCell.setCellType(cell.getCellType());
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_BLANK:
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        newCell.setCellValue(cell.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        newCell.setCellValue(cell.getRichStringCellValue());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void deleteSXSSFTempFiles(SXSSFWorkbook workbook) throws NoSuchFieldException, IllegalAccessException {

        int numberOfSheets = workbook.getNumberOfSheets();

        //iterate through all sheets (each sheet as a temp file)
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheetAt = workbook.getSheetAt(i);

            //delete only if the sheet is written by stream
            if (sheetAt instanceof SXSSFSheet) {
                SheetDataWriter sdw = (SheetDataWriter) getPrivateAttribute(sheetAt, "_writer");
                File f = (File) getPrivateAttribute(sdw, "_fd");

                if (!f.delete()) {
                    printStream.println(String.format("Временнный файл %s не был удален.", f.getName()));
                }
            }
        }
    }

    private Object getPrivateAttribute(Object containingClass, String fieldToGet) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = containingClass.getClass().getDeclaredField(fieldToGet);
        declaredField.setAccessible(true);
        return declaredField.get(containingClass);
    }
}
