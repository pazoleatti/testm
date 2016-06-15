package com.aplana.sbrf.taxaccounting.web.module.sources.client;

import com.aplana.gwt.client.Spinner;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.SourcesSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.AsyncDataProviderWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.AssignDialogView;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.assingDialog.ButtonClickHandlers;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LabelSeparator;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.aplana.sbrf.taxaccounting.web.widget.style.table.CheckBoxHeader;
import com.aplana.sbrf.taxaccounting.web.widget.utils.WidgetUtils;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.web.module.sources.client.SourcesView.Table.LEFT;
import static com.google.gwt.view.client.DefaultSelectionEventManager.createCustomManager;

/**
 * Представление формы "Указаник источников/приемников"
 * <p/>
 * Таблицы адаптированы под одновременное содержания как моделей
 * назначений форм так и моделей назначения деклараций.
 * На форме есть 2 переключателя: представление (НФ/декларация) и
 * способ назначения (Источник->Приемники / Пприемник->Источники).
 * <p/>
 * Поэтому может быть 4 состояния:
 * 1. Форма П->И
 * 2. Форма И->П
 * 3. Декларация П->И
 * 4. Декларация И->П
 *
 * @author many people
 * @author aivanov
 * @since 20.05.2014
 */
public class SourcesView extends ViewWithUiHandlers<SourcesUiHandlers> implements SourcesPresenter.MyView {

    interface Binder extends UiBinder<Widget, SourcesView> {
    }

    /**
     * Состояние формы
     * 1. Формы Приемник->Источники
     * 2. Формы Источник->Приемники
     * 3. Декларации Приемник->Источники
     * 4. Декларации Источник->Приемники
     */
    public enum FormState {
        FORM_REC_SOUR,      //1.Form_Recipient_Sources,
        FORM_SOUR_REC,      //2.Form_Source_Recipients,
        DEC_REC_SOUR,       //3.Declaration_Recipient_Sources,
        DEC_SOUR_REC        //4.Declaration_Source_Recipients
    }

    public static final String TITLE_FORM = "Налоговые формы";
    public static final String TITLE_DEC = "Декларации";
    public static final String TITLE_FORM_DEAL = "Формы";
    public static final String TITLE_DEC_DEAL = "Уведомления";

    private static final int CHECK_BOX_COLUMN_WIDTH = 40;

    @UiField(provided = true)
    ValueListBox<AppointmentType> appointmentTypePicker;

    @UiField(provided = true)
    ValueListBox<PeriodInfo>
            periodFrom,
            periodTo;

    @UiField(provided = true)
    com.aplana.gwt.client.ValueListBox<TaxType> taxTypeList;

    @UiField
    Spinner yearFrom,
            yearTo;

    @UiField
    GenericDataGrid<DepartmentAssign>
            rightTable,
            leftTable;
    @UiField
    GenericDataGrid<CurrentAssign> downTable;

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
    LinkButton formDecAnchor;
    @UiField
    Label titleLabel,
            taxTypeLabel,
            formDecLabel;

    @UiField
    LabelSeparator
            downLabel,
            leftLabel,
            rightLabel;

    @UiField
    HTMLPanel panel, leftPanel, rightPanel;

    @UiField
    HTML verSep;

    private GenericDataGrid.DataGridResizableHeader
            leftFormKindHeader,
            leftNameTypeHeader;

    private GenericDataGrid.DataGridResizableHeader
            rightFormKindHeader,
            rightNameTypeHeader;
    private final CheckBoxHeader rightCheckBoxHeader = new CheckBoxHeader();

    private GenericDataGrid.DataGridResizableHeader
            downFormKindHeader,
            downNameTypeHeader;

    private final CheckBoxHeader downCheckBoxHeader = new CheckBoxHeader();

    // для красивости можно и на Column заменить
    private TextColumn<DepartmentAssign> leftFormKindColumn;
    private TextColumn<DepartmentAssign> leftNameTypeColumn;

    private Column<DepartmentAssign, Boolean> rightCheckBoxColumn;
    private IdentityColumn<DepartmentAssign> rightFormKindColumn;
    private IdentityColumn<DepartmentAssign> rightNameTypeColumn;

    private Column<CurrentAssign, Boolean> downCheckBoxColumn;
    private IdentityColumn<CurrentAssign> downIndexColumn;
    private IdentityColumn<CurrentAssign> downAssignKindColumn;
    private IdentityColumn<CurrentAssign> downDepartmentColumn;
    private IdentityColumn<CurrentAssign> downNameTypeColumn;
    private IdentityColumn<CurrentAssign> downStartColumn;
    private IdentityColumn<CurrentAssign> downEndColumn;
    private IdentityColumn<CurrentAssign> downTaxTypeColumn;

    private boolean isForm = true;
    private boolean isTaxTypeDeal = false;
    private boolean isTaxTypeETRorMarket = false;

    private SingleSelectionModel<DepartmentAssign> leftSM;
    private MultiSelectionModel<DepartmentAssign> rightSM;
    private MultiSelectionModel<CurrentAssign> downSM;

    private AsyncDataProviderWithSortableTable leftTableDataProvider;
    private AsyncDataProviderWithSortableTable rightTableDataProvider;
    private AsyncDataProviderWithSortableTable downTableDataProvider;
    private SourcesSearchOrdering sortByColumnLeftTable;
    private SourcesSearchOrdering sortByColumnRightTable;
    private SourcesSearchOrdering sortByColumnDownTable;

    private Table table;
    private Integer defaultDepartment;

    public enum Table {
        LEFT, RIGHT, DOWN
    }

    ProvidesKey<DepartmentAssign> providesKey;

    String formKindColumnTitle = "Тип налоговой формы";
    String nameTypeColumnTitle = "Вид налоговой формы";

    @Inject
    @UiConstructor
    public SourcesView(final Binder uiBinder) {
        ValueBoxRenderer abstractRenderer = new ValueBoxRenderer();
        appointmentTypePicker = new ValueListBox<AppointmentType>(abstractRenderer);
        periodFrom = new ValueListBox<PeriodInfo>(abstractRenderer, new ProvidesKey<PeriodInfo>() {
            @Override
            public Object getKey(PeriodInfo item) {
                return item != null ? item.getCode() : null;
            }
        });
        periodTo = new ValueListBox<PeriodInfo>(abstractRenderer, new ProvidesKey<PeriodInfo>() {
            @Override
            public Object getKey(PeriodInfo item) {
                return item != null ? item.getCode() : null;
            }
        });
        taxTypeList = new com.aplana.gwt.client.ValueListBox<TaxType>(new AbstractRenderer<TaxType>() {
            @Override
            public String render(TaxType item) {
                return item.getName();
            }
        }) {
            @Override
            protected void updateLabelValue() {
                setLabelValue(((TaxType)((HasValue) this).getValue()).getName());
            }
        };
        taxTypeList.addValueChangeHandler(new ValueChangeHandler<TaxType>() {
            @Override
            public void onValueChange(ValueChangeEvent<TaxType> event) {
                loadRightData();
            }
        });

        initWidget(uiBinder.createAndBindUi(this));

        providesKey = new ProvidesKey<DepartmentAssign>() {
            @Override
            public Object getKey(DepartmentAssign item) {
                return item.getId();
            }
        };

        setupControlWidgets();

        setupLeftTables();
        setupRightTables();
        setupDownTables();

        leftTableDataProvider = new AsyncDataProviderWithSortableTable<DepartmentAssign, SourcesUiHandlers, SourcesView>(leftTable, this) {
            @Override
            public SourcesUiHandlers getViewUiHandlers() {
                table = LEFT;
                return getUiHandlers();
            }
        };

        rightTableDataProvider = new AsyncDataProviderWithSortableTable<DepartmentAssign, SourcesUiHandlers, SourcesView>(rightTable, this) {
            @Override
            public SourcesUiHandlers getViewUiHandlers() {
                table = Table.RIGHT;
                return getUiHandlers();
            }
        };

        downTableDataProvider = new AsyncDataProviderWithSortableTable<CurrentAssign, SourcesUiHandlers, SourcesView>(downTable, this) {
            @Override
            public SourcesUiHandlers getViewUiHandlers() {
                table = Table.DOWN;
                return getUiHandlers();
            }
        };

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                setPanelWidth();
            }
        });
        setPanelWidth();
    }

    @Override
    public void setPanelWidth() {
        int width = panel.getOffsetWidth();
        leftPanel.getElement().getStyle().setRight(width / 2 + 7 + CHECK_BOX_COLUMN_WIDTH / 2, Style.Unit.PX);
        rightPanel.getElement().getStyle().setLeft(width / 2 + 7 - CHECK_BOX_COLUMN_WIDTH / 2, Style.Unit.PX);
    }

    @Override
    public void clearSelection() {
        downSM.clear();
        rightSM.clear();
    }

    @Override
    public TaxType getSelectedTaxType() {
        return taxTypeList.getValue();
    }

    /**
     * Настройка левой таблицы
     */
    private void setupLeftTables() {
        leftSM = new SingleSelectionModel<DepartmentAssign>(providesKey);
        leftFormKindColumn = new TextColumn<DepartmentAssign>() {
            @Override
            public String getValue(DepartmentAssign object) {
                return object.getKind() != null ? object.getKind().getTitle() : "";
            }
        };

        leftNameTypeColumn = new TextColumn<DepartmentAssign>() {
            @Override
            public String getValue(DepartmentAssign object) {
                return object.getTypeName();
            }
        };

        leftFormKindHeader = leftTable.createResizableHeader(formKindColumnTitle, leftFormKindColumn);
        leftNameTypeHeader = leftTable.createResizableHeader(nameTypeColumnTitle, leftNameTypeColumn);

        leftSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                if (leftSM.getSelectedObject() != null) {
                    if (!rightSM.getSelectedSet().isEmpty()) {
                        assignButton.setEnabled(true);
                    }
                    getUiHandlers().getCurrentAssigns(leftSM.getSelectedObject());
                    loadRightData();
                } else {
                    clearDownTable();
                    assignButton.setEnabled(false);
                }
            }
        });
        leftTable.setSelectionModel(leftSM);
        leftTable.setEmptyTableWidget(SourcesUtils.getEmptyWidget(leftDepPicker));

        leftTable.addResizableColumn(leftFormKindColumn, "");
        leftTable.addResizableColumn(leftNameTypeColumn, "");

        leftFormKindColumn.setDataStoreName(SourcesSearchOrdering.KIND.name());
        leftNameTypeColumn.setDataStoreName(SourcesSearchOrdering.TYPE.name());
    }

    /**
     * Настройка правой таблицы
     */
    private void setupRightTables() {
        rightSM = new MultiSelectionModel<DepartmentAssign>(providesKey){
            @Override
            public Set<DepartmentAssign> getSelectedSet() {
                Set<DepartmentAssign> set = super.getSelectedSet();
                Iterator<DepartmentAssign> it = set.iterator();
                while (it.hasNext()) {
                    DepartmentAssign assign = it.next();
                    if (!assign.isEnabled())
                        it.remove();
                }
                return set;
            }
        };

        rightFormKindColumn = new IdentityColumn<DepartmentAssign>(new AbstractCell<DepartmentAssign>(){
            @Override
            public void render(Context context, DepartmentAssign value, SafeHtmlBuilder sb) {
                if (value != null) {
                    String text = value.getKind() != null ? value.getKind().getTitle() : "";
                    if (value.isEnabled()) {
                        sb.appendHtmlConstant(text);
                    } else {
                        sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + text + "</span>");
                    }
                }
            }
        });

        rightNameTypeColumn =  new IdentityColumn<DepartmentAssign>(new AbstractCell<DepartmentAssign>(){
            @Override
            public void render(Context context, DepartmentAssign value, SafeHtmlBuilder sb) {
                if (value != null) {
                    String text = value.getTypeName();
                    if (value.isEnabled()) {
                        sb.appendHtmlConstant(text);
                    } else {
                        sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + text + "</span>");
                    }
                }
            }
        });
        rightCheckBoxColumn = new Column<DepartmentAssign, Boolean>(new CheckboxCell(true, false){
            @Override
            public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                // Get the view data.
                DepartmentAssign key = (DepartmentAssign)context.getKey();
                Boolean viewData = getViewData(key);
                if (viewData != null && viewData.equals(value)) {
                    clearViewData(key);
                    viewData = null;
                }
                String checked = "";
                String enabled = "";
                if (key.isChecked() || value != null && ((viewData != null) ? viewData : value)) {
                    checked = "checked ";
                }
                if (!key.isEnabled()) {
                    enabled = "disabled ";
                }
                sb.appendHtmlConstant("<input type=\"checkbox\" tabindex=\"-1\" " + checked + enabled +"/>");

            }
        }) {
            @Override
            public Boolean getValue(DepartmentAssign object) {
                return (object == null || object.getId() == null) ? null : rightSM.isSelected(object);
            }
        };
        rightCheckBoxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    for (DepartmentAssign departmentAssign : rightTable.getVisibleItems()) {
                        if (departmentAssign.isEnabled()) {
                            rightSM.setSelected(departmentAssign, true);
                        }
                    }
                } else {
                    rightSM.clear();
                }
            }
        });

        rightFormKindHeader = rightTable.createResizableHeader(formKindColumnTitle, rightFormKindColumn);
        rightNameTypeHeader = rightTable.createResizableHeader(nameTypeColumnTitle, rightNameTypeColumn);

        rightSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                rightCheckBoxHeader.setValue(rightTable.getRowCount() != 0 && rightSM.getSelectedSet().size() == rightTable.getRowCount());
                assignButton.setEnabled(!rightSM.getSelectedSet().isEmpty() && leftSM.getSelectedObject() != null);
            }
        });
        rightTable.setSelectionModel(rightSM, createCustomManager(
                new DefaultSelectionEventManager.CheckboxEventTranslator<DepartmentAssign>(0) {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<DepartmentAssign> event) {
                        return false;
                    }

                    @Override
                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<DepartmentAssign> event) {
                        if (event.getValue() != null && event.getValue().isChecked())
                            return DefaultSelectionEventManager.SelectAction.SELECT;
                        else if (event.getValue() != null && !event.getValue().isEnabled())
                            return DefaultSelectionEventManager.SelectAction.IGNORE;
                        return DefaultSelectionEventManager.SelectAction.TOGGLE;
                    }
                }));

        rightTable.setEmptyTableWidget(SourcesUtils.getEmptyWidget(rightDepPicker));

        rightTable.addResizableColumn(rightFormKindColumn, "");
        rightTable.addResizableColumn(rightNameTypeColumn, "");

        rightFormKindColumn.setDataStoreName(SourcesSearchOrdering.KIND.name());
        rightNameTypeColumn.setDataStoreName(SourcesSearchOrdering.TYPE.name());
    }

    /**
     * Создание и настройка нижней таблицы
     */
    private void setupDownTables() {
        downSM = new MultiSelectionModel<CurrentAssign>(new ProvidesKey<CurrentAssign>() {
            @Override
            public Object getKey(CurrentAssign item) {
                return item.getId() + "_"
                        + item.getStartDateAssign().getTime() + "_"
                        + (item.getEndDateAssign() != null ? item.getEndDateAssign().getTime() : "null");
            }
        }) {
            @Override
            public Set<CurrentAssign> getSelectedSet() {
                Set<CurrentAssign> set = super.getSelectedSet();
                Iterator<CurrentAssign> it = set.iterator();
                while (it.hasNext()) {
                    CurrentAssign assign = it.next();
                    if (!assign.isEnabled())
                        it.remove();
                }
                return set;
            }
        };

        downCheckBoxColumn = new Column<CurrentAssign, Boolean>(new CheckboxCell(true, false){
            @Override
            public void render(Context context, Boolean value, SafeHtmlBuilder sb) {
                // Get the view data.
                CurrentAssign key = (CurrentAssign)context.getKey();
                Boolean viewData = getViewData(key);
                if (viewData != null && viewData.equals(value)) {
                    clearViewData(key);
                    viewData = null;
                }
                String checked = "";
                String enabled = "";
                if (value != null && ((viewData != null) ? viewData : value)) {
                    checked = "checked ";
                }
                if (!key.isEnabled()) {
                    enabled = "disabled ";
                }
                sb.appendHtmlConstant("<input type=\"checkbox\" tabindex=\"-1\" " + checked + enabled +"/>");
            }
        }) {
            @Override
            public Boolean getValue(CurrentAssign object) {
                return (object == null || object.getId() == null) ? null : downSM.isSelected(object);
            }
        };

        downCheckBoxHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                if (event.getValue()) {
                    for (CurrentAssign currentAssign : downTable.getVisibleItems()) {
                        if (currentAssign.isEnabled())
                            downSM.setSelected(currentAssign, true);
                    }
                } else {
                    downSM.clear();
                }
            }
        });


        downIndexColumn = new IdentityColumn<CurrentAssign>(new AbstractCell<CurrentAssign>() {
            @Override
            public void render(Context context, CurrentAssign value, SafeHtmlBuilder sb) {
                if (value.isEnabled()) {
                    sb.append(context.getIndex() + 1);
                } else {
                    sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + (context.getIndex() + 1) + "</span>");
                }
            }
        });
        downIndexColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        downTaxTypeColumn = new IdentityColumn<CurrentAssign>(new AbstractCell<CurrentAssign>(){
            @Override
            public void render(Context context, CurrentAssign value, SafeHtmlBuilder sb) {
                if (value != null) {
                    String text = value.getTaxType().getName();
                    if (value.isEnabled()) {
                        sb.appendHtmlConstant(text);
                    } else {
                        sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + text + "</span>");
                    }
                }
            }
        });

        downDepartmentColumn = new IdentityColumn<CurrentAssign>(new AbstractCell<CurrentAssign>(){
            @Override
            public void render(Context context, CurrentAssign value, SafeHtmlBuilder sb) {
                if (value != null) {
                    String text = value.getDepartmentName();
                    if (value.isEnabled()) {
                        sb.appendHtmlConstant(text);
                    } else {
                        sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + text + "</span>");
                    }
                }
            }
        });

        downAssignKindColumn = new IdentityColumn<CurrentAssign>(new AbstractCell<CurrentAssign>(){
            @Override
            public void render(Context context, CurrentAssign value, SafeHtmlBuilder sb) {
                if (value != null) {
                    String text = value.getFormKind() != null ? value.getFormKind().getTitle() : "—";
                    if (value.isEnabled()) {
                        sb.appendHtmlConstant(text);
                    } else {
                        sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + text + "</span>");
                    }
                }
            }
        });

        downNameTypeColumn = new IdentityColumn<CurrentAssign>(new AbstractCell<CurrentAssign>(){
            @Override
            public void render(Context context, CurrentAssign value, SafeHtmlBuilder sb) {
                if (value != null) {
                    String text = value.getName();
                    if (value.isEnabled()) {
                        sb.appendHtmlConstant(text);
                    } else {
                        sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + text + "</span>");
                    }
                }
            }
        });

        downStartColumn = new IdentityColumn<CurrentAssign>(new AbstractCell<CurrentAssign>(){
            @Override
            public void render(Context context, CurrentAssign value, SafeHtmlBuilder sb) {
                if (value != null) {
                    String rend = value.getStartDateAssign() != null ? WidgetUtils.dateTimeFormat.format(value.getStartDateAssign()) : "—";
                    if (value.isEnabled()) {
                        sb.append(SafeHtmlUtils.fromString(rend));
                    } else {
                        sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + SafeHtmlUtils.fromString(rend).asString() + "</span>");
                    }
                }
            }
        });

        downEndColumn = new IdentityColumn<CurrentAssign>(new AbstractCell<CurrentAssign>(){
            @Override
            public void render(Context context, CurrentAssign value, SafeHtmlBuilder sb) {
                if (value != null) {
                    String rend = value.getEndDateAssign() != null ? WidgetUtils.dateTimeFormat.format(value.getEndDateAssign()) : "—";
                    if (value.isEnabled()) {
                        sb.append(SafeHtmlUtils.fromString(rend));
                    } else {
                        sb.appendHtmlConstant("<span class=\"gwt-TextBox-readonly\">" + SafeHtmlUtils.fromString(rend).asString() + "</span>");
                    }
                }
            }
        });

        downStartColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        downEndColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        downFormKindHeader = downTable.createResizableHeader(formKindColumnTitle, downAssignKindColumn);
        downNameTypeHeader = downTable.createResizableHeader(nameTypeColumnTitle, downNameTypeColumn);

        downSM.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                int selectedCount = downSM.getSelectedSet().size();
                downCheckBoxHeader.setValue(selectedCount == downTable.getRowCount());
                editButton.setEnabled(selectedCount > 0);
                cancelButton.setEnabled(selectedCount > 0);
            }
        });

        downTable.setSelectionModel(downSM, createCustomManager(
                new DefaultSelectionEventManager.CheckboxEventTranslator<CurrentAssign>(0) {
                    @Override
                    public boolean clearCurrentSelection(CellPreviewEvent<CurrentAssign> event) {
                        return false;
                    }

                    @Override
                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent(CellPreviewEvent<CurrentAssign> event) {
                        if (event.getValue() != null && !event.getValue().isEnabled())
                            return DefaultSelectionEventManager.SelectAction.IGNORE;
                        return DefaultSelectionEventManager.SelectAction.TOGGLE;
                    }
                }));

        final Label lab = new Label("Для просмотра данных выберите запись в таблице слева.");
        lab.getElement().getStyle().setColor("#aeaeac");
        lab.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    boolean isSources = isSource();
                    if (leftSM.getSelectedObject() == null) {
                        lab.setText("Для просмотра данных выберите запись в таблице слева.");
                    } else {
                        lab.setText("У выбранного " + (isSources ? "приемника" : " источника") + " нет указанных " + (isSources ? "источников." : "приемников."));
                    }
                }
            }
        });
        downTable.setEmptyTableWidget(lab);

        downTable.addResizableColumn(downTaxTypeColumn, "");
        downTable.addResizableColumn(downDepartmentColumn, "");
        downTable.addResizableColumn(downAssignKindColumn, "");
        downTable.addResizableColumn(downNameTypeColumn, "");
        downTable.addResizableColumn(downStartColumn, "");
        downTable.addResizableColumn(downEndColumn, "");

        downTaxTypeColumn.setDataStoreName(SourcesSearchOrdering.TAX_TYPE.name());
        downDepartmentColumn.setDataStoreName(SourcesSearchOrdering.DEPARTMENT.name());
        downAssignKindColumn.setDataStoreName(SourcesSearchOrdering.KIND.name());
        downNameTypeColumn.setDataStoreName(SourcesSearchOrdering.TYPE.name());
        downStartColumn.setDataStoreName(SourcesSearchOrdering.START.name());
        downEndColumn.setDataStoreName(SourcesSearchOrdering.END.name());
    }

    /**
     * Создание и настройка верхних виджетов
     */
    private void setupControlWidgets() {
        ValueChangeHandler<PeriodInfo> periodTitleChanger = new ValueChangeHandler<PeriodInfo>() {
            @Override
            public void onValueChange(ValueChangeEvent<PeriodInfo> event) {
                SourcesUtils.setupPeriodTitle((ValueListBox<PeriodInfo>) event.getSource());
            }
        };

        periodFrom.addValueChangeHandler(periodTitleChanger);
        periodTo.addValueChangeHandler(periodTitleChanger);

        yearFrom.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                yearFrom.setTitle(event.getValue() != null ? event.getValue() + " год" : "");
            }
        });
        yearTo.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                yearTo.setTitle(event.getValue() != null ? event.getValue() + " год" : "");
            }
        });
    }

    @Override
    public void init(TaxType taxType, List<AppointmentType> types, AppointmentType type, int year, List<PeriodInfo> periods,
                     boolean isForm, boolean isControlUNP) {
        this.isForm = isForm;
        this.isTaxTypeDeal = taxType.equals(TaxType.DEAL);
        this.isTaxTypeETRorMarket = taxType.equals(TaxType.ETR) || taxType.equals(TaxType.MARKET);

        taxTypeLabel.setText(taxType.getName());

        //настрока листбоксов
        appointmentTypePicker.setValue(type);
        appointmentTypePicker.setAcceptableValues(types);
        WidgetUtils.setupOptionTitle(appointmentTypePicker);

        periodFrom.setValue(periods.get(0));
        periodFrom.setAcceptableValues(periods);
        WidgetUtils.setupOptionTitle(periodFrom);
        SourcesUtils.setupPeriodTitle(periodFrom);

        periodTo.setValue(null);
        periodTo.setAcceptableValues(periods);
        WidgetUtils.setupOptionTitle(periodTo);
        SourcesUtils.setupPeriodTitle(periodTo);

        taxTypeList.setValue(taxType);
        if (isControlUNP) {
            taxTypeList.setAcceptableValues(Arrays.asList(TaxType.values()));
            taxTypeList.setEnabled(true);
        } else {
            taxTypeList.setAcceptableValues(Arrays.asList(taxType));
            taxTypeList.setEnabled(false);
        }

        yearFrom.setValue(year);
        yearTo.setValue(null);

        assignButton.setEnabled(false);
        cancelButton.setEnabled(false);
        editButton.setEnabled(false);

        setupView(isForm, isTaxTypeDeal, isTaxTypeETRorMarket, AppointmentType.SOURCES.equals(type));
    }

    /**
     * Основной метод переключения состояний формы
     *
     * @param isForm        true - представление формы, иначе представление декларации
     * @param isTaxTypeDeal true - тип налога "Учет КС", иначе все остальные
     * @param isSources     true - назанчение источников, иначе назанчение приемников
     */
    private void setupView(boolean isForm, boolean isTaxTypeDeal, boolean isTaxTypeETRorMarket, boolean isSources) {

        leftLabel.setText(isSources ? "Приемник" : "Источник");
        rightLabel.setText(isSources ? "Источники" : "Приемники");
        downLabel.setText("Указанные " + (isSources ? "источники" : "приемники"));

        if (isTaxTypeETRorMarket) {
            verSep.setVisible(false);
            formDecAnchor.setVisible(false);
        } else {
            verSep.setVisible(true);
            formDecAnchor.setVisible(true);
            formDecAnchor.setText(isForm ?
                    (isTaxTypeDeal ? TITLE_DEC_DEAL : TITLE_DEC) :
                    (isTaxTypeDeal ? TITLE_FORM_DEAL : TITLE_FORM));
        }

        formDecLabel.setText(!isForm ?
                (isTaxTypeDeal ? TITLE_DEC_DEAL : TITLE_DEC) :
                (isTaxTypeDeal || isTaxTypeETRorMarket ? TITLE_FORM_DEAL : TITLE_FORM));

        clearLeftTable();
        clearRightTable();
        clearDownTable();

        leftTable.removeAllColumns();
        rightTable.removeAllColumns();
        downTable.removeAllColumns();

        formKindColumnTitle = isTaxTypeDeal || isTaxTypeETRorMarket ? "Тип формы" : "Тип налоговой формы";
        leftFormKindHeader.setTitle(formKindColumnTitle);
        rightFormKindHeader.setTitle(formKindColumnTitle);
        downFormKindHeader.setTitle(formKindColumnTitle);

        boolean isNotThirdState = !FormState.DEC_REC_SOUR.equals(getState(isForm, isSources));     // флаг что сейчас 1 2 или 4 состояние
        boolean isNotFourState = !FormState.DEC_SOUR_REC.equals(getState(isForm, isSources));       // флаг что сейчас 1 2 или 3 состояние
        leftNameTypeHeader.setTitle(getNameTypeHeaderTitle(isNotThirdState));
        rightNameTypeHeader.setTitle(getNameTypeHeaderTitle(isNotFourState));
        downNameTypeHeader.setTitle(getNameTypeHeaderTitle(isNotFourState));

        if (isNotThirdState) {
            leftTable.addColumn(leftFormKindColumn, leftFormKindHeader, 110, Style.Unit.PX);
        }

        leftTable.addColumn(leftNameTypeColumn, leftNameTypeHeader, 100, Style.Unit.PCT);

        rightTable.addColumn(rightCheckBoxColumn, rightCheckBoxHeader, CHECK_BOX_COLUMN_WIDTH, Style.Unit.PX);
        if (isNotFourState) {
            rightTable.addColumn(rightFormKindColumn, rightFormKindHeader, 110, Style.Unit.PX);
        }
        rightTable.addColumn(rightNameTypeColumn, rightNameTypeHeader, 100, Style.Unit.PCT);

        downTable.addColumn(downCheckBoxColumn, downCheckBoxHeader, 40, Style.Unit.PX);
        downTable.addColumn(downIndexColumn, "№ пп", 40, Style.Unit.PX);
        downTable.addResizableColumn(downTaxTypeColumn, "Налог", 150, Style.Unit.PX);
        downTable.addResizableColumn(downDepartmentColumn, "Подразделение", 250, Style.Unit.PX);
        if (isNotFourState) {
            downTable.addColumn(downAssignKindColumn, downFormKindHeader, 250, Style.Unit.PX);
        }

        downTable.addColumn(downNameTypeColumn, downNameTypeHeader, 100, Style.Unit.PCT);
        downTable.addColumn(downStartColumn, "Начало назначения", 90, Style.Unit.PX);
        downTable.addColumn(downEndColumn, "Окончание назначения", 90, Style.Unit.PX);

        leftTable.redrawHeaders();
        rightTable.redrawHeaders();
        downTable.redrawHeaders();

        loadLeftData(null);
        loadRightData();
    }

    @Override
    public void setAvailableFormRight(List<DepartmentAssign> departmentFormTypes) {
        clearRightTable();
        rightTable.setRowData(0, departmentFormTypes);
        rightTable.setVisibleRange(new Range(0, departmentFormTypes.size()));
        for(DepartmentAssign departmentAssign: departmentFormTypes) {
            if (departmentAssign.isChecked())
                rightSM.setSelected(departmentAssign, true);
        }
    }

    @Override
    public void setAvailableFormsLeft(List<DepartmentAssign> departmentFormTypes, DepartmentAssign selectedLeftRecord) {
        clearLeftTable();
        leftTable.setRowData(departmentFormTypes);
        leftTable.setVisibleRange(new Range(0, departmentFormTypes.size()));
        if (selectedLeftRecord != null) {
            leftSM.setSelected(selectedLeftRecord, true);
            getUiHandlers().getCurrentAssigns(selectedLeftRecord);
        } else {
            if (leftTable.getRowCount() > 0) {
                leftSM.setSelected(leftTable.getVisibleItem(0), true);
                getUiHandlers().getCurrentAssigns(leftTable.getVisibleItem(0));
            }
        }
        loadRightData();
    }

    @Override
    public void setAvailableDecsRight(List<DepartmentAssign> departmentDeclarationTypes) {
        clearRightTable();
        rightTable.setRowData(departmentDeclarationTypes);
        rightTable.setVisibleRange(new Range(0, departmentDeclarationTypes.size()));
        for(DepartmentAssign departmentAssign: departmentDeclarationTypes) {
            if (departmentAssign.isChecked())
                rightSM.setSelected(departmentAssign, true);
        }
    }

    @Override
    public void setAvailableDecsLeft(List<DepartmentAssign> departmentDeclarationTypes, DepartmentAssign selectedLeftRecord) {
        clearLeftTable();
        leftTable.setRowData(departmentDeclarationTypes);
        leftTable.setVisibleRange(new Range(0, departmentDeclarationTypes.size()));
        if (selectedLeftRecord != null) {
            leftSM.setSelected(selectedLeftRecord, true);
            getUiHandlers().getCurrentAssigns(selectedLeftRecord);
        } else {
            if (leftTable.getRowCount() > 0) {
                leftSM.setSelected(leftTable.getVisibleItem(0), true);
                getUiHandlers().getCurrentAssigns(leftTable.getVisibleItem(0));
            }
        }
        loadRightData();
    }

    @Override
    public void setCurrentSources(List<CurrentAssign> departmentFormTypes) {
        clearDownTable();
        downTable.setRowData(0, departmentFormTypes);
        downTable.setVisibleRange(new Range(0, departmentFormTypes.size()));
    }

    @Override
    public final PeriodsInterval getPeriodInterval() {
        return new PeriodsInterval(yearFrom.getValue(), periodFrom.getValue(), yearTo.getValue(), periodTo.getValue());
    }

    @Override
    public final boolean isSource() {
        return AppointmentType.SOURCES.equals(appointmentTypePicker.getValue());
    }

    @Override
    public boolean isDeclaration() {
        return !isForm;
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments, Integer departmentId) {
        leftDepPicker.setAvalibleValues(departments, availableDepartments);
        defaultDepartment = departmentId;
        if (defaultDepartment != null) {
            leftDepPicker.setValue(Arrays.asList(defaultDepartment));
        } else {
            leftDepPicker.setValue(null);
        }
        rightDepPicker.setAvalibleValues(departments, availableDepartments);
        rightDepPicker.setValue(null);
    }

    @Override
    public boolean isAscSorting() {
        return false;
    }

    @Override
    public boolean isAscSorting(Table table) {
        boolean isAscSorting = false;

        switch (table) {
            case LEFT:
                isAscSorting = leftTableDataProvider.isAscSorting();
                break;
            case RIGHT:
                isAscSorting = rightTableDataProvider.isAscSorting();
                break;
            case DOWN:
                isAscSorting = downTableDataProvider.isAscSorting();
        }

        return isAscSorting;
    }

    @Override
    public SourcesSearchOrdering getSearchOrderingLeftTable() {
        if (sortByColumnLeftTable == null) {
            sortByColumnLeftTable = SourcesSearchOrdering.TYPE;
        }
        return sortByColumnLeftTable;
    }

    @Override
    public SourcesSearchOrdering getSearchOrderingRightTable() {
        if (sortByColumnRightTable == null) {
            sortByColumnRightTable = SourcesSearchOrdering.TYPE;
        }
        return sortByColumnRightTable;
    }

    @Override
    public SourcesSearchOrdering getSearchOrderingDownTable() {
        if (sortByColumnDownTable == null) {
            sortByColumnDownTable = SourcesSearchOrdering.TYPE;
        }
        return sortByColumnDownTable;
    }

    @Override
    public void setSortByColumn(String sortByColumn) {
        switch (this.table) {
            case LEFT:
                this.sortByColumnLeftTable = SourcesSearchOrdering.valueOf(sortByColumn);
                break;
            case RIGHT:
                this.sortByColumnRightTable = SourcesSearchOrdering.valueOf(sortByColumn);
                break;
            case DOWN:
                this.sortByColumnDownTable = SourcesSearchOrdering.valueOf(sortByColumn);
                break;
        }
    }

    @UiHandler("formDecAnchor")
    public void changeView(ClickEvent event) {
        this.isForm = !this.isForm;
        appointmentTypePicker.setValue(AppointmentType.SOURCES);
        if (defaultDepartment != null) {
            leftDepPicker.setValue(Arrays.asList(defaultDepartment));
        } else {
            leftDepPicker.setValue(null);
        }
        rightDepPicker.setValue(null);
        setupView(isForm, isTaxTypeDeal, isTaxTypeETRorMarket, isSource());
    }

    @UiHandler("assignButton")
    public void assign(ClickEvent event) {
        if (leftSM.getSelectedObject() == null || rightSM.getSelectedSet().isEmpty()) {
            return;
        }
        getUiHandlers().openAssignDialog(
                isSource() ? AssignDialogView.State.CREATE_SOURCES : AssignDialogView.State.CREATE_RECEPIENTS,
                getPeriodInterval(),
                new ButtonClickHandlers() {
                    @Override
                    public void ok(PeriodsInterval periodsInterval) {
                        getUiHandlers().createAssign(leftSM.getSelectedObject(), rightSM.getSelectedSet(), periodsInterval, leftDepPicker.getValue(), rightDepPicker.getValue());
                    }

                    @Override
                    public void cancel() {
                        getUiHandlers().closeAssignDialog();
                    }
                });
    }

    @UiHandler("cancelButton")
    public void cancel(ClickEvent event) {
        if (downSM.getSelectedSet().isEmpty()) {
            return;
        }

        final List<Long> sourceIds = new ArrayList<Long>();
        for (CurrentAssign source : downSM.getSelectedSet()) {
            sourceIds.add(source.getId());
        }
        Dialog.confirmMessage("Удаление назначений", "Вы действительно хотите удалить выбранные назначения?", new DialogHandler() {
            @Override
            public void yes() {
                getUiHandlers().deleteCurrentAssign(leftSM.getSelectedObject(), downSM.getSelectedSet());
            }
        });
    }

    @UiHandler("editButton")
    public void edit(ClickEvent event) {
        if (downSM.getSelectedSet().isEmpty()) {
            return;
        }
        /*if (downSM.getSelectedSet().size() > 1) {
            Dialog.errorMessage("Возможно редактирование не более одной записи!");
            return;
        }*/
        getUiHandlers().prepareUpdateAssign(leftSM.getSelectedObject(), downSM.getSelectedSet());
    }

    @UiHandler("appointmentTypePicker")
    public void change(ValueChangeEvent<AppointmentType> event) {
        setupView(isForm, isTaxTypeDeal, isTaxTypeETRorMarket, isSource());
    }

    /**
     * @see FormState
     */
    @UiHandler("leftDepPicker")
    public void leftChangeDep(ValueChangeEvent<List<Integer>> event) {
        loadLeftData(null);
        loadRightData();
    }

    @Override
    public final void loadLeftData(DepartmentAssign selectedLeftRecord) {
        clearLeftTable();
        clearDownTable();
        Integer selectedDepartment = leftDepPicker.getSingleValue();
        if (selectedDepartment != null) {
            if (isForm) {
                getUiHandlers().getFormsLeft(selectedDepartment, selectedLeftRecord);
            } else {
                if (isSource()) {
                    getUiHandlers().getDecsLeft(selectedDepartment, selectedLeftRecord);
                } else {
                    getUiHandlers().getFormsLeft(selectedDepartment, selectedLeftRecord);
                }
            }
        }
    }

    /**
     * @see FormState
     */
    @UiHandler("rightDepPicker")
    public void rightChangeDep(ValueChangeEvent<List<Integer>> event) {
        loadRightData();
    }

    @Override
    public final void loadRightData() {
        clearRightTable();
        Integer selected = rightDepPicker.getSingleValue();
        if (selected != null) {
            if (isForm) {
                getUiHandlers().getFormsRight(selected, leftSM.getSelectedObject());
            } else {
                if (isSource()) {
                    getUiHandlers().getFormsRight(selected, leftSM.getSelectedObject());
                } else {
                    getUiHandlers().getDecsRight(selected, leftSM.getSelectedObject());
                }
            }
        }
    }

    @UiHandler("apply")
    void onApplyButtonClicked(ClickEvent event) {
        if (periodTo.getValue() == null) {
            yearTo.setValue(null);
        }
        boolean isPeriodCorrect = SourcesUtils.isCorrectPeriod(getPeriodInterval());
        //Фикс какой то странной ошибки - если таблицы уже активны и попытаться сделать их активными снова, то гвт падает
        if (leftDepPicker.isEnabled() && !isPeriodCorrect) {
            leftDepPicker.setEnabled(false);
            rightDepPicker.setEnabled(false);
            leftTable.setEnabled(false);
            rightTable.setEnabled(false);
            downTable.setEnabled(false);
        }

        if (!leftDepPicker.isEnabled() && isPeriodCorrect) {
            leftDepPicker.setEnabled(true);
            rightDepPicker.setEnabled(true);
            leftTable.setEnabled(true);
            rightTable.setEnabled(true);
            downTable.setEnabled(true);
        }

        if (isPeriodCorrect) {
            DepartmentAssign selectedLeftRecord = leftSM.getSelectedObject();
            leftSM.clear();
            rightSM.clear();
            downSM.clear();
            loadLeftData(selectedLeftRecord);
            loadRightData();
        } else {
            Dialog.errorMessage("Указание источников/приемников", "Неверно указан период!");
        }
    }

    @Override
    public Table getTable() {
        return table;
    }

    private void clearLeftTable() {
        leftTable.setRowCount(0);
        leftSM.clear();
    }

    private void clearRightTable() {
        Set<DepartmentAssign> departmentAssignSet = rightSM.getSelectedSet();
        for(DepartmentAssign departmentAssign: departmentAssignSet)
            rightSM.setSelected(departmentAssign, false);
        rightSM.clear();
        //rightTable.
        rightTable.setRowCount(0);
    }

    private void clearDownTable() {
        downTable.setRowCount(0);
        downSM.clear();
    }

    /**
     * Текущее состояние формы
     *
     * @see FormState
     */
    public FormState getState(boolean isForm, boolean isSources) {
        return isForm ?
                isSources ? FormState.FORM_REC_SOUR : FormState.FORM_SOUR_REC :
                isSources ? FormState.DEC_REC_SOUR : FormState.DEC_SOUR_REC;
    }

    private String getNameTypeHeaderTitle(boolean isForm) {
        return isForm ?
                (isTaxTypeDeal || isTaxTypeETRorMarket ? "Вид формы" : "Вид налоговой формы") :
                (isTaxTypeDeal ? "Вид уведомления" : "Вид декларации");
    }
}