/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search.test;

import java.util.Comparator;

/**
 *
 * @author victor.lorenzana
 */
public class OcupacionInfo implements Comparator<OcupacionInfo>
{

    transient public Long id;
    transient public Long count;

    @Override
    public int compare(final OcupacionInfo ocupacion1, final OcupacionInfo ocupacion2)
    {
        return ocupacion2.count.compareTo(ocupacion1.count);
    }
}
