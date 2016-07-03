/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gob.mx.stps.empleo.search;


/**
 * Clase excepción para busquedas
 * @author victor.lorenzana
 */
public final class BusquedaException extends Exception
{
            
    /**
     * Constructor de busqueda con mensaje
     * @param message
     */
    public BusquedaException(final String message)
    {
        super(message);
    }
    /**
     * Constructor con mensaje y excepción
     * @param message Mensaje a desplegar
     * @param throwable Error que lo origina
     */
    public BusquedaException(final String message,final Throwable throwable)
    {
        super(message, throwable);
    }
    /**
     * Constructor con error
     * @param throwable Error que lo origina
     */
    public BusquedaException(final Throwable throwable)
    {
        super(throwable);
    }
}
