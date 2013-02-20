package com.aplana.sbrf.taxaccounting.model.util;

import java.util.Comparator;

import com.aplana.sbrf.taxaccounting.model.FormType;

/**
 * Компаратор для сортировки списка видов налоговых форм по алфавиту
 * @author dsultanbekov
 */
public class FormTypeAlphanumericComparator implements Comparator<FormType> {
	@Override
	public int compare(FormType ft1, FormType ft2) {
		if (ft1 == null && ft2 == null) {
			return 0;
		} else if (ft1 == null) {
			return -1;
		} else if (ft2 == null) {
			return 1;
		} else if (ft1.getName() == null && ft2.getName() == null) {
			return 0;
		} else if (ft1.getName() == null) {
			return -1;
		} else {
			return ft1.getName().compareTo(ft2.getName());
		}
	}

}
