namespace VSAX3
{
    using System;
    using System.Runtime.CompilerServices;
    using System.Runtime.InteropServices;

    public class ErrorsStruct
    {
        public ErrorsStruct(string xpath, string message, string value = "", string code = "", string docnaim = "", string docznach = "", string criticalcode = "")
        {
            this.XPath = xpath;
            this.ErrorCode = code;
            this.Value = value;
            this.IdDocNaim = docnaim;
            this.IdDocZnach = docznach;
            this.ErrorText = message;
            this.CriticalCode = criticalcode;
        }

        public string CriticalCode { get; private set; }

        public string ErrorCode { get; private set; }

        public string ErrorText { get; private set; }

        public string IdDocNaim { get; private set; }

        public string IdDocZnach { get; private set; }

        public string Value { get; private set; }

        public string XPath { get; private set; }
    }
}

