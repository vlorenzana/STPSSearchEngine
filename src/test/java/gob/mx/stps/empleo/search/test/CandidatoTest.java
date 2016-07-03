/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import gob.mx.stps.empleo.search.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Ignore;

/**
 *
 * @author victor.lorenzana
 */
public class CandidatoTest
{

    private static final String CANDIDATO = " candidato: ";
    private static final String CONSULTA = " consulta:";
    private static final String FALLOCANDIDATO = "Fallo al buscar candidato: ";
    private static final Locale LOCALE = new Locale("es", "MX");
    private static final String IDCATALOGO = "id_catalogo_opcion";
    private static final String OPCION = "opcion";
    private static final String SEARCH = "search: ";
    private static final String SEARCHNULO = "search nulo";
    private static final String SEPARATOR = "--------------------";
    private static final String TEXTO = " texto: ";
    private static final List<Long> IDS = new ArrayList<Long>();
    private static final Log LOG = LogFactory.getLog(CandidatoTest.class);
    private static Candidato instanceTest;
    //private static Random r = new Random();
    private static CandidatoSearch instance = null;
    private static final Map<Integer, String> GRADOESTUDIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> CARRERAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> IDIOMAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> DOMINIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> EXPERIENCIAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> HORARIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> STATUSESTUDIO = new HashMap<Integer, String>();
    private static final Map<Integer, String> OCUPACIONES = new HashMap<Integer, String>();
    private transient final Set<String> busquedas = new HashSet<String>();

    private static void llenaIDs()
    {
        Result candidatos = null;
        try
        {
            candidatos = instance.getCandidatos();
            while (candidatos.hasNextElement())
            {
                final long idcandidato = candidatos.nextElement().id;
                IDS.add(idcandidato);

            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            if (candidatos != null)
            {
                candidatos.close();
            }
        }
        Collections.sort(IDS);
    }

    @BeforeClass
    public static void setUpClass()
    {
        final long tini = System.currentTimeMillis();
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
        LOG.info("Fin de llenado catalogos... tiempo: " + dif + " ms");
        final Catalogo catalogo = new Catalogo();
        catalogo.carrera_especialidad_catalogo.putAll(CARRERAS);
        catalogo.dominios.putAll(DOMINIOS);
        catalogo.experiencia_catalogo.putAll(EXPERIENCIAS);
        catalogo.grado_estudios.putAll(GRADOESTUDIOS);
        catalogo.horario_catalogo.putAll(HORARIOS);
        catalogo.idiomas_catalogo.putAll(IDIOMAS);
        catalogo.ocupacion_catalogo.putAll(OCUPACIONES);
        catalogo.status_estudio_catalogo.putAll(STATUSESTUDIO);
        instance = new CandidatoSearch(catalogo);
        llenaIDs();

        final Random ramdom = new Random();
        final int index = ramdom.nextInt(IDS.size());
        final long idramdom = IDS.get(index);
        instanceTest = instance.get(idramdom);
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
            GRADOESTUDIOS.put(idcatalogo, desc);

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
            STATUSESTUDIO.put(idcatalogo, desc);

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
    /*@AfterClass
    public static void tearDownClass() throws Exception
    {
    }*/

    /*@Before
    public void setUp()
    {
    }*/

    /*@After
    public void tearDown()
    {
    }*/
    @Test
    //@Ignore
    public void testDisponibilidadViajar()
    {
        /*try
        {
        Thread.sleep(500000);
        }
        catch(Exception e){}*/
        Result result = null;
        try
        {
            result = instance.search("disponibilidad para viajar");
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.DISPONIBILIDAD.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.isDisponibilidad());
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testDisponibilidadViajar2()
    {

        Result result = null;
        try
        {
            result = instance.search("disponibilidad viajar");
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.DISPONIBILIDAD.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.isDisponibilidad());
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }


    }

    @Test
    //@Ignore
    public void testDisponibilidadRadicarFuera()
    {

        Result result = null;
        try
        {
            result = instance.search("disponibilidad para radicar fuera");
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.DISPONIBILIDAD_VIAJAR_CIUDAD.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.isDisponibilidadViajarCiudad());

            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }


    }

    @Test
    //@Ignore
    public void testDisponibilidadRadicarFuera2()
    {

        Result result = null;
        try
        {
            result = instance.search("disponibilidad radicar fuera");
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.DISPONIBILIDAD_VIAJAR_CIUDAD.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.isDisponibilidadViajarCiudad());
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }


    }

    @Test
    //@Ignore
    public void testExperienciaTotal1()
    {
        final int iexperiencia = instanceTest.getExperiencia();
        final String experiencia = EXPERIENCIAS.get(iexperiencia);
        final String search = experiencia;
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.EXPERIENCIA.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                final int index = candidato.getExperiencia();
                final String desc = CompassInfo.EXPERIENCIA.get(index);
                final Set<Integer> indices = new HashSet<Integer>();
                if (Character.isDigit(desc.charAt(0)))
                {
                    try
                    {
                        final int iaños = Integer.parseInt(String.valueOf(desc.charAt(0)));
                        indices.addAll(CompassInfo.getExperiencia(iaños));
                    }
                    catch (Exception e)
                    {
                        Assert.fail(e.getMessage());
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
                        Assert.fail(e.getMessage());
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
                        Assert.fail(e.getMessage());
                    }
                }
                if(!indices.contains(candidato.getExperiencia()))
                {
                    for(Integer i : indices)
                    {
                        System.out.println("i: "+i+ " exp: "+candidato.getExperiencia());
                        
                    }
                }
                Assert.assertTrue(SEARCH + search, indices.contains(candidato.getExperiencia()));
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    public void testExperienciaTotal2()
    {
        final int años = 5;
        final String search = años + " años de experiencia";

        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.EXPERIENCIA.toString());

            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                final Set<Integer> indices = CompassInfo.getExperiencia(años);
                Assert.assertTrue(indices.contains(candidato.getExperiencia()));
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testExperienciaTotal3()
    {

        final String search = "6 meses a 1 año";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().toString(), Campos.EXPERIENCIA.toString());
            Assert.assertNotSame(result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testExperienciaTotal4()
    {
        final int iexperiencia = instanceTest.getExperiencia();
        final String experiencia = EXPERIENCIAS.get(iexperiencia);
        final String search = experiencia.replace('-', 'a');
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.EXPERIENCIA.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                final int index = candidato.getExperiencia();
                final String desc = CompassInfo.EXPERIENCIA.get(index);
                final Set<Integer> indices = new HashSet<Integer>();
                if (Character.isDigit(desc.charAt(0)))
                {
                    try
                    {
                        final int iaños = Integer.parseInt(String.valueOf(desc.charAt(0)));
                        indices.addAll(CompassInfo.getExperiencia(iaños));
                    }
                    catch (Exception e)
                    {
                        Assert.fail(e.getMessage());
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
                        Assert.fail(e.getMessage());
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
                        Assert.fail(e.getMessage());
                    }
                }
                Assert.assertTrue(SEARCH + search, indices.contains(candidato.getExperiencia()));
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testHorario()
    {
        final int ihorario = instanceTest.getHorario();
        final String search = HORARIOS.get(ihorario);
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.HORARIO_DE_EMPLEO.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.getHorario() == ihorario);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }


    }

    @Test
    //@Ignore
    public void testEdad()
    {

        final String search = instanceTest.getEdad() + " años de edad";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.EDAD.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.getEdad() <= instanceTest.getEdad());

            }

        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }


    }

    @Test
    public void testIngenieriaComputacionIngles80Porciento()
    {

        String search = "Ingeniería en computación inglés al 80%";
        Result result = null;
        Result result2 = null;
        try
        {
            result = instance.search(search);
            search = "Inglés 80% Ingeniero en computación";
            result2 = instance.search(search);
            Assert.assertTrue(search, result.getConsulta().hasCriterio(Campos.CARRERA_O_ESPECIALIDAD.toString()));
            Assert.assertTrue(search, result.getConsulta().hasCriterio(Campos.IDIOMA.toString()));
            Assert.assertTrue(search, result.getConsulta().hasCriterio(Campos.DOMINIO.toString()));
            Assert.assertTrue(search, result2.getConsulta().hasCriterio(Campos.CARRERA_O_ESPECIALIDAD.toString()));
            Assert.assertTrue(search, result2.getConsulta().hasCriterio(Campos.IDIOMA.toString()));
            Assert.assertTrue(search, result2.getConsulta().hasCriterio(Campos.DOMINIO.toString()));
            Assert.assertTrue(search, result.getCount() == result2.getCount());
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
            result2.close();
        }
    }

    @Test
    //@Ignore
    public void testRangoEdad()
    {
        final int edad_de = instanceTest.getEdad();
        final int edad_hasta = edad_de + 5;
        final String search = edad_de + " años de edad  " + edad_hasta + " años de edad";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.EDAD.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.getEdad() >= edad_de && candidato.getEdad() <= edad_hasta);
            }

        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testSalario1()
    {


        final String search = "$10000";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.SALARIO.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);

                Assert.assertTrue(candidato.getSalario() <= 10000);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testSalario2()
    {
        final String search = "$10,000";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.SALARIO.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.getSalario() <= 10000);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testSalario3()
    {
        final String search = "10000 pesos";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.SALARIO.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.getSalario() >= 1000);
                Assert.assertTrue(candidato.getSalario() <= 10000);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testTodosIdiomas()
    {
        final Set<String> busqueda = new HashSet<String>();
        for (Integer _idioma : IDIOMAS.keySet())
        {
            final String search = IDIOMAS.get(_idioma);
            Result result = null;
            try
            {
                if (busqueda.contains(search))
                {
                    continue;
                }
                busqueda.add(search);
                result = instance.search(search);
                Assert.assertNotSame(result.getCount(), 0);
                if (!(_idioma.intValue() == 9 || _idioma.intValue() == 1)) // indice 9 No es requisito duplicado con experiencia
                {
                    Assert.assertEquals(result.getConsulta().toString(), Campos.IDIOMA.toString());
                    while (result.hasNextElement())
                    {
                        final long idcandidato = result.nextElement().id;
                        final Candidato candidato = instance.get(idcandidato);
                        boolean found = false;
                        for (Idioma oidioma : candidato.getIdiomas())
                        {
                            if (oidioma.id == _idioma)
                            {
                                found = true;
                                break;
                            }
                        }
                        Assert.assertTrue(found);
                    }
                }

            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
            finally
            {
                result.close();
            }
        }

    }

    @Test
    //@Ignore
    public void testTodosIdiomasDominios()
    {
        for (Integer idioma : IDIOMAS.keySet())
        {
            for (Integer idominio : DOMINIOS.keySet())
            {
                final String _idioma = IDIOMAS.get(idioma);
                final String dominio = DOMINIOS.get(idominio);
                final String search = _idioma + " " + dominio;
                if (busquedas.contains(search))
                {
                    continue;
                }
                busquedas.add(search);
                consultaIdioma(search, idioma, idominio);

            }
        }

    }

    private void agregaSimilares(final Consulta consulta, final Set<Integer> totales)
    {
        if (consulta != null && !consulta.carreras_detectadas.isEmpty())
        {

            totales.addAll(consulta.carreras_detectadas);
            for (Integer i_carrera : consulta.carreras_detectadas)
            {
                final Integer[] similares = CompassInfo.getCarrerasSimilaresCatalogo(i_carrera);
                if (similares != null)
                {
                    totales.addAll(Arrays.asList(similares));
                }
            }
        }
    }

    private boolean checkAbreviaciones(final String nword, final String palabras)
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

    private void checkCarreras(final Result result, final String search, final int icarrera, final Candidato candidato) throws AssertionError
    {
        boolean found = false;
        while (result.hasNextElement())
        {
            final ResultInfo info = result.nextElement();            
            final Candidato rescandidato = instance.get(info.id);
            final boolean isValid = isValid(rescandidato, search, result.getConsulta());            
            Assert.assertTrue(FALLOCANDIDATO + rescandidato.id + " carrera " + icarrera + TEXTO + search, isValid);
            if (info.id == candidato.id)
            {
                found = true;
            }
        }
        Assert.assertTrue(FALLOCANDIDATO + candidato.id + " carrera " + icarrera + TEXTO + search, found);

    }

    private void checkOcupaciones(final Result result, final String search, final int iocupacion, final Candidato candidato)
    {
        boolean found = false;
        while (result.hasNextElement())
        {
            final ResultInfo info = result.nextElement();
            final Candidato rescandidato = instance.get(info.id);
            if (!isValid(rescandidato, search, result.getConsulta()))
            {
                LOG.info("debug");
            }
            final boolean isValid = isValid(rescandidato, search, result.getConsulta());

            Assert.assertTrue(FALLOCANDIDATO + rescandidato.id + " ocupación " + iocupacion + TEXTO + search, isValid);

            if (info.id == candidato.id)
            {
                found = true;
            }
        }
        Assert.assertTrue(FALLOCANDIDATO + candidato.id + " ocupación " + iocupacion + TEXTO + search, found);

    }

    /*private void checkResults(final Result result, final Integer igrado, final String nsearch1)
    {
    while (result.hasNextElement())
    {
    final long idcandidato = result.nextElement().id;
    final Candidato candidato = instance.get(idcandidato);
    boolean found = false;
    for (InformacionAcademica info : candidato.informacion_academica)
    {
    if (info.grado_estudios == igrado)
    {
    found = true;
    break;
    }
    }
    if (!found)
    {
    Assert.fail(SEARCH + nsearch1 + " igrado:" + igrado + CANDIDATO + candidato.id);
    }
    }
    }*/
    private boolean checkSinonimos(final String word, final String palabras)
    {
        boolean found = false;
        for (String sin : CompassInfo.getSinonimos(word))
        {
            if (palabras.indexOf(sin) != -1)
            {
                found = true;
                break;
            }
        }
        return found;
    }

    private void checkTodosGrados(final Result result, final Integer igrado, final String search)
    {
        while (result.hasNextElement())
        {
            final ResultInfo res = result.nextElement();
            if (igrado != 1)
            {
                boolean found = false;
                final long idcandidato = res.id;
                final Candidato candidato = instance.get(idcandidato);
                for (InformacionAcademica info : candidato.getInformacionAcademica())
                {
                    if (info.grado_estudios == igrado)
                    {
                        found = true;
                        break;
                    }
                }
                Assert.assertTrue(SEARCH + search + " igrado:" + igrado + CANDIDATO + candidato.id, found);

            }
        }
    }

    private void consultaIdioma(final String search, final Integer idioma, final Integer idominio)
    {
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            if (!(idioma == 9 || idioma == 1)) // no es requisito es ambiguo
            {
                Assert.assertTrue(result.getConsulta().hasCriterio(Campos.IDIOMA.toString()));
                Assert.assertTrue(result.getConsulta().hasCriterio(Campos.DOMINIO.toString()));
                while (result.hasNextElement())
                {
                    final long idcandidato = result.nextElement().id;
                    final Candidato candidato = instance.get(idcandidato);
                    boolean found = false;
                    for (Idioma oidioma : candidato.getIdiomas())
                    {
                        if (oidioma.id == idioma && oidioma.dominio_id >= idominio)
                        {
                            found = true;
                            break;
                        }
                    }
                    Assert.assertTrue("search:" + search + " idioma: " + idioma, found);
                }
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }



    }

    @Test
    public void testIdiomas100()
    {
        final String search = "ingles 10%";
        Result result = null;
        try
        {
            result = instance.search(search);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testIdiomas1()
    {
        final int idioma = instanceTest.getIdiomas().get(0).id;
        final String search = IDIOMAS.get(idioma);
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            if (!(idioma == 9 || idioma == 1)) // no es requisito es ambiguo
            {
                Assert.assertEquals(result.getConsulta().toString(), Campos.IDIOMA.toString());
                while (result.hasNextElement())
                {
                    final long idcandidato = result.nextElement().id;
                    final Candidato candidato = instance.get(idcandidato);
                    boolean found = false;
                    for (Idioma oidioma : candidato.getIdiomas())
                    {
                        if (oidioma.id == idioma)
                        {
                            found = true;
                            break;
                        }
                    }
                    Assert.assertTrue(found);
                }
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }



    }

    @Test
    //@Ignore
    public void testIdiomas2()
    {
        final int idioma = instanceTest.getIdiomas().get(0).id;
        final int idominio = instanceTest.getIdiomas().get(0).dominio_id;
        final String _idioma = IDIOMAS.get(idioma);
        final String dominio = DOMINIOS.get(idominio);
        final String search = _idioma + " " + dominio;
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
            if (!(idioma == 9 || idioma == 1)) // no es requisito es ambiguo
            {
                Assert.assertTrue(SEARCH + search + " idioma: " + idioma + CONSULTA + result.getConsulta().toString(), result.getConsulta().hasCriterio(Campos.IDIOMA.toString()));
                Assert.assertTrue(SEARCH + search + CONSULTA + result.getConsulta().toString(), result.getConsulta().hasCriterio(Campos.DOMINIO.toString()));
                while (result.hasNextElement())
                {
                    final long idcandidato = result.nextElement().id;
                    final Candidato candidato = instance.get(idcandidato);
                    boolean found = false;
                    for (Idioma oidioma : candidato.getIdiomas())
                    {
                        if (oidioma.id == idioma && oidioma.dominio_id >= idominio)
                        {
                            found = true;
                            break;
                        }
                    }
                    Assert.assertTrue("search:" + search + " idioma: " + idioma, found);
                }
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testOperador()
    {

        final String search = "operador";        
        Result result = null;
        try
        {
            
            result = instance.search(search);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Candidato candidato = instance.get(info.id);
                if (!isValid(candidato, search, result.getConsulta()))
                {
                    LOG.info("debug");
                }
                if (!isValid(candidato, search, result.getConsulta()))
                {
                    Assert.fail("No es valido candidato: " + info.id + " search:" + search);
                }
                /*LOG.info("id: "+ info.id +" ocupacion : "+CompassInfo.OCUPACIONES.get(candidato.ocupacion));
                for(InformacionAcademica academica : candidato.academica)
                {
                String carrera=CompassInfo.CARRERAS.get(academica.carrera);
                LOG.info("id: "+ info.id +" carrera : "+carrera);
                }*/
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testCarerra()
    {
        final int icarrera = instanceTest.getInformacionAcademica().get(0).carrera;
        final String carrera = CARRERAS.get(icarrera);
        final String search = carrera;
        if (search == null)
        {
            Assert.fail(SEARCHNULO);
        }
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(SEARCH + search + " carrera: " + icarrera + " textcarrera: " + carrera, result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    @Ignore
    public void testSelecionadorDePiel()
    {


        final String search = "Seleccionador de piel";
        if (search == null)
        {
            Assert.fail(SEARCHNULO);
        }
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testOcupacion()
    {

        final int iocupacion = instanceTest.getOcupacion();
        final String ocupacion = OCUPACIONES.get(iocupacion);
        final String search = ocupacion;
        if (search == null)
        {
            Assert.fail(SEARCHNULO);
        }
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(SEARCH + search + " iocupacion: " + iocupacion + " textocupacion: " + ocupacion, result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testlicenciatura()
    {
        Result result = null;
        try
        {
            final String search = "licenciatura";
            result = instance.search(search);
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
            Assert.assertEquals(SEARCH + search, result.getConsulta().toString(), Campos.GRADO_DE_ESTUDIOS.toString());

        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testTodosGrado()
    {

        for (Integer igrado : GRADOESTUDIOS.keySet())
        {
            final String grado = GRADOESTUDIOS.get(igrado);
            final String search = grado;
            if (search == null)
            {
                Assert.fail(SEARCHNULO);
            }
            final String[] values = search.split("(/| o | y )");
            for (String value : values)
            {
                Result result = null;
                try
                {
                    if (busquedas.contains(value))
                    {
                        continue;
                    }
                    busquedas.add(value);
                    LOG.info(SEPARATOR);
                    LOG.info("Grado: " + value);
                    LOG.info(SEPARATOR);
                    result = instance.search(value);
                    Assert.assertNotSame(SEARCH + value, result.getCount(), 0);
                    Assert.assertEquals(SEARCH + value + " grado: " + igrado, result.getConsulta().toString(), Campos.GRADO_DE_ESTUDIOS.toString());
                    checkTodosGrados(result, igrado, value);
                }
                catch (Exception e)
                {
                    Assert.fail(e.getMessage());
                }
                finally
                {
                    result.close();
                }
            }

        }
    }

    @Test
    public void testGrado()
    {
        final int igrado = instanceTest.getInformacionAcademica().get(0).grado_estudios;
        final String grado = GRADOESTUDIOS.get(igrado);
        final String search = grado;
        if (search == null)
        {
            Assert.fail(SEARCHNULO);
        }
        final int pos = search.indexOf('/');
        if (pos == -1)
        {
            Result result = null;
            try
            {
                result = instance.search(search);
                Assert.assertEquals(result.getConsulta().toString(), Campos.GRADO_DE_ESTUDIOS.toString());
                if (result.getCount() == 0)
                {
                    Assert.fail(" search " + search);
                }
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
            finally
            {
                result.close();
            }
        }
        else
        {
            final String nsearch1 = search.substring(pos + 1);
            final String nsearch2 = search.substring(0, pos);
            Result result = null;
            try
            {
                result = instance.search(nsearch1);
                Assert.assertEquals(result.getConsulta().toString(), Campos.GRADO_DE_ESTUDIOS.toString());
                if (result.getCount() == 0)
                {
                    Assert.fail(" search " + nsearch1);
                }
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
            finally
            {
                result.close();
            }
            try
            {
                result = instance.search(nsearch2);
                Assert.assertEquals(result.getConsulta().toString(), Campos.GRADO_DE_ESTUDIOS.toString());
                if (result.getCount() == 0)
                {
                    Assert.fail(" search " + nsearch2);
                }
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
            finally
            {
                result.close();
            }


        }



    }

    @Test
    //@Ignore
    public void testStatus()
    {
        final int status = instanceTest.getInformacionAcademica().get(0).status;
        final String search = STATUSESTUDIO.get(status);
        if (search == null)
        {
            Assert.fail(SEARCHNULO);
        }
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertEquals(result.getConsulta().toString(), Campos.STATUS_ACADEMICO.toString());
            Assert.assertNotSame(result.getCount(), 0);
        }
        catch (Exception e)
        {
            LOG.error(e);
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

    @Test
    //@Ignore
    public void testConocimientos()
    {
        final String search = instanceTest.getConocimientos().get(1).name;
        if (search == null)
        {
            Assert.fail(SEARCHNULO);
        }
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    //@Ignore
    public void testHabilidad()
    {
        final String search = instanceTest.getHabilidades().get(1).name;
        if (search == null)
        {
            Assert.fail(SEARCHNULO);
        }
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    public void testTodasCarreras()
    {        
        int index = IDS.indexOf(Long.valueOf(151));
        for (; index < IDS.size(); index++)
        {
            final long idcandidato = IDS.get(index);
            final Candidato candidato = instance.get(idcandidato);

            for (InformacionAcademica academica : candidato.getInformacionAcademica())
            {
                final int icarrera = academica.carrera;
                String search = CARRERAS.get(icarrera);                
                Result result = null;
                try
                {
                    search = CompassInfo.extractParentesis(search);
                    if (busquedas.contains(search))
                    {
                        continue;
                    }
                    if ("De nivel superior".equals(search))
                    {
                        continue;
                    }
                    if ("Educación superior".equals(search) || "Educación media superior".equals(search))
                    {
                        continue;
                    }
                    if ("Francés".equals(search))
                    {
                        continue;
                    }
                    if ("Ingles".equals(search) || "Inglés".equals(search))
                    {
                        continue;
                    }
                    if ("Italiano".equals(search))
                    {
                        continue;
                    }
                    if ("Alemán".equals(search))
                    {
                        continue;
                    }
                    busquedas.add(search);
                    LOG.info("candidato: " + idcandidato);
                    result = instance.search(search);
                    Assert.assertFalse(FALLOCANDIDATO + candidato.id + " carrera " + icarrera + SEARCH + search + CANDIDATO + candidato.id, result.getCount() == 0);
                    checkCarreras(result, search, icarrera, candidato);

                }
                catch (Exception e)
                {
                    LOG.error(e);
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
    public void testTodasOcupaciones()
    {
        int index = IDS.indexOf(Long.valueOf(1));        
        for (; index < IDS.size(); index++)
        {
            final long idcandidato = IDS.get(index);
            final Candidato candidato = instance.get(idcandidato);
            final int iocupacion = candidato.getOcupacion();
            String search = OCUPACIONES.get(iocupacion);
            if (search == null)
            {
                Assert.fail("search nulo " + iocupacion);
            }
            Result result = null;
            try
            {
                search = CompassInfo.extractParentesis(search);
                if (busquedas.contains(search))
                {
                    continue;
                }
                busquedas.add(search);
                LOG.info("candidato: " + idcandidato);
                result = instance.search(search);
                Assert.assertFalse("Fallo al buscar ocupación " + iocupacion + TEXTO + search + CANDIDATO + candidato.id, result.getCount() == 0);
                checkOcupaciones(result, search, iocupacion, candidato);
            }
            catch (Exception e)
            {
                LOG.error(e);
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

    @Test
    public void testgetExperienciaCatalogo()
    {
        final Set<Integer> index = CompassInfo.getExperiencia(6);
        Assert.assertFalse(index.isEmpty());
        Assert.assertTrue(index.size() == 8);
    }

    @Test
    public void testAbreviacion()
    {
        final String search = "mec.";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertTrue(SEARCH + search + CONSULTA + result.getConsulta().toString(), result.getConsulta().hasCriterio(Campos.CARRERA_O_ESPECIALIDAD.toString()));
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    public void testVentas()
    {
        final String search = "ventas";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertTrue(SEARCH + search + CONSULTA + result.getConsulta().toString(), result.getConsulta().hasCriterio(Campos.CARRERA_O_ESPECIALIDAD.toString()));
            Assert.assertTrue(SEARCH + search + CONSULTA + result.getConsulta().toString(), result.getCount() > 0);

        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    public void testSiderurgia()
    {

        String search = "Siderurgia (procesos primarios y aceración)";
        Result result = null;
        try
        {
            search = CompassInfo.extractParentesis(search);
            result = instance.search(search);
            Assert.assertTrue(SEARCH + search + CONSULTA + result.getConsulta().toString(), result.getConsulta().hasCriterio(Campos.CARRERA_O_ESPECIALIDAD.toString()));
            Assert.assertTrue(SEARCH + search + CONSULTA + result.getConsulta().toString(), result.getCount() > 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Candidato rescandidato = instance.get(info.id);
                if (!isValid(rescandidato, search, null))
                {
                    Assert.fail("Fallo validación candidato: " + rescandidato.id + " search: " + search);
                }
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    public void testVariante()
    {


        final String search = "sin educación";
        Result result = null;
        try
        {
            result = instance.search(search);
            if (result.getCount() == 0)
            {
                Assert.fail("Fallo Variante " + search);
            }
            if (!result.getConsulta().toString().equals("GRADO_DE_ESTUDIOS"))
            {
                Assert.fail("Fallo Variante " + search);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
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
            if (result.getCount() == 0)
            {
                Assert.fail("Fallo comas " + search);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    public void testAdministrador()
    {
        final String search = "administrador";
        Result result = null;
        try
        {
            result = instance.search(search);
            if (result.getCount() == 0)
            {

                Assert.fail("Fallo testAdministrador " + search);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    public Set<String> getWordsFromAbrev(final String _abrev)
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

    public Set<String> getWordsRelatedAbrev(final String word)
    {
        final Set<String> related = new HashSet<String>();
        for (String word_key : CompassInfo.ABREVIACIONESNOM.keySet())
        {
            final Set<String> _abrevs = CompassInfo.ABREVIACIONESNOM.get(word_key);
            for (String _abrev : _abrevs)
            {
                final Set<String> words_abrev = getWordsFromAbrev(_abrev);
                if (words_abrev.contains(word))
                {
                    related.addAll(words_abrev);
                }

            }
        }
        return related;
    }

    private boolean hasAllWords(final String palabras, final String variante)
    {
        
        for (String word : CompassInfo.sortedtokenizer(variante))
        {
            boolean found = false;
            String nword = CompassInfo.steam(word);
            if (palabras.indexOf(nword) == -1)
            {
                nword = CompassInfo.normaliza(word);
                if (palabras.indexOf(nword) != -1)
                {
                    found = true;
                }
            }
            else
            {
                found = true;

            }

            if (!found)
            {
                found = checkAbreviaciones(nword, palabras);
            }
            if (!found)
            {
                found = checkSinonimos(word, palabras);
            }
            if (!found)
            {
                return false;
            }


        }
        return true;
    }

    private boolean isValid(final Candidato candidato, final String text2, final Consulta consulta)
    {
        String search = text2.toLowerCase();
        final String palabras = getPalabras(candidato);
        if (hasAllWords(palabras, search))
        {
            return true;
        }
        search = CompassInfo.changeCharacters(search);
        if (revisaPalabras(search, palabras))
        {
            return true;
        }

        if (isValidVariantes(palabras, search))
        {
            return true;
        }
        if (revisaSinonimosRef(consulta, candidato))
        {
            return true;
        }

        if (revisaCarreras(consulta, candidato))
        {
            return true;
        }

        if (revisaOcupaciones(consulta, candidato))
        {
            return true;
        }
        return false;
    }

    private boolean revisaPalabras(final String search, final String palabras)
    {
        for (String word : CompassInfo.sortedtokenizer(search))
        {
            word = CompassInfo.normaliza(word);
            for (String sin : CompassInfo.getSinonimos(word))
            {
                if (palabras.indexOf(sin) != -1)
                {
                    return true;
                }
                if (revisaVariantes(sin, palabras))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void llenaSimilaresOcupaciones(final Consulta consulta, final Set<Integer> totales)
    {
        if (consulta != null && !consulta.ocupaciones_detectadas.isEmpty())
        {

            totales.addAll(consulta.ocupaciones_detectadas);
            for (Integer i_ocupacion : consulta.ocupaciones_detectadas)
            {
                final Integer[] similares = CompassInfo.getOcupacionesSimilaresCatalogo(i_ocupacion);
                if (similares != null)
                {
                    totales.addAll(Arrays.asList(similares));
                }
            }
        }
    }

    private boolean revisaOcupaciones(final Consulta consulta, final Candidato candidato)
    {
        final Set<Integer> totales = new HashSet<Integer>();
        llenaSimilaresOcupaciones(consulta, totales);
        if (consulta != null && totales.size() > 1)
        {
            for (Integer iocupacion : totales)
            {
                if (CompassInfo.OCUPACIONES.containsKey(iocupacion))
                {
                    final String text = CompassInfo.OCUPACIONES.get(iocupacion);
                    if (isValid(candidato, text, null))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean revisaCarreras(final Consulta consulta, final Candidato candidato)
    {
        final Set<Integer> totales = new HashSet<Integer>();
        agregaSimilares(consulta, totales);
        if (consulta != null && totales.size() > 1)
        {
            for (Integer icarrera_det : totales)
            {
                if (CompassInfo.CARRERAS.containsKey(icarrera_det))
                {
                    final String text = CompassInfo.CARRERAS.get(icarrera_det);
                    if (isValid(candidato, text, null))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean revisaSinonimosRef(final Consulta consulta, final Candidato candidato)
    {
        if (consulta != null && !consulta.sinonimos_ref.isEmpty())
        {
            for (String key : consulta.sinonimos_ref.keySet())
            {
                final Set<String> values = consulta.sinonimos_ref.get(key);
                for (String value : values)
                {
                    if (isValid(candidato, value, null))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean revisaVariantes(final String sin, final String palabras)
    {
        final String[] variantes = CompassInfo.getVariantes(sin);
        if (variantes != null)
        {
            for (String variante : variantes)
            {
                boolean found = false;
                for (String word_var : CompassInfo.simpleSortedtokenizer(variante))
                {
                    if (palabras.indexOf(word_var) == -1)
                    {
                        found = false;
                    }
                    else
                    {
                        found = true;
                    }
                }
                if (found)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidVariantes(final String palabras, final String search)
    {

        for (String variante : CompassInfo.getVariantes(search))
        {
            if (hasAllWords(palabras, variante))
            {
                return true;
            }
            variante = CompassInfo.normaliza(variante);
            for (String sinonimo : CompassInfo.getSinonimos(variante))
            {
                if (hasAllWords(palabras, sinonimo))
                {
                    return true;
                }
            }

        }
        return false;
    }

    private String getPalabras(final Candidato candidato)
    {
        final StringBuilder sb = new StringBuilder();
        final CandidatoIndex index = new CandidatoIndex(candidato);
        sb.append(candidato.getPalabras());
        sb.append(" ");
        for (Integer carerera : index.getCarreras())
        {
            String text = CompassInfo.CARRERAS.get(carerera);
            if (text != null)
            {
                text = CompassInfo.normaliza(text);
                sb.append(text);
                sb.append(" ");
            }
        }

        for (Integer carerera : index.getCarreras())
        {
            for (Integer isimilar : CompassInfo.getCarrerasSimilaresCatalogo(carerera))
            {
                String text = CompassInfo.CARRERAS.get(isimilar);
                if (text != null)
                {
                    text = CompassInfo.normaliza(text);
                    sb.append(text);
                    sb.append(" ");
                }
            }
        }
        for (Integer isimilar : CompassInfo.getOcupacionesSimilaresCatalogo(candidato.getOcupacion()))
        {
            String text = CompassInfo.OCUPACIONES.get(isimilar);
            if (text != null)
            {
                text = CompassInfo.normaliza(text);
                sb.append(text);
                sb.append(" ");
            }
        }
        CompassInfo.toLowerCase(sb);        
        return sb.toString();
    }

    @Test
    public void testMedicoMedicina()
    {

        final String search = "medico", search2 = "Medicina";
        Result result = null;
        Result result2 = null;
        try
        {
            result = instance.search(search);
            result2 = instance.search(search2);
            Assert.assertTrue(search, result.getCount() > 0);
            Assert.assertTrue(search, result2.getCount() > 0);
            Assert.assertTrue(search, result2.getCount() == result.getCount());

        }
        catch (Exception e)
        {
            LOG.error(e);
            Assert.fail(e.getMessage());
        }
        finally
        {
            result2.close();
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
            Assert.assertTrue(search, result.getCount() > 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Candidato candidato = instance.get(info.id);
                final boolean isValid = isValid(candidato, search, result.getConsulta());
                final String palabras = getPalabras(candidato);
                Assert.assertTrue(SEARCH + search + " palabras: " + palabras, isValid);
            }
        }
        catch (Exception e)
        {
            LOG.error(e);
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }

    }

    @Test
    public void testMesero()
    {
        final String search = "mesero";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertTrue(search, result.getCount() > 0);
            while (result.hasNextElement())
            {
                final ResultInfo info = result.nextElement();
                final Candidato candidato = instance.get(info.id);
                final boolean isValid = isValid(candidato, search, result.getConsulta());
                Assert.assertTrue(SEARCH + search + " palabras: " + getPalabras(candidato), isValid);
            }
        }
        catch (Exception e)
        {
            LOG.error(e);
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }

    @Test
    //@Ignore
    public void testGenero()
    {
        final String search = "licenciada";
        Result result = null;
        try
        {
            result = instance.search(search);
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }
    
    @Test
    //@Ignore
    public void testMunicpio()
    {
        final String search = "";
        Result result = null;
        try
        {
            result = instance.search(search,2,2);
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
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
            result = instance.search(search,2,null);
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
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
            result = instance.search(search, null, null, "01000");
            while(result.hasNextElement())
            {
                ResultInfo info=result.nextElement();
                long id=info.id;
                System.out.println(""+instance.get(id).getDiscapacidad());
            }
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }
    
    
    @Test
    //@Ignore
    public void testFuente()
    {
        final String search = "";
        Result result = null;
        try
        {
            
            result = instance.search(search, null, null, null,2);
            while(result.hasNextElement())
            {
                ResultInfo info=result.nextElement();
                long id=info.id;
                System.out.println(""+instance.get(id).getFuente());
            }
            Assert.assertNotSame(SEARCH + search, result.getCount(), 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }
    }


    @Test
    //@Ignore
    public void testHorarioConTextoExperiencia()
    {
        final int horario = instanceTest.getHorario();
        final StringBuilder search = new StringBuilder(HORARIOS.get(horario));
        if (search == null)
        {
            Assert.fail(SEARCHNULO);
        }
        Result result = null;
        try
        {
            if (search.toString().endsWith("año"))
            {
                search.append("s de experiencia");
            }
            if (search.toString().endsWith("años"))
            {
                search.append(" de experiencia");
            }
            result = instance.search(search.toString());
            Assert.assertNotSame(result.getCount(), 0);
            Assert.assertEquals(result.getConsulta().toString(), Campos.HORARIO_DE_EMPLEO.toString());
            while (result.hasNextElement())
            {
                final long idcandidato = result.nextElement().id;
                final Candidato candidato = instance.get(idcandidato);
                Assert.assertTrue(candidato.getHorario() == horario);
            }
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
        finally
        {
            result.close();
        }


    }
}
