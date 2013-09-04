package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentFormTypeShared;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class SourcesView extends ViewWithUiHandlers<SourcesUiHandlers>
		implements SourcesPresenter.MyView, ValueChangeHandler<List<Integer>> {

	interface Binder extends UiBinder<Widget, SourcesView> { }

	interface LinkStyle extends CssResource {
		String enabled();
		String disabled();
	}

	private boolean isForm;
	private boolean canCancel;

	private static final List<TaxType> TAX_TYPES = Arrays.asList(TaxType.values());

	private Map<Integer, FormType> sourcesFormTypes;
	private Map<Integer, FormType> receiversFormTypes;
	private Map<Integer, DeclarationType> receiversDeclarationTypes;

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
	GenericDataGrid<DepartmentFormTypeShared> currentSourcesTable;

	@UiField
	DepartmentPickerPopupWidget departmentReceiverPicker;
	@UiField
	DepartmentPickerPopupWidget departmentSourcePicker;

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
				currentSourcesTable.setRowCount(0);
			}
		});
		taxTypePicker.setAcceptableValues(TAX_TYPES);

		initWidget(uiBinder.createAndBindUi(this));

		departmentReceiverPicker.setWidth(500);
		departmentSourcePicker.setWidth(500);
		departmentReceiverPicker.addValueChangeHandler(this);
		departmentSourcePicker.addValueChangeHandler(this);

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

		departmentReceiverPicker.setValue(null);
		departmentSourcePicker.setValue(null);

		enableButtonLink(assignButton, false);
		enableButtonLink(cancelButton, false);

		sourcesTable.setRowCount(0);
		formReceiversTable.setRowCount(0);
		declarationReceiversTable.setRowCount(0);
		currentSourcesTable.setRowCount(0);
	}

	@Override
	public void setAvalibleSources(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes) {
		sourcesFormTypes = formTypes;
		sourcesTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setAvalibleFormReceivers(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes) {
		receiversFormTypes = formTypes;
		formReceiversTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setAvalibleDeclarationReceivers(Map<Integer, DeclarationType> declarationTypes,
										List<DepartmentDeclarationType> departmentDeclarationTypes) {
		receiversDeclarationTypes = declarationTypes;
		declarationReceiversTable.setRowData(departmentDeclarationTypes);
	}

	@Override
	public void setCurrentSources(List<DepartmentFormTypeShared> departmentFormTypes) {
		currentSourcesTable.setRowData(departmentFormTypes);
		enableButtonLink(cancelButton, false);
	}

	@Override
	public void setDepartments(List<Department> departments) {
		Set<Integer> availableDepartments = new HashSet<Integer>(departments.size());
		for (Department department : departments) {
			availableDepartments.add(department.getId());
		}
		departmentReceiverPicker.setAvalibleValues(departments, availableDepartments);
		departmentReceiverPicker.setValue(new ArrayList<Integer>());
		departmentSourcePicker.setAvalibleValues(departments, availableDepartments);
		departmentSourcePicker.setValue(new ArrayList<Integer>());
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
		Column<DepartmentFormTypeShared, Boolean> checkBoxColumn =
				new Column<DepartmentFormTypeShared, Boolean>(new CheckboxCell()) {
					@Override
					public Boolean getValue(DepartmentFormTypeShared object) {
						return object.isChecked();
					}
				};
				
		checkBoxColumn.setFieldUpdater(new FieldUpdater<DepartmentFormTypeShared, Boolean>() {
			@Override
			public void update(int index, DepartmentFormTypeShared object, Boolean value) {
				canCancel = false;
				enableButtonLink(cancelButton, false);
				currentSourcesTable.getVisibleItem(index).setChecked(value);
				for(DepartmentFormTypeShared source : currentSourcesTable.getVisibleItems()) {
					if (source.isChecked()) {
						enableButtonLink(cancelButton, true);
						canCancel = true;
						break;
					}
				}
			}
		});

		TextColumn<DepartmentFormTypeShared> indexColumn = new TextColumn<DepartmentFormTypeShared>() {
			@Override
			public String getValue(DepartmentFormTypeShared object) {
				return String.valueOf(object.getIndex());
			}
		};
		indexColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		TextColumn<DepartmentFormTypeShared> departmentColumn = new TextColumn<DepartmentFormTypeShared>() {
			@Override
			public String getValue(DepartmentFormTypeShared object) {
				return object.getDepartmentName();
			}
		};

		TextColumn<DepartmentFormTypeShared> receiverSourcesKindColumn = new TextColumn<DepartmentFormTypeShared>() {
			@Override
			public String getValue(DepartmentFormTypeShared object) {
				return object.getKind().getName();
			}
		};

		TextColumn<DepartmentFormTypeShared> receiverSourcesTypeColumn = new TextColumn<DepartmentFormTypeShared>() {
			@Override
			public String getValue(DepartmentFormTypeShared object) {
				return object.getFormTypeName();
			}
		};

		currentSourcesTable.addColumn(checkBoxColumn);
		currentSourcesTable.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);
		currentSourcesTable.addColumn(indexColumn, "№ пп");
		currentSourcesTable.setColumnWidth(indexColumn, 40, Style.Unit.PX);
		currentSourcesTable.addColumn(departmentColumn, "Подразделение");
		currentSourcesTable.setColumnWidth(departmentColumn, 250, Style.Unit.PX);
		currentSourcesTable.addColumn(receiverSourcesKindColumn, "Тип налоговой формы");
		currentSourcesTable.setColumnWidth(receiverSourcesKindColumn, 150, Style.Unit.PX);
		currentSourcesTable.addColumn(receiverSourcesTypeColumn, "Вид налоговой формы");
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

		for (DepartmentFormTypeShared source : currentSourcesTable.getVisibleItems()) {
			if (sourceId.equals(source.getId())) {
				getUiHandlers().showAssignErrorMessage(isForm);
				return;
			}
			sourceIds.add(source.getId());
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
		for (DepartmentFormTypeShared source: currentSourcesTable.getVisibleItems()) {
			if (!source.isChecked()) {
				sourceIds.add(source.getId());
			}
		}

		if (isForm) {
			getUiHandlers().updateFormSources(formReceiversSelectionModel.getSelectedObject(), sourceIds);
		} else {
			getUiHandlers().updateDeclarationSources(declarationReceiversSelectionModel.getSelectedObject(), sourceIds);
		}

	}


	private void setSources() {
		if (taxTypePicker.getValue() != null
				&& departmentSourcePicker.getValue().iterator().hasNext()) {
			getUiHandlers().getFormSources(
					departmentSourcePicker.getValue().iterator().next(), taxTypePicker.getValue());
		}
	}

	private void setReceivers() {
		if (taxTypePicker.getValue() != null
				&& departmentReceiverPicker.getValue().iterator().hasNext()) {
			if (isForm) {
				getUiHandlers().getFormReceivers(
						departmentReceiverPicker.getValue().iterator().next(), taxTypePicker.getValue());
			} else {
				getUiHandlers().getDeclarationReceivers(
						departmentReceiverPicker.getValue().iterator().next(), taxTypePicker.getValue());
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

	@Override
	public void onValueChange(ValueChangeEvent<List<Integer>> event) {
		if (departmentSourcePicker == event.getSource()) {
			setSources();
		} else if (departmentReceiverPicker == event.getSource()) {
			setReceivers();
			currentSourcesTable.setRowCount(0);
		}
	}

}