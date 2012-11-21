package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.service.DataHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DataHandlerServiceImpl implements DataHandlerService {

	@Autowired
	private FormDataSearchDao formDataSearchDao;

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
			formDataList = formDataSearchDao.findByFilter(formDataDaoFilter);
		}
		return formDataList;
	}
}
