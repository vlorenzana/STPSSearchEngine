/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

/**
 *
 * @author victor.lorenzana
 */
public class IndexInfo
{
    public IndexInfo(final int pos, final int len)
        {
            this.pos = pos;
            this.len = len;
        }

        public IndexInfo()
        {
        }
        public transient int pos = -1;
        public transient int len = 0;
}
