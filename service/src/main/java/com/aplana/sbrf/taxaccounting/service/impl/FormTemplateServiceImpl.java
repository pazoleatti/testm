package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.dao.ObjectLockDao;
import com.aplana.sbrf.taxaccounting.log.Logger;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;

/**
 * Реализация сервиса для работы с шаблонами налоговых форм
 * TODO: сейчас это просто тонкая обёртка над FormTemplateDao, нужно дополнить её 
 * 	- вызовом механизма блокировок
 *  - проверкой прав доступа пользователя
 *  - возможно, валидацией сохраняемых данных
 * @author dsultanbekov
 */
@Service
@Transactional
public class FormTemplateServiceImpl implements FormTemplateService {
	private static final int FORM_VERSION_MAX_VALUE = 20;
	private static final int FORM_STYLE_ALIAS_MAX_VALUE = 20;
	private static final int FORM_COLUMN_NAME_MAX_VALUE = 1000;
	private static final int FORM_COLUMN_ALIAS_MAX_VALUE = 100;
	private static final int FORM_COLUMN_GROUP_NAME_MAX_VALUE = 1000;
	private static final int FORM_COLUMN_DICTIONARY_CODE_MAX_VALUE = 30;
	//TODO: надо подумать как хендлить длину строковой ячейки и нужно ли это тут
	//private static final int FORM_COLUMN_CHK_MAX_VALUE = 500;
	private static final int FORM_SCRIPT_NAME_MAX_VALUE = 255;
	private static final int DATA_ROW_ALIAS_MAX_VALUE = 20;

	private Set<String> checkSet = new HashSet<String>();

	@Autowired
	private FormTemplateDao formTemplateDao;

	@Autowired
	private ObjectLockDao lockDao;

	@Override
	public List<FormTemplate> listAll() {
		return formTemplateDao.listAll();
	}

	@Override
	public FormTemplate get(int formTemplateId) {
		return formTemplateDao.get(formTemplateId);
	}

	@Override
	public int save(FormTemplate formTemplate) {
		return formTemplateDao.save(formTemplate);
	}

	@Override
	public int getActiveFormTemplateId(int formTypeId) {
		return formTemplateDao.getActiveFormTemplateId(formTypeId);
	}

	@Override
	public void checkLockedByAnotherUser(Integer formTemplateId, int userId){
		if (formTemplateId!=null){
			ObjectLock<Integer> objectLock = lockDao.getObjectLock(formTemplateId, FormTemplate.class);
			if(objectLock != null && objectLock.getUserId() != userId){
				throw new AccessDeniedException("Шаблон формы заблокирован другим пользователем");
			}
		}
	}

	@Override
	public boolean lock(int formTemplateId, int userId){
		ObjectLock<Integer> objectLock = lockDao.getObjectLock(formTemplateId, FormTemplate.class);
		if(objectLock != null && objectLock.getUserId() != userId){
			return false;
		} else {
			lockDao.lockObject(formTemplateId, FormTemplate.class ,userId);
			return true;
		}
	}

	@Override
	public boolean unlock(int formTemplateId, int userId){
		ObjectLock<Integer> objectLock = lockDao.getObjectLock(formTemplateId, FormTemplate.class);
		if(objectLock != null && objectLock.getUserId() != userId){
			return false;
		} else {
			lockDao.unlockObject(formTemplateId, FormTemplate.class, userId);
			return true;
		}
	}

	@Override
	public void validateFormTemplate(FormTemplate formTemplate, Logger logger) {
		//TODO: подумать над обработкой уникальности версии, на данный момент версия не меняется

		if (formTemplate.getVersion().getBytes().length > FORM_VERSION_MAX_VALUE) {
			logger.error("значение для версии шаблона формы слишком велико (фактическое: " +
					formTemplate.getVersion().getBytes().length + ", максимальное: "+ FORM_VERSION_MAX_VALUE + ")");
		}

		validateFormColumns(formTemplate.getColumns(), logger);
		validateFormStyles(formTemplate.getStyles(), logger);
		validateFormScripts(formTemplate.getScripts(), logger);
		validateFormRows(formTemplate.getRows(), logger);
	}

	private void validateFormColumns(List<Column> columns, Logger logger){
		checkSet.clear();

		for (Column column : columns) {
			if (!checkSet.add(column.getAlias())) {
				logger.error("найден повторяющийся алиас \" " + column.getAlias() +
						"\" для столбца " + column.getName());
			}

			if (column.getName() != null && column.getName().getBytes().length > FORM_COLUMN_NAME_MAX_VALUE) {
				logger.error("значение для имени столбца \"" + column.getName() +
						"\" слишком велико (фактическое: " + column.getName().getBytes().length +
						", максимальное: " + FORM_COLUMN_NAME_MAX_VALUE + ")");
			}
			if (column.getAlias() != null && column.getAlias().getBytes().length > FORM_COLUMN_ALIAS_MAX_VALUE) {
				logger.error("значение для алиаса столбца \"" + column.getAlias() +
						"\" слишком велико (фактическое: " + column.getAlias().getBytes().length
						+ ", максимальное: " + FORM_COLUMN_ALIAS_MAX_VALUE + ")");
			}
			if (column.getGroupName() != null && column.getGroupName().getBytes().length > FORM_COLUMN_GROUP_NAME_MAX_VALUE) {
				logger.error("значение для имени группы столбца \"" + column.getGroupName() +
						"\" слишком велико (фактическое: " + column.getGroupName().getBytes().length +
						", максимальное: " + FORM_COLUMN_GROUP_NAME_MAX_VALUE + ")");
			}
			if (column instanceof StringColumn && ((StringColumn)column).getDictionaryCode() != null &&
					((StringColumn)column).getDictionaryCode().getBytes().length > FORM_COLUMN_DICTIONARY_CODE_MAX_VALUE) {
				logger.error("значение для кода справочника \"" + ((StringColumn)column).getDictionaryCode() +
						"\" слишком велико (фактическое: " + ((StringColumn)column).getDictionaryCode().getBytes().length +
						", максимальное: " + FORM_COLUMN_DICTIONARY_CODE_MAX_VALUE + ")");
			}
			if (column instanceof NumericColumn && ((NumericColumn)column).getDictionaryCode() != null &&
					((NumericColumn)column).getDictionaryCode().getBytes().length > FORM_COLUMN_DICTIONARY_CODE_MAX_VALUE) {
				logger.error("значение для кода справочника \"" + ((NumericColumn)column).getDictionaryCode() +
						"\" солишком велико (фактическое: " + ((NumericColumn)column).getDictionaryCode().getBytes().length +
						", максимальное: " + FORM_COLUMN_DICTIONARY_CODE_MAX_VALUE + ")");
			}
		}
	}

	private void validateFormRows(List<DataRow> rows, Logger logger) {
		//TODO: подумать о уникальности порядка строк
		for (DataRow row : rows) {
			if (row.getAlias() != null && row.getAlias().getBytes().length > DATA_ROW_ALIAS_MAX_VALUE) {
				logger.error("значение для кода строки \"" + row.getAlias() +
						"\" слишком велико (фактическое: " + row.getAlias().getBytes().length +
						", максимальное: " + DATA_ROW_ALIAS_MAX_VALUE + ")");
			}
		}
	}

	private void validateFormScripts(List<Script> scrips, Logger logger) {
		for (Script script : scrips) {
			if (script.getName() != null && script.getName().getBytes().length > FORM_SCRIPT_NAME_MAX_VALUE) {
				logger.error("значение для имени скрипта \"" + script.getName() +
						"\" слишком велико (фактическое: " + script.getName().getBytes().length +
						", максимальное: " + FORM_SCRIPT_NAME_MAX_VALUE + ")");
			}
		}
	}

	private void validateFormStyles(List<FormStyle> styles, Logger logger) {
		checkSet.clear();

		for (FormStyle style : styles) {
			if (!checkSet.add(style.getAlias())) {
				logger.error("найден повторяющийся алиас стиля " + style.getAlias());
			}

			if (style.getAlias() != null && style.getAlias().getBytes().length > FORM_STYLE_ALIAS_MAX_VALUE) {
				logger.error("значение для алиаса стиля \"" + style.getAlias() +
						"\" слишком велико (фактическое: " + style.getAlias().getBytes().length +
						", максимальное: " + FORM_STYLE_ALIAS_MAX_VALUE + ")");
			}
		}
	}
}
