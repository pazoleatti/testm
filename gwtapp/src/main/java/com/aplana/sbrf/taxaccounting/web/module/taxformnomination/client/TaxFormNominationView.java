package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.aplana.sbrf.taxaccounting.web.widget.style.LinkButton;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.*;
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

    // изменяемые колонки в таблице
    private TextColumn<TableModel> receiverSourcesKindColumn;
    private TextColumn<TableModel> receiverSourcesTypeColumn;
    private TextColumn<TableModel> departmentColumn;
    private TextColumn<TableModel> performerColumn;
    private TextColumn<TableModel> declarationType;

    interface Binder extends UiBinder<Widget, TaxFormNominationView> {
    }

    interface LinkStyle extends CssResource {
        String enabled();

        String disabled();
    }

    //private Long depoId = null;

    @UiField
    LinkStyle css;
    @UiField
    Anchor formAnchor;
    @UiField
    Label formLabel;
    @UiField
    Anchor declarationAnchor;
    @UiField
    Label declarationLabel;
    @UiField(provided = true)
    MyValueListBox<TaxType> boxTaxType;
    @UiField
    Anchor assignAnchor;
    @UiField
    Anchor cancelAnchor;
    @UiField
    GenericDataGrid<TableModel> formGrid;
    @UiField
    GenericDataGrid<TableModel> declarationGrid;
    @UiField
    LinkButton editAnchor;
    @UiField
    DepartmentPickerPopupWidget departmentPicker;
    @UiField
    Button search;
    @UiField
    Button assign;

    private static final List<TaxType> TAX_TYPES = Arrays.asList(TaxType.values());
    private static final List<FormDataKind> FORM_DATA_KIND = Arrays.asList(FormDataKind.values());
    // список с инде4ксами выделенных строк в гриде
    private Set<Integer> selectedRows;

    @Inject
    @UiConstructor
    public TaxFormNominationView(final Binder uiBinder) {
        selectedRows = new HashSet<Integer>();
        initBoxes();
        initWidget(uiBinder.createAndBindUi(this));
        initFormGrid();
        initDeclarationGrid();
        formViewInit();
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
                formGrid.getVisibleItem(index).setChecked(value);
                if (value) {
                    selectedRows.add(index);
                } else {
                    selectedRows.remove(index);
                }

                cancelAnchor.setEnabled(selectedRows.size() > 0);
                //enableAnchor(cancelAnchor, selectedRows.size() > 0);
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
                return object.getDepartment().getName();
            }
        };

        performerColumn = new TextColumn<TableModel>() {
            @Override
            public String getValue(TableModel object) {
                return object.getPerformer() != null ? object.getPerformer().getName():"";
            }
        };

        formGrid.addColumn(checkBoxColumn);
        formGrid.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);

        formGrid.addColumn(indexColumn, "№ пп");
        formGrid.setColumnWidth(indexColumn, 40, Style.Unit.PX);

        formGrid.addColumn(departmentColumn, "Подразделение");

        formGrid.addColumn(receiverSourcesKindColumn, "Тип налоговой формы");

        formGrid.addColumn(receiverSourcesTypeColumn, "Вид налоговой формы");

        formGrid.addColumn(performerColumn, "Исполнитель");
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
	            declarationGrid.getVisibleItem(index).setChecked(value);
                //enableAnchor(cancelAnchor, isCanCancel());
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
                return object.getDepartment().getName();
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

        declarationGrid.addColumn(departmentColumn, "Подразделение");

        declarationGrid.addColumn(declarationType, "Вид декларации");
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

    private void initBoxes() {
        boxTaxType = new MyValueListBox<TaxType>(new AbstractRenderer<TaxType>() {
            @Override
            public String render(TaxType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });
        boxTaxType.setAcceptableValues(TAX_TYPES);
    }

    private class TableModel {
        private boolean checked;
        private int index;
        private FormTypeKind departmentFormType;
        private Department department;
        private Department performer;

        public Department getPerformer() {
            return performer;
        }

        public void setPerformer(Department performer) {
            this.performer = performer;
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
    public void init(TaxType nType) {
        this.isForm = true;
        // Вид налога: в зависимости от налога, выбранного в главном меню ("Вид налога": "Налог на прибыль")
        boxTaxType.setValue((nType != null ? nType : TaxType.INCOME), false);
        setTaxFormKind(new ArrayList<FormType>());
        //boxFormDataKind.setAcceptableValues(new ArrayList<FormDataKind>());
        setHandlers();
    }

    private void setHandlers(){
        // переключение режима просмотра на "Назначение налоговых форм"
        formAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formViewInit();
                event.preventDefault();
            }
        });

        // переключение режима просмотра на "Назначение деклараций"
        declarationAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                declarationViewInit();
                event.preventDefault();
            }
        });

        search.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                //setDepoId(departmentPicker.getValue());
                if (isForm) reloadFormGrid();
                else reloadDeclarationGrid();
            }
        });
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

    private List<FormTypeKind> getSelectedItems(GenericDataGrid<TableModel> grid) {
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
        formGrid.setVisible(true);
        declarationGrid.setVisible(false);
    }

    /**
     * Установить вид представления на "Назначение деклараций" (а-ля вкладки)
     * и применить изменения к представлению
     *
     * TODO Год: указанный в назначении налоговых форм (в 0.3.5 не реализуем)
     * TODO Таблица "Список назначенных деклараций на подразделение": не заполнена
     */
    private void declarationViewInit(){
        isForm = false;
        renderNav();
        // Кнопка "Редактировать" — не отображается
        editAnchor.setVisible(false);
        // Кнопка "Отменить назначение" — неактивна (в 0.3.7 удаляем)
        cancelAnchor.setEnabled(false);
        // показать соответствующую таблицу
        formGrid.setVisible(false);
        declarationGrid.setVisible(true);
        // очистить таблицу с декларациями
        declarationGrid.setRowCount(0);
    }

    /**
     * Отображение навигации (а-ля вкладки)
     */
    private void renderNav(){
        formLabel.setVisible(isForm);
        formAnchor.setVisible(!isForm);
        // показать соответствующую таблицу
        declarationLabel.setVisible(!isForm);
        declarationAnchor.setVisible(isForm);
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
    public void setDataToFormTable(List<FormTypeKind> departmentFormTypes) {
        selectedRows.clear();
        List<TableModel> types = new ArrayList<TableModel>();

        Collections.sort(departmentFormTypes, new Comparator<FormTypeKind>() {
            /**
             * Порядок сортировки записей при отображении:
             * - Подразделение
             * - Тип налоговой формы
             * - Вид налоговой формы
             *
             * @param o1
             * @param o2
             * @return
             */
            public int compare(FormTypeKind o1, FormTypeKind o2) {
                int result = o1.getDepartment().getName().compareTo(o2.getDepartment().getName());
                if (result == 0){
                    result = o1.getKind().getName().compareTo(o2.getKind().getName());
                    if (result == 0){
                        result = o1.getName().compareTo(o2.getName());
                    }
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
            model.setPerformer(type.getPerformer());
            types.add(model);
        }

        formGrid.setRowData(types);
    }

    @Override
    public void setDataToDeclarationTable(List<FormTypeKind> departmentFormTypes) {
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
            model.setPerformer(type.getPerformer());
            types.add(model);
        }

        declarationGrid.setRowData(types);
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


    @Override
    public TaxType getTaxType() {
        return boxTaxType.getValue();
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

    private void enableAnchor(Anchor anchor, boolean enabled) {
        if (enabled) {
            anchor.setStyleName(css.enabled());
        } else {
            anchor.setStyleName(css.disabled());
        }
        anchor.setEnabled(enabled);
    }

	@UiHandler("cancelAnchor")
	public void clickCancelAnchor(ClickEvent event){
		if (getUiHandlers() != null) {
            getUiHandlers().onClickFormCancelAnchor();
        } else {
            getUiHandlers().onClickDeclarationCancelAnchor();
        }

	}

}