package com.aplana.sbrf.taxaccounting.service.component.operation;

public interface TransportFileAsyncTaskDescriptor {

    /**
     * Создать описание асинхронной задачи для транспортного файла
     * @param fileName имя файла
     * @return  строку описания
     */
    String createDescription(String fileName);
}
