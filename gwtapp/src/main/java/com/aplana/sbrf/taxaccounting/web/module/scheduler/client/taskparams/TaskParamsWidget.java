package com.aplana.sbrf.taxaccounting.web.module.scheduler.client.taskparams;

import com.aplana.gwt.client.LongBox;
import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.*;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.CheckBox;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.TextBox;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.gwt.user.client.ui.HasVerticalAlignment.ALIGN_MIDDLE;

/**
 * Фасад для части формы отвечающий за показ
 * параметров задачи
 *
 * @author auldanov
 */
public class TaskParamsWidget extends HTMLPanel {
    /** список внутренних виджетов */
    private List<ParamWidget> widgets;
    /** название стиля для отображения имени параметра */
    private String labelStyleName;
    /** Сообщение ошибки которые возникли при валидации */
    private String errorMessage;
    /** Панель оборачивающий текушую*/
    private Panel wrapper;

    public TaskParamsWidget() {
        super("");
        widgets = new ArrayList<ParamWidget>();
    }

    /**
     * Установка параметров задачи
     *
     * @param params
     */
    public void setParams(List<FormElement> params){
        buildWidgets(params);
        appendAllWidgets();
    }

    /**
     * Валидация всех параметров
     *
     * @return
     */
    public boolean validate(){
        boolean result = true;
        errorMessage = "";
        for (ParamWidget widget : widgets) {
            result = result ? widget.isValid() : result;
            String errorMsg = widget.getErrorMsg();
            if (errorMsg != null){
                errorMessage += errorMsg;
            }
        }

        return result;
    }

    /**
     * Добавляем все виджеты параметов во вьюшку,
     * т.е. в панель, при этом учитываем форматирование
     */
    private void appendAllWidgets() {
        // очистим панель
        clear();

        // добавление всех виджетов на панель
        for (ParamWidget widget : widgets) {
            Label label = new Label(widget.getName() + ": ");
            label.setStyleName(labelStyleName);

            HorizontalPanel panel = new HorizontalPanel();
            panel.setVerticalAlignment(ALIGN_MIDDLE);
            panel.setSpacing(5);
            panel.add(label);
            panel.add(widget.getWidget());
            add(panel);
        }
    }

    /**
     * Оборачиваем элементы в виджет с нужным нам
     * интерфейсом ParamWidget
     *
     * @param params
     */
    private void buildWidgets(List<FormElement> params) {
        widgets.clear();

        if (params != null){
            for (FormElement param : params) {
                ParamWidget widget;
                if (param instanceof SelectBox){
                    List<SelectBoxItem> values = ((SelectBox) param).getValues();
                    WSelectBox wSelectBox = new WSelectBox(values);
                    wSelectBox.setWidth("475px");
                    widget = wSelectBox;
                    setParamWidgetConfig(widget, param);
                } else if (param instanceof CheckBox){
                    WCheckBox wCheckBox = new WCheckBox();
                    widget = wCheckBox;
                    setParamWidgetConfig(widget, param);
                } else if (param instanceof DateBox){
                    WDateBox wDateBox = new WDateBox();
                    wDateBox.setWidth("475px");
                    widget = wDateBox;
                    setParamWidgetConfig(widget, param);
                } else{
                    WTextBox wTextBox = new WTextBox();
                    wTextBox.setWidth("475px");
                    widget = wTextBox;
                    setParamWidgetConfig(widget, param);
                }

                widgets.add(widget);
            }
        }
    }

    /**
     * Установка таких параметров как:
     * 1. обязательность заполнения параметра
     * 2. название параметра
     * 3. тип параметра
     *
     * @param widget
     * @param param
     */
    private void setParamWidgetConfig(ParamWidget widget, FormElement param){
        widget.setRequired(param.isRequired());
        widget.setName(param.getName());
        widget.setType(param.getType());
    }

    /**
     * Получить ассоциативный масиив где в
     * качестве ключей указаны названия параметров,
     * и в качестве значений значения параметров
     *
     * @return
     */
    public List<TaskParamModel> getParamsValues(){
        List<TaskParamModel> result = new ArrayList<TaskParamModel>();
        for (ParamWidget widget : widgets) {
            TaskParamModel model = new TaskParamModel();
            model.setTaskParamName(widget.getName());
            model.setTaskParamType(widget.getType().getId());
            model.setTaskParamValue(widget.getValue());
            result.add(model);
        }

        return result;
    }

    public void setLabelStyleName(String labelStyleName) {
        this.labelStyleName = labelStyleName;
    }

    public String getLabelStyleName() {
        return labelStyleName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setWrapper(Panel wrapper) {
        this.wrapper = wrapper;
        this.wrapper.add(this);
    }

    /**
     * Установить значения в виджеты
     *
     * @param params
     */
    public void setParamsValues(List<TaskParamModel> params) {
        // преобразуем данные к виду "название параметра" -> "строковое значение параметра"
        Map<String, String> widgetValues = new HashMap<String, String>();
        for (TaskParamModel param : params) {
            widgetValues.put(param.getTaskParamName(), param.getTaskParamValue());
        }

        // установим значения в виджеты
        for (ParamWidget widget : widgets) {
            String widgetName = widget.getName();
            if (widgetValues.containsKey(widgetName)){
                String value = widgetValues.get(widgetName);
                widget.setValue(value);
            }
        }
    }

    /**
     * Установить редактируемость полей ввода
     *
     * @param enable
     */
    public void setEnable(boolean enable){
        for (ParamWidget widget : widgets) {
            widget.setEnable(enable);
        }

    }
}
