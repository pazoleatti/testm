package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.web.widget.cell.IndexCell;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomHeaderBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.CustomTableBuilder;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumn;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEvent;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.events.CellModifiedEventHandler;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.DropdownButton;
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
import com.google.gwt.user.cellview.client.DataGrid;
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
	private NoSelectionModel<DataRow<Cell>> noSelectionModel;

    public static final String FORM_DATA_KIND_TITLE = "Тип налоговой формы:";
    public static final String FORM_DATA_KIND_TITLE_D = "Тип формы:";

    private TaxType taxType;
    private boolean fixedRows;
    // флаг что нужно заскролить к выделенной строке, используется для формы поиска
    private Integer needScrollToRow = null;
    // содержит ссылку на предыдуще выделенную строку при использовании NoSelectionModel
    private DataRow<Cell> prevSelectedRow = null;

    interface Binder extends UiBinder<Widget, FormDataView> {
	}

	private DataRowColumnFactory factory = new DataRowColumnFactory();

	private AsyncDataProvider<DataRow<Cell>> dataProvider = new  AsyncDataProvider<DataRow<Cell>>() {
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
	DataGrid<DataRow<Cell>> formDataTable;
	@UiField
	FlexiblePager pager;
	@UiField
    LinkButton addRowButton, removeRowButton, correctionButton;
	@UiField
	Button originalVersionButton;
	@UiField
	Button recalculateButton;
	@UiField
	Button checkButton;
	@UiField
	Button deleteFormButton;

	@UiField
    DropdownButton printAnchor;
	@UiField
	Anchor returnAnchor;

	@UiField
	Button cancelButton;
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
	Label stateLabel;
	@UiField
	Label title;
	@UiField
	LinkButton signersAnchor;

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
    LinkButton sources;

    private final static int DEFAULT_TABLE_TOP_POSITION = 104;
    private final static int DEFAULT_REPORT_PERIOD_LABEL_WIDTH = 150;
    private final static int LOCK_INFO_BLOCK_HEIGHT = 25;

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
                        if ("td".equals(target.getTagName().toLowerCase())) {
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

        LinkButton printToExcel = new LinkButton("Excel");
        printToExcel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getUiHandlers() != null) {
                    getUiHandlers().onPrintExcelClicked();
                }
            }
        });

        LinkButton printToCSV = new LinkButton("CSV");
        printToCSV.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getUiHandlers() != null) {
                    getUiHandlers().onPrintCSVClicked();
                }
            }
        });
        printAnchor.addItem(printToExcel);
        printAnchor.addItem(printToCSV);
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

			if (showCheckedColumns.getValue() || !col.isChecking()) {
				com.google.gwt.user.cellview.client.Column<DataRow<Cell>, ?> tableCol = factory
						.createTableColumn(col, formDataTable);
				formDataTable.addColumn(tableCol, col.getName());
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
				if (col.getWidth() >= 0) {
					formDataTable.setColumnWidth(tableCol, col.getWidth(), Style.Unit.EM);
				}
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

	@Override
	public DataRow<Cell> getSelectedRow() {
		return fixedRows ? noSelectionModel.getLastSelectedObject() : singleSelectionModel.getSelectedObject();
	}

	@Override
	public void setSelectedRow(DataRow<Cell> item, boolean selected) {
        if(fixedRows) {
            noSelectionModel.setSelected(item, selected);
        } else{
            singleSelectionModel.setSelected(item, selected);
        }
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
            getUiHandlers().onEditClicked(false);
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
			getUiHandlers().onRecalculateClicked();
		}
	}

	@UiHandler("checkButton")
	void onCheckButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onCheckClicked();
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

	@Override
	public void setAdditionalFormInfo(
			String formType, TaxType taxType, String formKind, String departmentId, String reportPeriod, String state,
            Date startDate, Date endDate, Long formDataId, boolean correctionPeriod, boolean correctionDiff) {
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
        if (!taxType.equals(TaxType.DEAL)) {
            formKindTitle.setText(FORM_DATA_KIND_TITLE);
        } else {
            formKindTitle.setText(FORM_DATA_KIND_TITLE_D);
        }
        factory.setFormDataId(formDataId);
        // Признак корректирующего периода
        correctionButton.setVisible(correctionPeriod);
        // Признак сравнения корректирующих значений
        getView().setCorrectionText(correctionDiff ? "Абсолютные значения" : "Корректировка");

        if (correctionDiff) {
            checkButton.setVisible(false);
            workflowButtons.setVisible(false);
        }
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
	public void showSaveCancelPanel(boolean show) {
		saveCancelPanel.setVisible(show);
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
	public void showCheckButton(boolean show) {
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
	public void setLockInformation(boolean isVisible, String lockDate, String lockedBy){
		lockInformation.setVisible(isVisible);
		if(lockedBy != null && lockDate != null){
            String text = "Выбранная налоговая форма в текущий момент редактируется другим пользователем \"" + lockedBy
                    + "\" (с "+ lockDate + " )";
			lockInformation.setText(text);
			lockInformation.setTitle(text);
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
        formDataTableStyle.setProperty("top", DEFAULT_TABLE_TOP_POSITION + downShift, Style.Unit.PX);
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
		return showCheckedColumns.getValue();
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
    public void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler) {
        fileUploader.addValueChangeHandler(changeHandler);
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
        getUiHandlers().onOpenSearchDialog();
    }

    @UiHandler("sources")
    public void onSourcesClicked(ClickEvent event){
        getUiHandlers().onOpenSourcesDialog();
    }

    @Override
    public void setFocus(final Long rowIndex) {
        if (fixedRows){
            formDataTable.setKeyboardSelectedRow(rowIndex.intValue() - 1);
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
        if (fixedRows){
            noSelectionModel = new NoSelectionModel<DataRow<Cell>>(KEY_PROVIDER);
            formDataTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.BOUND_TO_SELECTION);
            noSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                    FormDataUiHandlers handlers = getUiHandlers();
                    if (handlers != null) {
                        handlers.onSelectRow();
                    }
                    if (prevSelectedRow != null) {
                        TableCellElement item = formDataTable.getRowElement(prevSelectedRow.getIndex() - 1).getCells().getItem(0);
                        item.removeAttribute("style");
                    }
                    DataRow<Cell> selectedRow = getSelectedRow();
                    if (selectedRow != null) {
                        prevSelectedRow = selectedRow;
                        TableCellElement item = formDataTable.getRowElement(getSelectedRow().getIndex() - 1).getCells().getItem(0);
                        item.setAttribute("style", "background-color: #5a5a5a !important;");
                    }
                }
            });

            formDataTable.setSelectionModel(noSelectionModel);
        } else {
            singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>(KEY_PROVIDER);
            formDataTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);
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
    }

    @Override
    public void setCorrectionText(String text) {
        correctionButton.setText(text);
    }
}
