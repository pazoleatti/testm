package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RefBookParamInfo;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.math.BigDecimal;
import java.util.*;

/**
 * Форма "Параметры печатной формы"
 */
public class SubreportParamsView extends PopupViewWithUiHandlers<SubreportParamsUiHandlers> implements SubreportParamsPresenter.MyView {

    private static final int STRING_VALUE_MAX_LENGTH = 2000;
    private static final int NUMBER_VALUE_PRECISION = 19;

    private Label noResultLabel = new Label();

    public interface Binder extends UiBinder<PopupPanel, SubreportParamsView> {
    }

    @UiField
    VerticalPanel editPanel;

    @UiField
    ModalWindow modalWindow;

    @UiField
    Button findButton;

    @UiField
    Button createButton;

    @UiField
    Button cancelButton;

    @UiField
    Label horSep, selectRecordLabel;

    @UiField
    ResizeLayoutPanel resultPanel;

    @UiField
    DataGrid<DataRow<Cell>> resultTable;

    @UiField
    FlexiblePager pager;

    //private final PopupPanel widget;
    Map<DeclarationSubreportParam, HasValue> widgets;

    ListDataProvider<DataRow<Cell>> model;
    private SingleSelectionModel<DataRow<Cell>> singleSelectionModel;

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    @Inject
    public SubreportParamsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        initWidget(uiBinder.createAndBindUi(this));

        pager.setDisplay(resultTable);
        // хак для горизонтального скроллбара у пустой таблицы
        resultTable.setEmptyTableWidget(noResultLabel);

        singleSelectionModel = new SingleSelectionModel<DataRow<Cell>>();
        singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (event.getSource() == null) {
                    createButton.setEnabled(false);
                } else {
                    createButton.setEnabled(true);
                }
            }
        });

        resultTable.setSelectionModel(singleSelectionModel);

        model = new ListDataProvider<DataRow<Cell>>();
        model.addDataDisplay(resultTable);

    }

    @UiHandler("findButton")
    public void onFind(ClickEvent event) {
        getUiHandlers().onFind();
    }

    @UiHandler("createButton")
    public void onCreate(ClickEvent event) {
       getUiHandlers().onCreate();
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        hide();
    }

    @Override
    public void setSubreport(DeclarationSubreport declarationSubreport, Map<Long, RefBookParamInfo> refBookParamInfoMap, Date startDate, Date endDate) {
        modalWindow.setTitle(declarationSubreport.getName());
        editPanel.clear();
        if (widgets != null) {
            widgets.clear();
        }
        if (declarationSubreport.isSelectRecord()) {
            createButton.setEnabled(false);
            findButton.setVisible(true);
            horSep.setVisible(false);
            selectRecordLabel.setVisible(false);
        } else {
            createButton.setEnabled(true);
            findButton.setVisible(false);
            horSep.setVisible(false);
            selectRecordLabel.setVisible(false);
        }
        Map<DeclarationSubreportParam, HasValue> widgets = new LinkedHashMap<DeclarationSubreportParam, HasValue>();
        for (final DeclarationSubreportParam subreportParam : declarationSubreport.getDeclarationSubreportParams()) {
            HorizontalPanel oneField = new HorizontalPanel();
            oneField.setWidth("100%");
            oneField.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

            Label label;
            if (subreportParam.isRequired()){
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.appendHtmlConstant(subreportParam.getName() + ":<span class='required'>*</span>");
                label = new HTML(builder.toSafeHtml());
            } else {
                label = new Label(subreportParam.getName() + ":");
            }
            label.getElement().getStyle().setProperty("lineHeight", "12px");
            label.getElement().getStyle().setProperty("width", "200px");
            oneField.add(label);
            oneField.setCellHorizontalAlignment(label, HasHorizontalAlignment.ALIGN_LEFT);
            oneField.setCellVerticalAlignment(label, HasVerticalAlignment.ALIGN_MIDDLE);

            final Widget widget;
            switch (subreportParam.getType()) {
                case NUMBER:
                    widget = new com.aplana.gwt.client.NumberBox(NUMBER_VALUE_PRECISION);
                    break;
                case STRING:
                    widget = new com.aplana.gwt.client.TextBox();
                    break;
                case DATE:
                    widget = new DateMaskBoxPicker();
                    if (!subreportParam.isRequired()) {
                        ((DateMaskBoxPicker) widget).setCanBeEmpty(true);
                    }
                    break;
                case REFBOOK:
                    RefBookPickerWidget refbookWidget = new RefBookPickerWidget(refBookParamInfoMap.get(subreportParam.getRefBookAttributeId()).isHierarchic(), false);
                    refbookWidget.setManualUpdate(true);
                    refbookWidget.setAttributeId(subreportParam.getRefBookAttributeId());
                    refbookWidget.setTitle(refBookParamInfoMap.get(subreportParam.getRefBookAttributeId()).getRefBookName());
                    refbookWidget.setVersionEnabled(refBookParamInfoMap.get(subreportParam.getRefBookAttributeId()).isVersioned());
                    refbookWidget.setPeriodDates(startDate, endDate);
                    refbookWidget.setFilter(subreportParam.getFilter());
                    widget = refbookWidget;
                    break;
                default:
                    widget = new com.aplana.gwt.client.TextBox();
                    break;
            }

            ((HasValue)widget).addValueChangeHandler(new ValueChangeHandler() {
                @Override
                public void onValueChange(ValueChangeEvent event) {
                    if (getUiHandlers() != null) {
                        checkValueChange(subreportParam, widget, event.getValue());
                    }
                }
            });

            widget.setWidth("100%");
            // Устанавливаем фиксированную ширину для поля типа DATE
            if (widget instanceof DateMaskBoxPicker) {
                widget.getElement().getStyle().setProperty("width", "110px");
            } else {
                //widget.getElement().getStyle().setProperty("width", "196px");
            }
            oneField.add(widget);
            oneField.setCellWidth(widget, "80%");
            oneField.setCellHorizontalAlignment(widget, HasHorizontalAlignment.ALIGN_LEFT);
            oneField.setCellVerticalAlignment(widget, HasVerticalAlignment.ALIGN_MIDDLE);

            editPanel.add(oneField);
            widgets.put(subreportParam, (HasValue) widget);
            modalWindow.center();
        }

        this.widgets = widgets;
    }

    void checkValueChange(DeclarationSubreportParam subreportParam, Widget widget, Object value) {
        switch (subreportParam.getType()) {
            case STRING:
                if (value != null && value.toString().length() > 38) {
                    widget.getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("#ffccd2");
                } else {
                    widget.getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("");
                }
                break;
            case NUMBER:
                if (widget instanceof CheckBox) {
                    return;
                }
                com.aplana.gwt.client.TextBox hasValue = new com.aplana.gwt.client.TextBox();
                hasValue.setText(value.toString());
                try {
                    checkNumber(subreportParam, hasValue, null);
                    widget.getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("");
                } catch (Exception e) {
                    widget.getElement().getFirstChildElement().getFirstChildElement()
                            .getStyle().setBackgroundColor("#ffccd2");
                }
                break;
        }
    }

    private BigDecimal checkNumber(DeclarationSubreportParam declarationSubreportParam, HasValue value, Map<String, String> errorMap)  throws BadValueException{
        BigDecimal number;
        if (value instanceof CheckBox) {
            number = value.getValue() == null ?
                    null :
                    (Boolean) value.getValue() ?
                            BigDecimal.ONE : BigDecimal.ZERO;
        } else {
            if (value.getValue() != null && !value.getValue().toString().trim().isEmpty()) {
                String valStr = (String) value.getValue();
                number = new BigDecimal(valStr);
            } else {
                number = null;
            }
        }
        return number;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getFieldsValues() throws BadValueException {
        Map<String, Object> fieldsValues = new HashMap<String, Object>();
        Map<String, String> errorMap = new LinkedHashMap<String, String>();
        boolean required = false;
        for (Map.Entry<DeclarationSubreportParam, HasValue> field : widgets.entrySet()) {
            Object value = null;
            try {
                switch (field.getKey().getType()) {
                    case NUMBER:
                        value = checkNumber(field.getKey(), field.getValue(), errorMap);
                        break;
                    case STRING:
                        String string = (field.getValue().getValue() == null || ((String)field.getValue().getValue()).trim().isEmpty()) ?
                                null : (String)field.getValue().getValue();
                        if (string!= null && string.length() > STRING_VALUE_MAX_LENGTH) {
                            errorMap.put(field.getKey().getName(), "количество символов превышает максимально допустимое = " + STRING_VALUE_MAX_LENGTH);
                        }
                        value = string;
                        break;
                    case DATE:
                        Date date = field.getValue().getValue() == null ? null : (Date)field.getValue().getValue();
                        value = date;
                        break;
                    case REFBOOK:
                        Long longValue = (field.getValue().getValue() == null || ((List<Long>) field.getValue().getValue()).isEmpty()) ? null : ((List<Long>)field.getValue().getValue()).get(0);
                        value = longValue;
                        break;
                    default:
                        break;
                }
                checkRequired(field.getKey(), value, errorMap);
                fieldsValues.put(field.getKey().getAlias(), value);
                if (value != null) {
                    required = true;
                }
            } catch (NumberFormatException nfe) {
                errorMap.put(field.getKey().getName(), "значение некорректно. Неправильный формат числа");
            } catch (ClassCastException cce) {
                errorMap.put(field.getKey().getName(), "значение некорректно");
            }
        }
        if (errorMap.isEmpty() && !required) {
            errorMap.put("", "Необходимо заполнить хотя бы одно из полей");
        }
        if (!errorMap.isEmpty())
            throw new BadValueException(errorMap);
        return fieldsValues;
    }

    private void checkRequired(DeclarationSubreportParam declarationSubreportParam, Object val, Map<String, String> errorMap) throws BadValueException {
        if (declarationSubreportParam.isRequired() && (val == null)) {
            errorMap.put(declarationSubreportParam.getName(), "обязателен для заполнения");
        }
    }

    @Override
    public void setTableData(PrepareSpecificReportResult prepareSpecificReportResult) {
        if (prepareSpecificReportResult == null) {
            pager.setVisible(false);
            resultPanel.setVisible(false);
            resultPanel.setHeight("0px");
            horSep.setVisible(false);
            selectRecordLabel.setVisible(false);
            modalWindow.center();
        } else {
            pager.setVisible(true);
            resultPanel.setVisible(true);
            resultPanel.setHeight("150px");
            horSep.setVisible(true);
            selectRecordLabel.setVisible(true);
            modalWindow.center();

            factory.setReadOnly(true);

            while (resultTable.getColumnCount() > 0) {
                resultTable.removeColumn(0);
            }

            for (Column column: prepareSpecificReportResult.getTableColumns()) {
                com.google.gwt.user.cellview.client.Column<DataRow<Cell>, ?> tableCol = factory.createTableColumn(column, resultTable);
                resultTable.addColumn(tableCol, column.getName());
                resultTable.setColumnWidth(tableCol, column.getWidth(), Style.Unit.EM);
            }
            model.getList().clear();
            if (prepareSpecificReportResult.getDataRows() != null) {
                model.getList().addAll(prepareSpecificReportResult.getDataRows());
            }
            resultTable.redraw();
        }
    }

    @Override
    public DataRow<Cell> getSelectedRow() {
        return singleSelectionModel.getSelectedObject();
    }
}
