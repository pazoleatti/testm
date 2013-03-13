package com.aplana.sbrf.taxaccounting.dao.script.impl.dictionary;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.script.dictionary.DictionaryTaxBenefitParamDao;
import com.aplana.sbrf.taxaccounting.model.DictionaryTaxBenefitParam;

@Repository
public class DictionaryTaxBenefitParamDaoImpl extends AbstractDictionaryDaoImpl implements DictionaryTaxBenefitParamDao{

	private String baseQuery = "SELECT * FROM DICT_TAX_BENEFIT_PARAM";
	
    @Override
    public String getBaseQuery() {
        return baseQuery;
    }

    @Override
    public List<DictionaryTaxBenefitParam> getListParams() {
        List<DictionaryTaxBenefitParam> result = getJdbcTemplate().query(getBaseQuery(), new RowMapper<DictionaryTaxBenefitParam>(){

			@Override
			public DictionaryTaxBenefitParam mapRow(ResultSet rs, int rowNum) throws SQLException {
				DictionaryTaxBenefitParam param = new DictionaryTaxBenefitParam();
				param.setDictRegionId(rs.getString("DICT_REGION_ID"));
				param.setItem(rs.getString("ITEM"));
				param.setPercent(rs.getDouble("PERCENT"));
				param.setRate(rs.getDouble("RATE"));
				param.setSection(rs.getString("SECTION"));
				param.setSubitem(rs.getString("SUBITEM"));
				param.setTaxBenefitId(rs.getString("TAX_BENEFIT_ID"));
				return param;
			}
        	
        });
                
        return result;
    }

}
