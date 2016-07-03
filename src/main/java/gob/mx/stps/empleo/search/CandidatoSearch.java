/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.BooleanQuery;
import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassIndexSession;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassQueryBuilder.CompassBooleanQueryBuilder;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * Clase de búsqueda de un candidato
 *
 * @author victor.lorenzana
 */
public final class CandidatoSearch
{

    private static final char[] REPLACE_VALUES =
    {
        '(', ')', ':', '/', ',', '-', '[', ']', '_'
    };
    private static final String REF = "REF";
    private static final String AÑOS = "AÑOS";
    private static final String CANDIDATO = "candidato";
    private static final String ERROR = "Error";
    private static final String MSG_SAVE = "Error salvando candidato: ";
    private static final String SEC = "sec.";
    private static final SetTerminos TERMS = new SetTerminos();
    private static final String ESTADO = "ESTADO";
    private static final String MUNICIPIO = "MUNICIPIO";
    /**
     * Log de mensajes
     */
    private static final Log LOG = LogFactory.getLog(CandidatoSearch.class);
    /**
     * Variable de acceso a Compass
     */
    private static final CompassInfo LUCENE = new CompassInfo();
    /**
     * Campo de búsqueda
     */
    private static final String VALUE_FIELD = "VALUE";
    private static final Locale LOCALE = new Locale("es", "MX");
    /**
     * Formato para monedas, utilizado para convertir un término encontrado a
     * decimal
     */
    private static final DecimalFormat CURRENCY = new DecimalFormat("$###,###,###", new DecimalFormatSymbols(new Locale("es", "MX")));

    private static void checkSinonimos(final StringBuilder text, final StringBuilder textTarget)
    {
        final Set<String>[] sinonimos = CompassInfo.getSinonimos();
        for (Set<String> set : sinonimos)
        {
            for (String termino : set)
            {
                final int pos = text.indexOf(termino);
                if (pos != -1)
                {
                    CompassInfo.replace(text, termino, "");
                    //text.replace(termino, "");
                    CompassInfo.replace(textTarget, termino, "");
                    //textTarget.replace(termino, "");
                    final String[] newsim = CompassInfo.getSinonimos(termino);
                    for (String sinToFind : newsim)
                    {
                        CompassInfo.replace(textTarget, sinToFind, "");
                        //textTarget = textTarget.replace(sinToFind, "");
                        CompassInfo.replace(text, sinToFind, "");
                        //text = text.replace(sinToFind, "");
                    }
                    break;
                }
            }
        }

    }

    /**
     * Constructor con los catálogos a utilizar el indexador tablas de términos
     *
     * @param catalogo Objeto catalogo con las tablas de términos, catalogos del
     * portal del empleo
     */
    public CandidatoSearch(final Catalogo catalogo)
    {

        CompassInfo.STATUS.putAll(catalogo.status_estudio_catalogo);
        CompassInfo.GRADOS.putAll(catalogo.grado_estudios);
        CompassInfo.OCUPACIONES.putAll(catalogo.ocupacion_catalogo);
        CompassInfo.CARRERAS.putAll(catalogo.carrera_especialidad_catalogo);
        CompassInfo.HORARIO.putAll(catalogo.horario_catalogo);
        CompassInfo.EXPERIENCIA.putAll(catalogo.experiencia_catalogo);


        copyData(CompassInfo.OCUPACIONESCAT, catalogo.ocupacion_catalogo);
        copyData(CompassInfo.CARRERASCAT, catalogo.carrera_especialidad_catalogo);
        copyData(CompassInfo.IDIOMAS, catalogo.idiomas_catalogo);
        copyData(CompassInfo.DOMINIOS, catalogo.dominios);
        copyGrado(catalogo.grado_estudios);
        copyDataExperiencia(catalogo.experiencia_catalogo);
        copyDataStatus(catalogo.status_estudio_catalogo);

        TERMS.addTerm("horario", "HORARIO");
        TERMS.addTerm("pesos", "PESOS");
        TERMS.addTerm("años", AÑOS);
        TERMS.addTerm("año", AÑOS);
        TERMS.addTerm("disponibilidad para viajar", "DISP");
        TERMS.addTerm("disponibilidad viajar", "DISP");
        TERMS.addTerm("disponibilidad para radicar fuera", "RAD");
        TERMS.addTerm("disponibilidad radicar fuera", "RAD");
        TERMS.addTerm("sin estudios", "SIN");
        TERMS.addTerm("salario", "SALARIO");
        TERMS.addTerm("años de edad", Campos.EDAD.toString());
        //terms.addTerm(CompassInfo.changeCharacters("conocimiento en"), Campos.CONOCIMIENTO.toString());
        //terms.addTerm(CompassInfo.changeCharacters("conocimientos en"), Campos.CONOCIMIENTO.toString());
        TERMS.addTerm("años de experiencia", AÑOS);
        TERMS.addTerm("año de experiencia", AÑOS);
        TERMS.addTerm("años experiencia", AÑOS);
        TERMS.addTerm("año experiencia", AÑOS);
        //terms.addTerm(CompassInfo.changeCharacters("habilidad en"), "HAB");
        //terms.addTerm(CompassInfo.changeCharacters("habilidades en"), "HAB");

        for (Integer iexperencia : CompassInfo.EXPERIENCIA.keySet())
        {
            agregaExperiencia(iexperencia);
        }
        processTerms();
        addSimilares();

    }

    private void agregaExperiencia(Integer iexperencia)
    {
        final String value = CompassInfo.EXPERIENCIA.get(iexperencia).toLowerCase();
        final Term term = new Term(value.trim(), Campos.EXPERIENCIA.toString(), iexperencia, value);
        TERMS.add(term, false, true);
    }

    private void addSimilaresOcupaciones()
    {
        final List<CampoInfo> ocupaciones = new ArrayList<CampoInfo>();
        for (Integer iocupacion : CompassInfo.OCUPACIONESCAT.keySet())
        {
            final String ocupacion = CompassInfo.OCUPACIONESCAT.get(iocupacion);
            ocupaciones.add(new CampoInfo(iocupacion, ocupacion));
        }
        Collections.sort(ocupaciones, new Comparator<CampoInfo>()
        {

            @Override
            public int compare(final CampoInfo info1, final CampoInfo info2)
            {
                final Integer integer1 = info1.getValue().length();
                final Integer integer2 = info2.getValue().length();
                return integer1.compareTo(integer2);
            }
        });


        for (int icarrera = 0; icarrera < ocupaciones.size(); icarrera++)
        {
            final CampoInfo info = ocupaciones.get(icarrera);
            final CarrerasOcupacionSimilares similares = new CarrerasOcupacionSimilares();
            similares.add(info.getIndex());
            similares.setIndiceCarreraOcupacion(info.getIndex());
            for (int icarrera2 = icarrera + 1; icarrera2 < ocupaciones.size(); icarrera2++)
            {
                final CampoInfo info2 = ocupaciones.get(icarrera2);
                final String ocupacion = info.getValue();
                final String text = info2.getValue();
                if (text.startsWith(ocupacion))
                {
                    similares.add(info2.getIndex());
                }
            }
            if (similares.size() > 1)
            {
                synchronized (CompassInfo.OCUPASIMILARES)
                {
                    CompassInfo.OCUPASIMILARES.add(similares);
                }
            }
        }
    }

    private void addSimilaresCarreras()
    {
        final List<CampoInfo> carreras = new ArrayList<CampoInfo>();

        for (Integer icarrera : CompassInfo.CARRERASCAT.keySet())
        {
            final String carrera = CompassInfo.CARRERASCAT.get(icarrera);
            carreras.add(new CampoInfo(icarrera, carrera));
        }
        Collections.sort(carreras, new Comparator<CampoInfo>()
        {

            @Override
            public int compare(final CampoInfo info1, final CampoInfo info2)
            {
                final Integer integer1 = info1.getValue().length();
                final Integer integer2 = info2.getValue().length();
                return integer1.compareTo(integer2);
            }
        });
        for (int icarrera = 0; icarrera < carreras.size(); icarrera++)
        {
            final CampoInfo info = carreras.get(icarrera);
            final CarrerasOcupacionSimilares similares = new CarrerasOcupacionSimilares();
            similares.add(info.getIndex());
            similares.setIndiceCarreraOcupacion(info.getIndex());
            for (int icarrera2 = icarrera + 1; icarrera2 < carreras.size(); icarrera2++)
            {
                final CampoInfo info2 = carreras.get(icarrera2);
                final String carrera = info.getValue();
                final String text = info2.getValue();
                if (text.startsWith(carrera))
                {
                    similares.add(info2.getIndex());
                }
            }
            if (similares.size() > 1)
            {
                synchronized (CompassInfo.CARRERASSIMILARES)
                {
                    CompassInfo.CARRERASSIMILARES.add(similares);
                }
            }
        }
    }

    /**
     * Agrega las carreras similares
     */
    private void addSimilares()
    {

        LOG.info("Iniciando agregar similares...");
        final long tini = System.currentTimeMillis();
        addSimilaresCarreras();
        addSimilaresOcupaciones();
        final long tfin = System.currentTimeMillis();
        final long dif = tfin - tini;
        LOG.info("Fin de cargar similares tiempo: " + dif + " ms");

    }

    public void replacePuntuationCharacters(final StringBuilder search)
    {

        //return search.replace('(', ' ').replace(')', ' ').replace(':', ' ').replace('/', ' ').replace(',', ' ').replace('-', ' ').replace('[', ' ').replace(']', ' ').replace('_', ' ');
        for (int i = 0; i < search.length(); i++)
        {
            for (char test : REPLACE_VALUES)
            {
                final char _char = search.charAt(i);
                if (test == _char)
                {
                    search.setCharAt(i, ' ');
                    break;
                }
            }
        }
    }

    private void agregaExperiencia(final Map<Integer, String> source, final Integer key)
    {
        String data = source.get(key).toLowerCase().replace('-', ' ');
        final StringBuilder sbtemp = new StringBuilder(data);
        CompassInfo.changeCharacters(sbtemp);
        final String temp = sbtemp.toString();
        TERMS.addTerm(new Term(temp, Campos.EXPERIENCIA.toString(), key.intValue(), data));
        if (temp.endsWith("año"))
        {
            TERMS.addTerm(new Term(temp + "s de experiencia", Campos.EXPERIENCIA.toString(), key.intValue(), data));
            TERMS.addTerm(new Term(temp + "s experiencia", Campos.EXPERIENCIA.toString(), key.intValue(), data));
        }
        if (temp.endsWith("años"))
        {
            TERMS.addTerm(new Term(temp + " de experiencia", Campos.EXPERIENCIA.toString(), key.intValue(), data));
            TERMS.addTerm(new Term(temp + " experiencia", Campos.EXPERIENCIA.toString(), key.intValue(), data));
        }
        data = data.replace('á', 'a');
        data = data.replace('-', 'a');
        if (data.endsWith("año"))
        {
            TERMS.addTerm(new Term(data + "s de experiencia", Campos.EXPERIENCIA.toString(), key.intValue(), data));
            TERMS.addTerm(new Term(data + "s experiencia", Campos.EXPERIENCIA.toString(), key.intValue(), data));
        }
        if (data.endsWith("años"))
        {
            TERMS.addTerm(new Term(data + " de experiencia", Campos.EXPERIENCIA.toString(), key.intValue(), data));
            TERMS.addTerm(new Term(data + " experiencia", Campos.EXPERIENCIA.toString(), key.intValue(), data));
        }
        if (data.startsWith("6m"))
        {
            String newdata = "6 meses a 1 año";
            TERMS.addTerm(new Term(newdata, Campos.EXPERIENCIA.toString(), key.intValue(), data));
            newdata = "6 meses 1 año";
            TERMS.addTerm(newdata, Campos.EXPERIENCIA.toString(), key.intValue(), data);
            newdata = "6 meses a 1 año de experiencia";
            TERMS.addTerm(newdata, Campos.EXPERIENCIA.toString(), key.intValue(), data);
            newdata = "6 meses 1 año de experiencia";
            TERMS.addTerm(newdata, Campos.EXPERIENCIA.toString(), key.intValue(), data);
            newdata = "6 meses 1 año experiencia";
            TERMS.addTerm(newdata, Campos.EXPERIENCIA.toString(), key.intValue(), data);
            newdata = "6 meses de experiencia ";
            TERMS.addTerm(newdata, Campos.EXPERIENCIA.toString(), key.intValue(), data);
            newdata = "6 meses experiencia ";
            TERMS.addTerm(newdata, Campos.EXPERIENCIA.toString(), key.intValue(), data);
        }
    }

    private void carrerasOcupaciones(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        final List<CompassQuery> qcarrerasocup = new ArrayList<CompassQuery>();
        creaConsultaCarreras(consulta, builder, qcarrerasocup);
        creaConsultaOcupaciones(consulta, builder, qcarrerasocup);
        if (!qcarrerasocup.isEmpty())
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder bcarrerasocu = builder.bool();
            for (CompassQuery q : qcarrerasocup)
            {
                bcarrerasocu.addShould(q);
            }
            queries.add(bcarrerasocu.toQuery());
        }
    }

    private void copyDataExperiencia(final Map<Integer, String> source)
    {
        for (Integer key : source.keySet())
        {
            agregaExperiencia(source, key);
        }

    }

    private void creaCarrera(final Consulta consulta, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        final Set<Integer> carreras = getCarreras(consulta);
        for (Integer carrera_add : carreras)
        {
            final String data = "g" + carrera_add + "_*";
            final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            setcarrerasocup.add(query);
        }
    }

    private void creaConsultaAcademica(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries) throws BusquedaException
    {
        final Set<CompassQuery> setcarrerasocup = new HashSet<CompassQuery>();
        creaStatus(consulta, builder, setcarrerasocup);
        creaConsultaAcademica(consulta, builder, setcarrerasocup);
        if (!setcarrerasocup.isEmpty())
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder bool2 = builder.bool();
            for (CompassQuery q : setcarrerasocup)
            {
                bool2.addShould(q);
            }
            queries.add(bool2.toQuery());
        }
    }

    private void creaConsultaAcademica(final Consulta consulta, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {

        for (InformacionAcademica informacionAcademica : consulta.academica)
        {

            if (!(informacionAcademica.carrera <= 0 && informacionAcademica.grado_estudios <= 0 && informacionAcademica.status <= 0))
            {
                creaNoComodines(informacionAcademica.grado_estudios, informacionAcademica.grado_estudios, informacionAcademica.carrera, builder, setcarrerasocup);
            }

//            String carrera = String.valueOf(informacionAcademica.carrera);
//            if (informacionAcademica.carrera <= 0)
//            {
//                carrera = "*";
//            }
//            String grado = String.valueOf(informacionAcademica.gradoEstudios);
//            if (informacionAcademica.gradoEstudios <= 0)
//            {
//                grado = "*";
//            }
//            String status = String.valueOf(informacionAcademica.status);
//            if (informacionAcademica.status <= 0)
//            {
//                status = "*";
//            }
//
//            if (!("*".equals(carrera) && "*".equals(grado) && "*".equals(status)))
//            {
//                creaNoComodines(grado, status, carrera, builder, setcarrerasocup);
//            }
        }

    }

    private void creaConsultaConocimiento(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries) throws BusquedaException
    {
        if (!consulta.conocimientos.isEmpty())
        {


            for (Conocimiento conocimiento : consulta.conocimientos)
            {
                if (conocimiento.name == null)
                {
                    throw new BusquedaException("El valor de conocimiento no puede ser nulo");
                }
                if (conocimiento.experiencia <= 0)
                {
                    final CompassQueryBuilder.CompassBooleanQueryBuilder bool2 = builder.bool();
                    creaConsultaConocimientoSinExperiencia(conocimiento, builder, bool2);
                    queries.add(bool2.toQuery());
                }
                else
                {
                    final CompassQueryBuilder.CompassBooleanQueryBuilder bool2 = builder.bool();
                    creaConsultaConocimientoExperiencia(conocimiento, builder, bool2);
                    queries.add(bool2.toQuery());
                }
            }

        }
    }

    private void creaConsultaHabilidad(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries) throws BusquedaException
    {
        if (!consulta.habilidades.isEmpty())
        {


            for (Habilidad habilidad : consulta.habilidades)
            {
                if (habilidad.name == null)
                {
                    throw new BusquedaException("El valor de habilidad no puede ser nulo");
                }
                if (habilidad.experiencia <= 0)
                {
                    final CompassQueryBuilder.CompassBooleanQueryBuilder bool2 = builder.bool();
                    creaConsultaHabilidadSinExp(habilidad, builder, bool2);
                    queries.add(bool2.toQuery());
                }
                else
                {
                    final CompassQueryBuilder.CompassBooleanQueryBuilder bool2 = builder.bool();
                    creaConsultaHabilidadExperiencia(habilidad, builder, bool2);
                    queries.add(bool2.toQuery());
                }
            }



        }
    }

    private void creaDispViajar(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.disponibilidad_viajar_ciudad != null)
        {
            final String data = "b" + consulta.disponibilidad_viajar_ciudad;
            final CompassQuery query = builder.term(VALUE_FIELD, data);
            queries.add(query);
        }
    }

    private void creaDisponibilidad(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.disponibilidad != null)
        {
            final String data = "a" + consulta.disponibilidad;
            final CompassQuery query = builder.term(VALUE_FIELD, data);
            queries.add(query);
        }
    }

    private void creaExperiencia(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.experiencia_total != null)
        {
            consulta.otras_experiencias.add(consulta.experiencia_total);
        }
        if (!consulta.otras_experiencias.isEmpty())
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder bool2 = builder.bool();
            for (Integer index : consulta.otras_experiencias)
            {
                final String data = "c" + index;
                final CompassQuery query = builder.term(VALUE_FIELD, data);
                bool2.addShould(query);
            }
            queries.add(bool2.toQuery());
        }
    }

    private void creaGrado(final Consulta consulta, final Integer igrado, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        final Set<Integer> carreras = getCarreras(consulta);
        for (Integer carrera_add : carreras)
        {
            final String data = "g" + carrera_add + "?j" + igrado + "_k*";
            final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            setcarrerasocup.add(query);
        }
    }

    private void creaIndicador(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.indicador_estudios != null)
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder bool2 = builder.bool();

            String data = "e" + consulta.indicador_estudios.toString();
            CompassQuery query = builder.term(VALUE_FIELD, data);
            bool2.addShould(query);

            data = "j1";
            query = builder.term(VALUE_FIELD, data);
            bool2.addShould(query);

            queries.add(bool2.toQuery());
        }
    }

    private void creaStatus(final Consulta consulta, final Integer istatus, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        final Set<Integer> carreras = getCarreras(consulta);
        for (Integer carrera_add : carreras)
        {
            final String data = "g" + carrera_add + "?j*_k" + istatus;
            final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            setcarrerasocup.add(query);
        }
    }

    private void creaStatus1(final Consulta consulta, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        final Set<Integer> carreras = getCarreras(consulta);
        if (!carreras.isEmpty())
        {
            if (consulta.getGradoEstudios() == null && consulta.getStatusEstudio() == null)
            {
                for (Integer carrera_add : carreras)
                {
                    final String data = "g" + carrera_add + "_*";
                    final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                    setcarrerasocup.add(query);
                }
            }
            else
            {
                for (Integer carrera_add : carreras)
                {
                    final String grado = consulta.getGradoEstudios() == null ? "*" : consulta.getGradoEstudios().toString();
                    final String status = consulta.getGradoEstudios() == null ? "*" : consulta.getStatusEstudio().toString();
                    final String data = "g" + carrera_add + "?j" + grado + "_k" + status;
                    final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                    setcarrerasocup.add(query);
                }
            }
        }
    }

    private void creaStatus2(final Consulta consulta, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        for (Integer igrado : consulta.otros_grados)
        {

            final Set<Integer> carreras = getCarreras(consulta);
            if (carreras.isEmpty())
            {
                if (igrado > 0)
                {
                    final String data = "*j" + igrado + "_*";
                    final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                    setcarrerasocup.add(query);
                }
            }
            else
            {
                if (igrado > 0)
                {
                    for (Integer carrera_add : carreras)
                    {
                        final String data = "g" + carrera_add + "?j" + igrado + "_*";
                        final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                        setcarrerasocup.add(query);
                    }
                }
                else
                {
                    for (Integer carrera_add : carreras)
                    {
                        final String data = "g" + carrera_add + "_*";
                        final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                        setcarrerasocup.add(query);
                    }
                }
            }
        }
    }

    private void creaStatus3(final Consulta consulta, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        for (Integer istatus : consulta.otros_status_estudio)
        {

            final Set<Integer> carreras = getCarreras(consulta);
            if (carreras.isEmpty())
            {
                if (istatus > 0)
                {
                    final String data = "*k" + istatus;
                    final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                    setcarrerasocup.add(query);
                }
            }
            else
            {
                if (istatus > 0)
                {
                    for (Integer carrera_add : carreras)
                    {
                        final String data = "g" + carrera_add + "_*k" + istatus;
                        final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                        setcarrerasocup.add(query);
                    }

                }
                else
                {
                    for (Integer carrera_add : carreras)
                    {
                        final String data = "g" + carrera_add + "_*";
                        final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                        setcarrerasocup.add(query);
                    }
                }
            }
        }
    }

    private void creaStatus4(final Consulta consulta, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        for (Integer igrado : consulta.otros_grados)
        {
            for (Integer istatus : consulta.otros_status_estudio)
            {
                revisaGradoStatus(igrado, istatus, consulta, builder, setcarrerasocup);
            }
        }
    }

    private void createIndex(final Candidato candidato, final CompassSession session) throws CompassException
    {
        final CandidatoIndex index = new CandidatoIndex(candidato);
        session.create(index);
    }

    private void revisaGradoStatus(final Integer igrado, final Integer istatus, final Consulta consulta, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        if (igrado > 0 && istatus > 0)
        {
            creaStatusGrado(consulta, igrado, istatus, builder, setcarrerasocup);
        }
        else if (igrado > 0 && istatus <= 0)
        {
            creaGrado(consulta, igrado, builder, setcarrerasocup);
        }
        else if (igrado <= 0 && istatus > 0)
        {
            creaStatus(consulta, istatus, builder, setcarrerasocup);
        }
        else if (igrado <= 0 && istatus <= 0)
        {
            creaCarrera(consulta, builder, setcarrerasocup);
        }
    }

    private void creaStatusGrado(final Consulta consulta, final Integer igrado, final Integer istatus, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        final Set<Integer> carreras = getCarreras(consulta);
        for (Integer carrera_add : carreras)
        {
            final String data = "g" + carrera_add + "?j" + igrado + "_k" + istatus;
            final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            setcarrerasocup.add(query);
        }
    }

    private String parse(final int valor)
    {
        String parse;
        if (valor > 0)
        {
            parse = String.valueOf(valor);
        }
        else
        {
            parse = "*";
        }
        return parse;
    }

    private void creaNoComodines(final int igrado, final int istatus, final int icarrera, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup)
    {
        final String grado = parse(igrado);
        final String status = parse(istatus);
        final String carrera = parse(icarrera);
        if ("*".equals(grado) && "*".equals(status))
        {
            final String data = "g" + carrera + "_*";
            final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            setcarrerasocup.add(query);
        }
        else if ("*".equals(carrera) && "*".equals(status))
        {
            final String data = "*j" + grado + "_*";
            final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            setcarrerasocup.add(query);
        }
        else if ("*".equals(carrera) && "*".equals(grado))
        {
            final String data = "*k" + status;
            final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            setcarrerasocup.add(query);
        }
        else
        {
            final String data = "g" + carrera + "?j" + grado + "_k" + status;
            final CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            setcarrerasocup.add(query);
        }
    }

    private void creaConsultaCarreras(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> qcarrerasocup)
    {
        if (!consulta.carreras_detectadas.isEmpty())
        {
            final Set<Integer> totalCarreras = new HashSet<Integer>();
            for (Integer iCarrera : consulta.carreras_detectadas)
            {
                totalCarreras.add(iCarrera);
                final Integer[] similares = CompassInfo.getCarrerasSimilaresCatalogo(iCarrera);
                if (similares != null)
                {
                    totalCarreras.addAll(Arrays.asList(similares));
                }
            }
            for (Integer iCarrera : totalCarreras)
            {
                if (CompassInfo.CARRERAS.containsKey(iCarrera))
                {
                    final CompassQuery query = builder.term("CARRERACANDIDATO", iCarrera.toString());
                    qcarrerasocup.add(query);
                }

            }
        }
    }

    private void creaConsultaConocimientoExperiencia(final Conocimiento conocimiento, final CompassQueryBuilder builder, final CompassBooleanQueryBuilder bool2)
    {
        int max = conocimiento.experiencia;
        final List<Integer> adominios = new ArrayList<Integer>();
        adominios.addAll(CompassInfo.EXPERIENCIA.keySet());
        Collections.sort(adominios);
        final int max_temp = adominios.get(adominios.size() - 1);
        if (max_temp > max)
        {
            max = max_temp;
        }
        for (int i = conocimiento.experiencia; i <= max; i++)
        {
            for (String token : CompassInfo.tokenizer(conocimiento.name))
            {
                final StringBuilder sbtoken = new StringBuilder(token);
                CompassInfo.changeCharacters(sbtoken);
                CompassInfo.replaceAbreviaciones(sbtoken);
                CompassInfo.changeBadcharacters(sbtoken, true);
                CompassInfo.trim(sbtoken);
                for (String sinonimo : CompassInfo.getSinonimos(sbtoken.toString()))
                {
                    String data = "h_" + sinonimo + "_" + i;
                    CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                    bool2.addShould(query);

                    data = "i_" + sinonimo + "_" + i;
                    query = builder.wildcard(VALUE_FIELD, data);
                    bool2.addShould(query);
                }
                String data = "h_" + sbtoken + "_" + i;
                CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                bool2.addShould(query);

                data = "i_" + sbtoken + "_" + i;
                query = builder.wildcard(VALUE_FIELD, data);
                bool2.addShould(query);
            }
        }
    }

    private void creaConsultaConocimientoSinExperiencia(final Conocimiento conocimiento, final CompassQueryBuilder builder, final CompassBooleanQueryBuilder bool2)
    {
        for (String token : CompassInfo.tokenizer(conocimiento.name))
        {
            final StringBuilder sbToken = new StringBuilder(token);
            CompassInfo.changeCharacters(sbToken);
            CompassInfo.replaceAbreviaciones(sbToken);
            CompassInfo.changeBadcharacters(sbToken, true);
            CompassInfo.trim(sbToken);
            for (String sinonimo : CompassInfo.getSinonimos(sbToken.toString()))
            {
                String data = "h_" + sinonimo + "_*";
                CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                bool2.addShould(query);

                data = "i_" + sinonimo + "_*";
                query = builder.wildcard(VALUE_FIELD, data);
                bool2.addShould(query);
            }
            String data = "h_" + sbToken + "_*";
            CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            bool2.addShould(query);

            data = "i_" + sbToken + "_*";
            query = builder.wildcard(VALUE_FIELD, data);
            bool2.addShould(query);
        }
    }

    private void creaConsultaHabilidadExperiencia(final Habilidad habilidad, final CompassQueryBuilder builder, final CompassBooleanQueryBuilder bool2)
    {
        int max = habilidad.experiencia;
        final List<Integer> adominios = new ArrayList<Integer>();
        adominios.addAll(CompassInfo.EXPERIENCIA.keySet());
        Collections.sort(adominios);
        final int max_temp = adominios.get(adominios.size() - 1);
        if (max_temp > max)
        {
            max = max_temp;
        }
        for (int i = habilidad.experiencia; i <= max; i++)
        {
            for (String token : CompassInfo.tokenizer(habilidad.name))
            {
                final StringBuilder sbToken = new StringBuilder(token);
                CompassInfo.changeCharacters(sbToken);
                CompassInfo.replaceAbreviaciones(sbToken);
                CompassInfo.changeBadcharacters(sbToken, true);
                CompassInfo.trim(sbToken);
                for (String sinonimo : CompassInfo.getSinonimos(sbToken.toString()))
                {
                    String data = "i_" + sinonimo + "_" + i;
                    CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                    bool2.addShould(query);

                    data = "h_" + sinonimo + "_" + i;
                    query = builder.wildcard(VALUE_FIELD, data);
                    bool2.addShould(query);
                }

                String data = "i_" + sbToken + "_" + i;
                CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                bool2.addShould(query);

                data = "h_" + sbToken + "_" + i;
                query = builder.wildcard(VALUE_FIELD, data);
                bool2.addShould(query);
            }
        }
    }

    private void creaConsultaHabilidadSinExp(final Habilidad habilidad, final CompassQueryBuilder builder, final CompassBooleanQueryBuilder bool2)
    {
        for (String token : CompassInfo.tokenizer(habilidad.name))
        {
            final StringBuilder sbToken = new StringBuilder(token);
            CompassInfo.changeCharacters(sbToken);
            CompassInfo.replaceAbreviaciones(sbToken);
            CompassInfo.changeBadcharacters(sbToken, true);
            CompassInfo.trim(sbToken);
            for (String sinonimo : CompassInfo.getSinonimos(sbToken.toString()))
            {
                String data = "i_" + sinonimo + "_*";
                CompassQuery query = builder.wildcard(VALUE_FIELD, data);
                bool2.addShould(query);

                data = "h_" + sinonimo + "_*";
                query = builder.wildcard(VALUE_FIELD, data);
                bool2.addShould(query);
            }
            String data = "i_" + sbToken + "_*";
            CompassQuery query = builder.wildcard(VALUE_FIELD, data);
            bool2.addShould(query);

            data = "h_" + sbToken + "_*";
            query = builder.wildcard(VALUE_FIELD, data);
            bool2.addShould(query);
        }
    }

    private void creaConsultaIdiomas(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.idiomas != null && !consulta.idiomas.isEmpty())
        {
            final StringBuilder qbuilder = new StringBuilder();
            for (Idioma idioma : consulta.idiomas)
            {
                if (idioma.dominio_id <= 0)
                {
                    final String data = "f" + idioma.id + "_*";
                    qbuilder.append(" ");
                    qbuilder.append(data);
                    qbuilder.append(" ");
                }
                else
                {
                    int max = idioma.dominio_id;
                    final List<Integer> adominios = new ArrayList<Integer>();
                    adominios.addAll(CompassInfo.DOMINIOS.keySet());
                    Collections.sort(adominios);
                    final int max_temp = adominios.get(adominios.size() - 1);
                    if (max_temp > max)
                    {
                        max = max_temp;
                    }
                    for (int i = idioma.dominio_id; i <= max; i++)
                    {
                        final String data = "f" + idioma.id + "_" + i;
                        qbuilder.append(" ");
                        qbuilder.append(data);
                        qbuilder.append(" ");
                    }
                }
            }

            final CompassQuery query = builder.queryString(VALUE_FIELD + ":(" + qbuilder.toString() + ")").useOrDefaultOperator().toQuery();
            queries.add(query);
        }
    }

    private void creaConsultaOcupaciones(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> qcarrerasocup)
    {
        if (!consulta.ocupaciones_detectadas.isEmpty())
        {
            final Set<Integer> totalOcupaciones = new HashSet<Integer>();
            for (Integer iOcupacion : consulta.ocupaciones_detectadas)
            {
                totalOcupaciones.add(iOcupacion);
                final Integer[] similares = CompassInfo.getOcupacionesSimilaresCatalogo(iOcupacion);
                if (similares != null)
                {
                    totalOcupaciones.addAll(Arrays.asList(similares));
                }
            }
            for (Integer iOcupacion : totalOcupaciones)
            {
                if (CompassInfo.OCUPACIONES.containsKey(iOcupacion))
                {
                    final CompassQuery query = builder.term("OCUPACIONCANDIDATO", iOcupacion.toString());
                    qcarrerasocup.add(query);
                }

            }
        }
    }

    private void creaConsultaPalabras(final Set<String> words, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (words != null)
        {
            removeStopWords(words);

            if (!words.isEmpty())
            {
                final CompassQueryBuilder.CompassBooleanQueryBuilder boolword = builder.bool();
                for (String word : words)
                {
                    word = word.replace(',', ' ');
                    if (word.startsWith("\""))
                    {
                        final CompassQuery query = createPraseQuery(word, builder);
                        if (query != null)
                        {
                            boolword.addMust(query);
                        }
                    }
                    else
                    {
                        final CompassQuery query = builder.term("WORDS", word.trim());
                        boolword.addMust(query);
                    }

                }
                queries.add(boolword.toQuery());

            }
        }
    }

    private void creaHorario(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.horario != null)
        {
            consulta.otros_horarios.add(consulta.horario);
        }
        if (consulta.otros_horarios.size() > 0)
        {
            final StringBuilder qbuilder = new StringBuilder();
            for (Integer ihorario : consulta.otros_horarios)
            {
                final String data = "d" + ihorario;
                qbuilder.append(" ");
                qbuilder.append(data);
                qbuilder.append(" ");
            }
            final String queryString = VALUE_FIELD + ":(" + qbuilder.toString().trim() + ")";
            final CompassQuery query = builder.queryString(queryString).useOrDefaultOperator().toQuery();
            queries.add(query);
        }
    }

    private PhraseParser extractPhrases(final StringBuilder search)
    {
        final PhraseParser phraseParser = CompassInfo.getPhraseParser(search);
        int pos = search.indexOf("(");
        while (pos != -1)
        {
            final int pos2 = search.indexOf(")", pos + 1);
            if (pos2 == -1)
            {
                //search = search.substring(0, pos).trim();
                search.setLength(pos);
            }
            else
            {
//                final String temp = search.substring(0, pos).trim();
//                final String temp2 = search.substring(pos2 + 1);
//                search = temp + " " + temp2;
//                search = search.trim();
                search.delete(pos, pos2 + 1);
            }
            pos = search.indexOf("(");
        }
        return phraseParser;
    }

    private void procesaCurrency(final Term term, final Consulta consulta)
    {
        String value = term.getText();
        if (value.length() > 2 && value.toCharArray()[0] == '$')
        {
            value = value.substring(1);
        }
        try
        {
            consulta.setSalario(Double.parseDouble(value));
        }
        catch (NumberFormatException nfe)
        {
            LOG.debug("Error obtenido informaci�n de salario", nfe);
        }
    }

    private void procesaDecimal(final Term term, final int indice, final List<Term> terminos, final Consulta consulta)
    {
        String value = term.getText();
        if (value.length() > 0 && value.toCharArray()[0] == '$')
        {
            value = value.substring(1);
        }

        if ((indice + 1) < terminos.size())
        {
            final Term nextTerm = terminos.get(indice + 1);
            if (AÑOS.equals(nextTerm.getLemma()))
            {
                try
                {
                    final Double num = Double.parseDouble(value);
                    final int años = num.intValue();
                    final Set<Integer> indices = CompassInfo.getExperiencia(años);
                    consulta.otras_experiencias.addAll(indices);
                }
                catch (NumberFormatException nfe)
                {
                    LOG.debug(ERROR, nfe);
                }
            }
            else
            {
                try
                {
                    final Double num = Double.parseDouble(value);
                    consulta.setSalario(num);
                }
                catch (NumberFormatException nfe)
                {
                    LOG.debug(ERROR, nfe);
                }

            }
        }

    }

    private void procesaMonetary(final Term term, final Consulta consulta)
    {
        try
        {
            final String _value = term.getText();
            if (_value.length() > 0 && _value.toCharArray()[0] == '$')
            {
                consulta.setSalario(CURRENCY.parse("$" + _value).doubleValue());
            }
            else
            {
                consulta.setSalario(CURRENCY.parse(_value).doubleValue());
            }
            /*
             * if (!_value.startsWith("$")) { _value = "$" + _value; }
             */

        }
        catch (ParseException pe)
        {
            LOG.debug("Error al convertir moneda", pe);
        }
    }

    private void procesaNumber(final Term term, final int indice, final List<Term> terminos, final Consulta consulta)
    {

        if ((indice + 1) < terminos.size())
        {
            final Term nextTerm = terminos.get(indice + 1);
            try
            {


                final String nextfield = nextTerm.getLemma();
                if (Campos.EXPERIENCIA.toString().equalsIgnoreCase(nextfield))
                {
                    final String value = term.getText();
                    final Long num = Long.parseLong(value);
                    consulta.otras_experiencias.add(num.intValue());
                    addOtrasExperiencias(consulta);
                }
                else if (AÑOS.equalsIgnoreCase(nextfield))
                {
                    final String value = term.getText();
                    final Long num = Long.parseLong(value);
                    final int años = num.intValue();
                    final Set<Integer> indices = CompassInfo.getExperiencia(años);
                    consulta.otras_experiencias.addAll(indices);
                }
                else if ("PESOS".equalsIgnoreCase(nextfield))
                {
                    final String value = term.getText();
                    final Long num = Long.parseLong(value);
                    consulta.setSalario(num.doubleValue());
                }
                else if (isCampo(nextfield))
                {
                    final Campos nextcampo = Campos.valueOf(nextfield);
                    if (nextcampo == Campos.EDAD)
                    {
                        final String value = term.getText();
                        final Long num = Long.parseLong(value);
                        consulta.setEdad(num.intValue());
                    }
                    else if (nextcampo == Campos.EXPERIENCIA)
                    {
                        final String value = term.getText();
                        final Long num = Long.parseLong(value);
                        consulta.otras_experiencias.add(num.intValue());
                        addOtrasExperiencias(consulta);
                    }

                }

            }
            catch (NumberFormatException nfe)
            {
                LOG.debug(ERROR, nfe);
            }
        }
    }

    private void procesaPart1Term(final String data, final IndexInfo info, final Campos campo, final Integer key)
    {
        final StringBuilder data1 = new StringBuilder(data.substring(0, info.pos));
        CompassInfo.cleanTerm(data1);
        CompassInfo.replaceAbreviaciones(data1);
        CompassInfo.changeCharacters(data1);

        CompassInfo.trim(data1);
        //CompassInfo.normaliza(data1);
        TERMS.addTerm(new Term(data1.toString(), campo.toString(), key.intValue(), data1.toString()));
        CompassInfo.normaliza(data1);
        for (String var : CompassInfo.getVariantes(data1.toString()))
        {
            TERMS.addTerm(new Term(var, campo.toString(), key.intValue(), data1.toString()));
        }
    }

    private void procesaPart2Term(final String data, final IndexInfo info, final Campos campo, final Integer key)
    {
        final String temp = data.substring(info.pos + info.len);
        final StringBuilder data2 = new StringBuilder(temp);
        CompassInfo.cleanTerm(data2);
        CompassInfo.changeCharacters(data2);
        CompassInfo.replaceAbreviaciones(data2);
        CompassInfo.trim(data2);
        //CompassInfo.normaliza(data2);
        TERMS.addTerm(new Term(data2.toString(), campo.toString(), key.intValue(), data2.toString()));
        CompassInfo.normaliza(data2);
        for (String var : CompassInfo.getVariantes(data2.toString()))
        {
            TERMS.addTerm(new Term(var, campo.toString(), key.intValue(), data2.toString()));
        }
    }

    private void removeStopWords(final Set<String> words)
    {
        final Set<String> wordsToDelete = new HashSet<String>();
        for (String word : words)
        {
            if (CompassInfo.isStopWords(word))
            {
                wordsToDelete.add(word);
            }
        }
        for (String word : wordsToDelete)
        {
            words.remove(word);
        }
    }

    private void creaConsultaSinonimosRef(final Consulta consulta)
    {
        if (!consulta.sinonimos_ref.isEmpty())
        {
            for (String text : consulta.sinonimos_ref.keySet())
            {
                final Set<String> sinref = consulta.sinonimos_ref.get(text);
                for (String sin_ref : sinref)
                {
                    final EntityTagger tagger = new EntityTagger(TERMS.getSWBTermDictionary());
                    try
                    {
                        tagger.tagEntities(sin_ref);
                        final Iterator<Term> itterms = tagger.listMatchedTerms();
                        final List<Term> terminos = new ArrayList<Term>();
                        while (itterms.hasNext())
                        {
                            final Term term = itterms.next();
                            terminos.add(term);
                        }
                        if (terminos.size() == 1)
                        {
                            final Term term = terminos.get(0);
                            final String field = term.getLemma();
                            if (Campos.OCUPACION.toString().equals(field))
                            {
                                consulta.ocupaciones_detectadas.add(term.getId());
                            }
                            if (Campos.CARRERA_O_ESPECIALIDAD.toString().equals(field))
                            {
                                consulta.carreras_detectadas.add(term.getId());
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        LOG.debug(e);
                    }
                }
            }
        }
    }

    private boolean isGrado(final Consulta consulta, final Term termino)
    {
        boolean isGrado = false;
        if (TERMS.isAmbiguo(termino))
        {
            for (Term term2 : TERMS.getTerms(termino))
            {
                if (term2.getLemma() != null && term2.getLemma().equals(Campos.GRADO_DE_ESTUDIOS.toString()))
                {
                    consulta.otros_grados.add(term2.getId());
                    isGrado = true;
                    break;
                }
            }
        }
        return isGrado;
    }

    private void procesaCarrera(final Term termino, final Consulta consulta)
    {
        if (!isGrado(consulta, termino))
        {
            consulta.carreras_words.add(termino.getOriginal());
            getConsultasRef(termino.getOriginal(), consulta);
            if (TERMS.isAmbiguo(termino))
            {
                llenaCarreras(termino, consulta);
            }
        }
    }

    private void llenaCarreras(final Term termino, final Consulta consulta)
    {
        for (Term ambiguo : TERMS.getTerms(termino))
        {
            if (ambiguo.getLemma().equals(Campos.CARRERA_O_ESPECIALIDAD.toString()))
            {
                consulta.carreras_detectadas.add(ambiguo.getId());
                consulta.carreras_words.add(ambiguo.getOriginal());

            }
            else if (ambiguo.getLemma().equals(REF))
            {
                // busca los sinonimos referenciales
                llenaRef(ambiguo, ambiguo.getOriginal(), consulta);
            }
            else if (ambiguo.getLemma().equals(Campos.OCUPACION.toString()))
            {
                consulta.ocupaciones_detectadas.add(ambiguo.getId());
                consulta.ocupaciones_words.add(ambiguo.getOriginal());
            }
            else if (ambiguo.getLemma().equals(Campos.IDIOMA.toString()))
            {
                consulta.idiomas.add(new Idioma(ambiguo.getId()));
                consulta.carreras_detectadas.remove(termino.getId());
                if (consulta.carrera != null && consulta.carrera.equals(termino.getId()))
                {
                    consulta.carrera = null;
                }
            }
        }
    }

    private void llenaRef(final Term ambiguo, final String text, final Consulta consulta)
    {
        for (List<String> conjunto : CompassInfo.SINREFERENCIALES)
        {
            if (conjunto.contains(ambiguo.getOriginal()))
            {
                final int index = conjunto.indexOf(text) + 1;
                final Set<String> ref = new HashSet<String>();
                for (int j = index; j < conjunto.size(); j++)
                {
                    final String simref = conjunto.get(j);
                    //consulta.ocupaciones_words.add(simref);
                    ref.add(simref);
                }
                if (!ref.isEmpty())
                {
                    if (consulta.sinonimos_ref.containsKey(text))
                    {
                        ref.addAll(consulta.sinonimos_ref.get(text));
                        consulta.sinonimos_ref.put(text, ref);
                    }
                    else
                    {
                        consulta.sinonimos_ref.put(text, ref);
                    }

                }
            }
        }
    }

    private void procesaExperiencia(final int indice, final List<Term> terminos, final Consulta consulta, final Term termino)
    {
        if (indice > 0)
        {
            final Term prev = terminos.get(indice - 1);
            if (prev.getLemma() != null && consulta.experiencia_total == null)
            {
                consulta.otras_experiencias.add(termino.getId());
                addOtrasExperiencias(consulta);
            }
        }
        else
        {
            consulta.otras_experiencias.add(termino.getId());
            addOtrasExperiencias(consulta);
        }
    }

    private void procesaGrado(final Consulta consulta, final Term termino)
    {

        consulta.setGradoEstudios(termino.getId());
        for (Term term2 : TERMS.getTerms(termino))
        {
            if (term2.getLemma() != null && term2.getLemma().equals(Campos.GRADO_DE_ESTUDIOS.toString()))
            {
                consulta.setGradoEstudios(term2.getId());

            }
            if (term2.getLemma() != null && term2.getLemma().equals(Campos.OCUPACION.toString()))
            {
                consulta.setGradoEstudios(null);
                consulta.otros_grados.remove(term2.getId());
                consulta.ocupaciones_detectadas.add(term2.getId());
                return;
            }
        }
    }

    private void procesaHorario(final Consulta consulta, final Term termino)
    {
        if (consulta.horario == null)
        {
            consulta.horario = termino.getId();
        }
        else
        {
            consulta.otros_horarios.add(termino.getId());
        }

        if (TERMS.isAmbiguo(termino))
        {
            for (Term term2 : TERMS.getTerms(termino))
            {
                if (term2.getLemma() != null && term2.getLemma().equals("HORARIO_DE_EMPLEO"))
                {
                    consulta.otros_horarios.add(term2.getId());
                }
            }
        }
    }

    private void procesaIdioma(final Term termino, final int indice, final List<Term> terminos, final Consulta consulta)
    {
        if ((indice + 1) < terminos.size())
        {
            final Term next = terminos.get(indice + 1);
            if (next.getLemma() != null && next.getLemma().equals(Campos.DOMINIO.toString()))
            {
                if (TERMS.isAmbiguo(next))
                {
                    for (Term other : TERMS.getTerms(next))
                    {
                        if (other.getLemma().equals(Campos.DOMINIO.toString()))
                        {
                            final Integer idIdioma = termino.getId();
                            final Idioma info = new Idioma(idIdioma.intValue(), other.getId());
                            consulta.idiomas.add(info);
                        }

                    }
                }
                else
                {
                    final Integer idIdioma = termino.getId();
                    final Idioma info = new Idioma(idIdioma.intValue());
                    consulta.idiomas.add(info);
                }
            }
            else
            {
                final Integer idIdioma = termino.getId();
                final Idioma info = new Idioma(idIdioma.intValue());
                consulta.idiomas.add(info);
            }
        }
        else
        {
            final Integer idIdioma = termino.getId();
            final Idioma info = new Idioma(idIdioma.intValue());
            consulta.idiomas.add(info);
        }
    }

    private void procesaOcupacion(final Term termino, final Consulta consulta)
    {
        getConsultasRef(termino.getOriginal(), consulta);

        consulta.ocupaciones_words.add(termino.getOriginal());
        consulta.ocupaciones_detectadas.add(termino.getId());
        if (TERMS.isAmbiguo(termino))
        {
            for (Term term2 : TERMS.getTerms(termino))
            {
                if (term2.getLemma().equals(Campos.OCUPACION.toString()))
                {
                    consulta.ocupaciones_detectadas.add(term2.getId());
                    consulta.ocupaciones_words.add(term2.getOriginal());
                }
                else if (term2.getLemma().equals(REF))
                {
                    // busca los sinonimos referenciales
                    llenaRef(term2, term2.getOriginal(), consulta);
                }
                else if (term2.getLemma().equals(Campos.CARRERA_O_ESPECIALIDAD.toString()))
                {
                    consulta.carreras_detectadas.add(term2.getId());
                    consulta.carreras_words.add(term2.getOriginal());
                }
            }
        }
    }

    private void procesaPartTerm(final String data, final IndexInfo info, final Integer key, final Campos campo)
    {
        procesaPart1Term(data, info, campo, key);
        procesaPart2Term(data, info, campo, key);
    }

    private void procesaRefenciable(final Term term, final Set<String> words, final Consulta consulta)
    {
        for (Term term2 : TERMS.getTerms(term))
        {
            if (Campos.OCUPACION.toString().equals(term2.getLemma()))
            {
                consulta.ocupaciones_detectadas.add(term2.getId());
            }
            if (Campos.CARRERA_O_ESPECIALIDAD.toString().equals(term2.getLemma()))
            {
                consulta.carreras_detectadas.add(term2.getId());
            }
            if (Campos.GRADO_DE_ESTUDIOS.toString().equals(term2.getLemma()))
            {
                consulta.setGradoEstudios(term2.getId());
                consulta.otros_grados.add(term2.getId());
                return;
            }
        }
        for (List<String> conjunto : CompassInfo.SINREFERENCIALES)
        {
            if (conjunto.contains(term.getOriginal()))
            {
                words.add(term.getOriginal());
                final int index = conjunto.indexOf(term.getOriginal()) + 1;
                final Set<String> ref = new HashSet<String>();
                for (int j = index; j < conjunto.size(); j++)
                {
                    final String simref = conjunto.get(j);
                    ref.add(simref);

                }
                if (!ref.isEmpty())
                {
                    final String key = term.getOriginal().trim();
                    if (consulta.sinonimos_ref.get(key) == null)
                    {
                        consulta.sinonimos_ref.put(key, ref);
                    }
                    else
                    {
                        final Set<String> sim_ref = consulta.sinonimos_ref.get(key);
                        sim_ref.addAll(ref);
                        consulta.sinonimos_ref.put(key, sim_ref);

                    }
                }
            }
        }
    }

    private void procesaStatus(final Consulta consulta, final Term termino)
    {

        consulta.setStatusEstudio(termino.getId());


        if (TERMS.isAmbiguo(termino))
        {
            for (Term term : TERMS.getTerms(termino))
            {
                if (term.getLemma().equals(Campos.HORARIO_DE_EMPLEO.toString()))
                {
                    consulta.otros_horarios.add(term.getId());
                }
            }
        }
    }

    private void saveIndex(final Candidato candidato, final CompassSession session) throws CompassException
    {
        final CandidatoIndex index = new CandidatoIndex(candidato);
        session.save(index);
    }

    private void words(final Term term, final Set<String> words)
    {
        final String word = term.getText();
        if (word != null && !CompassInfo.isStopWords(word))
        {
            words.addAll(Arrays.asList(CompassInfo.tokenizer(word)));
        }
    }

    private void copyGrado(final Map<Integer, String> source)
    {
        for (Integer key : source.keySet())
        {
            String data = source.get(key).toLowerCase();
            if (data.indexOf(SEC) != -1)
            {
                data = data.replace(SEC, "secundaria");
            }

            final IndexInfo info = CompassInfo.indexOf(data);
            if (info.pos == -1)
            {
                procesaTerm(data, key, Campos.GRADO_DE_ESTUDIOS);
            }
            else
            {
                procesaPartTerm(data, info, key, Campos.GRADO_DE_ESTUDIOS);
            }
        }
        for (Integer key : source.keySet())
        {
            String data = source.get(key).toLowerCase();
            if (data.indexOf(SEC) != -1)
            {
                data = data.replace(SEC, "secundaria");
            }
            final IndexInfo info = CompassInfo.indexOf(data);
            if (info.pos == -1)
            {
                procesaTerm(data, key, Campos.GRADO_DE_ESTUDIOS);
            }
            else
            {
                procesaPartTerm(data, info, key, Campos.GRADO_DE_ESTUDIOS);
            }
        }
    }

    private void procesaTerm(final String term, final Integer key, final Campos campo)
    {
        final StringBuilder stringterm = new StringBuilder(term);
        CompassInfo.cleanTerm(stringterm);
        CompassInfo.replaceAbreviaciones(stringterm);
        CompassInfo.changeCharacters(stringterm);
        CompassInfo.trim(stringterm);

        TERMS.addTerm(new Term(stringterm.toString(), campo.toString(), key.intValue(), stringterm.toString()));
        CompassInfo.normaliza(stringterm);
        for (String var : CompassInfo.getVariantes(stringterm.toString()))
        {
            TERMS.addTerm(new Term(var, campo.toString(), key.intValue(), stringterm.toString()));
        }
    }

    private void copyDataStatus(final Map<Integer, String> source)
    {
        for (Integer key : source.keySet())
        {
            final String data = source.get(key).toLowerCase();
            final IndexInfo info = CompassInfo.indexOf(data);
            if (info.pos == -1)
            {
                procesaTerm(data, key, Campos.STATUS_ACADEMICO);
                /*
                 * data = CompassInfo.cleanTerm(data); data =
                 * CompassInfo.changeCharacters(data); if
                 * (!"".equals(data.trim())) { TERMS.addTerm(new Term(data,
                 * Campos.STATUS_ACADEMICO.toString(), key.intValue(), data)); }
                 */

            }
            else
            {
                procesaPartTerm(data, info, key, Campos.STATUS_ACADEMICO);
                /*
                 * String data1 = data.substring(0, pos); data1 =
                 * CompassInfo.cleanTerm(data1); String data2 =
                 * data.substring(pos + 3); data2 =
                 * CompassInfo.cleanTerm(data2); data1 =
                 * CompassInfo.changeCharacters(data1); if
                 * (!"".equals(data1.trim())) { TERMS.addTerm(new Term(data1,
                 * Campos.STATUS_ACADEMICO.toString(), key.intValue(), data1));
                 * } data2 = CompassInfo.changeCharacters(data2);
                 * TERMS.addTerm(new Term(data2,
                 * Campos.STATUS_ACADEMICO.toString(), key.intValue(), data2));
                 */


            }

        }
    }

    /**
     * Copia los catalogo, convirtiéndolos en términos
     *
     * @param target Catalogo origen
     * @param source Catalogo destino
     */
    private void copyData(final Map<Integer, String> target, final Map<Integer, String> source)
    {

        for (Integer key : source.keySet())
        {
            final StringBuilder text = new StringBuilder(source.get(key));
            CompassInfo.cleanTerm(text);
            CompassInfo.changeCharacters(text);
            CompassInfo.trim(text);
            if (text.length() > 0)
            {
                target.put(key, text.toString());
            }
        }
    }

    private void terminosRef()
    {
        for (String simref : CompassInfo.TOKENSREF)
        {
            TERMS.add(new Term(simref, REF, -1, simref), false, false);
        }
    }

    private void terminosCarrerasSimp()
    {
        for (Integer icarrera : CompassInfo.CARRERASCAT.keySet())
        {
            final String ncarrera = CompassInfo.CARRERASCAT.get(icarrera);
            String carrera = CompassInfo.CARRERASCAT.get(icarrera).replace('.', ' ').trim();
            final StringBuilder sbCarrera = new StringBuilder(carrera);
            CompassInfo.cleanTerm(sbCarrera);
            CompassInfo.changeCharacters(sbCarrera);
            carrera = sbCarrera.toString();
            Term term = new Term(carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera.intValue(), ncarrera);
            TERMS.addTerm(term);
            if (carrera != null)
            {
                for (String var : CompassInfo.getVariantes(carrera))
                {
                    term = new Term(var, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera, ncarrera);
                    TERMS.add(term, true, true);
                }
            }
        }
    }

    private void terminosSimpOcupaciones()
    {
        for (Integer iocupacion : CompassInfo.OCUPACIONESCAT.keySet())
        {
            String ocupacion = CompassInfo.OCUPACIONESCAT.get(iocupacion).replace('.', ' ').trim();
            final String nocupacion = CompassInfo.OCUPACIONESCAT.get(iocupacion);
            final StringBuilder sbocupacion = new StringBuilder(ocupacion);
            CompassInfo.cleanTerm(sbocupacion);
            CompassInfo.changeCharacters(sbocupacion);
            ocupacion = sbocupacion.toString();
            Term term = new Term(ocupacion, Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
            TERMS.addTerm(term);
            if (ocupacion != null)
            {
                for (String var : CompassInfo.getVariantes(ocupacion))
                {
                    term = new Term(var, Campos.OCUPACION.toString(), iocupacion, nocupacion);
                    TERMS.add(term, true, true);
                }
            }
        }
    }

    private void terminosCarreras()
    {
        for (Integer icarrera : CompassInfo.CARRERASCAT.keySet())
        {
            String carrera = CompassInfo.CARRERAS.get(icarrera).toLowerCase().replace('.', ' ').trim();
            final String ncarrera = carrera;
            StringBuilder sbcarrera = new StringBuilder(carrera);
            CompassInfo.changeCharacters(sbcarrera);
            carrera = sbcarrera.toString();
            Term term = new Term(carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera.intValue(), ncarrera);
            TERMS.addTerm(term);
            sbcarrera = new StringBuilder(carrera);
            CompassInfo.extractParentesis(sbcarrera);
            final String _carrera = sbcarrera.toString();
            if (!carrera.equals(_carrera))
            {
                term = new Term(_carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera, ncarrera);
                TERMS.addTerm(term);
            }
        }
    }

    private void terminosOcupaciones()
    {
        for (Integer iocupacion : CompassInfo.OCUPACIONESCAT.keySet())
        {
            String ocupacion = CompassInfo.OCUPACIONES.get(iocupacion).toLowerCase().replace('.', ' ').trim();
            final String nocupacion = ocupacion;
            StringBuilder sbocupacion = new StringBuilder(ocupacion);
            CompassInfo.changeCharacters(sbocupacion);
            ocupacion = sbocupacion.toString();
            Term term = new Term(ocupacion, Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
            TERMS.addTerm(term);
            sbocupacion = new StringBuilder(ocupacion);
            CompassInfo.extractParentesis(sbocupacion);
            final String _ocupacion = sbocupacion.toString();
            if (!ocupacion.equals(_ocupacion))
            {
                term = new Term(_ocupacion, Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
                TERMS.addTerm(term);
            }
        }
    }

    private void terminosHorario()
    {
        for (Integer ihorario : CompassInfo.HORARIO.keySet())
        {
            final String value = CompassInfo.HORARIO.get(ihorario).toLowerCase();
            Term term = new Term(value.trim(), Campos.HORARIO_DE_EMPLEO.toString(), ihorario.intValue(), value);
            TERMS.addTerm(term);
            if (value != null)
            {
                for (String var : CompassInfo.getVariantes(value))
                {
                    term = new Term(var, Campos.HORARIO_DE_EMPLEO.toString(), ihorario, value);
                    TERMS.add(term, true, true);
                }
            }
        }
    }

    private void terminosDominios()
    {
        for (Integer idominio : CompassInfo.DOMINIOS.keySet())
        {
            final String value = CompassInfo.DOMINIOS.get(idominio);
            final Term term = new Term(value, Campos.DOMINIO.toString(), idominio.intValue(), value);
            TERMS.addTerm(term);
            if (value != null)
            {
                for (String var : CompassInfo.getVariantes(value))
                {
                    TERMS.add(new Term(var, Campos.DOMINIO.toString(), idominio, value), true, true);
                }
            }
        }
    }

    private void terminosIdiomas()
    {
        for (Integer iidioma : CompassInfo.IDIOMAS.keySet())
        {
            final String value = CompassInfo.IDIOMAS.get(iidioma);
            final Term term = new Term(value, Campos.IDIOMA.toString(), iidioma.intValue(), value);
            TERMS.addTerm(term);
            if (value != null)
            {
                for (String var : CompassInfo.getVariantes(value))
                {
                    TERMS.add(new Term(var, Campos.IDIOMA.toString(), iidioma, value), true, true);
                }
            }
        }
    }

    /**
     * Procesa los catálogos y los agrega como términos
     */
    private void processTerms()
    {
        LOG.info("Iniciando agregar terminos...");
        final long tini = System.currentTimeMillis();
        terminosIdiomas();
        terminosDominios();
        terminosHorario();
        terminosOcupaciones();
        terminosCarreras();
        terminosSimpOcupaciones();
        terminosCarrerasSimp();
        terminosRef();
        final long tfin = System.currentTimeMillis();
        final long dif = tfin - tini;
        LOG.info("Fin de cargar terminos tiempo: " + dif + " ms");
    }
    /*
     * private void processTerms() { final Thread hilo = new Thread(new
     * Runnable() {
     *
     * private static final String REF = "REF";
     *
     * @Override public void run() {
     *
     * LOG.info("Iniciando agregar terminos..."); long tini =
     * System.currentTimeMillis(); terminosIdiomas(); terminosDominios();
     * terminosHorario(); terminosOcupaciones(); terminosCarreras();
     * terminosSimpOcupaciones(); terminosCarrerasSimp(); terminosRef(); long
     * tfin = System.currentTimeMillis(); long dif = tfin - tini; LOG.info("Fin
     * de cargar terminos tiempo: " + dif + " ms");
     *
     *
     *
     * }
     *
     * private void terminosRef() { for (String simref : CompassInfo.TOKENSREF)
     * { TERMS.add(new Term(simref, REF, -1, simref), false, false); } }
     *
     * private void terminosCarrerasSimp() { for (Integer icarrera :
     * CompassInfo.CARRERASCAT.keySet()) { String ncarrera =
     * CompassInfo.CARRERASCAT.get(icarrera); String carrera =
     * CompassInfo.CARRERASCAT.get(icarrera).replace('.', ' ').trim();
     * StringBuilder sbCarrera = new StringBuilder(carrera);
     * CompassInfo.cleanTerm(sbCarrera);
     * CompassInfo.changeCharacters(sbCarrera); carrera = sbCarrera.toString();
     * Term term = new Term(carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(),
     * icarrera.intValue(), ncarrera); TERMS.addTerm(term); if (carrera != null)
     * { for (String var : CompassInfo.getVariantes(carrera)) { term = new
     * Term(var, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera, ncarrera);
     * TERMS.add(term, true, true); } } } }
     *
     * private void terminosSimpOcupaciones() { for (Integer iocupacion :
     * CompassInfo.OCUPACIONESCAT.keySet()) { String ocupacion =
     * CompassInfo.OCUPACIONESCAT.get(iocupacion).replace('.', ' ').trim();
     * String nocupacion = CompassInfo.OCUPACIONESCAT.get(iocupacion);
     * StringBuilder sbocupacion = new StringBuilder(ocupacion);
     * CompassInfo.cleanTerm(sbocupacion);
     * CompassInfo.changeCharacters(sbocupacion); ocupacion =
     * sbocupacion.toString(); Term term = new Term(ocupacion,
     * Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
     * TERMS.addTerm(term); if (ocupacion != null) { for (String var :
     * CompassInfo.getVariantes(ocupacion)) { term = new Term(var,
     * Campos.OCUPACION.toString(), iocupacion, nocupacion); TERMS.add(term,
     * true, true); } } } }
     *
     * private void terminosCarreras() { for (Integer icarrera :
     * CompassInfo.CARRERASCAT.keySet()) { String carrera =
     * CompassInfo.CARRERAS.get(icarrera).toLowerCase().replace('.', '
     * ').trim(); String ncarrera = carrera; StringBuilder sbcarrera = new
     * StringBuilder(carrera); CompassInfo.changeCharacters(sbcarrera); carrera
     * = sbcarrera.toString(); Term term = new Term(carrera,
     * Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera.intValue(), ncarrera);
     * TERMS.addTerm(term); sbcarrera = new StringBuilder(carrera);
     * CompassInfo.extractParentesis(sbcarrera); String _carrera =
     * sbcarrera.toString(); if (!carrera.equals(_carrera)) { term = new
     * Term(_carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera,
     * ncarrera); TERMS.addTerm(term); } } }
     *
     * private void terminosOcupaciones() { for (Integer iocupacion :
     * CompassInfo.OCUPACIONESCAT.keySet()) { String ocupacion =
     * CompassInfo.OCUPACIONES.get(iocupacion).toLowerCase().replace('.', '
     * ').trim(); String nocupacion = ocupacion; StringBuilder sbocupacion = new
     * StringBuilder(ocupacion); CompassInfo.changeCharacters(sbocupacion);
     * ocupacion = sbocupacion.toString(); Term term = new Term(ocupacion,
     * Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
     * TERMS.addTerm(term); sbocupacion = new StringBuilder(ocupacion);
     * CompassInfo.extractParentesis(sbocupacion); String _ocupacion =
     * sbocupacion.toString(); if (!ocupacion.equals(_ocupacion)) { term = new
     * Term(_ocupacion, Campos.OCUPACION.toString(), iocupacion.intValue(),
     * nocupacion); TERMS.addTerm(term); } } }
     *
     * private void terminosHorario() { for (Integer ihorario :
     * CompassInfo.HORARIO.keySet()) { String value =
     * CompassInfo.HORARIO.get(ihorario).toLowerCase(); Term term = new
     * Term(value.trim(), Campos.HORARIO_DE_EMPLEO.toString(),
     * ihorario.intValue(), value); TERMS.addTerm(term); if (value != null) {
     * for (String var : CompassInfo.getVariantes(value)) { term = new Term(var,
     * Campos.HORARIO_DE_EMPLEO.toString(), ihorario, value); TERMS.add(term,
     * true, true); } } } }
     *
     * private void terminosDominios() { for (Integer idominio :
     * CompassInfo.DOMINIOS.keySet()) { String value =
     * CompassInfo.DOMINIOS.get(idominio); Term term = new Term(value,
     * Campos.DOMINIO.toString(), idominio.intValue(), value);
     * TERMS.addTerm(term); if (value != null) { for (String var :
     * CompassInfo.getVariantes(value)) { TERMS.add(new Term(var,
     * Campos.DOMINIO.toString(), idominio, value), true, true); } } } }
     *
     * private void terminosIdiomas() { for (Integer iidioma :
     * CompassInfo.IDIOMAS.keySet()) { String value =
     * CompassInfo.IDIOMAS.get(iidioma); Term term = new Term(value,
     * Campos.IDIOMA.toString(), iidioma.intValue(), value);
     * TERMS.addTerm(term); if (value != null) { for (String var :
     * CompassInfo.getVariantes(value)) { TERMS.add(new Term(var,
     * Campos.IDIOMA.toString(), iidioma, value), true, true); } } } } });
     * hilo.start();
     *
     *
     *
     * }
     */

    /**
     * Constructor por defecto
     */
    public CandidatoSearch()
    {
    }

    public synchronized void save(final List<Candidato> candidatos) throws BusquedaException
    {
        final CompassSession session = LUCENE.getCompass().openSession();
        CompassTransaction transaction = null;
        try
        {
            transaction = session.beginTransaction();
            for (Candidato candidato : candidatos)
            {
                saveIndex(candidato, session);
            }
            transaction.commit();
            LUCENE.getCompass().getSearchEngineIndexManager().notifyAllToClearCache();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            throw new BusquedaException(e);
        }
        finally
        {

            if (!session.isClosed())
            {
                session.close();
            }
        }
    }

    public synchronized void create(final List<Candidato> candidatos) throws BusquedaException
    {
        final CompassSession session = LUCENE.getCompass().openSession();
        session.getSettings().setSetting(LuceneEnvironment.Transaction.Processor.TYPE,
                LuceneEnvironment.Transaction.Processor.Lucene.NAME);
        CompassTransaction transaction = null;
        try
        {
            transaction = session.beginTransaction();
            for (Candidato candidato : candidatos)
            {
                createIndex(candidato, session);
            }
            transaction.commit();
            LUCENE.getCompass().getSearchEngineIndexManager().notifyAllToClearCache();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            throw new BusquedaException(e);
        }
        finally
        {

            if (!session.isClosed())
            {
                session.close();
            }
        }
    }

    /**
     * Guardar un candidato en bitacora
     *
     * @param candidato Candidato a almacenar
     * @param sync Requiere sincronización?
     * @throws BusquedaException En caso de no poder alacenar el candidato
     */
    /**
     * Guardar un candidato en bitacora
     *
     * @param candidato Candidato a almacenar
     * @throws BusquedaException En caso de no poder almacenar el candidato
     */
    public synchronized void save(final Candidato candidato) throws BusquedaException
    {
        try
        {
            tryUpdateCandidato(new CandidatoIndex(candidato));
        }
        catch (Exception e)
        {
            throw new BusquedaException(e);
        }
    }

    private void remove(final CandidatoIndex index) throws BusquedaException
    {
        try
        {
            tryDeleteCandidato(index);
        }
        catch (Exception e)
        {
            throw new BusquedaException(e);
        }
    }

    /**
     * Elimina un candidato, lo guarda en bitácora y posteriormente en
     * eliminado, sin sincronización
     *
     * @param idcandidato Id del candidato
     * @throws BusquedaException En caso de no poder almacenar el candidato
     */
    public synchronized void remove(final int idcandidato) throws BusquedaException
    {
        try
        {
            tryDeleteCandidato(new CandidatoIndex(idcandidato));
        }
        catch (Exception e)
        {
            throw new BusquedaException(e);
        }
    }
    /*
     * public synchronized void remove(int id) throws BusquedaException { if (id
     * <= 0) { return; } Candidato candidato = new Candidato(id);
     * saveTask(candidato, ModeIndexLog.REMOVE);
     *
     * }
     */

    /**
     * Elimina un candidato, lo guarda en bitacora y posteriormente en
     * eliminado, sin soncronización
     *
     * @param candidato Candidato a eliminar
     * @throws BusquedaException En caso de no poder almacenar el candidato
     */
    /*
     * public synchronized void remove(Candidato candidato) throws
     * BusquedaException { if (candidato.id <= 0) { return; }
     * saveTask(candidato, ModeIndexLog.REMOVE); // CandidatoIndex index = new
     * CandidatoIndex(candidato); // index.deleted = true; //
     * server.send(index);
     *
     * }
     */

    /*
     * public synchronized void remove(CandidatoIndex candidato) throws
     * BusquedaException { if (candidato.id <= 0) { return; } try {
     * tryDeleteCandidato(candidato); } catch (Exception e) { throw new
     * BusquedaException(e); }
     *
     *
     *
     * }
     */
    /**
     * Procesa un termino en base a los campos definidos
     *
     * @param campo Campo detectado
     * @param consulta Consulta a generar
     * @param terminos Lista de términos encontrados
     * @param indice Índice en la lista del termino a procesar
     */
    private void procesaCampo(final Campos campo, final Consulta consulta, final List<Term> terminos, final int indice)
    {

        if (campo == Campos.HORARIO_DE_EMPLEO)
        {
            final Term termino = terminos.get(indice);
            procesaHorario(consulta, termino);
        }
        else if (campo == Campos.CARRERA_O_ESPECIALIDAD)
        {
            final Term termino = terminos.get(indice);
            procesaCarrera(termino, consulta);
        }
        else if (campo == Campos.OCUPACION)
        {
            final Term termino = terminos.get(indice);
            procesaOcupacion(termino, consulta);
        }
        else if (campo == Campos.STATUS_ACADEMICO)
        {
            final Term termino = terminos.get(indice);
            procesaStatus(consulta, termino);
        }
        else if (campo == Campos.EXPERIENCIA)
        {
            final Term termino = terminos.get(indice);
            procesaExperiencia(indice, terminos, consulta, termino);
        }
        else if (campo == Campos.GRADO_DE_ESTUDIOS)
        {
            final Term termino = terminos.get(indice);
            procesaGrado(consulta, termino);

        }
        else if (campo == Campos.IDIOMA)
        {
            final Term termino = terminos.get(indice);
            procesaIdioma(termino, indice, terminos, consulta);
        }
    }

    /**
     * Detecta si un valor es un texto de campo
     *
     * @param value Valor a probar
     * @return True, si corresponde a un nombre de un campos, False caso
     * contrario
     */
    private boolean isCampo(final String value)
    {
        boolean resultado = false;
        for (Campos campo : Campos.values())
        {
            if (campo.toString().equals(value))
            {
                resultado = true;
                break;
            }
        }
        return resultado;

    }

    /**
     * Procesa términos
     *
     * @param term Términos a procesar
     * @param consulta Consulta a generar
     * @param indice Índice del termino
     * @param terminos Lista de términos
     * @param words Lista de palabras a generar en caso de que un termino no sea
     * un termino reconocido
     */
    private void procesaTermino(final Consulta consulta, final Term term, final int indice, final List<Term> terminos, final Set<String> words)
    {
        final String field = term.getLemma();
        if (field == null)
        {
            words(term, words);
        }
        else if (field.equals(REF))
        {
            procesaRefenciable(term, words, consulta);
        }
        else if (isCampo(field))
        {
            final Campos campo = Campos.valueOf(field);
            procesaCampo(campo, consulta, terminos, indice);
        }
        else if ("SIN".equals(field))
        {
            consulta.indicador_estudios = false;
        }
        else if ("CURR".equals(field)) // currency
        {
            procesaCurrency(term, consulta);
        }
        else if ("MON".equals(field))
        {
            procesaMonetary(term, consulta);

        }
        else if ("DISP".equals(field))
        {
            consulta.disponibilidad = true;
        }
        else if ("RAD".equals(field))
        {
            consulta.disponibilidad_viajar_ciudad = true;
        }
        else if ("NUM".equals(field)) // numero decimal
        {
            procesaNumber(term, indice, terminos, consulta);
        }
        else if ("DEC".equals(field)) // numero decimal
        {
            procesaDecimal(term, indice, terminos, consulta);
        }

    }

    public PhraseParser prepareSearh(final StringBuilder search)
    {
        CompassInfo.toLowerCase(search);
        CompassInfo.trim(search);
        final PhraseParser phraseParser = extractPhrases(search);
        CompassInfo.replace(search, " con conocimientos en", " ");
        CompassInfo.replace(search, " con conocimiento en", " ");
        CompassInfo.normalizeMonedas(search);
        CompassInfo.changeCharacters(search);
        replacePuntuationCharacters(search);
        CompassInfo.replaceAbreviaciones(search);
        CompassInfo.changeBadcharacters(search, false);
        CompassInfo.normaliza(search);
        CompassInfo.trim(search);
        return phraseParser;
    }
    public Result search(final String textSearch, Integer estado, Integer municpio, String discapacidad) throws BusquedaException
    {
        return search(textSearch, estado, municpio, discapacidad, null);
    }
    public Result search(final String textSearch, Integer estado, Integer municpio, String discapacidad,Integer fuente) throws BusquedaException
    {
        String search = textSearch;
        final String original = search;

        final Consulta consulta = new Consulta(search);
        consulta.originalText = original;

        if (estado == null && municpio == null && discapacidad==null && fuente==null)
        {
            if (search == null || search.trim().isEmpty())
            {
                LOG.warn("Cadena de busqueda nula o vácia");
                return new Result(consulta);
            }
        }
        search = search.trim();
        if (estado == null && municpio == null && discapacidad==null && fuente==null)
        {
            if (search.length() <= 1)
            {
                return new Result(consulta);
            }
        }
        final long tini = System.currentTimeMillis();
        final StringBuilder sbSearch = new StringBuilder(search);
        CompassInfo.normalizeMonedas(sbSearch);
        final PhraseParser phraseParser = prepareSearh(sbSearch);
        search = sbSearch.toString();


        consulta.text = search;
        final EntityTagger tagger = new EntityTagger(TERMS.getSWBTermDictionary());
        try
        {
            final Set<String> words = new HashSet<String>();
            final String text = tagger.tagEntities(search);
            consulta.tagedText = text;
            final Iterator<Term> itTerminos = tagger.listMatchedTerms();
            final List<Term> terminos = new ArrayList<Term>();
            while (itTerminos.hasNext())
            {
                final Term term = itTerminos.next();
                terminos.add(term);
            }
            for (int i = 0; i < terminos.size(); i++)
            {

                final Term term = terminos.get(i);
                procesaTermino(consulta, term, i, terminos, words);
            }
            words.addAll(phraseParser.getPrases());
            consulta.edo = estado;
            consulta.municipio = municpio;
            consulta.discapacidad = discapacidad;            
            consulta.setFuente(fuente);            
            final Result result = search(consulta, words);
            if (LOG.isTraceEnabled())
            {
                final long tfin = System.currentTimeMillis();
                final long dif = (tfin - tini);
                LOG.trace("Tiempo candidato: " + dif + " ms resultados: " + result.getCount() + " search: " + search + " cadena original:" + consulta.originalText);
            }
            return result;

        }
        catch (IOException ex)
        {
            LOG.error(ERROR, ex);
            throw new BusquedaException(ex);
        }
    }

    public Result search(final String textSearch, Integer estado, Integer municpio) throws BusquedaException
    {
        return search(textSearch, estado, municpio, null);
    }

    /**
     * Función de detección de términos y generación de consulta de búsqueda
     *
     * @param search Texto a buscar
     * @return Conjunto de resultados encontrados
     * @throws BusquedaException Error en caso de que suceda un problema con los
     * índices
     */
    public Result search(final String textSearch) throws BusquedaException
    {
        return search(textSearch, null, null);
    }

    /**
     * Regresa true en caso de que una palabra se encuentre en un conjunto de
     * palabras
     *
     * @param word Palabra a buscar
     * @param values Conjunto de palabras
     * @return rue en caso de que una palabra se encuentre en un conjunto de
     * palabras, false caso contrario
     */
    private static boolean isIn(final String word, final String[] values)
    {
        boolean isIn = false;
        for (String value : values)
        {
            if (word.equals(value))
            {
                isIn = true;
                break;
            }
        }
        return isIn;
    }

    /**
     * Verifica la compatibilidad de dos palabras considerando sus sinónimos
     *
     * @param source palabra a probar
     * @param target palabra de referencia
     * @return True si son compatibles, false caso contrario
     */
    public static boolean checkCompatibility(final String source, final String target)
    {
        final StringBuilder text = new StringBuilder(source);
        CompassInfo.toLowerCase(text);
        final StringBuilder textTarget = new StringBuilder(target);
        CompassInfo.toLowerCase(textTarget);
        if (text == null || textTarget == null || text.toString().isEmpty() || textTarget.toString().isEmpty())
        {
            return false;
        }
        else
        {
            String[] stoens = CompassInfo.tokenizer(text.toString().trim());
            String[] ttoens = CompassInfo.tokenizer(textTarget.toString().trim());
            final HashSet<String> values = new HashSet<String>();
            for (String sourceToken : stoens)
            {
                if (!CompassInfo.isStopWords(sourceToken))
                {
                    if (sourceToken.indexOf('.') == -1)
                    {
                        final StringBuilder sbToken = new StringBuilder(sourceToken);
                        CompassInfo.changeCharacters(sbToken);
                        values.add(sbToken.toString());
                    }
                    else
                    {
                        boolean found = false;
                        final Map<String, Set<String>> abreviaciones = CompassInfo.getAbreviaciones();
                        for (String key : abreviaciones.keySet())
                        {
                            if (abreviaciones.get(key).contains(sourceToken))
                            {
                                final StringBuilder sbToken = new StringBuilder(key);
                                CompassInfo.changeCharacters(sbToken);
                                values.add(sbToken.toString());
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                        {
                            final StringBuilder sbToken = new StringBuilder(sourceToken);
                            CompassInfo.changeCharacters(sbToken);
                            values.add(sbToken.toString());
                        }

                    }
                }
            }
            stoens = values.toArray(new String[values.size()]);
            values.clear();
            for (String sourceToken : ttoens)
            {
                if (!CompassInfo.isStopWords(sourceToken))
                {
                    if (sourceToken.indexOf('.') == -1)
                    {
                        final StringBuilder sbToken = new StringBuilder(sourceToken);
                        CompassInfo.changeCharacters(sbToken);
                        values.add(sbToken.toString());
                    }
                    else
                    {
                        boolean found = false;
                        final Map<String, Set<String>> abreviaciones = CompassInfo.getAbreviaciones();
                        for (String key : abreviaciones.keySet())
                        {
                            if (abreviaciones.get(key).contains(sourceToken))
                            {
                                final StringBuilder sbToken = new StringBuilder(key);
                                CompassInfo.changeCharacters(sbToken);
                                values.add(sbToken.toString());
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                        {
                            final StringBuilder sbToken = new StringBuilder(sourceToken);
                            CompassInfo.changeCharacters(sbToken);
                            values.add(sbToken.toString());
                        }

                    }
                }
            }
            ttoens = values.toArray(new String[values.size()]);

            boolean isIn = false;
            for (String sourceToken : stoens)
            {
                isIn = isIn(sourceToken, ttoens);
                if (!isIn)
                {
                    break;
                }
            }
            if (!isIn)
            {
                checkSinonimos(text, textTarget);
                stoens = CompassInfo.tokenizer(text.toString().trim());
                //ttoens = CompassInfo.tokenizer(textTarget.trim());
                isIn = true;
                for (String sourceToken : stoens)
                {
                    isIn = isIn(sourceToken, ttoens);
                    if (!isIn)
                    {
                        break;
                    }
                }
                if (isIn)
                {
                    for (String sourceToken : ttoens)
                    {
                        isIn = isIn(sourceToken, stoens);
                        if (!isIn)
                        {
                            break;
                        }
                    }
                }
            }
            return isIn;
        }

        /*
         * if (text == null && textTarget == null) { return true; } else if
         * (text != null && textTarget == null) { return false; } else if (text
         * == null && textTarget != null) { return false; } else {
         *
         * }
         */

    }

    /**
     * Busca candidatos con un objeto de consulta
     *
     * @param consulta Objeto consulta
     * @return Resultados de la búsqueda
     * @throws BusquedaException Error en caso de que exista algún problema en
     * los indices
     */
    public Result search(final Consulta consulta) throws BusquedaException
    {
        return search(consulta, null, null);
    }

    /**
     * Busca candidatos con un objeto de consulta y un conjunto de palabras
     *
     * @param consulta Objeto consulta
     * @param words Palabras a buscar en habilidades o conocimientos
     * @return Lista de resultados
     * @throws BusquedaException Error en caso de que exista algun problema en
     * los índices
     */
    public Result search(final Consulta consulta, final Set<String> words) throws BusquedaException
    {
        return search(consulta, words, null);
    }

    public Result search(final Consulta consulta, final Set<String> words, final Set<Integer> paramsimilares) throws BusquedaException
    {
        if (LUCENE.getCompass().isClosed())
        {
            CompassInfo.reopen();
        }
        final CompassSearchSession session = LUCENE.getCompass().openSearchSession();
        final CompassQuery query = creaConsulta(consulta, words, paramsimilares, session);
        if (query != null)
        {
            final CompassHits hits = query.hits();
            return new Result(hits, session, query.count(), consulta);
        }
        return new Result(consulta);

    }
    
    private void llenaBusquedadDiscapacidad(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.discapacidad != null && consulta.discapacidad.length() == 5)
        {
            String _consulta=consulta.discapacidad;
            /*CompassQuery qDiscapacidad=builder.queryString("DISCAPACIDAD:"+_consulta.replace('0', '?')).toQuery();
            queries.add(qDiscapacidad);            */
            
            if ("00000".equals(_consulta))
            {
                CompassQuery qDiscapacidad = builder.queryString("DISCAPACIDAD:" + _consulta).toQuery();
                queries.add(qDiscapacidad);
            } else
            {
                CompassBooleanQueryBuilder boolq = builder.bool();
                CompassQuery qDiscapacidad = builder.queryString("DISCAPACIDAD:" + _consulta.replace('1', '?')).toQuery();
                boolq.addMust(qDiscapacidad);
                CompassQuery notqDiscapacidad = builder.queryString("DISCAPACIDAD:" + "00000").toQuery();
                boolq.addMustNot(notqDiscapacidad);
                queries.add(boolq.toQuery());
            }
        }

    }

    private void llenaBusquedadFuente(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.fuente != null)
        {            
            CompassQuery qfuente=builder.term("FUENTE",consulta.fuente);
            queries.add(qfuente);            
        }

    }
    private void llenaEstadoMunicpio(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.edo != null && consulta.edo > 0)
        {
            final CompassQuery qedo = builder.term(ESTADO, consulta.edo);
            queries.add(qedo);
        }

        if (consulta.municipio != null && consulta.municipio > 0)
        {
            final CompassQuery qmunicipio = builder.term(MUNICIPIO, consulta.municipio);
            queries.add(qmunicipio);
        }
    }

    private void getConsultasRef(final String text, final Consulta consulta)
    {
        final String[] referenciales = CompassInfo.getSinonimosReferenciales(text);
        if (referenciales != null)
        {
            final Set<String> set_ref = new HashSet<String>();
            set_ref.addAll(Arrays.asList(referenciales));
            consulta.sinonimos_ref.put(text, set_ref);
        }
    }

    public CompassQuery createPraseQuery(final String text, final CompassQueryBuilder builder, final String field)
    {

        final StringBuilder wordToCreateQuery = new StringBuilder(text);
        CompassInfo.replace(wordToCreateQuery, '"', ' ');
        CompassInfo.replaceAbreviaciones(wordToCreateQuery);
        CompassInfo.changeBadcharacters(wordToCreateQuery, true);

        CompassInfo.trim(wordToCreateQuery);
        if ("".equals(wordToCreateQuery.toString()))
        {
            return null;
        }
        CompassInfo.changeCharacters(wordToCreateQuery);
        final StringBuilder content = new StringBuilder();
        for (String _word : CompassInfo.sortedtokenizer(wordToCreateQuery.toString()))
        {
            content.append(_word);
            content.append(" ");
        }
        return builder.queryString(field + ":\"" + content.toString().trim() + "\"").toQuery();
    }

    public CompassQuery createPraseQuery(final String word, final CompassQueryBuilder builder)
    {
        return createPraseQuery(word, builder, "WORDS");
    }

    private void creaEdad(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries) throws BusquedaException
    {

        /*
         * if (consulta.edad_de != null && consulta.edad_hasta != null &&
         * consulta.edad_de.doubleValue() > consulta.edad_hasta.doubleValue()) {
         * final Integer tmp = consulta.edad_de; consulta.edad_de =
         * consulta.edad_hasta; consulta.edad_hasta = tmp; }
         */

        if (consulta.getEdadDe() != null && consulta.getEdadHasta() == null)
        {
            /*
             * if (consulta.edad_de.intValue() <= 0) { throw new
             * BusquedaException("La edad inicial (edad_de), no puede ser cero o
             * negativa"); }
             */
            final DecimalFormat dfedad = new DecimalFormat("#0000", new DecimalFormatSymbols(LOCALE));
            final CompassQuery query = builder.le(Campos.EDAD.toString(), dfedad.format(consulta.getEdadDe()));
            queries.add(query);
        }
        else if (consulta.getEdadDe() == null && consulta.getEdadHasta() != null)
        {
            /*
             * if (consulta.edad_hasta.intValue() <= 0) { throw new
             * BusquedaException("La edad inicial (edad_de), no puede ser cero o
             * negativa"); }
             */
            final DecimalFormat dfedad = new DecimalFormat("#0000", new DecimalFormatSymbols(LOCALE));

            final CompassQuery query = builder.le(Campos.EDAD.toString(), dfedad.format(consulta.getEdadHasta()));
            queries.add(query);
        }
        else if (consulta.getEdadDe() != null && consulta.getEdadHasta() != null)
        {
            /*
             * if (consulta.edad_de.intValue() <= 0) { throw new
             * BusquedaException("La edad inicial (edad_de), no puede ser cero o
             * negativa"); } if (consulta.edad_hasta.intValue() <= 0) { throw
             * new BusquedaException("La edad final (edad_hasta), no puede ser
             * cero o negativa"); }
             */
            final DecimalFormat dfedad = new DecimalFormat("#0000", new DecimalFormatSymbols(LOCALE));
            final String _edad_de = dfedad.format(consulta.getEdadDe());
            final String _edad_hasta = dfedad.format(consulta.getEdadHasta());
            final CompassQuery query = builder.between(Campos.EDAD.toString(), _edad_de, _edad_hasta, true);
            queries.add(query);
        }
    }

    private void creaSalario(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries) throws BusquedaException
    {
        if (consulta.getSalarioDe() != null && consulta.getSalarioHasta() == null)
        {


            final DecimalFormat dfsalario = new DecimalFormat("#0000000.00", new DecimalFormatSymbols(LOCALE));
            final CompassQuery query = builder.le("SALARIO_PRETENDIDO", dfsalario.format(consulta.getSalarioDe()));
            queries.add(query);
        }
        else if (consulta.getSalarioDe() == null && consulta.getSalarioHasta() != null)
        {


            final DecimalFormat dfsalario = new DecimalFormat("#0000000.00", new DecimalFormatSymbols(LOCALE));
            final CompassQuery query = builder.ge("SALARIO_PRETENDIDO", dfsalario.format(consulta.getSalarioHasta()));
            queries.add(query);
        }
        else if (consulta.getSalarioDe() != null && consulta.getSalarioHasta() != null)
        {


            final DecimalFormat dfsalario = new DecimalFormat("#0000000.00", new DecimalFormatSymbols(LOCALE));
            final CompassQuery query = builder.between("SALARIO_PRETENDIDO", dfsalario.format(consulta.getSalarioDe()), dfsalario.format(consulta.getSalarioHasta()), true);
            queries.add(query);
        }
    }

    private void creaStatus(final Consulta consulta, final CompassQueryBuilder builder, final Set<CompassQuery> setcarrerasocup) throws BusquedaException
    {

        if (consulta.otros_status_estudio.isEmpty() && consulta.otros_grados.isEmpty())
        {
            creaStatus1(consulta, builder, setcarrerasocup);
        }
        else if (consulta.otros_status_estudio.isEmpty() && !consulta.otros_grados.isEmpty())
        {
            creaStatus2(consulta, builder, setcarrerasocup);
        }
        else if (!consulta.otros_status_estudio.isEmpty() && consulta.otros_grados.isEmpty())
        {
            creaStatus3(consulta, builder, setcarrerasocup);
        }
        else
        {
            creaStatus4(consulta, builder, setcarrerasocup);

        }
    }

    /**
     * Busca candidatos con un objeto de consulta , un conjunto de palabras y
     * una lista de carreras similares
     *
     * @param consulta Objeto consulta
     * @param words Palabras a buscar en habilidades o conocimientos
     * @param paramsimilares Carreras similares
     * @return Lista de resultados
     * @throws BusquedaException Error en caso de que exista algún problema en
     * los índices
     */
    public CompassQuery creaConsulta(final Consulta consulta, final Set<String> words, final Set<Integer> paramsimilares, final CompassSearchSession session) throws BusquedaException
    {
        BooleanQuery.setMaxClauseCount(3000);
        if (consulta == null)
        {
            //return new Result(consulta);
            return null;
        }

        final CompassQueryBuilder builder = session.queryBuilder();
        final List<CompassQuery> queries = new ArrayList<CompassQuery>();



        try
        {
            llenaBusquedadFuente(consulta, builder, queries);            
            llenaBusquedadDiscapacidad(consulta, builder, queries);
            llenaEstadoMunicpio(consulta, builder, queries);
            creaConsultaPalabras(words, builder, queries);
            creaDisponibilidad(consulta, builder, queries);
            creaDispViajar(consulta, builder, queries);
            creaExperiencia(consulta, builder, queries);
            creaHorario(consulta, builder, queries);
            creaIndicador(consulta, builder, queries);
            creaEdad(consulta, builder, queries);
            creaSalario(consulta, builder, queries);
            creaConsultaIdiomas(consulta, builder, queries);
            creaConsultaSinonimosRef(consulta);
            carrerasOcupaciones(consulta, builder, queries);
            creaConsultaAcademica(consulta, builder, queries);
            creaConsultaConocimiento(consulta, builder, queries);
            creaConsultaHabilidad(consulta, builder, queries);
        }
        catch (CompassException e)
        {
            LOG.error("Error al procesar la consulta", e);
            session.close();
            //return new Result(consulta);
            return null;
        }
        try
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder bool = builder.bool();
            if (queries.isEmpty())
            {
                LOG.trace("Consulta: queries.isEmpty() && optional.isEmpty() cadena procesada: \"" + consulta.text + "\" cadena original: \"" + consulta.originalText + "\"");
                //return new Result(consulta);
                return null;
            }

            bool.addMust(builder.alias(CANDIDATO));
            for (CompassQuery query : queries)
            {
                bool.addMust(query);
            }

            final CompassQuery query = bool.toQuery();

            query.addSort(CompassQuery.SortImplicitType.SCORE);
            final CompassHits hits = query.hits();
            LOG.trace("Consulta: " + query.toString() + " alias: " + CANDIDATO + " resultados: " + hits.getLength() + " cadena original: " + consulta.originalText + " cadena procesada: " + consulta.text + " ,criterios: " + (consulta == null ? null : consulta.toString()));

            return query;

        }
        catch (RuntimeException e)
        {
            LOG.error("Error en consulta " + consulta.originalText, e);
            throw e;
        }
        /*
         * finally { compassSearchSession.close(); }
         */
    }

    /**
     * Regresa el número de candidatos existentes en el índice
     *
     * @return Número de candidatos
     */
    public long getCount()
    {
        final long tini = System.currentTimeMillis();
        if (LUCENE.getCompass().isClosed())
        {
            CompassInfo.reopen();
        }
        final CompassSearchSession session = LUCENE.getCompass().openSearchSession();
        final CompassQueryBuilder builder = session.queryBuilder();
        try
        {
            final CompassQuery query = builder.alias(CANDIDATO);
            final long count = query.count();
            final long tfin = System.currentTimeMillis();
            final long dif = (tfin - tini);
            LOG.trace("Tiempo de conteo: " + dif + " ms, count:" + count);
            return count;
        }
        catch (RuntimeException e)
        {
            LOG.error(ERROR, e);
            throw e;
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    private static void tryUpdateCandidato(final CandidatoIndex index) throws BusquedaException
    {
        try
        {
            if (LUCENE.getCompass().isClosed())
            {
                CompassInfo.reopen();
            }
            CompassIndexSession session = LUCENE.getCompass().openIndexSession();
            LUCENE.getCompass().getSearchEngineIndexManager().refreshCache(CANDIDATO);
            if (session.isClosed())
            {
                session = LUCENE.getCompass().openIndexSession();
            }


            try
            {

                session.save(index);
                session.commit();
                LUCENE.getCompass().getSearchEngineIndexManager().notifyAllToClearCache();
            }
            catch (Exception e)
            {
                session.rollback();
                LOG.error("candidato error " + index.id);
                throw new BusquedaException(e);
            }
            finally
            {
                if (!session.isClosed())
                {
                    session.close();
                }
            }


        }
        catch (SearchEngineException se)
        {
            LOG.error(MSG_SAVE + index.getId(), se);
            throw se;
        }
        catch (CompassException ce)
        {
            LOG.error(MSG_SAVE + index.getId(), ce);
            throw ce;
        }
        catch (IllegalStateException ce)
        {
            LOG.error(MSG_SAVE + index.getId(), ce);
            throw ce;
        }
        catch (RuntimeException ce)
        {
            LOG.error(MSG_SAVE + index.getId(), ce);
            throw ce;
        }
    }

    /**
     * Función que guarda un candidato
     *
     * @param candidato Candidato a indexar
     * @param session Sesión de compass
     * @throws Exception Error en caso de no poder indexar un candidato
     */
    /*
     * private static synchronized void updateCandidato(final Candidato
     * candidato) throws BusquedaException { final CandidatoIndex index = new
     * CandidatoIndex(candidato); //server.send(index); try {
     * tryUpdateCandidato(index); } catch (Exception e) { try {
     * Thread.sleep(500); } catch (Exception e2) { LOG.error(ERROR, e2); }
     * LOG.info("Segundo intento para " + candidato.id);
     * tryUpdateCandidato(index); LOG.info("Fin de Segundo intento para " +
     * candidato.id); }
     *
     *
     * }
     */

    /*
     * private static void updateCandidato(final CandidatoIndex index) throws
     * BusquedaException {
     *
     * try { tryUpdateCandidato(index); } catch (Exception e) { try {
     * Thread.sleep(500); } catch (Exception e2) { LOG.error(ERROR, e2); }
     * LOG.info("Segundo intento para " + index.id); tryUpdateCandidato(index);
     * LOG.info("Fin de Segundo intento para " + index.id); }
     *
     *
     * }
     */
    private static void tryDeleteCandidato(final CandidatoIndex index) throws BusquedaException
    {
        try
        {
            if (LUCENE.getCompass().isClosed())
            {
                CompassInfo.reopen();
            }
            CompassIndexSession session = LUCENE.getCompass().openIndexSession();
            LUCENE.getCompass().getSearchEngineIndexManager().refreshCache(CANDIDATO);
            if (session.isClosed())
            {
                session = LUCENE.getCompass().openIndexSession();
            }


            try
            {
                session.delete(index);
                session.commit();
                LUCENE.getCompass().getSearchEngineIndexManager().notifyAllToClearCache();
            }
            catch (Exception e)
            {
                session.rollback();
                LOG.error("candidato error " + index.id);
                throw new BusquedaException(e);
            }
            finally
            {
                if (!session.isClosed())
                {
                    session.close();
                }
            }


        }
        catch (SearchEngineException se)
        {
            LOG.error(MSG_SAVE + index.getId(), se);
            throw se;
        }
        catch (CompassException ce)
        {
            LOG.error(MSG_SAVE + index.getId(), ce);
            throw ce;
        }
        catch (IllegalStateException ce)
        {
            LOG.error(MSG_SAVE + index.getId(), ce);
            throw ce;
        }
        catch (RuntimeException ce)
        {
            LOG.error(MSG_SAVE + index.getId(), ce);
            throw ce;
        }
    }

    /**
     * Regresa un Candidato en base a su id, de los índices
     *
     * @param id Id del candidato
     * @return Candidato encontrado, nulo si no lo encuentra
     */
    public Candidato get(final long idcandidato)
    {
        if (LUCENE.getCompass().isClosed())
        {
            CompassInfo.reopen();
        }
        final CompassSearchSession session = LUCENE.getCompass().openSearchSession();
        try
        {
            final CandidatoIndex candidato = (CandidatoIndex) session.load(CandidatoIndex.class, idcandidato);


            if (candidato
                    != null)
            {
                return candidato.get();
            }
        }
        catch (CompassException ce)
        {
            LOG.error("Error al buscar candidato " + idcandidato, ce);
        }
        finally
        {
            session.close();
        }
        return null;
    }

    public synchronized static void removeIndice()
    {
        CompassInfo.removeIndice(CANDIDATO);
    }

    private Set<Integer> getCarrerasSimilares(final Set<Integer> carreras)
    {
        final Set<Integer> similares = new HashSet<Integer>();
        for (Integer icarrera : carreras)
        {
            synchronized (CompassInfo.CARRERASSIMILARES)
            {
                for (CarrerasOcupacionSimilares set : CompassInfo.CARRERASSIMILARES)
                {
                    if (set.contains(icarrera))
                    {
                        if (set.getIndiceCarreraOcupacion() == null)
                        {
                            similares.addAll(set);
                        }
                        else
                        {
                            if (set.getIndiceCarreraOcupacion().intValue() == icarrera.intValue())
                            {
                                similares.addAll(set);
                            }
                        }
                    }
                }
            }
        }
        return similares;
    }

    private Set<Integer> getCarreras(final Consulta consulta)
    {
        final Set<Integer> getCarreras = new HashSet<Integer>();
        if (consulta.carrera != null)
        {
            getCarreras.add(consulta.carrera);
        }
        if (!consulta.otras_carreras.isEmpty())
        {
            getCarreras.addAll(consulta.otras_carreras);
        }
        if (!getCarreras.isEmpty())
        {
            final Set<Integer> similares = getCarrerasSimilares(getCarreras);
            getCarreras.addAll(similares);
        }
        return getCarreras;
    }

    private void addOtrasExperiencias(final Consulta consulta)
    {
        if (!consulta.otras_experiencias.isEmpty())
        {
            final Set<Integer> indices = new HashSet<Integer>();
            indices.addAll(consulta.otras_experiencias);
            for (Integer index : consulta.otras_experiencias)
            {
                final String desc = CompassInfo.EXPERIENCIA.get(index);
                if (Character.isDigit(desc.charAt(0)))
                {
                    try
                    {
                        final int iaños = Integer.parseInt(String.valueOf(desc.charAt(0)));
                        indices.addAll(CompassInfo.getExperiencia(iaños));
                    }
                    catch (Exception e)
                    {
                        LOG.error("Error al obtener indices de expeiencia", e);
                    }
                }
                else if (desc.toLowerCase(LOCALE).startsWith("más"))
                {
                    try
                    {
                        indices.addAll(CompassInfo.getExperiencia(6));
                    }
                    catch (Exception e)
                    {
                        LOG.error("Error al obtener indices de expeiencia", e);
                    }
                }
                else
                {
                    try
                    {
                        indices.addAll(CompassInfo.getExperiencia(0));
                    }
                    catch (Exception e)
                    {
                        LOG.error("Error al obtener indices de expeiencia", e);
                    }
                }
            }
            consulta.otras_experiencias.addAll(indices);
        }
    }

    public Result getCandidatos()
    {
        final long tini = System.currentTimeMillis();
        if (LUCENE.getCompass().isClosed())
        {
            CompassInfo.reopen();
        }
        final CompassSearchSession session = LUCENE.getCompass().openSearchSession();
        final CompassQueryBuilder builder = session.queryBuilder();
        try
        {
            final CompassQuery query = builder.alias(CANDIDATO);
            query.addSort(CompassQuery.SortImplicitType.DOC);
            final long count = query.count();
            final long tfin = System.currentTimeMillis();
            final long dif = (tfin - tini);
            LOG.trace("Tiempo de conteo: " + dif + " ms, count:" + count);
            return new Result(query.hits(), session, count, null);

        }
        catch (RuntimeException e)
        {
            LOG.error(ERROR, e);
            throw e;
        }
        /*
         * finally { if (compassSearchSession != null) {
         * compassSearchSession.close(); } }
         */

    }
}
