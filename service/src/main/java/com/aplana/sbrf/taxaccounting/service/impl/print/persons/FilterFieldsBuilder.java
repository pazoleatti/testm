package com.aplana.sbrf.taxaccounting.service.impl.print.persons;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Вспомогательный класс на формирования фильтра в заголовке отчета реестра ФЛ
 */
public class FilterFieldsBuilder {

    private StringBuilder sb = new StringBuilder();
    private FastDateFormat dateFormat = FastDateFormat.getInstance("dd.MM.yyyy");

    String build() {
        return sb.toString();
    }

    private FilterFieldsBuilder append(String string) {
        sb.append(string);
        return this;
    }

    private FilterFieldsBuilder delimiter() {
        if (sb.length() > 0) {
            sb.append("; ");
        }
        return this;
    }

    void add(String fieldName, String value) {
        if (!isEmpty(value)) {
            delimiter().append(fieldName).append(": ").append(value);
        }
    }

    void add(String fieldName, Date dateFrom, Date dateTo) {
        if (dateFrom != null || dateTo != null) {
            delimiter().append(fieldName).append(": с ").append(dateFrom != null ? dateFormat.format(dateFrom) : "-")
                    .append(" по ").append(dateTo != null ? dateFormat.format(dateTo) : "-");
        }
    }

    void addVip(String fieldName, Boolean bool) {
        if (bool != null) {
            delimiter().append(fieldName).append(": ").append(bool ? "VIP" : "Не VIP");
        }
    }

    void addDepartments(String fieldName, List<Department> departments) {
        if (!isEmpty(departments)) {
            delimiter().append(fieldName).append(": ").append(Joiner.on(", ").join(transform(departments, new Function<Department, String>() {
                @Override
                public String apply(Department input) {
                    return input.getShortName();
                }
            })));
        }
    }

    void addDocTypes(String fieldName, List<RefBookDocType> docTypes) {
        if (!isEmpty(docTypes)) {
            delimiter().append(fieldName).append(": ").append(Joiner.on(", ").join(transform(docTypes, new Function<RefBookDocType, String>() {
                @Override
                public String apply(RefBookDocType input) {
                    return "(" + input.getCode() + ") " + input.getName();
                }
            })));
        }
    }

    void addCountries(String fieldName, List<RefBookCountry> countries) {
        if (!isEmpty(countries)) {
            delimiter().append(fieldName).append(": ").append(Joiner.on(", ").join(transform(countries, new Function<RefBookCountry, String>() {
                @Override
                public String apply(RefBookCountry input) {
                    return "(" + input.getCode() + ") " + input.getName();
                }
            })));
        }
    }

    void addTaxpayerStates(String fieldName, List<RefBookTaxpayerState> taxpayerStates) {
        if (!isEmpty(taxpayerStates)) {
            delimiter().append(fieldName).append(": ").append(Joiner.on(", ").join(transform(taxpayerStates, new Function<RefBookTaxpayerState, String>() {
                @Override
                public String apply(RefBookTaxpayerState input) {
                    return "(" + input.getCode() + ") " + input.getName();
                }
            })));
        }
    }

    void addAsnus(String fieldName, List<RefBookAsnu> asnus) {
        if (!isEmpty(asnus)) {
            delimiter().append(fieldName).append(": ").append(Joiner.on(", ").join(transform(asnus, new Function<RefBookAsnu, String>() {
                @Override
                public String apply(RefBookAsnu input) {
                    return "(" + input.getCode() + ") " + input.getName();
                }
            })));
        }
    }
}
