package com.aplana.sbrf.taxaccounting.web.widget.departmentpicker;

import com.aplana.sbrf.taxaccounting.model.Department;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Вспомогательный класс для работы с деревом
 * @author Eugene Stetsenko
 */
public class TreeUtils {

	private static List<Integer> getHierarchy(Department department, Map<Integer, Department> idToDepMap) {
		List<Integer> result = new ArrayList<Integer>();
		if (department == null){
			return result;
		}
		if (department !=null && department.getParentId() != null) {
			result.addAll(getHierarchy(idToDepMap.get(department.getParentId()), idToDepMap));
			result.add(department.getId());
		} else {
			result.add(department.getId());
		}
		return result;
	}

	public static List<Integer> getCachedHierarchy(Department department,
	                                               Map<Integer, Department> idToDepMap,
	                                               Map<Department, List<Integer>> cacheStorage) {
		if (!cacheStorage.containsKey(department)) {
			cacheStorage.put(department, getHierarchy(department, idToDepMap));
		}
		return cacheStorage.get(department);
	}

}
