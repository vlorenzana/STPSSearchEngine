/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.HashMap;
import java.util.Map;




/**
 * Clase catalogo
 * @author victor.lorenzana
 */
public final class Catalogo
{       
       
    /**
     * Catalogo de grados de estudio
     */
    public final Map<Integer, String> grado_estudios = new HashMap<Integer, String>();
    /**
     * Catalogo de dominios
     */
    public final Map<Integer, String> dominios = new HashMap<Integer, String>();
    /**
     * Catalogo de idiomas
     */
    public final Map<Integer, String> idiomas_catalogo = new HashMap<Integer, String>();
    /**
     * Catalogo de carreras
     */
    public final Map<Integer, String> carrera_especialidad_catalogo = new HashMap<Integer, String>();
    /**
     * Catalogo de status de estudio
     */
    public final Map<Integer, String> status_estudio_catalogo = new HashMap<Integer, String>();
    /**
     * Catalogo de experiencias
     */
    public final Map<Integer, String> experiencia_catalogo = new HashMap<Integer, String>();
    /**
     * Catalogo de horarios
     */
    public final Map<Integer, String> horario_catalogo = new HashMap<Integer, String>();
    /**
     * Catalogo de ocupaciones
     */
    public final Map<Integer, String> ocupacion_catalogo = new HashMap<Integer, String>();
/**
 * Catalogo de estados
 */
    
    public final Map<Integer, String> estados_catalogo = new HashMap<Integer, String>();

    /**
     * Catalogo de municipios
     */
    public final Map<Integer, String> municipios_catalogo = new HashMap<Integer, String>();
}
