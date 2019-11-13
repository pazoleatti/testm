package com.aplana.sbrf.taxaccounting.dao.impl.sqlBuilder;

import com.aplana.sbrf.taxaccounting.model.DeclarationType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.dto.Declaration2NdflFLDTO;
import com.aplana.sbrf.taxaccounting.model.filter.Declaration2NdflFLFilter;
import com.google.common.base.Joiner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isAllEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Формирует запрос на список форм 2-ДФЛ (ФЛ)
 */
public class Declaration2NdflFLSqlBuilder {
    private String query;
    private String countQuery;
    private MapSqlParameterSource params = new MapSqlParameterSource();

    public RowMapper<Declaration2NdflFLDTO> getRowMapper() {
        return new RowMapper<Declaration2NdflFLDTO>() {
            @Override
            public Declaration2NdflFLDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
                Declaration2NdflFLDTO item = new Declaration2NdflFLDTO();
                item.setDeclarationDataId(rs.getLong("declarationDataId"));
                item.setDeclarationType(rs.getString("declarationType"));
                item.setPersonId(rs.getLong("personId"));
                item.setPerson(rs.getString("person"));
                item.setDepartment(rs.getString("department"));
                item.setReportPeriod(rs.getString("reportPeriod"));
                item.setState(rs.getString("state"));
                item.setKpp(rs.getString("kpp"));
                item.setOktmo(rs.getString("oktmo"));
                item.setCreationDate(new Date(rs.getTimestamp("creationDate").getTime()));
                item.setCreationUserName(rs.getString("creationUserName"));
                item.setNote(rs.getString("note"));
                return item;
            }
        };
    }

    public void build(Declaration2NdflFLFilter filter, PagingParams pagingParams) {
        //language=sql
        String baseQuery = "" +
                "select dd.id declarationDataId, dtype.name declarationType, person.id personId,\n" +
                "  nvl2(person.last_name, person.last_name || ' ', '') || " +
                "    nvl2(person.first_name, person.first_name || ' ', '') || " +
                "    nvl(person.middle_name, '') person, " +
                "  dep_fullpath.shortname department,\n" +
                "  case when drp.correction_date is not null \n" +
                "    then tp.year || ': ' || rp.name || ', корр. (' || to_char(drp.correction_date, 'DD.MM.YYYY') || ')'\n" +
                "    else tp.year || ': ' || rp.name\n" +
                "  end as reportPeriod, state.name state, dd.kpp, dd.oktmo, dd.created_date creationDate, su.name creationUserName, dd.note\n" +
                "from DECLARATION_DATA dd\n" +
                "join ref_book_person person on person.id = dd.person_id\n" +
                "left join ref_book_country citizenship_country on citizenship_country.id = person.citizenship\n" +
                "left join ref_book_country address_country on address_country.id = person.country_id\n" +
                "join SEC_USER su on su.id = dd.created_by\n" +
                "join DECLARATION_TEMPLATE dt on dt.id = dd.declaration_template_id\n" +
                "join DECLARATION_TYPE dtype on dtype.id = dt.declaration_type_id\n" +
                "join DEPARTMENT_REPORT_PERIOD drp on drp.id = dd.department_report_period_id\n" +
                "join REPORT_PERIOD rp on rp.id = drp.report_period_id\n" +
                "join TAX_PERIOD tp on tp.id = rp.tax_period_id\n" +
                "join STATE state on state.id = dd.state\n" +
                "join DEPARTMENT dep on dep.id = drp.department_id\n" +
                "join DEPARTMENT_FULLPATH dep_fullpath on dep_fullpath.id = dep.id\n" +
                whereMultiple(
                        equals("dtype.id", DeclarationType.NDFL_2_FL),
                        in("rp.id", filter.getReportPeriodIds()),
                        in("dep.id", filter.getDepartmentIds()),
                        like("dd.id", filter.getDeclarationDataId()),
                        dateBetween("dd.created_date", filter.getCreationDateFrom(), filter.getCreationDateTo()),
                        in("dd.state", filter.getFormStates()),
                        likeIgnoreCase("dd.note", filter.getNote()),
                        anyLikeIgnoreCase(asList("su.login", "su.name"), filter.getCreationUserName()),
                        likeIgnoreCase("dd.kpp", filter.getKpp()),
                        likeIgnoreCase("dd.oktmo", filter.getOktmo()),
                        likeIgnoreCase("person.last_name", filter.getLastName()),
                        likeIgnoreCase("person.first_name", filter.getFirstName()),
                        likeIgnoreCase("person.middle_name", filter.getMiddleName()),
                        dateBetween("person.birth_date", filter.getBirthDateFrom(), filter.getBirthDateTo()),
                        documentCondition(filter.getDocTypeIds(), filter.getDocumentNumber()),
                        in("citizenship_country.id", filter.getCitizenshipCountryIds()),
                        in("person.taxpayer_state", filter.getTaxpayerStateIds()),
                        likeIgnoreCase("person.inn", filter.getInn()),
                        likeIgnoreCase("person.inn_foreign", filter.getInnForeign()),
                        likeIgnoreCaseAndDelimiters("person.snils", filter.getSnils()),
                        equals("person.vip", filter.getVipInt())
                );

        String orderedSql = baseQuery + " order by " + pagingParams.getProperty() + " " + pagingParams.getDirection();
        String numberedSql = "select rownum rn, ordered.* from (" + orderedSql + ") ordered";
        query = "select * from (" + numberedSql + ") where rn between :start and :end";
        countQuery = "select count(*) from (" + baseQuery + ")";
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());
    }

    private String whereMultiple(String... conditions) {
        if (isAllEmpty(conditions)) {
            return "";
        }
        return "where " + Joiner.on("\n and ").skipNulls().join(conditions);
    }

    private String equals(String field, Object value) {
        if (value != null && isNotEmpty(value.toString())) {
            String paramName = field.replace(".", "_");
            params.addValue(paramName, value);
            return field + " = :" + paramName;
        }
        return null;
    }

    private String dateBetween(String field, Date from, Date to) {
        if (from != null || to != null) {
            String s = "";
            String fieldName = field.replace(".", "_");
            if (from != null) {
                s += field + " >= :" + fieldName + "_from\n";
                params.addValue(fieldName + "_from", from);
            }
            if (to != null) {
                if (isNotEmpty(s)) {
                    s += " and ";
                }
                s += field + " <= :" + fieldName + "_to\n";
                params.addValue(fieldName + "_to", to);
            }
            return s;
        }
        return null;
    }

    private String anyLikeIgnoreCase(List<String> fields, String value) {
        if (isNotEmpty(value)) {
            List<String> conditions = new ArrayList<>();
            for (String field : fields) {
                conditions.add(likeIgnoreCase(field, value));
            }
            return "(" + Joiner.on(" \n or ").skipNulls().join(conditions) + ")";
        }
        return null;
    }

    private String likeIgnoreCase(String field, String value) {
        if (isNotEmpty(value)) {
            String paramName = field.replace(".", "_");
            params.addValue(paramName, value);
            return  field + " like '%' || upper(:" + paramName + ") || '%'";
        }
        return null;
    }

    private String likeIgnoreCaseAndDelimiters(String field, String value) {
        if (isNotEmpty(value)) {
            String filteredValue = com.aplana.sbrf.taxaccounting.model.util.StringUtils.filterDelimiters(value);
            String upperCaseValue = filteredValue.toUpperCase();
            return "regexp_replace(" + field + ",'[^0-9A-Za-zА-Яа-я]','') like '%" + upperCaseValue + "%'";
        }
        return null;
    }

    private String like(String field, Object value) {
        if (value != null && isNotEmpty(value.toString())) {
            String paramName = field.replace(".", "_");
            params.addValue(paramName, value);
            return field + " like '%' || :" + paramName + " || '%'";
        }
        return null;
    }

    private String in(String field, Collection<?> values) {
        if (isNotEmpty(values)) {
            String paramName = field.replace(".", "_");
            params.addValue(paramName, values);
            return field + " in (:" + paramName + ")";
        }
        return null;
    }

    protected String documentCondition(List<Long> docTypeIds, String documentNumber) {
        if (isNotEmpty(docTypeIds) || isNotEmpty(documentNumber)) {
            return "\n" +
                    "person.record_id in ( \n" +
                    "   select record_id \n" +
                    "   from ref_book_person p \n" +
                    "   where p.id in ( \n" +
                    "       select d.person_id \n" +
                    "       from ref_book_id_doc d \n" +
                    whereMultiple(
                            likeIgnoreCaseAndDelimiters("d.doc_number", documentNumber),
                            in("d.doc_id", docTypeIds)) +
                    "    ) \n" +
                    ")";
        }
        return null;
    }

    public String getQuery() {
        return query;
    }

    public String getCountQuery() {
        return countQuery;
    }

    public MapSqlParameterSource getParams() {
        return params;
    }
}
