package com.aplana.sbrf.taxaccounting.dao.script.impl.dictionary;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.DictionaryRegionDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryRegion;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("DictionaryRegionDao")
public class DictionaryRegionDaoImpl extends AbstractDictionaryDaoImpl implements DictionaryRegionDao {

    private String from = "DICT_REGION";
    @Override
    public String getQueryFrom() {
        return from;
    }

    @Override
    public List<DictionaryRegion> getListRegions() {
        List<DictionaryRegion> result = getJdbcTemplate().query(
                "SELECT * FROM " + getQueryFrom(),
                new BeanPropertyRowMapper(DictionaryRegion.class));
        return result;
    }
}
