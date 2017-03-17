package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.DocType;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Andrey Drunk
 */
public class IdentificationUtils {

    public static String getVal(Map<String, RefBookValue> refBookPersonRecord, String attrName) {
        RefBookValue refBookValue = refBookPersonRecord.get(attrName);
        if (refBookValue != null) {
            return refBookValue.toString();
        } else {
            return null;
        }
    }

    public static String buildRefBookNotice(Map<String, RefBookValue> refBookPersonRecord) {

        StringBuilder sb = new StringBuilder();

        sb.append("Номер '").append(getVal(refBookPersonRecord, "RECORD_ID")).append("': ");
        sb.append(getVal(refBookPersonRecord, "LAST_NAME")).append(" ");
        sb.append(getVal(refBookPersonRecord, "FIRST_NAME")).append(" ");
        sb.append(getVal(refBookPersonRecord, "MIDDLE_NAME")).append(" ");
        sb.append(" [id=").append(getVal(refBookPersonRecord, RefBook.RECORD_ID_ALIAS)).append("]");

        return sb.toString();

    }


    /**
     * Формирует строку в виде
     * <Номер в форме(ИНП)>: <Фамилия> <Имя> <Отчество>, <Название ДУЛ> № <Серия и номер ДУЛ>
     *
     * @return
     */
    public static String buildRefBookNotice(NaturalPerson personData) {
        return buildNotice(personData);
    }

    /**
     * Формирует строку с информацией о физлице по данным первичной формы
     * Для 1151111 указывается номер физлица, для РНУ-НДФЛ ИНП
     * <Номер в форме(ИНП)>: <Фамилия> <Имя> <Отчество>, <Название ДУЛ> № <Серия и номер ДУЛ>
     *
     * @return
     */
    public static String buildNotice(NaturalPerson naturalPerson) {

        StringBuilder sb = new StringBuilder();

        if (naturalPerson.getNum() != null) {
            sb.append("Номер '").append(naturalPerson.getNum()).append("': ");
        } else if (naturalPerson.getPersonIdentifier() != null && naturalPerson.getPersonIdentifier().getInp() != null) {
            sb.append("ИНП '").append(naturalPerson.getPersonIdentifier().getInp()).append("': ");
        } else if (naturalPerson.getSnils() != null) {
            sb.append("СНИЛС '").append(naturalPerson.getSnils()).append("': ");
        } else if (naturalPerson.getInn() != null) {
            sb.append("ИНН ФЛ '").append(naturalPerson.getSnils()).append("': ");
        } else if (naturalPerson.getInnForeign() != null) {
            sb.append("ИНН ИНО '").append(naturalPerson.getInnForeign()).append("': ");
        }

        sb.append(emptyIfNull(naturalPerson.getLastName())).append(" ");
        sb.append(emptyIfNull(naturalPerson.getFirstName())).append(" ");
        sb.append(emptyIfNull(naturalPerson.getMiddleName())).append(", ");

        PersonDocument personDocument = naturalPerson.getPersonDocument();

        if (personDocument != null) {

            DocType docType = personDocument.getDocType();

            if (docType != null && docType.getName() != null) {
                sb.append(docType.getName()).append(": ");
            } else if (docType != null && docType.getCode() != null) {
                sb.append("Код ДУЛ ").append(docType.getCode()).append(": ");
            }
            sb.append(personDocument.getDocumentNumber());
        }


        if (naturalPerson.getPrimaryPersonId() != null || naturalPerson.getId() != null) {

            sb.append(" [");
            if (naturalPerson.getPrimaryPersonId() != null) {
                sb.append(" ").append(naturalPerson.getPrimaryPersonId());
            }
            if (naturalPerson.getId() != null) {
                sb.append(", ").append(naturalPerson.getId());
            }
            sb.append("]");
        }

        return sb.toString();
    }


    private static String emptyIfNull(String string) {
        return StringUtils.defaultString(string);
    }

}
