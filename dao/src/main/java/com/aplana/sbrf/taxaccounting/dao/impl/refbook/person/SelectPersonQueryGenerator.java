package com.aplana.sbrf.taxaccounting.dao.impl.refbook.person;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SortDirection;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;
import com.aplana.sbrf.taxaccounting.model.util.DateUtils;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;
import com.google.common.base.Joiner;

import org.intellij.lang.annotations.Language;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.collections4.CollectionUtils.*;

/**
 * Генератор SQL-запросов для RefBookPerson
 */
public class SelectPersonQueryGenerator {

    private static final String DEFAULT_SORT_PROPERTY = "id";

    @Language("SQL")
    private static final String SELECT_FULL_PERSON = "" +
            // Используем параллельный запрос для ускорения работы, с разрешения БД-разработчиков.
            "select /*+ parallel(person,8) first_rows(1)*/ \n" +
            "       person.id, person.record_id, person.old_id, person.last_name, person.first_name, \n" +
            "       person.middle_name, person.birth_date, person.birth_place, person.vip, person.inn, \n" +
            "       person.inn_foreign, person.snils, person.version, \n" +
            "       (   select min(version) - interval '1' day \n" +
            "           from ref_book_person p \n" +
            "           where status in (0, 2) \n" +
            "               and p.version > person.version \n" +
            "               and p.record_id = person.record_id \n" +
            "       ) as version_to, \n" +
            "       doc_type.name doc_name, doc.doc_number, \n" +
            "       citizenship_country.id citizenship_country_id, citizenship_country.code citizenship_country_code, \n" +
            "       citizenship_country.name citizenship_country_name, \n" +
            "       state.id state_id, state.code state_code, state.name state_name, \n" +
            "       address.id address_id, address.address_type, address.postal_code, address.region_code, \n" +
            "       address.district, address.city, address.locality, address.street, address.house, \n" +
            "       address.build building, address.appartment apartment, address.address, \n" +
            "       address_country.id address_country_id, address_country.code address_country_code, \n" +
            "       address_country.name address_country_name, \n" +
            "       asnu.id asnu_id, asnu.code asnu_code, asnu.name asnu_name, asnu.type asnu_type, asnu.priority asnu_priority \n" +
            "from ref_book_person person \n" +
            "   left join ref_book_id_doc doc on doc.id = person.report_doc \n" +
            "   left join ref_book_doc_type doc_type on doc_type.id = doc.doc_id \n" +
            "   left join ref_book_country citizenship_country on citizenship_country.id = person.citizenship \n" +
            "   left join ref_book_taxpayer_state state on state.id = person.taxpayer_state \n" +
            "   left join ref_book_address address on address.id = person.address \n" +
            "   left join ref_book_country address_country on address_country.id = address.country_id \n" +
            "   left join ref_book_asnu asnu on asnu.id = person.source_id \n" +
            "where person.status = 0";

    /**
     * Список полей, по которым сортировать, в зависимости от того, чему равно pagingParams.getProperty()
     */
    private static final Map<String, List<String>> SORT_FIELDS_BY_PROPERTY = new HashMap<>();

    static {
        SORT_FIELDS_BY_PROPERTY.put("oldId", Arrays.asList("old_id", "id"));
        SORT_FIELDS_BY_PROPERTY.put("vip", Collections.singletonList("id"));
        SORT_FIELDS_BY_PROPERTY.put("lastName", Arrays.asList("last_name", "id"));
        SORT_FIELDS_BY_PROPERTY.put("firstName", Arrays.asList("first_name", "id"));
        SORT_FIELDS_BY_PROPERTY.put("middleName", Arrays.asList("middle_name", "id"));
        SORT_FIELDS_BY_PROPERTY.put("birthDate", Arrays.asList("birth_date", "id"));
        SORT_FIELDS_BY_PROPERTY.put("docName", Arrays.asList("doc_name", "id"));
        SORT_FIELDS_BY_PROPERTY.put("docNumber", Arrays.asList("doc_number", "id"));
        SORT_FIELDS_BY_PROPERTY.put("citizenship", Arrays.asList("citizenship_country_code", "id"));
        SORT_FIELDS_BY_PROPERTY.put("taxpayerState", Arrays.asList("state_code", "id"));
        SORT_FIELDS_BY_PROPERTY.put("inn", Arrays.asList("inn", "id"));
        SORT_FIELDS_BY_PROPERTY.put("innForeign", Arrays.asList("inn_foreign", "id"));
        SORT_FIELDS_BY_PROPERTY.put("snils", Arrays.asList("snils", "id"));
        SORT_FIELDS_BY_PROPERTY.put("address", Arrays.asList("postal_code", "region_code", "district", "city", "locality", "street",
                "house", "building", "apartment", "address_id", "id"));
        SORT_FIELDS_BY_PROPERTY.put("foreignAddress", Arrays.asList("address_country_code", "address", "address_id", "id"));
        SORT_FIELDS_BY_PROPERTY.put("source", Arrays.asList("asnu_code", "id"));
        SORT_FIELDS_BY_PROPERTY.put("version", Arrays.asList("version", "id"));
        SORT_FIELDS_BY_PROPERTY.put("versionEnd", Arrays.asList("version_to", "id"));
        SORT_FIELDS_BY_PROPERTY.put("id", Collections.singletonList("id"));
    }

    // Значения из pagingParams.getProperty(), для которых нужна дополнительная сортировка по полю 'vip'
    private static final List<String> PERMISSIVE_PROPERTIES =
            Arrays.asList("vip", "docName", "docNumber", "inn", "innForeign", "snils", "address", "foreignAddress");


    @Language("SQL")
    private String query;

    private RefBookPersonFilter filter;
    private PagingParams pagingParams;

    public SelectPersonQueryGenerator(RefBookPersonFilter filter) {
        this.filter = filter;
    }

    public SelectPersonQueryGenerator(RefBookPersonFilter filter, PagingParams pagingParams) {
        this.filter = filter;
        this.pagingParams = pagingParams;
    }

    /**
     * Генерирует SQL-запрос с фильтром.
     */
    public String generateFilteredQuery() {
        initSelectPerson();
        addWhereConditions();
        return query;
    }

    /**
     * Генерирует SQL-запрос с фильтром, сортировкой и пагинацией.
     */
    public String generatePagedAndFilteredQuery() {
        initSelectPerson();
        addWhereConditions();
        addOrder();
        addPagination();
        return query;
    }


    private void initSelectPerson() {
        query = SELECT_FULL_PERSON;
    }

    private void addWhereConditions() {
        if (filter != null) {
            addLike("person.old_id", filter.getId());
            addLikeIgnoreCase("person.last_name", filter.getLastName());
            addLikeIgnoreCase("person.first_name", filter.getFirstName());
            addLikeIgnoreCase("person.middle_name", filter.getMiddleName());
            addBirthDateConditions();
            addDocumentsConditions();
            addAddressConditions();
            addVersionsConditions();
        }
    }

    private void addLike(String field, String value) {
        if (isNotEmpty(value)) {
            query = query + "\n" + "and " + field + " like '%" + value + "%'";
        }
    }

    private void addLikeIgnoreCase(String field, String value) {
        if (isNotEmpty(value)) {
            query = query + "\n" + "and lower(" + field + ") like '%" + value.toLowerCase() + "%'";
        }
    }

    private void addBirthDateConditions() {
        if (filter.getBirthDateFrom() != null) {
            query = query + "\n" + "and birth_date >= " + DateUtils.formatForSql(filter.getBirthDateFrom());
        }
        if (filter.getBirthDateTo() != null) {
            query = query + "\n" + "and birth_date <= " + DateUtils.formatForSql(filter.getBirthDateTo());
        }
    }

    private void addDocumentsConditions() {
        if (isDocumentsFilterNotEmpty()) {
            query = query + "\n" +
                    "and person.record_id in ( \n" +
                    "   select record_id \n" +
                    "   from ref_book_person p \n" +
                    "   where p.id in ( \n" +
                    "       select d.person_id \n" +
                    "       from ref_book_id_doc d \n" +
                    whereMultiple(
                            likeIgnoreCaseAndDelimiters("d.doc_number", filter.getDocumentNumber()),
                            searchIn("d.doc_id", filter.getDocumentTypes())) +
                    "    ) \n" +
                    ")";
        }
    }

    private boolean isDocumentsFilterNotEmpty() {
        return isNotEmpty(filter.getDocumentTypes()) || isNotEmpty(filter.getDocumentNumber());
    }

    private String searchIn(String field, List<Long> values) {
        if (isEmpty(values)) {
            return null;
        }
        String valuesList = join(values, ", ");
        return field + " in (" + valuesList + ")";
    }

    private String whereMultiple(String... conditions) {
        if (isAllEmpty(conditions)) {
            return "";
        }
        return "where " + Joiner.on(" \n and ").skipNulls().join(conditions);
    }

    private String likeIgnoreCaseAndDelimiters(String field, String value) {
        if (isEmpty(value)) {
            return null;
        }
        String filteredValue = StringUtils.filterDelimiters(value);
        String lowerCaseValue = filteredValue.toLowerCase();
        return "regexp_replace(lower(" + field + "),'[^0-9A-Za-zА-Яа-я]','') like '%" + lowerCaseValue + "%'";
    }

    private void addAddressConditions() {
        addLikeIgnoreCase("postal_code", filter.getPostalCode());
        addLikeIgnoreCase("region_code", filter.getRegion());
        addLikeIgnoreCase("district", filter.getDistrict());
        addLikeIgnoreCase("city", filter.getCity());
        addLikeIgnoreCase("locality", filter.getLocality());
        addLikeIgnoreCase("street", filter.getStreet());
    }

    private void addVersionsConditions() {
        if (notAllVersions()) {
            String versionStr = DateUtils.formatForSql(filter.getVersionDate());
            query = "" +
                    "select * \n" +
                    "from (" + query + ") \n" +
                    "where version <= " + versionStr + " and (version_to >= " + versionStr + " or version_to is null)";
        }
    }

    private boolean notAllVersions() {
        return (filter.isAllVersions() != null && !filter.isAllVersions() && filter.getVersionDate() != null);
    }

    private void addOrder() {
        if (pagingParams != null) {
            query = query + "\n " + "order by ";

            String sortProperty = getSortProperty(pagingParams);
            SortDirection sortDirection = SortDirection.of(pagingParams.getDirection());

            if (isPropertyPermissive(sortProperty)) {
                query = query + "vip " + sortDirection.opposite() + ", ";
            }

            List<String> sortFields = getSortFieldsByProperty(sortProperty);
            String fieldsString = generateSortFieldsString(sortFields, sortDirection);
            query = query + fieldsString;
        }
    }

    private boolean isPropertyPermissive(String field) {
        return PERMISSIVE_PROPERTIES.contains(field);
    }

    private String getSortProperty(PagingParams pagingParams) {
        if (pagingParams == null || isEmpty(pagingParams.getProperty())) {
            return DEFAULT_SORT_PROPERTY;
        } else {
            return pagingParams.getProperty();
        }
    }

    /**
     * (["name", "id"], ASC) -> "name asc, id asc"
     */
    private String generateSortFieldsString(List<String> fields, SortDirection direction) {
        List<String> fieldsWithDirection = new ArrayList<>();
        for (String field : fields) {
            fieldsWithDirection.add(field + " " + direction);
        }
        return Joiner.on(", ").join(fieldsWithDirection);
    }

    private List<String> getSortFieldsByProperty(String sortProperty) {
        List<String> result = SORT_FIELDS_BY_PROPERTY.get(sortProperty);
        if (result != null) {
            return result;
        } else {
            return SORT_FIELDS_BY_PROPERTY.get(DEFAULT_SORT_PROPERTY);
        }
    }

    private void addPagination() {
        if (pagingParams != null && pagingParams.getCount() > 0) {
            int startIndex = pagingParams.getStartIndex();
            int endIndex = startIndex + pagingParams.getCount();
            query = "" +
                    "select * \n" +
                    "from ( \n" +
                    "   select rownum rnum, a.* \n" +
                    "   from ( \n" + query + "\n" + ") a \n" +
                    "   where rownum <= " + endIndex + " \n" +
                    ") \n" +
                    "where rnum > " + startIndex;
        }
    }
}
