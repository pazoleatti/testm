package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.CalendarDao;
import com.aplana.sbrf.taxaccounting.script.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service("calendarService")
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {

    @Autowired
    private CalendarDao calendarDao;

    @Override
    public Date getWorkDay(Date startDate, int offset) {
        return calendarDao.getWorkDay(startDate, offset);
    }

    @Override
    public Date getLastWorkDayByYear(int year) {
        return calendarDao.getLastWorkDayByYear(year);
    }

    @Override
    public int getDateDif(Date startDate, Date endDate) {
        return calendarDao.getDateDif(startDate, endDate);
    }

    @Override
    public int getWorkDayCount(Date startDate, Date endDate) {
        return calendarDao.getWorkDayCount(startDate, endDate);
    }
}
