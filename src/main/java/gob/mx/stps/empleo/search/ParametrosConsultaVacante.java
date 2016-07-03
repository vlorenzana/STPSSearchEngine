/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

/**
 *
 * @author victor.lorenzana
 */
public class ParametrosConsultaVacante
{
    public String search=null;
    public Integer estado=null;
    public Integer municipio=null;
    public CampoOrdenamiento campo=CampoOrdenamiento.SCORE;
    public Sort sort=Sort.AUTO;
    public String discapacidad=null;
    public Integer fuente=null;
    public Integer edad=null;
}
