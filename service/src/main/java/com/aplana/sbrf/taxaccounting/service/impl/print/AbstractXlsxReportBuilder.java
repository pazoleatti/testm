package com.aplana.sbrf.taxaccounting.service.impl.print;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: avanteev
 * Date: 20.05.13
 */
public abstract class AbstractXlsxReportBuilder {

    protected Workbook workBook;

    protected Sheet sheet;

    protected static final int cellWidthMin = 30;
    protected static final int cellWidthMax = 100;


    protected Map<Integer, Integer> widthCellsMap = new HashMap<Integer, Integer>();

    /*
     * Нужно создать в классе наследнике блок static, для определения в нем имени файла
     */
    protected static String fileName;

    //Порядок формирования заголовка и шапки таблицы в такой последовательности не случайно,
    //а по причине наличия нулевых столбцов в налоговых отчетах, чтобы потом некоторые значения случайно не пропали.
    public final String createReport() throws IOException {
        createTableHeaders();
        createDataForTable();
        cellAlignment();
        fillHeader();
        fillFooter();
        setPrintSetup();
        return flush();
    }

    protected void cellAlignment() {
        for (Map.Entry<Integer, Integer> width : widthCellsMap.entrySet()) {
            //logger.debug("----n" + width.getKey() + ":" + width.getValue());
            sheet.setColumnWidth(width.getKey(), width.getValue() *256);
        }
    }

    protected abstract void createTableHeaders();

    protected abstract void fillHeader();

    protected abstract void createDataForTable();

    protected abstract void fillFooter();

    /**
     * Выставляем область печати. Она может маштабироваться самим Excel, в зависимости от ширины области печати.
     */
    protected void setPrintSetup(){

    }

    private String flush() throws IOException {
        File file = File.createTempFile(fileName, ".xlsx");
        OutputStream out = new FileOutputStream(file);
        workBook.write(out);

        return file.getAbsolutePath();
    }

    /**
     * Необходимо чтобы знать какой конечный размер ячеек установить. Делается только в самом конце.
     * @param cellNumber
     * @param length
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
