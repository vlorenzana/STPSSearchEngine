/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import gob.mx.stps.empleo.search.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;

/**
 *
 * @author victor.lorenzana
 */
public class VacanteTest
{

    private static final String CONSULTA = "consulta: ";
    private static final String ERROR = " ERROR: ";
    private static final String FALLO_ID = "Fallo: id: ";
    private static final String IDCATALOGO = "id_catalogo_opcion";
    private static final String MILLISECONDS = " ms";
    private static final String OPCION = "opcion";
    private static final String PALABRAS = " palabras: ";
    private static final String SEARCH = " search: ";
    private static final Log LOG = LogFactory.getLog(VacanteTest.class);
    private static final String VACANTE = " vacante: ";
    private static VacanteSearch instance = null;
    private static final Map<Integer, String> GRADOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> CARRERAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> IDIOMAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> DOMINIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> EXPERIENCIAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> HORARIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> OCUPACIONES = new HashMap<Integer, String>();
    private static final Map<Integer, String> ESTATUS = new HashMap<Integer, String>();

    @BeforeClass
    public static void setUpClass()
    {

        System.setProperty("showResultInfo", "false");
        final long tini = System.currentTimeMillis();
        final Catalogo catalogo = new Catalogo();

        Connection con = null;
        try
        {
            //Class.forName("oracle.jdbc.OracleDriver");
            Class.forName("org.gjt.mm.mysql.Driver");
            LOG.info("llenado catalogos...");

            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/empleo", "root", "root");
            llenaHorarios(con);
            llenaCarreras(con);
            llenaOcupaciones(con);

            llenaIdiomas(con);
            llenaDominios(con);
            llenaStatus(con);

            llenaGrados(con);
            llenaExperiencias(con);

        } catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        } finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                } catch (Exception e2)
                {
                    LOG.error(e2.getMessage(), e2);
                }
            }
        }

        final long tfin = System.currentTimeMillis();
        final long dif = (tfin - tini);
        LOG.info("Fin de llenado catalogos... tiempo: " + dif + MILLISECONDS);

        catalogo.carrera_especialidad_catalogo.putAll(CARRERAS);
        catalogo.dominios.putAll(DOMINIOS);
        catalogo.experiencia_catalogo.putAll(EXPERIENCIAS);
        catalogo.grado_estudios.putAll(GRADOS);
        catalogo.horario_catalogo.putAll(HORARIOS);
        catalogo.idiomas_catalogo.putAll(IDIOMAS);
        catalogo.ocupacion_catalogo.putAll(OCUPACIONES);
        catalogo.status_estudio_catalogo.putAll(ESTATUS);

        instance = new VacanteSearch(catalogo);
        //instance.indexaCarreras();
        //instance.indexaOcupaciones();

        CompassInfo.optimize();

    }

    private static boolean checkAbreviaciones(final String nword, final String palabras)
    {
        boolean found = false;
        for (String wordAbrev : getWordsRelatedAbrev(nword))
        {
            if (palabras.indexOf(wordAbrev) != -1)
            {
                found = true;
                break;
            }
        }
        return found;
    }

    private static boolean checkAlternate(final String nword, final String palabras, final String word)
    {
        boolean found = checkAbreviaciones(nword, palabras);
        if (!found)
        {
            found = checkSinonimosWord(word, palabras);
        }
        if (!found)
        {
            found = revisaSinonimosRef(word, palabras);
        }

        return found;
    }

    private static boolean revisaSinonimosRef(final String word, final String palabras)
    {
        boolean found = false;
        final String[] ref = CompassInfo.getSinonimosReferenciales(word);
        if (ref != null)
        {
            for (String sin_ref : ref)
            {
                if (palabras.indexOf(sin_ref) != -1)
                {
                    found = true;
                    break;
                }
                if (!found)
                {
                    found = revisaSinonimoRef(sin_ref, palabras);
                }
            }
        }
        return found;
    }

    private static boolean checkCarrera(final Integer iCarrera, final String palabras)
    {
        final String carrera = CompassInfo.CARRERASCAT.get(iCarrera);
        if (hasAllWords(palabras, carrera))
        {
            return true;
        }
        return false;
    }

    private static boolean checkCarreras(final Consulta consulta, final String palabras)
    {
        if (consulta != null)
        {
            final Set<Integer> totales = new HashSet<Integer>();
            totales.addAll(consulta.carreras_detectadas);
            for (Integer iCarrera : consulta.carreras_detectadas)
            {
                final Integer[] similares = CompassInfo.getCarrerasSimilaresCatalogo(iCarrera);
                totales.addAll(Arrays.asList(similares));

            }
            if (checkTodasCarreras(totales, palabras))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean checkSinonimosWord(final String word, final String palabras)
    {
        boolean found = false;
        final String normword = CompassInfo.steam(word);
        for (String sin : CompassInfo.getSinonimos(normword))
        {
            if (palabras.indexOf(sin) != -1)
            {
                found = true;
                break;
            }
        }
        return found;
    }

    private static boolean checkTodasCarreras(final Set<Integer> totales, final String palabras)
    {
        if (totales.size() > 1)
        {
            for (Integer iCarrera : totales)
            {
                if (checkCarrera(iCarrera, palabras))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkOcupacion(final Integer iOcupacion, final String palabras)
    {
        final String ocupacion = CompassInfo.OCUPACIONESCAT.get(iOcupacion);
        if (hasAllWords(palabras, ocupacion))
        {
            return true;
        }
        return false;
    }

    private static boolean checkOcupaciones(final Consulta consulta, final String palabras)
    {
        if (consulta != null)
        {
            final Set<Integer> totales = new HashSet<Integer>();
            totales.addAll(consulta.ocupaciones_detectadas);
            for (Integer iOcupacion : consulta.ocupaciones_detectadas)
            {
                final Integer[] similares = CompassInfo.getOcupacionesSimilaresCatalogo(iOcupacion);
                totales.addAll(Arrays.asList(similares));

            }
            if (checkTodasOcupaciones(totales, palabras))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean checkTodasOcupaciones(final Set<Integer> totales, final String palabras)
    {
        if (totales.size() > 1)
        {
            for (Integer iOcupacion : totales)
            {
                if (checkOcupacion(iOcupacion, palabras))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkSinonimos(final String search, final String palabras)
    {
        final StringBuilder content = new StringBuilder(search);
        CompassInfo.changeCharacters(content);
        for (String word : CompassInfo.sortedtokenizer(content.toString()))
        {
            final StringBuilder sbword = new StringBuilder(word);
            CompassInfo.normaliza(sbword);
            for (String sin : CompassInfo.getSinonimos(sbword.toString()))
            {
                if (palabras.indexOf(sin) != -1)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkWord(final String word, final String palabras)
    {
        boolean found = false;
        String nword = CompassInfo.steam(word);
        if (palabras.indexOf(nword) == -1)
        {
            final StringBuilder sbword = new StringBuilder(nword);
            CompassInfo.normaliza(sbword);
            nword = sbword.toString();
            if (palabras.indexOf(nword) != -1)
            {
                found = true;
            }
        } else
        {
            found = true;
        }
        if (!found)
        {
            found = checkAlternate(nword, palabras, word);
        }
        if (!found)
        {
            return true;
        }
        return false;
    }

    private static boolean checkWordsOcupaciones(final Consulta consulta, final String palabras)
    {
        // puede ser varias ocupaciones
        if (consulta != null && consulta.ocupaciones_words.size() > 1)
        {
            for (String ocupacion : consulta.ocupaciones_words)
            {
                if (hasAllWords(palabras, ocupacion))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static void llenaExperiencias(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=14");
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=14");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            EXPERIENCIAS.put(idcatalogo, desc);

        }
        query.close();
        stmt.close();
    }

    private static void llenaGrados(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=8");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=8");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            GRADOS.put(idcatalogo, desc);

        }
        query.close();

        stmt.close();
    }

    private static void llenaStatus(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=10");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=10");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            ESTATUS.put(idcatalogo, desc);

        }
        query.close();
        stmt.close();
    }

    private static void llenaDominios(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=13");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=13");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            DOMINIOS.put(idcatalogo, desc);

        }
        query.close();
        stmt.close();
    }

    private static void llenaIdiomas(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=11");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=11");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            IDIOMAS.put(idcatalogo, desc);

        }
        query.close();

        stmt.close();
    }

    private static void llenaOcupaciones(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=21");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=21");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            OCUPACIONES.put(idcatalogo, desc);

        }
        query.close();
        stmt.close();
    }

    private static void llenaCarreras(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=40 or id_catalogo=42 or id_catalogo=43 or id_catalogo=44");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=40 or id_catalogo=42 or id_catalogo=43 or id_catalogo=44");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            CARRERAS.put(idcatalogo, desc);

        }
        query.close();

        stmt.close();
    }

    private static void llenaHorarios(final Connection con) throws SQLException
    {
        //con = DriverManager.getConnection("jdbc:oracle:thin:@200.38.177.133:1531:EMPLEOQA", "desa1", "desa1");
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        //ResultSet rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=15");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=15");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            HORARIOS.put(idcatalogo, desc);

        }
        query.close();
        stmt.close();
    }

    private static boolean revisaSinonimoRef(final String sin_ref, final String palabras)
    {
        boolean found = false;
        for (String sin : CompassInfo.getSinonimos(sin_ref))
        {
            if (palabras.indexOf(sin) != -1)
            {
                found = true;
                break;
            }
        }
        return found;
    }

    @Test
    public void testIngeniero()
    {

        final String search = "ingeniero";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                boolean found = false;
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                Assert.assertTrue(SEARCH + search + VACANTE + vacante.id + PALABRAS + vacante, found);
            }

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }

    }

    @Test
    public void testMedico()
    {

        final String search = "medico";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                /*LOG.info("------------  vacante: " + info.id + " ------------------");
                 String ocupacion = CompassInfo.OCUPACIONES.get(vacante.ocupacion);
                 for (Integer carrera : vacante.carreras)
                 {
                
                 String text = CompassInfo.CARRERAS.get(carrera);
                 LOG.info("carrera: " + text);
                 }
                 if (ocupacion != null)
                 {
                 LOG.info("ocupacion: " + ocupacion);
                 }*/
                boolean found = false;
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                final String palabras = getPalabras(vacante);
                Assert.assertTrue(SEARCH + search + VACANTE + vacante + PALABRAS + palabras, found);
            }

        } catch (Exception e)
        {
            LOG.error(e);
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }

    }

    @Test
    public void testOperador()
    {

        final String search = "operador";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                /*LOG.info("-----------------------");
                 LOG.info("ocupacion: " + CompassInfo.OCUPACIONES.get(vacante.ocupacion));
                 LOG.info("titulo: " + vacante.titulo);
                 LOG.info("-----------------------");*/
                boolean found = false;
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                final String palabras = getPalabras(vacante);
                Assert.assertTrue(SEARCH + search + VACANTE + vacante + PALABRAS + palabras, found);
            }

        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }

    }

    @Test
    public void testChofer()
    {

        final String search = "chofer";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                /*LOG.info("-----------------------");
                 LOG.info("ocupacion: " + CompassInfo.OCUPACIONES.get(vacante.ocupacion));
                 LOG.info("titulo: " + vacante.titulo);
                 LOG.info("-----------------------");*/
                boolean found = false;
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                final String palabras = getPalabras(vacante);
                Assert.assertTrue(SEARCH + search + VACANTE + vacante + PALABRAS + palabras, found);
            }

        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }

    }

    @Test
    public void testAgricultura()
    {
        final String search = "Agricultura";
        Result result = null;
        try
        {
            result = instance.search(search);
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    @Test
    //@Ignore
    public void testlicenciada()
    {
        final String search = "licenciada";
        Result result = null;
        try
        {
            result = instance.search(search);
            //Assert.assertNotSame(result.getCount(), 0);
            Assert.assertTrue(SEARCH + search, result.getConsulta().text.equals("licenci"));
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    @Test
    //@Ignore
    public void testIngeniera()
    {
        final String search = "ingeniera";
        Result result = null;
        try
        {
            result = instance.search(search);
            while (result.hasNextElement())
            {
                final long idvacante = result.nextElement().id;
                final Vacante vacante = instance.get(idvacante);
                boolean found = false;
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                Assert.assertTrue(SEARCH + search, found);
            }

        } catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    @Test
    //@Ignore
    public void testSinonimos()
    {
        String search = "Canto";
        Result result = null;
        Result result2 = null;
        Result result3 = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
            search = "Vocalización";
            result2 = instance.search(search);
            Assert.assertNotSame(SEARCH + search, result2.getCount(), 0);
            Assert.assertEquals(SEARCH + search, result2.getCount(), result.getCount());

            search = "Cantante";
            result3 = instance.search(search);
            Assert.assertEquals(SEARCH + search, result3.getCount(), result.getCount());

            Assert.assertTrue(SEARCH + search, result.getConsulta().text.equals(result2.getConsulta().text));
            Assert.assertTrue(SEARCH + search, result2.getConsulta().text.equals(result3.getConsulta().text));

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();
            result2.close();
            result3.close();

        }
    }

    @Test
    //@Ignore
    public void testGeneroIngeniera()
    {
        final String search = "Ingeniera";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
            while (result.hasNextElement())
            {
                final long idvacante = result.nextElement().id;
                final Vacante vacante = instance.get(idvacante);
                boolean found = false;
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                Assert.assertTrue(SEARCH + search + VACANTE + vacante.id + PALABRAS + getPalabras(vacante), found);

            }
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    private static String getPalabras(final Vacante vacante)
    {
        final StringBuilder content = new StringBuilder();
        if (vacante.getTitulo() != null)
        {
            content.append(vacante.getTitulo());
            content.append(" ");
        }
        for (String c : vacante.conocimientos)
        {
            content.append(c);
            content.append(" ");
        }
        for (Integer i_carrera : vacante.carreras)
        {
            final String carrera = CompassInfo.CARRERAS.get(i_carrera);
            content.append(carrera);
            content.append(" ");
        }
        final String ocupacion = CompassInfo.OCUPACIONES.get(vacante.ocupacion);
        if (ocupacion != null)
        {
            content.append(ocupacion);
            content.append(" ");
        }
        return content.toString();
    }

    public static boolean isValid(final String palabras, final String search, final boolean validaRef, final Consulta consulta)
    {
        if (hasAllWords(palabras, search))
        {
            return true;
        }
        if (checkSinonimos(search, palabras))
        {
            return true;
        }
        if (validaRef && isValidRef(palabras, search, consulta))
        {
            return true;
        }
        final boolean isValidVariantes = isValidVariantes(palabras, search);
        if (isValidVariantes)
        {
            return true;
        }
        if (checkWordsOcupaciones(consulta, palabras))
        {
            return true;
        }

        if (checkOcupaciones(consulta, palabras))
        {
            return true;
        }

        if (checkCarreras(consulta, palabras))
        {
            return true;
        }

        return false;
    }

    public static boolean isValid(final Vacante vacante, final String txtSearch, final Consulta consulta)
    {
        final StringBuilder search = new StringBuilder(txtSearch);
        final StringBuilder palabras = new StringBuilder(getPalabras(vacante));
        CompassInfo.replaceAbreviaciones(palabras);
        CompassInfo.changeCharacters(palabras);
        CompassInfo.changeBadcharacters(palabras, false);

        CompassInfo.replaceAbreviaciones(search);
        CompassInfo.changeBadcharacters(search, false);
        CompassInfo.changeCharacters(search);

        return isValid(palabras.toString(), search.toString(), true, consulta);
    }

    public static boolean isValidRef(final String palabras, final String search, final Consulta consulta)
    {

        final String[] refs = CompassInfo.getSinonimosReferenciales(CompassInfo.steam(search));
        if (refs != null)
        {
            for (String sinref : refs)
            {
                if (isValid(palabras, sinref, false, null))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isValidVariantes(final String palabras, final String search)
    {
        final StringBuilder searchNorm = new StringBuilder(search);
        CompassInfo.normaliza(searchNorm);
        for (String variante : CompassInfo.getVariantes(searchNorm.toString()))
        {
            if (hasAllWords(palabras, variante))
            {
                return true;
            }
            final StringBuilder sbvariante = new StringBuilder(variante);
            CompassInfo.normaliza(sbvariante);
            for (String sinonimo : CompassInfo.getSinonimos(sbvariante.toString()))
            {
                if (hasAllWords(palabras, sinonimo))
                {
                    return true;
                }
            }

        }
        return false;
    }

    public static Set<String> getWordsRelatedAbrev(final String word)
    {
        final Set<String> getWordsRelatedAbrev = new HashSet<String>();
        for (String word_key : CompassInfo.ABREVIACIONESNOM.keySet())
        {
            final Set<String> _abrevs = CompassInfo.ABREVIACIONESNOM.get(word_key);
            for (String _abrev : _abrevs)
            {
                final Set<String> words_abrev = getWordsFromAbrev(_abrev);
                if (words_abrev.contains(word))
                {
                    getWordsRelatedAbrev.addAll(words_abrev);
                }

            }
        }
        return getWordsRelatedAbrev;
    }

    public static Set<String> getWordsFromAbrev(final String _abrev)
    {
        final Set<String> getWordsFromAbrev = new HashSet<String>();
        for (String word_key : CompassInfo.ABREVIACIONESNOM.keySet())
        {
            if (CompassInfo.ABREVIACIONESNOM.get(word_key).contains(_abrev))
            {
                getWordsFromAbrev.add(word_key);
            }
        }
        return getWordsFromAbrev;
    }

    public static boolean hasAllWords(final String palabras, final String variante)
    {

        for (String word : CompassInfo.sortedtokenizer(variante))
        {
            if (checkWord(word, palabras))
            {
                return false;
            }

        }
        return true;
    }

    @Test
    @Ignore
    public void testVariantes()
    {
        long tini=System.currentTimeMillis();
        for (Set<String> set : CompassInfo.getVariantes())
        {
            final List<Integer> resultados = new ArrayList<Integer>();
            final List<String> consultas = new ArrayList<String>();
            for (String search : set)
            {
                Result result = null;
                try
                {
                    result = instance.search(search);
                    resultados.add(result.getCount());
                    consultas.add(result.getQuery());

                } catch (Exception e)
                {
                    LOG.error(e.getMessage(), e);
                    Assert.fail(e.getMessage());
                } finally
                {

                    result.close();

                }
            }

            int[] expecteds = new int[resultados.size()];
            for (int i = 0; i < resultados.size(); i++)
            {
                expecteds[i] = resultados.get(0);
            }

            final int[] actuals = new int[resultados.size()];
            for (int i = 0; i < resultados.size(); i++)
            {
                actuals[i] = resultados.get(i);
            }
            final StringBuilder content = new StringBuilder();
            for (String text : set)
            {
                content.append(text);
                content.append(",");
            }
            Assert.assertArrayEquals(content.toString() + " " + consultas.size(), expecteds, actuals);
        }
        long dif=System.currentTimeMillis()-tini;
        LOG.info("Tiempo: "+dif+" ms");
        
    }

    @Test
    public void testAbreviacion2()
    {
        final String search = "admon.";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().text, "administracion");

            while (result.hasNextElement())
            {

                final long idvacante = result.nextElement().id;
                final Vacante vacante = instance.get(idvacante);
                Assert.assertTrue("search" + search + VACANTE + vacante.id + " palabras " + vacante, isValid(vacante, search, result.getConsulta()));

            }
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    @Test
    public void testComas()
    {
        final String search = "administración,admon.,admon.";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertTrue("Fallo comas " + search, result.getCount() != 0);
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    @Test
    public void testAdministrador()
    {
        String search = "Administrador";
        Result result = null;
        int admon = 0, gerente = 0, director = 0;
        try
        {
            result = instance.search(search);
            admon = result.getCount();

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }

        search = "gerente";
        Result result2 = null;
        try
        {
            result2 = instance.search(search);
            gerente = result2.getCount();
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result2.close();

        }

        search = "director";
        Result result3 = null;
        try
        {
            result3 = instance.search(search);
            director = result3.getCount();
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result3.close();

        }
        Assert.assertTrue("admon>=(gerente+director): ", admon >= (gerente + director));
        Assert.assertTrue("gerente!=director: ", gerente != director);

    }

    @Test
    public void testCapturista()
    {
        final String search = "capturista";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().text, "capturist");
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    @Test
    public void testMecanico()
    {
        long tini=System.currentTimeMillis();
        final String search = "mecanico";
        final String search2 = "mec.";
        Result result = null;
        Result result2 = null;
        try
        {
            result = instance.search(search);
            result2 = instance.search(search2);
            Assert.assertEquals(SEARCH + search + " result.getCount(): " + result.getCount() + " result2.getCount():" + result2.getCount(), result.getCount(), result2.getCount());
            Assert.assertTrue(SEARCH + search, result.getConsulta().text.equals("mecanic"));
            Assert.assertTrue(SEARCH + search, result2.getConsulta().text.equals("mecanic"));
            Assert.assertTrue(SEARCH + search, result.getConsulta().hasCriterio(Campos.CARRERA_O_ESPECIALIDAD.toString()));
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            if(result2!=null)
                result2.close();
            if(result!=null)
            result.close();

        }
        long dif=System.currentTimeMillis()-tini;
        LOG.info("Tiempo: "+dif+" ms");        
    }

    @Test
    public void testAbreviacion()
    {
        final String search = "ing.";
        final String search2 = "ingeniero";
        final String search3 = "ingeniera";
        final String search4 = "ingenierias";
        Result result = null;
        Result result2 = null;
        Result result3 = null;
        Result result4 = null;
        try
        {
            result = instance.search(search);
            result2 = instance.search(search2);
            result3 = instance.search(search3);
            result4 = instance.search(search4);
            Assert.assertTrue(SEARCH + search, result.getConsulta().text.equals("ingenier"));
            Assert.assertTrue(SEARCH + search, result.getConsulta().text.equals(result2.getConsulta().text));
            Assert.assertTrue(SEARCH + search, result.getCount() == result2.getCount());
            Assert.assertTrue(SEARCH + search, result2.getCount() == result3.getCount());
            Assert.assertTrue(SEARCH + search, result3.getCount() == result4.getCount());
            while (result.hasNextElement())
            {

                final long idvacante = result.nextElement().id;
                final Vacante vacante = instance.get(idvacante);
                boolean found = false;
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                Assert.assertTrue(SEARCH + search + VACANTE + vacante.id + PALABRAS + vacante, found);
            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {
            result2.close();
            result3.close();
            result4.close();

            result.close();

        }
    }

    @Test
    public void testGerenteDeSistemas()
    {

        final String search = "sistemas";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
            while (result.hasNextElement())
            {
                final long idvacante = result.nextElement().id;
                final Vacante vacante = instance.get(idvacante);
                boolean found = false;
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                if (!found)
                {
                    Assert.fail("Vacante: " + vacante.id + " no encontrada");

                }

            }

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    @Test
    public void testPrase()
    {

        String search = "\"sistemas\"";
        Result result = null;
        try
        {
            result = instance.search(search);
            search = checkResults(result, search);
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {

            result.close();

        }

        final String search2 = "sistemas";
        Result result2 = null;
        try
        {
            result2 = instance.search(search2);
            search = checkResults(result2, search);
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {

            result.close();

        }

    }

    private String checkResults(final Result result, String search)
    {
        //int i = -1;
        while (result.hasNextElement())
        {

            final long idvacante = result.nextElement().id;
            final Vacante vacante = instance.get(idvacante);
            boolean found = false;
            if (!found)
            {
                search = search.replace('"', ' ');
                search = search.trim();
                if (search.indexOf(search) != -1)
                {
                    found = true;
                }
            }
            if (!found)
            {
                LOG.info(vacante);
            }
            Assert.assertTrue(search + VACANTE + vacante.id + PALABRAS + vacante, found);
        }
        return search;
    }

    @Test
    public void testIndustrialIngeniero()
    {

        final String search = "\"industrial ingeniero\"";

        Result result = null;
        try
        {
            result = instance.search(search);
            while (result.hasNextElement())
            {
                final long idvacante = result.nextElement().id;
                final Vacante vacante = instance.get(idvacante);
                boolean found = false;

                if (!found && isValid(vacante, search, result.getConsulta()))
                {

                    found = true;

                }
                if (!found)
                {
                    LOG.info(vacante);
                }
                Assert.assertTrue(search + VACANTE + vacante.id + PALABRAS + vacante, found);
            }

        } catch (Exception e)
        {
            LOG.error(e, e);
            Assert.fail(e.getMessage());
        } finally
        {
            if (result != null)
            {
                result.close();
            }

        }
    }

    @Test
    public void testOrdenamientoEmpresa()
    {
        long tini=System.currentTimeMillis();
        final String search = "\"profesor\"";

        Result result = null;
        try
        {
            result = instance.search(search, CampoOrdenamiento.FECHA);
            while (result.hasNextElement())
            {
                final long idvacante = result.nextElement().id;
                final Vacante vacante = instance.get(idvacante);
                boolean found = false;
                //System.out.println("fecha :" + vacante.getFecha());
                if (!found && isValid(vacante, search, result.getConsulta()))
                {

                    found = true;

                }
                if (!found)
                {
                    LOG.info(vacante);
                }
                Assert.assertTrue(search + VACANTE + vacante.id + PALABRAS + vacante, found);
            }

        } catch (Exception e)
        {
            LOG.error(e, e);
            Assert.fail(e.getMessage());
        } finally
        {
            if (result != null)
            {
                result.close();
            }

        }
        long dif=System.currentTimeMillis()-tini;
        LOG.info("Tiempo: "+dif+" ms");        
    }

    @Test
    public void testConconocimientos()
    {
        final String search = "administración de empresas con conocimientos en conocimiento";
        Result result = null;
        try
        {
            result = instance.search(search);
            if (result.getCount() == 0)
            {
                Assert.fail("Fallo consulta " + search);
            }
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

        }
    }

    @Test
    public void testSinonimosAdministraciónImpuestos()
    {
        String search = "Administrador de impuestos";
        Result result = null;
        Result result2 = null;
        Result result3 = null;
        Result result4 = null;
        try
        {
            result = instance.search(search);

            search = "Administración de impuestos";
            result2 = instance.search(search);

            Assert.assertTrue(SEARCH + search, result2.getCount() == result.getCount());

            search = "Administración";
            result3 = instance.search(search);

            Assert.assertTrue(SEARCH + search, result2.getCount() <= result3.getCount());

            search = "Administrador";
            result4 = instance.search(search);

            Assert.assertTrue(SEARCH + search, result4.getCount() == result3.getCount());

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();

            result2.close();

            result3.close();

            result4.close();

        }
    }

    @Test
    public void testOperadorNotChofer()
    {

        Result result = null;
        Result result2 = null;
        try
        {
            result = instance.search("operador");
            result2 = instance.search("chofer");
            Assert.assertNotSame(result.getCount(), result2.getCount());

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {

            result.close();
            result2.close();
        }
    }

    @Test
    public void testOperativo()
    {
        final String search = "operativo";
        Result result = null;

        try
        {
            result = instance.search(search);
            LOG.info(result.getCount());
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            if (result != null)
            {
                result.close();
            }

        }
    }

    @Test
    public void testGerente()
    {
        String search = "Gerente";
        Result result = null;
        Result result2 = null;
        try
        {
            result = instance.search(search);

            search = "Gerente general";
            result2 = instance.search(search);

            Assert.assertTrue(SEARCH + search, result2.getCount() <= result.getCount());

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            if (result != null)
            {
                result.close();
            }
            if (result2 != null)
            {
                result2.close();
            }

        }
    }

    @Test
    public void testAdministración()
    {
        final String search = "administración";
        Result result = null;
        try
        {
            result = instance.search(search);
            if (result.getCount() == 0)
            {
                Assert.fail("Fallo consulta " + search);
            }
            while (result.hasNextElement())
            {
                final long idvacante = result.nextElement().id;
                final Vacante vacante = instance.get(idvacante);
                boolean found = false;
                if (isValid(vacante, search, result.getConsulta()))
                {
                    found = true;
                }
                Assert.assertTrue(SEARCH + search + VACANTE + vacante.id + PALABRAS + vacante, found);

            }
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            if (result != null)
            {
                result.close();
            }
        }
    }

    @Test
    public void testStopwords()
    {
        for (String word : CompassInfo.getStopWords())
        {
            final String search = word;
            Result result = null;
            try
            {
                result = instance.search(search);
                if (result.getCount() > 0)
                {
                    Assert.fail("Fallo consulta " + search);
                }

            } catch (Exception e)
            {
                Assert.fail(e.getMessage());
            } finally
            {
                if (result != null)
                {
                    result.close();
                }
            }
        }

    }

    @Test
    public void testVariante()
    {

        final String search = "cría de abejas";
        Result result = null;
        try
        {
            result = instance.search(search);
            //Assert.assertNotSame(result.getCount(), 0);

            if (!result.getConsulta().toString().equals(Campos.CARRERA_O_ESPECIALIDAD.toString()))
            {
                Assert.fail("Fallo test de variante" + search + " text:" + result.getConsulta().text);
            }
            if (!result.getConsulta().carreras_detectadas.contains(111002))
            {
                Assert.fail("Fallo test de variante" + search + " text:" + result.getConsulta().text);
            }
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            if (result != null)
            {
                result.close();
            }
        }
    }

    @Test
    public void testMesero()
    {

        final long tini = System.currentTimeMillis();
        final String search = "mesero";
        final String search2 = "mesa";
        Result result = null;
        Result result2 = null;
        try
        {
            result = instance.search(search);
            result2 = instance.search(search2);
            Assert.assertTrue(SEARCH + search, result.getConsulta().text.equals("meser"));
            Assert.assertTrue(SEARCH + search2, result2.getConsulta().text.equals("mes"));
            Assert.assertTrue(SEARCH + search, result.getCount() > 0);
            Assert.assertTrue(SEARCH + search, result.getCount() != result2.getCount());
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail("Fallo busqueda: " + search + VACANTE + vacante.getId() + PALABRAS + getPalabras(vacante));
                }
            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {
            result2.close();
            result.close();
        }
        final long dif = System.currentTimeMillis() - tini;
        LOG.info("Tiempo " + dif + MILLISECONDS);

    }

    @Test
    public void testJavaJ2ee()
    {

        final long tini = System.currentTimeMillis();
        final String search = "java j2ee";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertTrue(result.getConsulta().text.equals("jav j2e"));
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
            for (int j = 0; j < 10; j++)
            {
                //ResultInfo info = result.nextElement();
                //LOG.info("titulo: " + info.titulo);
            }
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }
        final long dif = System.currentTimeMillis() - tini;
        LOG.info("Tiempo " + dif + MILLISECONDS);

    }

    @Test
    public void testChoferPHP()
    {

        final long tini = System.currentTimeMillis();
        final String search = "chofer php";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertTrue(result.getConsulta().text.equals("chof php"));
            Assert.assertNotSame(result.getCount(), 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(SEARCH + search + VACANTE + info.id + PALABRAS + getPalabras(vacante));
                }
            }
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }
        final long dif = System.currentTimeMillis() - tini;
        LOG.info("Tiempo " + dif + MILLISECONDS);

    }

    @Test
    public void testMeseroEstado()
    {

        final String search = "mesero";
        Result result = null;
        try
        {
            result = instance.search(search, 9);
            Assert.assertTrue(result.getConsulta().text.equals("meser"));

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }

    }

    @Test
    public void testEstado()
    {

        final String search = "";
        Result result = null;
        try
        {
            ParametrosConsultaVacante p = new ParametrosConsultaVacante();
            p.estado = 9;
            //result = instance.search(search, 9);
            result = instance.search(p);

        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }

    }

    @Test
    public void testCaracteresRaros()
    {
        final StringBuilder content = new StringBuilder("\r\n\t");
        for (int i = 0; i <= 255; i++)
        {
            final char _char = (char) i;
            content.append(_char);
        }
        final String search = content.toString();
        Result result = null;
        try
        {
            result = instance.search("\"" + search + "\"");
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
        }
    }

    @Test
    public void referenciableDramaturgoTest()
    {
        final String search = "Dramaturgo";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().text, "dramaturg");
            Assert.assertTrue(CONSULTA + search, !result.getConsulta().sinonimos_ref.isEmpty());
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    LOG.info(isValid(vacante, search, result.getConsulta()));
                }
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    final String palabras = getPalabras(vacante);
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + palabras);
                }
                //LOG.info("titulo: " + info.titulo);
            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
        }

    }

    @Test
    public void sinonimoSastreTest()
    {
        final String search = "Sastre";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().text, "sastr");
            Assert.assertTrue(CONSULTA + search, !result.getConsulta().sinonimos_ref.isEmpty());
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }
                //LOG.info("titulo: " + info.titulo);
            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
        }

    }

    @Test
    public void sinonimoComposiciónTest()
    {
        final String search = "Composición";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().text, "composicion");
            Assert.assertTrue(CONSULTA + search, !result.getConsulta().sinonimos_ref.isEmpty());
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }
                //LOG.info("titulo: " + info.titulo);
            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
        }

    }

    @Test
    public void variantePolitologoTest()
    {
        final String search = "Politólogo";
        final String search2 = "Analista político";
        Result result = null;
        Result result2 = null;
        try
        {
            result = instance.search(search);
            result2 = instance.search(search2);
            Assert.assertSame(CONSULTA + search, result.getCount(), result2.getCount());
            Assert.assertEquals(result.getConsulta().text, "politolog");
            Assert.assertEquals(result2.getConsulta().text, "analist politic");
            Assert.assertTrue(CONSULTA + search, !result.getConsulta().sinonimos_ref.isEmpty());
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }
                //LOG.info("titulo: " + info.titulo);
            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result2.close();
            result.close();
        }

    }

    @Test
    public void variantePiscicultorTest()
    {

        final String search = "Piscicultor";
        final String search2 = "Criador de peces";
        Result result = null;
        Result result2 = null;
        try
        {

            //Thread.sleep(100000);
            result = instance.search(search);
            result2 = instance.search(search2);
            Assert.assertEquals(result.getCount(), result2.getCount());
            Assert.assertEquals(result.getConsulta().text, "piscicultor");
            Assert.assertEquals(result2.getConsulta().text, "criador pec");
            Assert.assertTrue(CONSULTA + search, !result.getConsulta().sinonimos_ref.isEmpty());
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }

            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
            result2.close();
        }

    }

    @Test
    public void referenciableArtesTest()
    {
        final String search = "Artes";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().text, "artes");
            Assert.assertFalse(CONSULTA + search + " lista de referenciables vácia", result.getConsulta().sinonimos_ref.isEmpty());
            final int size = result.getConsulta().sinonimos_ref.get(result.getConsulta().text).size();
            Assert.assertTrue(CONSULTA + search + " sin_ref: " + size, size == 20);
            for (String sim_ref : result.getConsulta().sinonimos_ref.get(result.getConsulta().text))
            {
                LOG.info(sim_ref);
            }
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }
                //LOG.info("titulo: " + info.titulo);

            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
        }
    }

    @Test
    public void referenciableArtesendanzacontemporáneaTest()
    {
        final String search = "Artes en danza contemporánea";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().text, "artes danz contemporane");
            Assert.assertFalse(CONSULTA + search + " lista de referenciables vácia", result.getConsulta().sinonimos_ref.isEmpty());
            final int size = result.getConsulta().sinonimos_ref.get(result.getConsulta().text).size();
            Assert.assertTrue(CONSULTA + search + " sin_ref: " + size, size == 2);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }
                //LOG.info("titulo: " + info.titulo);

            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
        }

    }

    @Test
    public void referenciableAcróbataTest()
    {
        final String search = "Acróbata";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().text, "acrobat");
            Assert.assertTrue(CONSULTA + search, result.getConsulta().sinonimos_ref.isEmpty());
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }
                //LOG.info("titulo: " + info.titulo);

            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
        }

    }

    @Test
    public void promotor()
    {
        final String search = "PROMOTOR DE AFORES";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().text, "promotor afor");
            Assert.assertTrue(CONSULTA + search, !result.getConsulta().sinonimos_ref.isEmpty());
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }
                //LOG.info("titulo: " + info.titulo);

            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());

        } finally
        {
            result.close();
        }

    }

    @Test
    public void contructorTest()
    {
        final int estado = 1;
        final int municipio = 2;
        final int ocupacion = 3;
        final long idvacante = 4;
        final String titulo = "titulo";
        final List<Integer> carreras = new ArrayList<Integer>();
        carreras.add(1);
        carreras.add(2);
        carreras.add(3);
        final List<String> conocimientos = new ArrayList<String>();
        conocimientos.add("a");
        conocimientos.add("b");
        conocimientos.add("c");
        conocimientos.add("d");
        final Vacante vacante = new Vacante(idvacante, titulo, ocupacion, carreras, conocimientos, estado, municipio);
        Assert.assertEquals(idvacante, vacante.id);
        Assert.assertEquals(titulo, vacante.titulo);
        Assert.assertEquals(ocupacion, vacante.ocupacion);
        Assert.assertEquals(estado, vacante.estado);
        Assert.assertEquals(municipio, vacante.municipio);

        Assert.assertEquals(idvacante, vacante.getId());
        Assert.assertEquals(titulo, vacante.getTitulo());
        Assert.assertEquals(ocupacion, vacante.getOcupacion());
        Assert.assertEquals(estado, vacante.getEstado());
        Assert.assertEquals(municipio, vacante.getMunicipio());

        Assert.assertTrue(carreras.size() == vacante.getCarrera().size());
        Assert.assertTrue(conocimientos.size() == vacante.getConocimientos().size());

        Assert.assertTrue(carreras.size() == vacante.carreras.size());
        Assert.assertTrue(conocimientos.size() == vacante.conocimientos.size());

        Assert.assertArrayEquals(carreras.toArray(), vacante.getCarrera().toArray());
        Assert.assertArrayEquals(conocimientos.toArray(), vacante.getConocimientos().toArray());

    }

    @Test
    public void testChoferSinEstado()
    {

        final String titulo = "chofer";
        Result result = null;
        try
        {
            final String search = titulo;
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacante = instance.get(info.id);
                if (!isValid(vacante, search, result.getConsulta()))
                {
                    Assert.fail(FALLO_ID + vacante.id + PALABRAS + getPalabras(vacante));
                }

            }
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }

    }

    @Test
    public void testErrorBusqueda()
    {

        Result result = null;
        try
        {

            result = instance.search("");
            Assert.assertSame(result.getCount(), 0);
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testJava()
    {

        final String search = "Jefe de departamento de carnes";
        Result result = null;
        try
        {
            result = instance.search(search);
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }
    }

    @Test
    public void testLLavesFrase()
    {

        final String search = "\"{}\"";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertTrue(search, result.getCount() == 0);
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }
    }

    @Test
    public void testLLaves()
    {

        final String search = "{}";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertTrue(search, result.getCount() == 0);
        } catch (Exception e)
        {
            LOG.error(ERROR, e);
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testDiscapacidad()
    {
        final String search = "";
        Result result = null;
        try
        {
            ParametrosConsultaVacante p = new ParametrosConsultaVacante();
            p.search = search;
            p.discapacidad = "11000";
            result = instance.search(p);
            while (result.hasNextElement())
            {
                ResultInfo info = result.nextElement();
                long id = info.id;
                if(!"00000".equals(p.discapacidad) && "00000".equals(instance.get(id).getDiscapacidad()))
                {
                    Assert.fail("Fallo prueba trajo 00000");
                }
                System.out.println("" + instance.get(id).getDiscapacidad());
            }
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testFuente()
    {
        final String search = "java";
        Result result = null;
        try
        {
            ParametrosConsultaVacante p = new ParametrosConsultaVacante();
            p.search = search;
            p.fuente = 1;
            p.estado = 9;

            result = instance.search(p);
            while (result.hasNextElement())
            {
                ResultInfo info = result.nextElement();
                long id = info.id;
                if (instance.get(id).getFuente() != p.fuente)
                {
                    Assert.fail("No tiene la fuente correcta: " + p.fuente + " fuente vacante: " + instance.get(id).getFuente());
                }
                if (p.estado != null && instance.get(id).getEstado() != p.estado)
                {
                    Assert.fail("No tiene el estado correcto: " + p.estado + " estado vacante: " + instance.get(id).getEstado());
                }
                if (p.municipio != null && instance.get(id).getMunicipio() != p.municipio)
                {
                    Assert.fail("No tiene el municipio correcto: " + p.municipio + " municipio vacante: " + instance.get(id).getMunicipio());
                }
                System.out.println("" + instance.get(id).getFuente());
            }
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testEdad()
    {
        final String search = "";
        Result result = null;

        for (int i = 1; i <= 100; i++)
        {
            ParametrosConsultaVacante p = new ParametrosConsultaVacante();
            p.search = search;
            p.edad = i;
            try
            {

                result = instance.search(p);
                while (result.hasNextElement())
                {
                    ResultInfo info = result.nextElement();
                    long id = info.id;
                    if (instance.get(id).getEdad_de() != -1)
                    {
                        if (!(instance.get(id).getEdad_de() <= p.edad && p.edad <= instance.get(id).getEdad_hasta()))
                        {
                            Assert.fail("No corresponde la edad (" + p.edad + ") " + instance.get(id).getEdad_de() + " a " + instance.get(id).getEdad_hasta());
                        }
                    }
                    LOG.info("" + instance.get(id).getEdad_de() + " a " + instance.get(id).getEdad_hasta());
                }
                Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
            } catch (Exception e)
            {
                Assert.fail(e.getMessage());
            } finally
            {
                result.close();
            }
        }

    }

    @Test
    public void testSalario()
    {
        final String search = "otr";
        Result result = null;
        ParametrosConsultaVacante p = new ParametrosConsultaVacante();
        p.search=search;
        try
        {
            
            result = instance.search(p);
            while (result.hasNextElement())
            {
                ResultInfo info = result.nextElement();
                long id = info.id;
                if (instance.get(id).getEdad_de() != -1)
                {
                    if (!(instance.get(id).getEdad_de() <= p.edad && p.edad <= instance.get(id).getEdad_hasta()))
                    {
                        Assert.fail("No corresponde la edad (" + p.edad + ") " + instance.get(id).getEdad_de() + " a " + instance.get(id).getEdad_hasta());
                    }
                }
                LOG.info("" + instance.get(id).getEdad_de() + " a " + instance.get(id).getEdad_hasta());
            }
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testEdad30()
    {
        final String search = "";
        Result result = null;

        ParametrosConsultaVacante p = new ParametrosConsultaVacante();
        p.search = search;
        p.edad = 30;
        try
        {

            result = instance.search(p);
            while (result.hasNextElement())
            {
                ResultInfo info = result.nextElement();
                long id = info.id;
                if (instance.get(id).getEdad_de() != -1)
                {
                    if (!(instance.get(id).getEdad_de() <= p.edad && p.edad <= instance.get(id).getEdad_hasta()))
                    {
                        Assert.fail("No corresponde la edad (" + p.edad + ") " + instance.get(id).getEdad_de() + " a " + instance.get(id).getEdad_hasta());
                    }
                }
                LOG.info("" + instance.get(id).getEdad_de() + " a " + instance.get(id).getEdad_hasta());
            }
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        } catch (Exception e)
        {
            Assert.fail(e.getMessage());
        } finally
        {
            result.close();
        }

    }
}
