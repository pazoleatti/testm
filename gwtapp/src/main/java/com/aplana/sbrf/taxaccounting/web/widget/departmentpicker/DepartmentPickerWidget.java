package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Форма с деревом для отображения доступных подразделений
 * 
 * @author Eugene Stetsenko, Semyon Goryachkin
 */
public class DepartmentPickerWidget extends Composite implements
		DepartmentPicker, HasHandlers {

	interface Binder extends UiBinder<VerticalPanel, DepartmentPickerWidget> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public Label header;

	@UiField
	public Tree tree;

	@UiField
	public Button ok;

	/**
	 * Значения
	 */
	private List<Integer> value;

	/**
	 * Разименованные значения
	 */
	private List<String> valueDereference;

	boolean multiselection;

	public static final String CHECKBOX_GROUP = "MAIN_GROUP";

	
	public DepartmentPickerWidget(String header, boolean multiselection) {
		this();
		setHeader(header);
		setMultiselection(multiselection);
	}
	
	public DepartmentPickerWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setAvalibleValues(List<Department> departments,
			Set<Integer> availableDepartments) {
		tree.clear();
		List<TreeItem> itemsHierarchy = flatToHierarchy(departments,
				availableDepartments);
		Collections.sort(itemsHierarchy, new Comparator<TreeItem>() {
			@Override
			public int compare(TreeItem o1, TreeItem o2) {
				return ((CheckBox) o1.getWidget()).getText()
						.compareToIgnoreCase(
								((CheckBox) o2.getWidget()).getText());
			}
		});
		for (TreeItem item : itemsHierarchy) {
			tree.addItem(item);
		}
	}

	@SuppressWarnings("unchecked")
	private void selectItems(List<Integer> itemsIdToSelect) {
		this.valueDereference = new ArrayList<String>();
		List<TreeItem> allItems = new ArrayList<TreeItem>();
		for (int i = 0; i < tree.getItemCount(); i++) {
			allItems.addAll(hierarchyToFlat(tree.getItem(i)));
		}
		for (TreeItem item : allItems) {
			CheckBox checkBox = (CheckBox) item.getWidget();
			if ((itemsIdToSelect != null)
					&& itemsIdToSelect.contains(((Pair<Integer, String>) item
							.getUserObject()).first)) {
				checkBox.setValue(true);
                this.valueDereference.add(((Pair<Integer, String>) item
                        .getUserObject()).getSecond());
			} else {
				checkBox.setValue(false);
			}
		}
	}

	@UiHandler("ok")
	void onOkButtonClicked(ClickEvent event) {
		this.value = new ArrayList<Integer>();
		this.valueDereference = new ArrayList<String>();
		for (int i = 0; i < tree.getItemCount(); i++) {
			for (Pair<Integer, String> pair : getChecked(tree.getItem(i))) {
				this.value.add(pair.getFirst());
				this.valueDereference.add(pair.getSecond());
			}
		}
		ValueChangeEvent.fire(this, this.value);
	}

	// Рекурсивный обход дерева
	@SuppressWarnings("unchecked")
	private Set<Pair<Integer, String>> getChecked(TreeItem treeItem) {
		Set<Pair<Integer, String>> result = new HashSet<Pair<Integer, String>>();
		if (treeItem.getChildCount() == 0) {
			if (((CheckBox) treeItem.getWidget()).getValue()) {
				result.add((Pair<Integer, String>) treeItem.getUserObject());
			}
			return result;
		} else {
			if (((CheckBox) treeItem.getWidget()).getValue()) {
				result.add((Pair<Integer, String>) treeItem.getUserObject());
			}
			for (int i = 0; i < treeItem.getChildCount(); i++) {
				result.addAll(getChecked(treeItem.getChild(i)));
			}
			return result;
		}
	}

	private List<TreeItem> hierarchyToFlat(TreeItem treeItem) {
		List<TreeItem> result = new ArrayList<TreeItem>();
		if (treeItem == null) {
			return result;
		}
		if (treeItem.getChildCount() == 0) {
			result.add(treeItem);
			return result;
		} else {
			result.add(treeItem);
			for (int i = 0; i < treeItem.getChildCount(); i++) {
				result.addAll(hierarchyToFlat(treeItem.getChild(i)));
			}
			return result;
		}
	}

	private List<TreeItem> flatToHierarchy(final List<Department> list,
			final Set<Integer> availableDepartments) {
		final Map<Integer, TreeItem> lookup = new LinkedHashMap<Integer, TreeItem>();
		final List<TreeItem> nested = new ArrayList<TreeItem>();
		final Map<Integer, Department> idToDepMap = new HashMap<Integer, Department>();
		for (Department dep : list) {
			idToDepMap.put(dep.getId(), dep);
		}

		Collections.sort(list, new Comparator<Department>() {

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
		for (Department department : list) {
			CheckBox checkBox;
			if (multiselection) {
				checkBox = new CheckBox(department.getName());
			} else {
				checkBox = new RadioButton(CHECKBOX_GROUP, department.getName());
			}
			if (availableDepartments!=null && !availableDepartments.contains(department.getId())) {
				checkBox.setEnabled(false);
			}
			TreeItem newItem = new TreeItem(checkBox);
			newItem.setUserObject(new Pair<Integer, String>(department.getId(),
					department.getName()));
			if (department.getParentId() != null
					&& lookup.containsKey(department.getParentId())) {
				lookup.get(department.getParentId()).addItem(newItem);
			} else {
				nested.add(newItem);
			}
			lookup.put(department.getId(), newItem);
		}
		Collections.sort(nested, new Comparator<TreeItem>() {
			@Override
			public int compare(TreeItem o1, TreeItem o2) {
				return ((CheckBox) o1.getWidget()).getName()
						.compareToIgnoreCase(
								((CheckBox) o2.getWidget()).getName());
			}
		});
		return nested;

	}

	@Override
	public List<Integer> getValue() {
		return value;
	}

	@Override
	public void setValue(List<Integer> value) {
		if (value == null){
			value = new ArrayList<Integer>();
		}
		selectItems(value);
		this.value = value;
	}

	@Override
	public void setValue(List<Integer> value, boolean fireEvents) {
		setValue(value);
		if (fireEvents) {
			ValueChangeEvent.fire(this, this.value);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<List<Integer>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public List<String> getValueDereference() {
		return valueDereference;
	}

	@Override
	public void setHeader(String header) {
		this.header.setText(header);
	}

	@Override
	public void setMultiselection(boolean multiselection) {
		this.multiselection = multiselection;
	}

}
