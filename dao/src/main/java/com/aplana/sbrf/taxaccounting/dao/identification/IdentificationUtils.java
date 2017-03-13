package com.aplana.sbrf.taxaccounting.dao.identification;

import com.aplana.sbrf.taxaccounting.model.identification.DocType;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.identification.PersonDocument;

/**
 * @author Andrey Drunk
 */
public class IdentificationUtils {


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

        StringBuffer sb = new StringBuffer();

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
        if (string != null) {
            return string;
        } else {
            return "";
        }
    }

}
