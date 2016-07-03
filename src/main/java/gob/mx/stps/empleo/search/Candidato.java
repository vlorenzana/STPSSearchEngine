/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase intermedia que representa un candidato
 * @author victor.lorenzana
 */
public final class Candidato
{
    public int fuente;
    public int estado;

    public int getFuente() {
        return fuente;
    }

    public void setFuente(int fuente) {
        this.fuente = fuente;
    }
    public transient int municipio;
    
    private String palabras;
    /**
     * Identificador de candidato
     */
    public long id = -1;
    /**
     * Edad del candidato
     */
    private int edad = -1;
    /**
     * Id de experiencia del candidato
     */
    private int experiencia = -1;
    /**
     * Id de horario del candidato
     */
    private int horario = -1;
    /**
     * Booleano que representa si un candidato tiene estudios o no
     */
    private boolean indicardorEstudios;
    /**
     * Booleano que representa si un candidato tiene disponibilidad para viajar
     */
    private boolean disponibilidad;
    /**
     * Booleano que representa si un candidato tiene disponibilidad para radicar fuera
     */
    private boolean disponibilidadViajarCiudad;
    /**
     * Cantidad de salario deseado por el candidato
     */
    private double salario;
    /**
     * Id de ocupación deseada del candidato
     */
    private int ocupacion = -1;
    /**
     * Lista de conocimientos del candidato
     */
    private List<Conocimiento> conocimientos = new ArrayList<Conocimiento>();
    
    private String discapacidad;

    public String getDiscapacidad()
    {
        return discapacidad;
    }

    public void setDiscapacidad(String discapacidad)
    {
        this.discapacidad= discapacidad;
    }
    /**
     * Lista de idiomas que maneja el candidato
     */
    private List<Idioma> idiomas = new ArrayList<Idioma>();
    /**
     * Lista de habilidades que posee el candidato
     */
    private List<Habilidad> habilidades = new ArrayList<Habilidad>();
    /**
     * Lista de grados, estados y niveles académicos del candidato
     */
    private List<InformacionAcademica> informacionAcademica = new ArrayList<InformacionAcademica>();

    /**
     * Constructor de candidato por defecto, creado para que el motor pueda obtener candidatos del índice     * 
     */
    public Candidato()
    {
    }

    /**
     * Constructor con parámetro de identificador para candidato
     * @param idcandidato
     */
    public Candidato(final long idcandidato)
    {
        if (idcandidato <= 0)
        {
            throw new IllegalArgumentException("El identificador de candidato no puede ser cero o negativo");
        }
        this.id = idcandidato;
    }

    /**
     * Constructor completo para un candidato
     * @param idcandidato Identificador de candidato
     * @param ocupacion Id de ocupación
     * @param disponibilidad Tiene disponibilidad para viajar?
     * @param dispViajar Tiene disponibilidad para radicar fuera
     * @param experiencia Id de experiencia en su ramo
     * @param horario Id de horario del trabajo deseado
     * @param indEstudios Tiene estudios?
     * @param edad Edad del candidato
     * @param salario Salario deseado
     * @param informacion_académica Información académica
     * @param habilidades Habilidades que posee
     * @param idiomas Idiomas que domina
     * @param conocimientos Conocimientos que posee
     */
    public Candidato(final long idcandidato, final int ocupacion, final boolean disponibilidad, final boolean dispViajar, final int experiencia, final int horario, final boolean indEstudios, final int edad, final double salario, final List<InformacionAcademica> academica, final List<Habilidad> habilidades, final List<Idioma> idiomas, final List<Conocimiento> conocimientos)
    {

        if (idcandidato <= 0)
        {
            throw new IllegalArgumentException("El identificador de candidato no puede ser cero o negativo");
        }
        this.ocupacion = ocupacion;
        this.disponibilidad = disponibilidad;
        this.disponibilidadViajarCiudad = dispViajar;
        this.informacionAcademica.addAll(academica);
        habilidades.addAll(habilidades);
        idiomas.addAll(idiomas);
        conocimientos.addAll(conocimientos);

        this.horario = horario;
        this.habilidades = habilidades;
        this.indicardorEstudios = indEstudios;
        this.experiencia = experiencia;
        this.idiomas = idiomas;
        this.conocimientos = conocimientos;
        this.id = idcandidato;
        this.edad = edad;
        this.salario = salario;
    }

    /**
     * Cambia los idiomas que posee el candidato
     * @param idiomas Idiomas que domina
     */
    public void setIdiomas(final List<Idioma> idiomas)
    {
        this.idiomas.clear();
        this.idiomas.addAll(idiomas);
    }

    /**
     * Obtiene los idiomas que domina un candidato
     * @return Lista de Idiomas con dominios
     */
    public List<Idioma> getIdiomas()
    {
        return idiomas;
    }
    
    /**
     * Cambia la lista de conocimientos de un candidatos
     * @param conocimientos Conocimientos que posee
     */
    public void setConocimientos(final List<Conocimiento> conocimientos)
    {
        this.conocimientos.clear();
        this.conocimientos.addAll(conocimientos);
    }

    /**
     * Obtiene la lista de conocimientos
     * @return Lista de Conocimientos
     */
    public List<Conocimiento> getConocimientos()
    {
        return conocimientos;
    }

    /**
     * Cambia las habilidades de un candidato
     * @param habilidades Lista de habilidades que posee
     */
    public void setHabilidades(final List<Habilidad> habilidades)
    {
        this.habilidades.clear();
        this.habilidades.addAll(habilidades);
    }

    /**
     * Obtiene la lista de habilidades de un candidato
     * @return Lista de habilidades
     */
    public List<Habilidad> getHabilidades()
    {
        return habilidades;
    }

    /**
     * Cambia la ocupación de un candidato
     * @param ocupacion Id de ocupación
     */
    public void setOcupacion(final int ocupacion)
    {
        this.ocupacion = ocupacion;
    }

    /**
     * Obtiene del Id de la ocupación del candidato
     * @return Id d ocupación correspondiente en el catálogo
     */
    public int getOcupacion()
    {
        return this.ocupacion;
    }

    /**
     * Cambia la información academica del candidato
     * @param academica Lista de información academica, con grados, carreras y estatus del mismo
     */
    public void setInformacionAcademica(final List<InformacionAcademica> academica)
    {
        this.informacionAcademica.clear();
        this.informacionAcademica.addAll(academica);
    }

    /**
     * Obtiene el Id del candidato
     * @return Id del candidato
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * Cambia el Id del candidato
     * @param idcandidato
     */
    public void setId(final int idcandidato)
    {
        if (idcandidato <= 0)
        {
            throw new IllegalArgumentException("El identificador de un candidato no puede ser cero o negativo");
        }
        this.id = idcandidato;
    }

    /**
     * Regresa la edad del candidato
     * @return Edad del candidato
     */
    public int getEdad()
    {

        return edad;
    }

    /**
     * Cambia la edad del candidato
     * @param edad
     */
    public void setEdad(final int edad)
    {
        if (edad <= 0)
        {
            throw new IllegalArgumentException("La edad de un candidato no puede ser cero o negativo");
        }
        this.edad = edad;

    }

    /**
     * Obtiene el Id de la experiencia del candidato, el id proviene del catalogo de experiencias
     * @return Id de experiencia
     */
    public int getExperiencia()
    {
        return experiencia;
    }

    /**
     * Cambia el id de experiencia del candidato
     * @param experiencia Id de experiencia
     */
    public void setExperiencia(final int experiencia)
    {
        if (experiencia <= 0)
        {
            throw new IllegalArgumentException("La experiencia total de un candidato no puede ser cero o negativo");
        }
        this.experiencia = experiencia;
    }

    /**
     * Regresa el Id del horario del trabajo deseado, el id forma parte del catalogo de horarios
     * @return Id de horario
     */
    public int getHorario()
    {
        return horario;
    }

    /**
     * Cambia el Id del horario del candidato
     * @param horario Id del horario
     */
    public void setHorario(final int horario)
    {
        if (horario <= 0)
        {
            throw new IllegalArgumentException("El horario de un candidato no puede ser cero o negativo");
        }
        this.horario = horario;
    }

    /**
     * Cambia la información que indica que el candidato tiene estudios
     * @return Indica que el candidato tiene estudios
     */
    public boolean isIndicardorEstudios()
    {
        return indicardorEstudios;
    }

    /**
     * Cambia si el candidato tiene estudios
     * @param indicador Booleano, sí tiene estudios, no tiene estudios
     */
    public void setIndicardorEstudios(final boolean indicador)
    {
        this.indicardorEstudios = indicador;
    }

    /**
     * Obtiene la disponilidad de viajar por parte del candidato
     * @return Booleano, si tiene disponilidad de viajar por parte del candidato, no tiene disponilidad de viajar por parte del candidato
     */
    public boolean isDisponibilidad()
    {
        return disponibilidad;
    }

    /**
     * Cambia la disponilidad de viajar por parte del candidato
     * @param disponibilidad Booleano, si tiene disponilidad de viajar por parte del candidato, no tiene disponilidad de viajar por parte del candidato
     */
    public void setDisponibilidad(final boolean disponibilidad)
    {
        this.disponibilidad = disponibilidad;
    }

    /**
     * Obtiene la disponilidad de radicar fuera por parte del candidato
     * @return Booleano, si tiene disponilidad de radicar fuera por parte del candidato, no tiene disponilidad de radicar fuera por parte del candidato
     */
    public boolean isDisponibilidadViajarCiudad()
    {
        return disponibilidadViajarCiudad;
    }

    /**
     * Obtiene el salario deseado por parte del candidato
     * @return Cantidad deseada
     */
    public double getSalario()
    {
        return salario;
    }

    /**
     * Cambia el salario deseado por parte del candidato
     * @param salario Cantidad deseada
     */
    public void setSalario(final double salario)
    {
        if (salario <= 0)
        {
            throw new IllegalArgumentException("El salario de un candidato no puede ser cero o negativo");
        }
        this.salario = salario;
    }

    /**
     * Regresa el id del candidato
     * @return Id del candidato
     */
    @Override
    public String toString()
    {
        return String.valueOf(id);
    }

    /**
     * Compara dos objetos
     * @param obj Objeto a comparar
     * @return True, si son candidatos con el mismo id, falso caso contrario
     */
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
        final Candidato other = (Candidato) obj;
        if (this.id != other.id)
        {
            return false;
        }
        return true;
    }

    /**
     * Regresa el  hashCode basado en el id del candidato
     * @return HashCode basado en el id del candidato
     */
    @Override
    public int hashCode()
    {
        return 29 * 7 + (int) this.id;
    }

    /**
     * Regresa cadena vacía en caso de cadena nula
     * @param value Cadena a convertir
     * @return Cadena vacía o la misma cadena
     */
    public String getValueOrEmpty(final String value)
    {
        return value == null ? "" : value;
    }

    public void setPalabras(final String palabras)
    {
        this.palabras = palabras;
    }

    public String getPalabras()
    {
        return this.palabras;
    }

    public void setDisponibilidadViajarCiudad(final boolean disponibilidadviajarciudad)
    {
        this.disponibilidadViajarCiudad = disponibilidadviajarciudad;
    }

    public List<InformacionAcademica> getInformacionAcademica()
    {
        return informacionAcademica;
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
    
}
