package com.aplana.sbrf.taxaccounting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Запрос с параметрами
 */
@Getter
@Setter
@AllArgsConstructor
public class NamedParameterSql {
    private String sql;
    private MapSqlParameterSource params;
}
