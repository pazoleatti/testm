package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import java.util.HashSet;
import java.util.Set;

/**
 * DataRow type
 * 
 * @author sgoryachkin
 */
enum TypeFlag {
	// Строка добавлена
	DEL(-1),
	// Строка удалена
	ADD(+1),
	// Строка не изменялась
	SAME(0);

	private int key;

	private TypeFlag(int key) {
		this.key = key;
	}

	public int getKey() {
		return key;
	}

	public static Set<Integer> rtsToKeys(TypeFlag[] types) {
		Set<Integer> result = new HashSet<Integer>();
		for (TypeFlag rt : types) {
			result.add(rt.getKey());
		}
		return result;
	}
}
