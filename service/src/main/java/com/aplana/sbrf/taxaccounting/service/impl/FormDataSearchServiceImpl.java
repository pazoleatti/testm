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
	public List<FormData> findDataByUserIdAndFilter(int userId, FormDataFilter formDataFilter) {
		/*TODO: на данный момент параметр ВСЕ обрабатывается, но пока-что не учитывается userId
		* Еще один момент: пока что параметр ВСЕ передается не как null, а как Long.MAX_VALUE,
		* будет исправлено, когда решится проблема с LongListBoxEditor*/

		List<FormData> formDataList = new ArrayList<FormData>();
		FormDataDaoFilter formDataDaoFilter = new FormDataDaoFilter();

		if(formDataFilter.getDepartment() == Long.MAX_VALUE){
			List<Department> departmentList = departmentDao.listDepartments();
			List<Long> departmentLongList = new ArrayList<Long>();
			for(Department department : departmentList){
				departmentLongList.add((long)department.getId());
			}
			formDataDaoFilter.setDepartment(departmentLongList);
		} else {
			formDataDaoFilter.setDepartment(Arrays.asList(formDataFilter.getDepartment()));
		}

		if(formDataFilter.getPeriod() == Long.MAX_VALUE){
			List<ReportPeriod> reportPeriodList = reportPeriodDao.listAllPeriodsByTaxType(formDataFilter
					.getTaxType());
			List<Long> reportPeriodLongList = new ArrayList<Long>();
			/*TODO: По поводу обработки ситуации, когда у нас нету Отчетного периода или Типа формы с определенным TaxType,
			* нужно поговорить. Может все-таки имеет смысл обрабатывать это подобным образом на сервисном слое, чем многократно
			* усложнять логику DAO слоя дополнительными SQL скриптами?!?*/
			System.out.println("Check on empty reportPeriodList. isEmpty() = " + reportPeriodList.isEmpty());
 			if(!reportPeriodList.isEmpty()){
				for (ReportPeriod reportPeriod : reportPeriodList){
					reportPeriodLongList.add((long)reportPeriod.getId());
				}
			} else {
				reportPeriodLongList.add(-1L);
			}
			formDataDaoFilter.setPeriod(reportPeriodLongList);
		} else {
			formDataDaoFilter.setPeriod(Arrays.asList(formDataFilter.getPeriod()));
		}

		if(formDataFilter.getKind() == Long.MAX_VALUE){
			FormDataKind[] formDataKinds = FormDataKind.values();
			List<Long> formDataKindLongList = new ArrayList<Long>();
			for(int i = 0; i < formDataKinds.length; i++){
				formDataKindLongList.add((long)formDataKinds[i].getId());
			}
			formDataDaoFilter.setKind(formDataKindLongList);
		} else {
			formDataDaoFilter.setKind(Arrays.asList(formDataFilter.getKind()));
		}

		if(formDataFilter.getFormtype() == Long.MAX_VALUE){
			List<FormType> formTypeList = formTypeDao.listAllByTaxType(formDataFilter.getTaxType());
			List<Long> formTypeLongList = new ArrayList<Long>();
			//TODO: см коммент выше.
			System.out.println("Check on empty formTypeList. isEmpty() = " + formTypeList.isEmpty());
			if(!formTypeList.isEmpty()){
				for(FormType formType : formTypeList){
					formTypeLongList.add((long)formType.getId());
				}
			} else {
				formTypeLongList.add(-1L);
			}
			formDataDaoFilter.setFormtype(formTypeLongList);
		} else {
			formDataDaoFilter.setFormtype(Arrays.asList(formDataFilter.getFormtype()));
		}

		formDataList = formDataSearchDao.findByFilter(formDataDaoFilter);

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
	public List<FormType> listFormTypesByTaxType(TaxType taxType){
		return formTypeDao.listAllByTaxType(taxType);
	}

	@Override
	public List<ReportPeriod> listReportPeriodsByTaxType(TaxType taxType) {
		return reportPeriodDao.listAllPeriodsByTaxType(taxType);
	}

}
