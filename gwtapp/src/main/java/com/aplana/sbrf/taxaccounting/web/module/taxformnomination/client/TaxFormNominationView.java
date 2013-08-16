package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * View для формы форма «Назначение налоговых форм и деклараций»
 *
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationView extends ViewWithUiHandlers<TaxFormNominationUiHandlers>
        implements TaxFormNominationPresenter.MyView {

    // признак формы: true - налоговые формы, false - декларации
    private Boolean isForm;

    // изменяемые колонки в таблице
    private TextColumn<TableModel> receiverSourcesKindColumn;
    private TextColumn<TableModel> receiverSourcesTypeColumn;

    interface Binder extends UiBinder<Widget, TaxFormNominationView> {
    }

    interface LinkStyle extends CssResource {
        String enabled();

        String disabled();
    }

    private Long depoId = null;

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
    @UiField(provided = true)
    MyValueListBox<FormDataKind> boxFormDataKind;
    @UiField(provided = true)
    MyValueListBox<FormType> boxTaxFormKind;
    @UiField
    Anchor assignAnchor;
    @UiField
    Anchor cancelAnchor;
    @UiField
    GenericDataGrid<TableModel> grid;
    @UiField(provided = true)
    DepartmentPickerWidget tree;
    @UiField
    DockLayoutPanel panelFormDataKind;
    @UiField
    Label labelKind;
    @UiField
    DockLayoutPanel panelFormDataKind2;

    private static final List<TaxType> TAX_TYPES = Arrays.asList(TaxType.values());
    private static final List<FormDataKind> FORM_DATA_KIND = Arrays.asList(FormDataKind.values());

    @Inject
    @UiConstructor
    public TaxFormNominationView(final Binder uiBinder) {

        initBoxes();

        initTree();

        initWidget(uiBinder.createAndBindUi(this));

        initTable();
    }

    private void initTree() {
        tree = new DepartmentPickerWidget("", false);
        tree.ok.setVisible(false);

    }

    private void setDepoId(Long id) {
        if ((depoId != null && !depoId.equals(id)) || (depoId == null && id != null)) {
            depoId = id;
            onDepoChange();
        }
    }

    @UiHandler("assignAnchor")
    void onClickedAssign(ClickEvent event) {
        if (depoId != null && (boxFormDataKind.getValue() != null || !isForm) && boxTaxFormKind.getValue() != null) {
            getUiHandlers().save(null);
        }
    }

    @UiHandler("cancelAnchor")
    void onClickedCancel(ClickEvent event) {
        if (isCanCancel()) {
            Set<Long> set = new HashSet<Long>();
            for (TableModel source : grid.getVisibleItems()) {
                if (source.isChecked()) {
                    set.add(source.getDepartmentFormType().getId());
                }
            }
            getUiHandlers().save(set);
        }
    }

    private boolean isCanCancel() {
        for (TableModel source : grid.getVisibleItems()) {
            if (source.isChecked()) {
                return true;
            }
        }
        return false;
    }

    private void initTable() {
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
                grid.getVisibleItem(index).setChecked(value);
                enableAnchor(cancelAnchor, isCanCancel());
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
        grid.addColumn(checkBoxColumn);
        grid.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);

        grid.addColumn(indexColumn, "№ пп");
        grid.setColumnWidth(indexColumn, 40, Style.Unit.PX);

        grid.addColumn(receiverSourcesKindColumn, "Тип налоговой формы");

        grid.addColumn(receiverSourcesTypeColumn, "Вид налоговой формы");

        clearTable();
    }

    // Выбор "Вид налога"
    private void onTaxTypeChange() {
        reloadGrid();
        reloadTaxFormKind();
    }

    // Выбор "Подразделения"
    private void onDepoChange() {
        if (isForm) {
            boxFormDataKind.setAcceptableValues(FORM_DATA_KIND);
        }
        reloadGrid();
        reloadTaxFormKind();
    }

    // Изменение выбранного значения "Вид налоговой формы"
    private void onTaxFormKindChange() {
        enableAnchor(assignAnchor, boxTaxFormKind.hasSelectedItem() && (boxFormDataKind.hasSelectedItem() || !isForm) && depoId != null);
    }

    // Перезаполнение комбика "Вид налоговой формы"/"Вид декларации"
    private void reloadTaxFormKind() {
        getUiHandlers().getTaxFormKind();
    }

    // Перезаполнение таблицы
    private void reloadGrid() {
        if (depoId == null) {
            clearTable();
            return;
        }
        getUiHandlers().getTableData();
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

        boxTaxType.addValueChangeHandler(new ValueChangeHandler<TaxType>() {
            @Override
            public void onValueChange(ValueChangeEvent<TaxType> event) {
                onTaxTypeChange();
            }
        });

        boxFormDataKind = new MyValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
            @Override
            public String render(FormDataKind object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });
        boxFormDataKind.addValueChangeHandler(new ValueChangeHandler<FormDataKind>() {
            @Override
            public void onValueChange(ValueChangeEvent<FormDataKind> event) {
                onTaxFormKindChange();
            }
        });

        boxTaxFormKind = new MyValueListBox<FormType>(new AbstractRenderer<FormType>() {
            @Override
            public String render(FormType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        boxTaxFormKind.addValueChangeHandler(new ValueChangeHandler<FormType>() {
            @Override
            public void onValueChange(ValueChangeEvent<FormType> event) {
                onTaxFormKindChange();
            }
        });

    }

    private void enableAnchor(Anchor anchor, boolean enabled) {
        if (enabled) {
            anchor.setStyleName(css.enabled());
        } else {
            anchor.setStyleName(css.disabled());
        }
    }

    private class TableModel {
        private boolean checked;
        private int index;
        private FormTypeKind departmentFormType;

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

    Map<CheckBox, Long> cbMap = new HashMap<CheckBox, Long>();

    ClickHandler handler = new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
            setDepoId(cbMap.get(event.getSource()));
        }
    };

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartment) {
        tree.setAvalibleValues(departments, availableDepartment);

        cbMap.clear();
        Iterator<TreeItem> treeItemIterator = tree.tree.treeItemIterator();
        TreeItem item;

        while (treeItemIterator.hasNext()) {
            item = treeItemIterator.next();
            CheckBox widget = (CheckBox) item.getWidget();
            cbMap.put(widget, ((Integer) ((Pair) item.getUserObject()).getFirst()).longValue());
            widget.addClickHandler(handler);
        }
    }

    // Событие "Открытие формы"
    @Override
    public void init(Boolean isForm) {
        this.isForm = isForm;
            depoId = null;

        // видимость частных полей
        formLabel.setVisible(isForm);
        formAnchor.setVisible(!isForm);
        declarationLabel.setVisible(!isForm);
        declarationAnchor.setVisible(isForm);
        panelFormDataKind.setWidgetSize(panelFormDataKind2, isForm ? 200 : 0);
        //panelFormDataKind.getWidget(DeckLayoutPanel.)setWidth("0px");
        labelKind.setText(isForm ? "Вид налоговой формы" : "Вид декларации");

        // Таблица "Список назначенных налоговых форм на подразделение": не заполняется
        clearTable();
        initTableHeader();

        // "Вид налога": "Налог на прибыль"
        boxTaxType.setValue(TaxType.INCOME, false);

        // Кнопки "Назначить" и "Отменить назначение" — неактивны
        enableAnchor(assignAnchor, false);
        enableAnchor(cancelAnchor, false);

        setTaxFormKind(new ArrayList<FormType>());
        boxFormDataKind.setAcceptableValues(new ArrayList<FormDataKind>());
    }

    private void initTableHeader() {
        if (grid.getColumnIndex(receiverSourcesKindColumn) > 0) {
            grid.removeColumn(receiverSourcesKindColumn);
        }
        grid.removeColumn(receiverSourcesTypeColumn);
        if (isForm) {
            grid.addColumn(receiverSourcesKindColumn, "Тип налоговой формы");
            grid.setColumnWidth(receiverSourcesKindColumn, "150px");
            grid.addColumn(receiverSourcesTypeColumn, "Вид налоговой формы");
        } else {
            grid.addColumn(receiverSourcesTypeColumn, "Вид декларации");
            grid.setColumnWidth(receiverSourcesTypeColumn, "100%");
        }
    }


    private void clearTable() {
        grid.setRowCount(0);
    }


    @Override
    public void setTaxFormKind(List<FormType> formTypes) {
        Collections.sort(formTypes, new Comparator<FormType>() {
            public int compare(FormType o1, FormType o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        boxTaxFormKind.setAcceptableValues(formTypes);
        onTaxFormKindChange();
    }

    @Override
    public void setTableData(List<FormTypeKind> departmentFormTypes) {
        List<TableModel> types = new ArrayList<TableModel>();

        Collections.sort(departmentFormTypes, new Comparator<FormTypeKind>() {
            public int compare(FormTypeKind o1, FormTypeKind o2) {
                Integer s11 = isForm ? o1.getKind().getId() : -1;
                Integer s12 = isForm ? o2.getKind().getId() : -1;

                if (s11.equals(s12)) {
                    String s21 = o1.getName();
                    String s22 = o2.getName();
                    if (s21.equals(s22)) {
                        return 0;
                    }
                    return s21.compareTo(s22);
                }
                return s11.compareTo(s12);
            }
        });

        int index = 1;
        for (FormTypeKind type : departmentFormTypes) {
            TableModel model = new TableModel();
            model.setChecked(false);
            model.setIndex(index++);
            model.setDepartmentFormType(type);
            types.add(model);
        }

        grid.setRowData(types);

        enableAnchor(cancelAnchor, false);
    }


    @Override
    public boolean isForm() {
        return isForm;
    }

    @Override
    public Long departmentId() {
        return depoId;
    }

    @Override
    public Integer getTypeId() {
        return isForm && boxFormDataKind.hasSelectedItem() ? boxFormDataKind.getValue().getId() : null;
    }

    @Override
    public Integer getFormId() {
        return boxTaxFormKind.hasSelectedItem() ? boxTaxFormKind.getValue().getId() : null;
    }

    @Override
    public TaxType getTaxType() {
        return boxTaxType.getValue();
    }


}