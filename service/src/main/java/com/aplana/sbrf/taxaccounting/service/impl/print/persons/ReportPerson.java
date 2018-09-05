package com.aplana.sbrf.taxaccounting.service.impl.print.persons;

import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAddress;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Обертка над {@link RefBookPerson} для получения данных отображаемых в отчете
 */
public class ReportPerson {
    private RefBookPerson person;

    ReportPerson(RefBookPerson person) {
        this.person = person;
    }

    String getFLId() {
        if (person.getOldId() != null && person.getRecordId() != null) {
            return person.getOldId() + (person.getOldId().equals(person.getRecordId()) ? "" : " (Дубл.)");
        }
        return null;
    }

    String getVip() {
        return person.isVip() != null && person.isVip() ? "VIP" : "Не VIP";
    }

    String getLastName() {
        return person.getLastName();
    }

    String getFirstName() {
        return person.getFirstName();
    }

    String getMiddleName() {
        return person.getMiddleName();
    }

    String getBirthDay() {
        return person.getBirthDate() != null ? FastDateFormat.getInstance("dd.MM.yyyy").format(person.getBirthDate()) : null;
    }

    String getDocName() {
        return getPermissiveValue(person.getDocNameForJson());
    }

    String getDocNumber() {
        return getPermissiveValue(person.getDocNumberForJson());
    }

    String getCitizenship() {
        return person.getCitizenship() != null ? "(" + person.getCitizenship().getCode() + ") " + person.getCitizenship().getName() : null;
    }

    String getTaxpayerState() {
        return person.getTaxpayerState() != null ? person.getTaxpayerState().getCode() : null;
    }

    String getInn() {
        return getPermissiveValue(person.getInnForJson());
    }

    String getInnForeign() {
        return getPermissiveValue(person.getInnForeignForJson());
    }

    String getSnils() {
        return getPermissiveValue(person.getSnilsForJson());
    }

    private String getPermissiveValue(Permissive<String> permissive) {
        if (permissive != null) {
            return permissive.hasPermission() ? permissive.value() : "Доступ ограничен";
        }
        return null;
    }

    String getRussianAddress() {
        if (person.getAddress() != null) {
            if (person.getAddressForJson().hasPermission()) {
                return person.getAddress().getAddressType() == 0 ? getAddressString(person.getAddress()) : null;
            } else {
                return "Доступ ограничен";
            }
        }
        return null;
    }

    String getForeignAddress() {
        if (person.getAddress() != null) {
            if (person.getAddressForJson().hasPermission()) {
                return person.getAddress().getAddressType() == 1 ? getAddressString(person.getAddress()) : null;
            } else {
                return "Доступ ограничен";
            }
        }
        return null;
    }

    String getSource() {
        return person.getSource() != null ? "(" + person.getSource().getCode() + ") " + person.getSource().getName() : null;
    }

    Date getVersion() {
        return person.getVersion();
    }

    Date getVersionEnd() {
        return person.getVersionEnd();
    }

    Long getId() {
        return person.getId();
    }

    private String getAddressString(RefBookAddress address) {
        List<String> values = new ArrayList<>();
        if (address.getAddressType() == 0) {
            addIfNotEmpty(values, address.getPostalCode());
            addIfNotEmpty(values, address.getRegionCode());
            addIfNotEmpty(values, address.getDistrict());
            addIfNotEmpty(values, address.getCity());
            addIfNotEmpty(values, address.getLocality());
            addIfNotEmpty(values, address.getStreet());
            addIfNotEmpty(values, address.getHouse());
            addIfNotEmpty(values, address.getBuild());
            addIfNotEmpty(values, address.getAppartment());
        } else {
            if (address.getCountry() != null) {
                addIfNotEmpty(values, address.getCountry().getName());
            }
            addIfNotEmpty(values, address.getAddress());
        }
        return Joiner.on(", ").join(values);
    }

    private void addIfNotEmpty(List<String> values, String string) {
        if (!isEmpty(string)) {
            values.add(string);
        }
    }
}
