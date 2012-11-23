package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormDataRowMapper;
import com.aplana.sbrf.taxaccounting.dao.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

@Repository
@Transactional(readOnly = true)
public class FormDataSearchDaoImpl extends AbstractDao implements FormDataSearchDao {

	@Autowired
	private FormTemplateDao formTemplateDao;

	@Override
	public List<FormData> findByFilter(FormDataDaoFilter dataFilter){
		String query = "select * from form_data fd where fd.department_id in " + transformToSqlInStatement(dataFilter.
				getDepartment()) + " and exists (select 1 from form f where fd.form_id = f.id and f.type_id in " +
				transformToSqlInStatement(dataFilter.getKind()) + ")";

		return getJdbcTemplate().query(query, new FormDataRowMapper(formTemplateDao));
	}

}
