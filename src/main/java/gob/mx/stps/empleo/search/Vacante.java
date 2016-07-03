/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Clase que representa una vacante
 * @author victor.lorenzana
 */
public final class Vacante
{

    //private static final Log LOG = LogFactory.getLog(Vacante.class);
    /**
     * Id de la vacante
     */
    public long id = -1;
    public int ocupacion = -1;
    /**
     * Lista de carreras que el candidato debe cumplir
     */
    public transient Set<Integer> carreras = new HashSet<Integer>();
    /**
     * Lista de conocimientos requeridos
     */
    public List<String> conocimientos = new ArrayList<String>();
    /**
     * Estado en la que se ubica la vacante
     */
    public int edad_de=-1;
    public int edad_hasta=-1;

    public void setEdad_de(int edad_de) {
        this.edad_de = edad_de;
    }

    public void setEdad_hasta(int edad_hasta) {
        this.edad_hasta = edad_hasta;
    }

    public int getEdad_de() {
        return edad_de;
    }

    public int getEdad_hasta() {
        return edad_hasta;
    }
    public int estado;

    public int getFuente() {
        return fuente;
    }

    public void setFuente(int fuente) {
        this.fuente = fuente;
    }
    public int fuente;
    public transient int municipio;
    public String titulo;
    private String discapacidad;
    
    public String titulo_vacante;
    public String ubicacion;
    public long fecha;
    public int salario;
    public String empresa;
    /**
     * Constructor por defecto
     */
    public Vacante()
    {
    }

    /**
     * Constructor con id de vacante
     * @param idvacante Id de la vacante
     */
    public Vacante(final long idvacante)
    {
        if (idvacante <= 0)
        {
            throw new IllegalArgumentException("El identificador de vacante no puede ser cero o negativo");
        }
        this.id = idvacante;
    }

    public void setEstado(final int estado)
    {
        this.estado = estado;
    }

    public int getEstado()
    {
        return this.estado;
    }

    public void setMunicpio(final int municipio)
    {
        this.municipio = municipio;
    }

    public int getMunicipio()
    {
        return this.municipio;
    }

    /**
     * Constructor
     * @param idvacante Id de la vacante
     * @param status_academica Id del status academico, de acuerdo al catalogo
     * @param ocupacion Id de ocupación
     * @param disponibilidad Disponibilidad para viajar
     * @param disponibilidad_viajar_ciudad Disponibilidad para radircar fuera de la ciudad
     * @param experiencia Id de experiencia, de acuerdo al catalogo
     * @param horario Id de horario, de acuerdo al catalogo
     * @param indicardor_estudios La vacante requiere que el candidato tenga estudios
     * @param edad_de Edad inicial para el puesto
     * @param edad_hasta Edad final para el puesto
     * @param salario Salario ofrecido
     * @param carreras Listado de carreras posibles para el puesto
     * @param grado_estudios Id del grado de estudio, segun el catalogo
     * @param habilidades Listado de habilidades y experiencias
     * @param idiomas Listado de idiomas y dominios
     * @param conocimientos Listado de conocimientos y experiencias
     */
    public Vacante(final long idvacante, final String titulo, final int ocupacion, final List<Integer> carreras, final List<String> conocimientos, final int estado, final int municipio)
    {
        if (idvacante <= 0)
        {
            throw new IllegalArgumentException("El identificador de vacante no puede ser cero o negativo");
        }

        if (estado <= 0)
        {
            throw new IllegalArgumentException("El identificador de estado no puede ser cero o negativo");
        }

        if (municipio <= 0)
        {
            throw new IllegalArgumentException("El identificador de municipio no puede ser cero o negativo");
        }



        if (titulo == null)
        {
            throw new IllegalArgumentException("El titulo no puede ser nulo");
        }
        this.titulo = titulo;
        this.municipio = municipio;

        this.ocupacion = ocupacion;

        for (Integer carrera : carreras)
        {
            if (carrera != null)
            {
                this.carreras.add(carrera);
            }
        }

        this.conocimientos.addAll(conocimientos);



        this.id = idvacante;
        this.estado = estado;

    }

    /**
     * Modifica la lista de conocimientos
     * @param conocimientos Lista de conocimientos
     */
    public void setConocimientos(final List<String> conocimientos)
    {
        this.conocimientos.clear();
        this.conocimientos.addAll(conocimientos);
    }

    /**
     * Regresa al lista de conocimientos
     * @return Lista de conocmientos
     */
    public List<String> getConocimientos()
    {
        return conocimientos;
    }

    /**
     * Modifica el listado de carreras
     * @param carreras Listado de carreras
     */
    public void setCarrera(final List<Integer> carreras)
    {
        this.carreras.clear();
        for (Integer carrera : carreras)
        {
            if (carrera != null)
            {
                this.carreras.add(carrera);
            }
        }
    }

    /**
     * Regresa la lista de carreras
     * @return Listado de carreras
     */
    public Set<Integer> getCarrera()
    {
        return carreras;
    }

    /**
     * Modifica el is de ocupación
     * @param ocupacion Id de ocupación
     */
    public void setOcupacion(final int ocupacion)
    {
        this.ocupacion = ocupacion;
    }

    /**
     * Regresa la ocupación
     * @return Id de la ocupación
     */
    public int getOcupacion()
    {
        return ocupacion;
    }

    /**
     * Regresa el id de la vacante
     * @return Id de la vacante
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * Modifica el id de la vacante
     * @param idvacante Id de la vacante
     */
    public void setId(final int idvacante)
    {
        if (idvacante <= 0)
        {
            throw new IllegalArgumentException("El identificador de un vacante no puede ser cero o negativo");
        }
        this.id = idvacante;
    }

    /**
     * Regresa el id de la vacante como cadena
     * @return Id de la vacante como cadena
     */
    @Override
    public String toString()
    {
        return String.valueOf(id);
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
        final Vacante other = (Vacante) obj;
        if (this.id != other.id)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        
        return  89 * 7 + (int) (this.id ^ (this.id >>> 32));
        
    }

    public void setTitulo(final String titulo)
    {
        this.titulo = titulo;
    }

    public String getTitulo()
    {
        return this.titulo;
    }
    
    public void setTituloVacante(String tituloVacante)
    {
        this.titulo_vacante=tituloVacante;
    }
    public void setUbicacion(String ubicacion)
    {
        this.ubicacion=ubicacion;
    }
    public void setEmpresa(String empresa)
    {
        this.empresa=empresa;
    }
    public void setFecha(Date fecha)
    {
        this.fecha=fecha.getTime();
    }
    
    
    public String getTituloVacante()
    {
        return this.titulo_vacante;
    }
    public String getUbicacion()
    {
        return this.ubicacion;
    }
    public String getEmpresa()
    {
        return this.empresa;
    }
    public Date getFecha()
    {
        if(fecha==0)
        {
            return null;
        }
        else
        {
            return new Date(fecha);
        }
    }
    public int getSalario()
    {
        return this.salario;
    }
    public void setSalario(final int salario)
    {
        this.salario=salario;
    }
    public String getDiscapacidad()
    {
        return discapacidad;
    }

    public void setDiscapacidad(String discapacidad)
    {
        this.discapacidad= discapacidad;
    }
}
