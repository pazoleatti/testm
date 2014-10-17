namespace VSAX3.SnpLibrary
{
    using System;
    using System.Collections;
    using System.Collections.Generic;

    internal class SnpSchematronObjectEnumerator : IEnumerator<SnpSchematronObject>, IDisposable, IEnumerator
    {
        private readonly SnpSchematronCollection _collection;
        private SnpSchematronObject _curBox;
        private int _curIndex;

        public SnpSchematronObjectEnumerator(SnpSchematronCollection collection)
        {
            this._collection = collection;
            this._curIndex = -1;
            this._curBox = null;
        }

        public void Dispose()
        {
        }

        public bool MoveNext()
        {
            if (++this._curIndex >= this._collection.Count)
            {
                return false;
            }
            this._curBox = this._collection[this._curIndex];
            return true;
        }

        public void Reset()
        {
            this._curIndex = -1;
        }

        public SnpSchematronObject Current
        {
            get
            {
                return this._curBox;
            }
        }

        object IEnumerator.Current
        {
            get
            {
                return this.Current;
            }
        }
    }
}

