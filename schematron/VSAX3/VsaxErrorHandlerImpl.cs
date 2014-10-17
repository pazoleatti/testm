namespace VSAX3
{
    using System;
    using System.Collections.Generic;
    using System.Runtime.CompilerServices;

    internal sealed class VsaxErrorHandlerImpl : IVsaxErrors, IVsaxErrorHandler, IVsaxErrorPusher, IVsaxErrorCleaning
    {
        private readonly List<ErrorsStruct> _errors = new List<ErrorsStruct>();

        public void Clear()
        {
            this._errors.Clear();
            this.HasSysError = false;
            this.SysErrorDescription = string.Empty;
        }

        public void PushSystemError(string err)
        {
            this.HasSysError = true;
            this.SysErrorDescription = err;
        }

        public void PushValidatingError(ErrorsStruct err)
        {
            this._errors.Add(err);
        }

        public List<ErrorsStruct> Errors
        {
            get
            {
                return this._errors;
            }
        }

        public bool HasSysError { get; private set; }

        public string SysErrorDescription { get; private set; }
    }
}

