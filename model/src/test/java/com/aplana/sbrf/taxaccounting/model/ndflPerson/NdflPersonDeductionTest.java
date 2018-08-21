package com.aplana.sbrf.taxaccounting.model.ndflPerson;

import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonDeduction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class NdflPersonDeductionTest {

    @Test
    public void testNdflPersonDeductionComporatorByIncomeAccuredDate(){
        Calendar calendar = Calendar.getInstance();

        NdflPersonDeduction deduction1 = new NdflPersonDeduction();
        calendar.set(2010, Calendar.JANUARY, 5);
        deduction1.setId(1L);
        deduction1.setIncomeAccrued(calendar.getTime());


        NdflPersonDeduction deduction2 = new NdflPersonDeduction();
        calendar.set(2010, Calendar.JANUARY, 1);
        deduction2.setId(2L);
        deduction2.setIncomeAccrued(calendar.getTime());

        NdflPersonDeduction deduction3 = new NdflPersonDeduction();
        calendar.set(2010, Calendar.JANUARY, 3);
        deduction3.setId(3L);
        deduction3.setIncomeAccrued(calendar.getTime());

        ArrayList<NdflPersonDeduction> deductions = new ArrayList<>(Arrays.asList(deduction1, deduction2, deduction3));

        Collections.sort(deductions, NdflPersonDeduction.getComparator(null));

        assertEquals(new Long(2), deductions.get(0).getId());
        assertEquals(new Long(3), deductions.get(1).getId());
        assertEquals(new Long(1), deductions.get(2).getId());
    }

    @Test
    public void testNdflPersonDeductionComporatorByOperationId(){

        NdflPersonDeduction deduction1 = new NdflPersonDeduction();
        deduction1.setId(1L);
        deduction1.setOperationId("5");

        NdflPersonDeduction deduction2 = new NdflPersonDeduction();
        deduction2.setId(2L);
        deduction2.setOperationId("1");

        NdflPersonDeduction deduction3 = new NdflPersonDeduction();
        deduction3.setId(3L);
        deduction3.setOperationId("3");

        ArrayList<NdflPersonDeduction> deductions = new ArrayList<>(Arrays.asList(deduction1, deduction2, deduction3));
        ArrayList<String> operationIds = new ArrayList<>(Arrays.asList(deduction2.getOperationId(), deduction3.getOperationId(), deduction1.getOperationId()));

        Collections.sort(deductions, NdflPersonDeduction.getComparator(operationIds));

        assertEquals(new Long(2), deductions.get(0).getId());
        assertEquals(new Long(3), deductions.get(1).getId());
        assertEquals(new Long(1), deductions.get(2).getId());
    }

    @Test
    public void testNdflPersonDeductionComporatorByPeriodCurrentDate(){
        Calendar calendar = Calendar.getInstance();

        NdflPersonDeduction deduction1 = new NdflPersonDeduction();
        calendar.set(2010, Calendar.JANUARY, 5);
        deduction1.setId(1L);
        deduction1.setPeriodCurrDate(calendar.getTime());


        NdflPersonDeduction deduction2 = new NdflPersonDeduction();
        calendar.set(2010, Calendar.JANUARY, 1);
        deduction2.setId(2L);
        deduction2.setPeriodCurrDate(calendar.getTime());

        NdflPersonDeduction deduction3 = new NdflPersonDeduction();
        calendar.set(2010, Calendar.JANUARY, 3);
        deduction3.setId(3L);
        deduction3.setPeriodCurrDate(calendar.getTime());

        ArrayList<NdflPersonDeduction> deductions = new ArrayList<>(Arrays.asList(deduction1, deduction2, deduction3));

        Collections.sort(deductions, NdflPersonDeduction.getComparator(null));

        assertEquals(new Long(2), deductions.get(0).getId());
        assertEquals(new Long(3), deductions.get(1).getId());
        assertEquals(new Long(1), deductions.get(2).getId());
    }
}
