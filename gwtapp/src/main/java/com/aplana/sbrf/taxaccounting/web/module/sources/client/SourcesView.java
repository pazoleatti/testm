package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import java.util.*;

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
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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

import static java.util.Arrays.asList;

public class SourcesView extends ViewWithUiHandlers<SourcesUiHandlers>
		implements SourcesPresenter.MyView, ValueChangeHandler<List<Integer>> {

    public static final String TITLE_FORM = "Налоговые формы";
    public static final String TITLE_DEC = "Декларации";
    public static final String TITLE_FORM_DEAL = "Формы";
    public static final String TITLE_DEC_DEAL = "Уведомления";

    private GenericDataGrid.DataGridResizableHeader
            receiversKindHeader,
            receiversTypeHeader,
            receiverDecTypeColumnHeader;

    private GenericDataGrid.DataGridResizableHeader
            sourcesKindTitle,
            sourcesTypeTitle;

    private GenericDataGrid.DataGridResizableHeader
            receiverSourcesKindTitle,
            receiverSourcesTypeTitle;

    interface Binder extends UiBinder<Widget, SourcesView> { }

	private boolean isForm;
	private boolean canCancel;

	private Map<Integer, FormType> sourcesFormTypes;
	private Map<Integer, FormType> receiversFormTypes;
	private Map<Integer, DeclarationType> receiversDeclarationTypes;

	private final SingleSelectionModel<DepartmentDeclarationType> receiversDecSM = new SingleSelectionModel<DepartmentDeclarationType>();
	private final SingleSelectionModel<DepartmentFormType> receiversFormSM = new SingleSelectionModel<DepartmentFormType>();
	private final SingleSelectionModel<DepartmentFormType> sourcesSM = new SingleSelectionModel<DepartmentFormType>();

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
	GenericDataGrid<DepartmentFormType> receiversFormTable;
	@UiField
	GenericDataGrid<DepartmentDeclarationType> receiversDecTable;
	@UiField
	GenericDataGrid<DepartmentFormTypeShared> currentSourcesTable;

	@UiField
	DepartmentPickerPopupWidget receiversDepPicker;
	@UiField
	DepartmentPickerPopupWidget sourcesDepPicker;

	@UiField
    LinkButton assignButton;
	@UiField
    LinkButton cancelButton;

    @UiField
    LinkAnchor formDecAnchor;
    @UiField
    Label titleLabel;
    @UiField
    Label taxTypeLabel;
    @UiField
    Label formDecLabel;

    @UiField
    ResizeLayoutPanel receiversFormTableWrapper;
    @UiField
    ResizeLayoutPanel receiversDecTableWrapper;

    @Inject
	@UiConstructor
	public SourcesView(final Binder uiBinder) {
        appointmentTypePicker = new ValueListBox<AppointmentType>(new AbstractRenderer<AppointmentType>() {
			@Override
			public String render(AppointmentType object) {
                return object == null ? "" : object.getName();
            }
		});
        periodFrom = new ValueListBox<PeriodInfo>(new AbstractRenderer<PeriodInfo>() {
            @Override
            public String render(PeriodInfo object) {
                return object == null ? "" : object.getName();
            }
        });
        periodTo = new ValueListBox<PeriodInfo>(new AbstractRenderer<PeriodInfo>() {
            @Override
            public String render(PeriodInfo object) {
                return object == null ? "" : object.getName();
            }
        });

		initWidget(uiBinder.createAndBindUi(this));

		receiversDepPicker.addValueChangeHandler(this);
		sourcesDepPicker.addValueChangeHandler(this);

		setupSourcesTables();
		setupReceiversTables();
	}

	@Override
	public void init(TaxType taxType, List<AppointmentType> types, AppointmentType type, int year, List<PeriodInfo> periods,
                     boolean isForm, Integer selectedReceiverId, Integer selectedSourceId) {
		this.isForm = isForm;

        receiversFormTableWrapper.setVisible(isForm);
		receiversDecTableWrapper.setVisible(!isForm);
        appointmentTypePicker.setValue(type);
        appointmentTypePicker.setAcceptableValues(types);
        WidgetUtils.setupOptionTitle(appointmentTypePicker);

        periodFrom.setValue(periods.get(0));
        periodFrom.setAcceptableValues(periods);
        WidgetUtils.setupOptionTitle(periodFrom);

        periodTo.setValue(periods.get(0));
        periodTo.setAcceptableValues(periods);
        WidgetUtils.setupOptionTitle(periodTo);

        yearFrom.setValue(year);
        yearTo.setValue(year + 12);
        // Тип налога для ссылок
        updateLinks(taxType, selectedReceiverId, selectedSourceId);

        // Подразделение-приемник
        if (selectedReceiverId == null) {
		    receiversDepPicker.setValue(null);
        } else {
            receiversDepPicker.setValue(asList(selectedReceiverId), true);
        }
        // Подразделение-источник
        if (selectedSourceId == null) {
            sourcesDepPicker.setValue(null);
        } else {
            sourcesDepPicker.setValue(asList(selectedSourceId), true);
        }
        assignButton.setEnabled(false);
        cancelButton.setEnabled(false);

		sourcesTable.setRowCount(0);
		receiversFormTable.setRowCount(0);
		receiversDecTable.setRowCount(0);
		currentSourcesTable.setRowCount(0);

        boolean isTaxTypeDeal = taxType.equals(TaxType.DEAL);

        receiversKindHeader.setTitle(getKindColumnTitle(isTaxTypeDeal));
        receiversTypeHeader.setTitle(getTypeColumnTitle(isTaxTypeDeal));
        receiverDecTypeColumnHeader.setTitle(!isTaxTypeDeal ? "Вид декларации" : "Вид уведомления");

        sourcesKindTitle.setTitle(getKindColumnTitle(isTaxTypeDeal));
        sourcesTypeTitle.setTitle(getTypeColumnTitle(isTaxTypeDeal));

        receiverSourcesKindTitle.setTitle(getKindColumnTitle(isTaxTypeDeal));
        receiverSourcesTypeTitle.setTitle(getTypeColumnTitle(isTaxTypeDeal));

        receiversFormTable.redrawHeaders();
        sourcesTable.redrawHeaders();
        currentSourcesTable.redrawHeaders();
        receiversDecTable.redrawHeaders();

        taxTypeLabel.setText(taxType.getName());

        formDecAnchor.setText(isForm ?
                (isTaxTypeDeal ? TITLE_DEC_DEAL : TITLE_DEC) :
                (isTaxTypeDeal ? TITLE_FORM_DEAL : TITLE_FORM));

        formDecLabel.setText((isForm ?
                (isTaxTypeDeal ? TITLE_FORM_DEAL : TITLE_FORM) :
                (isTaxTypeDeal ? TITLE_DEC_DEAL : TITLE_DEC)));

        // TODO Установка фокуса на receiversDepPicker после реализации метода в http://jira.aplana.com/browse/SBRFACCTAX-5392
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
		receiversFormTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setAvalibleDeclarationReceivers(Map<Integer, DeclarationType> declarationTypes,
										List<DepartmentDeclarationType> departmentDeclarationTypes) {
		receiversDeclarationTypes = declarationTypes;
		receiversDecTable.setRowData(departmentDeclarationTypes);
	}

	@Override
	public void setCurrentSources(List<DepartmentFormTypeShared> departmentFormTypes) {
		currentSourcesTable.setRowData(departmentFormTypes);
        cancelButton.setEnabled(false);
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
		receiversDepPicker.setAvalibleValues(departments, availableDepartments);
		receiversDepPicker.setValue(null);
		sourcesDepPicker.setAvalibleValues(departments, availableDepartments);
		sourcesDepPicker.setValue(null);
	}

	public void setupReceiversTables() {
		// Form Receivers
		TextColumn<DepartmentFormType> receiverKindColumn = new TextColumn<DepartmentFormType>() {
			@Override
			public String getValue(DepartmentFormType object) {
                return object.getKind() != null ? object.getKind().getName() : "";
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

        receiversKindHeader = receiversFormTable.createResizableHeader("Тип налоговой формы", receiverKindColumn);
        receiversFormTable.addColumn(receiverKindColumn, receiversKindHeader);
		receiversFormTable.setColumnWidth(receiverKindColumn, 150, Style.Unit.PX);

        receiversTypeHeader = receiversFormTable.createResizableHeader("Вид налоговой формы", receiverTypeColumn);
		receiversFormTable.addColumn(receiverTypeColumn, receiversTypeHeader);

		receiversFormTable.setSelectionModel(receiversFormSM);
		receiversFormSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                getUiHandlers().getFormReceiverSources(receiversFormSM.getSelectedObject());
                if (sourcesSM.getSelectedObject() != null) {
                    assignButton.setEnabled(false);
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

        receiverDecTypeColumnHeader = receiversDecTable.createResizableHeader("Вид декларации", declarationReceiverTypeColumn);
		receiversDecTable.addColumn(declarationReceiverTypeColumn, receiverDecTypeColumnHeader);
		receiversDecTable.setSelectionModel(receiversDecSM);
		receiversDecSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                getUiHandlers().getDeclarationReceiverSources(receiversDecSM.getSelectedObject());
                if (sourcesSM.getSelectedObject() != null) {
                    assignButton.setEnabled(true);
                }
            }
        });
	}

	private void setupSourcesTables() {
		// Sources
		TextColumn<DepartmentFormType> sourceKindColumn = new TextColumn<DepartmentFormType>() {
			@Override
			public String getValue(DepartmentFormType object) {
                return object.getKind() != null ? object.getKind().getName() : "";
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

        sourcesKindTitle = sourcesTable.createResizableHeader("Тип налоговой формы", sourceKindColumn);
		sourcesTable.addColumn(sourceKindColumn, sourcesKindTitle);
		sourcesTable.setColumnWidth(sourceKindColumn, 150, Style.Unit.PX);

        sourcesTypeTitle = sourcesTable.createResizableHeader("Вид налоговой формы", sourceTypeColumn);
		sourcesTable.addColumn(sourceTypeColumn, sourcesTypeTitle);
		sourcesTable.setSelectionModel(sourcesSM);
		sourcesSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                assignButton.setEnabled(true);
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
                cancelButton.setEnabled(false);
				currentSourcesTable.getVisibleItem(index).setChecked(value);
				for(DepartmentFormTypeShared source : currentSourcesTable.getVisibleItems()) {
					if (source.isChecked()) {
                        cancelButton.setEnabled(true);
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
		if ((receiversFormSM.getSelectedObject() == null &&
				receiversDecSM.getSelectedObject() == null) ||
				sourcesSM.getSelectedObject() == null) {
			return;
		}

		List<Long> sourceIds = new ArrayList<Long>();
		Long sourceId = sourcesSM.getSelectedObject().getId();

		for (DepartmentFormTypeShared source : currentSourcesTable.getVisibleItems()) {
			if (sourceId.equals(source.getId())) {
				getUiHandlers().showAssignErrorMessage();
				return;
			}
			sourceIds.add(source.getId());
		}

		sourceIds.add(sourceId);

		if (isForm) {
			getUiHandlers().updateFormSources(receiversFormSM.getSelectedObject(), sourceIds);
		} else {
			getUiHandlers().updateDeclarationSources(receiversDecSM.getSelectedObject(), sourceIds);
		}
	}

	@UiHandler("cancelButton")
	public void cancel(ClickEvent event) {
		if (!canCancel || (receiversFormSM.getSelectedObject() == null &&
				receiversDecSM.getSelectedObject() == null)) {
			return;
		}

		List<Long> sourceIds = new ArrayList<Long>();
		for (DepartmentFormTypeShared source: currentSourcesTable.getVisibleItems()) {
			if (!source.isChecked()) {
				sourceIds.add(source.getId());
			}
		}

		if (isForm) {
			getUiHandlers().updateFormSources(receiversFormSM.getSelectedObject(), sourceIds);
		} else {
			getUiHandlers().updateDeclarationSources(receiversDecSM.getSelectedObject(), sourceIds);
		}

	}

	@Override
	public void onValueChange(ValueChangeEvent<List<Integer>> event) {
        Integer selectedReceiverId = null;
        Integer selectedSourceId = null;

        Iterator<Integer> receiverDepPickIterator = receiversDepPicker.getValue().iterator();
        if (receiversDepPicker.getValue() != null && receiverDepPickIterator.hasNext()) {
            selectedReceiverId = receiverDepPickIterator.next();
        }
        Iterator<Integer> sourcesDepPickIterator = sourcesDepPicker.getValue().iterator();
        if (sourcesDepPicker.getValue() != null && sourcesDepPickIterator.hasNext()) {
            selectedSourceId = sourcesDepPickIterator.next();
        }

        updateLinks(getUiHandlers().getTaxType(), selectedReceiverId, selectedSourceId);

        if (sourcesDepPicker == event.getSource()) {
            getUiHandlers().getFormSources(selectedSourceId);
		} else if (receiversDepPicker == event.getSource()) {
            if (isForm) {
				getUiHandlers().getFormReceivers(selectedReceiverId);
			} else {
				getUiHandlers().getDeclarationReceivers(selectedReceiverId);
			}
			currentSourcesTable.setRowCount(0);
		}
	}

    @Override
    public Map<Integer, FormType> getSourcesFormTypes() {
        return sourcesFormTypes;
    }
    @Override
    public Map<Integer, FormType> getReceiversFormTypes() {
        return receiversFormTypes;
    }
    @Override
    public Map<Integer, DeclarationType> getReceiversDeclarationTypes() {
        return receiversDeclarationTypes;
    }

    String getTypeColumnTitle(boolean isTaxTypeDeal) {
        return (!isTaxTypeDeal ? "Вид налоговой формы" : "Вид формы");
    }

    String getKindColumnTitle(boolean isTaxTypeDeal) {
        return (!isTaxTypeDeal ? "Тип налоговой формы" : "Тип формы");
    }
}