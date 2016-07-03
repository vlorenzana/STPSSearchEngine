/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author victor.lorenzana
 */
public class ResultInfo
{
    public ResultInfo(final long hey)
    {
        this.id=hey;
    }
    public transient long id;
    public final List<String> carreras=new ArrayList<String>();
    public String ocupacion;
    public final List<String> conocimientos=new ArrayList<String>();
    public String titulo;
}
