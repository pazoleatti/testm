<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<h1>Здесь будет выводиться список налоговых форм</h1>
<c:forEach items="${forms}" var="form">
	<c:out value="${form.id}"/> - <c:out value="${form.type.name}"/><br/>
</c:forEach>