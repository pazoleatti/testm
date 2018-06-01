namespace VSAX3
{
    using System;
    using System.Collections.Generic;

    public interface IVsaxErrorHandler
    {
        List<ErrorsStruct> Errors { get; }

        bool HasSysError { get; }

        string SysErrorDescription { get; }
    }
}

