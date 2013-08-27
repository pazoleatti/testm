package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.TaxPeriodDao;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.service.script.QuarterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: auldanov
 * Date: 03.06.13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
@Service("quarterService")
public class QuarterServiceImpl implements QuarterService {
    @Autowired
    ReportPeriodDao reportPeriodDao;

    @Autowired
    TaxPeriodDao taxPeriodDao;

    @Override
    public ReportPeriod getPrevReportPeriod(int reportPeriodId) {
        // текущий отчетный период
        ReportPeriod thisReportPeriod= reportPeriodDao.get(reportPeriodId);
        // список отчетных периодов в текущем налоговом периоде
        List<ReportPeriod> reportPeriodlist = reportPeriodDao.listByTaxPeriod(thisReportPeriod.getTaxPeriodId());
        for (int i = 0; i < reportPeriodlist.size(); i++){
            if (reportPeriodlist.get(i).getId() == reportPeriodId && i!=0){
                return reportPeriodlist.get(i-1);
            }
        }

        return null;
    }
}
