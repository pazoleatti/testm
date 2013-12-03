<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<title>АС "Учёт налогов"</title>

	<link rel="icon" href="resources/img/favicon.ico" type="image/vnd.microsoft.icon" />
	<link rel="shortcut icon" href="resources/img/favicon.ico" type="image/vnd.microsoft.icon" />

    <link type="text/css" rel="stylesheet" href="resources/css/Main.css">

   
	<script type="text/javascript" src="Main/Main.nocache.js"></script>
    <script type="text/javascript" src="resources/js/browserDetect.js"></script>

    <script>
        window.onload = function(){
            var data = browserDetectNav();
            if (!(((data[0]=="MSIE")&&(data[1]>7))||(data[0]=="Firefox")||(data[0]=="Safari")||(data[0]=="Chrome")))
                document.write('<div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px;">Работа приложения невозможна. Обнаружен несовместимый браузер. Приложение поддерживает Internet Explorer версии 8 и старше. В том числе и в режиме совместимости версия браузера и документов должны быть установлены не ниже 8.</div>');
        }
    </script>

</head>
<body>
	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1'
		style="position: absolute; width: 0; height: 100%; min-height: 100%; border: 0"></iframe>
	<noscript>
		<div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px;">
            Для корректной работы приложения должен быть включен JavaScript.
			</div>
	</noscript>
</body>
</html>
