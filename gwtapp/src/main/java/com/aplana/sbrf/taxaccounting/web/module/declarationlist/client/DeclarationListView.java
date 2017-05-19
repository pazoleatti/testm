package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.State;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataTokens;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client.TableWithCheckedColumn;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DeclarationListView extends
		ViewWithUiHandlers<DeclarationListUiHandlers> implements
		DeclarationListPresenter.MyView {

    public static final String DECLARATION_HEADER = "Список налоговых форм";
    public static final String DECLARATION_HEADER_R = "Отчетность";
    public static final String DECLARATION_CREATE = "Создать налоговую форму...";
    public static final String DECLARATION_CREATE_D = "Создать уведомление...";
    public static final String DECLARATION_CREATE_TITLE = "Создание налоговой формы";
    public static final String DECLARATION_KIND_TITLE = "Тип налоговой формы";
    public static final String DECLARATION_DATA_ID_TITLE = "Номер формы";
    public static final String DECLARATION_DATA_CREATION_DATE_TITLE = "Дата и время создания формы";
    public static final String DECLARATION_DATA_IMPORT_TF_TITLE = "Создал";
    public static final String DECLARATION_TYPE_TITLE = "Вид налоговой формы";
    public static final String DEPARTMENT_TITLE = "Подразделение";
    public static final String TAX_ORGAN_CODE_TITLE = "Код НО";
    public static final String TAX_ORGAN_CODE_KPP_TITLE = "КПП";
    public static final String OKTMO_TITLE = "ОКТМО";
    public static final String CREATE_DATE_TITLE = "Дата формирования";
    public static final String DOC_STATE_TITLE = "Состояние ЭД";
    public static final String NOTE_TITLE = "Примечание";
    public static final String ASNU_TITLE = "Наименование АСНУ";
    public static final String STATE_TITLE = "Состояние";
    public static final String FILE_NAME_TITLE = "Файл ТФ";
    public static final String FILE_NAME_TITLE_REPORT = "XML-файл формы";
    public static final String PERIOD_TITLE = "Период";

    private static final int TABLE_TOP3 = 75 + 27 + 30;
    private static final int TABLE_TOP4 = 130 + 32;

	interface MyBinder extends UiBinder<Widget, DeclarationListView> {}

    private GenericDataGrid.DataGridResizableHeader declarationTypeHeader;
    private Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem> declarationTypeColumn;
    private GenericDataGrid.DataGridResizableHeader reportPeriodHeader;
    private TextColumn<DeclarationDataSearchResultItem> reportPeriodColumn;

	private DeclarationDataSearchOrdering sortByColumn;

	private boolean isAscSorting;

    private Map<Integer, String> departmentFullNames;
    private Map<Long, String> asnuNames;

    private MultiSelectionModel<DeclarationDataSearchResultItem> selectionModel;

    private final static DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy");

    private final static DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

    @UiField
    Label declarationHeader;

	@UiField
	Panel filterContentPanel;

	@UiField
    Button checkButton, recalculateButton, deleteButton, acceptButton, cancelButton, changeStatusEDButton;

	@UiField
    GenericDataGrid<DeclarationDataSearchResultItem> declarationTable;

    @UiField
    FlexiblePager pager;

	@UiField
	Label titleDesc;

    @UiField
    LinkButton create, createReports, downloadReports;

    @UiField
    ResizeLayoutPanel tableWrapper;

    //private ListDataProvider<DeclarationDataSearchResultItem> model;
    private Column<DeclarationDataSearchResultItem, Boolean> checkColumn;
    private Header<Boolean> checkBoxHeader;
    private List<DeclarationDataSearchResultItem> checkedRows = new LinkedList<DeclarationDataSearchResultItem>();

    private final AsyncDataProvider<DeclarationDataSearchResultItem> dataProvider = new AsyncDataProvider<DeclarationDataSearchResultItem>() {
        @Override
        protected void onRangeChanged(HasData<DeclarationDataSearchResultItem> display) {
            if (getUiHandlers() != null){
                // заполенине параметров по какой сортировать
                if (declarationTable.getColumnSortList().size() > 0) {
                    isAscSorting = declarationTable.getColumnSortList().get(0).isAscending();
                    setSortByColumn((String) declarationTable.getHeader(declarationTable.getColumnIndex((Column<DeclarationDataSearchResultItem, ?>) declarationTable.getColumnSortList().get(0).getColumn())).getValue());
                }
                final Range range = display.getVisibleRange();
                getUiHandlers().onRangeChange(range.getStart(), range.getLength());
            }
        }
    };

	@Inject
	public DeclarationListView(final MyBinder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));

        selectionModel = new MultiSelectionModel<DeclarationDataSearchResultItem>();
        declarationTable.setSelectionModel(selectionModel);

        pager.setDisplay(declarationTable);
        declarationTable.setPageSize(pager.getPageSize());
        dataProvider.addDataDisplay(declarationTable);
        declarationTable.addColumnSortHandler(new ColumnSortEvent.AsyncHandler(declarationTable));

        declarationTable.getColumnSortList().setLimit(1);       // сортировка только по одной колонке
    }

    /**
     * Наименование периода
     */
    private String getReportPeriodName(DeclarationDataSearchResultItem item) {
        String str = item.getReportPeriodYear() + ": " + item.getReportPeriodName();
        if (item.getCorrectionDate() != null) {
            str += ", корр. (" + DATE_FORMAT.format(item.getCorrectionDate()) + ")";
        }
        return str;
    }

    @Override
    public void initTable(TaxType taxType, boolean isReports) {
        if (isReports) {
            create.setVisible(false);
            createReports.setVisible(true);
            downloadReports.setVisible(true);
            recalculateButton.setVisible(false);
            changeStatusEDButton.setVisible(true);
        } else {
            create.setVisible(true);
            createReports.setVisible(false);
            downloadReports.setVisible(false);
            recalculateButton.setVisible(true);
            changeStatusEDButton.setVisible(false);
        }
        Style tableStyle = tableWrapper.getElement().getStyle();
        tableStyle.setProperty("top", (isReports) ?
                TABLE_TOP4 : TABLE_TOP3, Style.Unit.PX);

        clearTable();

        checkColumn = new Column<DeclarationDataSearchResultItem, Boolean>(
                new CheckboxCell(false, true)) {
            @Override
            public Boolean getValue(DeclarationDataSearchResultItem object) {
                return selectionModel.isSelected(object);
            }
        };
        checkColumn.setFieldUpdater(new FieldUpdater<DeclarationDataSearchResultItem, Boolean>() {
            @Override
            public void update(int index, DeclarationDataSearchResultItem object, Boolean chcked) {
                if (chcked) {
                    checkedRows.add(object);
                } else {
                    checkedRows.remove(object);
                }
                updateButton();
            }
        });

        checkBoxHeader = new CheckBoxHeader() {
            @Override
            public Boolean getValue() {
                for (DeclarationDataSearchResultItem item : declarationTable.getVisibleItems()) {
                    if (!selectionModel.isSelected(item)) {
                        return false;
                    }
                }
                return declarationTable.getVisibleItems().size() > 0;
            }
            @Override
            public void onBrowserEvent(Cell.Context context, Element elem, NativeEvent event) {
                InputElement input = elem.getFirstChild().cast();
                Boolean isChecked = input.isChecked();
                for (DeclarationDataSearchResultItem item : declarationTable.getVisibleItems()) {
                    selectionModel.setSelected(item, isChecked);
                    if (isChecked) {
                        checkedRows.add(item);
                    } else {
                        checkedRows.remove(item);
                    }
                }
                updateButton();
            }
        };

        TextColumn<DeclarationDataSearchResultItem> declarationDataIdColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getDeclarationDataId().toString();
            }
        };

        TextColumn<DeclarationDataSearchResultItem> declarationKindColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getDeclarationFormKind().getTitle();
            }
        };

        TextColumn<DeclarationDataSearchResultItem> departmentColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return departmentFullNames.get(object.getDepartmentId());
            }
        };
        
        Column reportPeriodYearColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return String.valueOf(object.getReportPeriodYear());
            }
        };

        reportPeriodColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return getReportPeriodName(object);
            }
        };

        declarationTypeColumn = new Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem>(
                new AbstractCell<DeclarationDataSearchResultItem>() {

                    @Override
                    public void render(Context context,
                                       DeclarationDataSearchResultItem declaration,
                                       SafeHtmlBuilder sb) {
                        if (declaration == null) {
                            return;
                        }

                        sb.appendHtmlConstant("<a href=\"#"
                                + DeclarationDataTokens.declarationData + ";"
                                + DeclarationDataTokens.declarationId + "="
                                + declaration.getDeclarationDataId() + "\">"
                                + declaration.getDeclarationType() + "</a>");
                    }
                }) {
            @Override
            public DeclarationDataSearchResultItem getValue(
                    DeclarationDataSearchResultItem object) {
                return object;
            }
        };

        TextColumn<DeclarationDataSearchResultItem> declarationTaxOrganColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getTaxOrganCode();
            }
        };

        TextColumn<DeclarationDataSearchResultItem> declarationKppColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getTaxOrganKpp();
            }
        };
        TextColumn<DeclarationDataSearchResultItem> declarationOktmoColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getOktmo();
            }
        };
        TextColumn<DeclarationDataSearchResultItem> declarationDocStateColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getDocState();
            }
        };

        TextColumn<DeclarationDataSearchResultItem> declarationAsnuColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getAsnuId() != null ? asnuNames.get(object.getAsnuId()) : null;
            }
        };

        TextColumn<DeclarationDataSearchResultItem> stateColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getState().getTitle();
            }
        };

        TextColumn<DeclarationDataSearchResultItem> declarationDataCreationDateColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                if (object.getDeclarationDataCreationDate() != null) {
                    return DATE_TIME_FORMAT.format(object.getDeclarationDataCreationDate(), TimeZone.createTimeZone(-180));
                } else {
                    return "";
                }
            }
        };

        TextColumn<DeclarationDataSearchResultItem> importTfUserNameColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                if (object.getDeclarationDataCreationUserName() != null) {
                    return object.getDeclarationDataCreationUserName();
                } else {
                    return "";
                }
            }
        };

        Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem> fileNameColumn = new Column<DeclarationDataSearchResultItem, DeclarationDataSearchResultItem>(
                new AbstractCell<DeclarationDataSearchResultItem>() {

                    @Override
                    public void render(Context context,
                                       DeclarationDataSearchResultItem declaration,
                                       SafeHtmlBuilder sb) {
                        if (declaration == null || declaration.getFileName() == null || declaration.getFileName().isEmpty()) {
                            return;
                        }

                        sb.appendHtmlConstant("<a href=\""
                                + "download/declarationData/xml/"+
                                + declaration.getDeclarationDataId()+"\">"
                                + declaration.getFileName() + "</a>");
                    }
                }) {
            @Override
            public DeclarationDataSearchResultItem getValue(
                    DeclarationDataSearchResultItem object) {
                return object;
            }
        };

        TextColumn<DeclarationDataSearchResultItem> declarationNoteColumn = new TextColumn<DeclarationDataSearchResultItem>() {
            @Override
            public String getValue(DeclarationDataSearchResultItem object) {
                return object.getNote();
            }
        };

        declarationDataIdColumn.setSortable(true);
        declarationKindColumn.setSortable(true);
        departmentColumn.setSortable(true);
        reportPeriodYearColumn.setSortable(true);
        reportPeriodColumn.setSortable(true);
        declarationTypeColumn.setSortable(true);
        declarationTaxOrganColumn.setSortable(true);
        declarationKppColumn.setSortable(true);
        declarationOktmoColumn.setSortable(true);
        declarationDocStateColumn.setSortable(true);
        declarationNoteColumn.setSortable(true);
        declarationAsnuColumn.setSortable(true);
        stateColumn.setSortable(true);
        fileNameColumn.setSortable(true);
        declarationDataCreationDateColumn.setSortable(true);
        importTfUserNameColumn.setSortable(true);

        reportPeriodHeader = declarationTable.createResizableHeader(PERIOD_TITLE, reportPeriodColumn);

        declarationTable.addColumn(checkColumn, checkBoxHeader);
        declarationTable.setColumnWidth(checkColumn, 2, Style.Unit.EM);

        declarationTable.addColumn(declarationDataIdColumn, declarationTable.createResizableHeader(DECLARATION_DATA_ID_TITLE, declarationDataIdColumn));
        declarationTable.setColumnWidth(declarationDataIdColumn, 4, Style.Unit.EM);

        if (!isReports) {
            declarationTable.addColumn(declarationKindColumn, declarationTable.createResizableHeader(DECLARATION_KIND_TITLE, declarationKindColumn));
            declarationTable.setColumnWidth(declarationKindColumn, 8.7, Style.Unit.EM);
        }
        declarationTypeHeader = declarationTable.createResizableHeader(DECLARATION_TYPE_TITLE, declarationTypeColumn);
        declarationTable.addColumn(declarationTypeColumn, declarationTypeHeader);
        declarationTable.addColumn(departmentColumn, declarationTable.createResizableHeader(DEPARTMENT_TITLE, departmentColumn));
        if (taxType == TaxType.NDFL || taxType == TaxType.PFR) {
            if (!isReports) {
                declarationTable.addColumn(declarationAsnuColumn, declarationTable.createResizableHeader(ASNU_TITLE, declarationAsnuColumn));
            }
        }

        declarationTable.addColumn(reportPeriodColumn, reportPeriodHeader);
        declarationTable.addColumn(stateColumn, declarationTable.createResizableHeader(STATE_TITLE, stateColumn));
        declarationTable.setColumnWidth(stateColumn, 6.5, Style.Unit.EM);

        if (isReports && taxType == TaxType.NDFL) {
            declarationTable.addColumn(declarationKppColumn, declarationTable.createResizableHeader(TAX_ORGAN_CODE_KPP_TITLE, declarationKppColumn));
            declarationTable.setColumnWidth(declarationKppColumn, 6, Style.Unit.EM);
            declarationTable.addColumn(declarationOktmoColumn, declarationTable.createResizableHeader(OKTMO_TITLE, declarationOktmoColumn));
            declarationTable.setColumnWidth(declarationOktmoColumn, 6, Style.Unit.EM);
            declarationTable.addColumn(declarationTaxOrganColumn, declarationTable.createResizableHeader(TAX_ORGAN_CODE_TITLE, declarationTaxOrganColumn));
            declarationTable.setColumnWidth(declarationTaxOrganColumn, 4, Style.Unit.EM);
            declarationTable.addColumn(declarationDocStateColumn, declarationTable.createResizableHeader(DOC_STATE_TITLE, declarationDocStateColumn));
            declarationTable.setColumnWidth(declarationDocStateColumn, 7, Style.Unit.EM);
            declarationTable.addColumn(declarationDataCreationDateColumn, declarationTable.createResizableHeader(DECLARATION_DATA_CREATION_DATE_TITLE, declarationDataCreationDateColumn));
            declarationTable.setColumnWidth(declarationDataCreationDateColumn, 8.7, Style.Unit.EM);
            declarationTable.addColumn(importTfUserNameColumn, declarationTable.createResizableHeader(DECLARATION_DATA_IMPORT_TF_TITLE, importTfUserNameColumn));
            declarationTable.addColumn(fileNameColumn, declarationTable.createResizableHeader(FILE_NAME_TITLE_REPORT, fileNameColumn));
            declarationTable.addColumn(declarationNoteColumn, declarationTable.createResizableHeader(NOTE_TITLE, declarationNoteColumn));
        } else {
            declarationTable.addColumn(fileNameColumn, declarationTable.createResizableHeader(FILE_NAME_TITLE, fileNameColumn));
            if (taxType == TaxType.PFR) {
                declarationTable.addColumn(declarationDocStateColumn, declarationTable.createResizableHeader(DOC_STATE_TITLE, declarationDocStateColumn));
                declarationTable.setColumnWidth(declarationDocStateColumn, 7, Style.Unit.EM);
            }
        }

        if (!isReports) {
            declarationTable.addColumn(declarationDataCreationDateColumn, declarationTable.createResizableHeader(DECLARATION_DATA_CREATION_DATE_TITLE, declarationDataCreationDateColumn));
            declarationTable.setColumnWidth(declarationDataCreationDateColumn, 8.7, Style.Unit.EM);
            declarationTable.addColumn(importTfUserNameColumn, declarationTable.createResizableHeader(DECLARATION_DATA_IMPORT_TF_TITLE, importTfUserNameColumn));
        }
    }

    @UiHandler("recalculateButton")
    public void onRecalculateButtonClicked(ClickEvent event){
        if (getUiHandlers() != null) {
            getUiHandlers().onRecalculateClicked();
        }
    }

    @UiHandler("acceptButton")
    public void onAccept(ClickEvent event){
        getUiHandlers().accept(true);
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event){
        getUiHandlers().accept(false);
    }

    @UiHandler("deleteButton")
    public void onDelete(ClickEvent event){
        getUiHandlers().delete();
    }

    @UiHandler("checkButton")
    public void onCheck(ClickEvent event){
        getUiHandlers().check();
    }

    @UiHandler("changeStatusEDButton")
    public void onchangeStatusED(ClickEvent event){
        getUiHandlers().changeStatusED();
    }


    private void updateCheckBoxHeader(boolean value) {
        if (declarationTable.getHeaderBuilder() instanceof TableWithCheckedColumn) {
            ((TableWithCheckedColumn) declarationTable.getHeaderBuilder()).getCheckBoxHeader().setValue(value);
        }
        declarationTable.redraw();
    }

    @Override
    public List<Long> getSelectedIds() {
        List<Long> ids = new ArrayList<Long>();
        for(DeclarationDataSearchResultItem declarationDataSearchResultItem: checkedRows) {
            ids.add(declarationDataSearchResultItem.getDeclarationDataId());
        }
        return ids;
    }

    @Override
    public void clearTable() {
        while (declarationTable.getColumnCount() > 0) {
            declarationTable.removeColumn(0);
        }
    }

    @Override
	public void setInSlot(Object slot, IsWidget content) {
		if (slot == DeclarationListPresenter.TYPE_filterPresenter) {
			filterContentPanel.clear();
			if (content != null) {
				filterContentPanel.add(content);
			}
		} else {
			super.setInSlot(slot, content);
		}
	}

    @Override
    public void updateData(int pageNumber) {
        if(pageNumber == 0){
            declarationTable.getColumnSortList().clear();
        }
        if (pager.getPage() == pageNumber){
            updateData();
        } else {
            pager.setPage(pageNumber);
        }
    }

    @Override
    public void setTableData(int start, long totalCount, List<DeclarationDataSearchResultItem> records, Map<Integer, String> departmentFullNames, Map<Long, String> asnuNames, List<Long> selectedItemIds) {
        declarationTable.setRowCount((int) totalCount);
        declarationTable.setRowData(start, records);
        this.departmentFullNames = departmentFullNames;
        this.asnuNames = asnuNames;
        selectionModel.clear();
        checkedRows.clear();
        if (selectedItemIds != null) {
            for(DeclarationDataSearchResultItem item: records) {
                if (selectedItemIds.contains(item.getDeclarationDataId())) {
                    selectionModel.setSelected(item, true);
                    break;
                }
            }
        }
    }

    @Override
    public void updateData() {
        declarationTable.setVisibleRangeAndClearData(declarationTable.getVisibleRange(), true);
    }

    @Override
	public DeclarationDataSearchOrdering getSearchOrdering(){
		if (sortByColumn == null){
			setSortByColumn("");
		}
		return sortByColumn;
	}

	@Override
	public boolean isAscSorting(){
		return isAscSorting;
	}

	@Override
	public void updateTitle(TaxType taxType){
		titleDesc.setText(taxType.getName());
		if (!getUiHandlers().getIsReports()) {
            declarationHeader.setText(DECLARATION_HEADER);
        } else {
            declarationHeader.setText(DECLARATION_HEADER_R);
        }
        declarationTable.clearColumnWidth(declarationTypeColumn);
        declarationTable.clearColumnWidth(reportPeriodColumn);
        create.setText(DECLARATION_CREATE);
        create.setTitle(DECLARATION_CREATE_TITLE);
        declarationTypeHeader.setTitle(DECLARATION_TYPE_TITLE);
        reportPeriodHeader.setTitle(PERIOD_TITLE);

        declarationTable.redrawHeaders();
	}

    @UiHandler("create")
    void onCreateButtonClicked(ClickEvent event){
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateClicked();
        }
    }


    @UiHandler("createReports")
    void onCreateReportsButtonClicked(ClickEvent event){
        if (getUiHandlers() != null) {
            getUiHandlers().onCreateReportsClicked();
        }
    }

    @UiHandler("downloadReports")
    void onCreateDownloadButtonClicked(ClickEvent event){
        if (getUiHandlers() != null) {
            getUiHandlers().onDownloadReportsClicked();
        }
    }

    @Override
    public void setPage(Integer page) {
        if (page != null) pager.setPage(page);
    }

    @Override
    public int getPage() {
        return pager.getPage();
    }

	private void setSortByColumn(String sortByColumn){
		if (DEPARTMENT_TITLE.equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.DEPARTMENT_NAME;
		} else if (PERIOD_TITLE.equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.REPORT_PERIOD_YEAR;
		} else if(DECLARATION_TYPE_TITLE.equals(sortByColumn)){
			this.sortByColumn = DeclarationDataSearchOrdering.DECLARATION_TYPE_NAME;
        } else if(STATE_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.DECLARATION_STATE;
        } else if(FILE_NAME_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.FILE_NAME;
        } else if(DECLARATION_KIND_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.DECLARATION_KIND_NAME;
        } else if(ASNU_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.ASNU;
        } else if(OKTMO_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.OKTMO;
        } else if(TAX_ORGAN_CODE_KPP_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.KPP;
        } else if(TAX_ORGAN_CODE_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.TAX_ORGAN;
        } else if(CREATE_DATE_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.CREATE_DATE;
        } else if(DOC_STATE_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.DOC_STATE;
        } else if(NOTE_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.NOTE;
        } else if(DECLARATION_DATA_ID_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.ID;
        } else if(DECLARATION_DATA_CREATION_DATE_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.DECLARATION_DATA_CREATE_DATE;
        } else if(DECLARATION_DATA_IMPORT_TF_TITLE.equals(sortByColumn)){
            this.sortByColumn = DeclarationDataSearchOrdering.IMPORT_USER_NAME;
        } else {
			this.sortByColumn = DeclarationDataSearchOrdering.ID;
		}
	}

    @Override
    public void updatePageSize(TaxType taxType) {
        pager.setType("declarationList" + taxType.getCode());
        declarationTable.setPageSize(pager.getPageSize());
    }

    @Override
    public void updateButton() {
        boolean check = checkedRows.size() > 0;
        boolean calculate = check;
        boolean delete = check;
        boolean accept = check;
        boolean cancel = check;
        boolean changeStatusED = check && getUiHandlers().getIsReports();
        for(DeclarationDataSearchResultItem row: checkedRows) {
            if (State.CREATED.equals(row.getState())) {
                accept = false;
                cancel = false;
                changeStatusED = false;
            }
            if (State.PREPARED.equals(row.getState())) {
                delete = false;
                changeStatusED = false;
            }
            if (State.ACCEPTED.equals(row.getState())) {
                check = false;
                calculate = false;
                accept = false;
                delete = false;
            }
        }
        checkButton.setEnabled(check);
        recalculateButton.setEnabled(calculate);
        deleteButton.setEnabled(delete);
        acceptButton.setEnabled(accept);
        cancelButton.setEnabled(cancel);
        changeStatusEDButton.setEnabled(changeStatusED);
    }

    @Override
    public void setVisibleCancelButton(boolean isVisible) {
        cancelButton.setVisible(isVisible);
    }
}
