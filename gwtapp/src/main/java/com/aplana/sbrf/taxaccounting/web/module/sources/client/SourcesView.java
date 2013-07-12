package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.NewDepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.SelectDepartmentsEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup.SelectDepartmentsEvent;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

public class SourcesView extends ViewWithUiHandlers<SourcesUiHandlers>
		implements SourcesPresenter.MyView, SelectDepartmentsEventHandler {

	interface Binder extends UiBinder<Widget, SourcesView> { }

	interface LinkStyle extends CssResource {
		String enabled();
		String disabled();
	}

	private static final List<TaxType> TAX_TYPES = Arrays.asList(TaxType.values());

	private Map<Integer, FormType> sourcesFormTypes;
	private Map<Integer, FormType> receiversFormTypes;
	private Map<Integer, FormType> receiverSourcesFormType;
	private final SingleSelectionModel<DepartmentFormType> receiverSelectionModel =
			new SingleSelectionModel<DepartmentFormType>();
	private final SingleSelectionModel<DepartmentFormType> sourcesSelectionModel =
			new SingleSelectionModel<DepartmentFormType>();

	@UiField LinkStyle css;

	@UiField(provided = true)
	ValueListBox<TaxType> taxTypePicker;
	@UiField
	GenericDataGrid<DepartmentFormType> sourcesTable;
	@UiField
	GenericDataGrid<DepartmentFormType> receiversTable;
	@UiField
	GenericDataGrid<CheckedDepartmentFormType> receiverSourcesTable;

	@UiField
	NewDepartmentPicker departmentReceiverPicker;
	@UiField
	NewDepartmentPicker departmentSourcePicker;

	@UiField
	Anchor formAnchor;
	@UiField
	Anchor declarationAnchor;
	@UiField
	Anchor assignButton;
	@UiField
	Anchor cancelButton;
	@UiField
	Label formLabel;
	@UiField
	Label declarationLabel;

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
				setReceivers();
				receiverSourcesTable.setRowCount(0);
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
		enableAnchor(assignButton, false);
		enableAnchor(cancelButton, false);
		setForms(true);
	}

	private void setupTables() {
		// Sources
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
					return sourcesFormTypes.get(object.getFormTypeId()).getName();
				} else {
					return "";
				}
			}
		};

		sourcesTable.addColumn(sourceKindColumn, "Вид налоговой формы");
		sourcesTable.setColumnWidth(sourceKindColumn, 150, Style.Unit.PX);
		sourcesTable.addColumn(sourceTypeColumn, "Тип налоговой формы");
		sourcesTable.setRowCount(0);
		sourcesTable.setSelectionModel(sourcesSelectionModel);
		sourcesSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				enableAnchor(assignButton, true);
			}
		});

		// Receivers
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
					return receiversFormTypes.get(object.getFormTypeId()).getName();
				} else {
					return "";
				}
			}
		};

		receiversTable.addColumn(receiverKindColumn, "Вид налоговой формы");
		receiversTable.setColumnWidth(receiverKindColumn, 150, Style.Unit.PX);
		receiversTable.addColumn(receiverTypeColumn, "Тип налоговой формы");
		receiversTable.setRowCount(0);
		receiversTable.setSelectionModel(receiverSelectionModel);
		receiverSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				getUiHandlers().setFormReceiverSources(receiverSelectionModel.getSelectedObject());
				enableAnchor(assignButton, true);
			}
		});

		// Receiver Sources
		Column<CheckedDepartmentFormType, Boolean> checkBoxColumn =
				new Column<CheckedDepartmentFormType, Boolean>(new CheckboxCell()) {
			@Override
			public Boolean getValue(CheckedDepartmentFormType object) {
				return object.isChecked();
			}
		};
		checkBoxColumn.setFieldUpdater(new FieldUpdater<CheckedDepartmentFormType, Boolean>() {
			@Override
			public void update(int index, CheckedDepartmentFormType object, Boolean value) {
				receiverSourcesTable.getVisibleItem(index).setChecked(value);
			}
		});

		TextColumn<CheckedDepartmentFormType> indexColumn = new TextColumn<CheckedDepartmentFormType>() {
			@Override
			public String getValue(CheckedDepartmentFormType object) {
				return "" + object.getIndex();
			}
		};
		indexColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		TextColumn<CheckedDepartmentFormType> receiverSourcesKindColumn = new TextColumn<CheckedDepartmentFormType>() {
			@Override
			public String getValue(CheckedDepartmentFormType object) {
				if (object.getDepartmentFormType().getKind() != null) {
					return object.getDepartmentFormType().getKind().getName();
				} else {
					return "";
				}
			}
		};

		TextColumn<CheckedDepartmentFormType> receiverSourcesTypeColumn = new TextColumn<CheckedDepartmentFormType>() {
			@Override
			public String getValue(CheckedDepartmentFormType object) {
				if (!receiverSourcesFormType.isEmpty() && object.getDepartmentFormType().getFormTypeId() != 0) {
					return receiverSourcesFormType.get(object.getDepartmentFormType().getFormTypeId()).getName();
				} else {
					return "";
				}
			}
		};

		receiverSourcesTable.addColumn(checkBoxColumn);
		receiverSourcesTable.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);
		receiverSourcesTable.addColumn(indexColumn, "№ пп");
		receiverSourcesTable.setColumnWidth(indexColumn, 40, Style.Unit.PX);
		receiverSourcesTable.addColumn(receiverSourcesKindColumn, "Вид налоговой формы");
		receiverSourcesTable.setColumnWidth(receiverSourcesKindColumn, 150, Style.Unit.PX);
		receiverSourcesTable.addColumn(receiverSourcesTypeColumn, "Тип налоговой формы");
		receiverSourcesTable.setRowCount(0);
	}

	private void enableAnchor(Anchor anchor, boolean enabled) {
		if (enabled) {
			anchor.setStyleName(css.enabled());
		} else {
			anchor.setStyleName(css.disabled());
		}

	}

	@Override
	public void setFormSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes) {
		sourcesFormTypes = formTypes;
		sourcesTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setFormReceivers(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes) {
		receiversFormTypes = formTypes;
		receiversTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setFormReceiverSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes) {
		receiverSourcesFormType = formTypes;
		List<CheckedDepartmentFormType> types = new ArrayList<CheckedDepartmentFormType>();
		int index = 1;
		for (DepartmentFormType type : departmentFormTypes) {
			CheckedDepartmentFormType model = new CheckedDepartmentFormType();
			model.setChecked(false);
			model.setIndex(index);
			model.setDepartmentFormType(type);
			types.add(model);
			index++;
		}
		receiverSourcesTable.setRowData(types);
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

	@UiHandler("assignButton")
	public void assign(ClickEvent event) {
		List<Long> sourceIds = new ArrayList<Long>();
		sourceIds.add(sourcesSelectionModel.getSelectedObject().getId());

		for (CheckedDepartmentFormType source : receiverSourcesTable.getVisibleItems()) {
			sourceIds.add(source.getDepartmentFormType().getId());
		}

		getUiHandlers().updateFormSources(receiverSelectionModel.getSelectedObject(), sourceIds);
	}

	@UiHandler("cancelButton")
	public void cancel(ClickEvent event) {
		List<Long> sources = new ArrayList<Long>();

		for (CheckedDepartmentFormType source: receiverSourcesTable.getVisibleItems()) {
			System.out.println("source.isChecked() " + source.isChecked());
			if (!source.isChecked()) {
				sources.add(source.getDepartmentFormType().getId());
			}
		}

		getUiHandlers().updateFormSources(receiverSelectionModel.getSelectedObject(), sources);
	}

	@Override
	public void onDepartmentsReceived(SelectDepartmentsEvent event) {
		if ("Выберите источник".equals(event.getHeader())) {
			setSources();
		} else if ("Выберите приёмник".equals(event.getHeader())) {
			setReceivers();
		}
	}

	private void setSources() {
		if (taxTypePicker.getValue() != null
				&& departmentSourcePicker.getSelectedItems().values().iterator().hasNext()) {
			getUiHandlers().setFormSources(
					departmentSourcePicker.getSelectedItems().values().iterator().next(), taxTypePicker.getValue());
		}
	}

	private void setReceivers() {
		if (taxTypePicker.getValue() != null
				&& departmentReceiverPicker.getSelectedItems().values().iterator().hasNext()) {
			getUiHandlers().setFormReceivers(
					departmentReceiverPicker.getSelectedItems().values().iterator().next(), taxTypePicker.getValue());
		}
	}

	private void setForms(boolean isForm) {
		formLabel.setVisible(isForm);
		formAnchor.setVisible(!isForm);
		declarationLabel.setVisible(!isForm);
		declarationAnchor.setVisible(isForm);
	}

	private class CheckedDepartmentFormType {
		private boolean checked;
		private int index;
		private DepartmentFormType departmentFormType;

		public boolean isChecked() {
			return checked;
		}

		public void setChecked(boolean checked) {
			this.checked = checked;
		}

		public DepartmentFormType getDepartmentFormType() {
			return departmentFormType;
		}

		public void setDepartmentFormType(DepartmentFormType departmentFormType) {
			this.departmentFormType = departmentFormType;
		}

		private int getIndex() {
			return index;
		}

		private void setIndex(int index) {
			this.index = index;
		}
	}

}