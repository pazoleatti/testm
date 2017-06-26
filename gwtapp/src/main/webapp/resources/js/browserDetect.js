/**
 * Created with IntelliJ IDEA.
 * User: vpetrov
 * Date: 07.11.13
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */
function browserDetectNav(chrAfterPoint)
{
    var
        UA=window.navigator.userAgent,       // содержит переданный браузером юзерагент
    //--------------------------------------------------------------------------------
        OperaB = /Opera[ \/]+\w+\.\w+/i,     //
        OperaV = /Version[ \/]+\w+\.\w+/i,   //
        FirefoxB = /Firefox\/\w+\.\w+/i,     // шаблоны для распарсивания юзерагента
        ChromeB = /Chrome\/\w+\.\w+/i,       //
        SafariB = /Version\/\w+\.\w+/i,      //
        IEB = /MSIE *\d+\.\w+/i,             //
        SafariV = /Safari\/\w+\.\w+/i,       //
    //--------------------------------------------------------------------------------
        browser = new Array(),               //массив с данными о браузере
        browserSplit = /[ \/\.]/i,           //шаблон для разбивки данных о браузере из строки
        OperaV = UA.match(OperaV),
        Firefox = UA.match(FirefoxB),
        Chrome = UA.match(ChromeB),
        Safari = UA.match(SafariB),
        SafariV = UA.match(SafariV),
        IE = UA.match(IEB),
        Opera = UA.match(OperaB);
//----- Opera ----
    if ((!Opera=="")&(!OperaV==""))
        browser[0]=OperaV[0].replace(/Version/, "Opera")
    else
    if (!Opera=="")
        browser[0]=Opera[0]
    else
//----- IE -----
    if (!IE=="")
        browser[0] = IE[0]
    else
//----- Firefox ----
    if (!Firefox=="")
        browser[0]=Firefox[0]
    else
//----- Chrom ----
    if (!Chrome=="")
        browser[0] = Chrome[0]
    else
//----- Safari ----
    if ((!Safari=="")&&(!SafariV==""))
        browser[0] = Safari[0].replace("Version", "Safari");
//------------ Разбивка версии -----------
    var outputData;
    // [0] - имя браузера,
    // [1] - целая часть версии
    // [2] - дробная часть версии
    if (browser[0] != null) outputData = browser[0].split(browserSplit);
    if ((chrAfterPoint==null)&&(outputData != null))
    {
        chrAfterPoint=outputData[2].length;
        outputData[2] = outputData[2].substring(0, chrAfterPoint); // берем нужное ко-во знаков
        outputData[3] = UA;
        return(outputData);
    }
    else
        return(false);
}