/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.Locale;

/**
 * Clase que representa un conocimiento
 * @author victor.lorenzana
 */
public final class Conocimiento
{
    private static final Locale LOCALE = new Locale("es", "MX");
    /**
     * Constructor con descripción del conocimiento y id de experiencia
     * @param name Descripción del conocimiento
     * @param experiencia Id de experiencia
     */
    public Conocimiento(final String name, final int experiencia)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("El nombre del la habilidad no puede ser nulo");
        }
        if (experiencia <= 0)
        {
            throw new IllegalArgumentException("El identificador de experiencia no puede ser cero o negativo");
        }
        this.name = name.toLowerCase(LOCALE);
        this.name = this.name.replace('_', ' ');
        this.experiencia = experiencia;
    }

    public Conocimiento(final String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("El nombre del la habilidad no puede ser negativo");
        }

        this.name = name;
        this.name = this.name.replace('_', ' ');
    }
    /**
     * Descripción de la experiencia
     */
    public String name;
    /**
     * Id de experiencia
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
