namespace VSAX3.SnpLibrary
{
    using System;

    internal interface ISnpObject
    {
        bool IsConstValue { get; }

        string Name { get; }

        string Value { get; }

        string XPath { get; }
    }
}

