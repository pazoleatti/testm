<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<h1>Здесь будет выводиться список данных по налоговой форме</h1>
<portlet:actionURL var="createNewURL" portletMode="edit">
	<portlet:param name="action" value="new"/>
</portlet:actionURL>
<script type="text/javascript">
	function ${namespace}_createNew() {
		window.location.href = '${createNewURL}';
	}
</script>

<button dojoType="dijit.form.Button" onClick="${namespace}_createNew()">Добавить</button>