package com.aplana.sbrf.taxaccounting.service.impl;


import com.aplana.sbrf.taxaccounting.model.IdentityObject;
import com.aplana.sbrf.taxaccounting.model.refbook.IdDoc;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonIdentifier;
import com.aplana.sbrf.taxaccounting.model.refbook.PersonTb;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookCountry;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDocType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookTaxpayerState;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPersonDTO;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Objects.firstNonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Вспомогательный класс для формирования записи истории изменения ФЛ
 */
class PersonChangeLogBuilder {

    private List<String> changes = new ArrayList<>();

    public String build() {
        return "В ходе редактирования карточки ФЛ изменено: " + Joiner.on(",\n").join(changes);
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    public void inpCreated(PersonIdentifier identifier) {
        changes.add("[ Добавлен ИНП: \"" + format(identifier.getInp()) + "\", АСНУ: \"" + format(identifier.getAsnu().getCode()) + "\" ]");
    }

    public void inpDeleted(PersonIdentifier identifier) {
        changes.add("[ Удалён ИНП: \"" + format(identifier.getInp()) + ", АСНУ: \"" + format(identifier.getAsnu().getCode()) + "\" ]");
    }

    public void inpUpdated(PersonIdentifier identifier) {
        changes.add("[ Изменён ИНП: \"" + format(identifier.getInp()) + ", АСНУ: \"" + format(identifier.getAsnu().getCode()) + "\" ]");
    }

    public void tbAdded(PersonTb tb) {
        changes.add("[ Добавлен Тербанк: \"" + format(tb.getTbDepartment().getShortName()) + "\" ]");
    }

    public void tbUpdated(PersonTb tb) {
        changes.add("[ Изменён Тербанк: \"" + format(tb.getTbDepartment().getShortName()) + "\"]");
    }

    public void tbDeleted(PersonTb tb) {
        changes.add("[ Удалён Тербанк: \"" + format(firstNonNull(tb.getTbDepartment().getShortName(), tb.getTbDepartment().getName())) + "\" ]");
    }

    public void dulCreated(IdDoc doc) {
        changes.add("[ Добавлен ДУЛ. \"Код ДУЛ\": \"" + format(doc.getDocType()) + "\", \"Серия и номер ДУЛ\": \"" + format(doc.getDocumentNumber()) + "\" ]");
    }

    public void dulDeleted(IdDoc doc) {
        changes.add("[ Удалён ДУЛ. \"Код ДУЛ\": \"" + format(doc.getDocType().getCode()) + "\", \"Серия и номер ДУЛ\": \"" + format(doc.getDocumentNumber()) + "\" ]");
    }

    public void dulUpdated(IdDoc doc) {
        changes.add("[ Изменён ДУЛ. \"Код ДУЛ\": \"" + format(doc.getDocType().getCode()) + "\", \"Серия и номер ДУЛ\": \"" + format(doc.getDocumentNumber()) + "\" ]");
    }

    public void originalSet(RegistryPersonDTO original) {
        changes.add("[ Назначен ФЛ-оригинал. Идентификатор ФЛ: \"" + original.getOldId().toString() +
                "\", ФИО: \"" + original.getLastName() + " " + original.getFirstName() + (isNotEmpty(original.getMiddleName()) ? " " + original.getMiddleName() : "") + "\" ]");
    }

    public void originalDeleted(RegistryPerson original) {
        changes.add("[ Откреплён ФЛ-оригинал. Идентификатор ФЛ: \"" + original.getOldId().toString() +
                "\", ФИО: \"" + original.getLastName() + " " + original.getFirstName() + (isNotEmpty(original.getMiddleName()) ? " " + original.getMiddleName() : "") + "\" ]");
    }

    public void duplicatesSet(List<RegistryPerson> duplicates) {
        for (RegistryPerson duplicate : duplicates) {
            changes.add("[ Прикреплен Дубликат ФЛ. Идентификатор ФЛ: \"" + duplicate.getOldId().toString() +
                    "\", ФИО: \"" + duplicate.getLastName() + " " + duplicate.getFirstName() + (isNotEmpty(duplicate.getMiddleName()) ? " " + duplicate.getMiddleName() : "") + "\" ]");
        }
    }

    public void duplicatesDeleted(List<RegistryPerson> duplicates) {
        for (RegistryPerson duplicate : duplicates) {
            changes.add("[ Откреплён Дубликат ФЛ. Идентификатор ФЛ: \"" + duplicate.getOldId().toString() +
                    "\", ФИО: \"" + duplicate.getLastName() + " " + duplicate.getFirstName() + (isNotEmpty(duplicate.getMiddleName()) ? " " + duplicate.getMiddleName() : "") + "\" ]");
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
            changes.add("[ " + "Статус налогоплательщика" + ": " + format(personWas.getTaxPayerState()) + " -> " + format(personBecome.getTaxPayerState()) + " ]");
        }
        if (!equal(emptyObjectToNull(personWas.getCitizenship()), emptyObjectToNull(personBecome.getCitizenship()))) {
            changes.add("[ " + "Гражданство" + ": " + format(personWas.getCitizenship()) + " -> " + format(personBecome.getCitizenship()) + " ]");
        }
        if (!equal(emptyObjectToNull(personWas.getReportDoc()), emptyObjectToNull(personBecome.getReportDoc()))) {
            changes.add("[ " + "Документ для отчетности" + ": " + format(personWas.getReportDoc()) + " -> " + format(personBecome.getReportDoc()) + " ]");
        }
        if (!equal(emptyObjectToNull(personWas.getSource()), emptyObjectToNull(personBecome.getSource()))) {
            changes.add("[ " + "Система-источник" + ": " + format(personWas.getSource()) + " -> " + format(personBecome.getSource()) + " ]");
        }
        if (!equal(personWas.isVip(), personBecome.isVip())) {
            changes.add("[ " + "Важность" + ": " + formatVip(personWas.isVip()) + " -> " + formatVip(personBecome.isVip()) + " ]");
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
            changes.add("[ " + "Код страны проживания" + ": " + format(personWas.getAddress().getCountry()) + " -> " + format(personBecome.getAddress().getCountry()) + " ]");
        }
        personPropertyUpdated("Адрес за пределами РФ", personWas.getAddress().getAddressIno(), personBecome.getAddress().getAddressIno());
    }

    private void personPropertyUpdated(String name, String string1, String string2) {
        if (!equal(string1, string2)) {
            changes.add("[ " + name + ": " + format(string1) + " -> " + format(string2) + " ]");
        }
    }

    private void personPropertyUpdated(String name, Date date1, Date date2) {
        if (!equal(date1, date2)) {
            changes.add("[ " + name + ": " + format(date1) + " -> " + format(date2) + " ]");
        }
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
