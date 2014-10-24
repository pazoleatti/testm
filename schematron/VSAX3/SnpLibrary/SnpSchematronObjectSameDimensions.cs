namespace VSAX3.SnpLibrary
{
    using System;
    using System.Collections.Generic;

    internal class SnpSchematronObjectSameDimensions : EqualityComparer<SnpSchematronObject>
    {
        public override bool Equals(SnpSchematronObject x, SnpSchematronObject y)
        {
            return (((x.Name == y.Name) && (x.XPath == y.XPath)) && (x.Value == y.Value));
        }

        public override int GetHashCode(SnpSchematronObject obj)
        {
            if (obj == null)
            {
                throw new NullReferenceException();
            }
            int num2 = (obj.Name.GetHashCode() ^ obj.XPath.GetHashCode()) ^ obj.Value.GetHashCode();
            return num2.GetHashCode();
        }
    }
}

