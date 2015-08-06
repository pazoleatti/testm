package com.aplana.sbrf.taxaccounting.model;

/**
 * Enum для идентификации наименования в json-объекте загрузки файлов
 */
public enum  UuidEnum {
    /**
     * новый uuid в таблице blob_data
     */
    UUID {
        @Override
        public String toString() {
            return "uuid";
        }
    },
    /**
     * uuid ошибок логгера
     */
    ERROR_UUID{
        @Override
        public String toString() {
            return "errorUuid";
        }
    },
    /**
     * uuid записи логгера в случае успешной обработки
     */
    SUCCESS_UUID,
    UPLOADED_FILE
}
