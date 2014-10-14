package com.aplana.sbrf.taxaccounting.web.module.periods.client;

import com.aplana.sbrf.taxaccounting.web.module.periods.shared.TableRow;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Fail Mukhametdinov
 */
public class PeriodsPresenterTest {

    PeriodsPresenter presenter = mock(PeriodsPresenter.class);

    @Before
    public void init() {
        when(presenter.canEditDeadLine(any(TableRow.class))).thenCallRealMethod();
    }

    @Test
    public void testCanEditDeadLine() throws NoSuchMethodException {
        TableRow tableRow = new TableRow();
        tableRow.setSubHeader(false);
        tableRow.setPeriodCondition(true);

        assertTrue(presenter.canEditDeadLine(tableRow));
    }

    @Test
    public void testCanEditDeadLineWithSubHeader() throws NoSuchMethodException {
        TableRow tableRow = new TableRow();
        tableRow.setSubHeader(true);
        tableRow.setPeriodCondition(true);

        assertFalse(presenter.canEditDeadLine(tableRow));
    }

    @Test
    public void testCanEditDeadLineNotOpened() throws NoSuchMethodException {
        TableRow tableRow = new TableRow();
        tableRow.setSubHeader(false);
        tableRow.setPeriodCondition(false);

        assertFalse(presenter.canEditDeadLine(tableRow));
    }

    @Test
    public void testCanEditDeadLineNotCorrection() throws NoSuchMethodException {
        TableRow tableRow = new TableRow();
        tableRow.setSubHeader(false);
        tableRow.setPeriodCondition(true);
        tableRow.setCorrectPeriod(new Date());

        assertFalse(presenter.canEditDeadLine(tableRow));
    }
}