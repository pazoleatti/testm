package com.aplana.sbrf.taxaccounting.permissions.logging;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

/**
 * Объединяет в себе несколько проверок {@link com.aplana.sbrf.taxaccounting.permissions.logging.units.CheckUnit}
 * и обеспечивает порядок их следования
 */
public interface LoggingPermissionChecker {

    /**
     * Запускает цепочку проверок {@link com.aplana.sbrf.taxaccounting.permissions.logging.units.CheckUnit} для
     * налоговой формы. Если очередная проверка не прошла, то остальные проверки прекращают выполняться.
     * @param logger            логгер
     * @param userInfo          информация о пользователе
     * @param declarationData   объект налоговой формы
     * @return Если все проверки пройдены возвращает <code>true</code>. Если не пройдена хотя бы одна проверка -
     * прекращается выполнения метода и возвращается <code>false</code>
     */
    boolean check(Logger logger, TAUserInfo userInfo, DeclarationData declarationData);

}
