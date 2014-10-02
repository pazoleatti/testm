package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.Diff;
import com.aplana.sbrf.taxaccounting.model.DiffType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.service.DiffService;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
}
