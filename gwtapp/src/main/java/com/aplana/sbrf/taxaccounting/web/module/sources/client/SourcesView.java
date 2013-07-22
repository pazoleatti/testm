package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.SelectDepartmentsEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.popup.SelectDepartmentsEvent;
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

	private boolean isForm;
	private boolean canCancel;

	private static final List<TaxType> TAX_TYPES = Arrays.asList(TaxType.values());
	private static final String SOURCE_DEPARTMENT_HEADER = "Выберите источник";
	private static final String RECEIVER_DEPARTMENT_HEADER = "Выберите приёмник";

	private Map<Integer, FormType> sourcesFormTypes;
	private Map<Integer, FormType> receiversFormTypes;
	private Map<Integer, DeclarationType> receiversDeclarationTypes;
	private Map<Integer, FormType> receiverSourcesFormType;
	private final SingleSelectionModel<DepartmentDeclarationType> declarationReceiversSelectionModel =
			new SingleSelectionModel<DepartmentDeclarationType>();
	private final SingleSelectionModel<DepartmentFormType> formReceiversSelectionModel =
			new SingleSelectionModel<DepartmentFormType>();
	private final SingleSelectionModel<DepartmentFormType> sourcesSelectionModel =
			new SingleSelectionModel<DepartmentFormType>();

	@UiField LinkStyle css;

	@UiField(provided = true)
	ValueListBox<TaxType> taxTypePicker;
	@UiField
	GenericDataGrid<DepartmentFormType> sourcesTable;
	@UiField
	GenericDataGrid<DepartmentFormType> formReceiversTable;
	@UiField
	GenericDataGrid<DepartmentDeclarationType> declarationReceiversTable;
	@UiField
	GenericDataGrid<CheckedDepartmentFormType> receiverSourcesTable;

	@UiField
	DepartmentPicker departmentReceiverPicker;
	@UiField
	DepartmentPicker departmentSourcePicker;

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

		initWidget(uiBinder.createAndBindUi(this));

		departmentReceiverPicker.setWidth(500);
		departmentSourcePicker.setWidth(500);
		departmentReceiverPicker.addDepartmentsReceivedEventHandler(this);
		departmentSourcePicker.addDepartmentsReceivedEventHandler(this);

		setupSourcesTables();
		setupReceiversTables();
	}

	@Override
	public void init(boolean isForm) {
		this.isForm = isForm;
		formLabel.setVisible(isForm);
		formAnchor.setVisible(!isForm);
		declarationLabel.setVisible(!isForm);
		declarationAnchor.setVisible(isForm);
		formReceiversTable.setVisible(isForm);
		declarationReceiversTable.setVisible(!isForm);
		taxTypePicker.setValue(TaxType.INCOME);

		departmentReceiverPicker.setSelectedItems(null);
		departmentSourcePicker.setSelectedItems(null);

		enableButtonLink(assignButton, false);
		enableButtonLink(cancelButton, false);

		sourcesTable.setRowCount(0);
		formReceiversTable.setRowCount(0);
		declarationReceiversTable.setRowCount(0);
		receiverSourcesTable.setRowCount(0);
	}

	@Override
	public void setFormSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes) {
		sourcesFormTypes = formTypes;
		sourcesTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setFormReceivers(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes) {
		receiversFormTypes = formTypes;
		formReceiversTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setDeclarationReceivers(Map<Integer, DeclarationType> declarationTypes,
										List<DepartmentDeclarationType> departmentDeclarationTypes) {
		receiversDeclarationTypes = declarationTypes;
		declarationReceiversTable.setRowData(departmentDeclarationTypes);
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
		enableButtonLink(cancelButton, false);
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

	public void setupReceiversTables() {
		// Form Receivers
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

		formReceiversTable.addColumn(receiverKindColumn, "Тип налоговой формы");
		formReceiversTable.setColumnWidth(receiverKindColumn, 150, Style.Unit.PX);
		formReceiversTable.addColumn(receiverTypeColumn, "Вид налоговой формы");
		formReceiversTable.setSelectionModel(formReceiversSelectionModel);
		formReceiversSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				getUiHandlers().getFormReceiverSources(formReceiversSelectionModel.getSelectedObject());
				if (sourcesSelectionModel.getSelectedObject() != null) {
					enableButtonLink(assignButton, true);
				}
			}
		});
		// Declaration Receivers
		TextColumn<DepartmentDeclarationType> declarationReceiverTypeColumn = new TextColumn<DepartmentDeclarationType>() {
			@Override
			public String getValue(DepartmentDeclarationType object) {
				if (!receiversDeclarationTypes.isEmpty() && object.getDeclarationTypeId() != 0) {
					return receiversDeclarationTypes.get(object.getDeclarationTypeId()).getName();
				} else {
					return "";
				}
			}
		};

		declarationReceiversTable.addColumn(declarationReceiverTypeColumn, "Вид декларации");
		declarationReceiversTable.setSelectionModel(declarationReceiversSelectionModel);
		declarationReceiversSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				getUiHandlers().getDeclarationReceiverSources(declarationReceiversSelectionModel.getSelectedObject());
				if (sourcesSelectionModel.getSelectedObject() != null) {
					enableButtonLink(assignButton, true);
				}
			}
		});
	}

	private void setupSourcesTables() {
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

		sourcesTable.addColumn(sourceKindColumn, "Тип налоговой формы");
		sourcesTable.setColumnWidth(sourceKindColumn, 150, Style.Unit.PX);
		sourcesTable.addColumn(sourceTypeColumn, "Вид налоговой формы");
		sourcesTable.setSelectionModel(sourcesSelectionModel);
		sourcesSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
				enableButtonLink(assignButton, true);
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
				canCancel = false;
				enableButtonLink(cancelButton, false);
				receiverSourcesTable.getVisibleItem(index).setChecked(value);

				for(CheckedDepartmentFormType source : receiverSourcesTable.getVisibleItems()) {
					if (source.isChecked()) {
						enableButtonLink(cancelButton, true);
						canCancel = true;
						break;
					}
				}
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
		receiverSourcesTable.addColumn(receiverSourcesKindColumn, "Тип налоговой формы");
		receiverSourcesTable.setColumnWidth(receiverSourcesKindColumn, 150, Style.Unit.PX);
		receiverSourcesTable.addColumn(receiverSourcesTypeColumn, "Вид налоговой формы");
	}

	@UiHandler("assignButton")
	public void assign(ClickEvent event) {
		if ((formReceiversSelectionModel.getSelectedObject() == null &&
				declarationReceiversSelectionModel.getSelectedObject() == null) ||
				sourcesSelectionModel.getSelectedObject() == null) {
			return;
		}

		List<Long> sourceIds = new ArrayList<Long>();
		Long sourceId = sourcesSelectionModel.getSelectedObject().getId();

		for (CheckedDepartmentFormType source : receiverSourcesTable.getVisibleItems()) {
			if (sourceId.equals(source.getDepartmentFormType().getId())) {
				getUiHandlers().showAssignErrorMessage(isForm);
				return;
			}
			sourceIds.add(source.getDepartmentFormType().getId());
		}

		sourceIds.add(sourceId);

		if (isForm) {
			getUiHandlers().updateFormSources(formReceiversSelectionModel.getSelectedObject(), sourceIds);
		} else {
			getUiHandlers().updateDeclarationSources(declarationReceiversSelectionModel.getSelectedObject(), sourceIds);
		}
	}

	@UiHandler("cancelButton")
	public void cancel(ClickEvent event) {
		if (!canCancel || (formReceiversSelectionModel.getSelectedObject() == null &&
				declarationReceiversSelectionModel.getSelectedObject() == null)) {
			return;
		}

		List<Long> sourceIds = new ArrayList<Long>();
		for (CheckedDepartmentFormType source: receiverSourcesTable.getVisibleItems()) {
			if (!source.isChecked()) {
				sourceIds.add(source.getDepartmentFormType().getId());
			}
		}

		if (isForm) {
			getUiHandlers().updateFormSources(formReceiversSelectionModel.getSelectedObject(), sourceIds);
		} else {
			getUiHandlers().updateDeclarationSources(declarationReceiversSelectionModel.getSelectedObject(), sourceIds);
		}

	}

	// Тут мы завязываемся на хидер как на id, тк у нас два пикера департаментов.
	@Override
	public void onDepartmentsReceived(SelectDepartmentsEvent event) {
		if (SOURCE_DEPARTMENT_HEADER.equals(event.getHeader())) {
			setSources();
		} else if (RECEIVER_DEPARTMENT_HEADER.equals(event.getHeader())) {
			setReceivers();
			receiverSourcesTable.setRowCount(0);
		}
	}

	private void setSources() {
		if (taxTypePicker.getValue() != null
				&& departmentSourcePicker.getSelectedItems().values().iterator().hasNext()) {
			getUiHandlers().getFormSources(
					departmentSourcePicker.getSelectedItems().values().iterator().next(), taxTypePicker.getValue());
		}
	}

	private void setReceivers() {
		if (taxTypePicker.getValue() != null
				&& departmentReceiverPicker.getSelectedItems().values().iterator().hasNext()) {
			if (isForm) {
				getUiHandlers().getFormReceivers(
						departmentReceiverPicker.getSelectedItems().values().iterator().next(), taxTypePicker.getValue());
			} else {
				getUiHandlers().getDeclarationReceivers(
						departmentReceiverPicker.getSelectedItems().values().iterator().next(), taxTypePicker.getValue());
			}
		}
	}

	private void enableButtonLink(Anchor anchor, boolean enabled) {
		if (enabled) {
			anchor.setStyleName(css.enabled());
		} else {
			anchor.setStyleName(css.disabled());
		}

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