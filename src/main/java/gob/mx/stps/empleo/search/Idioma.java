/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

/**
 * Cla que describe un idioma y su dominio
 * @author victor.lorenzana
 */
public final class Idioma
{

    /**
     * Constructor con id de idioma
     * @param idIdioma Id de idioma de acuerdo al catalogo
     */
    public Idioma(final int idIdioma)
    {
        if (idIdioma <= 0)
        {
            throw new IllegalArgumentException("El identificador de idioma no puede ser cero o negativo");
        }
        this.id = idIdioma;
    }

    /**
     * Constructor con id de idioma y id de dominio
     * @param idIdioma Id de idioma de acuerdo al catalogo
     * @param dominio Id de dominio de acuerdo al catalogo de dominios
     */
    public Idioma(final int idIdioma, final int dominio)
    {
        if (idIdioma <= 0)
        {
            throw new IllegalArgumentException("El identificador de idioma no puede ser cero o negativo");
        }
        if (dominio <= 0)
        {
            throw new IllegalArgumentException("El identificador del dominio no puede ser cero o negativo");
        }
        this.id = idIdioma;
        this.dominio_id = dominio;
    }
    /**
     * Id de idioma de acuerdo al catalogo
     */
    public transient int id = -1;
    /**
     * Id de dominio de acuerdo al catalogo de dominios
     */
    public transient int dominio_id = -1;
}
