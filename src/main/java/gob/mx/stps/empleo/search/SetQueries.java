/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.compass.core.CompassQuery;

/**
 *
 * @author victor.lorenzana
 */
public class SetQueries extends HashSet<CompassQuery>
{

    private transient final Set<String> queries = new HashSet<String>();

    @Override
    public boolean add(final CompassQuery queryToAdd)
    {
        CompassQuery query=queryToAdd;
        final String strQuery = query.toString();
        if (!queries.contains(strQuery))
        {
            if (strQuery.indexOf("TITULO:")!=-1)
            {
                query = query.setBoost(400);
            }
            queries.add(strQuery);
            super.add(query);
        }
        return true;
    }
    
    public boolean containsQuery(final CompassQuery query)
    {
        final String strQuery=query.toString();
        return queries.contains(strQuery);
    }
    

    

    @Override
    public boolean addAll(final Collection<? extends CompassQuery> collection)
    {
        for(CompassQuery q : collection)
        {
            add(q);            
        }
        return true;
    }
    public CompassQuery get(final int index)
    {
        return this.get(index);
    }


    

    
}
