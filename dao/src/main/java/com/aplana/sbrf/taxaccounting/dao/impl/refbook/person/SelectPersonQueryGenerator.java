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
class SelectPersonQueryGenerator {

    private static final String DEFAULT_SORT_PROPERTY = "id";

    // Используем параллельный запрос для ускорения работы, с разрешения БД-разработчиков.
    @Language("SQL")
    public static final String SELECT_HINT_CLAUSE = "select /*+ parallel(person,8) first_rows(1)*/\n";
    @Language("SQL")
    public static final String FULL_PERSON_SELECT_BASE = "person.id, person.record_id, person.old_id, person.last_name, person.first_name, \n" +
            "person.middle_name, person.birth_date, person.birth_place, person.vip, person.inn, \n" +
            "person.inn_foreign, person.snils, person.start_date, person.end_date,\n" +
            "doc.id d_id, doc.doc_number, doc_type.id doc_type_id, doc_type.code doc_code, doc_type.name doc_name, doc_type.priority doc_type_priority, \n" +
            "citizenship_country.id citizenship_country_id, citizenship_country.code citizenship_country_code, \n" +
            "citizenship_country.name citizenship_country_name, \n" +
            "state.id state_id, state.code state_code, state.name state_name, \n" +
            "person.postal_code, person.region_code, \n" +
            "person.district, person.city, person.locality, person.street, person.house, \n" +
            "person.build building, person.appartment apartment, person.address_foreign, \n" +
            "address_country.id address_country_id, address_country.code address_country_code, \n" +
            "address_country.name address_country_name, \n" +
            "asnu.id asnu_id, asnu.code asnu_code, asnu.name asnu_name, asnu.type asnu_type, asnu.priority asnu_priority \n" +
            "from ref_book_person person \n" +
            "left join ref_book_id_doc doc on doc.id = person.report_doc \n" +
            "left join ref_book_doc_type doc_type on doc_type.id = doc.doc_id \n" +
            "left join ref_book_country citizenship_country on citizenship_country.id = person.citizenship \n" +
            "left join ref_book_taxpayer_state state on state.id = person.taxpayer_state  \n" +
            "left join ref_book_country address_country on address_country.id = person.country_id \n" +
            "left join ref_book_asnu asnu on asnu.id = person.source_id";

    @Language("SQL")
    public static final String SELECT_FULL_PERSON = SELECT_HINT_CLAUSE + FULL_PERSON_SELECT_BASE;

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
        SORT_FIELDS_BY_PROPERTY.put("docType", Arrays.asList("doc_code", "doc_name", "id"));
        SORT_FIELDS_BY_PROPERTY.put("docNumber", Arrays.asList("doc_number", "id"));
        SORT_FIELDS_BY_PROPERTY.put("citizenship", Arrays.asList("citizenship_country_code", "id"));
        SORT_FIELDS_BY_PROPERTY.put("taxPayerState", Arrays.asList("state_code", "id"));
        SORT_FIELDS_BY_PROPERTY.put("inn", Arrays.asList("inn", "id"));
        SORT_FIELDS_BY_PROPERTY.put("innForeign", Arrays.asList("inn_foreign", "id"));
        SORT_FIELDS_BY_PROPERTY.put("snils", Arrays.asList("snils", "id"));
        SORT_FIELDS_BY_PROPERTY.put("address", Arrays.asList("postal_code", "region_code", "district", "city", "locality", "street",
                "house", "building", "apartment", "id"));
        SORT_FIELDS_BY_PROPERTY.put("foreignAddress", Arrays.asList("address_country_code", "address", "address_id", "id"));
        SORT_FIELDS_BY_PROPERTY.put("source", Arrays.asList("asnu_code", "id"));
        SORT_FIELDS_BY_PROPERTY.put("startDate", Arrays.asList("version", "id"));
        SORT_FIELDS_BY_PROPERTY.put("endDate", Arrays.asList("version_to", "id"));
        SORT_FIELDS_BY_PROPERTY.put("id", Collections.singletonList("id"));
    }

    // Значения из pagingParams.getProperty(), для которых нужна дополнительная сортировка по полю 'vip'
    private static final List<String> PERMISSIVE_PROPERTIES =
            Arrays.asList("vip", "docName", "docNumber", "inn", "innForeign", "snils", "address", "foreignAddress");


    @Language("SQL")
    protected String query;

    protected RefBookPersonFilter filter;
    private PagingParams pagingParams;

    SelectPersonQueryGenerator(RefBookPersonFilter filter) {
        this.filter = filter;
    }

    SelectPersonQueryGenerator(RefBookPersonFilter filter, PagingParams pagingParams) {
        this.filter = filter;
        this.pagingParams = pagingParams;
    }

    /**
     * Генерирует SQL-запрос с фильтром.
     */
    String generateFilteredQuery() {
        initSelectPerson(false);
        addWhereConditions();
        return query;
    }

    /**
     * Генерирует SQL-запрос с фильтром, сортировкой и пагинацией.
     */
    String generatePagedAndFilteredQuery() {
        initSelectPerson(true);
        addWhereConditions();
        addOrder();
        addPagination();
        return query;
    }

    public void setPagingParams(PagingParams pagingParams) {
        this.pagingParams = pagingParams;
    }

    private void initSelectPerson(boolean orderOptimization) {
        if (orderOptimization && pagingParams != null) {
            String sortProperty = getSortProperty(pagingParams);
            SortDirection sortDirection = SortDirection.of(pagingParams.getDirection());
            generateOrderOptimizationClause(sortProperty, sortDirection);
            query = generateOrderOptimizationClause(sortProperty, sortDirection) + FULL_PERSON_SELECT_BASE;
        } else {
            query = SELECT_FULL_PERSON;
        }
    }

    protected void addWhereConditions() {
        if (filter != null) {
            query = query + "\n" + "where 1 = 1";
            addLike("person.old_id", filter.getId());
            addVipCondition();
            addLikeIgnoreCase("person.last_name", filter.getLastName());
            addLikeIgnoreCase("person.first_name", filter.getFirstName());
            addLikeIgnoreCase("person.middle_name", filter.getMiddleName());
            addLikeIgnoreCase("person.inn", filter.getInn());
            addLikeIgnoreCase("person.inn_foreign", filter.getInnForeign());
            addLikeIgnoreCaseAndDelimiters("person.snils", filter.getSnils());
            addBirthDateConditions();
            addTBCondition();
            addDocumentsConditions();
            addSearchIn("citizenship_country.id", filter.getCitizenshipCountryIds());
            addSearchIn("person.taxpayer_state", filter.getTaxpayerStateIds());
            addSearchIn("person.source_id", filter.getSourceSystemIds());
            addInpCondition();
            addAddressConditions();
            addForeignAddressConditions();
            addDuplicatesCondition();
            addVersionsConditions();
        }
    }

    private void addVipCondition() {
        Boolean isVip = filter.getVip();
        if (isVip != null) {
            query = query + "\n" + "and person.vip = " + toInt(isVip);
        }
    }

    private int toInt(boolean bool) {
        return bool ? 1 : 0;
    }

    protected void addBirthDateConditions() {
        if (filter.getBirthDateFrom() != null) {
            query = query + "\n" + "and birth_date >= " + DateUtils.formatForSql(filter.getBirthDateFrom());
        }
        if (filter.getBirthDateTo() != null) {
            query = query + "\n" + "and birth_date <= " + DateUtils.formatForSql(filter.getBirthDateTo());
        }
    }

    private void addTBCondition() {
        List<Integer> terBanks = filter.getTerBankIds();
        if (isNotEmpty(terBanks)) {
            query = query + "\n" +
                    "and person.record_id in ( \n" +
                    "   select record_id \n" +
                    "   from ref_book_person p \n" +
                    "   where p.id in ( \n" +
                    "       select person_id \n" +
                    "       from ref_book_person_tb \n" +
                    "       where " + searchIn("tb_department_id", terBanks) + "\n" +
                    "    ) \n" +
                    ")";
        }
    }

    protected void addDocumentsConditions() {
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
                            searchIn("d.doc_id", filter.getDocTypeIds())) +
                    "    ) \n" +
                    ")";
        }
    }

    private boolean isDocumentsFilterNotEmpty() {
        return isNotEmpty(filter.getDocTypeIds()) || isNotEmpty(filter.getDocumentNumber());
    }

    private void addInpCondition() {
        if (isNotEmpty(filter.getInp())) {
            query = query + "\n" +
                    "and person.record_id in ( \n" +
                    "   select record_id \n" +
                    "   from ref_book_person p \n" +
                    "   where p.id in ( \n" +
                    "       select person_id \n" +
                    "       from ref_book_id_tax_payer \n" +
                    "       where " + likeIgnoreCase("inp", filter.getInp()) + " \n" +
                    "   ) \n" +
                    ")";
        }
    }

    private void addAddressConditions() {
        addLikeIgnoreCase("postal_code", filter.getPostalCode());
        addLikeIgnoreCase("region_code", filter.getRegion());
        addLikeIgnoreCase("district", filter.getDistrict());
        addLikeIgnoreCase("city", filter.getCity());
        addLikeIgnoreCase("locality", filter.getLocality());
        addLikeIgnoreCase("street", filter.getStreet());
    }

    private void addForeignAddressConditions() {
        addLikeIgnoreCase("person.address_foreign", filter.getForeignAddress());
        addSearchIn("address_country.id", filter.getCountryIds());
    }

    private void addDuplicatesCondition() {
        Boolean showOnlyDuplicates = filter.getDuplicates();
        if (showOnlyDuplicates != null) {
            String equalitySign = showOnlyDuplicates ? "<>" : "=";
            query = query + "\n" + "and person.record_id " + equalitySign + " person.old_id";
        }
    }

    protected void addVersionsConditions() {
        if (notAllVersions()) {
            String versionStr = DateUtils.formatForSql(filter.getVersionDate());
            query = "" +
                    "select * \n" +
                    "from (" + query + ") \n" +
                    "where start_date <= " + versionStr + " and (end_date >= " + versionStr + " or end_date is null)";
        }
    }

    private boolean notAllVersions() {
        return (filter.getAllVersions() != null && !filter.getAllVersions() && filter.getVersionDate() != null);
    }

    protected void addLike(String field, String value) {
        if (isNotEmpty(value)) {
            query = query + "\n" + "and " + field + " like '%" + value + "%'";
        }
    }

    protected void addLikeIgnoreCase(String field, String value) {
        if (isNotEmpty(value)) {
            query = query + "\n" + "and " + likeIgnoreCase(field, value);
        }
    }

    protected String likeIgnoreCase(String field, String value) {
        return "lower(" + field + ") like '%" + value.toLowerCase() + "%'";
    }

    protected void addLikeIgnoreCaseAndDelimiters(String field, String value) {
        if (isNotEmpty(value)) {
            query = query + "\n" + "and " + likeIgnoreCaseAndDelimiters(field, value);
        }
    }

    private String likeIgnoreCaseAndDelimiters(String field, String value) {
        if (isEmpty(value)) {
            return null;
        }
        String filteredValue = StringUtils.filterDelimiters(value);
        String lowerCaseValue = filteredValue.toLowerCase();
        return "regexp_replace(lower(" + field + "),'[^0-9A-Za-zА-Яа-я]','') like '%" + lowerCaseValue + "%'";
    }

    private void addSearchIn(String field, Collection<?> values) {
        if (isNotEmpty(values)) {
            query = query + "\n" + "and " + searchIn(field, values);
        }
    }

    private String searchIn(String field, Collection<?> values) {
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

    /**
     * Генерирует часть селекта касающуюся динамической оптимизации в зависимости от сортировки
     * @param sortProperty  свойство для сортировки
     * @param sortDirection направление сортировки.
     * @return  сгенерированное выражение
     */
    private String generateOrderOptimizationClause(String sortProperty, SortDirection sortDirection) {
        StringBuilder builder = new StringBuilder("select /*+ ");
        if (sortDirection.equals(SortDirection.ASC)) {
            builder.append("index_asc(");
        } else {
            builder.append("index_desc(");
        }
        switch (sortProperty) {
            case DEFAULT_SORT_PROPERTY: {
                builder.append("person PK_REF_BOOK_PERSON) ");
                break;
            }
            default: {
                return SELECT_HINT_CLAUSE;
            }
        }

        builder.append("parallel(person,8) first_rows(1)*/ ");
        return builder.toString();
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
