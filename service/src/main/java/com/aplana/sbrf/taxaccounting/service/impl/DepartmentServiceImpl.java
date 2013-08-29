package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

	public static final int UNP_ID = 1;
	
    @Autowired
    DepartmentDao departmentDao;
    
    @Override
    public Department getUNPDepartment() {
        return departmentDao.getDepartment(UNP_ID);
    }

    @Override
    public Department getDepartment(int departmentId) {
        return departmentDao.getDepartment(departmentId);
    }

    @Override
    public List<Department> listAll() {
        return departmentDao.listDepartments();
    }

    @Override
    public List<Department> getChildren(int parentDepartmentId) {
        return departmentDao.getChildren(parentDepartmentId);
    }

    @Override
    public Department getParent(int departmentId) {
        return departmentDao.getParent(departmentId);
    }

    @Override
    public Map<Integer, Department> getRequiredForTreeDepartments(Set<Integer> availableDepartments) {
        // TODO использовать древовидный запрос см. getAllChildren()
    	Map<Integer, Department> departmentSet = new HashMap<Integer, Department>();
    	
    	// Если NULL то считаем что доступны все департаменты
    	if (availableDepartments == null){
    		
    		for (Department department : this.listAll()) {
    			departmentSet.put(department.getId(), department);
			};
			
    	} else { //Иначе рассчитываем необходимые для отображения
    		
	        for (Integer departmentId : availableDepartments) {
	            departmentSet.put(departmentId, getDepartment(departmentId));
	        }
	        for (Integer departmentId : availableDepartments) {
	            Integer searchFor = departmentId;
	            while (true) {
	                Department department = getParent(searchFor);
	                if (department == null) {
	                    break;
	                }
	                if (department.getParentId() == null || departmentSet.containsKey(department.getParentId())) {
	                    departmentSet.put(department.getId(), department);
	                    break;
	                } else {
	                    departmentSet.put(department.getId(), department);
	                    searchFor = department.getParentId();
	                }
	            }
	        }
	        
    	}
        return departmentSet;
    }

    @Override
    public List<Department> listDepartments() {
        return departmentDao.listDepartments();
    }

    @Override
    public Department getDepartmentBySbrfCode(String sbrfCode) {
        return departmentDao.getDepartmentBySbrfCode(sbrfCode);
    }

    @Override
    public List<Department> getAllChildren(int parentDepartmentId) {
        return departmentDao.getAllChildren(parentDepartmentId);
    }

}
