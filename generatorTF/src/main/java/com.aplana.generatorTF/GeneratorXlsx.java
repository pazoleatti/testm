package com.aplana.generatorTF;

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

import static com.aplana.generatorTF.Dictionary.*;
import static com.aplana.generatorTF.Main.printStream;
import static com.aplana.generatorTF.Utils.*;

/**
 * Класс для генерации ТФ (Excel) РНУ
 */
public class GeneratorXlsx {

    private Random random = new Random(System.currentTimeMillis());

    private DataFormatter formatter = new DataFormatter();

    void generateXlsx(File sourceFile, int personCount) throws Exception {
        XSSFWorkbook sourceWorkbook;
        SXSSFWorkbook workBook = null;
        XSSFSheet sourceSheet;
        File outFile = new File(sourceFile.getAbsoluteFile().getParent() + "\\out.xlsx");
        try (OutputStream out = new FileOutputStream(outFile)) {
            sourceWorkbook = new XSSFWorkbook(new FileInputStream(sourceFile));
            workBook = new SXSSFWorkbook(sourceWorkbook, 100);
            sourceSheet = sourceWorkbook.getSheetAt(0);
            for (int i = 0; i <= sourceSheet.getLastRowNum(); i++) {
                if (isRowEmpty(sourceSheet.getRow(i))) {
                    sourceSheet.removeRow(sourceSheet.getRow(i));
                }
            }

            SXSSFSheet sheet = (SXSSFSheet) workBook.getSheetAt(0);

            List<Integer> personsPositions = new ArrayList<>();
            Row lastRow = null;
            for (int i = 2; i < sourceSheet.getLastRowNum(); i++) {
                if (lastRow == null || !isRowsEqualByFL(lastRow, sourceSheet.getRow(i))) {
                    personsPositions.add(i);
                }
                lastRow = sourceSheet.getRow(i);
            }

            int personNum = 1;
            int rowNum = sourceSheet.getLastRowNum();
            for (int i = 0; i < personCount; i++) {
                int personIndex = random.nextInt(personsPositions.size());
                int personRowIndex = personsPositions.get(personIndex);
                // копируем физика со всеми операциями
                {
                    int startIndex = Math.max(sheet.getLastRowNum() + 1, sourceSheet.getLastRowNum() + 1);
                    Row row = sheet.createRow(startIndex);
                    copyRow(sourceSheet.getRow(personRowIndex), row, rowNum);
                    mutateRow(row, sourceSheet.getRow(personRowIndex));
                    personRowIndex++;

                    while (personIndex == personsPositions.size() - 1 && personRowIndex <= sourceSheet.getLastRowNum() ||
                            personIndex < personsPositions.size() - 1 && personRowIndex < personsPositions.get(personIndex + 1)) {
                        row = sheet.createRow(Math.max(sheet.getLastRowNum() + 1, sourceSheet.getLastRowNum() + 1));
                        copyRow(sourceSheet.getRow(personRowIndex), row, rowNum);
                        copyRow(sheet.getRow(startIndex), row, Range.closed(1, 22), rowNum);
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
            workBook.write(out);
            printStream.println(String.format("Данные сохранена в файл %s", outFile.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workBook != null) {
                deleteSXSSFTempFiles(workBook);
            }
        }
    }

    private void mutateRow(Row row, Row rowStyle) {
        //генерация ИНП
        String inp = String.valueOf(1000000000 + random.nextInt(2000000000));
        row.createCell(1).setCellValue(inp.length() == 10 ? inp : inp.substring(inp.length() - 10, inp.length()));
        row.getCell(1).setCellStyle(rowStyle.getCell(1).getCellStyle());

        //генерация СНИЛС
        row.createCell(21).setCellValue(generateSnils(random));
        row.getCell(21).setCellStyle(rowStyle.getCell(21).getCellStyle());

        //генерация ФИО
        row.createCell(2).setCellValue(lastnameDictionary.get(random.nextInt(lastnameDictionary.size())));
        row.getCell(2).setCellStyle(rowStyle.getCell(2).getCellStyle());
        row.createCell(3).setCellValue(firstnameDictionary.get(random.nextInt(firstnameDictionary.size())));
        row.getCell(3).setCellStyle(rowStyle.getCell(3).getCellStyle());
        row.createCell(4).setCellValue(middlenameDictionary.get(random.nextInt(middlenameDictionary.size())));
        row.getCell(4).setCellStyle(rowStyle.getCell(4).getCellStyle());

        //генерация номера уд.лич.
        if (formatter.formatCellValue(row.getCell(9)).equals("21")) {
            row.createCell(10).setCellValue(generateNumberDul(random));
            row.getCell(10).setCellStyle(rowStyle.getCell(10).getCellStyle());
        }

        // генерация даты рождения
        row.createCell(5).setCellValue(generateDate(random));
        row.getCell(5).setCellStyle(rowStyle.getCell(5).getCellStyle());

        // генерация ИНН
        row.createCell(7).setCellValue(generateInn(random));
        row.getCell(7).setCellStyle(rowStyle.getCell(7).getCellStyle());
    }

    private boolean isRowsEqualByFL(Row row1, Row row2) {
        if (row1.getFirstCellNum() != row2.getFirstCellNum() || row1.getLastCellNum() != row2.getLastCellNum()) {
            return false;
        }
        for (int i = 1; i < 22; i++) {
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
        int fromIndex = range == null ? 1 : range.lowerEndpoint();
        int toIndex = range == null ? fromRow.getLastCellNum() : range.upperEndpoint();
        for (int i = fromIndex; i < toIndex; i++) {
            Cell cell = fromRow.getCell(i);
            Cell newCell;
            if (cell == null) {
                continue;
            }
            newCell = toRow.createCell(cell.getColumnIndex());
            newCell.setCellStyle(cell.getCellStyle());
            if (i == 0) {
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
