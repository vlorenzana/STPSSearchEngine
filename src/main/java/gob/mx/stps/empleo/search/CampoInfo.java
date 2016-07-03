/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

/**
 *
 * @author victor.lorenzana
 */
public class CampoInfo
{

    public CampoInfo(final Integer index, final String value)
    {
        this.index = index;
        this.value = value;
    }
    private final transient Integer index;
    private final transient String value;

    public Integer getIndex()
    {
        return index;
    }

    public String getValue()
    {
        return value;
    }
}
