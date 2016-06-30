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
    String STYLE_NO_CHANGE = "Корректировка-без изменений";
    String STYLE_INSERT = "Корректировка-добавлено";
    String STYLE_DELETE = "Корректировка-удалено";
    String STYLE_CHANGE = "Корректировка-изменено";

    /**
     * Вычисление изменений для строк
     * @param original Список исходных строк
     * @param revised Список ихмененных строк
     * @return Список изменений с указанием на номера строк и характер изменений
     */
    List<Diff> computeDiff(List<String> original, List<String>revised);

    /**
     * Порядок следования строк для объединенного результата
     * @param diffList Список изменений
     * @param maxRowCount Максимальное количество строк
     */
    List<Pair<Integer, Integer>> getMergedOrder(List<Diff> diffList, int maxRowCount);

    /**
     * Вычисление изменений для строк НФ
     * @param original Строки исходной НФ
     * @param revised Строки измененной НФ
     */
    List<DataRow<Cell>> getDiff(List<DataRow<Cell>> original, List<DataRow<Cell>> revised);

    /**
     * Перевод строки НФ в текстовое представление
     */
    String getRowAsString(DataRow<Cell> dataRow);
}
