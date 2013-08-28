package com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;

import java.util.Comparator;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.FormDataKind.*;

public class DepartmentFormTypeComparator implements Comparator<DepartmentFormType>{

	private final Map<Integer, FormType> formTypes;

	public DepartmentFormTypeComparator(Map<Integer, FormType> formTypes) {
		this.formTypes = formTypes;
	}

	@Override
	public int compare(DepartmentFormType o1, DepartmentFormType o2) {
		switch (o1.getKind()) {
			case PRIMARY:
				if (!o2.getKind().equals(PRIMARY)) {
					return -1;
				}
				break;
			case CONSOLIDATED:
				if (o2.getKind().equals(PRIMARY)) {
					return 1;
				} else if (!o2.getKind().equals(CONSOLIDATED)) {
					return -1;
				}
				break;
			case SUMMARY:
				if (o2.getKind().equals(PRIMARY) || o2.getKind().equals(CONSOLIDATED)) {
					return 1;
				} else if (!o2.getKind().equals(SUMMARY)) {
					return -1;
				}
				break;
			case ADDITIONAL:
				if (o2.getKind().equals(UNP)) {
					return -1;
				} else if (!o2.getKind().equals(ADDITIONAL)) {
					return 1;
				}
				break;
			case UNP:
				if (!o2.getKind().equals(UNP)) {
					return 1;
				}
				break;
		}

		return formTypes.get(o1.getFormTypeId()).getName().compareTo(formTypes.get(o2.getFormTypeId()).getName());
	}
}
