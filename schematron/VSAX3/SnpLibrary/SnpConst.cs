namespace VSAX3.SnpLibrary
{
    using System;
    using System.Runtime.InteropServices;

    internal sealed class SnpConst : SnpSchematronObject
    {
        public SnpConst(string name, string xpath, string value = null)
        {
            this.Name = name;
            this.XPath = xpath;
            this.Value = value;
            this.IsConstValue = true;
        }

        public override void ReplaceValue(string value)
        {
            if ((this.Value == null) && (value != null))
            {
                this.Value = value;
            }
        }
    }
}

