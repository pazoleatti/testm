package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DiffService;
import com.aplana.sbrf.taxaccounting.service.StyleService;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Levykin
 */
@Service
public class DiffServiceImpl implements DiffService {

    @Autowired
    private RefBookHelper refBookHelper;
	@Autowired
	private StyleService styleService;

    @Override
    public List<Diff> computeDiff(List<String> original, List<String> revised) {
        // Патч по алгоритму Myer's Diff
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
                if (revisedSize == 1) {
                    if ((i < originalSize && i < revisedSize - 1)) {
                        diffList.add(new Diff(originalStartIndex + i, revisedStartIndex + i, DiffType.CHANGE));
                    } else if (i >= originalSize) {
                        diffList.add(new Diff(null, revisedStartIndex + i, DiffType.INSERT));
                    } else if ((i >= revisedSize - 1)) {
                        if (i == maxIndex - 1) {
                            diffList.add(new Diff(originalStartIndex + i, revisedStartIndex, DiffType.CHANGE));
                        } else {
                            diffList.add(new Diff(originalStartIndex + i, null, DiffType.DELETE));
                        }
                    }
                } else {
                    if (i < originalSize && i < revisedSize) {
                        diffList.add(new Diff(originalStartIndex + i, revisedStartIndex + i, DiffType.CHANGE));
                    } else if (i >= originalSize) {
                        diffList.add(new Diff(null, revisedStartIndex + i, DiffType.INSERT));
                    } else if (i >= revisedSize) {
                        diffList.add(new Diff(originalStartIndex + i, null, DiffType.DELETE));
                    }
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

        // Строки почему-то не по порядку
        Comparator<DataRow<Cell>> dataRowComparator = new Comparator<DataRow<Cell>>() {
            @Override
            public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
        };
        Collections.sort(original, dataRowComparator);
        Collections.sort(revised, dataRowComparator);

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

        // Кэш разыменованных значений
        Map<Integer, Map<Long, String>> dereferenceCache = new HashMap<Integer, Map<Long, String>>();

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

            diffStyles(diffType, dataRow, originalRow, revisedRow, dereferenceCache);
            retVal.add(dataRow);
        }

        return retVal;
    }

    /**
     * Стили для отображения изменений
     */
    private void diffStyles(DiffType diffType, DataRow<Cell> dataRow, DataRow<Cell> originalRow,
                            DataRow<Cell> revisedRow, Map<Integer, Map<Long, String>> dereferenceCache) {
        // Очистка всех стилей
        rowStyle(dataRow, null);

        if (diffType == null) {
            // Строка не изменилась
            rowStyle(dataRow, styleService.get(FormStyle.NO_CHANGE_STYLE_ALIAS));
            return;
        }

        switch (diffType) {
            case INSERT:
                rowStyle(dataRow, styleService.get(FormStyle.INSERT_STYLE_ALIAS));
                return;
            case DELETE:
                rowStyle(dataRow, styleService.get(FormStyle.DELETE_STYLE_ALIAS));
                return;
            case CHANGE:
                for (String key : dataRow.keySet()) {
                    Cell originalCell = originalRow.getCell(key);
                    Cell revisedCell = revisedRow.getCell(key);
                    Cell cell = dataRow.getCell(key);
                    // Стиль
                    if (isValueChanged(originalCell.getValue(), revisedCell.getValue())) {
                        cell.setStyle(styleService.get(FormStyle.CHANGE_STYLE_ALIAS));
                    } else {
                        // Если ячейки являются зависимыми, то необходимо сравнить их родительские графы
                        if (originalCell.getColumn().getColumnType() == ColumnType.REFERENCE
                                && revisedCell.getColumn().getColumnType() == ColumnType.REFERENCE) {

                            ReferenceColumn originalReferenceColumn = (ReferenceColumn) originalCell.getColumn();
                            ReferenceColumn revisedReferenceColumn = (ReferenceColumn) revisedCell.getColumn();

                            int originalParentId = originalReferenceColumn.getParentId();
                            int revisedParentId = revisedReferenceColumn.getParentId();

                            Cell originalParentCell = getCellById(originalParentId, originalRow);
                            Cell revisedParentCell = getCellById(revisedParentId, revisedRow);

                            if (!isValueChanged(originalParentCell.getValue(), revisedParentCell.getValue())) {
                                // Если у зависимой графы не поменялось значение родительской графы, то зависимая графа
                                // тоже считается не измененной
                                cell.setStyle(styleService.get(FormStyle.NO_CHANGE_STYLE_ALIAS));
                            } else {
                                // Родительская графа изменилась, нужно сравнить разыменованные значения
                                String originalDereference = null;
                                String revisedDereference = null;

                                if (originalParentCell.getNumericValue() != null) {
                                    if (!dereferenceCache.containsKey(originalReferenceColumn.getId())) {
                                        dereferenceCache.put(originalReferenceColumn.getId(), new HashMap<Long, String>());
                                    }

                                    Map<Long, String> map =  dereferenceCache.get(originalReferenceColumn.getId());

                                    long refKey = originalParentCell.getNumericValue().longValue();
                                    if (map.containsKey(refKey)) {
                                        originalDereference = map.get(refKey);
                                    } else {
                                        refBookHelper.dataRowsDereference(new Logger(),
                                                Arrays.asList(new DataRow<Cell>(Arrays.asList(originalCell, originalParentCell))),
                                                Arrays.asList(originalCell.getColumn(), originalParentCell.getColumn()));
                                        originalDereference = originalCell.getRefBookDereference();
                                        map.put(refKey, originalDereference);
                                    }
                                }

                                if (revisedParentCell.getNumericValue() != null) {
                                    if (!dereferenceCache.containsKey(originalReferenceColumn.getId())) {
                                        dereferenceCache.put(originalReferenceColumn.getId(), new HashMap<Long, String>());
                                    }

                                    Map<Long, String> map =  dereferenceCache.get(originalReferenceColumn.getId());

                                    long refKey = revisedParentCell.getNumericValue().longValue();
                                    if (map.containsKey(refKey)) {
                                        revisedDereference = map.get(refKey);
                                    } else {
                                        refBookHelper.dataRowsDereference(new Logger(),
                                                Arrays.asList(new DataRow<Cell>(Arrays.asList(revisedCell, revisedParentCell))),
                                                Arrays.asList(revisedCell.getColumn(), revisedParentCell.getColumn()));
                                        revisedDereference = revisedCell.getRefBookDereference();
                                        map.put(refKey, revisedDereference);
                                    }
                                }
                                if (isValueChanged(originalDereference, revisedDereference)) {
                                    cell.setStyle(styleService.get(FormStyle.CHANGE_STYLE_ALIAS));
                                } else {
                                    cell.setStyle(styleService.get(FormStyle.NO_CHANGE_STYLE_ALIAS));
                                }
                            }
                        } else {
                            cell.setStyle(styleService.get(FormStyle.NO_CHANGE_STYLE_ALIAS));
                        }
                    }
                    // Значение
                    if (cell.getColumn().getColumnType() == ColumnType.NUMBER) {
                        cell.setNumericValue(calcDiff((BigDecimal)originalCell.getValue(),
                                (BigDecimal)revisedCell.getValue()));
                    }
                }
                return;
        }
    }

    /**
     * Поиск ячейки по id графы
     */
    private Cell getCellById(int id, DataRow<Cell> dataRow) {
        for (String key : dataRow.keySet()) {
            Cell cell = dataRow.getCell(key);
            if (cell.getColumn().getId().equals(id)) {
                return cell;
            }
        }
        return null;
    }

    /**
     * Признак изменения значения в ячейке
     */
    private boolean isValueChanged(Object original, Object revised) {
        if (original == null && revised == null) {
            return false;
        }
        if (original == null || revised == null) {
            return true;
        }
        return !original.equals(revised);
    }

    /**
     * Вычисление разницы для числовых значений
     */
    private BigDecimal calcDiff(BigDecimal original, BigDecimal revised) {
        if (revised == null) {
            revised = BigDecimal.ZERO;
        }
        if (original == null) {
            original = BigDecimal.ZERO;
        }
        return revised.subtract(original);
    }

    /**
     * Общий стиль для всей строки
     */
    private void rowStyle(DataRow<Cell> dataRow, FormStyle style) {
        for (String key : dataRow.keySet()) {
            Cell cell = dataRow.getCell(key);
            cell.setStyle(style);
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
