package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import java.util.ArrayList;
import java.util.List;

/**
 * Дерево множественного выбора (с идентификаторами).
 *
 * @author rtimerbaev
 */
public class SimpleTree extends MultiSelectTree<List<Integer>> {

    /** Дерево множественного выбора (с идентификаторами). */
    public SimpleTree() {
        super();
    }

    /**
     * Дерево множественного выбора (с идентификаторами).
     *
     * @param text заголовок
     */
    public SimpleTree(String text) {
        super(text);
    }

    /**
     * Дерево множественного выбора (с идентификаторами).
     *
     * @param text заголовок
     * @param multiSelection true - выбрать несколько элементов, false - выбрать один элемент
     */
    public SimpleTree(String text, boolean multiSelection) {
        super(text, multiSelection);
    }

    @Override
    protected boolean containInValues(List<Integer> values, Integer id) {
        return values.contains(id);
    }

    @Override
    protected boolean equalsValue(Object value, Integer id) {
        return value.equals(id);
    }

    @Override
    public List<Integer> getValue() {
        List<Integer> result = new ArrayList<Integer>();
        for (MultiSelectTreeItem item : getItems()) {
            if (item.getValue()) {
                result.add(item.getId());
            }
        }
        return result;
    }

    /**
     * Установить выбранный элемент по идентификатору.
     *
     * @param value идентификатор элемента
     */
    public void setValue(Integer value) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(value);
        setValue(list);
    }
}
