package com.aplana.sbrf.taxaccounting.web.widget.treepicker;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreePicker extends Composite{

	interface SelectionUiBinder extends UiBinder<HTMLPanel, TreePicker> {
	}

	private static SelectionUiBinder uiBinder = GWT.create(SelectionUiBinder.class);

	@UiField
	TextBox selected;

	@UiField
	Button selectButton;

	private static final String ROOT_PANEL_WIDTH        = "250px";
	private static final String ROOT_PANEL_HEIGHT       = "250px";
	private static final String PANEL_WITH_TREE_WIDTH   = "250px";
	private static final String PANEL_WITH_TREE_HEIGHT  = "250px";
	private static final String APPLY_BUTTON_WIDTH      = "50px";
	private static final String RADIO_BUTTON_GROUP      = "DEPARTMENT_SELECTION";
	private final boolean MULTISELECT_TREE;
	private boolean isTreeCreated = false;

	private final List<Department> sourceList = new ArrayList<Department>();
	private final List<Integer> availableForUserDepartmentIds = new ArrayList<Integer>();
	private final Map<String, Integer> allTreeItems = new HashMap<String, Integer>();
	private final Map<String, Integer> selectedItems = new HashMap<String, Integer>();
	private final Map<Integer,  Pair<Integer, DepartmentItem>> nodes = new HashMap<Integer, Pair<Integer, DepartmentItem>>();

	private final Label popupPanelLabel = new Label();
	private final Tree tree = new Tree();
	private final PopupPanel popup = new PopupPanel(true, false);
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final ScrollPanel panelWithTree = new ScrollPanel();
	private final HorizontalPanel buttonsPanel = new HorizontalPanel();
	private final Button applyButton = new Button("ОК");

	@UiConstructor
	public TreePicker(String popupPanelCaption) {
		this(popupPanelCaption, true);
	}

	@UiConstructor
	public TreePicker(String popupPanelCaption, boolean isMultiselect) {
		initWidget(uiBinder.createAndBindUi(this));
		setupUI(popupPanelCaption);
		MULTISELECT_TREE = isMultiselect;
	}

	public void setTreeValues(List<Department> source, Set<Integer> availableDepartments){
		sourceList.clear();
		availableForUserDepartmentIds.clear();

		availableForUserDepartmentIds.addAll(availableDepartments);
		sourceList.addAll(source);
	}

	public Map<String, Integer> getSelectedItems(){
		return selectedItems;
	}

	public void setSelectedItems(Map<String, Integer> values){
		if(!isTreeCreated){
			if(!handleTreeCreation()){
				return;
			}
		}

		selectedItems.clear();
		uncheckAll();
		if(values == null){
			processSelectedElements();
			return;
		}
		selectedItems.putAll(values);
		for(Map.Entry<String, Integer> entry : values.entrySet()){
			Pair<Integer, DepartmentItem> pair = nodes.get(entry.getValue());
			if (pair != null){
				pair.getB().getCheckBox().setValue(true);
			}
		}
		processSelectedElements();
	}

	//Если дерево создалось успешно - возвращаем true
	private boolean createTree(){
		allTreeItems.clear();
		if(sourceList.size() == 0){
			return false;
		}
		//Подразделения, для которых нету родителя, относятся к rootNode
		TreeItem rootNode = null;

		//Из списка всех департаментов, которые доступны для данного пользователя формируем структуру (Map),
		//ключ которой - Id департамента, Значение - Pair(Id родительского департамента, TreeItem - элемент дерева)
		//Также запонляем departmentsMap, в которой Ключ - Имя департамента, Значение - Id департамента. Данная
		//Map'ка нужна для того, чтобы мы смогли получить Id департамента, после того как пользователь выбрал
		//подразделение и нажал "ОК", т.к. элементом дерева у нас является RadioButton, которая может хранить только
		//имя подразделения.
		for(final Department department : sourceList){
			if(availableForUserDepartmentIds.contains(department.getId())){
				CheckBox checkbox;
				if(MULTISELECT_TREE){
					checkbox = new CheckBox(department.getName());
				} else {
					checkbox = new RadioButton(RADIO_BUTTON_GROUP, department.getName());
				}
				addValueChangeHandler(checkbox);
				DepartmentItem departmentItem = new DepartmentItem(checkbox);
				departmentItem.setCheckBox(checkbox);
				Pair<Integer, DepartmentItem> treeItemPair = new Pair<Integer, DepartmentItem>(department.getParentId(), departmentItem);
				nodes.put(department.getId(), treeItemPair);
			} else {
				Label treeElement = new Label(department.getName());
				Pair<Integer, DepartmentItem> treeItemPair = new Pair<Integer, DepartmentItem>(department.getParentId() ,
						new DepartmentItem(treeElement));
				nodes.put(department.getId(), treeItemPair);
			}
			allTreeItems.put(department.getName(), department.getId());
		}

		//После того, как мы имеем сфомрированную структуру, нам нужно пробежаться по ней (по всем значениям Map'ки)
		// и "связать" элементы дерева друг с другом.
		for(Map.Entry<Integer, Pair<Integer, DepartmentItem>> node : nodes.entrySet()){
			if(nodes.get(node.getValue().getA()) != null){
				//Если у элемента есть родитель - добавляем связь с родителем
				nodes.get(node.getValue().getA()).getB().addItem(node.getValue().getB());
			} else {
				//Если нету - добавляем в корень дерева
				rootNode = node.getValue().getB();
			}
		}

		tree.addItem(rootNode);
		isTreeCreated = true;
		return isTreeCreated;
	}

	private void setupUI(String popupPanelLabelText){
		rootPanel.setWidth(ROOT_PANEL_WIDTH);
		rootPanel.setHeight(ROOT_PANEL_HEIGHT);
		panelWithTree.setWidth(PANEL_WITH_TREE_WIDTH);
		panelWithTree.setHeight(PANEL_WITH_TREE_HEIGHT);
		applyButton.setWidth(APPLY_BUTTON_WIDTH);
		applyButton.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
		popupPanelLabel.setText(popupPanelLabelText);
		popupPanelLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);

		buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		applyButton.getElement().getStyle().setMarginLeft(200, Style.Unit.PX);
		buttonsPanel.add(applyButton);
		rootPanel.add(popupPanelLabel);
		rootPanel.add(panelWithTree);
		rootPanel.add(buttonsPanel);
		popup.add(rootPanel);
		panelWithTree.add(tree);

		applyButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				processSelectedElements();
				popup.hide();
			}
		});
		popup.setPopupPosition(getPopupLeftOffset(), getPopupTopOffset());
		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				processSelectedElements();
			}
		});

		selectButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if(!isTreeCreated){
					if(handleTreeCreation()){
						popup.show();
					}
				} else {
					popup.setPopupPosition(getPopupLeftOffset(), getPopupTopOffset());
					popup.show();
				}
			}
		});
	}

	private final class DepartmentItem extends TreeItem{
		private DepartmentItem(Widget widget){
			super(widget);
		}
		private CheckBox checkBox;

		public CheckBox getCheckBox() {
			return checkBox;
		}

		public void setCheckBox(CheckBox checkBox) {
			this.checkBox = checkBox;
		}
	}

	private void addValueChangeHandler(final CheckBox checkBox){
		checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				CheckBox selectedItem = (CheckBox)event.getSource();
				Integer selectedItemId = allTreeItems.get(selectedItem.getText());
				if(checkBox.getValue()){
					if(checkBox instanceof RadioButton){
						selectedItems.clear();
					}
					selectedItems.put(selectedItem.getText(), selectedItemId);
				} else if (!checkBox.getValue()) {
					selectedItems.remove(selectedItem.getText());
				}
			}
		});
	}

	private void uncheckAll(){
		for(Map.Entry<Integer,  Pair<Integer, DepartmentItem>> item : nodes.entrySet()){
			CheckBox checkBox = item.getValue().getB().getCheckBox();
			if(checkBox != null){
				checkBox.setValue(false);
			}
		}
	}

	private void processSelectedElements(){
		StringBuilder result = new StringBuilder();
		StringBuilder tooltipTitle = new StringBuilder();
		for(Map.Entry<String, Integer> selectedItem : selectedItems.entrySet()){
			result.append(selectedItem.getKey()).append("; ");
			tooltipTitle.append(selectedItem.getKey()).append("\n");
		}
		selected.setText(result.toString());
		selected.setTitle(tooltipTitle.toString());
	}

	private int getPopupLeftOffset(){
		return (Window.getClientWidth() / 2) - 125;
	}

	private int getPopupTopOffset(){
		return (Window.getClientHeight() / 2) - 125;
	}

	private boolean handleTreeCreation(){
		if(createTree()){
			selectedItems.clear();
			return true;
		} else {
			Window.alert("Нету доступных подразделений");
			return false;
		}
	}

	private final class Pair<A, B> {
		private final A a;
		private final B b;

		public Pair(A a, B b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Pair<?, ?>)) {
				return false;
			}
			Pair<?, ?> other = (Pair<?, ?>) o;
			return a.equals(other.a) && b.equals(other.b);
		}

		public A getA() {
			return a;
		}

		public B getB() {
			return b;
		}

		@Override
		public int hashCode() {
			return a.hashCode() * 13 + b.hashCode() * 7;
		}
	}
}
