package com.aplana.sbrf.taxaccounting.async.manager;

import com.aplana.sbrf.taxaccounting.async.balancing.BalancingVariants;
import com.aplana.sbrf.taxaccounting.async.entity.AsyncTaskType;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskPersistenceException;
import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskSerializationException;
import com.aplana.sbrf.taxaccounting.async.task.AsyncTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
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
    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void executeAsync(long taskTypeId, Map<String, Object> params, BalancingVariants balancingVariant) throws AsyncTaskException {
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

            for (Map.Entry<String, Object> param : params.entrySet()) {
                //Все параметры должны быть сериализуемы
                if (!Serializable.class.isAssignableFrom(param.getValue().getClass())) {
                    throw new AsyncTaskSerializationException("Все параметры должны поддерживать сериализацию!");
                }
            }
            AsyncTask task = applicationContext.getBean(asyncTaskType.getHandlerJndi(), AsyncTask.class);
            task.execute(params);
        } catch (EmptyResultDataAccessException e) {
            throw new AsyncTaskPersistenceException("Не найден тип задачи с идентификатором = " + taskTypeId);
        }
    }

    private JdbcTemplate getJdbcTemplate() {
        return (JdbcTemplate)namedParameterJdbcTemplate.getJdbcOperations();
    }
}
