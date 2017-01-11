package com.aplana.sbrf.taxaccounting.common.service;

/**
 * Сервис предоставляющий доступ к API сервиса установки использования подразделения в модуле гарантий
 * Конкретная реализация представляет из себя ejb, которая получается через lookup по jndi-имени.
 * Это исключает необходимость указания модуля core как зависимости, а также позволяет использовать разные реализации
 * Пример lookup в контексте Spring. Можно также использовать Remote интерфейс
 * <jee:local-slsb id="commonService" jndi-name="ejblocal:{имя приложения, развернутое на сервере приложений}/department-ejb.jar/DepartmentUsageServiceBean#com.aplana.sbrf.taxaccounting.common.department.DepartmentUsageServiceLocal" business-interface="com.aplana.sbrf.taxaccounting.common.department.DepartmentUsageService" lookup-home-on-startup="true"/>
 * Далее этот сервис можно использовать как спринговый бин. Для dev-мода, эту строку надо убрать, но при этом в контексте Spring должен существовать какой то бин-заглушка этого интерфейса
 * @see EventAuditService
 * @author aivanov
 */
public interface DepartmentUsageService {
    /**
     * Установка флага использования подразделения в модуле гарантий
     *
     * @param id идентификатор подразделения
     * @param used значение флага, true - используется, false - не используется
     * @throws CommonServiceException выбрасывается в случае, если выполнить операцию невозможно:
     *         подразделение заблокировано для изменений, откат транзакции, подразделение удалено.
     *         Исключение несет в себе информацию о причине невозможности выполнения действия.
     */
    void setDepartmentUsedByGarant(long id, boolean used) throws CommonServiceException;

}
