package com.aplana.sbrf.taxaccounting.service.impl.print.transportMessages;

import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessage;
import com.aplana.sbrf.taxaccounting.model.messaging.TransportMessageContentType;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * <b>класс для формирования Excel-файла с транспортными сообщениями</b>
 * Created by <i><b>s.molokovskikh</i></b> on 25.09.19.
 */
public class ExcelTransportMessagesReportBuilder extends AbstractReportBuilder {

    // Шапка таблицы
    private static final String[] headers = new String[]{
            "Номер сообщения",
            "Дата и время сообщения",
            "Система-источник",
            "Система-получатель",
            "Пользователь",
            "Статус сообщения",
            "Тип сообщения",
            "Содержимое сообщения",
            "Номер формы",
            "Вид формы",
            "Подразделение",
            "Идентификатор сообщения",
            "Сообщение",
            "Файл вложение"
    };

    private static final String SHEET_NAME = "Транспортные сообщения";
    private static final String FONT_NAME = "Calibri";
    private static final int FONT_11_SIZE = 11;
    private static final Color GRAY_COLOR = new Color(191, 191, 191, 255);
    private static final FastDateFormat DATE_FORMATTER = FastDateFormat.getInstance("dd.MM.yyyy HH:mm");

    private final List<TransportMessage> transportMessages;
    private final String headerDescription;
    private int rowNumber = 0;
    private int rowTableHeadersNumber = 3;

    public ExcelTransportMessagesReportBuilder(List<TransportMessage> transportMessages, String headerDescription) {
        this.transportMessages = transportMessages;
        this.headerDescription = headerDescription;

        workBook = new XSSFWorkbook();
        workBook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);

        sheet = workBook.createSheet(SHEET_NAME);
        sheet.setRowSumsBelow(false);
    }


    @Override
    protected void fillHeader() {
        CellStyle cs = createFillHeaderCellStyle();
        sheet.createRow(rowNumber++);

        Row row = sheet.createRow(rowNumber++);
        Cell cell = row.createCell(0);
        cell.setCellStyle(cs);
        cell.setCellValue(headerDescription);

        sheet.createRow(rowNumber++);
    }

    @Override
    protected void createTableHeaders() {
        CellStyle cs = createTableHeadersCellStyle();
        Row row = sheet.createRow(rowNumber);
        rowTableHeadersNumber = rowNumber;
        rowNumber++;
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(cs);
            cell.setCellValue(headers[i]);
        }
    }

    @Override
    protected void createDataForTable() {
        CellStyle cs = createDataForTableCellStyle();

        for (TransportMessage transportMessage : this.transportMessages) {
            Row row = sheet.createRow(rowNumber++);
            int cellNumber = 0;

            Cell cellId = row.createCell(cellNumber++);
            cellId.setCellStyle(cs);
            cellId.setCellValue(transportMessage.getId());


            Cell cellDate = row.createCell(cellNumber++);
            cellDate.setCellStyle(cs);
            cellDate.setCellValue(DATE_FORMATTER.format(transportMessage.getDateTime().toDate()));

            Cell cellSource = row.createCell(cellNumber++);
            cellSource.setCellStyle(cs);
            if (transportMessage.getSenderSubsystem() != null) {
                cellSource.setCellValue("(" + transportMessage.getSenderSubsystem().getId() + ") " +
                        transportMessage.getSenderSubsystem().getName());
            }

            Cell cellDest = row.createCell(cellNumber++);
            cellDest.setCellStyle(cs);
            if (transportMessage.getReceiverSubsystem() != null) {
                cellDest.setCellValue("(" + transportMessage.getReceiverSubsystem().getId() + ") " +
                        transportMessage.getReceiverSubsystem().getName());
            }

            Cell cellUser = row.createCell(cellNumber++);
            cellUser.setCellStyle(cs);
            if (transportMessage.getInitiatorUser() != null) {
                cellUser.setCellValue(transportMessage.getInitiatorUser().getName() + " " +
                        transportMessage.getInitiatorUser().getLogin());
            }

            Cell cellStatus = row.createCell(cellNumber++);
            cellStatus.setCellStyle(cs);
            if (transportMessage.getState() != null) {
                cellStatus.setCellValue(transportMessage.getState().getText());
            }

            Cell cellType = row.createCell(cellNumber++);
            cellType.setCellStyle(cs);
            if (transportMessage.getType() != null) {
                cellType.setCellValue(transportMessage.getType().getText());
            }

            Cell cellContent = row.createCell(cellNumber++);
            cellContent.setCellStyle(cs);

            cellContent.setCellValue(getDescriptionByContentType(transportMessage.getContentType()));


            Cell cellFormNumber = row.createCell(cellNumber++);
            cellFormNumber.setCellStyle(cs);
            if (transportMessage.getDeclaration() != null && transportMessage.getDeclaration().getId() != null) {
                cellFormNumber.setCellValue(transportMessage.getDeclaration().getId());
            }

            Cell cellFormType = row.createCell(cellNumber++);
            cellFormType.setCellStyle(cs);
            if (transportMessage.getDeclaration() != null) {
                cellFormType.setCellValue(transportMessage.getDeclaration().getTypeName());
            }

            Cell cellFormDepartment = row.createCell(cellNumber++);
            cellFormDepartment.setCellStyle(cs);
            if (transportMessage.getDeclaration() != null) {
                cellFormDepartment.setCellValue(transportMessage.getDeclaration().getDepartmentName());
            }

            Cell cellUuid = row.createCell(cellNumber++);
            cellUuid.setCellStyle(cs);
            cellUuid.setCellValue(transportMessage.getMessageUuid());

            Cell cellAttachment = row.createCell(cellNumber);
            cellAttachment.setCellStyle(cs);
            cellAttachment.setCellValue(transportMessage.getBodyFileName());

            Cell cellFilename = row.createCell(cellNumber++);
            cellFilename.setCellStyle(cs);
            if (transportMessage.getBlob() != null) {
                cellFilename.setCellValue(transportMessage.getBlob().getName());
            }

        }
    }

    private String getDescriptionByContentType(TransportMessageContentType contentType) {
        switch (contentType) {
            case RECEIPT_DOCUMENT:
                return "Квитанция о приёме";
            case REJECTION_NOTICE:
                return "Уведомление об отказе";

            case CORRECTION_NOTICE:
                return "Уведомление об уточнении";
            case ENTRY_NOTICE:
                return "Извещение о вводе";
            case RECEIVED_DOCUMENTS_REGISTRY:
                return "Реестр принятых документов";
            case NDFL2_ACCEPTANCE_PROTOCOL:
                return "Протокол приёма 2-НДФЛ";
            case ERROR_MESSAGE:
                return "Сообщение об ошибке";
            case TECH_RECEIPT:
                return "Технологическая квитанция";
            case NDFL6:
                return "6-НДФЛ";
            case NDFL2_1:
                return "2-НДФЛ (1)";
            case NDFL2_2:
                return "2-НДФЛ (2)";
            case UNKNOWN:
            default:
                return "Неизвестно";
        }
    }

    /**
     * Создание шрифта для заголовка отчета
     * @return
     */
    private Font createFillHeaderFont() {
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) FONT_11_SIZE);
        return font;
    }

    /**
     * Создание ситиля для заголовка отчета
     * @return
     */
    private CellStyle createFillHeaderCellStyle() {
        CellStyle cs = workBook.createCellStyle();
        cs.setFont(createFillHeaderFont());
        cs.setAlignment(CellStyle.ALIGN_LEFT);
        cs.setBorderBottom(CellStyle.BORDER_NONE);
        cs.setBorderTop(CellStyle.BORDER_NONE);
        cs.setBorderRight(CellStyle.BORDER_NONE);
        cs.setBorderLeft(CellStyle.BORDER_NONE);
        cs.setWrapText(false);
        return cs;
    }

    /**
     * Создание шрифта для шапки таблицы
     * @return
     */
    private Font createTableHeadersFont() {
        Font font = workBook.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) FONT_11_SIZE);
        return font;
    }

    /**
     * Создание стиля для шапки таблицы
     * @return
     */
    private CellStyle createTableHeadersCellStyle() {
        XSSFCellStyle cs = (XSSFCellStyle) workBook.createCellStyle();
        cs.setFont(createTableHeadersFont());
        cs.setAlignment(CellStyle.ALIGN_CENTER);
        cs.setBorderBottom(CellStyle.BORDER_THIN);
        cs.setBorderTop(CellStyle.BORDER_THIN);
        cs.setBorderRight(CellStyle.BORDER_THIN);
        cs.setBorderLeft(CellStyle.BORDER_THIN);
        cs.setFillBackgroundColor(new XSSFColor(GRAY_COLOR));
        cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cs.setWrapText(false);
        return cs;
    }

    /**
     * Создание шрифта для данныз таблицы
     * @return
     */
    private Font createDataForTableFont() {
        Font font = workBook.createFont();
        font.setFontName(FONT_NAME);
        font.setFontHeightInPoints((short) FONT_11_SIZE);
        return font;
    }

    /**
     * Создание стиля для данных таблицы
     * @return
     */
    private CellStyle createDataForTableCellStyle() {
        XSSFCellStyle cs = (XSSFCellStyle) workBook.createCellStyle();
        cs.setFont(createDataForTableFont());
        cs.setAlignment(CellStyle.ALIGN_LEFT);
        cs.setBorderBottom(CellStyle.BORDER_THIN);
        cs.setBorderTop(CellStyle.BORDER_THIN);
        cs.setBorderRight(CellStyle.BORDER_THIN);
        cs.setBorderLeft(CellStyle.BORDER_THIN);
        cs.setFillPattern(CellStyle.NO_FILL);
        cs.setWrapText(false);
        return cs;
    }

    @Override
    protected void cellAlignment() {
        Iterator<Cell> cellIterator = sheet.getRow(rowTableHeadersNumber).cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            int columnIndex = cell.getColumnIndex();
            sheet.autoSizeColumn(columnIndex);
        }
    }

    /***
     * Сформировать Excel-файл с транспортными сообщениями
     * @return InputStream, который необходимо закрыть из вызяваемого кода используя метод InputStream.close()
     * @throws IOException
     */
    public InputStream createReportAsStream() throws IOException {

        //Заполнение заголовка
        fillHeader();
        //Заполнение запки таблицы
        createTableHeaders();
        //Заполнение таблицы
        createDataForTable();
        //Задать ширину ячеек
        cellAlignment();
        //Подвал отчета
        fillFooter();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workBook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } finally {
            outputStream.close();
        }

    }
}
