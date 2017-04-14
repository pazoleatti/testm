package com.aplana.sbrf.taxaccounting.web.module.scheduler.client.taskparams;

import com.aplana.sbrf.taxaccounting.model.TaskParamModel;
import com.aplana.sbrf.taxaccounting.model.scheduler.SchedulerTaskParam;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.*;
import com.aplana.sbrf.taxaccounting.scheduler.api.form.CheckBox;
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
    @SuppressWarnings("all")
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
    private void setParams(List<SchedulerTaskParam> params){
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
    private void buildWidgets(List<SchedulerTaskParam> params) {
        widgets.clear();

        if (params != null){
            for (SchedulerTaskParam param : params) {
                ParamWidget widget;
                WTextBox wTextBox = new WTextBox();
                wTextBox.setWidth("475px");
                widget = wTextBox;
                setParamWidgetConfig(widget, param);
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
    private void setParamWidgetConfig(ParamWidget widget, SchedulerTaskParam param){
        widget.setId(param.getId());
        widget.setRequired(true);
        widget.setName(param.getParamName());
        widget.setType(param.getParamType());
        //widget.setType(param.getType());
    }

    /**
     * Получить ассоциативный масиив где в
     * качестве ключей указаны названия параметров,
     * и в качестве значений значения параметров
     *
     * @return
     */
    public List<SchedulerTaskParam> getParamsValues(){
        List<SchedulerTaskParam> result = new ArrayList<SchedulerTaskParam>();
        for (ParamWidget widget : widgets) {
            SchedulerTaskParam model = new SchedulerTaskParam();
            model.setId(widget.getId());
            model.setValue(widget.getValue());
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
    public void setParamsValues(List<SchedulerTaskParam> params) {
        setParams(params);
        // преобразуем данные к виду "название параметра" -> "строковое значение параметра"
        Map<Long, String> widgetValues = new HashMap<Long, String>();
        for (SchedulerTaskParam param : params) {
            widgetValues.put(param.getId(), param.getValue());
        }

        // установим значения в виджеты
        for (ParamWidget widget : widgets) {
            if (widgetValues.containsKey(widget.getId())){
                String value = widgetValues.get(widget.getId());
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
