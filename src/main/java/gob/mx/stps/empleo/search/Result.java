/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.Iterator;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.CompassHit;
import org.compass.core.CompassHits;
import org.compass.core.CompassSearchSession;

/**
 * Clase que encapsula un conjunto de resultados
 * @author victor.lorenzana
 */
public final class Result
{

    /**
     * Log de mensajes
     */
    private static final Log LOG = LogFactory.getLog(Result.class);
    /**
     * Hist de resultados
     */
    transient private final CompassHits hits;
    /**
     * Iterador de hista de resultados
     */
    transient private final Iterator<CompassHit> ithits;
    /**
     * Sessionde compass, esta incluida ya que deben de cerrar los resultados, para cerrar la sesión, por cuestiones de rendimiento
     */
    transient private final CompassSearchSession session;
    /**
     * Identificador de resultados
     */
    transient private final String name = UUID.randomUUID().toString();
    /**
     * Número de resultados obtenidos
     */
    transient private final long count;
    transient private final String query;
    /**
     * Contructor por defecto
     *
     */
    transient private final Consulta consulta;

    public Result(final Consulta consulta)
    {
        this.count = 0;
        this.consulta = consulta;
        this.query = null;
        this.hits = null;
        ithits = null;
        this.session = null;
    }

    public Result(final CompassHits hits, final CompassSearchSession session, final long count, final Consulta consulta)
    {
        this.query = hits.getQuery().toString();
        this.consulta = consulta;
        this.hits = hits;
        ithits = this.hits.iterator();

        this.session = session;
        this.count = count;

    }

    public String getQuery()
    {
        return query;
    }

    /**
     * Cierra la sesión
     */
    public void close()
    {
        if (session != null)
        {
            try
            {
//                if (log.isTraceEnabled())
//                {
//                    log.trace("Cerrando Result " + name);
//                }
                session.close();
            }
            catch (Exception e)
            {
                LOG.error("Error cerrando session de compass", e);
            }
        }
    }

    public Consulta getConsulta()
    {

        return consulta;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Regresa el número de resultados
     * @return Número de resultados
     */
    public int getCount()
    {
        return (int) count;
    }

    @Override
    public String toString()
    {
        return String.valueOf(getCount());
    }

    /**
     * Regresa un resultado en base  a un indice, -1 si no encuentra el indice
     * @param index
     * @return Id de vacante o candidato encontrada
     */
    public ResultInfo get(final int index)
    {
        if (hits == null)
        {
            return null;
        }
        if (index < getCount())
        {
            try
            {
                final Object data = hits.data(index);
                ResultInfo info = new ResultInfo(-1);
                if (data instanceof CandidatoIndex)
                {
                    final CandidatoIndex candidato = (CandidatoIndex) hits.data(index);
                    if (candidato != null)
                    {
                        info.id = candidato.id;
                        return info;
                    }
                }
                else if (data instanceof VacanteIndex)
                {
                    final VacanteIndex vacante = (VacanteIndex) hits.data(index);
                    if (vacante != null)
                    {
                        info = fill(vacante);
                        return info;
                    }
                }
                else
                {
                    return null;
                }

            }
            catch (CompassException ce)
            {
                LOG.error("Error al obtener un resultado", ce);
            }
        }
        return null;
    }

    /**
     * Regresa true si tiene más elementos
     * @return true si tiene más elementos, false caso contrario
     */
    public boolean hasNextElement()
    {
        if (hits == null)
        {
            return false;
        }
        else
        {
            return ithits.hasNext();
        }
    }

    /*public List<Long> getId(int from, int to)
    {
    ArrayList<Long> _values = new ArrayList<Long>();
    int min = (int) Math.min(to, hits.length());
    for (int i = from; i < min; i++)
    {
    try
    {
    Document doc = LuceneHelper.getLuceneSearchEngineHits(hits).getHits().doc(i);
    _values.add(new Long(doc.get("id")));
    }
    catch (Exception e)
    {
    e.printStackTrace();
    }
    }
    return _values;
    }
    
    public List<Long> getAll()
    {
    ArrayList<Long> _values = new ArrayList<Long>();
    for (int i = 0; i < hits.length(); i++)
    {
    try
    {
    Document doc = LuceneHelper.getLuceneSearchEngineHits(hits).getHits().doc(i);
    _values.add(new Long(doc.get("id")));
    }
    catch (Exception e)
    {
    e.printStackTrace();
    }
    }
    return _values;
    }*/
    /**
     * Regresa el siguiente id de vacante o candidato
     * @return Id de candidato o vacante
     */
    public ResultInfo nextElement()
    {
        if (hits == null)
        {
            return null;
        }
        if (ithits.hasNext())
        {
            final CompassHit hit = ithits.next();
            ResultInfo info = new ResultInfo(-1);
            final Object obj = hit.getData();
            if (obj instanceof CandidatoIndex)
            {
                final CandidatoIndex candidato = (CandidatoIndex) hit.getData();
                info.id = candidato.id;
                return info;
            }
            if (obj instanceof VacanteIndex)
            {
                final VacanteIndex vacante = (VacanteIndex) hit.getData();
                info = fill(vacante);
                return info;
            }
        }
        return null;
    }

    public ResultInfo fill(final VacanteIndex vacante)
    {
        boolean show = true;
        final String valueshow = System.getProperty("showResultInfo", "true");
        show = Boolean.parseBoolean(valueshow);
        final ResultInfo info = new ResultInfo(-1);
        info.id = vacante.id;
        filltitulo(vacante, info);
        if (show)
        {
            fillCarreras(vacante, info);
            fillOcupaciones(vacante, info);
            fillConocimientos(vacante, info);
        }
        return info;
    }

    private void fillConocimientos(final VacanteIndex vacante, final ResultInfo info) throws CompassException
    {
        if (hits.length() > 0 && hits.getQuery().toString().indexOf("CONOCIMIENTO") != -1)
        {
            for (String conocimiento : vacante.conocimientos)
            {
                final String value = hits.highlighter(0).fragment("CONOCIMIENTO", conocimiento);
                if (value == null)
                {
                    info.conocimientos.add(conocimiento);

                }
                else
                {
                    info.conocimientos.add(value);
                }
            }
        }
        else
        {
            info.conocimientos.addAll(vacante.conocimientos);
        }
    }

    private void fillOcupaciones(final VacanteIndex vacante, final ResultInfo info) throws CompassException
    {
        if (hits.length() > 0 && hits.getQuery().toString().indexOf("OCUPACIONWORDS") != -1)
        {
            if (vacante.ocupacion != null)
            {
                final String value = hits.highlighter(0).fragment("OCUPACIONWORDS", vacante.ocupacion);
                if (value == null)
                {
                    info.ocupacion = vacante.ocupacion;
                }
                else
                {
                    info.ocupacion = value;
                }
            }
        }
        else
        {
            info.ocupacion = vacante.ocupacion;
        }
    }

    private void fillCarreras(final VacanteIndex vacante, final ResultInfo info) throws CompassException
    {
        if (hits.length() > 0 && hits.getQuery().toString().indexOf("CARRERAWORDS") != -1)
        {
            for (String carrera : vacante.carrera)
            {
                final String value = hits.highlighter(0).fragment("CARRERAWORDS", carrera);
                if (value == null)
                {
                    info.carreras.add(carrera);

                }
                else
                {
                    info.carreras.add(value);

                }
            }
        }
        else
        {
            info.carreras.addAll(vacante.carrera);
        }
    }

    private void filltitulo(final VacanteIndex vacante, final ResultInfo info) throws CompassException
    {
        if (vacante.titulo != null && hits.length() > 0 && hits.getQuery().toString().indexOf("TITULO") != -1)
        {

            final String value = hits.highlighter(0).fragment("TITULO", vacante.titulo);
            if (value == null)
            {
                info.titulo = vacante.titulo;
            }
            else
            {
                info.titulo = value;
            }

        }
        else
        {
            info.titulo = vacante.titulo;
        }
    }
}
