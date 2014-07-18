package com.aplana.sbrf.taxaccounting.web.module.scheduler.client.taskparams;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;
import com.google.gwt.user.client.ui.Widget;

import java.util.Map;

/**
 * Обертка для всех типов виджетов (поля для ввода параметров задачи)
 *
 * Так как навязыть модулю scheduler зависимости с gwt не хорошо,
 * для этого создали обертки под все типы в текущем модуле.
 * Базовый класс для виджетов - пользовательских параметров,
 * содержит функции для получения вилжета для отображения (в зависимости от типа)
 * и валидации
 *
 * @author auldanov
 */
abstract class ParamWidget{
    /**
     * Является ли поле обязательным для заполнения
     */
    private boolean required;

    /**
     * Имя параметра
     */
    private String name;

    /**
     * Тип параметра, используется при валидации данных
     */
    private TaskParamType type;

    /**
     * Метод получения виджета для отображения
     * на форме
     *
     * @return
     */
    abstract Widget getWidget();

    /**
     * Метод валидации
     *
     * @return
     */
    abstract boolean isValid();

    /**
     * Информация об ошибках валидации
     */
    abstract public String getErrorMsg();

    /**
     * Получить значение параметра в качестве строки
    */
    public abstract String getValue();

    /**
     * Установить значение параметра
     */
    public abstract void setValue(String value);

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public TaskParamType getType() {
        return type;
    }

    public void setType(TaskParamType type) {
        this.type = type;
    }

    /**
     * Установить редактируемость поля воода
     *
     * @param enable
     */
    public abstract void setEnable(boolean enable);
}
