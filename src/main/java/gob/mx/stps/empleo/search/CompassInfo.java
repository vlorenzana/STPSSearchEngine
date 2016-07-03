/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.SingleTokenTokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * Librería de acceso a compass, variantes, sinónimos, etc.
 * @author victor.lorenzana
 */
public final class CompassInfo
{

    private static final Locale LOCALE = new Locale("es", "MX");
    //private static final String SPANISH = "Spanish";
    public static final String SPANISH = "SpanishSTPS";
    private static final DecimalFormat CURRENCY2 = new DecimalFormat("#########", new DecimalFormatSymbols(new Locale("es", "MX")));
    private static final DecimalFormat CURRENCY = new DecimalFormat("###,###,###", new DecimalFormatSymbols(new Locale("es", "MX")));
    /**
     * Objeto compass
     */
    private static Compass compass;
    /**
     * Log de mensajes
     */
    private static final Log LOG = LogFactory.getLog(CompassInfo.class);
    private static String segmentos = "200";
    private static String schedule = "false";
    private static String period = "90";
    private static final String NAME = "/gob/mx/stps/empleo/search/STPSSearchIndexer.properties";
    private static final Properties PROPERTIES = new Properties();
    private static final Map<String, Set<String>> ABREVIACIONES = Collections.synchronizedMap(new HashMap<String, Set<String>>());
    public static final Map<String, Set<String>> ABREVIACIONESNOM = Collections.synchronizedMap(new HashMap<String, Set<String>>());
    //private static final Map<String, Set<String>> replace = Collections.synchronizedMap(new HashMap<String, Set<String>>());
    // sinonimos de palabras a detectar
    private static final List<Set<String>> SINONIMOS = Collections.synchronizedList(new ArrayList<Set<String>>());
    public static final List<List<String>> SINREFERENCIALES = Collections.synchronizedList(new ArrayList<List<String>>());
    public static final List<String> TOKENSREF = new ArrayList<String>();
    // Variantes de terminos a detectar
    private static final List<Set<String>> VARIANTES = Collections.synchronizedList(new ArrayList<Set<String>>());
    public static final List<CarrerasOcupacionSimilares> CARRERASSIMILARES = Collections.synchronizedList(new ArrayList<CarrerasOcupacionSimilares>());
    public static final List<CarrerasOcupacionSimilares> OCUPASIMILARES = Collections.synchronizedList(new ArrayList<CarrerasOcupacionSimilares>());
    public static final Set<String> STOPWORDS = Collections.synchronizedSet(new HashSet<String>());
    public static final Map<String, String> RAICES = Collections.synchronizedMap(new HashMap<String, String>());
    public static final Map<String, String> PALABRASSINONIMOS = Collections.synchronizedMap(new HashMap<String, String>());
    public static final Map<Integer, String> DOMINIOS = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> IDIOMAS = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> CARRERASCAT = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Set<String> CARRERASNORM = Collections.synchronizedSet(new HashSet<String>());
    public static final Set<String> OCUPACIONNORM = Collections.synchronizedSet(new HashSet<String>());
    public static final Map<Integer, String> CARRERAS = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> STATUS = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> GRADOS = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> OCUPACIONES = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> EXPERIENCIA = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> HORARIO = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> OCUPACIONESCAT = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> ESTADOS = Collections.synchronizedMap(new HashMap<Integer, String>());
    public static final Map<Integer, String> MUNICIPIOS = Collections.synchronizedMap(new HashMap<Integer, String>());

    public static String loadFile(final String file)
    {

        final InputStream input = CompassInfo.class.getResourceAsStream("/gob/mx/stps/empleo/search/" + file);
        final StringBuilder content = new StringBuilder();
        if (input == null)
        {
            LOG.error("No en sencontro el archivo: " + file);
        }
        else
        {
            final InputStreamReader isr = new InputStreamReader(input, Charset.forName("utf-8"));
            final BufferedReader buffer = new BufferedReader(isr);
            try
            {
                String line = buffer.readLine();
                while (line != null)
                {
                    content.append(line);
                    content.append("\r\n");
                    line = buffer.readLine();

                }
            }
            catch (EOFException eof)
            {
                LOG.debug(eof);
            }
            catch (IOException ioe)
            {
                LOG.error("Error", ioe);
            }
        }
        return content.toString();
    }

    static
    {


        final InputStream inputresource = CompassInfo.class.getResourceAsStream(NAME);
        try
        {


            LOG.info("Versión buscador 5.0");
            LOG.info("-->Loading Property File:" + NAME);
            PROPERTIES.load(inputresource);
            {
                {
                    final String data = loadFile("ocupaciones_similares.txt");
                    final StringReader reader = new StringReader(data);
                    final BufferedReader afile = new BufferedReader(reader);
                    String line = afile.readLine();
                    while (line != null)
                    {
                        if (!line.trim().startsWith("#"))
                        {
                            final StringTokenizer separatorComa = new StringTokenizer(line, ",");
                            if (separatorComa.countTokens() > 1)
                            {
                                final CarrerasOcupacionSimilares tokens = new CarrerasOcupacionSimilares();
                                while (separatorComa.hasMoreTokens())
                                {
                                    String tokenToAdd = separatorComa.nextToken().trim();
                                    final StringTokenizer separatorSpace = new StringTokenizer(tokenToAdd, " ");
                                    while (separatorSpace.hasMoreTokens())
                                    {
                                        tokenToAdd = separatorSpace.nextToken().trim();
                                        try
                                        {
                                            tokens.add(Integer.parseInt(tokenToAdd));
                                        }
                                        catch (NumberFormatException nfe)
                                        {
                                            LOG.warn(nfe);
                                            LOG.warn("El valor de una ocupación no es númerico " + line);
                                        }
                                    }
                                }
                                OCUPASIMILARES.add(tokens);
                            }
                        }
                        line = afile.readLine();
                    }


                }

                final String data = loadFile("carreras_similares.txt");
                final StringReader reader = new StringReader(data);
                final BufferedReader afile = new BufferedReader(reader);
                String line = afile.readLine();
                while (line != null)
                {
                    if (!line.trim().startsWith("#"))
                    {
                        final StringTokenizer separatorComa = new StringTokenizer(line, ",");
                        if (separatorComa.countTokens() > 1)
                        {
                            final CarrerasOcupacionSimilares tokens = new CarrerasOcupacionSimilares();
                            while (separatorComa.hasMoreTokens())
                            {
                                String tokenToAdd = separatorComa.nextToken().trim();
                                final StringTokenizer separtatorSpace = new StringTokenizer(tokenToAdd, " ");
                                while (separtatorSpace.hasMoreTokens())
                                {
                                    tokenToAdd = separtatorSpace.nextToken().trim();
                                    try
                                    {
                                        tokens.add(Integer.parseInt(tokenToAdd));
                                    }
                                    catch (NumberFormatException nfe)
                                    {
                                        LOG.warn("El valor de una carrera no es numerico: " + line);
                                    }
                                }

                            }
                            CARRERASSIMILARES.add(tokens);
                        }
                    }
                    line = afile.readLine();
                }
            }
            {
                final String data = loadFile("abreviations.txt");
                final StringReader reader = new StringReader(data);
                final BufferedReader afile = new BufferedReader(reader);
                String line = afile.readLine();
                while (line != null)
                {
                    if (!line.trim().startsWith("#"))
                    {
                        final StringTokenizer separatorComa = new StringTokenizer(line, ",");
                        if (separatorComa.countTokens() > 1)
                        {
                            final StringBuilder token = new StringBuilder(separatorComa.nextToken());
                            toLowerCase(token);
                            procesaAbreviaciones(separatorComa, token.toString());
                        }
                    }
                    line = afile.readLine();

                }
            }


            segmentos = PROPERTIES.getProperty("MAX_NUMBER_OF_SEGMENTS", segmentos);
            schedule = PROPERTIES.getProperty("SCHEDULE", schedule);
            period = PROPERTIES.getProperty("SCHEDULE_PERIOD", period);
            {
                final String data = loadFile("variantes.txt");


                final StringReader reader = new StringReader(data);
                final BufferedReader afile = new BufferedReader(reader);
                String line = afile.readLine();
                while (line != null)
                {
                    if (!line.trim().startsWith("#"))
                    {
                        final StringTokenizer separatorComa = new StringTokenizer(line, ",");
                        final Set<String> set = new HashSet<String>();
                        while (separatorComa.hasMoreTokens())
                        {
                            final String tokenToAdd = separatorComa.nextToken().trim();
                            procesaVariantesAbreviaciones(tokenToAdd, set);
                        }
                        if (!set.isEmpty())
                        {
                            VARIANTES.add(set);
                        }
                    }
                    line = afile.readLine();
                }


            }
            {
                final String data = loadFile("stopwords.txt");
                final StringReader reader = new StringReader(data);
                final BufferedReader afile = new BufferedReader(reader);
                String line = afile.readLine();
                while (line != null)
                {
                    if (!line.trim().startsWith("#"))
                    {
                        final StringTokenizer separatorComa = new StringTokenizer(line, ",");
                        while (separatorComa.hasMoreTokens())
                        {
                            final StringBuilder tokenToAdd = new StringBuilder(separatorComa.nextToken().trim());
                            if (!"".equals(tokenToAdd.toString().trim()))
                            {
                                changeCharacters(tokenToAdd);
                                STOPWORDS.add(tokenToAdd.toString());
                            }
                        }
                    }
                    line = afile.readLine();
                }


            }
            {
                final String data = loadFile("sinonimos.txt");

                final HashSet<String> sininomos_words = new HashSet<String>();

                final StringReader reader = new StringReader(data);
                final BufferedReader afile = new BufferedReader(reader);
                String line = afile.readLine();
                while (line != null)
                {
                    if (!line.trim().startsWith("#"))
                    {
                        final StringTokenizer separatorComa = new StringTokenizer(line, ",");
                        final Set<String> set = new HashSet<String>();
                        while (separatorComa.hasMoreTokens())
                        {
                            final StringBuilder tokenToAdd = new StringBuilder(separatorComa.nextToken().trim());
                            if (!"".equals(tokenToAdd.toString().trim()) && tokenToAdd.toString().indexOf(' ') == -1)
                            {

                                changeCharacters(tokenToAdd);
                                final SingleTokenTokenStream tokens = new SingleTokenTokenStream(new org.apache.lucene.analysis.Token(tokenToAdd.toString(), 0, tokenToAdd.length()));
                                final SnowballFilter filter = new SnowballFilter(tokens, SPANISH);
                                final String raizword = filter.next().term();
                                set.add(raizword);
                                sininomos_words.add(raizword);
                                RAICES.put(tokenToAdd.toString(), raizword);
                            }
                        }
                        if (!set.isEmpty())
                        {
                            agregaSinonimos(set);
                        }
                    }
                    line = afile.readLine();
                }
                for (String word : sininomos_words)
                {
                    final HashSet<String> getSinonimos = new HashSet<String>();
                    for (Set<String> similars : SINONIMOS)
                    {
                        if (similars.contains(word))
                        {
                            getSinonimos.addAll(similars);
                        }
                    }
                    if (!getSinonimos.isEmpty())
                    {
                        final ArrayList<String> _sinonimos = new ArrayList<String>();
                        _sinonimos.addAll(getSinonimos);
                        final String[] lsinonimos = _sinonimos.toArray(new String[_sinonimos.size()]);
                        Arrays.sort(lsinonimos, new Comparator<String>()
                        {

                            @Override
                            public final int compare(final String o1, final String o2)
                            {
                                return o1.compareToIgnoreCase(o2);
                            }
                        });
                        if (lsinonimos.length > 0)
                        {

                            final String sinonimo = lsinonimos[0];
                            PALABRASSINONIMOS.put(word, sinonimo);
                        }

                    }

                }


            }
            {
                final String data = loadFile("referenciables.txt");
                final StringReader reader = new StringReader(data);
                final BufferedReader afile = new BufferedReader(reader);
                String line = afile.readLine();
                final Set<String> terms_ref_set = new HashSet<String>();
                while (line != null)
                {
                    if (!line.trim().startsWith("#"))
                    {
                        final StringTokenizer separatorComa = new StringTokenizer(line, ",");
                        final List<String> set = new ArrayList<String>();


                        while (separatorComa.hasMoreTokens())
                        {
                            final String tokenToAdd = separatorComa.nextToken().trim();
                            if (!"".equals(tokenToAdd.trim()))
                            {

                                final String raizword = analizerTokenizer(tokenToAdd).trim();
                                terms_ref_set.add(raizword);
                                set.add(raizword);

                            }
                        }
                        if (!set.isEmpty())
                        {
                            SINREFERENCIALES.add(set);
                        }

                    }
                    line = afile.readLine();
                }

                final String[] term_sorted = terms_ref_set.toArray(new String[terms_ref_set.size()]);
                Arrays.sort(term_sorted, new Comparator<String>()
                {

                    @Override
                    public final int compare(final String o1, final String o2)
                    {
                        final Integer integerToCompare = Integer.valueOf(o2.length());
                        return integerToCompare.compareTo(o1.length());
                    }
                });
                TOKENSREF.addAll(Arrays.asList(term_sorted));


            }

            final URL inCompassConfig = CompassInfo.class.getResource("/gob/mx/stps/empleo/search/CompassConfig.xml");
            if (inCompassConfig != null)
            {
                CompassConfiguration conf = new CompassConfiguration().configure(inCompassConfig);
                conf = conf.setSetting(LuceneEnvironment.Optimizer.MAX_NUMBER_OF_SEGMENTS, segmentos);
                conf = conf.setSetting(LuceneEnvironment.Optimizer.SCHEDULE, schedule);
                conf = conf.setSetting(LuceneEnvironment.Optimizer.SCHEDULE_PERIOD, period);
                //conf = conf.setSetting("compass.transaction.isolation", "batch_insert");               
                final StringBuilder stopwords_content = new StringBuilder();
                for (String stop : STOPWORDS)
                {
                    stopwords_content.append(stop);
                    stopwords_content.append(",");
                }
                String _stopwords = stopwords_content.toString();
                if (_stopwords.endsWith(","))
                {
                    _stopwords = _stopwords.substring(0, _stopwords.length() - 1);
                }
                conf = conf.setSetting("compass.engine.analyzer.default.stopwords", _stopwords);
                conf = conf.setSetting("compass.engine.analyzer.default.name", "SpanishSTPS");

                //conf = conf.setSetting(LuceneEnvironment.SpellCheck.ENABLE, "true");
                conf = conf.addClass(CandidatoIndex.class);
                conf = conf.addClass(VacanteIndex.class);
                compass = conf.buildCompass();
                try
                {
                    if (compass.getSearchEngineIndexManager().isLocked())
                    {
                        compass.getSearchEngineIndexManager().releaseLocks();
                    }
                    LOG.info("compass.transaction.lockTimeout: " + compass.getSettings().getSetting("compass.transaction.lockTimeout"));
                    LOG.info("compass.transaction.lockDir: " + compass.getSettings().getSetting("compass.transaction.lockDir"));
                    LOG.info("compass.transaction.commitTimeout: " + compass.getSettings().getSetting("compass.transaction.commitTimeout"));
                    LOG.info("compass.transaction.lockPollInterval: " + compass.getSettings().getSetting("v"));
                    
                    //LOG.info("compass.engine.store.lockFactory.type: " + compass.getSettings().getSetting("compass.engine.store.lockFactory.type"));
                    //compass.engine.store.lockFactory.type

                }
                catch (Exception e)
                {
                    LOG.warn("Error al incializar compass", e);
                }
            }

        }
        catch (Exception e)
        {
            LOG.error("Error reading property file:" + NAME, e);
        }
        try
        {
            inputresource.close();
        }
        catch (Exception e)
        {
            LOG.error("Error", e);
        }
        //pingServer = new PingServer(port);
        //pingServer.start();
    }

    public static boolean isStopWords(final String word)
    {
        boolean isStopword = false;
        if (word == null)
        {
            isStopword = true;
        }
        else if (",".equals(word))
        {
            isStopword = true;
        }
        else
        {
            isStopword = STOPWORDS.contains(word);
        }

        return isStopword;
    }

    public static Map<String, Set<String>> getAbreviaciones()
    {
        return ABREVIACIONES;
    }

//    public Map<String, String> getGeneros()
//    {
//        return generos;
//    }
    public static Integer[] getCarrerasSimilaresCatalogo(final Integer value)
    {
        final HashSet<Integer> setSimilares = new HashSet<Integer>();
        for (Set<Integer> set : CARRERASSIMILARES)
        {
            if (set.contains(value))
            {
                setSimilares.addAll(set);
            }
        }
        return setSimilares.toArray(new Integer[setSimilares.size()]);
    }

    public static Integer[] getOcupacionesSimilaresCatalogo(final Integer value)
    {
        final HashSet<Integer> setSimilares = new HashSet<Integer>();

        for (Set<Integer> set : OCUPASIMILARES)
        {
            if (set.contains(value))
            {
                setSimilares.addAll(set);
            }
        }
        return setSimilares.toArray(new Integer[setSimilares.size()]);
    }

    public static void toLowerCase(final StringBuilder data)
    {
        for (int i = 0; i < data.length(); i++)
        {
            final char _char = data.charAt(i);
            final char _newchar = Character.toLowerCase(_char);
            data.setCharAt(i, _newchar);
        }
    }

    public static String changeCharacters(final String data)
    {
        final StringBuilder sbdata = new StringBuilder(data);
        changeCharacters(sbdata);
        return sbdata.toString();
    }

    public static void changeCharacters(final StringBuilder data)
    {
        toLowerCase(data);
        replace(data, '[', ' ');
        replace(data, ']', ' ');
        replace(data, '/', ' ');
        replace(data, ';', ' ');
        replace(data, ':', ' ');
        replace(data, '-', ' ');
        replace(data, ',', ' ');
        replace(data, 'á', 'a');
        replace(data, 'é', 'e');
        replace(data, 'í', 'i');
        replace(data, 'ó', 'o');
        replace(data, 'ú', 'u');
        replace(data, 'à', 'a');
        replace(data, 'è', 'e');
        replace(data, 'ì', 'i');
        replace(data, 'ò', 'o');
        replace(data, 'ù', 'u');
        replace(data, 'ü', 'u');


        int pos = data.indexOf("  ");
        while (pos != -1)
        {
            data.replace(pos, pos + 2, " ");
            pos = data.indexOf("  ", pos);
        }
        /*final StringBuilder content=new StringBuilder();
        boolean add = true;
        for (char schar : data.toString().toCharArray())
        {
        if (schar == ' ')
        {
        if (add)
        {
        content.append(schar);
        add = false;
        }
        }
        else
        {
        content.append(schar);
        add = true;
        }
        
        }*/

    }

    public static List<Set<String>> getVariantes()
    {
        return VARIANTES;
    }

    public static String[] getVariantes(final String value)
    {
        final HashSet<String> getVariantes = new HashSet<String>();
        for (Set<String> similars : VARIANTES)
        {
            if (similars.contains(value))
            {
                getVariantes.addAll(similars);
            }
        }
        getVariantes.remove(value);
        return getVariantes.toArray(new String[getVariantes.size()]);
    }

    @SuppressWarnings("unchecked")
    public static Set<String>[] getSinonimos()
    {
        final Set[] sin = new Set[SINONIMOS.size()];
        return SINONIMOS.toArray(sin);
    }

    public static String getPrincipalSinonimo(final String value)
    {
        if (PALABRASSINONIMOS.containsKey(value))
        {
            return PALABRASSINONIMOS.get(value);
        }
        else
        {
            return value;
        }
    }

    public static String[] getSinonimos(final String value)
    {
        final HashSet<String> getSinonimos = new HashSet<String>();
        for (Set<String> similars : SINONIMOS)
        {
            if (similars.contains(value))
            {
                getSinonimos.addAll(similars);
            }
        }
        getSinonimos.remove(value);
        return getSinonimos.toArray(new String[getSinonimos.size()]);
    }

    public static String[] getSinonimosReferenciales(final String value)
    {
        final HashSet<String> getSinonimos = new HashSet<String>();
        for (List<String> similars : SINREFERENCIALES)
        {
            if (similars.size() > 0 && similars.contains(value))
            {
                final int index = similars.indexOf(value);
                for (int i = index; i < similars.size(); i++)
                {
                    getSinonimos.add(similars.get(i));
                }
            }
        }
        return getSinonimos.toArray(new String[getSinonimos.size()]);
    }

    public static synchronized void reopen()
    {
        if (compass == null || compass.isClosed())
        {
            LOG.info("Abriendo compass de nuevo ");
            final URL inCompassConfig = CompassInfo.class.getResource("/gob/mx/stps/empleo/search/CompassConfig.xml");
            if (inCompassConfig != null)
            {
                CompassConfiguration configuration = new CompassConfiguration().configure(inCompassConfig);
                configuration = configuration.setSetting(LuceneEnvironment.Optimizer.MAX_NUMBER_OF_SEGMENTS, segmentos);
                configuration = configuration.setSetting(LuceneEnvironment.Optimizer.SCHEDULE, schedule);
                configuration = configuration.setSetting(LuceneEnvironment.Optimizer.SCHEDULE_PERIOD, period);
                final StringBuilder content = new StringBuilder();
                for (String stop : STOPWORDS)
                {
                    content.append(stop);
                    content.append(",");
                }
                String _stopwords = content.toString();
                if (_stopwords.endsWith(","))
                {
                    _stopwords = _stopwords.substring(0, _stopwords.length() - 1);
                }
                configuration = configuration.setSetting("compass.engine.analyzer.default.stopwords", _stopwords);
                configuration = configuration.setSetting("compass.engine.analyzer.default.name", "SpanishSTPS");
                configuration = configuration.addClass(CandidatoIndex.class);
                configuration = configuration.addClass(VacanteIndex.class);
                compass = configuration.buildCompass();
            }
        }
    }

    private static void collectExp(final Integer index, final Integer años, final Set<Integer> experiencia)
    {
        final String desc = EXPERIENCIA.get(index).toLowerCase(LOCALE);
        if (desc.startsWith("ninguna") && años == 0)
        {
            experiencia.add(index);
        }
        else if (desc.startsWith("no es requisito") && años == 0)
        {
            experiencia.add(index);
        }
        else if (desc.startsWith("6m") && años == 1)
        {
            experiencia.add(index);
        }
        else if (desc.toCharArray()[0] == '1' && años <= 2)
        {
            experiencia.add(index);
        }
        else if (desc.toCharArray()[0] == '2' && años <= 3)
        {
            experiencia.add(index);
        }
        else if (desc.toCharArray()[0] == '3' && años <= 4)
        {
            experiencia.add(index);
        }
        else if (desc.toCharArray()[0] == '4' && años <= 5)
        {
            experiencia.add(index);
        }
        else if (desc.startsWith("más de 5 años") && años >= 6)
        {
            experiencia.add(index);
        }
    }

    private static void procesaAbreviaciones(final StringTokenizer separatorComa, final String token)
    {
        final Set<String> tokens = new HashSet<String>();
        while (separatorComa.hasMoreTokens())
        {
            final StringBuilder tokenToAdd = new StringBuilder(separatorComa.nextToken().trim());
            if (tokenToAdd.charAt(0) == '.')
            {
                tokenToAdd.delete(0, 1);// = tokenToAdd.substring(1);
            }
            if (tokenToAdd.charAt(tokenToAdd.length() - 1) != '.')
            {
                //tokenToAdd += ".";
                tokenToAdd.append(".");
            }
            if (!"".equals(tokenToAdd.toString().trim()) && !tokenToAdd.toString().equalsIgnoreCase(token))
            {
                changeCharacters(tokenToAdd);
                tokens.add(tokenToAdd.toString().trim());
            }
        }
        if (ABREVIACIONES.containsKey(token))
        {
            final Set<String> _tokens = ABREVIACIONES.get(token);
            _tokens.addAll(tokens);
        }
        else
        {
            ABREVIACIONES.put(token, tokens);
            final Set<String> tokensclean = new HashSet<String>();
            for (String tokenappend : tokens)
            {
                tokenappend = tokenappend.replace('.', '\0').trim();
                tokensclean.add(tokenappend);
            }
            final StringBuilder sbtoken = new StringBuilder(token);
            CompassInfo.normaliza(sbtoken);
            ABREVIACIONESNOM.put(sbtoken.toString(), tokensclean);
        }
    }

    private static void procesaVariantesAbreviaciones(final String addtoken, final Set<String> set)
    {
        final StringBuilder tokenToAdd = new StringBuilder(addtoken.trim());
        if (tokenToAdd.length() > 0)
        {
            tokenToAdd.insert(0, " ");
            tokenToAdd.append(" ");
            //tokenToAdd = tokenToAdd.replace(".", ". ");
            for (String token : ABREVIACIONES.keySet())
            {
                for (String tokenToReplace : ABREVIACIONES.get(token))
                {
                    CompassInfo.replace(tokenToAdd, " " + tokenToReplace + " ", " " + token + " ");
                }
            }
            changeCharacters(tokenToAdd);
            set.add(tokenToAdd.toString().trim());
        }
    }

    private static void agregaSinonimos(final Set<String> set)
    {
        boolean found = false;
        for (String text : set)
        {
            for (Set<String> conjunto_agregado : SINONIMOS)
            {
                if (conjunto_agregado.contains(text))
                {
                    conjunto_agregado.addAll(set);
                    found = true;
                    break;

                }
            }
            if (found)
            {
                break;
            }
        }
        if (!found)
        {
            SINONIMOS.add(set);
        }
    }

    public Compass getCompass()
    {
        if (compass == null || compass.isClosed())
        {
            reopen();
        }
        return compass;
    }

    @Override
    protected void finalize() throws Throwable
    {

        try
        {
            compass.close();
        }
        catch (CompassException ce)
        {
            LOG.error("Error al finalizar el objecto CompassInfo", ce);
        }
        super.finalize();
    }

    public synchronized static void removeIndice(final String index)
    {
        try
        {

            if (compass.getSearchEngineIndexManager() != null && compass.getSearchEngineIndexManager().subIndexExists(index))
            {
                compass.getSearchEngineIndexManager().releaseLock(index);
                try
                {
                    Thread.sleep(5000);
                }
                catch (Exception e)
                {
                    LOG.error("Error al intertar esperar borrado", e);
                }
                compass.getSearchEngineIndexManager().cleanIndex(index);

                try
                {
                    Thread.currentThread().sleep(5000);
                }
                catch (Exception e)
                {
                    LOG.error("Error al intertar esperar borrado", e);
                }
                //compass.getSearchEngineIndexManager().checkAndClearIfNotifiedAllToClearCache();
                compass.getSearchEngineIndexManager().notifyAllToClearCache();

                try
                {
                    Thread.sleep(5000);
                }
                catch (Exception e)
                {
                    LOG.error("Error al intertar esperar notificación", e);
                }
                compass.getSearchEngineIndexManager().refreshCache(index);
                try
                {
                    Thread.currentThread().sleep(5000);
                }
                catch (Exception e)
                {
                    LOG.error("Error al intertar esperar notificación", e);
                }
                //compass.getSearchEngineIndexManager().refreshCache(index);
            }

        }
        catch (Exception e)
        {
            LOG.warn("Error al borra el indice " + index, e);
        }


    }

    public static Set<Integer> getExperiencia(final Integer años)
    {
        final HashSet<Integer> experiencia = new HashSet<Integer>();
        if(años==6)
        {
            experiencia.addAll(EXPERIENCIA.keySet());            
        }
        
        for (Integer index : EXPERIENCIA.keySet())
        {
            collectExp(index, años, experiencia);
        }
        return experiencia;
    }

    public static void normalizeMonedas(final StringBuilder stringValue)
    {
        stringValue.insert(0, " ");
        stringValue.append(" ");
        final Pattern pattern = Pattern.compile("\\s+[\\$]?\\d{1,3},?\\d{1,3},?\\d+(\\.\\d+)?\\s+");
        final Matcher matcher = pattern.matcher(stringValue);
        //int ini = 0;
        while (matcher.find())
        {
            final int start = matcher.start();
            final int end = matcher.end();
            //ini = end;
            //String text = valueToNormaslize.substring(start, end).trim();
            String text = stringValue.substring(start, end).trim();
            if (text.charAt(0) == '$')
            {
                text = text.substring(1);
            }
            try
            {
                final Number number = CURRENCY.parse(text);
                stringValue.replace(start, end, " $" + CURRENCY2.format(number) + " ");

            }
            catch (Exception e)
            {
                LOG.debug("No se pudo normalizar: " + text, e);
            }
        }
        CompassInfo.trim(stringValue);

    }

    public static List<String> simpleSortedtokenizer(final String value)
    {
        final List<String> sortedtokenizer = new ArrayList<String>();
        if (value != null && !value.trim().equals(""))
        {

            final StringTokenizer separatorSpace = new StringTokenizer(value.trim(), " ");
            while (separatorSpace.hasMoreTokens())
            {
                final String token = separatorSpace.nextToken();
                if (!"".equals(token.trim()) && !STOPWORDS.contains(token.trim()))
                {
                    sortedtokenizer.add(token.trim());
                }
            }


        }
        return sortedtokenizer;
    }

    public static List<String> sortedtokenizer(final String value)
    {
        final List<String> sortedtokenizer = new ArrayList<String>();
        if (value != null && !value.trim().equals(""))
        {
            final StringTokenizer separatorSpace = new StringTokenizer(value.trim(), " ");
            while (separatorSpace.hasMoreTokens())
            {
                String token = separatorSpace.nextToken();
                boolean addDot = false;
                if (token.endsWith("."))
                {
                    token = token.substring(0, token.length() - 1);
                    addDot = true;
                }
                if (!CompassInfo.STOPWORDS.contains(token))
                {
                    sortedtokenizer.add(token);
                    if (addDot)
                    {
                        sortedtokenizer.add(".");
                    }
                }
            }
        }
        return sortedtokenizer;
    }

    public static String[] tokenizer(final String value)
    {
        final HashSet<String> tokenizer = new HashSet<String>();
        if (value != null)
        {
            final StringTokenizer separatorComa = new StringTokenizer(value, ",");
            while (separatorComa.hasMoreTokens())
            {
                final String _value = separatorComa.nextToken();
                if (!_value.trim().equals(""))
                {
                    final StringTokenizer separatorSpace = new StringTokenizer(_value.trim(), " ");
                    while (separatorSpace.hasMoreTokens())
                    {
                        final String token = separatorSpace.nextToken();
                        if (!CompassInfo.isStopWords(token))
                        {
                            tokenizer.add(token);
                        }
                    }
                }
            }
        }
        return tokenizer.toArray(new String[tokenizer.size()]);
    }

    public static void replace(final StringBuilder text, final String match, final String strreplace)
    {
        int pos = text.indexOf(match);
        while (pos != -1)
        {
            text.replace(pos, pos + match.length(), strreplace);
            pos = text.indexOf(match, pos + 1);
        }
    }

    public static String replace(final String text, final String match, final String strreplace)
    {
        final StringBuilder sbtext = new StringBuilder(text);
        replace(sbtext, match, strreplace);
        return sbtext.toString();
    }

    public static void replace(final StringBuilder text, final char match, final char replace)
    {
        for (int i = 0; i < text.length(); i++)
        {
            final char _char = text.charAt(i);
            if (_char == match)
            {
                text.setCharAt(i, replace);
            }
        }
    }

    /*public static String replace(final String text, final String match, final String strreplace)
    {
    String replace = strreplace;
    String str = text;
    str = " " + str + " ";
    if (match == null || match.length() == 0)
    {
    return str;
    }
    if (replace == null)
    {
    replace = "";
    }
    if (match.equals(replace))
    {
    return str;
    }
    int pos = str.indexOf(match);
    while (pos != -1)
    {
    final String ini = str.substring(0, pos);
    final String end = str.substring(pos + match.length());
    str = ini + replace + end;
    //pos = ini.length() + replace.length() - 1;
    pos = str.indexOf(match, pos);
    }
    return str;
    }*/
    public static String normaliza(final String txt)
    {
        final StringBuilder tmp = new StringBuilder(txt);
        normaliza(tmp);
        return tmp.toString();
    }

    public static void normaliza(final StringBuilder txt)
    {

        CompassInfo.replace(txt, ',', ' ');
        CompassInfo.replace(txt, '/', ' ');
        CompassInfo.replace(txt, '-', ' ');
        CompassInfo.replace(txt, ':', ' ');
        final List<String> words = sortedtokenizer(txt.toString());
        final StringBuilder content = new StringBuilder();
        for (int i = 0; i < words.size(); i++)
        {
            String word = words.get(i);
            if (STOPWORDS.contains(word) || (!word.isEmpty() && word.charAt(0) == '$'))
            {
                content.append(word);
                content.append(" ");
            }
            else
            {
                if (RAICES.containsKey(word))
                {
                    word = RAICES.get(word);
                }
                else
                {
                    final SingleTokenTokenStream tokens = new SingleTokenTokenStream(new org.apache.lucene.analysis.Token(word, 0, word.length()));
                    final SnowballFilter filter = new SnowballFilter(tokens, SPANISH);
                    try
                    {
                        word = filter.next().term();
                    }
                    catch (Exception e)
                    {
                        LOG.error("No se puede normalizar la palabra " + word, e);
                    }
                    RAICES.put(words.get(i), word);
                }
                final String prinicpal = getPrincipalSinonimo(word);
                content.append(prinicpal);
                content.append(" ");
            }
        }
        txt.replace(0, txt.length(), content.toString());
        trim(txt);
        // return content.toString().trim();
    }

    public static String cleanTerm(final String term)
    {
        final StringBuilder sbterm = new StringBuilder(term);
        cleanTerm(sbterm);
        return sbterm.toString();
    }

    public static void cleanTerm(final StringBuilder term)
    {
        toLowerCase(term);
        final String[] limits =
        {
            " y ", ",", ":", " o ", "/", "-"
        };
        int pos = term.indexOf("(");
        while (pos != -1)
        {
            final int pos2 = term.indexOf(")", pos + 1);
            if (pos2 == -1)
            {
                //text = text.substring(0, pos).trim();
                term.setLength(pos);
            }
            else
            {
                term.delete(pos, pos2 + 1);
//                final String temp = text.substring(0, pos).trim();
//                final String temp2 = text.substring(pos2 + 1);
//                text = temp + " " + temp2;
//                text = text.trim();
            }
            pos = term.indexOf("(", pos);
        }


        final List<Integer> positions = new ArrayList<Integer>();
        for (String limit : limits)
        {
            pos = term.indexOf(limit);
            if (pos != -1)
            {
                positions.add(pos);
            }
        }
        if (!positions.isEmpty())
        {
            Collections.sort(positions);
            pos = positions.get(0);
            //text = text.substring(0, pos);
            term.setLength(pos);

        }
    }

    public static String extractParentesis(final String value)
    {
        final StringBuilder sbvalue = new StringBuilder(value);
        extractParentesis(sbvalue);
        return sbvalue.toString();
    }

    public static void extractParentesis(final StringBuilder value)
    {

        int pos = value.indexOf("(");
        while (pos != -1)
        {
            final int pos2 = value.indexOf(")", pos + 1);
            if (pos2 == -1)
            {
                //value = value.substring(0, pos).trim();
                value.setLength(pos);
            }
            else
            {
//                final String temp = value.substring(0, pos).trim();
//                final String temp2 = value.substring(pos2 + 1);
//                value = temp + " " + temp2;
//                value = value.trim();
                value.delete(pos, pos2 + 1);

            }
            pos = value.indexOf("(", pos);
        }

    }

    public static Set<Integer> getExperiencia(final Integer index, final boolean vacante)
    {
        final HashSet<Integer> experiencia = new HashSet<Integer>();
        experiencia.add(1);
        experiencia.add(8);
        if (vacante)
        {
            switch (index)
            {
                case 2:

                    experiencia.add(2);
                    experiencia.add(3);

                    break;
                case 3:

                    experiencia.add(2);
                    experiencia.add(3);

                    experiencia.add(4);
                    break;
                case 4:

                    experiencia.add(2);
                    experiencia.add(3);

                    experiencia.add(4);
                    experiencia.add(5);
                    break;
                case 5:

                    experiencia.add(2);
                    experiencia.add(3);

                    experiencia.add(4);
                    experiencia.add(5);
                    experiencia.add(6);
                    break;

                case 6:

                    experiencia.add(2);
                    experiencia.add(3);

                    experiencia.add(4);
                    experiencia.add(5);
                    experiencia.add(6);
                    experiencia.add(7);
                    break;
                case 7:

                    experiencia.add(2);
                    experiencia.add(3);

                    experiencia.add(4);
                    experiencia.add(5);
                    experiencia.add(6);
                    experiencia.add(7);
                    break;
                default:
                    experiencia.add(1);
                    experiencia.add(8);
            }
        }
        return experiencia;
    }

    public static List<CarrerasOcupacionSimilares> getCarrerasSimilares()
    {
        return CARRERASSIMILARES;
    }

    public static List<CarrerasOcupacionSimilares> getOcupacionSimilares()
    {
        return OCUPASIMILARES;
    }

    public static void trim(final StringBuilder text)
    {
        int pos = text.lastIndexOf(" ");
        while (pos != -1 && (pos == text.length() - 1 || pos == 0))
        {
            text.deleteCharAt(pos);
            pos = text.lastIndexOf(" ");
        }
    }

    public static void changeBadcharacters(final StringBuilder text, final boolean special)
    {
        if (special)
        {
            replace(text, '$', ' ');
            replace(text, '%', ' ');
        }
        for (int i = 0; i < text.length(); i++)
        {
            final char _char = text.charAt(i);
            if (!(Character.isLetterOrDigit(_char) || Character.isWhitespace(_char) || _char == '$' || _char == '%'))
            {
                text.replace(i, i + 1, " ");
            }
        }
        /*for (Character ch : text.toCharArray())
        {
        if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch))
        {
        content.append(ch);
        }
        else
        {
        if (!special && (ch == '$' || ch == '%'))
        {
        content.append(ch);
        }
        else
        {
        content.append(' ');
        }
        
        }
        }*/

    }

    public static void changeBadcharactersForSearchVacantes(final StringBuilder text, final boolean special)
    {

        for (int i = 0; i < text.length(); i++)
        {
            final char _char = text.charAt(i);
            final boolean cond1 = !(_char == '.' || Character.isLetterOrDigit(_char) || Character.isWhitespace(_char));
            final boolean cond2 = !(!special && (_char == '$' || _char == '%'));
            if (cond1 && cond2)
            {
                text.setCharAt(i, ' ');
            }

        }

    }

    public static PhraseParser getPhraseParser(final StringBuilder search)
    {
        final List<String> phrases = new ArrayList<String>();
        int pos = search.indexOf("\"");
        while (pos != -1)
        {
            final int pos2 = search.indexOf("\"", pos + 1);
            if (pos2 != -1)
            {
                final String phrase = search.substring(pos, pos2 + 1);
                //search = search.substring(0, pos) + " " + search.substring(pos2 + 1);
                search.delete(pos, pos2);
                phrases.add(phrase);
            }
            pos = search.indexOf("\"", pos + 1);
        }
        final PhraseParser phraseParser = new PhraseParser();
        phraseParser.setPrases(phrases);
        return phraseParser;
    }

    public static String replaceAbreviaciones(final String text)
    {
        final StringBuilder sbtext = new StringBuilder(text);
        replaceAbreviaciones(sbtext);
        return sbtext.toString().trim();
    }

    public static void replaceAbreviaciones(final StringBuilder text)
    {
        text.insert(0, " ");
        text.append(" ");
        for (String token : ABREVIACIONES.keySet())
        {
            for (String tokenToReplace : ABREVIACIONES.get(token))
            {
                CompassInfo.replace(text, " " + tokenToReplace.toLowerCase(LOCALE) + " ", " " + token + " ");
            }
        }
        CompassInfo.trim(text);
    }

    public static String[] getStopWords()
    {
        return STOPWORDS.toArray(new String[STOPWORDS.size()]);
    }

    public static void optimize()
    {
        try
        {
            LOG.info("compass.getSearchEngineOptimizer().isRunning(): " + compass.getSearchEngineOptimizer().isRunning());
            if (!compass.getSearchEngineOptimizer().isRunning())
            {
                LOG.info("Ejecutando rutina de optimización");
                compass.getSearchEngineOptimizer().optimize();
                compass.getSearchEngineOptimizer().start();
                LOG.info("Fin de rutina de optimización");
            }
        }
        catch (Exception e)
        {
            LOG.error("Error tratando de optimizar", e);

        }
    }

    public static String analizerTokenizer(final String word_token)
    {
        final StringBuilder tokenizer = new StringBuilder();
        final StandardAnalyzer analizer = new StandardAnalyzer(CompassInfo.STOPWORDS);
        final TokenStream tokenStream = analizer.tokenStream("", new StringReader(word_token));
        try
        {
            org.apache.lucene.analysis.Token token = tokenStream.next();
            while (token != null)
            {
                if (token.term() != null)
                {
                    String word = token.term();
                    final SingleTokenTokenStream tokens = new SingleTokenTokenStream(new org.apache.lucene.analysis.Token(word, 0, word.length()));
                    final SnowballFilter filter = new SnowballFilter(tokens, CompassInfo.SPANISH);
                    try
                    {
                        word = filter.next().term();
                        tokenizer.append(word);
                        tokenizer.append(" ");
                    }
                    catch (Exception e)
                    {
                        LOG.error("No se puede normalizar la palabra " + word, e);
                    }
                }
                token = tokenStream.next();
            }
        }
        catch (Exception e)
        {
            LOG.debug("Error", e);
        }
        return tokenizer.toString();
    }

    public static String steam(final String text)
    {
        String word = text;
        word = word.replace('á', 'a');
        word = word.replace('é', 'e');
        word = word.replace('í', 'i');
        word = word.replace('ó', 'o');
        word = word.replace('ú', 'u');
        word = word.replace('à', 'a');
        word = word.replace('è', 'e');
        word = word.replace('ì', 'i');
        word = word.replace('ò', 'o');
        word = word.replace('ù', 'u');
        word = word.replace('ü', 'u');
        final SingleTokenTokenStream tokens = new SingleTokenTokenStream(new org.apache.lucene.analysis.Token(word, 0, word.length()));
        final SnowballFilter filter = new SnowballFilter(tokens, SPANISH);
        try
        {
            word = filter.next().term();
        }
        catch (Exception e)
        {
            LOG.error("No se puede normalizar la palabra " + word, e);
        }
        return word;
    }

    public static IndexInfo indexOf(final String text)
    {
        IndexInfo info = new IndexInfo();
        final String[] tokens =
        {
            " y ", " o ", "/",","
        };
        for (String token : tokens)
        {
            final int pos = text.indexOf(token);
            if (pos != -1)
            {
                info.pos = pos;
                info.len = token.length();
                info = new IndexInfo(pos, token.length());
                break;
            }
        }
        return info;
    }
}
