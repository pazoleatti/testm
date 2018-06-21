package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.io.IOException;

/**
 * Сервис создания приложения 2
 */
public interface CreateApplication2Service {

    /**
     * Создает задачу на создание Приложения 2 для Налога на прибыль
     *
     * @param reportYear отчетный год
     * @param userInfo   информация о пользователе
     * @return uuid уведомлений
     */
    String createApplication2Task(int reportYear, TAUserInfo userInfo);

    /**
     * Создает задачу на создание Приложения 2 для Налога на прибыль
     *
     * @param reportYear    отчетный год
     * @param userInfo      информация о пользователе
     * @param logger        логгер
     * @return uuid уведомлений
     */
    String performCreateApplication2(int reportYear, TAUserInfo userInfo, Logger logger) throws IOException;
}
