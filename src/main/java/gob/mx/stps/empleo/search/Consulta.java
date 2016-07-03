/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clase que describe una consulta de vacantes o candidatos
 * @author victor.lorenzana
 */
public final class Consulta
{

    public String text;

    public String getOriginalText()
    {
        return originalText;
    }

    public void setOriginalText(final String originalText)
    {
        this.originalText = originalText;
    }

    public String getText()
    {
        return text;
    }

    public void setText(final String text)
    {
        this.text = text;
    }
    public String originalText;
    public String tagedText;
    public String discapacidad;
    public Consulta()
    {
    }

    public Consulta(final String text)
    {
        this.text = text;
    }
    /**
     * Id de experiencia total
     */
    public transient Integer experiencia_total = null;
    /**
     * Id de horario
     */
    public transient Integer horario = null;
   
    /**
     * Edad inicial de una consulta
     */
    private transient Integer edadDe = null;
    /**
     * Edad final de una consulta
     */
    private transient Integer edadHasta = null;
    
    /**
     * Salario inicial
     */
    private transient Double salarioDe = null;
    /**
     * Salario final
     */
    private transient Double salarioHasta = null;
    /**
     * Disponibilidad para viajar
     */
    public transient Boolean disponibilidad = null;
    /**
     * Disponibilidad para radicar fuera de la ciudad
     */
    public transient Boolean disponibilidad_viajar_ciudad = null;
    /**
     * Indicador de estudios
     */
    public transient Boolean indicador_estudios = null;
    /**
     * Id de carrera a buscar
     */
    public transient Integer carrera = null;
    /**
     * Id de ocupación
     */
    public transient Integer ocupacion = null;
    public transient final Set<Integer> otras_experiencias = new HashSet<Integer>();
    /**
     * Status adicionales a buscar
     */
    public transient final Set<Integer> otros_status_estudio = new HashSet<Integer>();
    /**
     * Grados academicos adicionales para buscar
     */
    public transient final Set<Integer> otros_grados = new HashSet<Integer>();
    /**
     * ID de horarios adicionales para buscar
     */
    public transient final Set<Integer> otros_horarios = new HashSet<Integer>();
    public transient final Map<String, Set<String>> sinonimos_ref = new HashMap<String, Set<String>>();
    /**
     * Ocupaciones adicionales a buscar (ID)
     */
    public transient final Set<Integer> otras_ocupaciones = new HashSet<Integer>();
    /**
     * Id de carreras adicionales a buscar
     */
    public transient final Set<Integer> otras_carreras = new HashSet<Integer>();
    /**
     * Lista de informaciones académicas a buscar
     */
    public transient final List<InformacionAcademica> academica = new ArrayList<InformacionAcademica>();
    private Integer gradoEstudios = null;
    private Integer statusEstudio = null;
    public transient final List<Habilidad> habilidades = new ArrayList<Habilidad>();
    public transient final List<Idioma> idiomas = new ArrayList<Idioma>();
    public transient final List<Conocimiento> conocimientos = new ArrayList<Conocimiento>();
    
    
    public Integer getStatusEstudio()
    {
        return statusEstudio;
    }

    public void setStatusEstudio(final Integer statusEstudio)
    {
        if(statusEstudio!=null && statusEstudio>0)
        {
            this.otros_status_estudio.add(statusEstudio);   
            if(this.statusEstudio==null)
            {
                this.statusEstudio = statusEstudio;
            }
            else
            {
                this.otros_status_estudio.add(statusEstudio);                
            }
        }
    }

    
    
    public Integer getGradoEstudios()
    {
        return gradoEstudios;
    }

    public void setGradoEstudios(final Integer gradoEstudios)
    {
        if(gradoEstudios==null)
        {
            this.gradoEstudios=gradoEstudios;
        }
        if(gradoEstudios!=null && gradoEstudios>0)
        {
            if(this.gradoEstudios==null)
            {
                this.gradoEstudios = gradoEstudios;
            }
            else
            {
                this.otros_grados.add(gradoEstudios);                
            }
        }
    }

    public Double getSalarioDe()
    {
        return salarioDe;
    }

    public Double getSalarioHasta()
    {
        return salarioHasta;
    }
    
    public void setSalario(final Double salario)
    {
        if (salario <= 0)
        {
            return;
        }
        if (this.salarioDe == null)
        {
            this.salarioDe = salario;
        }
        else if (this.salarioHasta == null)
        {
            this.salarioHasta = salario;
        }


        if (this.salarioDe != null && this.salarioHasta != null && this.salarioDe.doubleValue() > this.salarioHasta.doubleValue())
        {
            final Double tmp = this.salarioDe;
            this.salarioDe = salarioHasta;
            this.salarioHasta = tmp;
        }
    }

    public void setEdad(final Integer edad)
    {
        if (edad == null)
        {
            return;
        }
        if (edad <= 0)
        {
            return;
        }
        if (this.edadDe == null)
        {
            this.edadDe = edad;
        }
        else if (this.edadHasta == null)
        {
            this.edadHasta = edad;
        }


        if (this.edadDe != null && this.edadHasta != null && this.edadDe.intValue() > this.edadHasta.intValue())
        {

            final Integer tmp = this.edadDe;
            this.edadDe = edadHasta;
            this.edadHasta = tmp;

        }
    }
    public transient Integer edo;
    public transient Integer municipio;
    public transient String vacante;

    public Integer getEdadDe()
    {
        return edadDe;
    }

    public Integer getEdadHasta()
    {
        return edadHasta;
    }

    public boolean hasCriterio(final String criterio)
    {
        return getCriterios().contains(criterio);
    }

    /**
     *
     * @return Regresa los criterios de información detectados en la consulta
     */
    public Set<String> getCriterios()
    {
        final Set<String> getCriterios = new HashSet<String>();
        if (experiencia_total != null || !otras_experiencias.isEmpty())
        {
            getCriterios.add(Campos.EXPERIENCIA.toString());
        }        
        if (horario != null)
        {
            getCriterios.add(Campos.HORARIO_DE_EMPLEO.toString());
        }        
        if (edadDe != null || edadHasta != null)
        {
            getCriterios.add(Campos.EDAD.toString());
        }
        if (salarioDe != null || salarioHasta != null)
        {
            getCriterios.add(Campos.SALARIO.toString());
        }        
        if (disponibilidad != null)
        {
            getCriterios.add(Campos.DISPONIBILIDAD.toString());
        }
        if (disponibilidad_viajar_ciudad != null)
        {
            getCriterios.add(Campos.DISPONIBILIDAD_VIAJAR_CIUDAD.toString());
        }
        if (edo != null)
        {
            getCriterios.add(Campos.EDO.toString());
        }
        if (municipio != null)
        {
            getCriterios.add(Campos.MUNICIPIO.toString());
        }
        if (vacante != null)
        {
            getCriterios.add(Campos.NOMBRE.toString());
        }
        if (indicador_estudios != null)
        {
            getCriterios.add(Campos.INDICADOR_DE_ESTUDIOS.toString());
        }
        if (carrera != null || !otras_carreras.isEmpty())
        {
            getCriterios.add(Campos.CARRERA_O_ESPECIALIDAD.toString());
        }
        if (ocupacion != null || !otras_ocupaciones.isEmpty() || !ocupaciones_detectadas.isEmpty())
        {
            getCriterios.add(Campos.OCUPACION.toString());
        }
        if (!otros_status_estudio.isEmpty())
        {
            getCriterios.add(Campos.STATUS_ACADEMICO.toString());
        }
        if (!otros_grados.isEmpty() || gradoEstudios != null)
        {
            getCriterios.add(Campos.GRADO_DE_ESTUDIOS.toString());
        }
        if (!otros_horarios.isEmpty())
        {
            getCriterios.add(Campos.HORARIO_DE_EMPLEO.toString());
        }               
        if (!academica.isEmpty())
        {
            getCriterios.add("INFORMACION ACADEMICA");
        }       
        if (statusEstudio != null)
        {
            getCriterios.add(Campos.STATUS_ACADEMICO.toString());
        }
        if (!habilidades.isEmpty())
        {
            getCriterios.add(Campos.HABILIDAD.toString());
        }
        if (!idiomas.isEmpty())
        {
            getCriterios.add(Campos.IDIOMA.toString());
            for (Idioma idioma : idiomas)
            {
                if (idioma.dominio_id > 0)
                {
                    getCriterios.add(Campos.DOMINIO.toString());
                }
            }
        }
        if (!conocimientos.isEmpty())
        {
            getCriterios.add(Campos.CONOCIMIENTO.toString());
        }
        if (!carreras_detectadas.isEmpty())
        {
            getCriterios.add(Campos.CARRERA_O_ESPECIALIDAD.toString());
        }
        
        return getCriterios;
    }

    @Override
    public String toString()
    {
        final StringBuilder content = new StringBuilder();
        for (String criterio : getCriterios())
        {
            content.append(criterio);
            content.append(",");
        }
        String toString = content.toString();
        if (toString.endsWith(","))
        {
            toString = toString.substring(0, toString.length() - 1);
        }
        return toString;
    }

    public String[] getAllCriterios()
    {
        final Set<String> getAllCriterios = new HashSet<String>();
        for (Campos campo : Campos.values())
        {
            getAllCriterios.add(campo.toString());
        }
        getAllCriterios.add("INFORMACION ACADEMICA");
        return getAllCriterios.toArray(new String[getAllCriterios.size()]);
    }
    public Set<String> carreras_words = new HashSet<String>();
    public Set<String> ocupaciones_words = new HashSet<String>();
    public transient Set<Integer> carreras_detectadas = new HashSet<Integer>();
    public transient Set<Integer> ocupaciones_detectadas = new HashSet<Integer>();
    public Integer fuente;

    public Integer getFuente() {
        return fuente;
    }

    public void setFuente(Integer fuente) {
        this.fuente = fuente;
    }
}
