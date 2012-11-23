package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.FormDataSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FormDataSearchServiceImpl implements FormDataSearchService {

	@Autowired
	private FormDataSearchDao formDataSearchDao;

	@Autowired
	private DepartmentDao departmentDao;

	@Autowired
	private FormTypeDao formTypeDao;

	@Autowired
	private ReportPeriodDao reportPeriodDao;


	@Override
	public List<FormData> findDataByUserIdAndFilter(Long userId, FormDataFilter formDataFilter) {
		/*TODO: сервис должен быть переписан после добавления параметра --ВСЕ--, так же на данный момент не используется
		параметр userId.  ==> это только прототип сервиса!
		*/
		List<FormData> formDataList = new ArrayList<FormData>();
		FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();

		if(false){
			/*В этой ветке будем обрабатывать параметр -ВСЕ-*/
		} else {
			formDataDaoFilter.setDepartment(Arrays.asList(formDataFilter.getDepartment()));
			formDataDaoFilter.setKind(Arrays.asList(formDataFilter.getKind()));
			formDataDaoFilter.setPeriod(Arrays.asList(formDataFilter.getPeriod()));
			formDataList = formDataSearchDao.findByFilter(formDataDaoFilter);
		}
		return formDataList;
	}

	@Override
	public List<Department> listDepartments() {
		return departmentDao.listDepartments();
	}

	@Override
	public List<FormType> listFormTypes() {
		return formTypeDao.listFormTypes();
	}

	@Override
	public List<ReportPeriod> listReportPeriodsByTaxType(TaxType taxType) {
		return reportPeriodDao.listAllPeriodsByTaxType(taxType);
	}
}
