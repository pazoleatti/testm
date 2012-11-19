package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.FormType;

import java.util.List;

public interface FormTypeDao {
	FormType getType(int typeId);
    List<FormType> listFormTypes();
}
