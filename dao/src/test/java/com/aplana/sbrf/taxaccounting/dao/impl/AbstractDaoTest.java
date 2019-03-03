package com.aplana.sbrf.taxaccounting.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


public class AbstractDaoTest {

    // Абстрактному классу - абстрактную реализацию!
    private class AbstractDaoTestImpl extends AbstractDao {
    }

    // Тестируемый объект
    private AbstractDao abstractDao;

    @Mock
    private NamedParameterJdbcTemplate templateMock;

    private RowMapper rowMapperStub = new RowMapper() {
        @Override
        public Object mapRow(ResultSet resultSet, int i) {
            return null;
        }
    };

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        abstractDao = spy(new AbstractDaoTestImpl());
        when(abstractDao.getNamedParameterJdbcTemplate()).thenReturn(templateMock);
    }

    @Test
    public void test_selectIn_byEmptyList_returnsEmptyList() {
        List result = abstractDao.selectIn("", Collections.emptyList(), "", rowMapperStub);
        assertThat(result).isEmpty();
    }

    @Test
    public void test_selectIn_by1000List_executesOneQuery() {
        List<Integer> list = new ArrayList<>(1000);
        for (int i = 1; i <= 1000; i++) {
            list.add(i);
        }
        abstractDao.selectIn("", list, "", rowMapperStub);
        verify(templateMock, times(1)).query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @Test
    public void test_selectIn_by1001List_executesTwoQueries() {
        List<Integer> list = new ArrayList<>(1001);
        for (int i = 1; i <= 1001; i++) {
            list.add(i);
        }
        abstractDao.selectIn("", list, "", rowMapperStub);
        verify(templateMock, times(2)).query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
    }

    @Test
    public void test_selectIn_by2001List_executesThreeQueries() {
        List<Integer> list = new ArrayList<>(2001);
        for (int i = 1; i <= 2001; i++) {
            list.add(i);
        }
        abstractDao.selectIn("", list, "", rowMapperStub);
        verify(templateMock, times(3)).query(anyString(), any(MapSqlParameterSource.class), any(RowMapper.class));
    }
}
