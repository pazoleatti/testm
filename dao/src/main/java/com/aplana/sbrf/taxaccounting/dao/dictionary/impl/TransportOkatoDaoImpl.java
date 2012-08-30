package com.aplana.sbrf.taxaccounting.dao.dictionary.impl;

import java.sql.Types;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.dictionary.TransportOkatoDao;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;

@Repository
@Transactional(readOnly = true)
public class TransportOkatoDaoImpl extends AbstractDao implements TransportOkatoDao{
	@Override
	public String getRegionName(String okato) {
		try {
			return getJdbcTemplate().queryForObject(
				"select name from okato where okato = ?", 
				new Object[] { okato },
				new int[] { Types.VARCHAR },
				String.class
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
}
