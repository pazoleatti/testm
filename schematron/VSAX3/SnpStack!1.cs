namespace VSAX3
{
    using System;
    using System.Collections.Generic;
    using System.Linq;

    internal class SnpStack<T> : Stack<T>
    {
        public override string ToString()
        {
            return ("/" + string.Join<T>("/", this.Reverse<T>()));
        }
    }
}

