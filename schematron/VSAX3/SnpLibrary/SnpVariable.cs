namespace VSAX3.SnpLibrary
{
    using System;
    using System.Runtime.InteropServices;

    internal sealed class SnpVariable : SnpSchematronObject
    {
        public SnpVariable(string name, string xpath, string value = null)
        {
            this.Name = name;
            this.XPath = xpath;
            this.Value = value;
            this.IsConstValue = false;
        }
    }
}

