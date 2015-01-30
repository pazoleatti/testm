package com.aplana.sbrf.taxaccounting.service.impl.print;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: avanteev
 * Date: 20.05.13
 */
public abstract class AbstractReportBuilder {

    protected final Log logger = LogFactory.getLog(getClass());

    protected Workbook workBook;

    protected Sheet sheet;

    protected static final int cellWidthMin = 30;
    protected static final int cellWidthMax = 100;

    protected Map<Integer, Integer> widthCellsMap = new HashMap<Integer, Integer>();

    private String fileName;
    private String postfix;

    protected AbstractReportBuilder(String fileName, String postfix) {
        this.fileName = fileName;
        this.postfix = postfix;
    }

    /**
     * Формирование отчета. Условно разбит на шесть частей.
     * Порядок формирования заголовка и шапки таблицы в такой последовательности не случайно,
     * а по причине наличия нулевых столбцов в налоговых отчетах, чтобы потом некоторые значения не пропали.
     * @return массив byte[]
     * @throws IOException
     */
    public final byte[] createBlobData() throws IOException  {
        fillHeader();
        createTableHeaders();
        createDataForTable();
        cellAlignment();
        fillFooter();
        setPrintSetup();
        return flushBlobData();
    }

    protected void cellAlignment() {
        for (Map.Entry<Integer, Integer> width : widthCellsMap.entrySet()) {
            sheet.setColumnWidth(width.getKey(), width.getValue() *256 *2);
        }
    }

    /**
     * Создание шапки таблицы.
     */
    protected abstract void createTableHeaders();

    /**
     * Заполнение шапки отчета.
     */
    protected abstract void fillHeader();

    /**
     * Заполнение таблицы данными.
     */
    protected abstract void createDataForTable();

    /**
     * Заполнение подвала отчета.
     */
    protected abstract void fillFooter();

    /**
     * Выставление области печати для отчета.
     * Она может масштабироваться самим Excel, в зависимости от ширины области печати.
     */
    protected void setPrintSetup(){
        //Nothing
    }

    protected byte[] flushBlobData() throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        workBook.write(data);

        return data.toByteArray();
    }

    /**
     * Необходимо, чтобы знать какой конечный размер ячеек установить. Делается только в самом конце.
     * @param cellNumber номер ячейки
     * @param length ширина
     */
    protected final void fillWidth(Integer cellNumber,Integer length){

        if(widthCellsMap.get(cellNumber) == null && length >= cellWidthMin && length <= cellWidthMax)
            widthCellsMap.put(cellNumber, length);
        else if(widthCellsMap.get(cellNumber) == null && length <= cellWidthMin){
            widthCellsMap.put(cellNumber, cellWidthMin);
        }
        else if(widthCellsMap.get(cellNumber) == null && length >= cellWidthMax){
            widthCellsMap.put(cellNumber, cellWidthMax);
        }
        else if(widthCellsMap.get(cellNumber) != null){
            if (length.compareTo(cellWidthMax) < 0 && length.compareTo(cellWidthMin) > 0 &&
                    widthCellsMap.get(cellNumber).compareTo(length) < 0)
                widthCellsMap.put(cellNumber, length);
        }
        else
            widthCellsMap.put(cellNumber, cellWidthMin);
    }
}
