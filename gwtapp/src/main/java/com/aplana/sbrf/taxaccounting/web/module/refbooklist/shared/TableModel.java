package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import java.io.Serializable;

/**
 * Модель для таблицы
 *
 * @author Stanislav Yasinskiy
 */
public class TableModel implements Serializable {

    private Long id;
    private String name;
    private Type type;

    public TableModel() {
    }

    public TableModel(Long id, String name, Type type) {
        setId(id);
        setName(name);
        setType(type);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
