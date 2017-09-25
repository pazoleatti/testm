package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.mask.ui.TextMaskBox;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreportParam;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreportParamType;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RefBookParamInfo;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.WarnValueException;
import com.aplana.sbrf.taxaccounting.web.widget.datarow.DataRowColumnFactory;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.pager.FlexiblePager;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Event;
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
    Label horSep, selectRecordLabel, infoLabel;

    @UiField
    ResizeLayoutPanel resultPanel;

    @UiField
    DataGrid<DataRow<Cell>> resultTable;

    @UiField
    FlexiblePager pager;

    @UiField
    HTMLPanel infoPanel;

    //private final PopupPanel widget;
    Map<DeclarationSubreportParam, HasValue> widgets;

    ListDataProvider<DataRow<Cell>> model;
    private SingleSelectionModel<DataRow<Cell>> singleSelectionModel;

    private DataRowColumnFactory factory = new DataRowColumnFactory();

    private HandlerRegistration nativePreviewHandler;

    final String SNILS_ALIAS = "snils";

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
            if (subreportParam.isRequired()) {
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
                    if (subreportParam.getAlias().equals(SNILS_ALIAS)) {
                        widget = new TextMaskBox("XXX-XXX-XXX XX");
                    } else {
                        widget = new com.aplana.gwt.client.TextBox();
                    }
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

            /*((HasValue) widget).addValueChangeHandler(new ValueChangeHandler() {
                @Override
                public void onValueChange(ValueChangeEvent event) {
                    if (getUiHandlers() != null) {
                        checkValueChange(subreportParam, widget, event.getValue());
                    }
                }
            });*/

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

    private BigDecimal checkNumber(DeclarationSubreportParam declarationSubreportParam, HasValue value, Map<String, String> errorMap) throws BadValueException {
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
    public Map<String, Object> getFieldsValues() throws BadValueException, WarnValueException {
        Map<String, Object> fieldsValues = new HashMap<String, Object>();
        Map<String, String> errorMap = new LinkedHashMap<String, String>();
        Map<String, String> warnMap = new LinkedHashMap<String, String>();
        boolean required = false;
        for (Map.Entry<DeclarationSubreportParam, HasValue> field : widgets.entrySet()) {
            Object value = null;
            try {
                switch (field.getKey().getType()) {
                    case NUMBER:
                        value = checkNumber(field.getKey(), field.getValue(), warnMap);
                        break;
                    case STRING:
                        String string = (field.getValue().getValue() == null || ((String) field.getValue().getValue()).trim().isEmpty()) ?
                                null : (String) field.getValue().getValue();
                        if (string != null && string.length() > STRING_VALUE_MAX_LENGTH) {
                            warnMap.put(field.getKey().getName(), "количество символов превышает максимально допустимое = " + STRING_VALUE_MAX_LENGTH);
                        }
                        value = string;
                        break;
                    case DATE:
                        Date date = field.getValue().getValue() == null ? null : (Date) field.getValue().getValue();
                        value = date;
                        break;
                    case REFBOOK:
                        Long longValue = (field.getValue().getValue() == null || ((List<Long>) field.getValue().getValue()).isEmpty()) ? null : ((List<Long>) field.getValue().getValue()).get(0);
                        value = longValue;
                        break;
                    default:
                        break;
                }
                checkRequired(field.getKey(), value, warnMap);
                fieldsValues.put(field.getKey().getAlias(), value);
                if (value != null) {
                    required = true;
                }
            } catch (NumberFormatException nfe) {
                if (field.getKey().getAlias().equalsIgnoreCase("inn")) {
                    warnMap.put(field.getKey().getName(), "значение должно состоять из цифр. Длина 12 символов.");
                } else {
                    warnMap.put(field.getKey().getName(), "значение некорректно. Неправильный формат числа");
                }
            } catch (ClassCastException cce) {
                warnMap.put(field.getKey().getName(), "значение некорректно");
            }
        }
        if (warnMap.isEmpty() && !required) {
            warnMap.put("", "Для поиска физического лица необходимо выбрать хотя бы один критерий поиска");
        }
        if (!errorMap.isEmpty())
            throw new BadValueException(errorMap);
        if (!warnMap.isEmpty()) {
            throw new WarnValueException(warnMap);
        }
        return fieldsValues;
    }

    @Override
    public Map<String, Object> getPersonFieldsValues() throws BadValueException, WarnValueException {
        final String LAST_NAME_ALIAS = "lastName";
        final String FIRST_NAME_ALIAS = "firstName";
        final String MIDDLE_NAME_ALIAS = "middleName";
        final String INN_ALIAS = "inn";
        final String INP_ALIAS = "inp";
        final String ID_DOC_NUMBER_ALIAS = "idDocNumber";
        final String REF_NUMBER_ALIAS = "pNumSpravka";

        final int LAST_NAME_LENGTH = 36;
        final int FIRST_NAME_LENGTH = 36;
        final int MIDDLE_NAME_LENGTH = 36;
        final int SNILS_LENGTH = 14;
        final int INN_LENGTH = 50;
        final int INP_LENGTH = 25;
        final int ID_DOC_NUMBER_LENGTH = 25;
        final int REF_NUMBER_LENGTH = 10;

        Map<String, Object> fieldsValues = new HashMap<String, Object>();
        Map<String, String> warnMap = new LinkedHashMap<String, String>();
        boolean required = false;
        for (Map.Entry<DeclarationSubreportParam, HasValue> field : widgets.entrySet()) {
            Object value = null;
            if (field.getKey().getType().equals(DeclarationSubreportParamType.STRING)) {
                String string = "";
                if (field.getKey().getAlias().equals(SNILS_ALIAS)) {
                    TextMaskBox textMaskBox = (TextMaskBox) field.getValue();
                    string = (textMaskBox.getText() == null || textMaskBox.getText().trim().isEmpty()) ?
                            null : textMaskBox.getText();
                } else {
                    string = (field.getValue().getValue() == null || ((String) field.getValue().getValue()).trim().isEmpty()) ?
                            null : (String) field.getValue().getValue();
                }

                value = string;
                if (field.getKey().getAlias().equals(LAST_NAME_ALIAS)) {
                    checkPersonStringFields(string, LAST_NAME_LENGTH, warnMap, field);
                } else if (field.getKey().getAlias().equals(FIRST_NAME_ALIAS)) {
                    checkPersonStringFields(string, FIRST_NAME_LENGTH, warnMap, field);
                } else if (field.getKey().getAlias().equals(MIDDLE_NAME_ALIAS)) {
                    checkPersonStringFields(string, MIDDLE_NAME_LENGTH, warnMap, field);
                } else if (field.getKey().getAlias().equals(SNILS_ALIAS)) {
                    checkSnils(string, SNILS_LENGTH, warnMap, field);
                } else if (field.getKey().getAlias().equals(INN_ALIAS)) {
                    checkPersonStringFields(string, INN_LENGTH, warnMap, field);
                } else if (field.getKey().getAlias().equals(INP_ALIAS)) {
                    checkPersonStringFields(string, INP_LENGTH, warnMap, field);
                } else if (field.getKey().getAlias().equals(ID_DOC_NUMBER_ALIAS)) {
                    checkPersonStringFields(string, ID_DOC_NUMBER_LENGTH, warnMap, field);
                } else if (field.getKey().getAlias().equals(REF_NUMBER_ALIAS)) {
                    checkPersonStringFields(string, REF_NUMBER_LENGTH, warnMap, field);
                }
            } else if (field.getKey().getType().equals(DeclarationSubreportParamType.NUMBER)) {
                value = checkNumber(field.getKey(), field.getValue(), warnMap);
            } else if (field.getKey().getType().equals(DeclarationSubreportParamType.DATE)) {
                Date date = field.getValue().getValue() == null ? null : (Date) field.getValue().getValue();
                value = date;
                if (value != null) {
                    // 01.01.1900
                    Long beginDate = -2208999600000L;
                    // 01.01.2100
                    Long endDate = 4102434000000L;
                    if (date.getTime() < beginDate || date.getTime() >= endDate) {
                        warnMap.put(field.getKey().getName(), "значение поля должно входить в интервал дат 'от 01.01.1900 до 31.12.2099'.");
                        Widget box = (Widget) field.getValue();
                        box.getElement().getFirstChildElement().getFirstChildElement()
                                .getStyle().setBackgroundColor("#ffccd2");
                    }
                }
            }
            if (value != null) {
                required = true;
            }

            fieldsValues.put(field.getKey().getAlias(), value);
        }
        if (warnMap.isEmpty() && !required) {
            warnMap.put("", "Для поиска физического лица необходимо выбрать хотя бы один критерий поиска");
        }
        if (!warnMap.isEmpty()) {
            throw new WarnValueException(warnMap);
        }
        return fieldsValues;
    }


    private void checkPersonStringFields(String string, int requiredLength, Map<String, String> errorMap, Map.Entry<DeclarationSubreportParam, HasValue> field) {
        UIObject textBox = (UIObject) field.getValue();
        if (string != null && string.length() > requiredLength) {
            final String INCORRECT_LENGTH = "Количество символов в значении не должно превышать ";
            errorMap.put(field.getKey().getName(), INCORRECT_LENGTH + requiredLength);
            textBox.getElement().getElementsByTagName("input").getItem(0).getStyle().setBackgroundColor("#ffccd2");
        } else {
            textBox.getElement().getElementsByTagName("input").getItem(0).getStyle().setBackgroundColor("");
        }
    }

    private void checkSnils(String text, int requiredLength, Map<String, String> errorMap, Map.Entry<DeclarationSubreportParam, HasValue> field) {
        UIObject textBox = (UIObject) field.getValue();
        if (text != null) {
            char[] charArray = text.toCharArray();
            boolean containsNonDigits = false;
            for (int i = 0; i < charArray.length; i++) {
                if (!Character.isDigit(charArray[i]) && i != 3 && i != 7 && i != 11) {
                    containsNonDigits = true;
                    break;
                }
            }
            if (text.length() > requiredLength || containsNonDigits) {
                final String INCORRECT = "Значение должно состоять из цифр. Количество символов в значении не должно превышать: ";
                errorMap.put(field.getKey().getName(), INCORRECT + requiredLength);
                textBox.getElement().getStyle().setBackgroundColor("#ffccd2");
            } else {
                textBox.getElement().getStyle().setBackgroundColor("");
            }
        }
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

            for (Column column : prepareSpecificReportResult.getTableColumns()) {
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

    @Override
    public void updateInfoLabel(boolean visible, String text, Map<String, String> styleMap) {
        if (text != null) {
            // Замена пробелов на non-breaking space дает возможность переноса строк с помощью \n
            infoLabel.setText(text.replaceAll(" +", "\u00a0").replaceAll("\n", " "));
        }
        if (styleMap != null) {
            for (Map.Entry<String, String> entry : styleMap.entrySet()) {
                infoPanel.getElement().getStyle().setProperty(entry.getKey(), entry.getValue());
            }
        }
        infoPanel.setVisible(visible);
    }

    @Override
    public void addEnterNativePreviewHandler() {
        nativePreviewHandler = Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    getUiHandlers().onFind();
                }
            }
        });
    }

    @Override
    public void removeEnterNativePreviewHandler() {
        nativePreviewHandler.removeHandler();
    }
}
