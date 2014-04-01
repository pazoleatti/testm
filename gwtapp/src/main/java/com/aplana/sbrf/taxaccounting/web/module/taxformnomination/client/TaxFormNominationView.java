package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericCellTable;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkAnchor;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * View для формы форма «Назначение налоговых форм и деклараций»
 *
 * Реализовано согласно <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9583288">аналитике</a>
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationView extends ViewWithUiHandlers<TaxFormNominationUiHandlers>
        implements TaxFormNominationPresenter.MyView {

    /**
     * признак формы: true - налоговые формы, false - декларации
     */
    private Boolean isForm;
    private TaxType taxType;

    // изменяемые колонки в таблице
    private GenericCellTable.TableCellResizableHeader receiverSourcesKindTitle, receiverSourcesTypeTitle, declarationTypeHeader;


    private TextColumn<TableModel> receiverSourcesKindColumn;
    private TextColumn<TableModel> receiverSourcesTypeColumn;
    private TextColumn<TableModel> departmentColumn;
    private TextColumn<TableModel> performerColumn;
    private TextColumn<TableModel> declarationType;

    interface Binder extends UiBinder<Widget, TaxFormNominationView> {
    }

    //private Long depoId = null;
    @UiField
    Button assignAnchor;
    @UiField
    Anchor cancelAnchor;
    @UiField
    GenericCellTable<TableModel> formGrid;
    @UiField
    GenericCellTable<TableModel> declarationGrid;
    @UiField
    LinkButton editAnchor;
    @UiField
    DepartmentPickerPopupWidget departmentPicker;
    @UiField
    Button search;
    @UiField
    Label taxTypeLabel;
    @UiField
    LinkAnchor switchMode;
    @UiField
    Label formHeader;
    @UiField
    ScrollPanel formGridWrapper;
    @UiField
    ScrollPanel declarationGridWrapper;
    @UiField
    FlexiblePager formPager;
    @UiField
    FlexiblePager declarationPager;

    private final AsyncDataProvider<TableModel> formDataProvider = new AsyncDataProvider<TableModel>() {
        @Override
        protected void onRangeChanged(HasData<TableModel> display) {
            if (getUiHandlers() != null){
                final Range range = display.getVisibleRange();
                getUiHandlers().onFormRangeChange(range.getStart(), range.getLength());
            }
        }
    };

    private final AsyncDataProvider<TableModel> declarationDataProvider = new AsyncDataProvider<TableModel>() {
        @Override
        protected void onRangeChanged(HasData<TableModel> display) {
            if (getUiHandlers() != null){
                final Range range = display.getVisibleRange();
                getUiHandlers().onDeclarationRangeChange(range.getStart(), range.getLength());
            }
        }
    };

    @Inject
    @UiConstructor
    public TaxFormNominationView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        formDataProvider.addDataDisplay(formGrid);
        declarationDataProvider.addDataDisplay(declarationGrid);
        initFormGrid();
        initDeclarationGrid();
        //formViewInit();
    }

    /**
     * Инициализация таблицы отображающий данные вкладки "Назначение налоговых форм"
     */
    private void initFormGrid() {
        Column<TableModel, Boolean> checkBoxColumn =
                new Column<TableModel, Boolean>(new CheckboxCell()) {
                    @Override
                    public Boolean getValue(TableModel object) {
                        return object.isChecked();
                    }
                };
        checkBoxColumn.setFieldUpdater(new FieldUpdater<TableModel, Boolean>() {
            @Override
            public void update(int index, TableModel object, Boolean value) {
                formGrid.getVisibleItem(index - formPager.getPageStart()).setChecked(value);
                updatePanelAnchors();
            }
        });

        TextColumn<TableModel> indexColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel object) {
                return "" + object.getIndex();
            }
        };
        indexColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        receiverSourcesKindColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel object) {
                if (object.getDepartmentFormType().getKind() != null) {
                    return object.getDepartmentFormType().getKind().getName();
                } else {
                    return "";
                }
            }
        };

        receiverSourcesTypeColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel object) {
                FormTypeKind departmentFormType = object.getDepartmentFormType();
                if (departmentFormType != null && departmentFormType.getId() != 0) {
                    return departmentFormType.getName();
                } else {
                    return "";
                }
            }
        };

        departmentColumn = new TextColumn<TableModel>(){
            @Override
            public String getValue(TableModel object) {
                return object.getDepartmentName();
            }
        };

        performerColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel object) {
                return object.getPerformer() != null ? object.getPerformerName():"";
            }
        };

        formGrid.addColumn(checkBoxColumn);
        formGrid.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);

        formGrid.addColumn(indexColumn, "№ пп");
        formGrid.setColumnWidth(indexColumn, 40, Style.Unit.PX);

        formGrid.addResizableColumn(departmentColumn, "Подразделение");

        receiverSourcesKindTitle = formGrid.createResizableHeader("Тип налоговой формы", receiverSourcesKindColumn);
        formGrid.addColumn(receiverSourcesKindColumn, receiverSourcesKindTitle);

        receiverSourcesTypeTitle = formGrid.createResizableHeader("Вид налоговой формы", receiverSourcesTypeColumn);
        formGrid.addColumn(receiverSourcesTypeColumn, receiverSourcesTypeTitle);

        formGrid.addResizableColumn(performerColumn, "Исполнитель");
    }

    /**
     * Обновление линков редактировать/отменить назначение
     */
    @Override
    public void updatePanelAnchors(){
        int selectedCount = getSelectedItemsOnFormGrid().size();
        cancelAnchor.setEnabled(selectedCount  > 0);
        editAnchor.setEnabled(selectedCount  > 0);
    }

    @Override
    public void updateDeclarationPanelAnchors(){
        int selectedCount = getSelectedItems(declarationGrid).size();
        cancelAnchor.setEnabled(selectedCount  > 0);
    }

    private void initDeclarationGrid(){
        Column<TableModel, Boolean> checkBoxColumn =
                new Column<TableModel, Boolean>(new CheckboxCell()) {
                    @Override
                    public Boolean getValue(TableModel object) {
                        return object.isChecked();
                    }
                };
        checkBoxColumn.setFieldUpdater(new FieldUpdater<TableModel, Boolean>() {
            @Override
            public void update(int index, TableModel object, Boolean value) {
                declarationGrid.getVisibleItem(index - declarationPager.getPageStart()).setChecked(value);
                updateDeclarationPanelAnchors();
            }
        });

        TextColumn<TableModel> indexColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel object) {
                return "" + object.getIndex();
            }
        };
        indexColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        departmentColumn = new TextColumn<TableModel>(){
            @Override
            public String getValue(TableModel object) {
                return object.getDepartmentName();
            }
        };

        declarationType  = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel object) {
                FormTypeKind departmentFormType = object.getDepartmentFormType();
                if (departmentFormType != null && departmentFormType.getId() != 0) {
                    return departmentFormType.getName();
                } else {
                    return "";
                }
            }
        };

        declarationGrid.addColumn(checkBoxColumn);
        declarationGrid.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);

        declarationGrid.addColumn(indexColumn, "№ пп");
        declarationGrid.setColumnWidth(indexColumn, 40, Style.Unit.PX);

        declarationGrid.addResizableColumn(departmentColumn, "Подразделение");

        declarationTypeHeader = declarationGrid.createResizableHeader("Вид декларации", declarationType);
        declarationGrid.addColumn(declarationType, declarationTypeHeader);
    }

    // Перезаполнение таблицы
    private void reloadFormGrid() {
        /*if (depoId == null) {
            clearTable();
            return;
        }*/
        getUiHandlers().reloadFormTableData();
    }

    private void reloadDeclarationGrid(){
        getUiHandlers().reloadDeclarationTableData();
    }

    // TODO не надо в формах определять классы моделек. вынести в shared
    private class TableModel {
        private boolean checked;
        private int index;
        private FormTypeKind departmentFormType;
        private Department department;
        private String departmentName;
        private Department performer;
        private String performerName;

        public String getPerformerName() {
            return performerName;
        }

        public void setPerformerName(String performerName) {
            this.performerName = performerName;
        }

        public Department getPerformer() {
            return performer;
        }

        public void setPerformer(Department performer) {
            this.performer = performer;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public Department getDepartment() {
            return department;
        }

        public void setDepartment(Department departmetn) {
            this.department = departmetn;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public FormTypeKind getDepartmentFormType() {
            return departmentFormType;
        }

        public void setDepartmentFormType(FormTypeKind departmentFormType) {
            this.departmentFormType = departmentFormType;
        }

        private int getIndex() {
            return index;
        }

        private void setIndex(int index) {
            this.index = index;
        }
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartment) {
        departmentPicker.setAvalibleValues(departments, availableDepartment);
    }

    /**
     * Событие "Открытие формы"
     * Инициализируется при создании формы
     */
    @Override
    public void init(TaxType nType, boolean isForm) {
        this.isForm = isForm;
        this.taxType = nType;
        // Вид налога: в зависимости от налога, выбранного в главном меню ("Вид налога": "Налог на прибыль")
        taxTypeLabel.setText(nType!= null ? nType.getName() : "Неизвестный вид налога");
        setupHeader();
        if(isForm){
            formViewInit();
        } else {
            declarationViewInit();
        }
    }

    @Override
    public List<Integer> getDepartments(){
        return departmentPicker.getValue();
    }

	@Override
	public List<FormTypeKind> getSelectedItemsOnDeclarationGrid() {
		return getSelectedItems(declarationGrid);
	}

    @Override
    public List<FormTypeKind> getSelectedItemsOnFormGrid() {
        return getSelectedItems(formGrid);
    }

    private void setupHeader(){
        boolean isTaxTypeDeal = taxType.equals(TaxType.DEAL);
        receiverSourcesKindTitle.setTitle(!isTaxTypeDeal?"Тип налоговой формы":"Тип формы");
        receiverSourcesTypeTitle.setTitle(!isTaxTypeDeal?"Вид налоговой формы":"Вид формы");
        declarationTypeHeader.setTitle(!isTaxTypeDeal?"Вид декларации":"Вид уведомления");
        formGrid.redrawHeaders();
        declarationGrid.redrawHeaders();

        // леваяя ссылка
        switchMode.setText(getHeader(isForm, isTaxTypeDeal));
        // средний лейбл
        formHeader.setText(getHeader(!isForm, isTaxTypeDeal));
    }

    private List<FormTypeKind> getSelectedItems(CellTable<TableModel> grid) {
        List<FormTypeKind> selected = new ArrayList<FormTypeKind>();
        for (TableModel row : grid.getVisibleItems()) {
            if (row.isChecked()) {
                selected.add(row.departmentFormType);
            }
        }
        return selected;
    }

	/**
     * Установить вид представления на "Назначение налоговых форм"
     * и применить изменения к представлению
     *
     * TODO: Год: не заполнен (в 0.3.5 не реализуем)
     * TODO Таблица "Список назначенных налоговых форм на подразделение": не заполнена
     *
     * TODO можно и в один метод с параметром isForm запилить
     */
    private void formViewInit(){
        isForm = true;
        renderNav();
        // Кнопка "Редактировать" — неактивна
        editAnchor.setEnabled(false);
        editAnchor.setVisible(true);
        // Кнопка "Отменить назначение" — неактивна (в 0.3.7 удаляем)
        cancelAnchor.setEnabled(false);
        // показать соответствующую таблицу
        formGridWrapper.setVisible(true);
        declarationGridWrapper.setVisible(false);

        formPager.setDisplay(formGrid);
        formPager.setVisible(true);
        declarationPager.setVisible(false);
    }

    /**
     * Установить вид представления на "Назначение деклараций" (а-ля вкладки)
     * и применить изменения к представлению
     *
     * TODO Год: указанный в назначении налоговых форм (в 0.3.5 не реализуем)
     * TODO Таблица "Список назначенных деклараций на подразделение": не заполнена
     *
     * TODO можно и в один метод с параметром isForm запилить
     */
    private void declarationViewInit(){
        isForm = false;
        renderNav();
        // Кнопка "Редактировать" — не отображается
        editAnchor.setVisible(false);
        // Кнопка "Отменить назначение" — неактивна (в 0.3.7 удаляем)
        cancelAnchor.setEnabled(false);
        // показать соответствующую таблицу
        formGridWrapper.setVisible(false);
        declarationGridWrapper.setVisible(true);
        // очистить таблицу с декларациями
        declarationGrid.setRowCount(0);

        formPager.setVisible(false);
        declarationPager.setVisible(true);
        declarationPager.setDisplay(declarationGrid);
    }

    /**
     * Отображение навигации (а-ля вкладки)
     */
    private void renderNav(){
        setupHeader();
        // очистить таблицу
        formGrid.setRowCount(0);
    }

    @Override
    public void setTaxFormKind(List<FormType> formTypes) {
        Collections.sort(formTypes, new Comparator<FormType>() {
            public int compare(FormType o1, FormType o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    @Override
    public void setDataToFormTable(int start, int totalCount, List<FormTypeKind> departmentFormTypes, Map<Integer, String> departmentFullNames) {
        List<TableModel> types = new ArrayList<TableModel>();

        int index = start + 1;
        for (FormTypeKind type : departmentFormTypes) {
            TableModel model = new TableModel();
            model.setChecked(false);
            model.setIndex(index++);
            model.setDepartmentFormType(type);
            model.setDepartment(type.getDepartment());
            model.setDepartmentName(departmentFullNames.get(type.getDepartment().getId()));
            model.setPerformer(type.getPerformer());
            if (type.getPerformer() != null) model.setPerformerName(departmentFullNames.get(type.getPerformer().getId()));
            types.add(model);
        }

        formGrid.setRowCount(totalCount);
        formGrid.setRowData(start, types);
    }

    @Override
    public void setDataToDeclarationTable(int start, int totalCount, List<FormTypeKind> departmentFormTypes, Map<Integer, String> departmentFullNames) {
	    if (departmentFormTypes.isEmpty()) {
		    declarationGrid.setRowCount(0);
		    return;
	    }
        List<TableModel> types = new ArrayList<TableModel>();

        Collections.sort(departmentFormTypes, new Comparator<FormTypeKind>() {
            public int compare(FormTypeKind o1, FormTypeKind o2) {
                int result = o1.getDepartment().getName().compareTo(o2.getDepartment().getName());
                if (result == 0){
                    result = o1.getName().compareTo(o2.getName());
                }

                return result;
            }
        });

        int index = 1;
        for (FormTypeKind type : departmentFormTypes) {
            TableModel model = new TableModel();
            model.setChecked(false);
            model.setIndex(index++);
            model.setDepartmentFormType(type);
            model.setDepartment(type.getDepartment());
            model.setDepartmentName(departmentFullNames.get(type.getDepartment().getId()));
            model.setPerformer(type.getPerformer());
            types.add(model);
        }


        declarationGrid.setRowCount(totalCount);
        declarationGrid.setRowData(start, types);
    }

    @Override
    public boolean isForm() {
        return isForm;
    }

    @Override
    public Integer getTypeId() {
        return null; //isForm && boxFormDataKind.hasSelectedItem() ? boxFormDataKind.getValue().getId() : null;
    }

    @Override
    public Integer getFormId() {
        return null;//boxTaxFormKind.hasSelectedItem() ? boxTaxFormKind.getValue().getId() : null;
    }

    @UiHandler("search")
    public void onSearchClick(ClickEvent event) {
        //setDepoId(departmentPicker.getValue());
        if (isForm) {
            reloadFormGrid();
            formPager.firstPage();
        } else {
            reloadDeclarationGrid();
	        formPager.firstPage();
        }
    }

    @UiHandler("switchMode")
    public void onSwitchModeClick(ClickEvent event) {
        // если уже режим формы то включаем режим декларации
        if (isForm) {
            declarationViewInit();
        } else {
            formViewInit();
        }
        event.preventDefault();
    }


    @UiHandler("editAnchor")
    public void clickEdit(ClickEvent event) {
        if(getUiHandlers() != null){
            getUiHandlers().onClickEditFormDestinations(getSelectedItemsOnFormGrid());
        }
    }

    @UiHandler("assignAnchor")
    public void clickAssignAnchor(ClickEvent event){
        if(getUiHandlers() != null){
            if (isForm){
                getUiHandlers().onClickOpenFormDestinations();
            } else {
                getUiHandlers().onClickOpenDeclarationDestinations();
            }
        }
    }

    @UiHandler("cancelAnchor")
    public void clickCancelAnchor(ClickEvent event) {
        if (getUiHandlers() != null) {
            if (isForm) {
                getUiHandlers().onClickFormCancelAnchor();
            } else {
                getUiHandlers().onClickDeclarationCancelAnchor();
            }
        }
    }

    @Override
    public void onReveal() {
        departmentPicker.setValue(null, false);
    }

    @Override
    public void clearFilter() {
        departmentPicker.clearFilter();
    }

    @Override
    public void setDepartments(List<Integer> department){
        departmentPicker.setValue(department);
    }

    private String getHeader(boolean isForm, boolean isTaxTypeDeal){
        return isForm ? (!isTaxTypeDeal ? "Назначение деклараций" : "Назначение уведомлений") : (!isTaxTypeDeal ? "Назначение налоговых форм" : "Назначение форм");
    }
}