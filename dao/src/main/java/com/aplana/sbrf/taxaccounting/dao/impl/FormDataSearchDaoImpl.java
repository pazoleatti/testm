package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.dao.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

@Repository
@Transactional(readOnly = true)
public class FormDataSearchDaoImpl extends AbstractDao implements FormDataSearchDao {

	@Autowired
	private FormTemplateDao formTemplateDao;

	@Override
	public List<FormDataSearchResultItem> findByFilter(FormDataDaoFilter dataFilter){
		/*Перед тем как выполнять SQL запрос, нам нужно убедиться что:
		 1)dataFilter.getFormtype() не пустой список
		 2)dataFilter.getPeriod() не пустой список
		 если один из списков пустой, то поиск не выполняем, а возвращаем пустой результат.
		 */
		if(dataFilter.getFormtype().isEmpty() || dataFilter.getPeriod().isEmpty()){
			return (new ArrayList<FormDataSearchResultItem>());
		}

		String query = "SELECT " +
				"  dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type, " +
				"  fd.ID as form_data_id, fd.form_id as form_template_id, fd.KIND as form_data_kind_id, fd.STATE,  " +
				"  ft.ID as form_type_id, ft.NAME as form_type_name, " +
				"  rp.ID as report_period_id, rp.NAME as report_period_name, rp.TAX_TYPE " +
				"FROM " +
				"  form_data fd, " +
				"  form_type ft, " +
				"  department dp, " +
				"  report_period rp " +
				"WHERE " +
				"  EXISTS (SELECT 1 FROM FORM f WHERE f.id = fd.form_id AND f.type_id = ft.id) " +
				"  AND dp.id = fd.department_id " +
				"  AND rp.id = fd.report_period_id" +
				"  AND ft.id in " + transformToSqlInStatement(dataFilter.getFormtype()) +
				"  AND rp.id in " + transformToSqlInStatement(dataFilter.getPeriod()) +
				"  AND fd.DEPARTMENT_ID in " + transformToSqlInStatement(dataFilter.getDepartment()) +
				"  AND fd.kind in " + transformToSqlInStatement(dataFilter.getKind()) ;

		return getJdbcTemplate().query(query, new FormDataSearchResultItemMapper());
	}

}
