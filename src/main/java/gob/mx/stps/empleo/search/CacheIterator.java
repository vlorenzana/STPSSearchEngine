/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author victor.lorenzana
 */
public class CacheIterator implements Iterator
{
    private Map map;
    int pos=0;
    public CacheIterator(Map map)
    {
        this.map=map;
    }
    
    @Override
    public boolean hasNext()
    {
        if(pos>=map.size())
        {
            return false;
        }
        return true;
    }

    @Override
    public Object next()
    {
        if(pos>=map.size())
        {
            return null;
        }
        else
        {
            return map.get(pos++);
        }
    }

    @Override
    public void remove()
    {
        
    }
    
}
