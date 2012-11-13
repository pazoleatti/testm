package com.aplana.sbrf.taxaccounting.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.aplana.sbrf.taxaccounting.model.Workflow;
import com.aplana.sbrf.taxaccounting.model.WorkflowMove;

public interface WorkflowMapper {
	@Select("select * from workflow where id = #{workflowId}")
	Workflow getWorkflow(@Param("workflowId") int workflowId);
	
	@Select("select * from wf_move where workflow_id = #{workflowId}")
	@Results({
		@Result(property="id"),
		@Result(property="name"),
		@Result(property="fromStateId", column="from_state_id"),
		@Result(property="toStateId", column="to_state_id")
	})
	List<WorkflowMove> getMoves(@Param("workflowId") int workflowId);
}
