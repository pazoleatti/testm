package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.gwt.client.Spinner;
import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.AppointmentType;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentFormTypeShared;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static java.util.Arrays.asList;

public class SourcesView extends ViewWithUiHandlers<SourcesUiHandlers>
		implements SourcesPresenter.MyView, ValueChangeHandler<List<Integer>> {

    public static final String FORM_DEC_ANCHOR_TITLE = "Указание форм источников для деклараций";
    public static final String FORM_DEC_ANCHOR_TITLE_D = "Указание форм источников для уведомлений";
    public static final String FORM_DEC_ANCHOR_TITLE_DEC = "Указание источников для налоговых форм";
    public static final String FORM_DEC_ANCHOR_TITLE_DEC_D = "Указание источников для форм";

    public static final String FORM_TITLE = "Налоговые формы";
    public static final String FORM_TITLE_DEC  = "Декларации";
    public static final String FORM_TITLE_D = "Формы";
    public static final String FORM_TITLE_DEC_D = "Уведомления";

    private GenericDataGrid.DataGridResizableHeader receiverKindHeader, receiverTypeHeader, declarationReceiverTypeColumnHeader;

    private GenericDataGrid.DataGridResizableHeader sourceKindTitle, sourceTypeTitle;

    private GenericDataGrid.DataGridResizableHeader receiverSourcesKindTitle, receiverSourcesTypeTitle;

    interface Binder extends UiBinder<Widget, SourcesView> { }

	interface LinkStyle extends CssResource {
		String enabled();
		String disabled();
	}

	private boolean isForm;
	private boolean canCancel;

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
	ValueListBox<AppointmentType> appointmentTypePicker;
    @UiField(provided = true)
    ValueListBox<PeriodInfo> periodFrom;
    @UiField
    Spinner yearFrom;
    @UiField(provided = true)
    ValueListBox<PeriodInfo> periodTo;
    @UiField
    Spinner yearTo;

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
	Anchor assignButton;
	@UiField
	Anchor cancelButton;

    @UiField
    LinkAnchor formDecAnchor;
    @UiField
    Label titleLabel;

    @Inject
	@UiConstructor
	public SourcesView(final Binder uiBinder) {
        appointmentTypePicker = new ValueListBox<AppointmentType>(new AbstractRenderer<AppointmentType>() {
			@Override
			public String render(AppointmentType object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});
        periodFrom = new ValueListBox<PeriodInfo>(new AbstractRenderer<PeriodInfo>() {
            @Override
            public String render(PeriodInfo object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });
        periodTo = new ValueListBox<PeriodInfo>(new AbstractRenderer<PeriodInfo>() {
            @Override
            public String render(PeriodInfo object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

		initWidget(uiBinder.createAndBindUi(this));

		departmentReceiverPicker.addValueChangeHandler(this);
		departmentSourcePicker.addValueChangeHandler(this);

		setupSourcesTables();
		setupReceiversTables();
	}

	@Override
	public void init(TaxType taxType, List<AppointmentType> types, AppointmentType type, int year, List<PeriodInfo> periods,
                     boolean isForm, Integer selectedReceiverId, Integer selectedSourceId) {
		this.isForm = isForm;

        formReceiversTable.setVisible(isForm);
		declarationReceiversTable.setVisible(!isForm);
        appointmentTypePicker.setValue(type);
        appointmentTypePicker.setAcceptableValues(types);
        periodFrom.setValue(periods.get(0));
        periodFrom.setAcceptableValues(periods);
        periodTo.setValue(periods.get(0));
        periodTo.setAcceptableValues(periods);
        yearFrom.setValue(year);
        yearTo.setValue(year + 12);
        // Тип налога для ссылок
        updateLinks(taxType, selectedReceiverId, selectedSourceId);

        // Подразделение-приемник
        if (selectedReceiverId == null) {
		    departmentReceiverPicker.setValue(null);
        } else {
            departmentReceiverPicker.setValue(asList(selectedReceiverId), true);
        }
        // Подразделение-источник
        if (selectedSourceId == null) {
            departmentSourcePicker.setValue(null);
        } else {
            departmentSourcePicker.setValue(asList(selectedSourceId), true);
        }

		enableButtonLink(assignButton, false);
		enableButtonLink(cancelButton, false);

		sourcesTable.setRowCount(0);
		formReceiversTable.setRowCount(0);
		declarationReceiversTable.setRowCount(0);
		currentSourcesTable.setRowCount(0);

        boolean isTaxTypeDeal = taxType.equals(TaxType.DEAL);

        receiverKindHeader.setTitle(getKindColumnTitle(isTaxTypeDeal));
        receiverTypeHeader.setTitle(getTypeColumnTitle(isTaxTypeDeal));
        declarationReceiverTypeColumnHeader.setTitle(!isTaxTypeDeal?"Вид декларации":"Вид уведомления");

        sourceKindTitle.setTitle(getKindColumnTitle(isTaxTypeDeal));
        sourceTypeTitle.setTitle(getTypeColumnTitle(isTaxTypeDeal));

        receiverSourcesKindTitle.setTitle(getKindColumnTitle(isTaxTypeDeal));
        receiverSourcesTypeTitle.setTitle(getTypeColumnTitle(isTaxTypeDeal));

        formReceiversTable.redrawHeaders();
        sourcesTable.redrawHeaders();
        currentSourcesTable.redrawHeaders();
        declarationReceiversTable.redrawHeaders();

        if (!isTaxTypeDeal) {
            if (isForm) {
                formDecAnchor.setText(FORM_DEC_ANCHOR_TITLE);
                titleLabel.setText(FORM_TITLE);
            } else {
                formDecAnchor.setText(FORM_DEC_ANCHOR_TITLE_DEC);
                titleLabel.setText(FORM_TITLE_DEC);
            }
        } else {
            if (isForm) {
                formDecAnchor.setText(FORM_DEC_ANCHOR_TITLE_D);
                titleLabel.setText(FORM_TITLE_D);
            } else {
                formDecAnchor.setText(FORM_DEC_ANCHOR_TITLE_DEC_D);
                titleLabel.setText(FORM_TITLE_DEC_D);
            }
        }
        // TODO Установка фокуса на departmentReceiverPicker после реализации метода в http://jira.aplana.com/browse/SBRFACCTAX-5392
	}

    /**
     * Подготовка ссылок
     * @param taxType
     * @param selectedReceiverId
     * @param selectedSourceId
     */
    private void updateLinks(TaxType taxType, Integer selectedReceiverId, Integer selectedSourceId) {
        String receiverStr = selectedReceiverId == null ? "" : ";dst=" + selectedReceiverId;
        String sourceStr = selectedSourceId == null ? "" : ";src=" + selectedSourceId;
        String href = "#!sources;nType=" + taxType.name() + receiverStr + sourceStr + ";isForm=" + !isForm;
        formDecAnchor.setHref(href);
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
    public PeriodInfo getPeriodFrom() {
        return periodFrom.getValue();
    }

    @Override
    public PeriodInfo getPeriodTo() {
        return periodTo.getValue();
    }

    @Override
    public int getYearFrom() {
        return yearFrom.getValue();
    }

    @Override
    public int getYearTo() {
        return yearTo.getValue();
    }

    @Override
	public void setDepartments(List<Department> departments, Set<Integer> availableDepartments) {
		departmentReceiverPicker.setAvalibleValues(departments, availableDepartments);
		departmentReceiverPicker.setValue(null);
		departmentSourcePicker.setAvalibleValues(departments, availableDepartments);
		departmentSourcePicker.setValue(null);
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

        receiverKindHeader = formReceiversTable.createResizableHeader("Тип налоговой формы", receiverKindColumn);
        formReceiversTable.addColumn(receiverKindColumn, receiverKindHeader);
		formReceiversTable.setColumnWidth(receiverKindColumn, 150, Style.Unit.PX);

        receiverTypeHeader = formReceiversTable.createResizableHeader("Вид налоговой формы", receiverTypeColumn);
		formReceiversTable.addColumn(receiverTypeColumn, receiverTypeHeader);

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

        declarationReceiverTypeColumnHeader = declarationReceiversTable.createResizableHeader("Вид декларации", declarationReceiverTypeColumn);
		declarationReceiversTable.addColumn(declarationReceiverTypeColumn, declarationReceiverTypeColumnHeader);
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

        sourceKindTitle = sourcesTable.createResizableHeader("Тип налоговой формы", sourceKindColumn);
		sourcesTable.addColumn(sourceKindColumn, sourceKindTitle);
		sourcesTable.setColumnWidth(sourceKindColumn, 150, Style.Unit.PX);

        sourceTypeTitle = sourcesTable.createResizableHeader("Вид налоговой формы", sourceTypeColumn);
		sourcesTable.addColumn(sourceTypeColumn, sourceTypeTitle);
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
		currentSourcesTable.addResizableColumn(departmentColumn, "Подразделение");
		currentSourcesTable.setColumnWidth(departmentColumn, 250, Style.Unit.PX);
        receiverSourcesKindTitle = currentSourcesTable.createResizableHeader("Тип налоговой формы", receiverSourcesKindColumn);
		currentSourcesTable.addColumn(receiverSourcesKindColumn, receiverSourcesKindTitle);
		currentSourcesTable.setColumnWidth(receiverSourcesKindColumn, 150, Style.Unit.PX);
        receiverSourcesTypeTitle = currentSourcesTable.createResizableHeader("Вид налоговой формы", receiverSourcesTypeColumn);
		currentSourcesTable.addColumn(receiverSourcesTypeColumn, receiverSourcesTypeTitle);
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
				getUiHandlers().showAssignErrorMessage();
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

	private void enableButtonLink(Anchor anchor, boolean enabled) {
        anchor.setStyleName(enabled ? css.enabled() : css.disabled());
	}

	@Override
	public void onValueChange(ValueChangeEvent<List<Integer>> event) {
        Integer selectedReceiverId = null;
        Integer selectedSourceId = null;

        if (departmentReceiverPicker.getValue() != null && departmentReceiverPicker.getValue().iterator().hasNext()) {
            selectedReceiverId = departmentReceiverPicker.getValue().iterator().next();
        }

        if (departmentSourcePicker.getValue() != null && departmentSourcePicker.getValue().iterator().hasNext()) {
            selectedSourceId = departmentSourcePicker.getValue().iterator().next();
        }

        updateLinks(getUiHandlers().getTaxType(), selectedReceiverId, selectedSourceId);

        if (departmentSourcePicker == event.getSource()) {
            getUiHandlers().getFormSources(selectedSourceId);
		} else if (departmentReceiverPicker == event.getSource()) {
            if (isForm) {
				getUiHandlers().getFormReceivers(selectedReceiverId);
			} else {
				getUiHandlers().getDeclarationReceivers(selectedReceiverId);
			}
			currentSourcesTable.setRowCount(0);
		}
	}

    String getTypeColumnTitle(boolean isTaxTypeDeal) {
        return (!isTaxTypeDeal?"Вид налоговой формы":"Вид формы");
    }

    String getKindColumnTitle(boolean isTaxTypeDeal) {
        return (!isTaxTypeDeal?"Тип налоговой формы":"Тип формы");
    }
}