package com.aplana.sbrf.taxaccounting.web.module.sources.server.assembler;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;

import java.util.Comparator;
import java.util.Map;

public class DepartmentDeclarationTypeComparator implements Comparator<DepartmentDeclarationType>{

	private final Map<Integer, DeclarationType> declarationTypes;

	public DepartmentDeclarationTypeComparator(Map<Integer, DeclarationType> declarationTypes) {
		this.declarationTypes = declarationTypes;
	}

	@Override
	public int compare(DepartmentDeclarationType o1, DepartmentDeclarationType o2) {
		return declarationTypes.get(o1.getDeclarationTypeId()).getName()
				.compareTo(declarationTypes.get(o2.getDeclarationTypeId()).getName());
	}
}
