package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.Diff;
import com.aplana.sbrf.taxaccounting.model.DiffType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DiffService;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Levykin
 */
@Service
public class DiffServiceImpl implements DiffService {
    @Override
    public List<Diff> computeDiff(List<String> original, List<String> revised) {
        // Патч по алгоритму Myers' Diff
        Patch<String> patch = DiffUtils.diff(original, revised);
        List<Diff> diffList = new LinkedList<Diff>();
        // По списку изменений (изменения блоками, нужно разделить на строки)
        for (Delta<String> delta : patch.getDeltas()) {
            // Номера строк
            int originalStartIndex = delta.getOriginal().getPosition();
            int revisedStartIndex = delta.getRevised().getPosition();
            // Размеры блоков (в количестве строк)
            int originalSize = delta.getOriginal().getLines().size();
            int revisedSize = delta.getRevised().getLines().size();
            // Максимальный размер (в количестве строк)
            int maxIndex = Math.max(originalSize, revisedSize);
            // По строкам блоков
            for (int i = 0; i < maxIndex; i++) {
                // TODO Левыкин: алгоритм можно улучшить, применив нечеткое сравнение строк, но скажется на производительности
                // Если патч сообщает о изменении, а количество строк не совпадает, то первые соответствующие строки
                // считаем измененными, а остальные добавленными или удаленными
                if (i < originalSize && i < revisedSize) {
                    diffList.add(new Diff(originalStartIndex + i, revisedStartIndex + i, DiffType.CHANGE));
                } else if (i >= originalSize) {
                    diffList.add(new Diff(null, revisedStartIndex + i, DiffType.INSERT));
                } else if (i >= revisedSize) {
                    diffList.add(new Diff(originalStartIndex + i, null, DiffType.DELETE));
                }
            }
        }
        return diffList;
    }

    @Override
    public List<Pair<Integer, Integer>> getMergedOrder(List<Diff> diffList, int maxRowCount) {
        if (maxRowCount < 1) {
            return new ArrayList<Pair<Integer, Integer>>(0);
        }
        // Объединенный список
        List<Pair<Integer, Integer>> retVal = new LinkedList<Pair<Integer, Integer>>();
        Integer x = 0;
        Integer y = 0;
        for (Diff diff : diffList) {
            Integer o = diff.getOriginalRowNumber();
            Integer r = diff.getRevisedRowNumber();
            // Неизмененные строки
            while (!x.equals(o) && !y.equals(r)) {
                retVal.add(new Pair(x++, y++));
            }
            if (o != null) {
                x++;
            }
            if (r != null) {
                y++;
            }
            retVal.add(new Pair(o, r));
        }
        // Неизмененные в конце
        while (Math.max(x, y) < maxRowCount) {
            retVal.add(new Pair(x++, y++));
        }
        return retVal;
    }

    @Override
    public List<DataRow<Cell>> getDiff(List<DataRow<Cell>> original, List<DataRow<Cell>> revised) {
        // Перевод в списки строк
        List<String> originalList = new ArrayList<String>(original.size());
        List<String> revisedList = new ArrayList<String>(revised.size());

        for (DataRow<Cell> dataRow : original) {
            originalList.add(getRowAsString(dataRow));
        }

        for (DataRow<Cell> dataRow : revised) {
            revisedList.add(getRowAsString(dataRow));
        }
        // Список изменений
        List<Diff> diffList = computeDiff(originalList, revisedList);

        Map<Integer, DiffType> originalDiffMap = new HashMap<Integer, DiffType>();
        Map<Integer, DiffType> revisedDiffMap = new HashMap<Integer, DiffType>();
        for (Diff diff : diffList) {
            if (diff.getOriginalRowNumber() != null) {
                originalDiffMap.put(diff.getOriginalRowNumber(), diff.getDiffType());
            }
            if (diff.getRevisedRowNumber() != null) {
                revisedDiffMap.put(diff.getRevisedRowNumber(), diff.getDiffType());
            }
        }

        // Пары строк для подстановок
        List<Pair<Integer, Integer>> pairList = getMergedOrder(diffList, Math.max(original.size(), revisedList.size()));
        List<DataRow<Cell>> retVal = new ArrayList<DataRow<Cell>>(pairList.size());
        for (Pair<Integer, Integer> pair : pairList) {
            DataRow<Cell> originalRow = null;
            DataRow<Cell> revisedRow = null;
            if (pair.getFirst() != null) {
                originalRow = original.get(pair.getFirst());
            }
            if (pair.getSecond() != null) {
                revisedRow = revised.get(pair.getSecond());
            }
            DataRow<Cell> dataRow = pair.getSecond() == null ? originalRow : revisedRow;
            DiffType diffType = pair.getSecond() == null ? originalDiffMap.get(pair.getFirst()) :
                    revisedDiffMap.get(pair.getSecond());

            diffStyles(diffType, pair, dataRow, originalRow, revisedRow);
            retVal.add(dataRow);
        }

        return retVal;
    }

    // Стили для отображения изменений
    private void diffStyles(DiffType diffType, Pair<Integer, Integer> pair, DataRow<Cell> dataRow,
                            DataRow<Cell> originalRow, DataRow<Cell> revisedRow) {
        // TODO Левыкин: Очистить стили
        if (diffType == null) {
            // Строка не изменилась
            return;
        }
        // TODO Левыкин: подсветка изменений по стилям и вычисление изменений
        for (String key : dataRow.keySet()) {
            Cell cell = dataRow.getCell(key);
            cell.setStyleAlias("Корректировка");
        }
    }

    @Override
    public String getRowAsString(DataRow<Cell> dataRow) {
        StringBuilder builder = new StringBuilder();
        for (String key : dataRow.keySet()) {
            Cell cell = dataRow.getCell(key);
            builder.append((cell.getValue() == null ? "" : cell.getValue()) + ";");
        }
        return builder.toString();
    }
}
