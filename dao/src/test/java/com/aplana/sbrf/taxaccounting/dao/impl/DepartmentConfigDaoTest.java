package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"DepartmentConfigDaoTest.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DepartmentConfigDaoTest {

    @Autowired
    private DepartmentConfigDao departmentConfigDao;

    @Test
    public void fetchKppOktmoPairsTest() {
        List<Pair<String, String>> kppOktmoPairs = departmentConfigDao.fetchKppOktmoPairs(asList(2, 3), new LocalDate(2018, 1, 1).toDate());
        Assert.assertThat(kppOktmoPairs.size(), is(5));
    }

    @Test
    public void findAllKppByFilterTest() {
        PagingResult<KppSelect> kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKpp(2, null, PagingParams.getInstance(1, 10));
        Assert.assertThat(kppSelectList.size(), is(3));

        kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKpp(2, null, PagingParams.getInstance(2, 2));
        Assert.assertThat(kppSelectList.size(), is(1));

        kppSelectList = departmentConfigDao.findAllKppByDepartmentIdAndKpp(2, "01", PagingParams.getInstance(1, 10));
        Assert.assertThat(kppSelectList.size(), is(2));
    }
}
