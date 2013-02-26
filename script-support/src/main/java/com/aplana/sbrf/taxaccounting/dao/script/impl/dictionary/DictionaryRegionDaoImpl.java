package com.aplana.sbrf.taxaccounting.dao.script.impl.dictionary;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.DictionaryRegionDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("DictionaryRegionDao")
public class DictionaryRegionDaoImpl extends AbstractDictionaryDaoImpl implements DictionaryRegionDao {

    private String baseQuery = "SELECT * FROM dict_region";
    @Override
    public String getBaseQuery() {
        return baseQuery;
    }

    @Override
    public List<DictionaryRegion> getListRegions() {
        List<DictionaryRegion> result = getJdbcTemplate().query(
                getBaseQuery(),
                new BeanPropertyRowMapper(DictionaryRegion.class));
        return result;
    }
}
