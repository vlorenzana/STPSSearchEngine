/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

/**
 * Clase que describe la información académica
 * @author victor.lorenzana
 */
public final class InformacionAcademica
{
    private static final String MSG_CARRERA = "La carrera no puede ser negativa";

    /**
     * Constructor por defecto
     */
    public InformacionAcademica()
    {
    }

    /**
     * Constructor con carrera, grado de estudios y estatus
     * @param carrera Id de carrera de acuerdo al catalogo
     * @param grado_estudios Id del grado de estudios en el catalogo
     * @param status Id del estatus de estudio, de acuerdo al catalogo
     */
    public InformacionAcademica(final int carrera, final int grado_estudios, final int status)
    {
        if (carrera <= 0)
        {
            throw new IllegalArgumentException(MSG_CARRERA);
        }
        if (grado_estudios <= 0)
        {
            throw new IllegalArgumentException("El grado de estudios no puede ser negativo");
        }
        if (status <= 0)
        {
            throw new IllegalArgumentException("El estatus academico no puede ser negativo");
        }
        this.carrera = carrera;
        this.grado_estudios = grado_estudios;
        this.status = status;
    }

    /**
     * Constructor compleo, con carrera y grado de estudios
     * @param carrera Id de carrera de acuerdo al catalogo
     * @param grado_estudios Id del estatus de estudio, de acuerdo al catalogo
     */
    public InformacionAcademica(final int carrera, final int grado_estudios)
    {
        if (carrera <= 0)
        {
            throw new IllegalArgumentException(MSG_CARRERA);
        }
        if (grado_estudios <= 0)
        {
            throw new IllegalArgumentException("El grado de estudios no puede ser negativo");
        }

        this.carrera = carrera;
        this.grado_estudios = grado_estudios;
    }

    /**
     * Constructor compleo con carrera
     * @param carrera Id de carrera de acuerdo al catalogo
     */
    public InformacionAcademica(final int carrera)
    {
        if (carrera <= 0)
        {
            throw new IllegalArgumentException(MSG_CARRERA);
        }

        this.carrera = carrera;
    }

    /**
     * Modifica una carrera
     * @param carrera Id de la carrera
     */
    public void setCarrera(final int carrera)
    {
        if (carrera <= 0)
        {
            throw new IllegalArgumentException(MSG_CARRERA);
        }

        this.carrera = carrera;
    }

    /**
     * Modifica el grado de estudios
     * @param grado_estudios Id del grado de estudios
     */
    public void setGradoEstudios(final int grado_estudios)
    {
        if (grado_estudios <= 0)
        {
            throw new IllegalArgumentException("El grado de estudios no puede ser negativo");
        }

        this.grado_estudios = grado_estudios;
    }

    /***
     * Modifica el status academico
     * @param status Id del estatus academico
     */
    public void setStatus(final int status)
    {
        if (status <= 0)
        {
            throw new IllegalArgumentException("El estatus academico no puede ser negativo");
        }
        this.status = status;
    }
    /**
     * Id del grado de estudios en el catalogo
     */
    public transient int grado_estudios = -1;
    /**
     * Id de carrera de acuerdo al catalogo
     */
    public transient int carrera = -1;
    /**
     * Id del estatus de estudio, de acuerdo al catalogo
     */
    public transient int status = -1;
}
