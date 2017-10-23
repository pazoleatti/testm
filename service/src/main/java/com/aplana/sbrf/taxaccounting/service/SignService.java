package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Set;

/**
 * Сервис для проверки ЭП
 * чтобы его использовать необходимо добавить в PATH путь до библиотек:
 * "grn/grn64", "bicr4/bicr4_64", "bicr_adm/bicr_adm_64".
 */
public interface SignService {

	/** Если установлено это значение, то проверку ЭП выполнять надо для всех ТФ */
	String SIGN_CHECK = "1";

	/**
	 *
	 * Проверить подписанный файл
	 * @param pathToSignFile путь к файлу для проверки
     * @param delFlag если равен 1, то в случае, если подпись есть (контрольная сумма совпала), она будет удалена из файла
     * если равен 0, подпись не удаляется. При N равным 2 и более будут удалены все подписи от 1 до N.
     * После удаления подписи и последующих вызовах функции для проверки одного и того же файла N необходимо снова устанавливать равным 1
     * @return статус проверки:
     *          первое значение - успешность выполненной проверки;
     *          второе - при успешном завершение - userId, иначе список кодов ошибок.
	 * */
    Pair<Boolean, Set<String>> checkSign(String fileName, String pathToSignFile, int delFlag, Logger logger);
}