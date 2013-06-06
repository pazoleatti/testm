package com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.SelectDepartmentsEventHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * Форма с деревом для отображения доступных подразделений
 * @author Eugene Stetsenko
 */
public class PopUpWithTree extends Composite implements PopUpWithTreeView, HasHandlers {

	interface Binder extends UiBinder<VerticalPanel, PopUpWithTree> {
	}

	private static Binder uiBinder = GWT.create(Binder.class);

	@UiField
	public Label header;

	@UiField
	public Tree tree;

	@UiField
	public Button ok;

	boolean withCheckBox;

	private HandlerManager handlerManager;

	public static final String CHECKBOX_GROUP = "MAIN_GROUP";

	public PopUpWithTree(String header, boolean withCheckBox) {
		initWidget(uiBinder.createAndBindUi(this));
		this.header.setText(header);
		handlerManager = new HandlerManager(this);
		this.withCheckBox = withCheckBox;
	}

	@Override
	public void setItems(List<Department> departments, Set<Integer> availableDepartments) {
		tree.clear();
		List<TreeItem> itemsHierarchy = flatToHierarchy(departments, availableDepartments);
		Collections.sort(itemsHierarchy, new Comparator<TreeItem>() {
			@Override
			public int compare(TreeItem o1, TreeItem o2) {
				return ((CheckBox)o1.getWidget()).getText().compareToIgnoreCase(((CheckBox)o2.getWidget()).getText());
			}
		});
		for (TreeItem item : itemsHierarchy) {
			tree.addItem(item);
		}
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		handlerManager.fireEvent(event);
	}

	@Override
	public HandlerRegistration addDepartmentsReceivedEventHandler(
			SelectDepartmentsEventHandler handler) {
		return handlerManager.addHandler(SelectDepartmentsEvent.TYPE, handler);
	}

	@Override
	public void selectItems(Collection<Integer> itemsIdToSelect) {
		List<TreeItem> allItems = new ArrayList<TreeItem>();
		for (int i=0; i<tree.getItemCount(); i++) {
			allItems.addAll(hierarchyToFlat(tree.getItem(i)));
		}
		for (TreeItem item : allItems) {
			CheckBox checkBox = (CheckBox)item.getWidget();
			if (itemsIdToSelect.contains(((Pair<Integer, String>)item.getUserObject()).first)) {
				checkBox.setValue(true);
			} else {
				checkBox.setValue(false);
			}
		}
	}

	@UiHandler("ok")
	void onOkButtonClicked(ClickEvent event) {
		Map<String, Integer> selectedItems = new HashMap<String, Integer>();
		for (int i=0; i<tree.getItemCount(); i++) {
			for (Pair<Integer, String> pair : getChecked(tree.getItem(i))) {
				selectedItems.put(pair.second, pair.first);
			}
		}
		SelectDepartmentsEvent selectEvent = new SelectDepartmentsEvent(selectedItems);
		fireEvent(selectEvent);
	}

	// Рекурсивный обход дерева
	private Set<Pair<Integer, String>> getChecked(TreeItem treeItem) {
		Set<Pair<Integer,String>> result = new HashSet<Pair<Integer,String>>();
		if (treeItem.getChildCount() == 0) {
			if (((CheckBox)treeItem.getWidget()).getValue()) {
				result.add((Pair<Integer, String>)treeItem.getUserObject());
			}
			return result;
		} else {
			if (((CheckBox)treeItem.getWidget()).getValue()) {
				result.add((Pair<Integer, String>)treeItem.getUserObject());
			}
			for (int i=0; i<treeItem.getChildCount(); i++) {
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
			for (int i=0; i<treeItem.getChildCount(); i++) {
				result.addAll(hierarchyToFlat(treeItem.getChild(i)));
			}
			return result;
		}
	}

	private List<TreeItem> flatToHierarchy(final List<Department> list, final Set<Integer> availableDepartments) {
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
				List<Integer> o1h = TreeUtils.getCachedHierarchy(o1, idToDepMap, hierarchyCacheStorage);
				List<Integer> o2h = TreeUtils.getCachedHierarchy(o2, idToDepMap, hierarchyCacheStorage);

				int min = Math.min(o1h.size(), o2h.size());
				for (int i=0; i<min-1; i++) {
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
			if (withCheckBox) {
				checkBox =new CheckBox(department.getName());
			} else {
				checkBox = new RadioButton(CHECKBOX_GROUP, department.getName());
			}
			if (!availableDepartments.contains(department.getId())) {
				checkBox.setEnabled(false);
			}
			TreeItem newItem = new TreeItem(checkBox);
			newItem.setUserObject(new Pair<Integer, String>(department.getId(), department.getName()));
			if (department.getParentId() != null && lookup.containsKey(department.getParentId())) {
				lookup.get(department.getParentId()).addItem(newItem);
			} else {
				nested.add(newItem);
			}
			lookup.put(department.getId(), newItem);
		}
		Collections.sort(nested, new Comparator<TreeItem>() {
			@Override
			public int compare(TreeItem o1, TreeItem o2) {
				return ((CheckBox)o1.getWidget()).getName().compareToIgnoreCase(((CheckBox)o2.getWidget()).getName());
			}
		});
		return nested;

	}

}
