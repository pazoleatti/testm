<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:set var="namespace"><portlet:namespace /></c:set>

<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
	@import "<c:url value="/js/codemirror/lib/codemirror.css"/>";
	table.${namespace}_propTable tbody tr td .value {
		width: 100%;
	}
	.CodeMirror {
		border: 1px solid #eee;
	}
	.CodeMirror-scroll {
		height: 100%;
	}
</style>
<script src="<c:url value="/js/aplana_datagrid_utils.js"/>"></script>
<script src="<c:url value="/js/codemirror/lib/codemirror.js"/>"></script>
<script src="<c:url value="/js/codemirror/mode/groovy/groovy.js"/>"></script>

<portlet:resourceURL var="getFormUrl" id="getForm"/>
<portlet:actionURL var="saveFormUrl" name="saveForm"/>
<script type="text/javascript">
	dojo.require('dojo.parser');
	dojo.require('dojo.data.ItemFileWriteStore');
	dojo.require('dijit.form.Button');
	dojo.require('dijit.form.TextBox');
	dojo.require('dijit.form.CheckBox');
	dojo.require('dijit.form.Textarea');	
	dojo.require('dijit.form.Select');
	dojo.require('dijit.layout.BorderContainer');
	dojo.require('dijit.layout.TabContainer');
	dojo.require('dijit.layout.ContentPane');
	dojo.require('dojox.grid.DataGrid');

	var ${namespace}_form = null;
	var ${namespace}_formRows = null;
	
	var ${namespace}_columnsGrid = null;
	var ${namespace}_rowsStore = null;
	var ${namespace}_rowsGrid = null;
	var ${namespace}_createScriptEditor = null;
	var ${namespace}_calcScriptsGrid = null;
	var ${namespace}_calcScriptConditionEditor = null;
	var ${namespace}_calcScriptEditor = null;
	var ${namespace}_idCounter = 0;
	
	<%--
		Сгенерировать идентификатор
	--%>
	var ${namespace}_generateId = function() {
		return --${namespace}_idCounter;
	};
	
	<%--
		Сохранение формы
	--%>
	${namespace}_saveForm = function() {		
		var form = ${namespace}_form;
		${namespace}_columnsGrid.acceptChanges(); 
		${namespace}_columnsGrid.store.save();
		
		if (${namespace}_calcScriptsGrid != null) {
			${namespace}_calcScriptsGrid.acceptChanges();
			${namespace}_calcScriptsGrid.store.save();
		}
		${namespace}_rowsStore.save();
		if (${namespace}_createScriptEditor != null) {
			if (form.createScript == null) {
				form.createScript = {
					id: ${namespace}_generateId()
				};			
			}
			form.createScript.body = ${namespace}_createScriptEditor.getValue();
		}
		
		var form = dojo.byId('${namespace}_saveForm');
		form.form.value = dojo.toJson(${namespace}_form);
		form.rows.value = dojo.toJson(${namespace}_formRows);
		form.submit(); 
	};
	
	<%--
		Создание редактора кода
	--%>
	var ${namespace}_createCodeEditor = function(textAreaId, script) {
		var scriptBody;
		if (script != null && script.body != null) {
			scriptBody = script.body;
		} else {
			scriptBody = '';
		}

		var result = CodeMirror.fromTextArea(
			dojo.byId(textAreaId), 
			{
				lineNumbers: true,
				matchBrackets: true,
				mode: 'text/x-groovy'        		 
			}
     	);
		result.setValue(scriptBody);
		return result;
	};

	dojo.addOnLoad(function(){
		dojo.parser.parse();
		dojo.xhrGet({
			url: '${getFormUrl}',
			handleAs: 'json',
			sync: true,
			preventCache: true,
			load: function(data) {
				${namespace}_form = data;
				${namespace}_formRows = ${namespace}_form.rows;
				delete ${namespace}_form.rows;
			},
			error: function(error) {
				alert(error);
			}
		});

		<%--
			Создаётся store для строк, его нужно создать вместе с контролами для редактирования состава 
			столбцов, так как при модификации столбцов нужно изменять и значения атрибутов в строках формы
		--%>
		dojo.forEach(${namespace}_formRows, function(row, index) { row.id = ${namespace}_generateId();});
		${namespace}_rowsStore = new dojo.data.ItemFileWriteStore({data: { identifier: 'id', items: ${namespace}_formRows}});
		${namespace}_rowsStore._saveEverything = function(saveCompleteCallback, saveFailedCallback, newFileContentString) {
			${namespace}_formRows = dojo.fromJson(newFileContentString).items;
			<%-- Удаляем ключи, добавленные для корректной работы dojo store --%>
			dojo.forEach(${namespace}_formRows, function(row) { delete row.id; });
		};
		<%--
			Фетчим данные по предопределённым строкам формы. Если этого не сделать, то при вызове _saveEverything содержимое store
			будет пустым. Вообще фетч произойдёт, если открыть закладку со списком строк, проблема в том, что пользователь может
			не открывать эту закладку, из-за этого и необходим данный код 
		--%>
		${namespace}_rowsStore.fetch({ onComplete: function(items, request) { }});
	});
</script>
<form id="${namespace}_saveForm" action="${saveFormUrl}" method="post">
	<input type="hidden" name="form"/>
	<input type="hidden" name="rows"/>
</form>
<div dojoType="dijit.layout.BorderContainer" style="width: 1000px; height: 750px">
	<div dojoType="dijit.layout.ContentPane" region="top">
		Параметры формы
		<div style="float: right;">
			<portlet:renderURL portletMode="view" var="backUrl" />
			<button dojoType="dijit.form.Button" onClick="window.location.href = '${backUrl}'">Отмена</button>
			<button dojoType="dijit.form.Button" onClick="${namespace}_saveForm();">Сохранить</button>
		</div>
	</div>
	<div dojoType="dijit.layout.TabContainer" style="width: 1000px; height: 750px;" region="center">
		<%@ include file="editColumns.jspf" %>
		<%@ include file="editRows.jspf"%>
		<%@ include file="editCreateScript.jspf"%>
		<%@ include file="editCalcScripts.jspf"%>
	</div>	
</div>