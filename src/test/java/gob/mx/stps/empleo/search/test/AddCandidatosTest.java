/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search.test;

import gob.mx.stps.empleo.search.Candidato;
import gob.mx.stps.empleo.search.CandidatoSearch;
import gob.mx.stps.empleo.search.Catalogo;
import gob.mx.stps.empleo.search.CompassInfo;
import gob.mx.stps.empleo.search.Conocimiento;
import gob.mx.stps.empleo.search.Consulta;
import gob.mx.stps.empleo.search.Habilidad;
import gob.mx.stps.empleo.search.Idioma;
import gob.mx.stps.empleo.search.InformacionAcademica;
import gob.mx.stps.empleo.search.Result;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author victor.lorenzana
 */
public class AddCandidatosTest
{

    private static final String IDCATALOGO = "id_catalogo_opcion";
    private static final Log LOG = LogFactory.getLog(AddCandidatosTest.class);
    private static final String OPCION = "opcion";
    private static Random random = new Random();
    private static CandidatoSearch candidatoSearch = null;
    private static final Map<Integer, String> GRADOESTUDIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> CARRERAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> IDIOMAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> DOMINIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> EXPERIENCIA = new HashMap<Integer, String>();
    private static final Map<Integer, String> HORARIOCATALOGO = new HashMap<Integer, String>();
    private static final Map<Integer, String> ESTATUSESTUDIO = new HashMap<Integer, String>();
    private static final Map<Integer, String> OCUPACIONES = new HashMap<Integer, String>();
    private static final int TOTAL = 10000;
    private static final int TAMBLOQUE = 1000;

    @BeforeClass
    public static void setUpClass()
    {
        final long tini = System.currentTimeMillis();
        try
        {
            Class.forName("org.gjt.mm.mysql.Driver");
        }
        catch (Exception e)
        {
            LOG.error(e);
        }
        LOG.info("llenado catalogos...");
        Connection conexion = null;
        try
        {
            //Class.forName("oracle.jdbc.OracleDriver");
            conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/empleo", "root", "root");
            llenaHorarios(conexion);
            llenaCarerras(conexion);
            llenaOcupaciones(conexion);

            llenaIdiomas(conexion);

            llenaDominios(conexion);

            llenaStatus(conexion);

            llenaGrados(conexion);
            llenaExperiencia(conexion);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
        {
            if (conexion != null)
            {
                try
                {
                    conexion.close();
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
        catalogo.experiencia_catalogo.putAll(EXPERIENCIA);
        catalogo.grado_estudios.putAll(GRADOESTUDIOS);
        catalogo.horario_catalogo.putAll(HORARIOCATALOGO);
        catalogo.idiomas_catalogo.putAll(IDIOMAS);
        catalogo.ocupacion_catalogo.putAll(OCUPACIONES);
        catalogo.status_estudio_catalogo.putAll(ESTATUSESTUDIO);
        candidatoSearch = new CandidatoSearch(catalogo);
        try
        {
            Thread.sleep(4 * 1000);
        }
        catch (Exception e)
        {
            LOG.error("Error", e);
        }

    }

    private static void llenaExperiencia(final Connection conexion) throws SQLException
    {
        final Statement stmt = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=14");
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=14");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            EXPERIENCIA.put(idcatalogo, desc);

        }
        query.close();
        stmt.close();
    }

    private static void llenaGrados(final Connection conexion) throws SQLException
    {
        final Statement stmt = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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

    private static void llenaStatus(final Connection conexion) throws SQLException
    {
        final Statement stmt = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=10");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=10");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            ESTATUSESTUDIO.put(idcatalogo, desc);

        }
        query.close();

        stmt.close();
    }

    private static void llenaDominios(final Connection conexion) throws SQLException
    {
        final Statement stmt = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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

    private static void llenaIdiomas(final Connection conexion) throws SQLException
    {
        final Statement stmt = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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

    private static void llenaOcupaciones(final Connection conexion) throws SQLException
    {
        final Statement stmt = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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

    private static void llenaCarerras(final Connection conexion) throws SQLException
    {
        Statement stmt = null;
        ResultSet query = null;
        try
        {
            stmt = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=40 or id_catalogo=42 or id_catalogo=43 or id_catalogo=44");
            query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=40 or id_catalogo=42 or id_catalogo=43 or id_catalogo=44");
            while (query.next())
            {
                final int idcatalogo = query.getInt(IDCATALOGO);
                final String desc = query.getString(OPCION);
                CARRERAS.put(idcatalogo, desc);

            }

        }
        finally
        {
            if (stmt != null)
            {
                stmt.close();
            }
            if (query != null)
            {
                query.close();
            }
        }
    }

    private static void llenaHorarios(final Connection conexion) throws SQLException
    {
        Statement stmt = null;
        ResultSet query = null;
        try
        {
            //con = DriverManager.getConnection("jdbc:oracle:thin:@200.38.177.133:1531:EMPLEOQA", "desa1", "desa1");
            stmt = conexion.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            //ResultSet rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=15");
            query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=15");
            while (query.next())
            {
                final int idcatalogo = query.getInt(IDCATALOGO);
                final String desc = query.getString(OPCION);
                HORARIOCATALOGO.put(idcatalogo, desc);

            }

        }
        finally
        {
            if (stmt != null)
            {
                stmt.close();
            }
            if (query != null)
            {
                query.close();
            }
        }
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
    public void addBach()
    {
        CompassInfo.removeIndice("candidato");

        final long tini = System.currentTimeMillis();

        final int blocks = TOTAL / TAMBLOQUE;
        for (int i = 0; i < blocks; i++)
        {
            final int indexini = (i * TAMBLOQUE) + 1;
            final int indexfin = indexini + TAMBLOQUE;
            final List<Candidato> candidatos = createBach(indexini, indexfin);
            try
            {
                candidatoSearch.save(candidatos);
                final long tfin = System.currentTimeMillis();
                final long dif = tfin - tini;
                final double rate = (indexfin - 1) * 1000 / dif;
                LOG.info("---- Total " + (indexfin - 1) + " candidatos tiempo: " + dif + " ms  rate: " + rate + " reg/s---");
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
                LOG.error(e);
            }
        }
        //candidatoSearch.endBatch();
        final long tfin = System.currentTimeMillis();
        final long dif = tfin - tini;
        LOG.info("---- Terminando de agregar " + TOTAL + " candidatos tiempo: " + dif + " ms ---");


    }

    private List<Candidato> createBach(final int ini, final int fin)
    {
        final List<Candidato> createBach = new ArrayList<Candidato>();
        for (int i = ini; i <= fin; i++)
        {
            createBach.add(createCandidato(i));
        }
        return createBach;
    }

    public static Candidato createCandidato(final int indice)
    {
        final Candidato candidato = new Candidato(indice);


        final int edad = random.nextInt(60) + 18;
        candidato.setEdad(edad);
        final int experiencia = getRandomCatalogo(EXPERIENCIA);
        candidato.setExperiencia(experiencia);


        final int horario = getRandomCatalogo(HORARIOCATALOGO);
        candidato.setHorario(horario);

        if (indice % 3 == 0)
        {
            candidato.setSalario(10000.0);
        }
        else if (indice % 3 == 1)
        {
            candidato.setSalario(20000.0);
        }
        else
        {
            candidato.setSalario(30000.0);
        }
        
        if (indice % 3 == 0)
        {
            candidato.setFuente(1);
        }
        else if (indice % 3 == 1)
        {
            candidato.setFuente(2);
        }
        else
        {
            candidato.setFuente(3);
        }
        

        if(indice%2==0)
        {
            candidato.setEstado(1);
            candidato.setMunicpio(1);
        }
        else
        {
            candidato.setEstado(2);
            candidato.setMunicpio(2);
        }
        if(indice%3==0)
        {
            candidato.setDiscapacidad("10110");
        }
        else if(indice%3==0)
        {
            candidato.setDiscapacidad("00110");
        }
        else
        {
            candidato.setDiscapacidad("01100");
        }
        candidato.setIndicardorEstudios(true);
        final int iocupacion = getRandomCatalogo(OCUPACIONES);
        candidato.setOcupacion(iocupacion);

        int carrera = getRandomCatalogo(CARRERAS);
        if (carrera <= 0)
        {

            carrera = getRandomCatalogo(CARRERAS);
        }
        final int grado = getRandomCatalogo(GRADOESTUDIOS);
        final int status = getRandomCatalogo(ESTATUSESTUDIO);
        final InformacionAcademica academica = new InformacionAcademica(carrera, grado, status);

        candidato.getInformacionAcademica().add(academica);

        if (indice % 2 == 0)
        {
            candidato.setDisponibilidad(false);
        }
        else
        {
            candidato.setDisponibilidad(true);
            if (indice % 5 == 0)
            {
                candidato.setDisponibilidadViajarCiudad(false);
            }
            else
            {
                candidato.setDisponibilidadViajarCiudad(true);
            }

        }

        

        int idiomaAdd = getRandomCatalogo(IDIOMAS);
        int dominioAdd = getRandomCatalogo(DOMINIOS);
        Idioma idioma = new Idioma(idiomaAdd, dominioAdd);
        candidato.getIdiomas().add(idioma);

        idiomaAdd = getRandomCatalogo(IDIOMAS);
        dominioAdd = getRandomCatalogo(DOMINIOS);
        idioma = new Idioma(idiomaAdd, dominioAdd);
        candidato.getIdiomas().add(idioma);

        idiomaAdd = getRandomCatalogo(IDIOMAS);
        dominioAdd = getRandomCatalogo(DOMINIOS);
        idioma = new Idioma(idiomaAdd, dominioAdd);
        candidato.getIdiomas().add(idioma);

        final ArrayList<Conocimiento> conocimientos = new ArrayList<Conocimiento>();
        Conocimiento conocimiento = new Conocimiento("conocimiento 1", 2);
        conocimientos.add(conocimiento);
        conocimiento = new Conocimiento("java j2ee", 2);
        conocimientos.add(conocimiento);
        conocimiento = new Conocimiento("[Indica el conocimiento] conocimiento áéíóú.) 3", 3);
        conocimientos.add(conocimiento);
        candidato.setConocimientos(conocimientos);

        final ArrayList<Habilidad> habilidades = new ArrayList<Habilidad>();
        Habilidad habilidad = new Habilidad("habilidad 1", 2);
        habilidades.add(habilidad);
        habilidad = new Habilidad("habilidad 2", 2);
        habilidades.add(habilidad);
        habilidad = new Habilidad("habilidad 3", 2);
        habilidades.add(habilidad);
        candidato.setHabilidades(habilidades);
        return candidato;
    }

    /**
     * Test of add method, of class CandidatoIndex.
     */
    @Test
    //@Ignore
    public void testAdd()
    {
        CompassInfo.removeIndice("candidato");

        final long tini = System.currentTimeMillis();

        for (int i = 1; i <= 23800; i++)
        {
            final Candidato candidato = createCandidato(i);
            try
            {
                candidatoSearch.save(candidato);
                if (i % 200 == 0)
                {
                    final long tfin = System.currentTimeMillis();
                    final long dif = tfin - tini;
                    final double rate = (i) * 1000 / dif;
                    LOG.info("---- Total " + (i) + " candidatos tiempo: " + dif + " ms  rate: " + rate + " reg/s---");
                }
            }
            catch (Exception e)
            {
                LOG.error(e.getMessage());
                Assert.fail(e.getMessage());
            }
        }
        final long tfin = System.currentTimeMillis();
        final long dif = tfin - tini;
        LOG.info("Terminando de agregar candidatos tiempo: " + dif + " ms");






    }

    private static Integer getRandomCatalogo(final Map<Integer, String> catalogo)
    {
        final int index = random.nextInt(catalogo.size());
        return (Integer) catalogo.keySet().toArray()[index];
    }

    @Test
    @Ignore
    public void testGet()
    {

        final long tini = System.currentTimeMillis();




        try
        {
            final Idioma idioma = new Idioma(1);
            final List<Idioma> idiomas = new ArrayList<Idioma>();
            idiomas.add(idioma);
            //Result res = instance.get(null, null, null, new Integer(2), null,new Integer(35), new Integer(40), null, null, null, idiomas, null);
            final Consulta consulta = new Consulta();
            consulta.idiomas.addAll(idiomas);
            final Result res = candidatoSearch.search(consulta, null);
            for (int i = 0; i < res.getCount(); i++)
            {
                final long idcandidato = res.nextElement().id;
                LOG.info("id: " + idcandidato);
            }
            final long tfin = System.currentTimeMillis();
            final long dif = (tfin - tini);
            LOG.info("resultados: " + res.getCount() + " Tiempo: " + dif + " ms testGet");
            res.close();

        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }

    }

    /**
     * Test of remove method, of class CandidatoIndex.
     */
    @Test
    @Ignore
    public void testRemove()
    {
        try
        {
            LOG.info("remove");
            candidatoSearch.remove(1);
            testGet();
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }
}
