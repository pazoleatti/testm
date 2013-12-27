package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTree;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTreeItem;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * Виджет-дерево для выбора подразделения. Возвращает пару значений выбранное подразделение-родительское подразделение
 *
 * @author dloshkarev
 */
public class DepartmentTreeWidget extends MultiSelectTree<List<DepartmentPair>> implements PairDepartmentPicker {

    /**
     * Выбранные подразделения
     */
    private List<DepartmentPair> value = new ArrayList<DepartmentPair>();

    public DepartmentTreeWidget(String header, boolean multiSelection) {
        super(header, multiSelection);
    }

    public DepartmentTreeWidget() {
        super();
    }

    @Override
    public void setAvailableValues(List<Department> departments) {
        tree.clear();
        List<DepartmentTreeItem> itemsHierarchy = flatToHierarchy(departments);
        for (DepartmentTreeItem item : itemsHierarchy) {
            item.setState(true);
            addTreeItem(item);
        }
        updateItems();
    }

    @Override
    public boolean isSelectedItemHasChildren() {
        DepartmentTreeItem selectedItem = (DepartmentTreeItem) tree.getSelectedItem();
        return (selectedItem != null &&
                selectedItem.getValue() &&
                selectedItem.getChildCount() > 0);
    }

    @Override
    public List<DepartmentPair> getSelectedChildren() {
        List<DepartmentPair> departments = new ArrayList<DepartmentPair>();
        DepartmentTreeItem selectedItem = (DepartmentTreeItem) tree.getSelectedItem();
        departments.add(selectedItem.getItemValue());
        for (int i = 0; i < selectedItem.getChildCount(); i++) {
            DepartmentTreeItem child = (DepartmentTreeItem) selectedItem.getChild(i);
            departments.add(child.getItemValue());
        }
        return departments;
    }

    @Override
    public void setAcceptableValues(Collection<List<DepartmentPair>> values) {
        // All value acceptable
    }

    @Override
    public List<DepartmentPair> getValue() {
        return value;
    }

    @Override
    public void setValue(List<DepartmentPair> value, boolean fireEvents) {
        super.setValue(value, fireEvents);
        if (value == null || value.isEmpty()) {
            return;
        }
        selectItems(value);
        this.value.clear();
        this.value.addAll(value);
        if (fireEvents) {
            ValueChangeEvent.fire(this, this.value);
        }
    }

    /**
     * Преобразует список подразделений в древовидную структуру, согласно их связям по parentId
     *
     * @param departments список подразделений
     * @return список элементов дерева
     */
    private List<DepartmentTreeItem> flatToHierarchy(final List<Department> departments) {
        final Map<Integer, DepartmentTreeItem> lookup = new LinkedHashMap<Integer, DepartmentTreeItem>();
        final List<DepartmentTreeItem> nested = new ArrayList<DepartmentTreeItem>();
        final Map<Integer, Department> idToDepMap = new HashMap<Integer, Department>();
        for (Department dep : departments) {
            idToDepMap.put(dep.getId(), dep);
        }

        Collections.sort(departments, new Comparator<Department>() {

            private Map<Department, List<Integer>> hierarchyCacheStorage = new HashMap<Department, List<Integer>>();

            @Override
            public int compare(Department o1, Department o2) {
                List<Integer> o1h = TreeUtils.getCachedHierarchy(o1,
                        idToDepMap, hierarchyCacheStorage);
                List<Integer> o2h = TreeUtils.getCachedHierarchy(o2,
                        idToDepMap, hierarchyCacheStorage);

                int min = Math.min(o1h.size(), o2h.size());
                for (int i = 0; i < min - 1; i++) {
                    if (o1h.get(i).compareTo(o2h.get(i)) != 0) {
                        return o1h.get(i).compareTo(o2h.get(i));
                    }
                }
                if (o1h.size() == o2h.size()) {
                    return o1.getName().compareTo(o2.getName());
                }
                return o1h.size() - o2h.size();
            }
        });
        for (Department department : departments) {
            DepartmentTreeItem newItem = new DepartmentTreeItem(department, multiSelection);
            newItem.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    value.clear();
                    executeTreeAction(new TreeElementAction() {
                        @Override
                        public void apply(DepartmentTreeItem treeItem) {
                            if (treeItem.getValue()) {
                                value.add(treeItem.getItemValue());
                            }
                        }
                    });
                    ValueChangeEvent.fire(DepartmentTreeWidget.this, value);
                }

            });
            if (department.getParentId() != null
                    && lookup.containsKey(department.getParentId())) {
                lookup.get(department.getParentId()).addItem(newItem);
            } else {
                nested.add(newItem);
            }
            lookup.put(department.getId(), newItem);
        }
        Collections.sort(nested, new Comparator<DepartmentTreeItem>() {
            @Override
            public int compare(DepartmentTreeItem o1, DepartmentTreeItem o2) {
                return o1.getItemValue().getDepartmentName().compareToIgnoreCase(o2.getItemValue().getDepartmentName());
            }
        });
        return nested;
    }

    /**
     * Применяет действие к элементам дерева по правилу
     *
     * @param action правило выполнения действия к элементам дерева
     */
    public void executeTreeAction(TreeElementAction action) {
        for (int i = 0; i < tree.getItemCount(); i++) {
            traverseTree((DepartmentTreeItem) tree.getItem(i), action);
        }
    }

    /**
     * Обход дерева и применение правила к элементам
     *
     * @param item   элемент дерева
     * @param action действие, выполняемое над элементом
     */
    private void traverseTree(DepartmentTreeItem item, TreeElementAction action) {
        if (item.getChildCount() != 0) {
            for (int i = 0; i < item.getChildCount(); i++) {
                traverseTree((DepartmentTreeItem) item.getChild(i), action);
            }
        }
        action.apply(item);
    }

    /**
     * Выделяет подразделения с указанной комбинацией идентификаторов в дереве
     *
     * @param departmentsToSelect идентификаторы подразделений в парах. Первый - самого подразделения, второй - его родительское подразделение
     */
    @SuppressWarnings("unchecked")
    private void selectItems(final List<DepartmentPair> departmentsToSelect) {
        executeTreeAction(new TreeElementAction() {
            @Override
            public void apply(DepartmentTreeItem treeItem) {
                CheckBox checkBox = (CheckBox) treeItem.getWidget();
                if (departmentsToSelect.contains(treeItem.getItemValue())) {
                    checkBox.setValue(true);
                    if (treeItem.getParentItem() != null) {
                        treeItem.getParentItem().setState(true);
                    }
                } else {
                    checkBox.setValue(false);
                }
            }
        });
    }
}
