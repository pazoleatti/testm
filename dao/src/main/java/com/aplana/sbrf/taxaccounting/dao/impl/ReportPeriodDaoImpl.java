package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.exсeption.DaoException;
import com.aplana.sbrf.taxaccounting.dao.mapper.ReportPeriodMapper;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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
			throw new DaoException("Не удалось найти активного отчетного периода с id = " + periodId);
		}
		return result;
	}

	@Override
	public ReportPeriod getCurrentPeriod(TaxType taxType) {
		try{
			ReportPeriod reportPeriod = reportPeriodMapper.getCurrentPeriod(taxType.getCode());
			return reportPeriod;
		} catch (MyBatisSystemException e) {
			/* Nested exception is TooManyResultsException */
			throw new DaoException("Существует несколько открытых периодов по виду налога " + taxType);
		}
	}

	@Override
	public List<ReportPeriod> listAllPeriodsByTaxType(TaxType taxType) {
		return reportPeriodMapper.listAllPeriodsByTaxType(taxType.getCode());
	}
}
