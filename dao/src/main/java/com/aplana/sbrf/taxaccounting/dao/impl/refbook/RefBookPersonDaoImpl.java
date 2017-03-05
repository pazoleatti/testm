package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.impl.AbstractDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.PersonData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.identity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

/**
 * @author Andrey Drunk
 */
@Repository
@Transactional
public class RefBookPersonDaoImpl extends AbstractDao implements RefBookPersonDao {

    /**
     * из oracle.jdbc.OracleTypes
     */
    public static final int CURSOR = -10;

    @Autowired
    RefBookDao refBookDao;

    @Override
    public Map<Long, Map<Long, NaturalPerson>> findRefBookPersonByPrimaryRnuNdflFunction(Long declarationDataId, Long asnuId, Date version) {

        Map<Long, Map<Long, NaturalPerson>> result = new HashMap<Long, Map<Long, NaturalPerson>>();

        long time = System.currentTimeMillis();

        fillRecordVersions(version);

        //Добавляем идентификаторы записей для создания
        addPersonForInsert(declarationDataId, result);

        //Добавляем записи найденные по определяющим параметрам
        addPersonForUpdate(declarationDataId, result);

        //Добавляем записи найденные по всем параметрам
        addPersonForCheck(declarationDataId, asnuId, result);

        System.out.println("findRefBookPersonByPrimaryRnuNdflFunction "+result.size()+" rows fetched (" + (System.currentTimeMillis() - time) + " ms)");

        return result;


    }

    private void fillRecordVersions(Date version) {
        long time = System.currentTimeMillis();
        getJdbcTemplate().update("call person_pkg.FillRecordVersions(?)", version);
        System.out.println("fillRecordVersions (" + (System.currentTimeMillis() - time) + " ms)");
    }


    /**
     * @param declarationDataId
     * @param result
     */
    private void addPersonForInsert(Long declarationDataId, final Map<Long, Map<Long, NaturalPerson>> result) {

        long time = System.currentTimeMillis();

        NaturalPersonInsertHandler naturalPersonHandler = new NaturalPersonInsertHandler(result);
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForIns");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        call.execute(params);

        System.out.println("addPersonForInsert "+naturalPersonHandler.getRowCount()+" rows fetched (" + (System.currentTimeMillis() - time) + " ms)");
    }

    /**
     * @param declarationDataId
     */
    private void addPersonForUpdate(Long declarationDataId, Map<Long, Map<Long, NaturalPerson>> resultMap) {

        long time = System.currentTimeMillis();

        NaturalPersonUpdateHandler naturalPersonHandler = new NaturalPersonUpdateHandler(resultMap);
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForUpd");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        call.execute(params);

        System.out.println("addPersonForUpdate "+naturalPersonHandler.getRowCount()+" rows fetched (" + (System.currentTimeMillis() - time) + " ms)");


    }

    private void addPersonForCheck(Long declarationDataId, Long asnuId, Map<Long, Map<Long, NaturalPerson>> resultMap) {

        long time = System.currentTimeMillis();

        NaturalPersonUpdateHandler naturalPersonHandler = new NaturalPersonUpdateHandler(resultMap);
        SimpleJdbcCall call = new SimpleJdbcCall(getJdbcTemplate()).withCatalogName("person_pkg").withFunctionName("GetPersonForCheck");
        call.declareParameters(new SqlOutParameter("ref_cursor", CURSOR, naturalPersonHandler), new SqlParameter("p_declaration", Types.NUMERIC), new SqlParameter("p_asnu", Types.NUMERIC));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("p_declaration", declarationDataId);
        params.addValue("p_asnu", asnuId);
        call.execute(params);

        System.out.println("addPersonForCheck "+naturalPersonHandler.getRowCount()+" rows fetched (" + (System.currentTimeMillis() - time) + " ms)");
    }

    private class NaturalPersonInsertHandler extends NaturalPersonHandler {

        public NaturalPersonInsertHandler(Map<Long, Map<Long, NaturalPerson>> result) {
            super(result);
        }

        @Override
        public void processRow(ResultSet rs, int rowNum, Map<Long, Map<Long, NaturalPerson>> map) throws SQLException {
            Long primaryPersonId = SqlUtils.getLong(rs, PERSON_ID);
            map.put(SqlUtils.getLong(rs, PERSON_ID), null);
            //System.out.println(rowNum+", "+primaryPersonId);
        }
    }


    private class NaturalPersonUpdateHandler extends NaturalPersonHandler {

        public NaturalPersonUpdateHandler(Map<Long, Map<Long, NaturalPerson>> result) {
            super(result);
        }

        @Override
        public void processRow(ResultSet rs, int rowNum, Map<Long, Map<Long, NaturalPerson>> map) throws SQLException {

            Long primaryPersonId = SqlUtils.getLong(rs, PERSON_ID);

            //if (primaryPersonId == null) {throw new ServiceException("Не задано значение PERSON_ID");}

            //Список сходных записей
            Map<Long, NaturalPerson> similarityPersonMap = map.get(primaryPersonId);

            if (similarityPersonMap == null) {
                similarityPersonMap = new HashMap<Long, NaturalPerson>();
                map.put(primaryPersonId, similarityPersonMap);
            }

            Long refBookPersonId = SqlUtils.getLong(rs, "ref_book_person_id");

            NaturalPerson naturalPerson = similarityPersonMap.get(refBookPersonId);

            if (naturalPerson == null) {
                naturalPerson = buildNaturalPerson(rs, refBookPersonId);
                similarityPersonMap.put(refBookPersonId, naturalPerson);
            }


            //Добавляем документы физлица
            addPersonDocument(rs, naturalPerson);

            //Добавляем идентификаторы
            addPersonIdentifier(rs, naturalPerson);

            //Адрес
            Address address = buildAddress(rs);
            naturalPerson.setAddress(address);


            System.out.println(rowNum + ", primaryPersonId=" + primaryPersonId + ", [" + naturalPerson + "][" + Arrays.toString(naturalPerson.getPersonDocumentMap().values().toArray()) + "][" + Arrays.toString(naturalPerson.getPersonIdentityMap().values().toArray()) + "][" + address + "]");


        }


        private void addPersonIdentifier(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {
            Long personIdentifierId = SqlUtils.getLong(rs, "book_id_tax_payer_id");
            Map<Long, PersonIdentifier> personIdentityMap = naturalPerson.getPersonIdentityMap();
            if (personIdentifierId != null && !personIdentityMap.containsKey(personIdentifierId)) {
                PersonIdentifier personIdentifier = new PersonIdentifier();
                personIdentifier.setId(personIdentifierId);
                personIdentifier.setInp(rs.getString("inp"));
                personIdentifier.setAsnuId(SqlUtils.getLong(rs, "as_nu"));
                personIdentifier.setNaturalPerson(naturalPerson);
                personIdentityMap.put(personIdentifierId, personIdentifier);
            }
        }

        private void addPersonDocument(ResultSet rs, NaturalPerson naturalPerson) throws SQLException {

            Long docId = SqlUtils.getLong(rs, "ref_book_id_doc_id");
            Map<Long, PersonDocument> pesonDocumentMap = naturalPerson.getPersonDocumentMap();

            if (docId != null && !pesonDocumentMap.containsKey(docId)) {
                DocType docType = buildDocumentType(rs);
                PersonDocument personDocument = new PersonDocument();
                personDocument.setId(docId);
                personDocument.setDocType(docType);
                personDocument.setDocumentNumber(rs.getString("doc_number"));
                personDocument.setIncRep(SqlUtils.getInteger(rs, "inc_rep"));
                personDocument.setNaturalPerson(naturalPerson);
                pesonDocumentMap.put(docId, personDocument);
            }

        }

        private DocType buildDocumentType(ResultSet rs) throws SQLException {

            Long docTypeId = SqlUtils.getLong(rs, "doc_id");

            if (docTypeId != null) {
                DocType docType = new DocType();
                docType.setId(docTypeId);
                //получаем из кэша
                return docType;
            } else {
                return null;
            }
        }


        private NaturalPerson buildNaturalPerson(ResultSet rs, Long personId) throws SQLException {
            NaturalPerson person = new NaturalPerson();

            //person
            person.setRefBookPersonId(personId);
            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setSex(SqlUtils.getInteger(rs, "sex"));
            person.setInn(rs.getString("inn"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setSnils(rs.getString("snils"));
            person.setBirthDate(rs.getDate("birth_date"));

            //ссылки на справочники
            person.setTaxPayerStatusId(SqlUtils.getLong(rs, "taxpayer_state"));
            person.setCitizenshipId(SqlUtils.getLong(rs, "citizenship"));

            //additional
            person.setPension(SqlUtils.getInteger(rs, "pension"));
            person.setMedical(SqlUtils.getInteger(rs, "medical"));
            person.setSocial(SqlUtils.getInteger(rs, "social"));
            person.setEmployee(SqlUtils.getInteger(rs, "employee"));
            person.setSourceId(SqlUtils.getLong(rs, "source_id"));
            person.setRecordId(SqlUtils.getLong(rs, "record_id"));

            return person;
        }

        private Address buildAddress(ResultSet rs) throws SQLException {
            Long addrId = SqlUtils.getLong(rs, "REF_BOOK_ADDRESS_ID");
            if (addrId != null) {
                Address address = new Address();
                address.setAddressType(SqlUtils.getInteger(rs, "address_type"));
                address.setCountryId(SqlUtils.getLong(rs, "country_id"));
                address.setRegionCode(rs.getString("region_code"));
                address.setPostalCode(rs.getString("postal_code"));
                address.setDistrict(rs.getString("district"));
                address.setCity(rs.getString("city"));
                address.setLocality(rs.getString("locality"));
                address.setStreet(rs.getString("street"));
                address.setHouse(rs.getString("house"));
                address.setBuild(rs.getString("build"));
                address.setAppartment(rs.getString("appartment"));
                address.setAddressIno(rs.getString("address"));
                return address;
            } else {
                return null;
            }
        }


    }

    private abstract class NaturalPersonHandler implements RowCallbackHandler {

        public static final String PERSON_ID = "PERSON_ID";

        private int rowCount;

        private Map<Long, Map<Long, NaturalPerson>> result;

        public NaturalPersonHandler(Map<Long, Map<Long, NaturalPerson>> result) {
            this.result = result;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            processRow(rs, this.rowCount++, this.result);
        }

        public abstract void processRow(ResultSet rs, int rowNum, Map<Long, Map<Long, NaturalPerson>> map) throws SQLException;

        public int getRowCount() {
            return rowCount;
        }

        public Map<Long, Map<Long, NaturalPerson>> getResult() {
            return result;
        }
    }


    private static final String FIND_REFBOOK_PERSON_BY_PRIMARY_RNU_NDFL_QUERY = buildFindRefBookPersonByPrimaryRnuNdflQuery();

    private static String buildFindRefBookPersonByPrimaryRnuNdflQuery() {
        StringBuilder SQL = new StringBuilder();
        SQL.append("WITH t AS ");
        SQL.append("  (SELECT MAX(version) version, record_id, :pVersion calc_date ");
        SQL.append("  FROM ref_book_person r ");
        SQL.append("  WHERE status = 0 ");
        SQL.append("  AND version <= :pVersion ");
        SQL.append("  AND NOT EXISTS ");
        SQL.append("    (SELECT 1 ");
        SQL.append("    FROM ref_book_person r2 ");
        SQL.append("    WHERE r2.record_id = r.record_id ");
        SQL.append("    AND r2.status     !=             -1 ");
        SQL.append("    AND r2.version BETWEEN r.version + interval '1' DAY AND :pVersion ");
        SQL.append("    ) ");
        SQL.append("  GROUP BY record_id ");
        SQL.append("  ) ");
        SQL.append("SELECT ");
        SQL.append("  dubl.last_name AS ndfl_person_ln, dubl.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, ");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, ");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, ");
        SQL.append("  taxpayer_id.id AS inp_id, taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, ");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address ");
        SQL.append("FROM t ");
        SQL.append("JOIN ");
        SQL.append("  (SELECT np.id id, lower(np.last_name) last_name, lower(np.first_name) first_name, lower(np.middle_name) middle_name, np.birth_day birthday, REPLACE(REPLACE(lower(np.snils), ' ', ''), '-', '') snils, np.inn_np inn, np.inn_foreign inn_f, dt.id id_doc_type, REPLACE(lower(np.id_doc_number), ' ', '') id_doc_number, lower(np.inp) inp, :pVersion calc_date ");
        SQL.append("  FROM ndfl_person np ");
        SQL.append("  LEFT JOIN ref_book_doc_type dt ");
        SQL.append("  ON (dt.code               = np.id_doc_type ");
        SQL.append("  AND dt.status             = 0) ");
        SQL.append("  WHERE declaration_data_id = :pDeclarationId ");
        SQL.append("  ) dubl ON (dubl.calc_date = t.calc_date) ");
        SQL.append("JOIN ref_book_person person ");
        SQL.append("ON (person.status                                    = 0 ");
        SQL.append("AND person.version                                   = t.version ");
        SQL.append("AND person.record_id                                 = t.record_id ");
        SQL.append("AND ((REPLACE(lower(person.last_name), ' ', '')      = dubl.last_name ");
        SQL.append("AND REPLACE(lower(person.first_name), ' ', '')       = dubl.first_name ");
        SQL.append("AND REPLACE(lower(person.middle_name), ' ', '')      = dubl.middle_name ");
        SQL.append("AND person.birth_date                                = dubl.birthday ");
        SQL.append("OR (REPLACE(REPLACE(person.snils, ' ', ''), '-', '') = dubl.snils) ");
        SQL.append("OR (REPLACE(person.inn, ' ', '')                     = dubl.inn) ");
        SQL.append("OR (REPLACE(person.inn_foreign, ' ', '')             = dubl.inn_f)))) ");
        SQL.append("LEFT JOIN ref_book_address addr ");
        SQL.append("ON (addr.id = person.address) ");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc ");
        SQL.append("ON (person_doc.person_id = person.id) ");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id ");
        SQL.append("ON (taxpayer_id.person_id                           = person.id) ");
        SQL.append("WHERE (taxpayer_id.as_nu                            = :asnuId ");
        SQL.append("AND (lower(taxpayer_id.inp)                         = dubl.inp) ");
        SQL.append("OR (person_doc.doc_id                               = dubl.id_doc_type ");
        SQL.append("AND (REPLACE(lower(person_doc.doc_number), ' ', '') = dubl.id_doc_number)))");
        return SQL.toString();
    }


    @Override
    public Map<Long, List<PersonData>> findRefBookPersonByPrimaryRnuNdfl(Long declarationDataId, Long asnuId, Date version) {

        MapSqlParameterSource param = new MapSqlParameterSource();

        param.addValue("pDeclarationId", declarationDataId);
        param.addValue("asnuId", asnuId);
        param.addValue("pVersion", version);

        long time = System.currentTimeMillis();
        Map<Long, List<PersonData>> result = getNamedParameterJdbcTemplate().query(FIND_REFBOOK_PERSON_BY_PRIMARY_RNU_NDFL_QUERY, param, new PersonDataExtractor());
        System.out.println("findRefBookPersonByPrimaryRnuNdfl size: " + result.size() + " (" + (System.currentTimeMillis() - time) + " ms)");

        return result;
    }

    private static final String FIND_REFBOOK_PERSON_BY_PRIMARY_1151111_QUERY = buildFindRefBookPersonByPrimary1151111lQuery();

    private static String buildFindRefBookPersonByPrimary1151111lQuery() {
        StringBuilder SQL = new StringBuilder();
        SQL.append("WITH t AS \n");
        SQL.append("  (SELECT MAX(version) version, record_id, :pVersion calc_date \n");
        SQL.append("  FROM ref_book_person r \n");
        SQL.append("  WHERE status = 0 \n");
        SQL.append("  AND version <= :pVersion \n");
        SQL.append("  AND NOT EXISTS \n");
        SQL.append("    (SELECT 1 \n");
        SQL.append("    FROM ref_book_person r2 \n");
        SQL.append("    WHERE r2.record_id = r.record_id \n");
        SQL.append("    AND r2.status     !=             -1 \n");
        SQL.append("    AND r2.version BETWEEN r.version + interval '1' DAY AND :pVersion \n");
        SQL.append("    ) \n");
        SQL.append("  GROUP BY record_id \n");
        SQL.append("  ) \n");
        SQL.append("SELECT \n");
        SQL.append("  --фл \n");
        SQL.append("  dubl.last_name AS ndfl_person_ln, dubl.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  NULL AS inp, NULL AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM t \n");
        SQL.append("JOIN \n");
        SQL.append("  (SELECT np.id id, lower(np.FAMILIA) last_name, lower(np.IMYA) first_name, lower(np.OTCHESTVO) middle_name, np.DATA_ROZD birthday, REPLACE(REPLACE(lower(np.SNILS), ' ', ''), '-', '') snils, np.INNFL inn, dt.id id_doc_type, REPLACE(lower(np.SER_NOM_DOC), ' ', '') id_doc_number, :pVersion calc_date \n");
        SQL.append("  FROM raschsv_pers_sv_strah_lic np \n");
        SQL.append("  LEFT JOIN ref_book_doc_type dt \n");
        SQL.append("  ON (dt.code               = np.KOD_VID_DOC \n");
        SQL.append("  AND dt.status             = 0) \n");
        SQL.append("  WHERE declaration_data_id = :pDeclarationId \n");
        SQL.append("  ) dubl ON (dubl.calc_date = t.calc_date) \n");
        SQL.append("JOIN ref_book_person person \n");
        SQL.append("ON (person.status                                    = 0 \n");
        SQL.append("AND person.version                                   = t.version \n");
        SQL.append("AND person.record_id                                 = t.record_id \n");
        SQL.append("AND ((REPLACE(lower(person.last_name), ' ', '')      = dubl.last_name \n");
        SQL.append("AND REPLACE(lower(person.first_name), ' ', '')       = dubl.first_name \n");
        SQL.append("AND REPLACE(lower(person.middle_name), ' ', '')      = dubl.middle_name \n");
        SQL.append("AND person.birth_date                                = dubl.birthday \n");
        SQL.append("OR (REPLACE(REPLACE(person.snils, ' ', ''), '-', '') = dubl.snils) \n");
        SQL.append("OR (REPLACE(person.inn, ' ', '')                     = dubl.inn)))) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id = person.address) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.person_id                            = person.id) \n");
        SQL.append("WHERE ((person_doc.doc_id                           = dubl.id_doc_type \n");
        SQL.append("AND (REPLACE(lower(person_doc.doc_number), ' ', '') = dubl.id_doc_number)))");
        return SQL.toString();
    }

    @Override
    public Map<Long, List<PersonData>> findRefBookPersonByPrimary1151111(Long declarationDataId, Long asnuId, Date version) {

        MapSqlParameterSource param = new MapSqlParameterSource();
        param.addValue("pDeclarationId", declarationDataId);
        param.addValue("asnuId", asnuId);
        param.addValue("pVersion", version);

        long time = System.currentTimeMillis();

        Map<Long, List<PersonData>> result = getNamedParameterJdbcTemplate().query(FIND_REFBOOK_PERSON_BY_PRIMARY_1151111_QUERY, param, new PersonDataExtractor());

        System.out.println("findRefBookPersonByPrimary1151111 size: " + result.size() + " (" + (System.currentTimeMillis() - time) + " ms)");

        return result;
    }


    private class PersonDataExtractor implements ResultSetExtractor<Map<Long, List<PersonData>>> {

        public Map<Long, List<PersonData>> extractData(ResultSet rs) throws SQLException, DataAccessException {

            Map<Long, List<PersonData>> result = new HashMap<Long, List<PersonData>>();

            System.out.println("result");

            while (rs.next()) {
                Long ndflPersonId = SqlUtils.getLong(rs, "ndfl_person_id");

                if (ndflPersonId == null) {
                    throw new ServiceException("Не задано значение person_id");
                }

                List<PersonData> personDataList = result.get(ndflPersonId);

                if (personDataList == null) {
                    personDataList = new ArrayList<PersonData>();
                    result.put(ndflPersonId, personDataList);
                }

                PersonData personData = getPersonData(rs);

                System.out.println("ndflPersonId=" + ndflPersonId + ", " + personData);
                personDataList.add(personData);

            }
            return result;
        }

        private PersonData getPersonData(ResultSet rs) throws SQLException {
            PersonData person = new PersonData();

            //person
            person.setId(SqlUtils.getLong(rs, "person_id"));
            person.setRecordId(SqlUtils.getLong(rs, "person_record_id"));
            person.setLastName(rs.getString("last_name"));
            person.setFirstName(rs.getString("first_name"));
            person.setMiddleName(rs.getString("middle_name"));
            person.setSex(SqlUtils.getInteger(rs, "sex"));
            person.setInn(rs.getString("inn"));
            person.setInnForeign(rs.getString("inn_foreign"));
            person.setSnils(rs.getString("snils"));
            person.setBirthDate(rs.getDate("birth_date"));

            //ссылки на справочники
            person.setTaxPayerStatusId(SqlUtils.getLong(rs, "status_ref_id"));
            person.setCitizenshipId(SqlUtils.getLong(rs, "citizenship_ref_id"));

            //identical
            person.setInp(rs.getString("inp"));
            person.setAsnuId(SqlUtils.getLong(rs, "asnu_ref_id"));

            //documents
            person.setDocumentTypeId(SqlUtils.getLong(rs, "document_type_ref_id"));
            person.setDocumentNumber(rs.getString("document_number"));

            //address
            //TODO рефакторинг, удалить
//            person.setAddressType(SqlUtils.getInteger(rs, "address_type"));
//            person.setCountryId(SqlUtils.getLong(rs, "country_id"));
//            person.setRegionCode(rs.getString("region_code"));
//            person.setPostalCode(rs.getString("postal_code"));
//            person.setDistrict(rs.getString("district"));
//            person.setCity(rs.getString("city"));
//            person.setLocality(rs.getString("locality"));
//            person.setStreet(rs.getString("street"));
//            person.setHouse(rs.getString("house"));
//            person.setBuild(rs.getString("build"));
//            person.setAppartment(rs.getString("appartment"));
//            person.setAddressIno(rs.getString("address"));

            //additional
            person.setPension(SqlUtils.getInteger(rs, "pension"));
            person.setMedical(SqlUtils.getInteger(rs, "midical"));
            person.setSocial(SqlUtils.getInteger(rs, "social"));
            person.setEmployee(SqlUtils.getInteger(rs, "employee"));

            return person;
        }
    }


    @Override
    public Map<Long, List<PersonData>> findRefBookPersonByPrimaryRnuNdflUnion(Long declarationDataId, Long asnuId, Date version) {

        MapSqlParameterSource param = new MapSqlParameterSource();

        param.addValue("decl_data_id", declarationDataId);
        param.addValue("asnuID", asnuId);
        //param.addValue("pVersion", version);

        long time = System.currentTimeMillis();
        Map<Long, List<PersonData>> result = getNamedParameterJdbcTemplate().query(FIND_REFBOOK_PERSON_BY_PRIMARY_RNU_NDFL_UNION_QUERY, param, new PersonDataExtractor());
        System.out.println("findRefBookPersonByPrimaryRnuNdflUnion size: " + result.size() + " (" + (System.currentTimeMillis() - time) + " ms)");

        return result;
    }


    private static final String FIND_REFBOOK_PERSON_BY_PRIMARY_RNU_NDFL_UNION_QUERY = buildFindRefBookPersonByPrimaryRnuNdflUnionQuery();

    private static String buildFindRefBookPersonByPrimaryRnuNdflUnionQuery() {
        StringBuilder SQL = new StringBuilder();
        SQL.append("/*По ФИО*/ \n");
        SQL.append("SELECT \n");
        SQL.append("  /*+ordered use_nl(person) use_nl(person_doc) use_nl(taxpayer_id) use_nl(addr)*/ \n");
        SQL.append("  --фл \n");
        SQL.append("  t.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM ndfl_person t \n");
        SQL.append("JOIN ref_book_person person \n");
        SQL.append("ON (REPLACE(lower(person.last_name),' ','')   = REPLACE(lower(t.last_name),' ','') \n");
        SQL.append("AND REPLACE(lower(person.first_name),' ','')  = REPLACE(lower(t.first_name),' ','') \n");
        SQL.append("AND REPLACE(lower(person.middle_name),' ','') = REPLACE(lower(t.middle_name),' ','') \n");
        SQL.append("AND person.birth_date                         =t.birth_day ) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.person_id=person.id) \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id \n");
        SQL.append("ON (taxpayer_id.person_id = person.id) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id                =person.address) \n");
        SQL.append("WHERE t.declaration_data_id=:decl_data_id \n");
        SQL.append("UNION \n");
        SQL.append(" \n");
        SQL.append("/*по СНИЛСУ*/ \n");
        SQL.append("SELECT \n");
        SQL.append("  /*+ordered use_nl(person) use_nl(person_doc) use_nl(taxpayer_id) use_nl(addr)*/ \n");
        SQL.append("  --фл \n");
        SQL.append("  t.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM ndfl_person t \n");
        SQL.append("JOIN ref_book_person person \n");
        SQL.append("ON (REPLACE(REPLACE(person.snils,' ',''),'-','') = REPLACE(REPLACE(t.snils, ' ', ''), '-', '')) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.person_id=person.id) \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id \n");
        SQL.append("ON (taxpayer_id.person_id = person.id) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id                =person.address) \n");
        SQL.append("WHERE t.declaration_data_id=:decl_data_id \n");
        SQL.append("UNION \n");
        SQL.append(" \n");
        SQL.append("/*По ИННу*/ \n");
        SQL.append("SELECT \n");
        SQL.append("  /*ordered use_nl(person) use_nl(person_doc) use_nl(taxpayer_id) use_nl(addr)*/ \n");
        SQL.append("  --фл \n");
        SQL.append("  t.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM ndfl_person t \n");
        SQL.append("JOIN ref_book_person person \n");
        SQL.append("ON (REPLACE(person.inn,' ','') = REPLACE(t.inn_np,' ','')) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.person_id=person.id) \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id \n");
        SQL.append("ON (taxpayer_id.person_id = person.id) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id                =person.address) \n");
        SQL.append("WHERE t.declaration_data_id=:decl_data_id \n");
        SQL.append("UNION \n");
        SQL.append(" \n");
        SQL.append("/*По ИННу иностранного государства*/ \n");
        SQL.append("SELECT \n");
        SQL.append("  /*ordered use_nl(person) use_nl(person_doc) use_nl(taxpayer_id) use_nl(addr)*/ \n");
        SQL.append("  --фл \n");
        SQL.append("  t.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM ndfl_person t \n");
        SQL.append("JOIN ref_book_person person \n");
        SQL.append("ON (REPLACE(person.inn_foreign,' ','') = REPLACE(t.inn_foreign,' ','')) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.person_id=person.id) \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id \n");
        SQL.append("ON (taxpayer_id.person_id = person.id) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id                =person.address) \n");
        SQL.append("WHERE t.declaration_data_id=:decl_data_id \n");
        SQL.append("UNION \n");
        SQL.append(" \n");
        SQL.append("/*По ДУЛ*/ \n");
        SQL.append("SELECT \n");
        SQL.append("  /*ordered use_nl(person_doc) use_nl(person) use_nl(taxpayer_id) use_nl(addr)*/ \n");
        SQL.append("  --фл \n");
        SQL.append("  t.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM ndfl_person t \n");
        SQL.append("LEFT JOIN ref_book_doc_type dt \n");
        SQL.append("ON (dt.code=t.id_doc_type) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.doc_id                            =dt.id \n");
        SQL.append("AND REPLACE(lower(person_doc.doc_number),' ','') = REPLACE(lower(t.id_doc_number),' ','')) \n");
        SQL.append("LEFT JOIN ref_book_person person \n");
        SQL.append("ON (person.id = person_doc.person_id) \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id \n");
        SQL.append("ON (taxpayer_id.person_id = person.id) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id                =person.address) \n");
        SQL.append("WHERE t.declaration_data_id=:decl_data_id \n");
        SQL.append("UNION \n");
        SQL.append(" \n");
        SQL.append("/*По ИНП*/ \n");
        SQL.append("SELECT \n");
        SQL.append("  /*ordered use_nl(taxpayer_id) use_nl(person)  use_nl(addr) use_nl(person_doc)*/ \n");
        SQL.append("  --фл \n");
        SQL.append("  t.id AS ndfl_person_id, person.id AS person_id, person.record_id AS person_record_id, person.last_name AS last_name, person.first_name AS first_name, person.middle_name AS middle_name, person.sex AS sex, person.birth_date AS birth_date, person.inn AS inn, person.inn_foreign AS inn_foreign, person.snils AS snils, person.pension AS pension, person.medical AS midical, person.social AS social, person.employee AS employee, \n");
        SQL.append("  --ссылки на записи \n");
        SQL.append("  person.citizenship AS citizenship_ref_id, person.taxpayer_state AS status_ref_id, person.version, \n");
        SQL.append("  --документы \n");
        SQL.append("  person_doc.doc_number AS document_number, person_doc.doc_id AS document_type_ref_id, \n");
        SQL.append("  --идентификаторы \n");
        SQL.append("  taxpayer_id.inp AS inp, taxpayer_id.as_nu AS asnu_ref_id, \n");
        SQL.append("  -- адрес \n");
        SQL.append("  addr.id AS addr_id, addr.address_type, addr.country_id, addr.region_code, addr.postal_code, addr.district, addr.city, addr.locality, addr.street, addr.house, addr.build, addr.appartment, addr.status, addr.record_id, addr.address \n");
        SQL.append("FROM ndfl_person t \n");
        SQL.append("LEFT JOIN ref_book_id_tax_payer taxpayer_id \n");
        SQL.append("ON (taxpayer_id.as_nu     = :asnuID \n");
        SQL.append("AND lower(taxpayer_id.inp)=lower(t.inp)) \n");
        SQL.append("LEFT JOIN ref_book_person person \n");
        SQL.append("ON (person.id = taxpayer_id.person_id) \n");
        SQL.append("LEFT JOIN ref_book_address addr \n");
        SQL.append("ON (addr.id=person.address) \n");
        SQL.append("LEFT JOIN ref_book_id_doc person_doc \n");
        SQL.append("ON (person_doc.person_id   =person.id) \n");
        SQL.append("WHERE t.declaration_data_id=:decl_data_id");
        return SQL.toString();
    }


}
