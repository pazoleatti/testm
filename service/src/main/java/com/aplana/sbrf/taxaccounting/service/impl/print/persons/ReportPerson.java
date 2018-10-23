package com.aplana.sbrf.taxaccounting.service.impl.print.persons;

import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.identification.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPersonDTO;
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
    private RegistryPersonDTO person;

    ReportPerson(RegistryPersonDTO person) {
        this.person = person;
    }

    String getFLId() {
        if (person.getOldId() != null && person.getRecordId() != null) {
            return person.getOldId() + (person.getOldId().equals(person.getRecordId()) ? "" : " (Дубл.)");
        }
        return null;
    }

    String getVip() {
        return person.getVip() ? "VIP" : "Не VIP";
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
        RefBookDocType docType = person.getReportDoc().value().getDocType();
        try {
            if (person.getReportDoc().hasPermission()) {
                return "(" + docType.getCode() + ") " + docType.getName();
            } else {
                return "Доступ ограничен";
            }
        } catch (NullPointerException npe) {
            return null;
        }
    }

    String getDocNumber() {
        return getPermissiveValue(Permissive.<String>forbidden());
    }

    String getCitizenship() {
        return person.getCitizenship().value() != null ? "(" + person.getCitizenship().value().getCode() + ") " + person.getCitizenship().value().getName() : null;
    }

    String getTaxpayerState() {
        return person.getTaxPayerState().value() != null ? person.getTaxPayerState().value().getCode() : null;
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
                return person.getAddress().value() != null ? getAddressString(person.getAddress().value()) : null;
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

    private String getAddressString(Address address) {
        List<String> values = new ArrayList<>();
            addIfNotEmpty(values, address.getPostalCode());
            addIfNotEmpty(values, address.getRegionCode());
            addIfNotEmpty(values, address.getDistrict());
            addIfNotEmpty(values, address.getCity());
            addIfNotEmpty(values, address.getLocality());
            addIfNotEmpty(values, address.getStreet());
            addIfNotEmpty(values, address.getHouse());
            addIfNotEmpty(values, address.getBuild());
            addIfNotEmpty(values, address.getAppartment());

        if (address.getCountry() != null) {
                addIfNotEmpty(values, address.getCountry().getName());
        }
        addIfNotEmpty(values, address.getAddressIno());
        return Joiner.on(", ").join(values);
    }

    private void addIfNotEmpty(List<String> values, String string) {
        if (!isEmpty(string)) {
            values.add(string);
        }
    }
}
