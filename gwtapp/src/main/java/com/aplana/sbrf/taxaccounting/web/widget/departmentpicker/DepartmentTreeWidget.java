package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentPair;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTree;
import com.aplana.sbrf.taxaccounting.web.widget.multiselecttree.MultiSelectTreeItem;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;

import java.util.*;

/**
 * Виджет-дерево для выбора подразделения. Возвращает пару значений выбранное подразделение-родительское подразделение
 *
 * @author dloshkarev
 */
public class DepartmentTreeWidget extends MultiSelectTree<List<DepartmentPair>> implements PairDepartmentPicker {

    private List<Integer> availableValuesList = new LinkedList<Integer>();

    /** Дерево для выбора подразделения. */
    public DepartmentTreeWidget(String header, boolean multiSelection) {
        super(header, multiSelection);
    }

    /** Дерево для выбора подразделения. */
    public DepartmentTreeWidget() {
        super();
    }

    @Override
    public List<Integer> getAvalibleValues() {
        return availableValuesList;
    }

    @Override
    public void setAvailableValues(List<Department> departments) {
        setAvailableValues(departments, null);
    }

    @Override
    public void setAvailableValues(List<Department> departments, Set<Integer> availableDepartments) {
        tree.clear();
        availableValuesList.clear();
        if (availableDepartments != null) {
            availableValuesList.addAll(availableDepartments);
        }
        List<DepartmentTreeItem> itemsHierarchy = flatToHierarchy(departments, availableDepartments);
        for (DepartmentTreeItem item : itemsHierarchy) {
            item.setState(true);
            addTreeItem(item);
        }
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
	    for (DepartmentTreeItem item : getAllChildren(selectedItem)) {
		    departments.add(item.getItemValue());
	    }
        return departments;
    }

	private List<DepartmentTreeItem> getAllChildren(DepartmentTreeItem treeItem) {
		List<DepartmentTreeItem> allChildren = new ArrayList<DepartmentTreeItem>();

		for (int i=0; i<treeItem.getChildCount(); i++) {
			allChildren.add((DepartmentTreeItem) treeItem.getChild(i));
			allChildren.addAll(getAllChildren((DepartmentTreeItem) treeItem.getChild(i)));
		}
		return allChildren;
	}

    @Override
    public void setAcceptableValues(Collection<List<DepartmentPair>> values) {
        // All value acceptable
    }

    @Override
    protected boolean containInValues(List<DepartmentPair> values, Integer id) {
        for (DepartmentPair item : values) {
            if (item.getDepartmentId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean equalsValue(Object value, Integer id) {
        return ((DepartmentPair) value).getDepartmentId().equals(id);
    }

    @Override
    public List<DepartmentPair> getValue() {
        List<DepartmentPair> result = new ArrayList<DepartmentPair>();
        for (MultiSelectTreeItem item : getItems()) {
            if (item.getValue()) {
                result.add(((DepartmentTreeItem)item).getItemValue());
            }
        }
        return result;
    }

    @Override
    public void setValue(List<DepartmentPair> value, boolean fireEvents) {
        super.setValue(value, fireEvents);
        if (value == null || value.isEmpty()) {
            return;
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /** Установить выбранными элементы по идентификаторам. */
    public void setValueById(List<Integer> value, boolean fireEvents) {
        List<DepartmentPair> list = new ArrayList<DepartmentPair>();
        for (Integer i : value) {
            DepartmentPair departmentPair = new DepartmentPair(i, null);
            list.add(departmentPair);
        }
        setValue(list, fireEvents);
    }

    /**
     * Преобразует список подразделений в древовидную структуру, согласно их связям по parentId
     *
     * @param departments список подразделений
     * @param availableDepartments список доступных подразделений
     *
     * @return список элементов дерева
     */
    private List<DepartmentTreeItem> flatToHierarchy(final List<Department> departments,
                                                     final Set<Integer> availableDepartments) {
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
                List<Integer> o1h = TreeUtils.getCachedHierarchy(o1, idToDepMap, hierarchyCacheStorage);
                List<Integer> o2h = TreeUtils.getCachedHierarchy(o2, idToDepMap, hierarchyCacheStorage);

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

            if (availableDepartments != null && !availableDepartments.contains(department.getId()) &&
                    newItem.getWidget() instanceof CheckBox) {
                ((CheckBox) newItem.getWidget()).setEnabled(false);
            }

            // если элемент выбрали
            newItem.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    ValueChangeEvent.fire(DepartmentTreeWidget.this, getValue());
                }
            });

            if (department.getParentId() != null && lookup.containsKey(department.getParentId())) {
                lookup.get(department.getParentId()).addItem(newItem);
            } else {
                nested.add(newItem);
            }
            lookup.put(department.getId(), newItem);
        }

        // сортировка по названию подразделения
        Collections.sort(nested, new Comparator<DepartmentTreeItem>() {
            @Override
            public int compare(DepartmentTreeItem o1, DepartmentTreeItem o2) {
                return o1.getItemValue().getDepartmentName().compareToIgnoreCase(o2.getItemValue().getDepartmentName());
            }
        });
        return nested;
    }
}