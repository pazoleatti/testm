package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Map;

/**
 * Сервис для с асинхронными задачами
 */
public interface AsyncTaskManagerService {

    /**
     * Возвращает пару (lock, restartMsg)
     * если lock == true, то существует блокировка и нужно вызвать диалог перезапуска с текстом restartMsg
     * если lock == false, добавили пользователя в очередь ожидания, выходим из сценария
     * иначе, нет блокировки/удалили блокировку с таким ключом, продолжаем выполнение сценария
     * @param keyTask
     * @param taskName
     * @param userInfo
     * @param force
     * @param logger
     * @return
     */
    Pair<Boolean, String> restartTask(String keyTask, String taskName, TAUserInfo userInfo, boolean force, Logger logger);

    /**
     * Постановка задачи в очередь по постановке http://conf.aplana.com/pages/viewpage.action?pageId=19663772
     */
    void createTask(String keyTask, ReportType reportType, Map<String, Object> params, boolean cancelTask, TAUserInfo userInfo, Logger logger, AsyncTaskHandler action);

    /**
     * Создает задачу на принятии налоговой формы, перед созданием задачи выполняются необходимые проверки
     * @param userInfo
     * @param declarationDataId
     * @param force если true, то удаляем старую задачу(и оправляем оповещения подписавщимся пользователям), иначе, если задача уже запущена, вызываем диалог
     * @param cancelTask если true, то удаляем задачи, которые должны удаляться при запуске текущей, иначе, если есть такие задачи, вызываем диалог
     */
    AcceptDeclarationResult createAcceptDeclarationTask(TAUserInfo userInfo, final long declarationDataId, final boolean force, final boolean cancelTask);
}
