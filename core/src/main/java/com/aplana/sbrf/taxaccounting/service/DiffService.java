package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Diff;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.List;

/**
 * Сервис сравнения строк
 *
 * @author Levykin
 */
public interface DiffService {
    public final static String STYLE_NO_CHANGE = "Корректировка-без изменений";
    public final static String STYLE_INSERT = "Корректировка-добавлено";
    public final static String STYLE_DELETE = "Корректировка-удалено";
    public final static String STYLE_CHANGE = "Корректировка-изменено";

    /**
     * Вычисление изменений для строк
     * @param original Список исходных строк
     * @param revised Список ихмененных строк
     * @return Список изменений с указанием на номера строк и характер изменений
     */
    public List<Diff> computeDiff(List<String> original, List<String>revised);

    /**
     * Порядок следования строк для объединенного результата
     * @param diffList Список изменений
     * @param maxRowCount Максимальное количество строк
     */
    public List<Pair<Integer, Integer>> getMergedOrder(List<Diff> diffList, int maxRowCount);

    /**
     * Вычисление изменений для строк НФ
     * @param original Строки исходной НФ
     * @param revised Строки измененной НФ
     */
    public List<DataRow<Cell>> getDiff(List<DataRow<Cell>> original, List<DataRow<Cell>> revised);

    /**
     * Перевод строки НФ в текстовое представление
     */
    public String getRowAsString(DataRow<Cell> dataRow);
}