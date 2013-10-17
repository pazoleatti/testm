package com.aplana.sbrf.taxaccounting.scheduler.api.entity;

import com.aplana.sbrf.taxaccounting.scheduler.api.exception.InvalidTaskParamException;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Пользователький параметр, передаваемый в задачу планировщика
 * @author dloshkarev
 */
public class TaskParam implements Serializable, Comparable {
    private static final long serialVersionUID = 8101116436573622004L;
    public static final String INVALID_PROP_ERROR_MESSAGE_TEPLATE = "Incorrect input parameter %s = %s";
    public static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

    private Integer id;
    private String name;
    private TaskParamType type;
    private String value;

    public TaskParam() {
    }

    public TaskParam(Integer id, String name, TaskParamType type, String value) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskParamType getType() {
        return type;
    }

    public void setType(TaskParamType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    /**
     * возращает типизованное значение свойства
     *
     * @return the typifiedValue
     * @throws com.aplana.sbrf.taxaccounting.scheduler.api.exception.InvalidTaskParamException
     */
    public Object getTypifiedValue() throws InvalidTaskParamException {
        Object result = null;
        try {
            switch (type) {
                case INT:
                    result = Integer.valueOf(getValue());
                    break;
                case LONG:
                    result = Long.valueOf(getValue());
                    break;
                case FLOAT:
                    result = Float.valueOf(getValue());
                    break;
                case DOUBLE:
                    result = Double.valueOf(getValue());
                    break;
                case BOOLEAN:
                    result = Boolean.valueOf(getValue());
                    break;
                case DATE:
                    result = df.parse(getValue());
                    break;
                case STRING:
                default:
                    result = getValue();
                    break;
            }
        } catch (Exception e) {
            throw new InvalidTaskParamException(String.format(INVALID_PROP_ERROR_MESSAGE_TEPLATE, getName(),getValue()),e);
        }
        return result;
    }

    @Override
    public String toString() {
        return "TaskParam{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        return this.id.compareTo(((TaskParam) o).getId());
    }
}
