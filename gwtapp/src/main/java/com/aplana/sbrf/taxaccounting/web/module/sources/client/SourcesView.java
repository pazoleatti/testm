package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.NewDepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.SelectDepartmentsEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.SelectDepartmentsEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class SourcesView extends ViewWithUiHandlers<SourcesUiHandlers>
		implements SourcesPresenter.MyView, SelectDepartmentsEventHandler {

	interface Binder extends UiBinder<Widget, SourcesView> { }

	private static final List<TaxType> TAX_TYPES = Arrays.asList(TaxType.values());

	private Map<Integer, String> sourcesFormTypes;
	private Map<Integer, String> receiversFormTypes;

	@UiField(provided = true)
	ValueListBox<TaxType> taxTypePicker;

	@UiField
	GenericDataGrid<DepartmentFormType> sourceTable;

	@UiField
	GenericDataGrid<DepartmentFormType> receiverTable;

	@UiField
	NewDepartmentPicker departmentReceiverPicker;

	@UiField
	NewDepartmentPicker departmentSourcePicker;

	@Inject
	@UiConstructor
	public SourcesView(final Binder uiBinder) {
		taxTypePicker = new ValueListBox<TaxType>(new AbstractRenderer<TaxType>() {
			@Override
			public String render(TaxType object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		taxTypePicker.addValueChangeHandler(new ValueChangeHandler<TaxType>() {
			@Override
			public void onValueChange(ValueChangeEvent<TaxType> event) {
				setSources();
			}
		});

		taxTypePicker.setAcceptableValues(TAX_TYPES);
		taxTypePicker.setValue(TaxType.INCOME);

		initWidget(uiBinder.createAndBindUi(this));
		setupTables();
		departmentReceiverPicker.setWidth(500);
		departmentSourcePicker.setWidth(500);
		departmentReceiverPicker.addDepartmentsReceivedEventHandler(this);
		departmentSourcePicker.addDepartmentsReceivedEventHandler(this);
	}

	private void setupTables() {
		TextColumn<DepartmentFormType> sourceKindColumn = new TextColumn<DepartmentFormType>() {
			@Override
			public String getValue(DepartmentFormType object) {
				if (object.getKind() != null) {
					return object.getKind().getName();
				} else {
					return "";
				}
			}
		};

		TextColumn<DepartmentFormType> sourceTypeColumn = new TextColumn<DepartmentFormType>() {
			@Override
			public String getValue(DepartmentFormType object) {
				if (!sourcesFormTypes.isEmpty() && object.getFormTypeId() != 0) {
					return sourcesFormTypes.get(object.getFormTypeId());
				} else {
					return "";
				}
			}
		};

		sourceTable.addColumn(sourceKindColumn, "Вид налоговой формы");
		sourceTable.setColumnWidth(sourceKindColumn, 100, Style.Unit.PX);
		sourceTable.addColumn(sourceTypeColumn, "Тип налоговой формы");
		sourceTable.setRowCount(0);

		TextColumn<DepartmentFormType> receiverKindColumn = new TextColumn<DepartmentFormType>() {
			@Override
			public String getValue(DepartmentFormType object) {
				if (object.getKind() != null) {
					return object.getKind().getName();
				} else {
					return "";
				}
			}
		};

		TextColumn<DepartmentFormType> receiverTypeColumn = new TextColumn<DepartmentFormType>() {
			@Override
			public String getValue(DepartmentFormType object) {
				if (!receiversFormTypes.isEmpty() && object.getFormTypeId() != 0) {
					return receiversFormTypes.get(object.getFormTypeId());
				} else {
					return "";
				}
			}
		};

		receiverTable.addColumn(receiverKindColumn, "Вид налоговой формы");
		receiverTable.setColumnWidth(receiverKindColumn, 100, Style.Unit.PX);
		receiverTable.addColumn(receiverTypeColumn, "Тип налоговой формы");
		receiverTable.setRowCount(0);
	}

	@Override
	public void setSourcesFormTypes(Map<Integer, String> formTypes, List<DepartmentFormType> departmentFormTypes) {
		sourcesFormTypes = formTypes;
		sourceTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setDepartments(List<Department> departments) {
		Set<Integer> availableDepartments = new HashSet<Integer>(departments.size());
		for (Department department : departments) {
			availableDepartments.add(department.getId());
		}
		departmentReceiverPicker.setTreeValues(departments, availableDepartments);
		departmentSourcePicker.setTreeValues(departments, availableDepartments);
	}

	@UiHandler("acceptButton")
	public void onAccept(ClickEvent event){
		getUiHandlers().accept();
	}

	@Override
	public void onDepartmentsReceived(SelectDepartmentsEvent event) {
		setSources();
	}

	private void setSources() {
		if (taxTypePicker.getValue() != null
				&& departmentSourcePicker.getSelectedItems().values().iterator().hasNext()) {
			getUiHandlers().setSources(
					departmentSourcePicker.getSelectedItems().values().iterator().next(), taxTypePicker.getValue());
		}
	}
}