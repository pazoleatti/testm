package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.style.GenericDataGrid;
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
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * View для формы форма «Согласование организации»
 *
 * @author Stanislav Yasinskiy
 */
public class TaxFormNominationView extends ViewWithUiHandlers<TaxFormNominationUiHandlers>
        implements TaxFormNominationPresenter.MyView {


    private Boolean isForm;

    interface Binder extends UiBinder<Widget, TaxFormNominationView> {
    }

    interface LinkStyle extends CssResource {
        String enabled();

        String disabled();
    }

    private Map<Integer, FormType> receiverSourcesFormType;

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
    Anchor assignButton;
    @UiField
    Anchor cancelButton;

    @UiField
    GenericDataGrid<CheckedDepartmentFormType> receiverSourcesTable;

    @UiField
    Panel departmentPickerPanel;
    @UiField
    VerticalPanel panelFormDataKind;
    @UiField
    Label lableTaxFormKind;
    @UiField
    Label lableDeclKind;

    private static final List<TaxType> TAX_TYPES = Arrays.asList(TaxType.values());
    private static final List<FormDataKind> FORMDATA_KIND = Arrays.asList(FormDataKind.values());

    @Inject
    @UiConstructor
    public TaxFormNominationView(final Binder uiBinder) {

        initBoxes();

        initWidget(uiBinder.createAndBindUi(this));

        initTable();
    }

    @UiHandler("assignButton")
    void onClickedAssign(ClickEvent event) {
        // TODO
    }

    @UiHandler("cancelButton")
    void onClickedCancel(ClickEvent event) {
        // TODO
    }


    private void initTable() {
        Column<CheckedDepartmentFormType, Boolean> checkBoxColumn =
                new Column<CheckedDepartmentFormType, Boolean>(new CheckboxCell()) {
                    @Override
                    public Boolean getValue(CheckedDepartmentFormType object) {
                        return object.isChecked();
                    }
                };
        checkBoxColumn.setFieldUpdater(new FieldUpdater<CheckedDepartmentFormType, Boolean>() {
            @Override
            public void update(int index, CheckedDepartmentFormType object, Boolean value) {
                receiverSourcesTable.getVisibleItem(index).setChecked(value);
            }
        });

        TextColumn<CheckedDepartmentFormType> indexColumn = new TextColumn<CheckedDepartmentFormType>() {
            @Override
            public String getValue(CheckedDepartmentFormType object) {
                return "" + object.getIndex();
            }
        };
        indexColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<CheckedDepartmentFormType> receiverSourcesKindColumn = new TextColumn<CheckedDepartmentFormType>() {
            @Override
            public String getValue(CheckedDepartmentFormType object) {
                if (object.getDepartmentFormType().getKind() != null) {
                    return object.getDepartmentFormType().getKind().getName();
                } else {
                    return "";
                }
            }
        };

        TextColumn<CheckedDepartmentFormType> receiverSourcesTypeColumn = new TextColumn<CheckedDepartmentFormType>() {
            @Override
            public String getValue(CheckedDepartmentFormType object) {
                if (!receiverSourcesFormType.isEmpty()
                        && object.getDepartmentFormType().getFormTypeId() != 0) {
                    return receiverSourcesFormType.get(object.getDepartmentFormType().getFormTypeId()).getName();
                } else {
                    return "";
                }
            }
        };
        receiverSourcesTable.addColumn(checkBoxColumn);
        receiverSourcesTable.setColumnWidth(checkBoxColumn, 40, Style.Unit.PX);
        receiverSourcesTable.addColumn(indexColumn, "№ пп");
        receiverSourcesTable.setColumnWidth(indexColumn, 40, Style.Unit.PX);
        receiverSourcesTable.addColumn(receiverSourcesKindColumn, "Вид налоговой формы");
        receiverSourcesTable.setColumnWidth(receiverSourcesKindColumn, 150, Style.Unit.PX);
        receiverSourcesTable.addColumn(receiverSourcesTypeColumn, "Тип налоговой формы");
        receiverSourcesTable.setRowCount(0);
    }

    // Выбор "Вид налога"
    private void onTaxTypeChange() {
        reloadGrid();
        reloadTaxFormKind();
    }

    // Выбор "Подразделения"
    private void onDepoChange() {
        if (isForm) {
            boxFormDataKind.setAcceptableValues(FORMDATA_KIND);
        }
        reloadGrid();
        reloadTaxFormKind();
    }

    // Выбор "Тип налоговой формы"
    private void onFormDataKindChange() {
        reloadTaxFormKind();
    }

    // Изменение выбранного значения "Вид налоговой формы"
    private void onTaxFormKindChange() {
        enableAnchor(assignButton, boxTaxFormKind.getValue() != null);
    }

    private void onTableSelectedChange() {
        // TODO если выбрана хоть 1 запись в таблице
        boolean hasSelected = false;
        if (isForm) {
        } else {
        }
        enableAnchor(cancelButton, hasSelected);
    }

    // Перезаполнение комбика "Вид налоговой формы"/"Вид декларации"
    private void reloadTaxFormKind() {
        getUiHandlers().reloadTaxFormKind(boxTaxType.getValue());
    }

    // Перезаполнение таблицы 1/2
    private void reloadGrid() {
        // фильтруем по виду налога
        char taxTypeCode = boxTaxType.getValue().getCode();
        // TODO фильтруем по выбранногму подразделению
        int formDataKind = 0;
        if (isForm) {
            // TODO Перезаполнение таблицы 1
        } else {
            // TODO Перезаполнение таблицы 2
        }
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

        boxTaxFormKind = new MyValueListBox<FormType>(new AbstractRenderer<FormType>() {
            @Override
            public String render(FormType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
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

    private class CheckedDepartmentFormType {
        private boolean checked;
        private int index;
        private DepartmentFormType departmentFormType;

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public DepartmentFormType getDepartmentFormType() {
            return departmentFormType;
        }

        public void setDepartmentFormType(DepartmentFormType departmentFormType) {
            this.departmentFormType = departmentFormType;
        }

        private int getIndex() {
            return index;
        }

        private void setIndex(int index) {
            this.index = index;
        }
    }

    // Событие "Открытие формы"
    @Override
    public void init(Boolean isForm) {
        this.isForm = isForm;
        // видимость частных полей
        formLabel.setVisible(isForm);
        formAnchor.setVisible(!isForm);
        declarationLabel.setVisible(!isForm);
        declarationAnchor.setVisible(isForm);
        panelFormDataKind.setVisible(isForm);
        lableTaxFormKind.setVisible(isForm);
        lableDeclKind.setVisible(!isForm);
        // "Тип налоговой формы": не заполняется
        boxFormDataKind.setValue(null);
        // "Вид налоговой формы": не заполняется
        boxTaxFormKind.setValue(null);
        // Таблица "Список назначенных налоговых форм на подразделение": не заполняется
        receiverSourcesTable.setRowCount(0);
        // "Вид налога": "Налог на прибыль"
        boxTaxType.setValue(TaxType.INCOME, true);
        //onTaxTypeChange();
        // TODO  фокус на конкретное подразделение не установлен
        // Кнопки "Назначить" и "Отменить назначение" — неактивны
        enableAnchor(assignButton, false);
        enableAnchor(cancelButton, false);
    }

    @Override
    public boolean isForm() {
        return isForm;
    }


    @Override
    public void setTaxFormKind(List<FormType> formTypes) {
        boxTaxFormKind.setAcceptableValues(formTypes);
    }
}