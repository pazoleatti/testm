package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.Diff;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.List;

/**
 * Сервис сравнения строк
 *
 * @author Levykin
 */
public interface DiffService {
    /**
     * Вычисление изменений
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
}
