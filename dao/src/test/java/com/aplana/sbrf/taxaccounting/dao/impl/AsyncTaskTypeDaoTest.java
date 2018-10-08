package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AsyncTaskTypeDao;
import com.aplana.sbrf.taxaccounting.model.AsyncTaskTypeData;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"AsyncTaskTypeDaoTest.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
public class AsyncTaskTypeDaoTest {

    @Autowired
    private AsyncTaskTypeDao asyncTaskTypeDao;

    @Test
    public void findByIdTest() {
        assertThat(asyncTaskTypeDao.findById(2), is(equalTo(
                AsyncTaskTypeData.builder().id(2).name("task2").handlerBean("handler2").shortQueueLimit(200L).taskLimit(2000L).limitKind("kind2").build()))
        );
    }

    @Test
    public void findAllTest() {
        PagingResult<AsyncTaskTypeData> findAllResult = asyncTaskTypeDao.findAll(PagingParams.getInstance(1, 2));
        assertThat(findAllResult, equalTo(
                asList(
                        AsyncTaskTypeData.builder().id(1).name("task1").handlerBean("handler1").shortQueueLimit(100L).taskLimit(1000L).limitKind("kind1").build(),
                        AsyncTaskTypeData.builder().id(2).name("task2").handlerBean("handler2").shortQueueLimit(200L).taskLimit(2000L).limitKind("kind2").build()
                ))
        );
        assertThat(findAllResult.getTotalCount(), is(3));
    }

    @Test
    public void updateLimitsTest() {
        AsyncTaskTypeData beforeUpdate = asyncTaskTypeDao.findById(1);
        assertThat(asList(beforeUpdate.getShortQueueLimit(), beforeUpdate.getTaskLimit()), equalTo(asList(100L, 1000L)));

        asyncTaskTypeDao.updateLimits(1L, 111L, 1111L);

        AsyncTaskTypeData afterUpdate = asyncTaskTypeDao.findById(1);
        assertThat(asList(afterUpdate.getShortQueueLimit(), afterUpdate.getTaskLimit()), equalTo(asList(111L, 1111L)));
    }
}
