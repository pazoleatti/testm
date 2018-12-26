package com.aplana.sbrf.taxaccounting.service.impl;


import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.List;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Objects.firstNonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Вспомогательный класс для формирования записи истории изменения ФЛ
 */
class PersonChangeLogBuilder {
    private StringBuilder sb = new StringBuilder();

    public String build() {
        sb.insert(0, "В ходе редактирования карточки ФЛ изменено: ");
        return sb.toString();
    }

    public void inpCreated(PersonIdentifier identifier) {
        comma().append("[ Добавлен ИНП: \"").append(format(identifier.getInp())).append(", АСНУ: \"").append(format(identifier.getAsnu().getCode())).append("\" ]");
    }

    public void inpDeleted(PersonIdentifier identifier) {
        comma().append("[ Удалён ИНП: \"").append(format(identifier.getInp())).append(", АСНУ: \"").append(format(identifier.getAsnu().getCode())).append("\" ]");
    }

    public void inpUpdated(PersonIdentifier identifier) {
        comma().append("[ Изменён ИНП: \"").append(format(identifier.getInp())).append(", АСНУ: \"").append(format(identifier.getAsnu().getCode())).append("\" ]");
    }

    public void tbAdded(PersonTb tb) {
        comma().append("[ Добавлен Тербанк: \"").append(format(tb.getTbDepartment().getShortName())).append("\" ]");
    }

    public void tbUpdated(PersonTb tb) {
        comma().append("[ Изменён Тербанк: \"").append(format(tb.getTbDepartment().getShortName())).append("\"]");
    }

    public void tbDeleted(PersonTb tb) {
        comma().append("[ Удалён Тербанк: \"").append(format(firstNonNull(tb.getTbDepartment().getShortName(), tb.getTbDepartment().getName()))).append("\" ]");
    }

    public void dulCreated(IdDoc doc) {
        comma().append("[ Добавлен ДУЛ: \"Код ДУЛ\"=\"")
                .append(format(doc.getDocType()))
                .append("\", \"Серия и номер ДУЛ\"=\"")
                .append(format(doc.getDocumentNumber())).append("\" ]");
    }

    public void dulDeleted(IdDoc doc) {
        comma().append("[ Удалён ДУЛ: \"Код ДУЛ\"=\"").append(format(doc.getDocType().getCode())).append("\", \"Серия и номер ДУЛ\"=\"")
                .append(format(doc.getDocumentNumber())).append("\" ]");
    }

    public void dulUpdated(IdDoc doc) {
        comma().append("[ Изменён ДУЛ: \"Код ДУЛ\"=\"").append(format(doc.getDocType().getCode())).append("\", \"Серия и номер ДУЛ\"=\"")
                .append(format(doc.getDocumentNumber())).append("\" ]");
    }

    public void originalSet(RegistryPersonDTO original) {
        comma().append("[ Назначен ФЛ-оригинал. Идентификатор ФЛ: \"").append(original.getOldId().toString()).append("\", ФИО: \"")
                .append(original.getLastName()).append(" ").append(original.getFirstName())
                .append(isNotEmpty(original.getMiddleName()) ? " " + original.getMiddleName() : "").append("\" ]");
    }

    public void originalDeleted(RegistryPerson original) {
        comma().append("[ Откреплён ФЛ-оригинал. Идентификатор ФЛ: \"").append(original.getOldId().toString()).append("\", ФИО: \"")
                .append(original.getLastName()).append(" ").append(original.getFirstName())
                .append(isNotEmpty(original.getMiddleName()) ? " " + original.getMiddleName() : "").append("\" ]");
    }

    public void duplicatesSet(List<RegistryPerson> duplicates) {
        for (RegistryPerson duplicate : duplicates) {
            comma().append("[ Прикреплен Дубликат ФЛ. Идентификатор ФЛ: \"").append(duplicate.getOldId().toString()).append("\", ФИО: \"")
                    .append(duplicate.getLastName()).append(" ").append(duplicate.getFirstName())
                    .append(isNotEmpty(duplicate.getMiddleName()) ? " " + duplicate.getMiddleName() : "").append("\" ]");
        }
    }

    public void duplicatesDeleted(List<RegistryPerson> duplicates) {
        for (RegistryPerson duplicate : duplicates) {
            comma().append("[ Откреплён Дубликат ФЛ. Идентификатор ФЛ: \"").append(duplicate.getOldId().toString()).append("\", ФИО: \"")
                    .append(duplicate.getLastName()).append(" ").append(duplicate.getFirstName())
                    .append(isNotEmpty(duplicate.getMiddleName()) ? " " + duplicate.getMiddleName() : "").append("\" ]");
        }
    }

    /**
     * Записывает в историю все измененные поля
     */
    public void personInfoUpdated(RegistryPerson personWas, RegistryPerson personBecome) {
        personPropertyUpdated("Дата начала действия", personWas.getStartDate(), personBecome.getStartDate());
        personPropertyUpdated("Дата окончания действия", personWas.getEndDate(), personBecome.getEndDate());
        personPropertyUpdated("Фамилия", personWas.getLastName(), personBecome.getLastName());
        personPropertyUpdated("Имя", personWas.getFirstName(), personBecome.getFirstName());
        personPropertyUpdated("Отчество", personWas.getMiddleName(), personBecome.getMiddleName());
        personPropertyUpdated("Дата рождения", personWas.getBirthDate(), personBecome.getBirthDate());
        personPropertyUpdated("ИНН в РФ", personWas.getInn(), personBecome.getInn());
        personPropertyUpdated("ИНН в стране гражданства", personWas.getInnForeign(), personBecome.getInnForeign());
        personPropertyUpdated("СНИЛС", personWas.getSnils(), personBecome.getSnils());
        if (!equal(emptyObjectToNull(personWas.getTaxPayerState()), emptyObjectToNull(personBecome.getTaxPayerState()))) {
            comma().append("[ ").append("Статус налогоплательщика").append(": ").
                    append(format(personWas.getTaxPayerState())).append(" -> ")
                    .append(format(personBecome.getTaxPayerState())).append(" ]");
        }
        if (!equal(emptyObjectToNull(personWas.getCitizenship()), emptyObjectToNull(personBecome.getCitizenship()))) {
            comma().append("[ ").append("Гражданство").append(": ")
                    .append(format(personWas.getCitizenship())).append(" -> ")
                    .append(format(personBecome.getCitizenship())).append(" ]");
        }
        if (!equal(emptyObjectToNull(personWas.getReportDoc()), emptyObjectToNull(personBecome.getReportDoc()))) {
            comma().append("[ ").append("Документ для отчетности").append(": ")
                    .append(format(personWas.getReportDoc())).append(" -> ")
                    .append(format(personBecome.getReportDoc())).append(" ]");
        }
        if (!equal(emptyObjectToNull(personWas.getSource()), emptyObjectToNull(personBecome.getSource()))) {
            comma().append("[ ").append("Система-источник").append(": ")
                    .append(format(personWas.getSource())).append(" -> ")
                    .append(format(personBecome.getSource())).append(" ]");
        }
        if (!equal(personWas.isVip(), personBecome.isVip())) {
            comma().append("[ ").append("Важность").append(": ")
                    .append(formatVip(personWas.isVip())).append(" -> ")
                    .append(formatVip(personBecome.isVip())).append(" ]");
        }
        personPropertyUpdated("Индекс", personWas.getAddress().getPostalCode(), personBecome.getAddress().getPostalCode());
        personPropertyUpdated("Регион", personWas.getAddress().getRegionCode(), personBecome.getAddress().getRegionCode());
        personPropertyUpdated("Район", personWas.getAddress().getDistrict(), personBecome.getAddress().getDistrict());
        personPropertyUpdated("Город", personWas.getAddress().getCity(), personBecome.getAddress().getCity());
        personPropertyUpdated("Нас. пункт", personWas.getAddress().getLocality(), personBecome.getAddress().getLocality());
        personPropertyUpdated("Улица", personWas.getAddress().getStreet(), personBecome.getAddress().getStreet());
        personPropertyUpdated("Дом", personWas.getAddress().getHouse(), personBecome.getAddress().getHouse());
        personPropertyUpdated("Корпус", personWas.getAddress().getBuild(), personBecome.getAddress().getBuild());
        personPropertyUpdated("Квартира", personWas.getAddress().getAppartment(), personBecome.getAddress().getAppartment());
        if (!equal(emptyObjectToNull(personWas.getAddress().getCountry()), emptyObjectToNull(personBecome.getAddress().getCountry()))) {
            comma().append("[ ").append("Код страны проживания").append(": ")
                    .append(format(personWas.getAddress().getCountry())).append(" -> ")
                    .append(format(personBecome.getAddress().getCountry())).append(" ]");
        }
        personPropertyUpdated("Адрес за пределами РФ", personWas.getAddress().getAddressIno(), personBecome.getAddress().getAddressIno());
    }

    private void personPropertyUpdated(String name, String string1, String string2) {
        if (!equal(string1, string2)) {
            comma().append("[ ").append(name).append(": ").append(format(string1)).append(" -> ").append(format(string2)).append(" ]");
        }
    }

    private void personPropertyUpdated(String name, Date date1, Date date2) {
        if (!equal(date1, date2)) {
            comma().append("[ ").append(name).append(": ").append(format(date1)).append(" -> ").append(format(date2)).append(" ]");
        }
    }

    private PersonChangeLogBuilder comma() {
        if (sb.length() > 0) {
            sb.append(",\n");
        }
        return this;
    }

    private PersonChangeLogBuilder append(String string) {
        sb.append(string);
        return this;
    }

    /**
     * Вместо null могут быть пустые объекты, так что убираем это всё
     */
    private <T extends IdentityObject> T emptyObjectToNull(T object) {
        if (object != null && object.getId() != null) {
            return object;
        }
        return null;
    }

    private String format(String string) {
        return string != null && !string.isEmpty() ? string : "__";
    }

    private String format(Date date) {
        return date != null ? FastDateFormat.getInstance("dd.MM.yyyy").format(date) : "__";
    }

    private String format(RefBookDocType docType) {
        return docType != null && docType.getId() != null ? docType.getCode() : "__";
    }

    private String format(RefBookTaxpayerState taxpayerState) {
        return taxpayerState != null && taxpayerState.getId() != null ? taxpayerState.getCode() : "__";
    }

    private String format(RefBookCountry country) {
        return country != null && country.getId() != null ? country.getCode() : "__";
    }

    private String format(RefBookAsnu asnu) {
        return asnu != null && asnu.getId() != null ? "\"(" + asnu.getCode() + ") " + asnu.getName() + "\"" : "__";
    }

    private String format(IdDoc doc) {
        if (doc != null && doc.getId() != null && doc.getDocType() != null) {
            return "\"" + doc.getDocumentNumber() + " - (" + doc.getDocType().getCode() + ") " + doc.getDocType().getName() + "\"";
        } else {
            return "__";
        }
    }

    private String formatVip(Boolean vip) {
        return vip != null ? vip ? "VIP" : "не VIP" : "__";
    }
}
