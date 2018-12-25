package com.aplana.sbrf.taxaccounting.dao.impl.refbook.person;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.filter.refbook.RefBookPersonFilter;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Генератор SQL-запросов для RefBookPerson при поиске оригиналов и дубликатов
 */
public class SelectPersonOriginalDuplicatesQueryGenerator extends SelectPersonQueryGenerator {
    public SelectPersonOriginalDuplicatesQueryGenerator(RefBookPersonFilter filter) {
        super(filter);
    }

    public SelectPersonOriginalDuplicatesQueryGenerator(RefBookPersonFilter filter, PagingParams pagingParams) {
        super(filter, pagingParams);
    }

    @Override
    protected void addWhereConditions() {
        if (filter != null) {
            query = query + "\n" + "where 1 = 1";
            addLike("person.old_id", filter.getId());
            addLikeIgnoreCase("person.last_name", filter.getLastName());
            addLikeIgnoreCase("person.first_name", filter.getFirstName());
            addLikeIgnoreCase("person.middle_name", filter.getMiddleName());
            addInnCondition(filter.getInn());
            addLikeIgnoreCaseAndDelimiters("person.snils", filter.getSnils());
            addBirthDateConditions();
            addDocumentsConditions();
            addVersionsConditions();
            selfExcludeCondition(filter.getRecordId());
            excludeDuplicates();
        }
    }

    private void addInnCondition(String value) {
        if (isNotEmpty(value)) {
            query = query + "\n" + "and (" + likeIgnoreCase("person.inn", value) + " or " + likeIgnoreCase("person.inn_foreign", value) + ")";
        }
    }

    private void selfExcludeCondition(Long value) {
        if (value != null) {
            query = query + "\n" + "and record_id <> " + value;
        }
    }

    private void excludeDuplicates() {
        query = query + "\n" + "and record_id = old_id";
    }
}
