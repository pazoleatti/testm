package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreport;
import com.aplana.sbrf.taxaccounting.model.DeclarationSubreportParam;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.RefBookParamInfo;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception.BadValueException;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
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

    public interface Binder extends UiBinder<PopupPanel, SubreportParamsView> {
    }

    @UiField
    VerticalPanel editPanel;

    @UiField
    ModalWindow modalWindow;

    @UiField
    Button createButton;

    @UiField
    Button cancelButton;

    private final PopupPanel widget;
    Map<DeclarationSubreportParam, HasValue> widgets;

    @Inject
    public SubreportParamsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);
        widget = uiBinder.createAndBindUi(this);
        widget.setAnimationEnabled(true);
    }

    @Override
    public Widget asWidget() {
        return widget;
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
            label.getElement().getStyle().setProperty("width", "150px");
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
                widget.getElement().getStyle().setProperty("width", "196px");
            }
            oneField.add(widget);
            oneField.setCellWidth(widget, "80%");
            oneField.setCellHorizontalAlignment(widget, HasHorizontalAlignment.ALIGN_LEFT);
            oneField.setCellVerticalAlignment(widget, HasVerticalAlignment.ALIGN_MIDDLE);

            editPanel.add(oneField);
            widgets.put(subreportParam, (HasValue) widget);
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


}
