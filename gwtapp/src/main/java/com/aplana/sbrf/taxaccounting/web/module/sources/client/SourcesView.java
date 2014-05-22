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
import com.aplana.sbrf.taxaccounting.web.widget.style.*;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
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
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;
import static java.util.Arrays.asList;

/**
 * Представление формы "Указаник источников/приемников"
 * @author many people
 */
public class SourcesView extends ViewWithUiHandlers<SourcesUiHandlers>
		implements SourcesPresenter.MyView, ValueChangeHandler<List<Integer>> {

    interface Binder extends UiBinder<Widget, SourcesView> {}

    public static final String TITLE_FORM = "Налоговые формы";
    public static final String TITLE_DEC = "Декларации";
    public static final String TITLE_FORM_DEAL = "Формы";
    public static final String TITLE_DEC_DEAL = "Уведомления";

    private GenericDataGrid.DataGridResizableHeader
            leftKindHeader,
            leftTypeHeader,
            leftDecTypeColumnHeader;

    private GenericDataGrid.DataGridResizableHeader
            rightKindHeader,
            rightTypeHeader;

    private GenericDataGrid.DataGridResizableHeader
            downKindHeader,
            downTypeHeader;

	private boolean isForm;
	private boolean canCancel;

	private Map<Integer, FormType> rightFormTypes;
	private Map<Integer, FormType> leftFormTypes;
	private Map<Integer, DeclarationType> leftDecTypes;

	private final SingleSelectionModel<DepartmentDeclarationType> leftDecSM = new SingleSelectionModel<DepartmentDeclarationType>();
	private final SingleSelectionModel<DepartmentFormType> leftFormSM = new SingleSelectionModel<DepartmentFormType>();
	private final MultiSelectionModel<DepartmentFormType> rightSM = new MultiSelectionModel<DepartmentFormType>(new ProvidesKey<DepartmentFormType>() {
        @Override
        public Object getKey(DepartmentFormType item) {
            return item.getId();
        }
    });

	@UiField(provided = true)
	ValueListBox<AppointmentType> appointmentTypePicker;

    @UiField(provided = true)
    ValueListBox<PeriodInfo>
            periodFrom,
            periodTo;
    @UiField
    Spinner yearFrom,
            yearTo;

	@UiField
	GenericDataGrid<DepartmentFormType>
            rightTable,
            leftFormTable;
	@UiField
	GenericDataGrid<DepartmentDeclarationType> leftDecTable;
	@UiField
	GenericDataGrid<DepartmentFormTypeShared> downTable;

	@UiField
	DepartmentPickerPopupWidget
            leftDepPicker,
            rightDepPicker;

	@UiField
    LinkButton
            assignButton,
            cancelButton,
            editButton;

    @UiField
    LinkAnchor formDecAnchor;
    @UiField
    Label titleLabel,
            taxTypeLabel,
            formDecLabel;

    @UiField
    ResizeLayoutPanel
            leftFormTableWrapper,
            leftDecTableWrapper;
    @UiField
    LabelSeparator
            downLabel,
            leftLabel,
            rightLabel;

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

		leftDepPicker.addValueChangeHandler(this);
		rightDepPicker.addValueChangeHandler(this);

		setupSourcesTables();
		setupReceiversTables();
	}

	@Override
	public void init(TaxType taxType, List<AppointmentType> types, AppointmentType type, int year, List<PeriodInfo> periods,
                     boolean isForm, Integer selectedReceiverId, Integer selectedSourceId) {
		this.isForm = isForm;

        leftFormTableWrapper.setVisible(isForm);
		leftDecTableWrapper.setVisible(!isForm);

//        //настрока листбоксов
        appointmentTypePicker.setValue(type);
        appointmentTypePicker.setAcceptableValues(types);
        WidgetUtils.setupOptionTitle(appointmentTypePicker);

        periodFrom.setAcceptableValues(periods);
        WidgetUtils.setupOptionTitle(periodFrom);
        periodFrom.setValue(periods.get(0));
        setupPeriodTitle(periodFrom);
        periodFrom.addValueChangeHandler(new ValueChangeHandler<PeriodInfo>() {
            @Override
            public void onValueChange(ValueChangeEvent<PeriodInfo> event) {
                setupPeriodTitle(periodFrom);
            }
        });

        periodTo.setAcceptableValues(periods);
        WidgetUtils.setupOptionTitle(periodTo);
        periodTo.setValue(periods.get(periods.size() - 1));
        setupPeriodTitle(periodTo);
        periodTo.addValueChangeHandler(new ValueChangeHandler<PeriodInfo>() {
            @Override
            public void onValueChange(ValueChangeEvent<PeriodInfo> event) {
                setupPeriodTitle(periodTo);
            }
        });

        yearFrom.setValue(year);
        yearTo.setValue(year);
        // Тип налога для ссылок
        updateLinks(taxType, selectedReceiverId, selectedSourceId);

        // Подразделение-приемник
        if (selectedReceiverId == null) {
		    leftDepPicker.setValue(null);
        } else {
            leftDepPicker.setValue(asList(selectedReceiverId), true);
        }
        // Подразделение-источник
        if (selectedSourceId == null) {
            rightDepPicker.setValue(null);
        } else {
            rightDepPicker.setValue(asList(selectedSourceId), true);
        }
        assignButton.setEnabled(false);
        cancelButton.setEnabled(false);
        editButton.setEnabled(false);

		rightTable.setRowCount(0);
		leftFormTable.setRowCount(0);
		leftDecTable.setRowCount(0);
		downTable.setRowCount(0);

        boolean isTaxTypeDeal = taxType.equals(TaxType.DEAL);
        String kindColumnTitle = getKindColumnTitle(isTaxTypeDeal);
        String typeColumnTitle = getTypeColumnTitle(isTaxTypeDeal);

        leftKindHeader.setTitle(kindColumnTitle);
        leftTypeHeader.setTitle(typeColumnTitle);
        leftDecTypeColumnHeader.setTitle(!isTaxTypeDeal ? "Вид декларации" : "Вид уведомления");

        rightKindHeader.setTitle(kindColumnTitle);
        rightTypeHeader.setTitle(typeColumnTitle);

        downKindHeader.setTitle(kindColumnTitle);
        downTypeHeader.setTitle(typeColumnTitle);

        leftFormTable.redrawHeaders();
        rightTable.redrawHeaders();
        downTable.redrawHeaders();
        leftDecTable.redrawHeaders();

        taxTypeLabel.setText(taxType.getName());

        formDecAnchor.setText(isForm ?
                (isTaxTypeDeal ? TITLE_DEC_DEAL : TITLE_DEC) :
                (isTaxTypeDeal ? TITLE_FORM_DEAL : TITLE_FORM));

        formDecLabel.setText(!isForm ?
                (isTaxTypeDeal ? TITLE_DEC_DEAL : TITLE_DEC) :
                (isTaxTypeDeal ? TITLE_FORM_DEAL : TITLE_FORM));
    }

    private void setupPeriodTitle(ValueListBox<PeriodInfo> widget) {
        widget.setTitle(widget.getValue() != null ? widget.getValue().getName() : "");
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
		rightFormTypes = formTypes;
		rightTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setAvalibleFormReceivers(Map<Integer, FormType> formTypes, List<DepartmentFormType> departmentFormTypes) {
		leftFormTypes = formTypes;
		leftFormTable.setRowData(departmentFormTypes);
	}

	@Override
	public void setAvalibleDeclarationReceivers(Map<Integer, DeclarationType> declarationTypes,
										List<DepartmentDeclarationType> departmentDeclarationTypes) {
		leftDecTypes = declarationTypes;
		leftDecTable.setRowData(departmentDeclarationTypes);
	}

	@Override
	public void setCurrentSources(List<DepartmentFormTypeShared> departmentFormTypes) {
		downTable.setRowData(departmentFormTypes);
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
		leftDepPicker.setAvalibleValues(departments, availableDepartments);
		leftDepPicker.setValue(null);
		rightDepPicker.setAvalibleValues(departments, availableDepartments);
		rightDepPicker.setValue(null);
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
                if (!leftFormTypes.isEmpty() && object.getFormTypeId() != 0) {
                    return leftFormTypes.get(object.getFormTypeId()).getName();
                } else {
                    return "";
                }
            }
        };

        leftKindHeader = leftFormTable.createResizableHeader("Тип налоговой формы", receiverKindColumn);
        leftFormTable.addColumn(receiverKindColumn, leftKindHeader);
		//leftFormTable.setColumnWidth(receiverKindColumn, 150, Style.Unit.PX);

        leftTypeHeader = leftFormTable.createResizableHeader("Вид налоговой формы", receiverTypeColumn);
		leftFormTable.addColumn(receiverTypeColumn, leftTypeHeader);

		leftFormTable.setSelectionModel(leftFormSM);
		leftFormSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                getUiHandlers().getFormReceiverSources(leftFormSM.getSelectedObject());
                if (rightSM.getSelectedSet().size() > 0) {
                    assignButton.setEnabled(false);
                }
            }
        });

        // Declaration Receivers
        TextColumn<DepartmentDeclarationType> declarationReceiverTypeColumn = new TextColumn<DepartmentDeclarationType>() {
            @Override
            public String getValue(DepartmentDeclarationType object) {
                if (!leftDecTypes.isEmpty() && object.getDeclarationTypeId() != 0) {
                    return leftDecTypes.get(object.getDeclarationTypeId()).getName();
                } else {
                    return "";
                }
            }
        };

        leftDecTypeColumnHeader = leftDecTable.createResizableHeader("Вид декларации", declarationReceiverTypeColumn);
		leftDecTable.addColumn(declarationReceiverTypeColumn, leftDecTypeColumnHeader);
		leftDecTable.setSelectionModel(leftDecSM);
		leftDecSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                getUiHandlers().getDeclarationReceiverSources(leftDecSM.getSelectedObject());
                if (rightSM.getSelectedSet().size() > 0) {
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
				if (!rightFormTypes.isEmpty() && object.getFormTypeId() != 0) {
					return rightFormTypes.get(object.getFormTypeId()).getName();
				} else {
					return "";
				}
			}
		};
        Column<DepartmentFormType, Boolean> cbColumn = new Column<DepartmentFormType, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(DepartmentFormType object) {
                return (object == null || object.getId() == null) ? null : rightSM.isSelected(object);
            }
        };
        final CheckBoxHeader checkBoxHeader = new CheckBoxHeader();
        checkBoxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if(event.getValue()){
                    for (DepartmentFormType departmentFormType : rightTable.getVisibleItems()) {
                        rightSM.setSelected(departmentFormType, true);
                    }
                } else {
                    rightSM.clear();
                }
            }
        });
        rightSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                checkBoxHeader.setValue(rightSM.getSelectedSet().size() == rightTable.getRowCount());
            }
        });

        rightTable.addColumn(cbColumn, checkBoxHeader);
        rightTable.setColumnWidth(cbColumn, 3, Style.Unit.EM);

        rightKindHeader = rightTable.createResizableHeader("Тип налоговой формы", sourceKindColumn);
		rightTable.addColumn(sourceKindColumn, rightKindHeader);
		rightTable.setColumnWidth(sourceKindColumn, 110, Style.Unit.PX);

        rightTypeHeader = rightTable.createResizableHeader("Вид налоговой формы", sourceTypeColumn);
		rightTable.addColumn(sourceTypeColumn, rightTypeHeader);
        //rightTable.setColumnWidth(sourceTypeColumn, 70, Style.Unit.PCT);

        rightTable.setSelectionModel(rightSM, createCustomManager(
                new DefaultSelectionEventManager.CheckboxEventTranslator<DepartmentFormType>(0) {
                    public boolean clearCurrentSelection(CellPreviewEvent<DepartmentFormType> event) {
                        return false;
                    }

                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<DepartmentFormType> event) {
                        return DefaultSelectionEventManager.SelectAction.TOGGLE;
                    }
                }));
        rightSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                assignButton.setEnabled(!rightSM.getSelectedSet().isEmpty());
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
                downTable.getVisibleItem(index).setChecked(value);
                for (DepartmentFormTypeShared source : downTable.getVisibleItems()) {
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

		downTable.addColumn(checkBoxColumn);
		downTable.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);
		downTable.addColumn(indexColumn, "№ пп");
		downTable.setColumnWidth(indexColumn, 40, Style.Unit.PX);
		downTable.addResizableColumn(departmentColumn, "Подразделение");
		downTable.setColumnWidth(departmentColumn, 250, Style.Unit.PX);
        downKindHeader = downTable.createResizableHeader("Тип налоговой формы", receiverSourcesKindColumn);
		downTable.addColumn(receiverSourcesKindColumn, downKindHeader);
		downTable.setColumnWidth(receiverSourcesKindColumn, 150, Style.Unit.PX);
        downTypeHeader = downTable.createResizableHeader("Вид налоговой формы", receiverSourcesTypeColumn);
		downTable.addColumn(receiverSourcesTypeColumn, downTypeHeader);
	}

	@UiHandler("assignButton")
	public void assign(ClickEvent event) {
        if ((leftFormSM.getSelectedObject() == null &&
                leftDecSM.getSelectedObject() == null) ||
                rightSM.getSelectedSet().isEmpty()) {
            return;
        }

		Set<Long> rightSelectIds = new HashSet<Long>();
        Set<Long> currenAssignIds = new HashSet<Long>();
        for (DepartmentFormType selectedDepFormType : rightSM.getSelectedSet()) {
            rightSelectIds.add(selectedDepFormType.getId());
        }
		for (DepartmentFormTypeShared source : downTable.getVisibleItems()) {
            currenAssignIds.add(source.getId());
		}
        rightSelectIds.addAll(currenAssignIds);
//        if (rightSelectIds.isEmpty()) {
//            Dialog.warningMessage("Выбранные назначения налоговой формы уже является источниками " +
//                    "для выбранного приемника!");
//        } else {
            if (isForm) {
                System.out.println("isForm" + isForm +"");
                getUiHandlers().updateFormSources(leftFormSM.getSelectedObject(), new ArrayList<Long>(rightSelectIds));
            } else {
                System.out.println("isForm" + false +"");
                getUiHandlers().updateDeclarationSources(leftDecSM.getSelectedObject(), new ArrayList<Long>(rightSelectIds));
            }
        //}
    }

	@UiHandler("cancelButton")
	public void cancel(ClickEvent event) {
		if (!canCancel || (leftFormSM.getSelectedObject() == null &&
				leftDecSM.getSelectedObject() == null)) {
			return;
		}

		List<Long> sourceIds = new ArrayList<Long>();
		for (DepartmentFormTypeShared source: downTable.getVisibleItems()) {
			if (!source.isChecked()) {
				sourceIds.add(source.getId());
			}
		}

		if (isForm) {
			getUiHandlers().updateFormSources(leftFormSM.getSelectedObject(), sourceIds);
		} else {
			getUiHandlers().updateDeclarationSources(leftDecSM.getSelectedObject(), sourceIds);
		}

	}

    @UiHandler("appointmentTypePicker")
    public void change(ValueChangeEvent<AppointmentType> event) {
        if(event.getValue().equals(AppointmentType.RECIPIENT)){
            leftLabel.setText("Источник");
            rightLabel.setText("Приемники");
            downLabel.setText("Указанные приемники");
        } else {
            leftLabel.setText("Приемник");
            rightLabel.setText("Источники");
            downLabel.setText("Указанные источники");
        }
    }

	@Override
	public void onValueChange(ValueChangeEvent<List<Integer>> event) {
        Integer selectedReceiverId = getSelectedDepId(leftDepPicker);
        Integer selectedSourceId = getSelectedDepId(rightDepPicker);

        updateLinks(getUiHandlers().getTaxType(), selectedReceiverId, selectedSourceId);

        if (rightDepPicker == event.getSource()) {
            getUiHandlers().getFormSources(selectedSourceId);
		} else if (leftDepPicker == event.getSource()) {
            if (isForm) {
				getUiHandlers().getFormReceivers(selectedReceiverId);
			} else {
				getUiHandlers().getDeclarationReceivers(selectedReceiverId);
			}
			downTable.setRowCount(0);
		}
	}
    //TODO aivanov сделать для депПикера метод для взятия единственного значения
    private Integer getSelectedDepId(DepartmentPickerPopupWidget dPicker){
        Iterator<Integer> depPickIterator = dPicker.getValue().iterator();
        if (dPicker.getValue() != null && depPickIterator.hasNext()) {
            return depPickIterator.next();
        }
        return null;
    }

    @Override
    public Map<Integer, FormType> getRightFormTypes() {
        return rightFormTypes;
    }
    @Override
    public Map<Integer, FormType> getLeftFormTypes() {
        return leftFormTypes;
    }
    @Override
    public Map<Integer, DeclarationType> getLeftDecTypes() {
        return leftDecTypes;
    }

    String getTypeColumnTitle(boolean isTaxTypeDeal) {
        return (!isTaxTypeDeal ? "Вид налоговой формы" : "Вид формы");
    }

    String getKindColumnTitle(boolean isTaxTypeDeal) {
        return (!isTaxTypeDeal ? "Тип налоговой формы" : "Тип формы");
    }
}