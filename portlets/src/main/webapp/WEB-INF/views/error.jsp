<%@page import="java.io.*"%>
<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects/>
<% Exception exception = (Exception) request.getAttribute("exception"); %>
<h1>Произошла ошибка: <%= exception.getMessage()%></h1>
<portlet:renderURL var="backUrl" portletMode="view" windowState="normal"></portlet:renderURL>
<p>Для возврата к исходной странице перейдите по <a href="${backUrl}">ссылке</a></p>
<h2>Стек ошибки:</h2>
<%  StringWriter stringWriter = new StringWriter();
	PrintWriter printWriter = new PrintWriter(stringWriter); 
	exception.printStackTrace(printWriter);
	printWriter.close();
	String errorStack = stringWriter.getBuffer().toString();
%>
<div style="overflow: auto; height: 500px; width: 100%"><pre>
<c:out value="<%= errorStack %>"/>
</pre></div>