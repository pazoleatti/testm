package com.aplana.sbrf.taxaccounting.dao.impl.components;

import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Билдер запросов для обнвления записи реесра ФЛ в зависимости от измененнных полей
 */
@Component
public class RegistryPersonUpdateQueryBuilder {
    /**
     * Билдер запроса для обновления данных ФЛ
     * @param changedFields список изменнных полей
     * @return  Построенный запрос
     */
    public String buildPersonUpdateQuery(List<RegistryPerson.UpdatableField> changedFields) {
        StringBuilder result = new StringBuilder("UPDATE ref_book_person set");
        StringBuilder fieldsBuilder = new StringBuilder();

        for (RegistryPerson.UpdatableField changedField : changedFields) {
            switch (changedField) {
                case VERSION:
                    fieldsBuilder.append("version = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case LAST_NAME:
                    fieldsBuilder.append("last_name = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case FIRST_NAME:
                    fieldsBuilder.append("first_name = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case MIDDLE_NAME:
                    fieldsBuilder.append("middle_name = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case BIRTH_DATE:
                    fieldsBuilder.append("birth_date = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case CITIZENSHIP:
                    fieldsBuilder.append("citizenship = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case REPORT_DOC:
                    fieldsBuilder.append("report_doc = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case INN:
                    fieldsBuilder.append("inn = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case INN_FOREIGN:
                    fieldsBuilder.append("inn_foreign = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case SNILS:
                    fieldsBuilder.append("snils = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case TAX_PAYER_STATE:
                    fieldsBuilder.append("taxpayer_state = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case SOURCE:
                    fieldsBuilder.append("source_id = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case VIP:
                    fieldsBuilder.append("vip = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
            }
        }
        if (fieldsBuilder.toString().isEmpty()) {
            return null;
        }
        fieldsBuilder.delete(fieldsBuilder.length() - 2, fieldsBuilder.length());
        result.append(" ")
                .append(fieldsBuilder)
                .append(" WHERE id = :id");
        return result.toString();
    }

    /**
     * Билдер запроса для обновления данных адреса ФЛ
     * @param changedFields список изменнных полей
     * @return  Построенный запрос
     */
    public String buildAddressUpdateQuery(List<RegistryPerson.UpdatableField> changedFields) {
        StringBuilder result = new StringBuilder("UPDATE ref_book_address set");
        StringBuilder fieldsBuilder = new StringBuilder();

        for (RegistryPerson.UpdatableField changedField : changedFields) {
            switch (changedField) {
                case REGION_CODE:
                    fieldsBuilder.append("region_code = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case POSTAL_CODE:
                    fieldsBuilder.append("postal_code = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case DISTRICT:
                    fieldsBuilder.append("district = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case CITY:
                    fieldsBuilder.append("city = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case LOCALITY:
                    fieldsBuilder.append("locality = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case STREET:
                    fieldsBuilder.append("street = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case HOUSE:
                    fieldsBuilder.append("house = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case BUILD:
                    fieldsBuilder.append("build = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case APPARTMENT:
                    fieldsBuilder.append("appartment = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case COUNTRY_ID:
                    fieldsBuilder.append("country_id = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
                case ADDRESS:
                    fieldsBuilder.append("address = :")
                            .append(changedField.getAlias())
                            .append(", ");
                    break;
            }
        }
        if (fieldsBuilder.toString().isEmpty()) {
            return null;
        }
        fieldsBuilder.delete(fieldsBuilder.length() - 2, fieldsBuilder.length());
        result.append(" ")
                .append(fieldsBuilder)
                .append(" WHERE id = :id");
        return result.toString();
    }


}
