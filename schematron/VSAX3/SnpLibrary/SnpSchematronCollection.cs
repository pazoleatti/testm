namespace VSAX3.SnpLibrary
{
    using System;
    using System.Collections;
    using System.Collections.Generic;
    using System.Linq;
    using System.Reflection;

    internal class SnpSchematronCollection : ICollection<SnpSchematronObject>, IEnumerable<SnpSchematronObject>, IEnumerable
    {
        private readonly List<SnpSchematronObject> _innerCol = new List<SnpSchematronObject>();
        private const bool IsRo = false;

        public void Add(SnpSchematronObject item)
        {
            if (!this.Contains(item))
            {
                this._innerCol.Add(item);
            }
        }

        public void Clear()
        {
            this._innerCol.Clear();
        }

        public bool Contains(SnpSchematronObject item)
        {
            SnpSchematronObjectSameDimensions comp = new SnpSchematronObjectSameDimensions();
            return this._innerCol.Any<SnpSchematronObject>(o => comp.Equals(o, item));
        }

        public int ContainXPath(string path)
        {
            int num = -1;
            foreach (SnpSchematronObject obj2 in this._innerCol)
            {
                num++;
                if (obj2.XPath == path)
                {
                    return num;
                }
            }
            return -1;
        }

        public void CopyTo(SnpSchematronObject[] array, int arrayIndex)
        {
            throw new NotImplementedException();
        }

        public IEnumerator<SnpSchematronObject> GetEnumerator()
        {
            return new SnpSchematronObjectEnumerator(this);
        }

        public bool Remove(SnpSchematronObject item)
        {
            for (int i = 0; i < this._innerCol.Count; i++)
            {
                SnpSchematronObject x = this._innerCol[i];
                if (new SnpSchematronObjectSameDimensions().Equals(x, item))
                {
                    this._innerCol.RemoveAt(i);
                    return true;
                }
            }
            return false;
        }

        IEnumerator IEnumerable.GetEnumerator()
        {
            return new SnpSchematronObjectEnumerator(this);
        }

        public int Count
        {
            get
            {
                return this._innerCol.Count;
            }
        }

        public bool IsReadOnly
        {
            get
            {
                return false;
            }
        }

        public SnpSchematronObject this[int index]
        {
            get
            {
                return this._innerCol[index];
            }
            set
            {
                this._innerCol[index] = value;
            }
        }
    }
}

