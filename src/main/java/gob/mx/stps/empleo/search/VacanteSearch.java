/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.QueryParser;
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
 * Clase de busqueda de Vacantes
 *
 * @author victor.lorenzana
 */
public final class VacanteSearch
{

    private static final String CARRERAWORDS = "CARRERAWORDS";
    private static final String CONOCIMIENTO = "CONOCIMIENTO";
    private static final String ERROR_SALVANDO_VACANTE_ = "Error salvando vacante: ";
    private static final String ESTADO = "ESTADO";
    private static final String MUNICIPIO = "MUNICIPIO";
    private static final String OCUPACIONWORDS = "OCUPACIONWORDS";
    private static final String SEPQUERY = ":\"";
    private static final String ENDSEPQUERY = "\"";
    private static final String ERROR = "Error";
    private static final String REF = "REF";
    private static final SetTerminos TERMS = new SetTerminos();
    private static final String TITULO = "TITULO";
    private static final String VACANTE = "vacante";
    private static final Locale LOCALE = new Locale("es", "MX");
    /**
     * Variable para mensajes
     */
    private static final Log LOG = LogFactory.getLog(VacanteSearch.class);
    /**
     * Variable de acceso a Compass
     */
    private static final CompassInfo LUCENE = new CompassInfo();

    /**
     * Constructor con los catálogos a utilizar el indexador tablas de términos
     *
     * @param catalogo Objeto catalogo con las tablas de términos, catálogos del
     * portal del empleo
     */
    public VacanteSearch(final Catalogo catalogo)
    {

        CompassInfo.OCUPACIONES.putAll(catalogo.ocupacion_catalogo);
        CompassInfo.CARRERAS.putAll(catalogo.carrera_especialidad_catalogo);
        copyData(CompassInfo.OCUPACIONESCAT, catalogo.ocupacion_catalogo);
        copyData(CompassInfo.CARRERASCAT, catalogo.carrera_especialidad_catalogo);
        //copyData(CompassInfo.DOMINIOS, catalogo.DOMINIOS);
        processTerms();
        addSimilares();

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

    private void agregaConsultaPalabrasCampos(final CompassQueryBuilder builder, final String word, final CompassBooleanQueryBuilder boolprases, final List<CompassQuery> queries)
    {
        final String[] fields =
        {
            TITULO, OCUPACIONWORDS, CARRERAWORDS, CONOCIMIENTO
        };
        for (String field : fields)
        {
            final CompassQuery query = builder.queryString(field + SEPQUERY + word + ENDSEPQUERY).toQuery();
            if (field.equals(TITULO))
            {
                boolprases.addShould(query.setBoost(400));
            }
        }
        queries.add(boolprases.toQuery());
    }

    private void agregaConsultaRef(final List<Term> terminos, final Set<Integer> ocupaciones_totales, final CompassQueryBuilder builder, final SetQueries set_carreras_ocupaciones_titulo, final Set<Integer> carreras_totales)
    {
        final Term term = terminos.get(0);
        final String field = term.getLemma();
        if (Campos.OCUPACION.toString().equals(field))
        {
            //consulta.ocupaciones_detectadas.add(term.getId());                                     
            final Integer iOcupacion = term.getId();
            if (!ocupaciones_totales.contains(iOcupacion))
            {
                final String search = iOcupacion.toString();
                final CompassQuery query = builder.term("OCUPACION", search);
                set_carreras_ocupaciones_titulo.add(query);
            }
        }
        if (Campos.CARRERA_O_ESPECIALIDAD.toString().equals(field))
        {
            //consulta.carreras_detectadas.add(term.getId());
            final Integer iCarrera = term.getId();
            if (!carreras_totales.contains(iCarrera))
            {
                final String search = iCarrera.toString();
                final CompassQuery query = builder.term("CARRERA", search);
                set_carreras_ocupaciones_titulo.add(query);
            }
        }
    }

    private Set<Integer> creaConsultaCarreras(final Consulta consulta, final SetQueries set_carreras_ocupaciones_titulo, final CompassQueryBuilder builder)
    {
        for (String carrera : consulta.carreras_words)
        {
            set_carreras_ocupaciones_titulo.addAll(createQueriesForTitle(carrera, builder, consulta));
            /*CompassQuery qcarreras = parseQuery(carrera, builder, "CARRERAWORDS");
             set_carreras_ocupaciones_titulo.add(qcarreras);
             for (CompassQuery qref : getConsultasRef(carrera, builder, "CARRERAWORDS", consulta))
             {
             set_carreras_ocupaciones_titulo.add(qref);
             }*/
        }
        final Set<Integer> carreras_totales = new HashSet<Integer>();
        if (!consulta.carreras_detectadas.isEmpty())
        {

            carreras_totales.addAll(consulta.carreras_detectadas);
            for (Integer iCarrera : consulta.carreras_detectadas)
            {
                final Integer[] isimilares = CompassInfo.getCarrerasSimilaresCatalogo(iCarrera);
                if (isimilares != null && isimilares.length > 0)
                {
                    carreras_totales.addAll(Arrays.asList(isimilares));
                }
            }

            for (Integer iCarrera : carreras_totales)
            {
                if (CompassInfo.CARRERAS.containsKey(iCarrera))
                {
                    final String search = iCarrera.toString();
                    final CompassQuery query = builder.term("CARRERA", search);
                    set_carreras_ocupaciones_titulo.add(query);
                }
            }
        }
        return carreras_totales;

    }

    private void creaConsultaConocimientos(final Consulta consulta, final CompassQueryBuilder builder, final SetQueries set_carreras_ocupaciones_titulo)
    {
        for (String carrera : consulta.carreras_words)
        {
            // Se agrego esta busqueda por casos java2 java computación en conocimientos y computación es carrera
            // no se ha agregado para ocupación, si se encuentra caso se agregara
            final CompassQuery qcarreras = parseQuery(carrera, builder, CONOCIMIENTO);
            set_carreras_ocupaciones_titulo.add(qcarreras);
            for (CompassQuery qref : getConsultasRef(carrera, builder, CONOCIMIENTO, consulta))
            {
                set_carreras_ocupaciones_titulo.add(qref);
            }
        }
    }

    private Set<Integer> creaConsultaOcupaciones(final Consulta consulta, final SetQueries set_carreras_ocupaciones_titulo, final CompassQueryBuilder builder)
    {
        for (String ocupacion : consulta.ocupaciones_words)
        {
            set_carreras_ocupaciones_titulo.addAll(createQueriesForTitle(ocupacion, builder, consulta));
            /*CompassQuery qocupacion = parseQuery(ocupacion, builder, "OCUPACIONWORDS");
             set_carreras_ocupaciones_titulo.add(qocupacion);
             for (CompassQuery qref : getConsultasRef(ocupacion, builder, "OCUPACIONWORDS", consulta))
             {
             set_carreras_ocupaciones_titulo.add(qref);
             }*/
        }
        final Set<Integer> totales = new HashSet<Integer>();
        if (!consulta.ocupaciones_detectadas.isEmpty())
        {

            totales.addAll(consulta.ocupaciones_detectadas);
            for (Integer iCarrera : consulta.ocupaciones_detectadas)
            {
                final Integer[] isimilares = CompassInfo.getCarrerasSimilaresCatalogo(iCarrera);
                if (isimilares != null && isimilares.length > 0)
                {
                    totales.addAll(Arrays.asList(isimilares));
                }
            }
            for (Integer iOcupacion : totales)
            {
                if (CompassInfo.OCUPACIONES.containsKey(iOcupacion))
                {
                    final String search = iOcupacion.toString();
                    final CompassQuery query = builder.term("OCUPACION", search);
                    set_carreras_ocupaciones_titulo.add(query);
                }
            }
        }
        return totales;
    }

    private void creaConsultaPalabras(final Set<String> words, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {

        if (words != null && !words.isEmpty())
        {
            final CompassQuery query = createMultiWordQuery(words, builder);
            queries.add(query);
        }
    }

    private void creaConsultaPharses(final Set<String> words, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (words != null)
        {
            final Set<String> delete = new HashSet<String>();
            for (String word : words) // busca frases
            {
                if (".".equals(word))
                {
                    delete.add(word);
                } else if (word.startsWith(ENDSEPQUERY))
                {
                    delete.add(word);
                    word = word.replace('\"', ' ').trim();
                    word = prepareForQueryString(word).trim();
                    createWordQuery(word, builder, queries);
                }
            }
            for (String word : delete)
            {
                words.remove(word);
            }
        }
    }

    private void createData(final Map<Integer, String> source, final Integer key, final Map<Integer, String> target)
    {
        String data = source.get(key);
        data = data.toLowerCase(LOCALE);
        if (data.startsWith("asistente, auxiliar o"))
        {
            data = data.replace("asistente, auxiliar o", "").trim();
        }

        if (data.startsWith("auxiliar o ayudante"))
        {
            data = data.replace("auxiliar o", "").trim();
        }

        if (data.startsWith("asistente, ayudante"))
        {
            data = data.replace("asistente,", "").trim();
        }

        if (data.startsWith("auxiliar y"))
        {
            data = data.replace("auxiliar y", "").trim();
        }

        if (data.startsWith("auxiliar, ayudante o aprendiz"))
        {
            data = data.replace("auxiliar, ayudante o", "").trim();
        }

        if (data.startsWith("asistente o auxiliar"))
        {
            data = data.replace("asistente o", "").trim();
        }

        final StringBuilder sbdata = new StringBuilder(data);
        CompassInfo.cleanTerm(sbdata);
        CompassInfo.changeCharacters(sbdata);
        data = sbdata.toString();
        if (!"".equals(data.trim()))
        {
            target.put(key, data);
        }
    }

    private void createWordQuery(final String word, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (!word.isEmpty())
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder boolprases = builder.bool();
            agregaConsultaPalabrasCampos(builder, word, boolprases, queries);
        }
    }

    private void creaConsultaRef(final Consulta consulta, final Set<Integer> ocupatotales, final CompassQueryBuilder builder, final SetQueries titilos, final Set<Integer> carrerastotales)
    {
        if (!consulta.sinonimos_ref.isEmpty())
        {
            for (String text : consulta.sinonimos_ref.keySet())
            {
                final Set<String> sinonimos = consulta.sinonimos_ref.get(text);
                for (String sin_ref : sinonimos)
                {
                    final EntityTagger tagger = new EntityTagger(TERMS.getSWBTermDictionary());
                    try
                    {
                        tagger.tagEntities(sin_ref);
                        final Iterator<Term> itTerminos = tagger.listMatchedTerms();
                        final List<Term> terminos = new ArrayList<Term>();
                        while (itTerminos.hasNext())
                        {
                            final Term term = itTerminos.next();
                            terminos.add(term);
                        }
                        if (terminos.size() == 1)
                        {
                            agregaConsultaRef(terminos, ocupatotales, builder, titilos, carrerastotales);
                        }
                    } catch (IOException e)
                    {
                        LOG.debug(e);
                    }
                }
            }
        }
    }

    private void creaConsultaconTitulo(final Consulta consulta, final CompassQueryBuilder builder, final SetQueries optional)
    {
        if (!CompassInfo.isStopWords(consulta.originalText))
        {
            final SetQueries queriesTitulo = createQueriesForTitle(consulta.originalText, builder, consulta);
            final SetQueries optional_final = new SetQueries();
            for (CompassQuery q : queriesTitulo)
            {
                CompassBooleanQueryBuilder bool = builder.bool();
                bool.addMust(q);
                List<CompassQuery> queries = new ArrayList<CompassQuery>();
                llenaBusquedadFuente(consulta, builder, queries);
                llenaBusquedadDiscapacidad(consulta, builder, queries);
                llenaEstadoMunicpio(consulta, builder, queries);
                llenaEdadBusqueda(consulta, builder, queries);
                for (CompassQuery qmust : queries)
                {
                    bool.addMust(qmust);
                }
                optional_final.add(bool.toQuery());
            }
            optional.addAll(optional_final);
        }
    }

    private void creaOcupaCarreras(final SetQueries ocupaCarreras, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (!ocupaCarreras.isEmpty())
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder carreras_ocupa = builder.bool();
            for (CompassQuery q : ocupaCarreras)
            {
                carreras_ocupa.addShould(q);
            }
            queries.add(carreras_ocupa.toQuery());
        }
    }

    private void creaRef(final Set<String> words, final Term term, final List<String> conjunto, final Consulta consulta)
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
            } else
            {
                final Set<String> sim_ref = consulta.sinonimos_ref.get(key);
                sim_ref.addAll(ref);
                consulta.sinonimos_ref.put(key, sim_ref);
            }
        }
    }

    private void generaRef(final List<String> conjunto, final String text, final Consulta consulta)
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
            } else
            {
                consulta.sinonimos_ref.put(text, ref);
            }

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

    private void obligatorias(final List<CompassQuery> queries, final CompassQueryBuilder builder, final CompassBooleanQueryBuilder qSearch)
    {
        if (!queries.isEmpty())
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder bool = builder.bool();
            final CompassQuery alias = builder.alias(VACANTE);
            bool.addMust(alias);
            for (CompassQuery query : queries)
            {
                bool.addMust(query);
            }
            qSearch.addShould(bool.toQuery());
        }
    }

    private void opcionales(final SetQueries optional, final CompassQueryBuilder builder, final Consulta consulta, final CompassBooleanQueryBuilder qSearch)
    {
        if (!optional.isEmpty())
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder bool_optional = builder.bool();
            final CompassQuery alias = builder.alias(VACANTE);
            bool_optional.addMust(alias);
            for (CompassQuery query : optional)
            {
                bool_optional.addMust(query);
            }

            if (consulta.edo != null && consulta.edo > 0)
            {
                final CompassQuery qedo = builder.term(ESTADO, consulta.edo);
                bool_optional.addMust(qedo);
            }

            if (consulta.municipio != null && consulta.municipio > 0)
            {
                final CompassQuery qmunicipio = builder.term(MUNICIPIO, consulta.municipio);
                bool_optional.addMust(qmunicipio);
            }
            qSearch.addShould(bool_optional.toQuery());
        }
    }

    private void procesaCarrera(final Term termino, final Consulta consulta)
    {
        getConsultasRef(termino.getOriginal(), consulta);
        String carrera_text = CompassInfo.CARRERAS.get(termino.getId());
        if (carrera_text != null)
        {
            carrera_text = CompassInfo.changeCharacters(carrera_text);
            carrera_text = CompassInfo.cleanTerm(carrera_text);
            consulta.carreras_words.add(CompassInfo.changeCharacters(termino.getOriginal()));
        }
        consulta.carreras_detectadas.add(termino.getId());
        if (TERMS.isAmbiguo(termino))
        {
            for (Term term2 : TERMS.getTerms(termino))
            {
                if (term2.getLemma() != null)
                {
                    if (term2.getLemma().equals(Campos.CARRERA_O_ESPECIALIDAD.toString()))
                    {

                        getConsultasRef(term2.getOriginal(), consulta);
                        consulta.carreras_detectadas.add(term2.getId());
                        consulta.carreras_words.add(CompassInfo.changeCharacters(term2.getOriginal()));

                        if (term2.varname != null)
                        {
                            consulta.carreras_words.add(CompassInfo.changeCharacters(term2.varname));
                        }
                    } else if (term2.getLemma().equals(REF))
                    {
                        procesaSinonimosRef(term2, consulta);

                    } else if (term2.getLemma().equals(Campos.OCUPACION.toString()))
                    {

                        getConsultasRef(term2.getOriginal(), consulta);
                        consulta.ocupaciones_detectadas.add(term2.getId());
                        consulta.ocupaciones_words.add(term2.getOriginal());

                    }
                }
            }
        }
    }

    private void procesaOcupacion(final Term termino, final Consulta consulta)
    {
        getConsultasRef(termino.getOriginal(), consulta);
        String ocupacion_text = CompassInfo.OCUPACIONESCAT.get(termino.getId());
        if (ocupacion_text != null)
        {
            ocupacion_text = CompassInfo.changeCharacters(ocupacion_text);
            ocupacion_text = CompassInfo.cleanTerm(ocupacion_text);
            consulta.ocupaciones_words.add(termino.getOriginal());
        }

        if (TERMS.isAmbiguo(termino))
        {
            final Term[] _terminos = TERMS.getTerms(termino);
            for (Term term2 : _terminos)
            {
                if (term2.getLemma() != null)
                {
                    if (term2.getLemma().equals(Campos.OCUPACION.toString()))
                    {
                        consulta.ocupaciones_detectadas.add(term2.getId());
                        consulta.ocupaciones_words.add(term2.getOriginal());
                        if (term2.varname != null)
                        {
                            consulta.ocupaciones_words.add(term2.varname);
                        }
                    } else if (term2.getLemma().equals(REF))
                    {
                        procesaRefOcupacion(term2, consulta);
                    } else if (term2.getLemma().equals(Campos.CARRERA_O_ESPECIALIDAD.toString()))
                    {
                        consulta.carreras_detectadas.add(term2.getId());
                        consulta.carreras_words.add(CompassInfo.changeCharacters(term2.getOriginal()));
                    }
                }

            }
        }
    }

    private void procesaRef(final Term term, final Set<String> words, final Consulta consulta)
    {
        for (List<String> conjunto : CompassInfo.SINREFERENCIALES)
        {
            if (conjunto.contains(term.getOriginal()))
            {
                creaRef(words, term, conjunto, consulta);
            }
        }
    }

    private void procesaRefOcupacion(final Term term2, final Consulta consulta)
    {
        final String text = term2.getOriginal();
        // busca los sinonimos referenciales
        for (List<String> conjunto : CompassInfo.SINREFERENCIALES)
        {
            if (conjunto.contains(text))
            {
                generaRef(conjunto, text, consulta);
            }
        }
    }

    private void procesaSinonimosRef(final Term term2, final Consulta consulta)
    {
        for (List<String> conjunto : CompassInfo.SINREFERENCIALES)
        {
            final String text = term2.getOriginal();
            if (conjunto.contains(text))
            {
                generaRef(conjunto, text, consulta);
            }
        }
    }

    /**
     * Procesa los catálogos y los agrega como términos
     */
    private void processTerms()
    {
        LOG.info("Iniciando agregar terminos modulo vacantes...");
        final long tini = System.currentTimeMillis();

        for (Integer iocupacion : CompassInfo.OCUPACIONESCAT.keySet())
        {
            String ocupacion = CompassInfo.OCUPACIONES.get(iocupacion).toLowerCase().replace('.', ' ').trim();
            final String nocupacion = ocupacion;

            final StringBuilder sbocupacion = new StringBuilder(ocupacion);
            //CompassInfo.addFirstWordOcupacion(ocupacion, iocupacion);
            CompassInfo.changeCharacters(sbocupacion);
            //Term term = new Term(ocupacion, Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
            //terms.add(term);
            CompassInfo.extractParentesis(sbocupacion);
            CompassInfo.normaliza(sbocupacion);
            ocupacion = sbocupacion.toString();
            CompassInfo.OCUPACIONNORM.add(ocupacion);
            final Term term = new Term(ocupacion, Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
            TERMS.addTerm(term);

        }
        for (Integer icarrera : CompassInfo.CARRERASCAT.keySet())
        {
            String carrera = CompassInfo.CARRERAS.get(icarrera).toLowerCase().replace('.', ' ').trim();
            final String ncarrera = carrera;
            final StringBuilder sbcarrera = new StringBuilder(carrera);
            CompassInfo.changeCharacters(sbcarrera);
            CompassInfo.extractParentesis(sbcarrera);
            carrera = sbcarrera.toString();
            CompassInfo.CARRERASNORM.add(CompassInfo.normaliza(carrera));
            final Term term = new Term(carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera, ncarrera);
            TERMS.addTerm(term);

        }
        for (Integer icarrera : CompassInfo.CARRERASCAT.keySet())
        {
            String carrera = CompassInfo.CARRERASCAT.get(icarrera).replace('.', ' ').trim();
            final String ncarrera = carrera;
            final StringBuilder sbCarrera = new StringBuilder(carrera);
            CompassInfo.cleanTerm(sbCarrera);
            CompassInfo.changeCharacters(sbCarrera);
            carrera = sbCarrera.toString();
            Term term = new Term(carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera.intValue(), ncarrera);
            TERMS.addTerm(term);

            for (String var : CompassInfo.getVariantes(carrera))
            {
                term = new Term(var, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera, ncarrera);
                TERMS.add(term, true, true);
            }

        }
        for (Integer iocupacion : CompassInfo.OCUPACIONESCAT.keySet())
        {
            String ocupacion = CompassInfo.OCUPACIONESCAT.get(iocupacion).replace('.', ' ').trim();
            final String nocupacion = ocupacion;
            final StringBuilder sbocupacion = new StringBuilder(ocupacion);
            CompassInfo.cleanTerm(sbocupacion);
            CompassInfo.changeCharacters(sbocupacion);
            ocupacion = sbocupacion.toString();
            Term term = new Term(ocupacion, Campos.OCUPACION.toString(), iocupacion, nocupacion);
            TERMS.addTerm(term);
            for (String var : CompassInfo.getVariantes(ocupacion))
            {
                term = new Term(var, Campos.OCUPACION.toString(), iocupacion, nocupacion);
                TERMS.add(term, true, true);
            }

        }

        for (String simref : CompassInfo.TOKENSREF)
        {
            TERMS.add(new Term(simref, REF, -1, simref), false, false);
        }
        final long tfin = System.currentTimeMillis();
        final long dif = tfin - tini;
        LOG.info("Fin de cargar terminos tiempo: " + dif + " ms");
    }
    /*private void processTerms()
     {
     Thread hilo = new Thread(new Runnable()
     {
    
     private static final String REF = "REF";
    
     @Override
     public void run()
     {
    
     LOG.info("Iniciando agregar terminos modulo vacantes...");
     long tini = System.currentTimeMillis();
    
    
    
     for (Integer iocupacion : CompassInfo.OCUPACIONESCAT.keySet())
     {
     String ocupacion = CompassInfo.OCUPACIONES.get(iocupacion).toLowerCase().replace('.', ' ').trim();
     String nocupacion = ocupacion;
    
     StringBuilder sbocupacion = new StringBuilder(ocupacion);
     //CompassInfo.addFirstWordOcupacion(ocupacion, iocupacion);
     CompassInfo.changeCharacters(sbocupacion);
     //Term term = new Term(ocupacion, Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
     //terms.add(term);
     CompassInfo.extractParentesis(sbocupacion);
     CompassInfo.normaliza(sbocupacion);
     ocupacion = sbocupacion.toString();
     CompassInfo.OCUPACIONNORM.add(ocupacion);
     Term term = new Term(ocupacion, Campos.OCUPACION.toString(), iocupacion.intValue(), nocupacion);
     TERMS.addTerm(term);
    
     }
     for (Integer icarrera : CompassInfo.CARRERASCAT.keySet())
     {
     String carrera = CompassInfo.CARRERAS.get(icarrera).toLowerCase().replace('.', ' ').trim();
     String ncarrera = carrera;
     StringBuilder sbcarrera = new StringBuilder(carrera);
     CompassInfo.changeCharacters(sbcarrera);
     CompassInfo.extractParentesis(sbcarrera);
     carrera = sbcarrera.toString();
     CompassInfo.CARRERASNORM.add(CompassInfo.normaliza(carrera));
     Term term = new Term(carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera, ncarrera);
     TERMS.addTerm(term);
    
     }
     for (Integer icarrera : CompassInfo.CARRERASCAT.keySet())
     {
     String carrera = CompassInfo.CARRERASCAT.get(icarrera).replace('.', ' ').trim();
     String ncarrera = carrera;
     StringBuilder sbCarrera = new StringBuilder(carrera);
     CompassInfo.cleanTerm(sbCarrera);
     CompassInfo.changeCharacters(sbCarrera);
     carrera = sbCarrera.toString();
     Term term = new Term(carrera, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera.intValue(), ncarrera);
     TERMS.addTerm(term);
    
     for (String var : CompassInfo.getVariantes(carrera))
     {
     term = new Term(var, Campos.CARRERA_O_ESPECIALIDAD.toString(), icarrera, ncarrera);
     TERMS.add(term, true, true);
     }
    
     }
     for (Integer iocupacion : CompassInfo.OCUPACIONESCAT.keySet())
     {
     String ocupacion = CompassInfo.OCUPACIONESCAT.get(iocupacion).replace('.', ' ').trim();
     String nocupacion = ocupacion;
     StringBuilder sbocupacion = new StringBuilder(ocupacion);
     CompassInfo.cleanTerm(sbocupacion);
     CompassInfo.changeCharacters(sbocupacion);
     ocupacion = sbocupacion.toString();
     Term term = new Term(ocupacion, Campos.OCUPACION.toString(), iocupacion, nocupacion);
     TERMS.addTerm(term);
     for (String var : CompassInfo.getVariantes(ocupacion))
     {
     term = new Term(var, Campos.OCUPACION.toString(), iocupacion, nocupacion);
     TERMS.add(term, true, true);
     }
    
     }
    
     for (String simref : CompassInfo.TOKENSREF)
     {
     TERMS.add(new Term(simref, REF, -1, simref), false, false);
     }
     long tfin = System.currentTimeMillis();
     long dif = tfin - tini;
     LOG.info("Fin de cargar terminos tiempo: " + dif + " ms");
    
    
    
     }
     });
     hilo.start();
    
    
    
     }*/

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
            createData(source, key, target);
        }
    }

    /**
     * Constructor por defecto
     */
    public VacanteSearch()
    {
    }

    /**
     * Clase que compara dos archivos de bitácora, para ordanarlas
     * cronológicamente
     */
    public synchronized void save(final List<Vacante> vacantes) throws BusquedaException
    {
        final CompassSession session = LUCENE.getCompass().openSession();
        CompassTransaction transaction = null;
        try
        {
            transaction = session.beginTransaction();
            for (Vacante vacante : vacantes)
            {
                final VacanteIndex index = new VacanteIndex(vacante);
                session.save(index);
            }
            transaction.commit();
            //LUCENE.getCompass().getSearchEngineIndexManager().notifyAllToClearCache();
        } catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            throw new BusquedaException(e);
        } finally
        {

            if (!session.isClosed())
            {
                session.close();
            }
        }
    }

    public synchronized void create(final List<Vacante> vacantes) throws BusquedaException
    {
        final CompassSession session = LUCENE.getCompass().openSession();
        session.getSettings().setSetting(LuceneEnvironment.Transaction.Processor.TYPE, LuceneEnvironment.Transaction.Processor.Lucene.NAME);
        CompassTransaction transaction = null;
        try
        {
            transaction = session.beginTransaction();
            for (Vacante vacante : vacantes)
            {
                final VacanteIndex index = new VacanteIndex(vacante);
                session.create(index);
            }
            transaction.commit();
            //LUCENE.getCompass().getSearchEngineIndexManager().checkAndClearIfNotifiedAllToClearCache();
        } catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            throw new BusquedaException(e);
        } finally
        {

            if (!session.isClosed())
            {
                session.close();
            }
        }
    }

    public synchronized void save(final Vacante vacante) throws BusquedaException
    {
        try
        {
            tryUpdateVacante(new VacanteIndex(vacante));
        } catch (Exception e)
        {
            throw new BusquedaException(e);
        }

    }

    private void remove(final VacanteIndex index) throws BusquedaException
    {
        try
        {
            tryDeleteVacante(index);
        } catch (Exception e)
        {
            throw new BusquedaException(e);
        }
    }

    public synchronized void remove(final int idvacante) throws BusquedaException
    {

        try
        {
            final VacanteIndex index = new VacanteIndex(idvacante);
            remove(index);
        } catch (Exception e)
        {
            throw new BusquedaException(e);
        }
    }

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
        if (campo == Campos.CARRERA_O_ESPECIALIDAD)
        {
            final Term termino = terminos.get(indice);
            procesaCarrera(termino, consulta);
        } else if (campo == Campos.OCUPACION)
        {
            final Term termino = terminos.get(indice);
            procesaOcupacion(termino, consulta);

        }
    }

    /**
     * Detiene la sincronización y los hilos de indexación, es utilizado para
     * apagar el motor
     */
    public static synchronized void stopSynchronization()
    {
        if (LUCENE.getCompass().getSearchEngineIndexManager().isLocked())
        {
            LUCENE.getCompass().getSearchEngineIndexManager().releaseLocks();
        }
    }

    /**
     * Detecta si un valor es un texto de campo
     *
     * @param ocupacion Valor a probar
     * @return True, si corresponde a un nombre de un campos, False caso
     * contrario
     */
    private boolean isCampo(final String value)
    {
        final boolean isCampo = false;
//        try
//        {
//            Campos campos = Campos.valueOf(ocupacion);
//            return campos != null;
//        }
//        catch (Exception e)
//        {
//            log.debug(e);
//        }

        for (Campos campo : Campos.values())
        {
            if (campo.toString().equals(value))
            {
                return true;
            }
        }
        return isCampo;

    }

    /**
     * Procesa terminos
     *
     * @param term Temrinos a procesar
     * @param consulta Consulta a generar
     * @param indice Indice del termino
     * @param terminos Lista de terminos
     * @param words Lista de palabras a generar en caso de que un termino no sea
     * un termino reconocido
     */
    private void procesaTermino(final Consulta consulta, final Term term, final int indice, final List<Term> terminos, final Set<String> words)
    {

        final String field = term.getLemma();
        if (field == null)
        {
            final String word = term.getText();
            if (word != null && !CompassInfo.isStopWords(word))
            {
                words.addAll(Arrays.asList(CompassInfo.tokenizer(word)));
            }
        } else
        {
            if (field.equals(REF))
            {
                procesaRef(term, words, consulta);
            } else if (isCampo(field))
            {
                final Campos campo = Campos.valueOf(field);
                procesaCampo(campo, consulta, terminos, indice);
            }

        }
    }

    private PhraseParser prepareSearch(final StringBuilder search)
    {
        CompassInfo.toLowerCase(search);
        CompassInfo.replace(search, " con conocimientos en", " ");
        CompassInfo.replace(search, " con conocimiento en", " ");
        CompassInfo.extractParentesis(search);

        final PhraseParser phraseParser = CompassInfo.getPhraseParser(search);
        CompassInfo.changeCharacters(search);
        CompassInfo.replace(search, '(', ' ');
        CompassInfo.replace(search, ')', ' ');
        CompassInfo.replace(search, '_', ' ');
        CompassInfo.replace(search, '[', ' ');
        CompassInfo.replace(search, ']', ' ');
        CompassInfo.replace(search, ')', ' ');
        CompassInfo.replace(search, ':', ' ');
        CompassInfo.replace(search, ',', ' ');
        CompassInfo.replace(search, '/', ' ');
        CompassInfo.replace(search, '-', ' ');
        CompassInfo.replaceAbreviaciones(search);
        CompassInfo.changeBadcharactersForSearchVacantes(search, false);
        CompassInfo.normaliza(search);
        CompassInfo.trim(search);
        return phraseParser;

    }

    /**
     * Función de detección de terminos y generación de consulta de busqueda
     *
     * @param search Texto a buscar
     * @return Conjunto de resultados encontrados
     * @throws BusquedaException Error en caso de sucesa un problema con los
     * indices
     */
    public Result search(ParametrosConsultaVacante parametros) throws BusquedaException
    {

        String search = parametros.search;
        final Consulta consulta = new Consulta();
        if (parametros.search == null)
        {
            parametros.search = "";
            search = parametros.search;
        }
        consulta.originalText = parametros.search;
        search = search.trim();
        final long tini = System.currentTimeMillis();
        final StringBuilder sbsearch = new StringBuilder(search);
        final PhraseParser phraseParser = prepareSearch(sbsearch);
        search = sbsearch.toString();
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
            consulta.edo = parametros.estado;
            consulta.municipio = parametros.municipio;
            consulta.setFuente(parametros.fuente);
            final Result result = search(consulta, words, null, false, parametros);

            final long tfin = System.currentTimeMillis();
            final long dif = (tfin - tini);
            LOG.info("Tiempo vacante: " + dif + " ms resultados: " + result.getCount() + " search: " + search + " cadena original: " + consulta.originalText);
            return result;

        } catch (IOException ex)
        {
            LOG.error(ERROR, ex);
            throw new BusquedaException(ex);
        }
    }

    public Result search(final String search) throws BusquedaException
    {
        ParametrosConsultaVacante p = new ParametrosConsultaVacante();
        p.search = search;
        return search(p);
    }

    public Result search(final String search, Integer estado) throws BusquedaException
    {
        ParametrosConsultaVacante p = new ParametrosConsultaVacante();
        p.search = search;
        p.estado = estado;
        return search(p);
    }

    public Result search(final String search, Integer estado, Integer municipio) throws BusquedaException
    {
        ParametrosConsultaVacante p = new ParametrosConsultaVacante();
        p.search = search;
        p.estado = estado;
        p.municipio = municipio;
        return search(p);
    }

    public Result search(final String search, CampoOrdenamiento campo) throws BusquedaException
    {
        ParametrosConsultaVacante p = new ParametrosConsultaVacante();
        p.search = search;
        p.campo = campo;
        return search(p);
    }

    public Result search(final Consulta consulta, final Set<String> words, final Set<Integer> paramsimilares, final boolean exact, ParametrosConsultaVacante parametros) throws BusquedaException
    {
        Result result = new Result(consulta);
        if (LUCENE.getCompass().isClosed())
        {
            CompassInfo.reopen();
        }
        consulta.discapacidad = parametros.discapacidad;
        consulta.setEdad(parametros.edad);
        final CompassSearchSession session = LUCENE.getCompass().openSearchSession();
        final CompassQuery query = creaConsulta(consulta, words, paramsimilares, session);
        if (query != null)
        {

            if (parametros.campo == CampoOrdenamiento.SCORE)
            {
                parametros.campo = null;
            }
            if (parametros.campo != null)
            {

                CompassQuery.SortPropertyType sortPropertyType = CompassQuery.SortPropertyType.STRING;
                if (CampoOrdenamiento.FECHA == parametros.campo)
                {
                    sortPropertyType = CompassQuery.SortPropertyType.LONG;
                    if (parametros.sort != null)
                    {
                        if (parametros.sort == Sort.AUTO)
                        {
                            parametros.sort = Sort.REVERSE;
                        } else
                        {
                            parametros.sort = Sort.AUTO;
                        }
                    }
                }
                if (CampoOrdenamiento.SALARIO == parametros.campo)
                {
                    sortPropertyType = CompassQuery.SortPropertyType.INT;
                    if (parametros.sort != null)
                    {
                        if (parametros.sort == Sort.AUTO)
                        {
                            parametros.sort = Sort.REVERSE;
                        } else
                        {
                            parametros.sort = Sort.AUTO;
                        }
                    }
                }
                if (null == parametros.sort)
                {
                    query.addSort(parametros.campo.toString(), sortPropertyType);
                } else
                {
                    if (parametros.sort == Sort.AUTO)
                    {
                        query.addSort(parametros.campo.toString(), sortPropertyType, CompassQuery.SortDirection.AUTO);
                    } else
                    {
                        query.addSort(parametros.campo.toString(), sortPropertyType, CompassQuery.SortDirection.REVERSE);
                    }
                }

            }
            final CompassHits hits = query.hits();
            result = new Result(hits, session, query.count(), consulta);
        }
        return result;

    }

//    public ArrayList<String> suggest(String word)
//    {
//        ArrayList<String> suggest = new ArrayList<String>();
//        String[] alias =
//        {
//            "vacante"
//        };
//
//        for (String newword : VacanteSearch.lucene.getCompass().getSpellCheckManager().suggestBuilder(word).aliases(alias).morePopular(true).suggest().getSuggestions())
//        {
//
//            if (CompassInfo.RAICES.containsValue(newword))
//            {
//                for (String key : CompassInfo.RAICES.keySet())
//                {
//                    String ocupacion = CompassInfo.RAICES.get(key);
//                    if (ocupacion.equals(newword))
//                    {
//                        suggest.add(key);
//                    }
//                }
//            }
//
//        }
//        return suggest;
//    }
    private CompassQuery parseQuery(final String phrase, final CompassQueryBuilder builder, final String field)
    {
        String search = phrase;
        search = CompassInfo.replaceAbreviaciones(search);
        search = prepareForQueryString(search);
        if ("".equals(search))
        {
            return null;
        }

        CompassQuery query = builder.multiPropertyQueryString(field + SEPQUERY + search + ENDSEPQUERY).toQuery();
        if (query.hits().length() == 0)
        {
            query = builder.queryString(field + SEPQUERY + search + ENDSEPQUERY).toQuery();
        }
        return query;

    }

    private List<CompassQuery> getConsultasRef(final String text, final CompassQueryBuilder builder, final String field, final Consulta consulta)
    {
        final List<CompassQuery> getConsultasRef = new ArrayList<CompassQuery>();
        if (consulta.sinonimos_ref.get(text) != null)
        {
            for (String ref : consulta.sinonimos_ref.get(text))
            {
                if (!ref.startsWith(text))
                {
                    if (text.indexOf(' ') == -1)
                    {
                        final String[] sinonimos = CompassInfo.getSinonimos(text);
                        if (sinonimos == null)
                        {
                            final CompassQuery query = parseQuery(ref, builder, field);
                            getConsultasRef.add(query);
                        } else
                        {
                            boolean found = false;
                            for (String sin : sinonimos)
                            {
                                if (ref.startsWith(sin))
                                {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                            {
                                final CompassQuery query = parseQuery(ref, builder, field);
                                getConsultasRef.add(query);
                            }
                        }

                    } else
                    {
                        final CompassQuery query = parseQuery(ref, builder, field);
                        getConsultasRef.add(query);
                    }
                }
            }
        }
        return getConsultasRef;
    }

    private CompassQuery createMultiWordQuery(final Set<String> words, final CompassQueryBuilder builder)
    {
        final String[] fields =
        {
            TITULO, CONOCIMIENTO, CARRERAWORDS, OCUPACIONWORDS
        };
        final SetQueries word_queries = new SetQueries();
        for (String field : fields)
        {
            final CompassQueryBuilder.CompassBooleanQueryBuilder bool_words = builder.bool();

            for (String word : words)
            {
                if (word.indexOf(' ') == -1)
                {
                    final CompassQuery query = builder.term(field, word);
                    bool_words.addMust(query);
                } else
                {
                    CompassQuery query = builder.multiPropertyQueryString(field + SEPQUERY + word + ENDSEPQUERY).toQuery();
                    if (query.hits().length() == 0)
                    {
                        query = builder.multiPropertyQueryString(field + ":" + word).toQuery();
                        if (query.hits().length() == 0)
                        {
                            query = builder.queryString(field + SEPQUERY + word + ENDSEPQUERY).toQuery();
                            if (query.hits().length() == 0)
                            {
                                query = builder.queryString(field + ":" + word).toQuery();
                            }
                        }
                    }
                    bool_words.addMust(query);
                }
            }
            word_queries.add(bool_words.toQuery());
        }
        final CompassQueryBuilder.CompassBooleanQueryBuilder querywords = builder.bool();
        if (!word_queries.isEmpty())
        {
            for (CompassQuery query : word_queries)
            {
                querywords.addShould(query);
            }
        }
        return querywords.toQuery();
    }

    private SetQueries createQueriesForTitle(final String text, final CompassQueryBuilder builder, final Consulta consulta)
    {
        final SetQueries set_titulo = new SetQueries();
        if (text == null || text.trim().equals(""))
        {
            return set_titulo;
        }
        final CompassQuery qtitulo = parseQuery(text, builder, TITULO);
        if (qtitulo != null)
        {
            set_titulo.add(qtitulo);
        }
        for (CompassQuery qref : getConsultasRef(text, builder, TITULO, consulta))
        {
            if (qref != null)
            {
                set_titulo.add(qref);
            }
        }
        final String norText = CompassInfo.normaliza(consulta.originalText);
        for (String variante : CompassInfo.getVariantes(norText))
        {
            if (!variante.startsWith(text))
            {
                final CompassQuery query = parseQuery(variante, builder, TITULO);
                if (query != null)
                {
                    set_titulo.add(query);
                }
            }
        }
        return set_titulo;
    }

    private String prepareForQueryString(final String word)
    {
        String wordToReturn = word;
        final int pos = wordToReturn.indexOf('(');
        if (pos != -1)
        {
            wordToReturn = wordToReturn.substring(0, pos);
        }
        wordToReturn = QueryParser.escape(wordToReturn);
        return wordToReturn.trim();
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

    /**
     * builder.term(field, "jef").hits().data(57) Busca vacantes con un objeto
     * de consulta , un conjunto de palabras y una lista de carreras similares
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
        final List<CompassQuery> queries = new ArrayList<CompassQuery>();

        //SetQueries titulos = new SetQueries();
        final CompassQueryBuilder builder = session.queryBuilder();
        final SetQueries optional = new SetQueries();
        llenaEdadBusqueda(consulta, builder, queries);
        creaConsultaconTitulo(consulta, builder, optional);
        llenaBusquedadFuente(consulta, builder, queries);
        llenaBusquedadDiscapacidad(consulta, builder, queries);
        llenaEstadoMunicpio(consulta, builder, queries);
        try
        {
            creaConsultaPharses(words, builder, queries);
            creaConsultaPalabras(words, builder, queries);
            final SetQueries ocupaCarreras = new SetQueries();
            final Set<Integer> carreras_totales = creaConsultaCarreras(consulta, ocupaCarreras, builder);
            creaConsultaConocimientos(consulta, builder, ocupaCarreras);
            final Set<Integer> ocupaciones = creaConsultaOcupaciones(consulta, ocupaCarreras, builder);
            creaConsultaRef(consulta, ocupaciones, builder, ocupaCarreras, carreras_totales);
            creaOcupaCarreras(ocupaCarreras, builder, queries);
            if (queries.isEmpty() && optional.isEmpty())
            {
                LOG.trace("Consulta: queries.isEmpty() && optional.isEmpty() cadena procesada: \"" + consulta.text + "\" cadena original: \"" + consulta.originalText + ENDSEPQUERY);
                //return new Result(consulta);
                return null;
            }
            final CompassQueryBuilder.CompassBooleanQueryBuilder qSearch = builder.bool();
            obligatorias(queries, builder, qSearch);
            opcionales(optional, builder, consulta, qSearch);
            final CompassQuery query = qSearch.toQuery();
            final CompassHits hits = query.hits();
            LOG.trace("Consulta: " + query.toString() + " alias: " + VACANTE + " resultados: " + hits.getLength() + " cadena original: " + consulta.originalText + " cadena procesada: " + consulta.text + " ,criterios: " + (consulta == null ? null : consulta.toString()));
            return query;
        } catch (RuntimeException e)
        {
            LOG.error("Error en consulta " + consulta.originalText, e);
            throw e;
        }
        /*finally
         {
         compassSearchSession.close();
         }*/
    }

    /**
     * Guarda una vacante en los índices
     *
     * @param vacante Vacante
     * @param session Sesión de Compass
     * @throws Exception
     */
    private static void tryUpdateVacante(final VacanteIndex index) throws BusquedaException
    {
        try
        {
            if (LUCENE.getCompass().isClosed())
            {
                CompassInfo.reopen();
            }
            CompassIndexSession session = LUCENE.getCompass().openIndexSession();
            LUCENE.getCompass().getSearchEngineIndexManager().refreshCache(VACANTE);
            if (session.isClosed())
            {
                session = LUCENE.getCompass().openIndexSession();
            }

            try
            {
                session.save(index);
                session.commit();
                //LUCENE.getCompass().getSearchEngineIndexManager().notifyAllToClearCache();
            } catch (Exception e)
            {
                session.rollback();
                LOG.error("vacante error " + index.id);
                throw new BusquedaException(e);
            } finally
            {
                if (!session.isClosed())
                {
                    session.close();
                }
            }

        } catch (SearchEngineException se)
        {
            LOG.error(ERROR_SALVANDO_VACANTE_ + index.id, se);
            throw se;

        } catch (CompassException ce)
        {
            LOG.error(ERROR_SALVANDO_VACANTE_ + index.id, ce);
            throw ce;
        } catch (IllegalStateException ce)
        {
            LOG.error(ERROR_SALVANDO_VACANTE_ + index.id, ce);
            throw ce;
        } catch (RuntimeException ce)
        {
            LOG.error(ERROR_SALVANDO_VACANTE_ + index.id, ce);
            throw ce;
        }
    }

    /**
     * Función que guarda un vacante
     *
     * @param vacante vacante a indexar
     * @param session Sesión de compass
     * @throws Exception Error en caso de no poder indexar un vacante
     */
    /*private static synchronized void updateVacante(final Vacante vacante) throws BusquedaException
     {
     final VacanteIndex index = new VacanteIndex(vacante);
     //server.send(index);
    
     try
     {
     tryUpdateVacante(index);
     }
     catch (Exception e)
     {
     try
     {
     Thread.sleep(500);
     }
     catch (Exception e2)
     {
     LOG.error(ERROR, e2);
     }
     LOG.info("Segundo intento para " + vacante.id);
     tryUpdateVacante(index);
     LOG.info("Fin de Segundo intento para " + vacante.id);
     }
    
    
     }*/

    /*private static void updateVacante(final VacanteIndex index) throws BusquedaException
     {
     //VacanteIndex index = new VacanteIndex(vacante);
     //server.send(index);
    
     try
     {
     tryUpdateVacante(index);
     }
     catch (Exception e)
     {
     try
     {
     Thread.sleep(500);
     }
     catch (Exception e2)
     {
     LOG.error(ERROR, e2);
     }
     LOG.info("Segundo intento para " + index.id);
     tryUpdateVacante(index);
     LOG.info("Fin de Segundo intento para " + index.id);
     }
    
    
     }*/
    private static void tryDeleteVacante(final VacanteIndex index) throws BusquedaException
    {
        try
        {
            if (LUCENE.getCompass().isClosed())
            {
                CompassInfo.reopen();
            }
            CompassIndexSession session = LUCENE.getCompass().openIndexSession();
            LUCENE.getCompass().getSearchEngineIndexManager().refreshCache(VACANTE);
            if (session.isClosed())
            {
                session = LUCENE.getCompass().openIndexSession();
            }

            try
            {
                session.delete(index);
                session.commit();
                //LUCENE.getCompass().getSearchEngineIndexManager().notifyAllToClearCache();
            } catch (Exception e)
            {
                session.rollback();
                LOG.error("vacante error " + index.id);
                throw new BusquedaException(e);
            } finally
            {
                if (!session.isClosed())
                {
                    session.close();
                }
            }

        } catch (SearchEngineException se)
        {
            LOG.error(ERROR_SALVANDO_VACANTE_ + index.id, se);
            throw se;
        } catch (CompassException ce)
        {
            LOG.error(ERROR_SALVANDO_VACANTE_ + index.id, ce);
            throw ce;
        } catch (IllegalStateException ce)
        {
            LOG.error(ERROR_SALVANDO_VACANTE_ + index.id, ce);
            throw ce;
        } catch (RuntimeException ce)
        {
            LOG.error(ERROR_SALVANDO_VACANTE_ + index.id, ce);
            throw ce;
        }
    }

    /**
     * Obtiene una vacante de los indices
     *
     * @param idvacante Id de la vacante, si no existe la vacante regresa null
     * @return Vacante encontrada, null si no existe
     */
    public Vacante get(final long idvacante)
    {

        if (LUCENE.getCompass().isClosed())
        {
            CompassInfo.reopen();
        }

        final CompassSearchSession session = LUCENE.getCompass().openSearchSession();
        try
        {
            final VacanteIndex vacante = (VacanteIndex) session.load(VacanteIndex.class, idvacante);
            if (vacante != null)
            {
                return vacante.get();
            }
        } catch (CompassException ce)
        {
            LOG.error("Error al buscar vacante " + idvacante, ce);
        } finally
        {
            session.close();
        }
        return null;
    }

    public Result getVacantes()
    {
        final long tini = System.currentTimeMillis();
        if (LUCENE.getCompass().isClosed())
        {
            CompassInfo.reopen();
        }
        final CompassSearchSession compassSearchSession = LUCENE.getCompass().openSearchSession();
        final CompassQueryBuilder builder = compassSearchSession.queryBuilder();
        try
        {
            final CompassQuery query = builder.alias(VACANTE);
            query.addSort(CompassQuery.SortImplicitType.DOC);
            final long count = query.count();
            final long tfin = System.currentTimeMillis();
            final long dif = (tfin - tini);
            LOG.trace("Tiempo de conteo: " + dif + " ms, count:" + count);
            return new Result(query.hits(), compassSearchSession, count, null);

        } catch (RuntimeException e)
        {
            LOG.error(ERROR, e);
            throw e;
        }

    }

    /**
     * Regresa el número de vacantes existentes en los indices
     *
     * @return Número de vacantes existentes en los indices
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
            final CompassQuery query = builder.alias(VACANTE);
            final long count = query.count();
            final long tfin = System.currentTimeMillis();
            final long dif = (tfin - tini);
            LOG.trace("Tiempo de conteo: " + dif + " ms, count:" + count);
            return count;
        } catch (RuntimeException e)
        {
            LOG.error(ERROR, e);
            throw e;
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    /**
     * Verifica la conpatibilidad de dos palabras considerando sus sinonimos
     *
     * @param source palabra a probar
     * @param target palabra de referencia
     * @return True si son compatibles, false caso contrario
     */
    public static boolean checkCompatibility(final String source, final String target)
    {
        return CandidatoSearch.checkCompatibility(source, target);
    }

    public synchronized static void removeIndice()
    {
        CompassInfo.removeIndice(VACANTE);
    }

    public SWBTermDictionary getSWBTermDictionary()
    {
        return TERMS.getSWBTermDictionary();
    }

    public static Term[] getTerms(final Term term)
    {
        return TERMS.getTerms(term);
    }

    private void llenaBusquedadDiscapacidad(final Consulta consulta, final CompassQueryBuilder builder, final List<CompassQuery> queries)
    {
        if (consulta.discapacidad != null && consulta.discapacidad.length() == 5)
        {
            String _consulta = consulta.discapacidad;
            /*if("11111".equals(_consulta))
             {
             CompassBooleanQueryBuilder boolq=builder.bool();
             CompassQuery qDiscapacidad = builder.queryString("DISCAPACIDAD:" + _consulta.replace('1', '?')).toQuery();
             boolq.addMust(qDiscapacidad);
             CompassQuery notqDiscapacidad = builder.queryString("DISCAPACIDAD:" + "00000").toQuery();
             boolq.addMustNot(notqDiscapacidad);
             queries.add(boolq.toQuery());
             }
             else
             {
             CompassQuery qDiscapacidad = builder.queryString("DISCAPACIDAD:" + _consulta.replace('1', '?')).toQuery();
             queries.add(qDiscapacidad);
             }*/
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
            CompassQuery qfuente = builder.term("FUENTE", consulta.fuente);
            queries.add(qfuente);
        }

    }

    private void llenaEdadBusqueda(Consulta consulta, CompassQueryBuilder builder, List<CompassQuery> queries)
    {
        Integer edad = consulta.getEdadDe();
        if (edad != null)
        {
            CompassBooleanQueryBuilder qEdad = builder.bool();

            CompassBooleanQueryBuilder and = builder.bool();
            CompassQuery qEdad_de = builder.le("EDAD_DE", edad);
            CompassQuery qEdad_hasta = builder.ge("EDAD_HASTA", edad);
            and.addMust(qEdad_de);//and
            and.addMust(qEdad_hasta);          //and
            CompassQuery qNotEdad_hasta = builder.term("EDAD_HASTA", -1);
            CompassQuery qNotEdad_de = builder.term("EDAD_DE", -1);

            and.addMustNot(qNotEdad_hasta);
            and.addMustNot(qNotEdad_de);

            qEdad.addShould(and.toQuery()); // or

            CompassQuery qEdad_de_indistinto = builder.term("EDAD_DE", -1);
            CompassQuery qEdad_hasta_indistinto = builder.term("EDAD_HASTA", -1);
            qEdad.addShould(qEdad_de_indistinto);         //or
            qEdad.addShould(qEdad_hasta_indistinto);      //or          
            queries.add(qEdad.toQuery());

        }
    }
}
