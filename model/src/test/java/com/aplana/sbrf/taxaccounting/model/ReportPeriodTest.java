package com.aplana.sbrf.taxaccounting.model;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

public class ReportPeriodTest {
	
	@Test
	public void getYear2012Success(){
       for (Integer i = 1970; i < 3344; i++) {
    	   Assert.assertEquals(i, createReportPeriod(i).getTaxPeriod().getYear());
       }
	}
	
	private ReportPeriod createReportPeriod(int year) {
        Calendar cl = Calendar.getInstance();
        cl.set(year, 1, 1);
        
		TaxPeriod tp = new TaxPeriod();
		tp.setYear(year);
		
		ReportPeriod rp = new ReportPeriod();
		rp.setTaxPeriod(tp);
		return rp;
	}

}
