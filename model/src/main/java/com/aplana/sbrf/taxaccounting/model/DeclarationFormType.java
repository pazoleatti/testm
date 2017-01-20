package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Вид наловой формы(declaration)
 * @author lkhaziev
 */
public class DeclarationFormType implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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