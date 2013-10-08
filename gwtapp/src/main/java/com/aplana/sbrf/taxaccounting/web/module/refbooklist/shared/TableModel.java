package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookType;

import java.io.Serializable;

/**
 * Модель для таблицы
 *
 * @author Stanislav Yasinskiy
 */
public class TableModel implements Serializable {

    private Long id;
    private String name;
    private RefBookType refBookType;

    public TableModel() {
    }

    public TableModel(Long id, String name, RefBookType refBookType) {
        setId(id);
        setName(name);
        setType(refBookType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RefBookType getType() {
        return refBookType;
    }

    public void setType(RefBookType refBookType) {
        this.refBookType = refBookType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
