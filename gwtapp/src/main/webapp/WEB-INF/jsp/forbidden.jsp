<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>403 &mdash; АС "Учёт налогов"</title>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="icon" href="resources/img/favicon.ico" type="image/vnd.microsoft.icon" />
    <link rel="shortcut icon" href="resources/img/favicon.ico" type="image/vnd.microsoft.icon" />
    <link type="text/css" rel="stylesheet" href="resources/css/Main.css">
</head>
<body>
    <h1>Ошибка 403 &mdash; Доступ запрещён!</h1>
    <p><a href="/resources/help_un.pdf">Руководство_пользователя "Учет Налогов"</a></p>
    <p><a href="/resources/help_uks.pdf">Руководство_пользователя "Учет КС"</a></p>
    <p>Обратитесь к администратору.</p>
    <!-- Завершение сеанса пользователя СУДИР -->
    <p><a href="${pageContext.servletContext.contextPath}/pkmslogout">Завершить сеанс</a></p>
</body>
</html>