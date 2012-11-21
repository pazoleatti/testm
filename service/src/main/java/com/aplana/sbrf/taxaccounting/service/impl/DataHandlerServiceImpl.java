package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.model.Filter;
import com.aplana.sbrf.taxaccounting.model.DataFilter;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.service.DataHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DataHandlerServiceImpl implements DataHandlerService {

	@Autowired
	private DataSearchDao dataSearchDao;

	@Override
	public List<FormData> findDataByUserIdAndFilter(Long userId, DataFilter dataFilter) {
		/*TODO: сервис должен быть переписан после добавления параметра --ВСЕ--, так же на данный момент не используется
		параметр userId.  ==> это только прототип сервиса!
		*/
		List<FormData> formDataList = new ArrayList<FormData>();
		Filter filter = new Filter();

		if(false){
			/*В этой ветке будем обрабатывать параметр -ВСЕ-*/
		} else {
			filter.setDepartment(Arrays.asList(dataFilter.getDepartment()));
			filter.setKind(Arrays.asList(dataFilter.getKind()));
			formDataList = dataSearchDao.findByFilter(filter);
		}
		return formDataList;
	}
}
