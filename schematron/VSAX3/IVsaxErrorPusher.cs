namespace VSAX3
{
    using System;

    internal interface IVsaxErrorPusher
    {
        void PushSystemError(string err);
        void PushValidatingError(ErrorsStruct err);
    }
}

