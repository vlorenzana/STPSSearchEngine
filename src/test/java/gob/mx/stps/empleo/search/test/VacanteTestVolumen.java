/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search.test;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import gob.mx.stps.empleo.search.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;
import static gob.mx.stps.empleo.search.test.VacanteTest.isValid;

/**
 *
 * @author victor.lorenzana
 */
public class VacanteTestVolumen
{

    private static final String MILLISECONDS = " ms";
    private static final String IDCATALOGO = "id_catalogo_opcion";
    private static final String OPCION = "opcion";
    private static final Log LOG = LogFactory.getLog(VacanteTestVolumen.class);
    private static final String PALABRAS = " palabras: ";
    private static final String ERROR = " Error: ";
    private static final String SEARCH = "search: ";
    private static final String SEP = "----------------------------------------------------------";
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
    private static final List<Long> IDS = new ArrayList<Long>();
    private final transient Set<String> busquedas = new HashSet<String>();

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

        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                }
                catch (Exception e2)
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
        llenaIDs();



    }

    private static void llenaIDs()
    {
        Result vacantes = null;
        try
        {
            vacantes = instance.getVacantes();
            while (vacantes.hasNextElement())
            {
                final long idvacante = vacantes.nextElement().id;
                IDS.add(idvacante);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            if (vacantes != null)
            {
                vacantes.close();
            }
        }
        Collections.sort(IDS);
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

    @Test
    public void testVolumen()
    {
        ExecutorService executor = Executors.newFixedThreadPool(4000);

        Runnable worker = new Runnable()
        {

            @Override
            public void run()
            {
                SecureRandom r = new SecureRandom();
                String search = "profesor";
                for (int i = 1; i < 1000; i++)
                {
                    try
                    {
                        
                        //int wait = (r.nextInt() % 100) + 1;
                        Thread.sleep(1000 * 100);

                    }
                    catch (Exception e)
                    {
                    }
                    Result result = null;
                    try
                    {
                        //System.out.println("hilo: " + Thread.currentThread().getName());

                        result = instance.search(search);
                        while (result.hasNextElement())
                        {
                            ResultInfo info = result.nextElement();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace(System.out);

                    }
                    finally
                    {
                        if (result != null)
                        {
                            result.close();
                        }
                    }//instance.search(search);
                }
            }
        };
        for (int i = 0; i < 4000; i++)
        {
            executor.execute(worker);
        }
        try
        {
            Thread.sleep(50000000);

        }
        catch (Exception e)
        {
        }

    }

    @Test
    public void testTodosConocimiento()
    {

        for (Long id : IDS)
        {
            final Vacante vacante = instance.get(id);
            for (String conocimiento : vacante.conocimientos)
            {

                final String search = conocimiento;
                if (search == null)
                {
                    Assert.fail("search nulo");
                }
                Result result = null;
                if (busquedas.contains(search))
                {
                    continue;
                }
                busquedas.add(search);

                try
                {
                    result = instance.search(search);
                    Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
                    checkOcupaciones(result, search, vacante, id);
                }
                catch (Exception e)
                {
                    Assert.fail(e.getMessage());
                }
                finally
                {
                    if (result != null)
                    {
                        result.close();
                    }
                }
            }
        }
    }

    private void checkEstadosCarreras(final Result result, final int estado, final int municpio, final Vacante vacante, final String search)
    {
        boolean found = false;
        while (result.hasNextElement())
        {
            final ResultInfo info = result.nextElement();
            final Vacante _vacante = instance.get(info.id);
            Assert.assertTrue("Fallo vacante: " + _vacante.id, _vacante.estado == estado);
            Assert.assertTrue("Fallo vacante: " + _vacante.id, _vacante.municipio == municpio);
            if (info.id == vacante.id)
            {
                found = true;
            }

        }
        if (!found)
        {
            Assert.fail("No se encontro vacante: " + vacante.id + " estado: " + estado + " search: " + search);
        }
    }

    private void checkOcupaciones(final Result result, final String search, final Vacante vacante, final Long id)
    {
        boolean found = false;
        while (result.hasNextElement())
        {
            final ResultInfo info = result.nextElement();
            final Vacante _vacante = instance.get(info.id);
            if (!isValid(_vacante, search, result.getConsulta()))
            {
                Assert.fail("Fallo vacante: " + _vacante.id + PALABRAS + getPalabras(vacante));
            }
            if (info.id == vacante.id)
            {
                found = true;
            }
        }
        if (!found)
        {
            Assert.fail("Fallo busqueda vacante: " + id + SEARCH + search);
        }
    }

    @Test
    public void testTodasCarreras()
    {

        int index = IDS.indexOf(Long.valueOf(1));
        for (; index < IDS.size(); index++)
        {
            final long idvacante = IDS.get(index);

            final Vacante vacante = instance.get(idvacante);
            for (Integer icarrera : vacante.carreras)
            {
                final String search = CompassInfo.extractParentesis(CARRERAS.get(icarrera));
                if (busquedas.contains(search))
                {
                    continue;
                }
                busquedas.add(search);
                busquedaCarreras(search, icarrera, vacante, idvacante);
            }
        }
    }

    private void busquedaCarreras(final String search, final Integer icarrera, final Vacante vacante, final long idvacante)
    {
        Result result = null;
        try
        {
            LOG.info(SEP);
            LOG.info(VACANTE + idvacante);
            LOG.info(SEP);
            result = instance.search(search);
            Assert.assertNotSame(result, 0);
            Assert.assertTrue("No se encontro la carrera: " + icarrera, result.getConsulta().carreras_detectadas.contains(icarrera));
            boolean found = false;
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                if (info.id == vacante.id)
                {
                    found = true;
                }
            }
            if (!found)
            {
                Assert.fail("No se encontro vacante: " + vacante.id + SEARCH + search);
            }

        }
        catch (Exception e)
        {
            Assert.fail(VACANTE + idvacante + SEARCH + search + ERROR + e.getMessage());
        }
        finally
        {
            if (result != null)
            {
                result.close();
            }
        }
    }

    @Test
    public void testTodosEstados()
    {


        int index = IDS.indexOf(Long.valueOf(1));

        for (; index < IDS.size(); index++)
        {
            final long idvacante = IDS.get(index);

            final Vacante vacante = instance.get(idvacante);
            if (vacante != null)
            {
                final int estado = vacante.estado;
                if (busquedas.contains(String.valueOf(estado)))
                {
                    continue;
                }
                busquedas.add(String.valueOf(estado));
                validaEstado(idvacante, estado, vacante);

            }
        }
    }

    private void checkResults(final Result result, final Vacante vacante, final String search)
    {
        boolean found = false;
        while (result.hasNextElement())
        {
            final ResultInfo info = result.nextElement();
            if (info.id == vacante.id)
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            Assert.assertTrue(SEARCH + search + VACANTE + vacante.id, found);
        }
    }

    private void checkResults(final Result result, final Vacante vacante, final String search, final long idvacante)
    {
        boolean found = false;
        while (result.hasNextElement())
        {
            final ResultInfo info = result.nextElement();
            if (info.id == vacante.id)
            {
                found = true;
                break;
            }
            final Vacante _vacante = instance.get(info.id);
            final boolean valid = isValid(_vacante, search, result.getConsulta());
            Assert.assertTrue("Vacante no válida search: " + search + " " + _vacante.id + " vacante ref: " + idvacante + "  titulo: " + _vacante.getTitulo() + PALABRAS + getPalabras(_vacante), valid);
        }
        Assert.assertTrue("No se encontro vacante " + vacante.id + " titulo: " + vacante.getTitulo(), found);
    }

    private void validaEstado(final long idvacante, final int estado, final Vacante vacante)
    {
        Result result = null;
        try
        {
            LOG.info(SEP);
            LOG.info(VACANTE + idvacante);
            LOG.info(SEP);
            result = instance.search("", estado);
            Assert.assertTrue("Fallo al buscar vacante: " + vacante.id + " estado: " + estado, result.getCount() != 0);

            boolean found = false;
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Vacante vacanteres = instance.get(info.id);
                Assert.assertTrue("vacante fallo " + info.id, vacanteres.estado == estado);
                if (info.id == vacante.id)
                {
                    found = true;
                }


            }
            if (!found)
            {
                Assert.fail("No se encontro vacante: " + vacante.id + " estado: " + estado);
            }

        }
        catch (Exception e)
        {
            Assert.fail("vacante: " + idvacante + " estado:" + estado + ERROR + e.getMessage());
        }
        finally
        {
            if (result != null)
            {
                result.close();
            }
        }
    }

    @Test
    public void testTodosEstadosCarrera()
    {


        int index = IDS.indexOf(Long.valueOf(1));

        for (; index < IDS.size(); index++)
        {
            final long idvacante = IDS.get(index);
            LOG.info(SEP);
            LOG.info("oferta: " + idvacante);
            LOG.info(SEP);
            final Vacante vacante = instance.get(idvacante);


            final int estado = vacante.estado;
            final int municpio = vacante.municipio;
            for (Integer icarrera : vacante.carreras)
            {
                final String search = CompassInfo.extractParentesis(CARRERAS.get(icarrera));
                if (busquedas.contains(search))
                {
                    continue;
                }
                busquedas.add(search);

                Result result = null;
                try
                {
                    result = instance.search(search, estado, municpio);
                    if (result.getCount() == 0)
                    {
                        Assert.fail("Fallo al buscar vacante: " + vacante.id + " carrera " + icarrera + " search: " + search);
                    }
                    else
                    {

                        checkEstadosCarreras(result, estado, municpio, vacante, search);
                    }
                }
                catch (Exception e)
                {
                    Assert.fail("vacante: " + idvacante + " search:" + search + ERROR + e.getMessage());
                }
                finally
                {
                    if (result != null)
                    {
                        result.close();
                    }
                }


            }
        }
    }

    @Test
    public void testTodasOcupaciones()
    {

        for (Long id : IDS)
        {
            final Vacante vacante = instance.get(id);
            final int iocupacion = vacante.ocupacion;
            String search = CompassInfo.extractParentesis(OCUPACIONES.get(iocupacion));
            final IndexInfo indexInfo = CompassInfo.indexOf(search);
            if (indexInfo.pos != -1)
            {
                search = search.substring(0, indexInfo.pos);
            }
            if (busquedas.contains(search))
            {
                continue;
            }
            busquedas.add(search);
            Result result = null;
            try
            {
                LOG.info(SEP);
                LOG.info(VACANTE + id);
                LOG.info(SEP);
                result = instance.search(search);
                Assert.assertNotSame(result.getCount(), 0);
                if (indexInfo.pos == -1)
                {
                    Assert.assertTrue("No se encontro la ocupación: " + iocupacion + SEARCH + search + " texto original: " + CompassInfo.OCUPACIONESCAT.get(iocupacion), result.getConsulta().ocupaciones_detectadas.contains(iocupacion));

                }
                checkResults(result, vacante, search);
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
            finally
            {
                if (result != null)
                {
                    result.close();
                }
            }
        }

    }

    private String getPalabras(final Vacante vacante)
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

    @Test
    @Ignore
    public void testVariantes()
    {
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

                }
                catch (Exception e)
                {
                    LOG.error(e.getMessage(), e);
                    Assert.fail(e.getMessage());
                }
                finally
                {
                    if (result != null)
                    {
                        result.close();
                    }
                }
            }

            int[] expecteds = new int[resultados.size()];
            for (int i = 0; i < resultados.size(); i++)
            {
                expecteds[i] = resultados.get(0);
            }

            int[] actuals = new int[resultados.size()];
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
    }

    @Test
    public void testTitulosAsPhrases()
    {


        int index = IDS.indexOf(Long.valueOf(1));
        for (; index < IDS.size(); index++)
        {
            final long idvacante = IDS.get(index);
            final Vacante vacante = instance.get(idvacante);
            if (vacante != null)
            {

                String search = vacante.getTitulo().replace('"', ' ');
                final int pos = search.indexOf('(');
                if (pos != -1)
                {
                    search = search.substring(0, pos).trim();
                }
                if ("".equals(search))
                {
                    continue;
                }


                if (busquedas.contains(search))
                {
                    continue;
                }
                busquedas.add(search);

                Result result = null;
                try
                {

                    LOG.info(SEP);
                    LOG.info(VACANTE + idvacante);
                    LOG.info(SEP);
                    result = instance.search("\"" + search + "\"");
                    checkResults(result, vacante, search, idvacante);
                }
                catch (Exception e)
                {
                    Assert.fail(e.getMessage());
                }
                finally
                {
                    if (result != null)
                    {
                        result.close();
                    }
                }
            }
        }


    }

    @Test
    public void testTodosLosTitulos()
    {


        int index = IDS.indexOf(Long.valueOf(1));


        for (; index < IDS.size(); index++)
        {
            final long idvacante = IDS.get(index);

            final Vacante vacante = instance.get(idvacante);
            if (vacante != null)
            {
                String search = vacante.getTitulo();

                final int pos = search.indexOf('(');
                if (pos != -1)
                {
                    search = search.substring(0, pos).trim();
                }
                if ("".equals(search))
                {
                    continue;
                }
                Result result = null;
                if (busquedas.contains(search))
                {
                    continue;
                }
                busquedas.add(search);
                try
                {
                    LOG.info(SEP);
                    LOG.info("oferta: " + idvacante);
                    LOG.info(SEP);
                    result = instance.search(search);
                    Assert.assertTrue("Fallo al buscar vacante: " + vacante.id + SEARCH + search, result.getCount() == 0);
                    checkResults(result, vacante, search, idvacante);
                }
                catch (Exception e)
                {
                    Assert.fail("vacante: " + idvacante + SEARCH + search + ERROR + e.getMessage());
                }
                finally
                {
                    result.close();
                }
            }
        }
    }

    private boolean isValid(final Vacante vacante, final String search, final Consulta consulta)
    {
        return VacanteTest.isValid(vacante, search, consulta);
    }
}
