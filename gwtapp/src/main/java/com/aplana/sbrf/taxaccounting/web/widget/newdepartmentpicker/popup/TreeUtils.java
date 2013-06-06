package com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.popup;

import com.aplana.sbrf.taxaccounting.model.Department;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Вспомогательный класс для работы с деревом
 * @author Eugene Stetsenko
 */
public class TreeUtils {
	private static Map<Department, List<Integer>> hierarchyCache = new HashMap<Department, List<Integer>>();

	private static List<Integer> getHierarchy(Department department, Map<Integer, Department> idToDepMap) {
		List<Integer> result = new ArrayList<Integer>();
		if (department.getParentId() != null) {
			result.addAll(getHierarchy(idToDepMap.get(department.getParentId()), idToDepMap));
			result.add(department.getId());
		} else {
			result.add(department.getId());
		}
		return result;
	}

	public static List<Integer> getCachedHierarchy(Department department, Map<Integer, Department> idToDepMap) {
		if (!hierarchyCache.containsKey(department)) {
			hierarchyCache.put(department, getHierarchy(department, idToDepMap));
		}
		return hierarchyCache.get(department);
	}

}
