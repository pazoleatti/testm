package com.aplana.sbrf.taxaccounting.model.util;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;

import java.util.Comparator;

/**
 * Компаратор для сортировки списка видов деклараций по алфавиту
 * @author Eugene Stetsenko
 */
public class DeclarationTypeAlphanumericComparator implements Comparator<DeclarationType> {
	@Override
	public int compare(DeclarationType dt1, DeclarationType dt2) {
		if (dt1 == null && dt2 == null) {
			return 0;
		} else if (dt1 == null) {
			return -1;
		} else if (dt2 == null) {
			return 1;
		} else if (dt1.getName() == null && dt2.getName() == null) {
			return 0;
		} else if (dt1.getName() == null) {
			return -1;
		} else {
			return dt1.getName().compareTo(dt2.getName());
		}
	}

}
