package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.identification.NaturalPersonRefbookHandler;
import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class RefBookPersonDaoImpl extends AbstractDao implements RefBookPersonDao {

    @Autowired
    RefBookDao refBookDao;

    //--------------------------- РНУ ---------------------------

    @Override
    public void fillRecordVersions(Date version) {
        //long time = System.currentTimeMillis();
        getJdbcTemplate().update("call person_pkg.FillRecordVersions(?)", version);
        //System.out.println("fillRecordVersions (" + (System.currentTimeMillis() - time) + " ms)");
    }

    @Override
    public List<NaturalPerson> findPersonForInsertFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, RowMapper<NaturalPerson> primaryRnuRowMapper) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForIns");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, primaryRnuRowMapper), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        Map<String, Object> returnedResults = call.execute(params);
        return (List<NaturalPerson>) returnedResults.get("ref_cursor");
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForUpd");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        call.execute(params);
        return naturalPersonHandler.getResult();
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForCheck");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC), new SqlParameter("p_asnu", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        params.addValue("p_asnu", asnuId);
        call.execute(params);
        return naturalPersonHandler.getResult();
    }

    /**
     * Получение данных о ФЛ из ПНФ
     *
     * @param declarationDataId
     * @return
     */
    @Override
    public List<NaturalPerson> findNaturalPersonPrimaryDataFromNdfl(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper) {

        StringBuilder SQL = new StringBuilder();
        SQL.append("SELECT id, declaration_data_id, person_id, row_num, inp, snils, last_name, first_name, middle_name, birth_day, citizenship, inn_np, inn_foreign, id_doc_type, id_doc_number, status, post_index, region_code, area, city, locality, street, house, building, flat, country_code, address, additional_data, NULL sex, NULL pension, NULL medical, NULL social, NULL correct_num, NULL period, NULL rep_period, NULL num, NULL sv_date  \n");
        SQL.append("FROM ndfl_person \n");
        SQL.append("WHERE declaration_data_id = :declarationDataId");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);

        return getNamedParameterJdbcTemplate().query(SQL.toString(), params, naturalPersonRowMapper);

    }

    //--------------------------- 1151111 ---------------------------

    @Override
    public List<NaturalPerson> findNaturalPersonPrimaryDataFrom1151111(long declarationDataId, RowMapper<NaturalPerson> naturalPersonRowMapper) {

        StringBuilder SQL = new StringBuilder();

        SQL.append("SELECT n.id, n.person_id, NULL inp, n.snils, n.familia last_name, n.imya first_name, n.otchestvo middle_name, n.data_rozd birth_day, n.grazd citizenship, n.innfl inn_np, NULL inn_foreign, n.kod_vid_doc id_doc_type, n.ser_nom_doc id_doc_number, NULL status, NULL post_index, NULL region_code, NULL area, NULL city, NULL locality, NULL street, NULL house, NULL building, NULL flat, NULL country_code, NULL address, NULL additional_data, pol sex, priz_ops pension, priz_oms medical, priz_oss social, nom_korr correct_num, period period, otchet_god rep_period, nomer num, sv_data sv_date \n");
        SQL.append("FROM RASCHSV_PERS_SV_STRAH_LIC n \n");
        SQL.append("WHERE n.declaration_data_id = :declarationDataId");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);

        return getNamedParameterJdbcTemplate().query(SQL.toString(), params, naturalPersonRowMapper);
    }

    @Override
    public void fillRecordVersions1151111(Date version) {
        getJdbcTemplate().update("call person_pkg.FillRecordVersions(?)", version);
    }

    @Override
    public List<NaturalPerson> findPersonForInsertFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, RowMapper<NaturalPerson> primaryRowMapper) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForIns115");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, primaryRowMapper), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        Map<String, Object> returnedResults = call.execute(params);
        return (List<NaturalPerson>) returnedResults.get("ref_cursor");
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForUpdateFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForUpd115");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        call.execute(params);
        return naturalPersonHandler.getResult();
    }

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findPersonForCheckFromPrimary1151111(Long declarationDataId, Long asnuId, Date version, NaturalPersonRefbookHandler naturalPersonHandler) {
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForCheck115");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC), new SqlParameter("p_asnu", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        params.addValue("p_asnu", asnuId);
        call.execute(params);
        return naturalPersonHandler.getResult();
    }

}
