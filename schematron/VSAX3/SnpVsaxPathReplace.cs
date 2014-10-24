namespace VSAX3
{
    using System;
    using System.Linq;
    using System.Text.RegularExpressions;

    internal static class SnpVsaxPathReplace
    {
        public static string PathConcat(string path, string leftPath, string rightPath)
        {
            string str = path.Split(new char[] { '/' }, StringSplitOptions.RemoveEmptyEntries).Reverse<string>().FirstOrDefault<string>();
            Regex regex = new Regex("/" + str + "[[0-9]+]");
            return regex.Replace(rightPath, leftPath, 1);
        }
    }
}

