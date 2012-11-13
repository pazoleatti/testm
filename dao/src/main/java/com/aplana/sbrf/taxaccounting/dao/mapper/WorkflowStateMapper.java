package com.aplana.sbrf.taxaccounting.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.aplana.sbrf.taxaccounting.model.WorkflowState;

public interface WorkflowStateMapper {
	@Select("select * from wf_state where id = #{stateId}")
	WorkflowState getState(@Param("stateId") int stateId);
}
