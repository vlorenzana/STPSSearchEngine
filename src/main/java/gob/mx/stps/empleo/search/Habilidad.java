/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.Locale;

/**
 * Clase que describe una habilidad y su nivel de experiencia
 * @author victor.lorenzana
 */
public final class Habilidad
{
    private static final Locale LOCALE = new Locale("es", "MX");
    public Habilidad(final String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("El nombre de la habilidad no puede ser negativo");
        }
        this.name = name.toLowerCase(LOCALE);

    }

    /**
     * Constructor con descripción y experiencia
     * @param name Nombre de la habilidad
     * @param experiencia Id de experiencia de acuerdo al catalogo de experiencias
     */
    public Habilidad(final String name,final int experiencia)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("El nombre de la habilidad no puede ser negativo");
        }
        if (experiencia <= 0)
        {
            throw new IllegalArgumentException("El identificador de experiencia no puede ser cero o negativo");
        }
        this.name = name.toLowerCase(LOCALE);
        this.name = this.name.replace('_', ' ');
        this.experiencia = experiencia;
    }
    /**
     * Nombre de la habilidad
     */
    public String name;
    /*
     * Id de experiencia de acuerdo al catalogo de experiencias
     */
    public int experiencia = -1;

    public int getExperiencia()
    {
        return experiencia;
    }

    public void setExperiencia(final int experiencia)
    {
        this.experiencia = experiencia;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
