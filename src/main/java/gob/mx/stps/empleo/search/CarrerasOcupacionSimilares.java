/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.HashSet;

/**
 *
 * @author victor.lorenzana
 */
public class CarrerasOcupacionSimilares extends HashSet<Integer>
{
        
    
    private transient Integer indice;
    public CarrerasOcupacionSimilares()
    {
        super();
    }
    public CarrerasOcupacionSimilares(final Integer indice)
    {
        super();
        this.indice=indice;        
    }
    public Integer getIndiceCarreraOcupacion()
    {
        return indice;
    }
    public void setIndiceCarreraOcupacion(final Integer indice)
    {
        this.indice=indice;
    }
    
}
