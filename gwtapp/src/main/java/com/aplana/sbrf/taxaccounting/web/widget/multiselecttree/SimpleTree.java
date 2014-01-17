package com.aplana.sbrf.taxaccounting.web.widget.multiselecttree;

import java.util.ArrayList;
import java.util.List;

public class SimpleTree extends MultiSelectTree<List<Integer>> {

    public SimpleTree() {
        super();
    }

    public SimpleTree(String text) {
        super(text);
    }

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

    public void setValue(Integer value) {
        List<Integer> list = new ArrayList<Integer>();
        list.add(value);
        setValue(list);
    }
}
