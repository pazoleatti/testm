package com.aplana.sbrf.taxaccounting.web.widget.treepicker;


import com.aplana.sbrf.taxaccounting.model.Department;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TreePicker extends Composite{

	interface SelectionUiBinder extends UiBinder<HTMLPanel, TreePicker> {
	}

	private static SelectionUiBinder uiBinder = GWT.create(SelectionUiBinder.class);

	private HandlerManager handlerManager;

	@UiField
	TextBox selected;

	@UiField
	Button selectButton;

	private static final String ROOT_PANEL_WIDTH        = "250px";
	private static final String ROOT_PANEL_HEIGHT       = "250px";
	private static final String PANEL_WITH_TREE_WIDTH   = "250px";
	private static final String PANEL_WITH_TREE_HEIGHT  = "250px";
	private static final String APPLY_BUTTON_WIDTH      = "50px";

	private final List<Department> sourceList = new ArrayList<Department>();
	private final Map<String, Integer> allTreeItems = new HashMap<String, Integer>();
	private final Map<String, Integer> selectedItems = new HashMap<String, Integer>();
	private final Map<Integer,  Pair<Integer, TreeItem>> nodes = new HashMap<Integer, Pair<Integer, TreeItem>>();

	private final Label popupPanelLabel = new Label();
	private final Tree tree = new Tree();
	private final PopupPanel popup = new PopupPanel(true, false);
	private final VerticalPanel rootPanel = new VerticalPanel();
	private final ScrollPanel panelWithTree = new ScrollPanel();
	private final HorizontalPanel buttonsPanel = new HorizontalPanel();
	private final Button applyButton = new Button("ОК");

	@UiConstructor
	public TreePicker(String popupPanelCaption) {
		initWidget(uiBinder.createAndBindUi(this));
		setupUI(popupPanelCaption);
	}

	public void setTreeValues(List<Department> source){
		sourceList.clear();
		sourceList.addAll(source);
	}

	public Map<String, Integer> getSelectedItems(){
		return selectedItems;
	}

	public void setSelectedItems(Map<String, Integer> values){
		selectedItems.clear();
		selectedItems.putAll(values);
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
			CheckBox treeElement = new CheckBox(department.getName());
			Pair<Integer, TreeItem> treeItemPair = new Pair<Integer, TreeItem>(department.getParentId() ,
					new TreeItem(treeElement));
			nodes.put(department.getId(), treeItemPair);
			allTreeItems.put(department.getName(), department.getId());
		}

		//После того, как мы имеем сфомрированную структуру, нам нужно пробежаться по ней (по всем значениям Map'ки)
		// и "связать" элементы дерева друг с другом.
		for(Map.Entry<Integer, Pair<Integer, TreeItem>> node : nodes.entrySet()){
			if(nodes.get(node.getValue().getA()) != null){
				//Если у элемента есть родитель - добавляем связь с родителем
				nodes.get(node.getValue().getA()).getB().addItem(node.getValue().getB());
			} else {
				//Если нету - добавляем в корень дерева
				rootNode = node.getValue().getB();
			}
		}

		tree.addItem(rootNode);
		return true;
	}

	private void clearTree(){
		tree.clear();
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

		popup.addCloseHandler(new CloseHandler<PopupPanel>() {
			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				processSelectedElements();
			}
		});

		selectButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearTree();
				if(createTree()){
					selectedItems.clear();
					popup.show();
				}
			}
		});

		tree.addSelectionHandler(new SelectionHandler<TreeItem>() {
			@Override
			public void onSelection(SelectionEvent event) {
				TreeItem selectedItem = (TreeItem)event.getSelectedItem();
				Integer selectedItemId = allTreeItems.get(selectedItem.getText());
				if(!selectedItems.containsKey(selectedItem.getText())){
					selectedItems.put(selectedItem.getText(), selectedItemId);
				} else {
					selectedItems.remove(selectedItem.getText());
				}
			}
		});
	}

	private void processSelectedElements(){
		StringBuilder result = new StringBuilder();
		StringBuilder tooltipTitle = new StringBuilder();
		for(Map.Entry<String, Integer> selectedItem : selectedItems.entrySet()){
			result.append(selectedItem.getKey()).append(";");
			tooltipTitle.append(selectedItem.getKey()).append("\n");
		}
		selected.setText(result.toString());
		selected.setTitle(tooltipTitle.toString());
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
