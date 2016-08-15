package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.LockInfo;
import com.aplana.sbrf.taxaccounting.web.widget.cell.IndexCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomTableBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.CheckHandler;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.DropdownButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Вьюха конкретной формы с данными
 *
 * @author unknown
 */
public class FormDataView extends ViewWithUiHandlers<FormDataUiHandlers>
		implements FormDataPresenterBase.MyView {

	private SingleSelectionModel<DataRow<Cell>> singleSelectionModel;

    public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы:";
    public static final String FORM_DATA_KIND_TITLE_D = "Тип формы:";

    private TaxType taxType;
    private boolean fixedRows;
    // флаг что нужно заскролить к выделенной строке, используется для формы поиска
    private Integer needScrollToRow = null;

    interface Binder extends UiBinder<Widget, FormDataView> {
	}

	private DataRowColumnFactory factory = new DataRowColumnFactory();

	private AsyncDataProvider<DataRow<Cell>> dataProvider = new AsyncDataProvider<DataRow<Cell>>() {
		@Override
		protected void onRangeChanged(HasData<DataRow<Cell>> display) {
			Range range = display.getVisibleRange();
			getUiHandlers().onRangeChange(range.getStart(), range.getLength());
		}
	};

    /*
    * Провайдер для идентификации конкретноого объекта в строке
    * С помощью провайдера при листании селектшнМодел понимает что
    * за объект был выделе или развыделен
    */
    public static final ProvidesKey<DataRow<Cell>> KEY_PROVIDER = new ProvidesKey<DataRow<Cell>>() {
        @Override
        public Object getKey(DataRow<Cell> item) {
            return item.getIndex();
        }
    };

    private Label noResultLabel = new Label();

	@UiField
    GenericDataGrid<DataRow<Cell>> formDataTable;
	@UiField
	FlexiblePager pager;
	@UiField
    LinkButton addRowButton, removeRowButton, correctionButton;
	@UiField
	Button originalVersionButton, fillPreviousButton;
    @UiField
    Button consolidationButton;
    @UiField
    Button refreshButton;
	@UiField
	Button recalculateButton;
	@UiField
	Button checkButton;
	@UiField
	Button deleteFormButton;

//	@UiField
//    DropdownButton printAnchor;
	@UiField
	Anchor returnAnchor;

	@UiField
	Button cancelButton;
    @UiField
    Button exitAndSaveButton;
	@UiField
	Button saveButton;

	@UiField
    HorizontalPanel workflowButtons;
	@UiField
    HorizontalPanel saveCancelPanel;

    @UiField
    Label formKindTitle;
	@UiField
	Label formKindLabel;
	@UiField
	Label lockInformation;
	@UiField
	Label departmentIdLabel;
	@UiField
	Label reportPeriodLabel;
    @UiField
    Label comparativePeriodTitle;
    @UiField
    Label comparativePeriodLabel;
	@UiField
	Label stateLabel;
	@UiField
	Label title;
	@UiField
	LinkButton signersAnchor;

    @UiField
    Panel linkButtonPanel;

	@UiField
	CheckBox showCheckedColumns;

    @UiField
    FileUploadWidget fileUploader;

    @UiField
    HorizontalPanel addRemoveRowsBlock;

    @UiField
    HorizontalPanel centerBlock;
    @UiField
    LinkButton editAnchor;

    @UiField
    LinkButton manualAnchor;

    @UiField
    LinkButton deleteManualAnchor;

    @UiField
    Label editModeLabel;
    @UiField
    ResizeLayoutPanel tableWrapper;
    @UiField
    LinkButton search;

    @UiField
    LinkButton manualVersionLink;
    @UiField
    Label manualVersionLabel;
    @UiField
    LinkButton autoVersionLink;
    @UiField
    Label autoVersionLabel;
    @UiField
    HorizontalPanel versionBlock;
    @UiField
    LinkButton filesComments, sources;
    @UiField
    DropdownButton printAnchor;

    public static final int DEFAULT_TABLE_TOP_POSITION = 104;
    public static final int DEFAULT_RIGHT_BUTTONS_HEIGHT = 61;
    private static final int DEFAULT_REPORT_PERIOD_LABEL_WIDTH = 150;
    private static final int LOCK_INFO_BLOCK_HEIGHT = 25;

    private int sessionId;

    /** Положение таблицы по высоте в данный момент времени */
    private int tableTopPosition = DEFAULT_TABLE_TOP_POSITION;

    @Override
    public void updateTableTopPosition(int position) {
        tableTopPosition = position;
    }

    @Override
    public void updateRightButtonsHeight(int height) {
        linkButtonPanel.getElement().getStyle().setHeight(height, Style.Unit.PX);
    }

    @Inject
    public FormDataView(final Binder binder) {
        initWidget(binder.createAndBindUi(this));

        fileUploader.addStartLoadHandler(new StartLoadFileEvent.StartLoadFileHandler() {
            @Override
            public void onStartLoad(StartLoadFileEvent event) {
                getUiHandlers().onStartLoad();
            }
        });

        fileUploader.addEndLoadHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                getUiHandlers().onEndLoad();
            }
        });

        formDataTable.addCellPreviewHandler(new CellPreviewEvent.Handler<DataRow<Cell>>() {
            @Override
            public void onCellPreview(CellPreviewEvent<DataRow<Cell>> event) {
                if ("mouseover".equals(event.getNativeEvent().getType())) {
                    EventTarget eventTarget = event.getNativeEvent().getEventTarget();
                    if (Element.is(eventTarget)) {
                        Element target = Element.as(eventTarget);
                        if ("td".equalsIgnoreCase(target.getTagName())) {
                            TableCellElement cellElement = formDataTable.getRowElement(event.getIndex() - formDataTable.getPageStart()).getCells().getItem(event.getColumn());
                            if (cellElement.getInnerText().replace("\u00A0", "").trim().isEmpty()) {
                                cellElement.removeAttribute("title");
                            } else {
                                cellElement.setTitle(cellElement.getInnerText());
                            }
                        }
                    }
                }
            }
        });

        // хак для горизонтального скроллбара у пустой таблицы
        formDataTable.setEmptyTableWidget(noResultLabel);
        formDataTable.setRowData(new ArrayList());

        formDataTable.setPageSize(pager.getPageSize());
        pager.setDisplay(formDataTable);
        recalcReportPeriodLabelWidth();     // пересчитаем при первом отображении страницы

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                recalcReportPeriodLabelWidth();
            }
        });
    }

    public int generateNewSessionId() {
        sessionId = (int) System.currentTimeMillis();
        return sessionId;
    }

    @Override
    public void updatePrintReportButtonName(String fdReportType, boolean isLoad) {
        if (fdReportType != null && !fdReportType.isEmpty()) {
            LinkButton linkButton = (LinkButton) printAnchor.getItem(fdReportType);
            if (linkButton != null) {
                if (isLoad) {
                    linkButton.setText("Выгрузить \"" + fdReportType + "\"");
                } else {
                    linkButton.setText("Сформировать \"" + fdReportType + "\"");
                }
            }
        }
    }

    @Override
    public void showConsolidation(boolean isCons) {
        consolidationButton.setVisible(isCons);
    }

	@Override
	public void setColumnsData(List<Column> columnsData, boolean readOnly, boolean forceEditMode) {
		// Clean columns
		while (formDataTable.getColumnCount() > 0) {
			formDataTable.removeColumn(0);
		}
        //Create order column
        NumericColumn numericColumn = new NumericColumn();

        DataRowColumn<Integer> indexColumn = new DataRowColumn<Integer>(new IndexCell(), numericColumn) {
            @Override
            public Integer getValue(DataRow<Cell> object) {
                return object.getIndex();
            }

            @Override
            public String getCellStyleNames(com.google.gwt.cell.client.Cell.Context context, DataRow<Cell> object) {
                // этот метод не вызывается при выделении строк при работе с NoSelectionModel
                DataRow<Cell> selectedRow = getSelectedRow();
                return selectedRow != null && selectedRow.equals(object) ? "orderSelected" : "order";
            }
        };
        formDataTable.addColumn(indexColumn, "№");
        formDataTable.setColumnWidth(indexColumn, 3, Style.Unit.EM);

		factory.setReadOnly(readOnly);
		factory.setSuperEditMode(forceEditMode);

		boolean hideCheckedColumnsCheckbox = true;
		for (Column col : columnsData) {
			if (col.isChecking()) {
				hideCheckedColumnsCheckbox = false;
			}

            com.google.gwt.user.cellview.client.Column<DataRow<Cell>, ?> tableCol = factory
                    .createTableColumn(col, formDataTable);
            if (showCheckedColumns.getValue() || !col.isChecking()) {
                formDataTable.addColumn(tableCol, col.getName());
            } else {
                formDataTable.addColumn(tableCol);
            }
            ((DataRowColumn<?>)tableCol).addCellModifiedEventHandler(new CellModifiedEventHandler() {
                @Override
                public void onCellModified(CellModifiedEvent event, boolean withReference) {
                    if (getUiHandlers() != null) {
                        getUiHandlers().onCellModified(event.getDataRow());
                        // Зависимые ячейки - обновление всей строки
                        if (withReference) {
                            formDataTable.redrawRow(event.getDataRow().getIndex() - 1);
                        }
                    }
                }
            });
            if (!showCheckedColumns.getValue() && col.isChecking()) {
                formDataTable.setColumnWidth(tableCol, "0px");
            } else if (col.getWidth() >= 0) {
                formDataTable.setColumnWidth(tableCol, col.getWidth(), Style.Unit.EM);
            }
		}
		showCheckedColumns.setVisible(!hideCheckedColumnsCheckbox);
		//TODO КОСТЫЛИ! По возможности убрать.
		float tableWidth = 0;
		for (int i=0; i<formDataTable.getColumnCount(); i++) {
			String width = formDataTable.getColumnWidth(formDataTable.getColumn(i));
			if (width == null) {
				continue;
			}
			for (Style.Unit unit : Style.Unit.values()) {
				if (width.contains(unit.getType())) {
					width = width.replace(unit.getType(), "");
					break;
				}
			}
			tableWidth += Float.parseFloat(width);
		}
		formDataTable.setTableWidth(tableWidth, Style.Unit.EM);
        noResultLabel.setWidth(tableWidth + "em");
	}

    @Override
    public void setTableLockMode(boolean lockMode) {
        factory.setLockMode(lockMode);
    }

	@Override
	public void setRowsData(int start, int totalCount, List<DataRow<Cell>> rowsData) {
		formDataTable.setRowCount(totalCount);
		formDataTable.setRowData(start, rowsData);
        if (needScrollToRow != null && !fixedRows) {
            selectRow(needScrollToRow);
            formDataTable.setKeyboardSelectedRow(needScrollToRow - 1 - formDataTable.getPageStart());
            formDataTable.setKeyboardSelectedColumn(0);
            needScrollToRow = null;
        }
    }

	@Override
	public void addCustomHeader(List<DataRow<HeaderCell>> headers) {
		CustomHeaderBuilder builder = new CustomHeaderBuilder(formDataTable, false, 1, headers);
		formDataTable.setHeaderBuilder(builder);
	}

	// После вызова этого метода таблица получает возможность объединять ячейки и применять стили
	@Override
	public void addCustomTableStyles(List<FormStyle> allStyles) {
		CustomTableBuilder<DataRow<Cell>> builder = new CustomTableBuilder<DataRow<Cell>>(formDataTable, false);
		formDataTable.setTableBuilder(builder);
	}

    @UiHandler("correctionButton")
    void onCorrectionLinkButtonClicked(ClickEvent event) {
        getUiHandlers().onCorrectionSwitch();
    }

	@UiHandler("cancelButton")
	void onCancelButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCancelClicked();
		}
	}

    @UiHandler("exitAndSaveButton")
    void onExitAndSaveButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onExitAndSaveClicked();
        }
    }

	@UiHandler("saveButton")
	void onSaveButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSaveClicked();
		}
	}

	@UiHandler("addRowButton")
	void onAddRowButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onAddRowClicked();
		}
	}

	@UiHandler("removeRowButton")
	void onRemoveRowButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onRemoveRowClicked();
		}
	}

    @UiHandler("fillPreviousButton")
    void onFillPreviousButtonClicked(ClickEvent event) {
        getUiHandlers().onFillPreviousButtonClicked();
    }

	@Override
	public DataRow<Cell> getSelectedRow() {
		return singleSelectionModel.getSelectedObject();
	}

	@Override
	public void setSelectedRow(DataRow<Cell> item, boolean selected) {
        singleSelectionModel.setSelected(item, selected);
	}

    @UiHandler(value = {"manualVersionLink", "autoVersionLink"})
    void onModeClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onModeChangeClicked();
        }
    }

    @UiHandler("editAnchor")
    void onEditButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onEditClicked(false, false);
        }
    }

    @UiHandler("manualAnchor")
    void onCreateManualClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateManualClicked();
        }
    }

	@UiHandler("infoAnchor")
	void onInfoButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onInfoClicked();
		}
	}

	@UiHandler("originalVersionButton")
	void onOriginalVersionButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onOriginalVersionClicked();
		}
	}

	@UiHandler("recalculateButton")
	void onRecalculateButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onRecalculateClicked(false, false);
		}
	}

    @UiHandler("refreshButton")
    void onUpdateButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onRefreshClicked(false, false);
        }
    }

	@UiHandler("checkButton")
	void onCheckButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCheckClicked(false);
		}
	}

	@UiHandler("signersAnchor")
	void onSignersAnchorClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onSignersClicked();
		}
	}

	@UiHandler("returnAnchor")
	void onReturnAnchorClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onReturnClicked();
			event.preventDefault();
			event.stopPropagation();
		}
	}

	@UiHandler("deleteFormButton")
	void onDeleteFormButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onDeleteFormClicked();
		}
	}

    @UiHandler("deleteManualAnchor")
    void onDeleteManualButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onDeleteManualClicked();
        }
    }

	@UiHandler("showCheckedColumns")
	void onShowCheckedColumnsClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onShowCheckedColumns();
		}
	}

    @UiHandler("consolidationButton")
    void onConsolidationButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onConsolidate(false, false);
        }
    }

	@Override
	public void setAdditionalFormInfo(
			String formType, TaxType taxType, String formKind, String departmentId, String reportPeriod, String comparativPeriod, String state,
            Date startDate, Date endDate, Long formDataId, boolean correctionPeriod, boolean correctionDiff, boolean readOnly) {
        returnAnchor.setText(taxType.getName());
        title.setText(formType);
		title.setTitle(formType);
		formKindLabel.setText(formKind);
        departmentIdLabel.setText(departmentId);
        departmentIdLabel.setTitle(departmentId);
		reportPeriodLabel.setText(reportPeriod);
		reportPeriodLabel.setTitle(reportPeriod);
		stateLabel.setText(state);
		factory.setDateRange(startDate, endDate);
        if (taxType.isTax()) {
            formKindTitle.setText(FORM_DATA_KIND_TITLE);
        } else {
            formKindTitle.setText(FORM_DATA_KIND_TITLE_D);
        }
        if (comparativPeriod != null && !comparativPeriod.isEmpty()) {
            comparativePeriodLabel.setVisible(true);
            comparativePeriodTitle.setVisible(true);
            comparativePeriodLabel.setText(comparativPeriod);
            linkButtonPanel.getElement().getStyle().clearOverflowY();
        } else {
            comparativePeriodLabel.setVisible(false);
            comparativePeriodTitle.setVisible(false);
            linkButtonPanel.getElement().getStyle().setOverflowY(Style.Overflow.SCROLL);
        }
        factory.setFormDataId(formDataId);
        // Признак корректирующего периода
        showCorrectionButton(correctionPeriod);
        // Признак сравнения корректирующих значений
        updateCorrectionButton(correctionDiff);

        if (correctionDiff) {
            checkButton.setVisible(false);
        } else {
            checkButton.setVisible(true);
        }
        fillPreviousButton.setVisible(correctionPeriod && !readOnly);
	}

	/**
	 * Показывает кнопки для доступных переходов, если null, то скрываем все
	 * кнопки.
	 */
	@Override
	public void setWorkflowButtons(List<WorkflowMove> moves) {
		boolean show = false;
		workflowButtons.clear();

		if (moves != null && !moves.isEmpty()) {
			for (final WorkflowMove workflowMove : moves) {
				Button button = new Button(workflowMove.getName(),
						new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								getUiHandlers().onWorkflowMove(workflowMove);
							}
						});
                button.getElement().getStyle().setMarginRight(9, Style.Unit.PX);
				workflowButtons.add(button);
			}
			show = true;
		}

		workflowButtons.setVisible(show);
	}

	@Override
	public void setBackButton(String link) {
		returnAnchor.setHref(link);
	}

	@Override
	public void showOriginalVersionButton(boolean show) {
		// http://jira.aplana.com/browse/SBRFACCTAX-2242 В режиме редактирования доступна кнопка "Исходная версия",
		// при нажатии на которую диалоговое окно "в разработке". Кнопку в версии 0.2.1 необходимо скрыть.
		originalVersionButton.setVisible(false);
	}

	@Override
	public void showSaveCancelPanel(boolean show, boolean readOnlyMode) {
        if (show || readOnlyMode) {
            saveCancelPanel.setVisible(show);
            cancelButton.setVisible(show);
            saveButton.setVisible(show);
        } else {
            saveCancelPanel.setVisible(true);
            cancelButton.setVisible(true);
            saveButton.setVisible(false);
        }
        fileUploader.setVisible(show);
	}

    @Override
    public void showAddRemoveRowsBlock(boolean show){
        addRemoveRowsBlock.setVisible(show);
    }

	@Override
	public void showRecalculateButton(boolean show) {
		recalculateButton.setVisible(show);
	}

    @Override
    public void showRefreshButton(boolean show) {
        refreshButton.setVisible(show);
    }

	@Override
	public void showCheckButton(boolean show) {
        checkButton.setVisible(show);
	}

	@Override
	public void enableRemoveRowButton(boolean enable) {
		removeRowButton.setEnabled(enable);
	}

	@Override
	public void showPrintAnchor(boolean show) {
        printAnchor.setVisible(show);
	}

    @Override
    public void showEditModeLabel(boolean show) {
        editModeLabel.setVisible(show);
    }

    @Override
    public void showEditAnchor(boolean show) {
        editAnchor.setVisible(show);
    }

    @Override
	public void showDeleteFormButton(boolean show) {
		deleteFormButton.setVisible(show);
	}

	@Override
	public void showSignersAnchor(boolean show) {
		signersAnchor.setVisible(show);
	}

    @Override
    public void showModeAnchor(boolean show, boolean manual) {
        versionBlock.setVisible(show);

        autoVersionLabel.setVisible(!manual);
        autoVersionLink.setVisible(manual);

        manualVersionLabel.setVisible(manual);
        manualVersionLink.setVisible(!manual);
    }

    @Override
    public void showManualAnchor(boolean show) {
        manualAnchor.setVisible(show);
    }

    @Override
    public void showDeleteManualAnchor(boolean show) {
        deleteManualAnchor.setVisible(show);
    }

    @Override
	public void setLockInformation(boolean isVisible, boolean readOnlyMode, LockInfo lockInfo, TaxType taxType){
		lockInformation.setVisible(isVisible);
		if(lockInfo != null){
            String text;
            if (readOnlyMode) {
                if (lockInfo.isEditMode()) {
                    text =
                            "Выбранная "
                                    + (taxType.isTax() ? "налоговая форма" : "форма")
                                    + " в текущий момент редактируется " +
                                    (lockInfo.isLockedMe() ? "текущим пользователем" : ("другим пользователем \"" + lockInfo.getLockedByUser() + "\""))
                                    + " (с " + lockInfo.getLockDate() + ")";
                } else {
                    text =
                            "Выбранная "
                                    +  (taxType.isTax() ? "налоговая форма" : "форма")
                                    + " в текущий момент заблокирована на изменение "
                                    + (lockInfo.isLockedMe() ? "текущим пользователем" : ("другим пользователем \"" + lockInfo.getLockedByUser() + "\""))
                                    + " (с " + lockInfo.getLockDate() + ")";
                }
            } else {
                text =
                        "Выбранная "
                                +   (taxType.isTax() ? "налоговая форма" : "форма")
                                + " в текущий момент заблокирована на редактирование текущим пользователем (с " + lockInfo.getLockDate() + ")";
            }
			lockInformation.setText(text);
			lockInformation.setTitle(lockInfo.getTitle());
		}
        changeTableTopPosition(isVisible);
	}

    /**
     * Увеличивает верхний отступ у таблицы, когда показывается сообщение о блокировки
     * @param isLockInfoVisible показано ли сообщение
     */
    private void changeTableTopPosition(Boolean isLockInfoVisible){
        Style formDataTableStyle = tableWrapper.getElement().getStyle();
        int downShift = 0;
        if (isLockInfoVisible){
            downShift = LOCK_INFO_BLOCK_HEIGHT;
        }
        formDataTableStyle.setProperty("top", tableTopPosition + downShift, Style.Unit.PX);
    }

    /**
     * Перечет и установка ширины контейнера с значением налогового периода.
     * делается в ручную потому что контернер находится в табличной ячейке
     * и заворачивание во многоточние происходит если только явно задать ширину в пикселях
     */
    private void recalcReportPeriodLabelWidth(){

        // сбрасывает прошлое значение лейбла что бы он не мешал замеру его родительского контейнра
        reportPeriodLabel.getElement().getStyle().setPropertyPx("width", DEFAULT_REPORT_PERIOD_LABEL_WIDTH);

        // берется ширина ячейки в которой находится контейнер с информационном блоком формы
        Element centerBlockParentElement = centerBlock.getElement().getParentElement();
        if (centerBlockParentElement != null) {
            Integer parentWidth = centerBlockParentElement.getOffsetWidth();
            if (parentWidth != null) {
                int width = parentWidth - 135;
                if (width > 0) {
                    reportPeriodLabel.getElement().getStyle().setPropertyPx("width", width);
                }
            }
        }

    }

	@Override
	public boolean getCheckedColumnsClicked() {
		return showCheckedColumns.isVisible() ? showCheckedColumns.getValue() : false;
	}

	@Override
	public void assignDataProvider(int pageSize) {
        pager.setPageSize(pageSize);
		if(!dataProvider.getDataDisplays().contains(formDataTable)) {
			dataProvider.addDataDisplay(formDataTable);
		}
	}

    @Override
    public int getPageSize() {
        return pager.getPageSize();
    }

    @Override
	public void updateData() {
        formDataTable.setVisibleRangeAndClearData(formDataTable.getVisibleRange(), true);
	}

    @UiFactory
    FormDataView getView(){
        return this;
    }

	private void updateData(int pageNumber) {
		if (pager.getPage() == pageNumber){
			updateData();
		} else {
			pager.setPage(pageNumber);
		}
	}

    @Override
    public void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler, CheckHandler checkHandler) {
        fileUploader.addValueChangeHandler(changeHandler);
        fileUploader.setCheckHandler(checkHandler);
    }

    @Override
    public void isCanEditPage(boolean visible){
        pager.isCanEditPage(visible);
    }

    @Override
    public void updatePageSize(TaxType taxType) {
        if (this.taxType == null || !this.taxType.equals(taxType)) {
            this.taxType = taxType;
            pager.setType("formData" + taxType.getCode());
            formDataTable.setPageSize(pager.getPageSize());
        }
        updateData(0);
    }

    @Override
    public TaxType getTaxType() {
        return taxType;
    }

    @UiHandler("search")
    public void onSearchClicked(ClickEvent event){
        getUiHandlers().onOpenSearchDialog(sessionId);
    }

    @UiHandler("filesComments")
    public void onFilesCommentsClicked(ClickEvent event){
        getUiHandlers().onFilesCommentsDialog();
    }

    @UiHandler("sources")
    public void onSourcesClicked(ClickEvent event){
        getUiHandlers().onOpenSourcesDialog();
    }

    @Override
    public void setFocus(final Long rowIndex) {
        if (fixedRows){
            singleSelectionModel.clear();
            selectRow(rowIndex.intValue());
            formDataTable.setKeyboardSelectedRow(rowIndex.intValue() - 1);
            formDataTable.setKeyboardSelectedColumn(0);
        } else {
            singleSelectionModel.clear();
            // go to essential page
            Long page = (rowIndex - 1) / getPageSize();
            if (pager.getPage() != page.intValue()){
                this.needScrollToRow = rowIndex.intValue();
                pager.setPage(page.intValue());
            } else {
                selectRow(rowIndex.intValue());
                formDataTable.setKeyboardSelectedRow(rowIndex.intValue() - 1 - formDataTable.getPageStart());
                formDataTable.setKeyboardSelectedColumn(0);
            }
        }
    }

    void selectRow(int rowIndex) {
        List<DataRow<Cell>> rows = formDataTable.getVisibleItems();
        for (DataRow<Cell> cell: rows)
            if (cell.getIndex()==rowIndex)
                singleSelectionModel.setSelected(cell, true);
    }

    @Override
    public void setupSelectionModel(boolean fixedRows) {
        this.fixedRows = fixedRows;
        singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>(KEY_PROVIDER);
        formDataTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.BOUND_TO_SELECTION);
        singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                FormDataUiHandlers handlers = getUiHandlers();
                if (handlers != null) {
                    handlers.onSelectRow();
                }
            }
        });

        formDataTable.setSelectionModel(singleSelectionModel);
    }

    @Override
    public void showCorrectionButton(boolean correctionPeriod) {
        correctionButton.setVisible(correctionPeriod);
    }

    public void updateCorrectionButton(boolean correctionDiff) {
        correctionButton.setText(correctionDiff ? "Показать абсолютные значения" : "Показать изменения");
    }

    @Override
    public void setReportTypes(List<FormDataReportType> reportTypes) {
        printAnchor.clear();
        for(final FormDataReportType reportType: reportTypes) {
            LinkButton linkButton = new LinkButton("Сформировать \"" + reportType.getReportName() + "\"");
            linkButton.setHeight("20px");
            linkButton.setDisableImage(true);
            linkButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (getUiHandlers() != null) {
                        getUiHandlers().onPrintClicked(reportType, false);
                    }
                }
            });
            printAnchor.addItem(reportType.getReportName(), linkButton);
        }
    }
}
