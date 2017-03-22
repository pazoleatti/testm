package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Вид наловой формы(declaration)
 * @author lkhaziev
 */
public class DeclarationFormType implements Serializable {
	private static final long serialVersionUID = 1L;

    public static final int DECLARATION_TYPE_1151111 = 1;
    public static final int DECLARATION_TYPE_2NDFL_1 = 3;
    public static final int DECLARATION_TYPE_2NDFL_2 = 4;
    public static final int DECLARATION_TYPE_6NDFL = 5;

	private long id;
	private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DeclarationFormType{" +
                "name='" + name + '\'' +
                '}';
    }
}
