package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.mapper.ReportPeriodMapper;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация DAO для работы с {@link ReportPeriod отчётными периодами}
 * @author srybakov
*/
@Repository
@Transactional(readOnly = true)
public class ReportPeriodDaoImpl implements ReportPeriodDao {

	@Autowired
	ReportPeriodMapper reportPeriodMapper;

	@Override
	public ReportPeriod get(int periodId) {
		ReportPeriod result = reportPeriodMapper.get(periodId);
		if (result == null) {
			throw new DaoException("Не удалось найти отчетного периода с id = " + periodId);
		}
		return result;
	}

	@Override
	public ReportPeriod getCurrentPeriod(TaxType taxType) {
		try{
			return reportPeriodMapper.getCurrentPeriod(taxType.getCode());
		} catch (MyBatisSystemException e) {
			/* Nested exception is TooManyResultsException */
			if(e.getCause() instanceof TooManyResultsException){
				throw new DaoException("Существует несколько открытых периодов по виду налога " + taxType);
			} else {
				throw e;
			}
		}
	}

	@Override
	public List<ReportPeriod> listAllPeriodsByTaxType(TaxType taxType) {
		return reportPeriodMapper.listAllPeriodsByTaxType(taxType.getCode());
	}

	@Override
	public List<ReportPeriod> listByTaxPeriod(int taxPeriodId) {
		return reportPeriodMapper.listByTaxPeriod(taxPeriodId);
	}
}
