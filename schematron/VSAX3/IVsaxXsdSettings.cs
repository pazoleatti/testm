namespace VSAX3
{
    using System;
    using System.Collections.Specialized;
    using System.IO;
    using VSAX3.SnpLibrary;

    internal interface IVsaxXsdSettings
    {
        bool HasSchematronNamespace { get; }

        bool HasSnpNamespace { get; }

        bool HasUnisoftNamespace { get; }

        StringDictionary Paths { get; }

        Stream Schematron { get; }

        EVsaxValidateType Type { get; }

        SnpSchematronCollection XslVariables { get; }
    }
}

