package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;

/**
 * Класс для передачи данных из ДАО в сервис
 *
 * @author Andrey Drunk
 */
public class PersonData extends NaturalPerson {

    /**
     * Номер физлица, для 115 это поле номер для РНУ это инп, используется при формировании сообщения в логах
     */
    private String personNumber;

    /**
     * Идентификаторы налогоплательщика. Уникальный неизменяемый цифровой идентификатор налогоплательщика
     */
    private String inp;

    /**
     * Идентификаторы налогоплательщика. Ссылка на справочник {@link com.aplana.sbrf.taxaccounting.model.refbook.RefBook.Id#ASNU}
     */
    private Long asnuId;

    /**
     * Ссылка на справочник виды документов
     */
    private Long documentTypeId;

    /**
     * Код типа документа
     */
    private String documentTypeCode;

    /**
     * Номер документа
     */
    private String documentNumber;

    /**
     *
     */
    private String citizenship;

    /**
     *
     */
    private String status;

    /**
     * Признак что в результат запроса надо включить адрес
     */
    private boolean useAddress = true;

    public boolean isUseAddress() {
        return useAddress;
    }

    public void setUseAddress(boolean useAddress) {
        this.useAddress = useAddress;
    }

    public String getInp() {
        return inp;
    }

    public void setInp(String inp) {
        this.inp = inp;
    }

    public Long getAsnuId() {
        return asnuId;
    }

    public void setAsnuId(Long asnuId) {
        this.asnuId = asnuId;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }


    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(String documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }

}
