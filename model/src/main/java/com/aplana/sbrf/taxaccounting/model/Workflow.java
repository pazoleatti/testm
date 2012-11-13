package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.util.List;

/**
 * Жизненный цикл системы
 */
public class Workflow implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String name;
	private List<WorkflowMove> moves;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<WorkflowMove> getMoves() {
		return moves;
	}
	public void setMoves(List<WorkflowMove> moves) {
		this.moves = moves;
	}
}
