package com.aplana.sbrf.taxaccounting.service.impl.print.persons;

import com.aplana.sbrf.taxaccounting.model.Permissive;
import com.aplana.sbrf.taxaccounting.model.refbook.Address;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPersonDTO;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Обертка над {@link RegistryPersonDTO} для получения данных отображаемых в отчете
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
        return person.isVip() ? "VIP" : "Не VIP";
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
        if (person.getReportDoc() != null) {
            try {
                if (person.getReportDoc().hasPermission()) {
                    if (person.getReportDoc().value() != null) {
                        RefBookDocType docType = person.getReportDoc().value().getDocType();
                        return "(" + docType.getCode() + ") " + docType.getName();
                    } else {
                        return null;
                    }
                } else {
                    return "Доступ ограничен";
                }
            } catch (NullPointerException npe) {
                return null;
            }
        } else {
            return null;
        }
    }

    String getDocNumber() {
        if (person.getReportDoc() != null) {
            try {
                if (person.getReportDoc().hasPermission()) {
                    if (person.getReportDoc().value() != null) {
                        return person.getReportDoc().value().getDocumentNumber();
                    } else {
                        return null;
                    }
                } else {
                    return "Доступ ограничен";
                }
            } catch (NullPointerException npe) {
                return null;
            }
        } else {
            return null;
        }
    }

    String getCitizenship() {
        return person.getCitizenship() != null && person.getCitizenship().value() != null && person.getCitizenship().value().getCode() != null ? "(" + person.getCitizenship().value().getCode() + ") " + person.getCitizenship().value().getName() : null;
    }

    String getTaxpayerState() {
        return person.getTaxPayerState() != null && person.getTaxPayerState().value() != null && person.getTaxPayerState().value().getCode() != null ? person.getTaxPayerState().value().getCode() : null;
    }

    String getInn() {
        return getPermissiveValue(person.getInn());
    }

    String getInnForeign() {
        return getPermissiveValue(person.getInnForeign());
    }

    String getSnils() {
        return getPermissiveValue(person.getSnils());
    }

    private String getPermissiveValue(Permissive<String> permissive) {
        if (permissive != null) {
            return permissive.hasPermission() ? permissive.value() : "Доступ ограничен";
        }
        return null;
    }

    String getRussianAddress() {
        if (person.getAddress() != null) {
            if (person.getAddress().hasPermission()) {
                return person.getAddress().value() != null ? getAddressString(person.getAddress().value()) : null;
            } else {
                return "Доступ ограничен";
            }
        }
        return null;
    }

    String getForeignAddress() {
        if (person.getAddress().hasPermission()) {
            return person.getAddress().value().getAddressIno();
        } else {
            return "Доступ ограничен";
        }
    }

    String getSource() {
        return person.getSource() != null && person.getSource().getCode() != null? "(" + person.getSource().getCode() + ") " + person.getSource().getName() : null;
    }

    Date getVersion() {
        return person.getStartDate();
    }

    Date getVersionEnd() {
        return person.getEndDate();
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
