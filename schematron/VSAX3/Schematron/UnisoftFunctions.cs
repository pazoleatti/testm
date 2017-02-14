namespace VSAX3.Schematron
{
    using System;
    using System.IO;

    internal class UnisoftFunctions
    {
        private readonly string _fileName;
        private string _lasterror = "";

        public UnisoftFunctions(string srcFileName)
        {
            this._fileName = srcFileName;
        }

        public bool checkDateCurrent(string thisdate)
        {
            this.setCheckError("");
            if (DateTime.Parse(thisdate).CompareTo(DateTime.Today) > 0)
            {
                return false;
            }
            return true;
        }

        public bool checkDocument(string value, string pattern)
        {
            this.setCheckError("");
            return true;
        }

        public bool checkINN(string str)
        {
            switch (str.Length)
            {
                case 10:
                    return this.checkINNUL(str);

                case 12:
                    return this.checkINNFL(str);
            }
            this.setCheckError("ИНН имеет неправильную длину.");
            return false;
        }

        public bool checkINNFL(string str)
        {
            this.setCheckError("");
            if (str.Length == 12)
            {
                int num = Convert.ToInt32(str.Substring(0, 1)) * 7;
                int num2 = Convert.ToInt32(str.Substring(1, 1)) * 2;
                int num3 = Convert.ToInt32(str.Substring(2, 1)) * 4;
                int num4 = Convert.ToInt32(str.Substring(3, 1)) * 10;
                int num5 = Convert.ToInt32(str.Substring(4, 1)) * 3;
                int num6 = Convert.ToInt32(str.Substring(5, 1)) * 5;
                int num7 = Convert.ToInt32(str.Substring(6, 1)) * 9;
                int num8 = Convert.ToInt32(str.Substring(7, 1)) * 4;
                int num9 = Convert.ToInt32(str.Substring(8, 1)) * 6;
                int num10 = Convert.ToInt32(str.Substring(9, 1)) * 8;
                decimal num11 = ((((((((num + num2) + num3) + num4) + num5) + num6) + num7) + num8) + num9) + num10;
                num = Convert.ToInt32(str.Substring(0, 1)) * 3;
                num2 = Convert.ToInt32(str.Substring(1, 1)) * 7;
                num3 = Convert.ToInt32(str.Substring(2, 1)) * 2;
                num4 = Convert.ToInt32(str.Substring(3, 1)) * 4;
                num5 = Convert.ToInt32(str.Substring(4, 1)) * 10;
                num6 = Convert.ToInt32(str.Substring(5, 1)) * 3;
                num7 = Convert.ToInt32(str.Substring(6, 1)) * 5;
                num8 = Convert.ToInt32(str.Substring(7, 1)) * 9;
                num9 = Convert.ToInt32(str.Substring(8, 1)) * 4;
                num10 = Convert.ToInt32(str.Substring(9, 1)) * 6;
                int num12 = Convert.ToInt32(str.Substring(10, 1)) * 8;
                decimal num13 = (((((((((num + num2) + num3) + num4) + num5) + num6) + num7) + num8) + num9) + num10) + num12;
                decimal num14 = Math.Floor((decimal) (num11 / 11M)) * 11M;
                decimal num15 = Math.Floor((decimal) (num13 / 11M)) * 11M;
                decimal num16 = num11 - num14;
                decimal num17 = num13 - num15;
                if (num16 >= 10M)
                {
                    num16 -= 10M;
                }
                if (num17 >= 10M)
                {
                    num17 -= 10M;
                }
                decimal num18 = Convert.ToDecimal(str.Substring(10, 1));
                decimal num19 = Convert.ToDecimal(str.Substring(11, 1));
                string str2 = "";
                if (num18 != num16)
                {
                    str2 = string.Format("Предпоследняя контрольная цифра ИНН не совпадает. Должна быть: {0}.", num16);
                }
                if (num19 != num17)
                {
                    str2 = str2 + string.Format("Последняя контрольная цифра ИНН не совпадает. Должна быть: {0}.", num17);
                }
                if ((num18 == num16) && (num19 == num17))
                {
                    return true;
                }
                this.setCheckError(str2);
                return false;
            }
            this.setCheckError("ИНН имеет неправильную длину.");
            return false;
        }

        public bool checkINNUL(string str)
        {
            this.setCheckError("");
            if (str.Length == 10)
            {
                int num = Convert.ToInt32(str.Substring(0, 1)) * 2;
                int num2 = Convert.ToInt32(str.Substring(1, 1)) * 4;
                int num3 = Convert.ToInt32(str.Substring(2, 1)) * 10;
                int num4 = Convert.ToInt32(str.Substring(3, 1)) * 3;
                int num5 = Convert.ToInt32(str.Substring(4, 1)) * 5;
                int num6 = Convert.ToInt32(str.Substring(5, 1)) * 9;
                int num7 = Convert.ToInt32(str.Substring(6, 1)) * 4;
                int num8 = Convert.ToInt32(str.Substring(7, 1)) * 6;
                int num9 = Convert.ToInt32(str.Substring(8, 1)) * 8;
                decimal num10 = (((((((num + num2) + num3) + num4) + num5) + num6) + num7) + num8) + num9;
                decimal num11 = Math.Floor((decimal) (num10 / 11M)) * 11M;
                decimal num12 = num10 - num11;
                if (num12 >= 10M)
                {
                    num12 -= 10M;
                }
                decimal num13 = Convert.ToDecimal(str.Substring(9, 1));
                string str2 = string.Format("Контрольная цифра не совпадает. Должна быть: {0}.", num10 - num11);
                if (num12 == num13)
                {
                    return true;
                }
                this.setCheckError(str2);
                return false;
            }
            this.setCheckError("ИНН имеет неправильную длину.");
            return false;
        }

        public bool checkKladr(string sin)
        {
            this.setCheckError("");
            return true;
        }

        public bool checkNSI(string catalog, string column, string value)
        {
            this.setCheckError("");
            return true;
        }

        public string getCheckError()
        {
            return this._lasterror;
        }

        public string getDateCurrent()
        {
            this.setCheckError("");
            return DateTime.Today.ToString("dd.MM.yyyy");
        }

        public string getFileName()
        {
            this.setCheckError("");
            return Path.GetFileNameWithoutExtension(this._fileName);
        }

        public string getNSIValue(string catalog, string incolumn, string outcolumn, string value)
        {
            this.setCheckError("");
            return "";
        }

        public bool iif(bool condition, bool truePath, bool falsePath)
        {
            this.setCheckError("");
            return (condition ? truePath : falsePath);
        }

        private void setCheckError(string str)
        {
            this._lasterror = str;
        }
    }
}

