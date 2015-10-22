package com.aplana.sbrf.taxaccounting.async.manager;

import com.aplana.sbrf.taxaccounting.model.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.entity.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskPersistenceException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceLoggerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Заглушка для использования асинхронного апи в дев-моде. Получает класс-обработчик по имени бина. Может использовать только Spring-реализацию
 * @author dloshkarev
 */
@Service
public class AsyncManagerMock implements AsyncManager {

    // private static final Log LOG = LogFactory.getLog(AsyncManagerMock.class);

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public BalancingVariants checkCreate(long taskTypeId, Map<String, Object> params) throws AsyncTaskException {
        try {
            AsyncTaskType asyncTaskType = getJdbcTemplate().queryForObject("select id, name, handler_jndi from async_task_type where id = ?", new RowMapper<AsyncTaskType>() {
                @Override
                public AsyncTaskType mapRow(ResultSet rs, int rowNum) throws SQLException {
                    AsyncTaskType result = new AsyncTaskType();
                    result.setId(rs.getLong("id"));
                    result.setName(rs.getString("name"));
                    result.setHandlerJndi(rs.getString("handler_jndi"));
                    return result;
                }
            }, taskTypeId);

            if (asyncTaskType.getHandlerJndi().startsWith("ejb")) {
                throw new AsyncTaskException("Некорректный формат JNDI-имени для класса-исполнителя. В дев-моде это имя должно ссылаться на спринговый бин!");
            }

            AsyncTask task = applicationContext.getBean(asyncTaskType.getHandlerJndi(), AsyncTask.class);
            return task.checkTaskLimit(params);
        } catch (Exception e) {
            throw new AsyncTaskException(e);
        }
    }

    @Override
    public void executeAsync(long taskTypeId, Map<String, Object> params, BalancingVariants balancingVariant) throws AsyncTaskException, ServiceLoggerException {
        try {
            AsyncTaskType asyncTaskType = getJdbcTemplate().queryForObject("select id, name, handler_jndi from async_task_type where id = ?", new RowMapper<AsyncTaskType>() {
                @Override
                public AsyncTaskType mapRow(ResultSet rs, int rowNum) throws SQLException {
                    AsyncTaskType result = new AsyncTaskType();
                    result.setId(rs.getLong("id"));
                    result.setName(rs.getString("name"));
                    result.setHandlerJndi(rs.getString("handler_jndi"));
                    return result;
                }
            }, taskTypeId);

            if (asyncTaskType.getHandlerJndi().startsWith("ejb")) {
                throw new AsyncTaskException("Некорректный формат JNDI-имени для класса-исполнителя. В дев-моде это имя должно ссылаться на спринговый бин!");
            }

            checkParams(params);
            AsyncTask task = applicationContext.getBean(asyncTaskType.getHandlerJndi(), AsyncTask.class);
            task.execute(params);
        } catch (EmptyResultDataAccessException e) {
            throw new AsyncTaskPersistenceException("Не найден тип задачи с идентификатором = " + taskTypeId);
        } catch (ServiceLoggerException e) {
            throw e;
        } catch (Exception e) {
            throw new AsyncTaskException(e);
        }
    }

    /**
     * Проверяем обязательные параметры. Они должны быть заполнены и содержать значение правильного типа
     * @param params параметры
     */
    private void checkParams(Map<String, Object> params) throws AsyncTaskSerializationException {
        for (AsyncTask.RequiredParams key : AsyncTask.RequiredParams.values()) {
            if (!params.containsKey(key.name())) {
                throw new IllegalArgumentException("Не указан обязательный параметр \"" + key.name() + "\"!");
            }
            if (!key.getClazz().isInstance(params.get(key.name()))) {
                throw new IllegalArgumentException("Обязательный параметр \"" + key.name() + "\" имеет неправильный тип " + params.get(key.name()).getClass().getName() + "! Должен быть: " + key.getClazz().getName());
            }
        }

        for (Map.Entry<String, Object> param : params.entrySet()) {
            //Все параметры должны быть сериализуемы
            if (!Serializable.class.isAssignableFrom(param.getValue().getClass())) {
                throw new AsyncTaskSerializationException("Параметр \"" + param.getKey() + "\" не поддерживает сериализацию!");
            }
        }
    }

    private JdbcTemplate getJdbcTemplate() {
        return (JdbcTemplate)namedParameterJdbcTemplate.getJdbcOperations();
    }
}
