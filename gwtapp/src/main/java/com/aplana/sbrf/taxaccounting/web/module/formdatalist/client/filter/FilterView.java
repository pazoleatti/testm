package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterView extends ViewImpl implements FilterPresenter.MyView, Editor<FormDataFilter>{

    interface MyBinder extends UiBinder<Widget, FilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, FilterView>{
    }

    private final Widget widget;

    private final MyDriver driver;

    @UiField(provided = true)
	ValueListBox<Integer> reportPeriodId;

    @UiField(provided = true)
	ValueListBox<Integer> formTypeId;

    @UiField(provided = true)
	ValueListBox<FormDataKind> formDataKind;

	@UiField(provided = true)
	ValueListBox<WorkflowState> formState;

	@UiField
	@Editor.Ignore
	TextBox selectedDepartmentTextBox;

	@UiField
	Button selectDepartmentButton;

	private Map<Integer, String> formTypesMap;
	private Map<Integer, String> reportPeriodMaps;

	private List<Department> availableDepartments = new ArrayList<Department>();
	private Integer selectedDepartmentId;

    @Inject
	@UiConstructor
    public FilterView(final MyBinder binder, final MyDriver driver) {
		formState = new ValueListBox<WorkflowState>(new AbstractRenderer<WorkflowState>() {
			@Override
			public String render(WorkflowState object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		formDataKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
			@Override
			public String render(FormDataKind object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		formTypeId = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return formTypesMap.get(object);
			}
		});

		reportPeriodId = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
			@Override
			public String render(Integer object) {
				if (object == null) {
					return "";
				}
				return reportPeriodMaps.get(object);
			}
		});

        widget = binder.createAndBindUi(this);
        this.driver = driver;
        this.driver.initialize(this);
    }


    @Override
    public Widget asWidget() {
        return widget;
    }


    @Override
    public void setDataFilter(FormDataFilter formDataFilter) {
        driver.edit(formDataFilter);
    }


    @Override
    public FormDataFilter getDataFilter() {
        return driver.flush();
    }

    @Override
    public void setKindList(List<FormDataKind> list) {
		formDataKind.setAcceptableValues(list);
    }

	@Override
	public void setFormStateList(List<WorkflowState> list){
		formState.setAcceptableValues(list);
	}

	@Override
	public void setFormTypesMap(Map<Integer, String> formTypesMap){
		formTypesMap.put(null, "");
		this.formTypesMap = formTypesMap;
		/** .setValue(null) see
		 *  http://stackoverflow.com/questions/11176626/how-to-remove-null-value-from-valuelistbox-values **/
		formTypeId.setValue(null);
		formTypeId.setAcceptableValues(formTypesMap.keySet());
	}

	@Override
	public void setReportPeriodMaps(Map<Integer, String> reportPeriodMaps) {
		reportPeriodMaps.put(null, "");
		this.reportPeriodMaps = reportPeriodMaps;
		/** .setValue(null) see
		 *  http://stackoverflow.com/questions/11176626/how-to-remove-null-value-from-valuelistbox-values **/
		reportPeriodId.setValue(null);
		reportPeriodId.setAcceptableValues(reportPeriodMaps.keySet());
	}

	@Override
	public void setDepartmentsList(List<Department> list){
		this.availableDepartments.addAll(list);
	}

	@Override
	public Integer getSelectedDepartmentId(){
		return this.selectedDepartmentId;
	}

	@Override
	public String getSelectedDepartmentName(){
		return this.selectedDepartmentTextBox.getText();
	}

	@Override
	public void setSelectedDepartment(Integer departmentId, String departmentName){
		selectedDepartmentId = departmentId;
		selectedDepartmentTextBox.setText(departmentName);
	}

	@UiHandler("selectDepartmentButton")
	void onSelectDepartmentButtonClicked(ClickEvent clickEvent){
		DepartmentSelectionTree departmentSelection = new DepartmentSelectionTree(availableDepartments);
		Widget popup = selectedDepartmentTextBox.asWidget();
		departmentSelection.show(popup.getAbsoluteLeft(),
				popup.getAbsoluteTop() + popup.getOffsetHeight()
		);
	}


	private final class DepartmentSelectionTree extends Widget {

		private static final String ROOT_PANEL_WIDTH        = "250px";
		private static final String ROOT_PANEL_HEIGHT       = "250px";
		private static final String PANEL_WITH_TREE_WIDTH   = "250px";
		private static final String PANEL_WITH_TREE_HEIGHT  = "250px";
		private static final String APPLY_BUTTON_WIDTH      = "50px";
		private static final String DEPARTMENT_SELECTION_HEADER = "Выберите подразделение";
		private static final String ALL_VALUE_CAPTION = "Все";
		private static final String DEPARTMENT_RADIO_BUTTON_GROUP = "DEPARTMENTS";

		private final List<Department> departmentList;
		private final Map<String, Integer> departmentsMap = new HashMap<String, Integer>();
		private final Map<Integer,  Pair<Integer, TreeItem>> nodes = new HashMap<Integer, Pair<Integer, TreeItem>>();

		private final Label departmentLabel = new Label(DEPARTMENT_SELECTION_HEADER);
		private final Tree tree = new Tree();
		private final PopupPanel popupPanel = new PopupPanel(true, false);
		private final VerticalPanel rootPanel = new VerticalPanel();
		private final ScrollPanel panelWithTree = new ScrollPanel();
		private final HorizontalPanel buttonsPanel = new HorizontalPanel();
		private final Button applyButton = new Button("ОК");


		public DepartmentSelectionTree(List<Department> departments){
			departmentList = departments;
			createTree();
			setupUI();
		}

		private void createTree(){
			//Подразделения, для которых нету родителя, относятся к rootNode
			TreeItem rootNode = null;

			//Из списка всех департаментов, которые доступны для данного пользователя формируем структуру (Map),
			//ключ которой - Id департамента, Значение - Pair(Id родительского департамента, TreeItem - элемент дерева)
			//Также запонляем departmentsMap, в которой Ключ - Имя департамента, Значение - Id департамента. Данная
			//Map'ка нужна для того, чтобы мы смогли получить Id департамента, после того как пользователь выбрал
			//подразделение и нажал "ОК", т.к. элементом дерева у нас является RadioButton, которая может хранить только
			//имя подразделения.
			for(Department department : departmentList){
				RadioButton treeElement = new RadioButton(DEPARTMENT_RADIO_BUTTON_GROUP, department.getName());
				Pair<Integer, TreeItem> treeItemPair = new Pair<Integer, TreeItem>(department.getParentId() ,
						new TreeItem(treeElement));
				nodes.put(department.getId(), treeItemPair);

				departmentsMap.put(department.getName(), department.getId());
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

			tree.addItem(new TreeItem(new RadioButton(DEPARTMENT_RADIO_BUTTON_GROUP, ALL_VALUE_CAPTION)));
			tree.addItem(rootNode);
		}

		private void setupUI(){
			rootPanel.setWidth(ROOT_PANEL_WIDTH);
			rootPanel.setHeight(ROOT_PANEL_HEIGHT);
			panelWithTree.setWidth(PANEL_WITH_TREE_WIDTH);
			panelWithTree.setHeight(PANEL_WITH_TREE_HEIGHT);
			applyButton.setWidth(APPLY_BUTTON_WIDTH);
			applyButton.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);
			departmentLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);

			buttonsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			applyButton.getElement().getStyle().setMarginLeft(200, Style.Unit.PX);
			buttonsPanel.add(applyButton);
			rootPanel.add(departmentLabel);
			rootPanel.add(panelWithTree);
			rootPanel.add(buttonsPanel);
			popupPanel.add(rootPanel);
			panelWithTree.add(tree);

			applyButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					setSelectedDepartment(departmentsMap.get(tree.getSelectedItem().getText()),
							tree.getSelectedItem().getText());
					popupPanel.hide();
				}
			});
		}

		public void show(int left, int top){
			popupPanel.setPopupPosition(left, top);
			popupPanel.show();
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
