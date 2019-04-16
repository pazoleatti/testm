package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Подразделения
 */
@Getter
@Setter
public class RefBookDepartment extends RefBookSimple<Integer> {
    //Наименование
    private String name;
    //Сокращенное наименование
    private String shortName;
    //Родительское подразделение
    private RefBookDepartment parent;
    //Дочерние подразделения
    private List<RefBookDepartment> children;
    //Тип подразделения (1 - Банк, 2- ТБ, 3- ЦСКО, ПЦП, 4- Управление, 5- Не передается в СУДИР)
    private DepartmentType type;
    //Индекс территориального банка
    private String tbIndex;
    //Код подразделения в нотации Сбербанка
    private String sbrfCode;
    //Код региона
    private Long regionId;
    //Действующее подразделение
    private boolean isActive;
    //Код подразделения
    private Long code;
    //Полное имя подразделения
    private String fullName;

    public void setParentId(Integer parentId) {
        parent = new RefBookDepartment();
        parent.setId(parentId);
    }

    public Integer getParentId() {
        return parent != null ? parent.id : null;
    }

    public void addChild(RefBookDepartment department) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(department);
    }

    public RefBookDepartment id(Integer id) {
        this.id = id;
        return this;
    }

    public RefBookDepartment name(String name) {
        this.name = name;
        return this;
    }
}
