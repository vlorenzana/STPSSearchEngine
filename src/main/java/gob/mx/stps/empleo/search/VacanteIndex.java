/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableMetaData;
import org.compass.annotations.SearchableProperty;

/**
 * Clase Índice de una vacante
 *
 * @author victor.lorenzana
 */
@Searchable(alias = "vacante")
public final class VacanteIndex
{

    private static final Log LOG = LogFactory.getLog(VacanteIndex.class);

    /**
     * Constructor por defecto
     */
    public VacanteIndex()
    {
    }

    /**
     * Constructor con id de vacante
     *
     * @param idvacante Id de la vacante
     */
    public VacanteIndex(final long idvacante)
    {
        this.id = idvacante;
    }

    /**
     * Constructor con vacante
     *
     * @param vacante Vacante a indexar
     */
    public VacanteIndex(final Vacante vacante)
    {
        this.fuente = vacante.getFuente();
        this.id = vacante.id;
        this.edad_de = vacante.edad_de;
        this.edad_hasta = vacante.edad_hasta;
        this.discapacidad = vacante.getDiscapacidad();
        this.carrera.clear();
        this.conocimientos.clear();
        this.icarrera.clear();
        this.setTitulo(vacante.titulo);

        this.empresa = vacante.empresa;
        this.ubicacion = vacante.ubicacion;
        this.fecha = vacante.fecha;
        this.titulo_vacante = vacante.titulo_vacante;
        this.salario = vacante.salario;

        this.estado = vacante.estado;
        this.municipio = vacante.municipio;
        setCarreras(vacante);
        setConocimientos(vacante);

        this.ocupacion = CompassInfo.OCUPACIONES.get(vacante.ocupacion);
        if (this.ocupacion != null)
        {
            this.ocupacion = this.ocupacion.replace('-', ' ');
        }
        this.iocupacion = vacante.ocupacion;
        if (this.titulo != null)
        {
            this.titulo = this.titulo.replace('-', ' ');
        }

    }

    private void setConocimientos(final Vacante vacante)
    {
        if (vacante.conocimientos != null)
        {
            final StringBuilder content = new StringBuilder();
            for (String conocimiento : vacante.conocimientos)
            {
                if (conocimiento != null)
                {
                    conocimientos.add(conocimiento.replace('-', ' '));
                    content.append(conocimiento);
                    content.append(" ");
                }
            }
        }
    }

    private void setCarreras(final Vacante vacante)
    {
        if (vacante.carreras != null && vacante.carreras.size() > 0)
        {
            for (Integer _icarrera : vacante.carreras)
            {
                try
                {
                    String _carrera = CompassInfo.CARRERAS.get(_icarrera);
                    _carrera = _carrera.replace('-', ' ');
                    carrera.add(_carrera);
                    icarrera.add(_icarrera);

                } catch (Exception e)
                {
                    LOG.error("No se pudo generar indice de carreras vacante " + this.id + " carrera:" + _icarrera, e);
                }

            }

        }
    }

    /**
     * Regresa una vacante
     *
     * @return Regresa una vacante
     */
    public Vacante get()
    {
        final Vacante vacante = new Vacante();
        vacante.id = id;
        vacante.setEdad_de(edad_de);
        vacante.setEdad_hasta(edad_hasta);
        vacante.setFuente(fuente);
        vacante.estado = estado;
        vacante.setDiscapacidad(discapacidad);
        vacante.municipio = municipio;
        vacante.ocupacion = iocupacion;
        vacante.carreras.addAll(icarrera);
        vacante.conocimientos.addAll(conocimientos);
        vacante.titulo = this.titulo;
        vacante.titulo_vacante = titulo_vacante;
        vacante.ubicacion = ubicacion;
        vacante.empresa = empresa;
        vacante.fecha = fecha;
        vacante.salario = salario;
        return vacante;
    }

    public @SearchableProperty(index = Index.TOKENIZED, name = "DISCAPACIDAD")
    @SearchableMetaData(index = Index.TOKENIZED, name = "DISCAPACIDAD")
    String discapacidad;

    public @SearchableId
    @SearchableMetaData(name = "id")
    /**
     * Id de la vacante
     */
    transient long id = -1;
    public @SearchableProperty(index = Index.TOKENIZED, name = "OCUPACIONWORDS")
    @SearchableMetaData(index = Index.TOKENIZED, name = "OCUPACIONWORDS")
    /**
     * Ocupación
     */
    transient String ocupacion;
    public @SearchableProperty(index = Index.NOT_ANALYZED, name = "OCUPACION")
    @SearchableMetaData(index = Index.NOT_ANALYZED, name = "OCUPACION")
    /**
     * Ocupación
     */
    transient int iocupacion;
    public @SearchableProperty(index = Index.NOT_ANALYZED, name = "ESTADO")
    @SearchableMetaData(index = Index.NOT_ANALYZED, name = "ESTADO")
    /**
     * Estado
     */
    transient int estado;
    public @SearchableProperty(index = Index.NOT_ANALYZED, name = "MUNICIPIO")
    @SearchableMetaData(index = Index.NOT_ANALYZED, name = "MUNICIPIO")
    /**
     * Estado
     */
    transient int municipio;

    public @SearchableProperty(index = Index.NOT_ANALYZED, name = "FUENTE")
    @SearchableMetaData(index = Index.NOT_ANALYZED, name = "FUENTE")
    /**
     * Estado
     */
    transient int fuente;

    public @SearchableProperty(index = Index.TOKENIZED, name = "CARRERAWORDS")
    @SearchableMetaData(index = Index.TOKENIZED, name = "CARRERAWORDS")
    /**
     * Lista de carreras
     */
    transient List<String> carrera = new ArrayList<String>();
    public @SearchableProperty(index = Index.NOT_ANALYZED, name = "CARRERA")
    @SearchableMetaData(index = Index.NOT_ANALYZED, name = "CARRERA")
    /**
     * Lista de carreras
     */
    transient List<Integer> icarrera = new ArrayList<Integer>();
    public @SearchableProperty(index = Index.TOKENIZED, name = "CONOCIMIENTO")
    @SearchableMetaData(index = Index.TOKENIZED, name = "CONOCIMIENTO")
    /**
     * Lista de carreras
     */
    transient List<String> conocimientos = new ArrayList<String>();
    public @SearchableProperty(index = Index.TOKENIZED, name = "TITULO")
    @SearchableMetaData(index = Index.TOKENIZED, name = "TITULO")
    /**
     * Palabra complementarias a indexar, considerado para habilidades y
     * conocimientos
     */
    String titulo;

    @SearchableProperty(index = Index.UN_TOKENIZED, name = "TITULO_VACANTE")
    @SearchableMetaData(index = Index.UN_TOKENIZED, name = "TITULO_VACANTE")
    String titulo_vacante;

    @SearchableProperty(index = Index.UN_TOKENIZED, name = "UBICACION")
    @SearchableMetaData(index = Index.UN_TOKENIZED, name = "UBICACION")
    String ubicacion;

    @SearchableProperty(index = Index.UN_TOKENIZED, name = "EMPRESA")
    @SearchableMetaData(index = Index.UN_TOKENIZED, name = "EMPRESA")
    String empresa;

    @SearchableProperty(index = Index.UN_TOKENIZED, name = "FECHA")
    @SearchableMetaData(index = Index.UN_TOKENIZED, name = "FECHA")
    long fecha;

    @SearchableProperty(index = Index.UN_TOKENIZED, name = "SALARIO")
    @SearchableMetaData(index = Index.UN_TOKENIZED, name = "SALARIO")
    int salario;

    public @SearchableProperty(format = "#0000", index = Index.NOT_ANALYZED, name = "EDAD_DE")
    @SearchableMetaData(format = "#0000", index = Index.NOT_ANALYZED, name = "EDAD_DE")

    int edad_de = -1;

    public @SearchableProperty(format = "#0000", index = Index.NOT_ANALYZED, name = "EDAD_HASTA")
    @SearchableMetaData(format = "#0000", index = Index.NOT_ANALYZED, name = "EDAD_HASTA")

    /**
     * Edad del candidato
     */
    int edad_hasta = -1;

    public void setEdad_de(int edad_de)
    {
        this.edad_de = edad_de;
    }

    public void setEdad_hasta(int edad_hasta)
    {
        this.edad_hasta = edad_hasta;
    }

    public int getEdad_de()
    {
        return edad_de;
    }

    public int getEdad_hasta()
    {
        return edad_hasta;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final VacanteIndex other = (VacanteIndex) obj;
        if (this.id != other.id)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    /**
     * Regresa el id de la vacante
     *
     * @return Id de la vacante
     */
    @Override
    public String toString()
    {
        return String.valueOf(id);
    }

    public void setTitulo(final String titulo)
    {
        this.titulo = titulo;
    }

    public String getTitulo()
    {
        return titulo;
    }

    public String getEmpresa()
    {
        return empresa;
    }

    public long getFecha()
    {
        return fecha;
    }

    public String getTituloVacante()
    {
        return titulo_vacante;
    }

    public String getUbicacion()
    {
        return ubicacion;
    }

    public int getSalario()
    {
        return salario;
    }
}
