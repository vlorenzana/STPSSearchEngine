/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search.test;

import gob.mx.stps.empleo.search.BusquedaException;
import gob.mx.stps.empleo.search.Catalogo;
import gob.mx.stps.empleo.search.CompassInfo;
import gob.mx.stps.empleo.search.Vacante;
import gob.mx.stps.empleo.search.VacanteSearch;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
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
public class AddVacantesTest
{

    public static final int BATCH_SIZE = 1000;
    public static final int BLOQUE = 200;
    private static final String CONOCIMIENTO = "conocimiento";
    private static final String CONOCIMIENTO_1 = "conocimiento 1";
    private static final String CONOC_1 = "conoc 1";
    private static final String IDCATALOGO = "id_catalogo_opcion";
    private static final Log LOG = LogFactory.getLog(AddVacantesTest.class);
    private static final String OPCION = "opcion";
    private static final String PHP = "php";
    private static final String VACANTE = "vacante";
    private static Map<Integer, String> ofertas = new HashMap<Integer, String>();
    private static Random ramdom = new Random();
    private static VacanteSearch instance = null;
    private static final Map<Integer, String> GRADOSESTUDIO = new HashMap<Integer, String>();
    private static final Map<Integer, String> CARRERAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> IDIOMAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> DOMINIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> EXPERIENCIA = new HashMap<Integer, String>();
    private static final Map<Integer, String> HORARIO = new HashMap<Integer, String>();
    private static final Map<Integer, String> OCUPACIONES = new HashMap<Integer, String>();
    private static final Map<Integer, String> STATUSSTUDIO = new HashMap<Integer, String>();
    private static final Map<Integer, String> ESTADOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> MUNICPIOS = new HashMap<Integer, String>();

    @BeforeClass
    public static void setUpClass()
    {
        final long tini = System.currentTimeMillis();
        agregaTitulosOferta();
        catalogoEstadosMunicpios();


        try
        {
            Class.forName("org.gjt.mm.mysql.Driver");
        }
        catch (Exception e)
        {
            LOG.error(e);
        }
        LOG.info("llenado catalogos...");
        Connection con = null;
        try
        {

            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/empleo", "root", "root");
            agregaHorarios(con);
            agregaCarreras(con);
            agregaOcupaciones(con);
            agregaIdiomas(con);
            agregaDominios(con);
            agregaStatus(con);
            agregaGrados(con);
            agregaExperiencia(con);
            agregaTitulos(con);
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
        catalogo.experiencia_catalogo.putAll(EXPERIENCIA);
        catalogo.grado_estudios.putAll(GRADOSESTUDIO);
        catalogo.horario_catalogo.putAll(HORARIO);
        catalogo.idiomas_catalogo.putAll(IDIOMAS);
        catalogo.ocupacion_catalogo.putAll(OCUPACIONES);
        catalogo.status_estudio_catalogo.putAll(STATUSSTUDIO);
        catalogo.estados_catalogo.putAll(ESTADOS);

        instance = new VacanteSearch(catalogo);
        //instance.indexaCarreras();
        //instance.indexaOcupaciones();

        try
        {
            Thread.sleep(1000); // tiempo para carga de catalogos //40 segundos
        }
        catch (Exception e)
        {
            LOG.error("Error", e);
        }
    }

    private static void agregaTitulos(final Connection con) throws SQLException
    {

        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        final ResultSet query = stmt.executeQuery("select * from oferta_empleo");
        while (query.next())
        {
            final int idcatalogo = query.getInt("id_oferta_empleo");
            final String desc = query.getString("titulo_oferta");
            ofertas.put(idcatalogo, desc);

        }
        query.close();

        stmt.close();
    }

    private static void agregaExperiencia(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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

    private static void agregaGrados(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=8");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=8");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            GRADOSESTUDIO.put(idcatalogo, desc);

        }
        query.close();

        stmt.close();
    }

    private static void agregaStatus(final Connection con) throws SQLException
    {
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        //rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=10");
        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=10");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            STATUSSTUDIO.put(idcatalogo, desc);

        }
        query.close();
        stmt.close();
    }

    private static void agregaDominios(final Connection con) throws SQLException
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

    private static void agregaIdiomas(final Connection con) throws SQLException
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

    private static void agregaOcupaciones(final Connection con) throws SQLException
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

    private static void agregaCarreras(final Connection con) throws SQLException
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

    private static void agregaHorarios(final Connection con) throws SQLException
    {
        //con = DriverManager.getConnection("jdbc:oracle:thin:@200.38.177.133:1531:EMPLEOQA", "desa1", "desa1");

        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);


        final ResultSet query = stmt.executeQuery("select opcion,id_catalogo_opcion from catalogo_opcion where id_catalogo=15");
        while (query.next())
        {
            final int idcatalogo = query.getInt(IDCATALOGO);
            final String desc = query.getString(OPCION);
            HORARIO.put(idcatalogo, desc);

        }
        query.close();
        stmt.close();
    }

    private static void agregaTitulosOferta()
    {
        ofertas.put(1, "auxiliar de oficina");
        ofertas.put(2, "diseñador gráfico");
        ofertas.put(3, "médico");
        ofertas.put(4, "ingeniero civil");
        ofertas.put(5, "auxiliar");
        ofertas.put(6, "chofer");
        ofertas.put(7, "Operador de calderas");
        ofertas.put(8, "capturistas");
    }

    private static void catalogoEstadosMunicpios()
    {
        for (int i = 1; i <= 32; i++)
        {
            ESTADOS.put(i, "Aguascalientes");
        }

        for (int i = 1; i <= 1000; i++)
        {
            MUNICPIOS.put(i, "Municpio");
        }
    }

    /*
     * @AfterClass public static void tearDownClass() throws Exception {
    }
     */

    /*
     * @Before public void setUp() {
    }
     */

    /*
     * @After public void tearDown() {
    }
     */
    @Test
    //@Ignore
    public void addBach()
    {
        CompassInfo.removeIndice(VACANTE);

        final long tini = System.currentTimeMillis();
        final int total = 10000;
        final int bach = 100;
        final int blocks = total / bach;



        for (int i = 0; i < blocks; i++)
        {
            final int indexini = (i * bach) + 1;
            final int indexfin = indexini + bach;
            final List<Vacante> vacantes = createBach(indexini, indexfin);


            try
            {
                instance.save(vacantes);
                final long tfin = System.currentTimeMillis();
                final long dif = tfin - tini;
                final double rate = (indexfin - 1) * 1000 / dif;
                LOG.info("---- Total " + (indexfin - 1) + " vacante tiempo: " + dif + " ms  rate: " + rate + " reg/s---");
            }
            catch (Exception e)
            {
                LOG.error(e);
                Assert.fail(e.getMessage());
            }
        }
        final long tfin = System.currentTimeMillis();
        final long dif = tfin - tini;
        LOG.info("---- Terminando de agregar " + total + " candidatos tiempo: " + dif + " ms ---");


    }

    private List<Vacante> creaBloque(final int ini)
    {
        final List<Vacante> vacantes = new ArrayList<Vacante>();
        for (int i = 0; i < BLOQUE; i++)
        {
            final int idVacante = ini + i;
            final Vacante vacante = creaVacante(idVacante);
            vacantes.add(vacante);
        }
        return vacantes;

    }

    private Vacante creaVacante(final int idVacante)
    {
        final Vacante vacante = new Vacante(idVacante);
        final int index = ramdom.nextInt(ofertas.size()) + 1;
        final Integer idvacante = idVacante;
        final String titulo = ofertas.get(index);
        LOG.info("id: " + idvacante + " titulo: " + titulo);
        vacante.setTitulo(titulo);
        vacante.ocupacion = (Integer) OCUPACIONES.keySet().toArray()[idVacante - 1];
        final int estado = getRandomCatalogo(ESTADOS);
        vacante.estado = estado;
        final int municipio = getRandomCatalogo(MUNICPIOS);
        vacante.municipio = municipio;
        for (int j = 0; j < 2; j++)
        {
            final int carrera = getRandomCatalogo(CARRERAS);
            vacante.carreras.add(carrera);
        }
        final List<String> conocimientos = new ArrayList<String>();
        if (idVacante % 2 == 0)
        {

            conocimientos.add(CONOCIMIENTO_1);
            conocimientos.add("java j2ee computacion");
            conocimientos.add("conocimiento 3 áéíó(.");
        }
        else
        {
            conocimientos.add(CONOC_1);
            conocimientos.add(PHP);
            conocimientos.add(CONOCIMIENTO);

        }
        vacante.setConocimientos(conocimientos);
        return vacante;
    }

    private List<Vacante> creaBloqueVacanteCarreras(final int ini)
    {
        final List<Vacante> vacantes = new ArrayList<Vacante>();
        for (int i = 0; i < BLOQUE; i++)
        {
            final int idVacante = i + ini;
            final Vacante vacante = creaVacanteCarreras(idVacante);
            vacantes.add(vacante);
        }
        return vacantes;
    }

    private Vacante creaVacanteCarreras(final int idVacante)
    {
        final Vacante vacante = new Vacante(idVacante);
        final int index = ramdom.nextInt(ofertas.size()) + 1;
        final Integer idvacante = idVacante;
        final String titulo = ofertas.get(index);
        LOG.info("id: " + idvacante + " titulo: " + titulo);
        vacante.setTitulo(titulo);
        vacante.ocupacion = getRandomCatalogo(OCUPACIONES);
        final int estado = getRandomCatalogo(ESTADOS);
        vacante.estado = estado;
        final int municipio = getRandomCatalogo(MUNICPIOS);
        vacante.municipio = municipio;
        vacante.carreras.add((Integer) CARRERAS.keySet().toArray()[idVacante - 1]);
        if (idVacante % 2 == 0)
        {
            final List<String> conocimientos = new ArrayList<String>();
            conocimientos.add(CONOCIMIENTO_1);
            conocimientos.add("java j2ee computacion");
            conocimientos.add("conocimiento 3 áéíó(. ASP.net");
            vacante.setConocimientos(conocimientos);
        }
        else
        {
            final List<String> conocimientos = new ArrayList<String>();
            conocimientos.add(CONOC_1);
            conocimientos.add(PHP);
            conocimientos.add(CONOCIMIENTO);
            vacante.setConocimientos(conocimientos);
        }
        return vacante;
    }

    private List<Vacante> creaBloqueVacanteTitulo(final Integer ini)
    {
        final List<Vacante> vacantes = new ArrayList<Vacante>();
        for (int i = 0; i < BLOQUE; i++)
        {
            final int idVacante = ini + i;
            final Vacante vacante = createVacanteTitulo(idVacante);
            vacantes.add(vacante);
        }
        return vacantes;
    }

    private Vacante createVacanteTitulo(final int idVacante)
    {
        final Vacante vacante = new Vacante(idVacante);
        //Integer id = i;
        final String titulo = ofertas.get(idVacante);
        //LOG.info("id: " + id + " titulo: " + titulo);
        vacante.setTitulo(titulo);
        vacante.ocupacion = getRandomCatalogo(OCUPACIONES);
        final int estado = getRandomCatalogo(ESTADOS);
        vacante.estado = estado;
        final int municipio = getRandomCatalogo(MUNICPIOS);
        vacante.municipio = municipio;
        vacante.carreras.add(getRandomCatalogo(CARRERAS));
        if (idVacante % 2 == 0)
        {
            final List<String> conocimientos = new ArrayList<String>();
            conocimientos.add(CONOCIMIENTO_1);
            conocimientos.add("java j2ee computacion");
            conocimientos.add("conocimiento 3 áéíó(. ASP.net");
            vacante.setConocimientos(conocimientos);
        }
        else
        {
            final List<String> conocimientos = new ArrayList<String>();
            conocimientos.add(CONOC_1);
            conocimientos.add(PHP);
            conocimientos.add(CONOCIMIENTO);
            vacante.setConocimientos(conocimientos);
        }
        return vacante;
    }

    private List<Vacante> createBach(final int ini, final int fin)
    {
        final List<Vacante> createBach = new ArrayList<Vacante>();
        for (int i = ini; i <= fin; i++)
        {
            createBach.add(createVacante(i));
        }
        return createBach;
    }
    private static void creaRangos(Vacante v)
    {
        int edad_desde=-1;
        int edad_hasta=-1;
        
        Random r=new Random();
        
        if(r.nextInt(100)%10!=0)
        {
            edad_desde=r.nextInt(100)+10;
            edad_hasta=edad_desde+r.nextInt(50)+1;
        }        
        v.setEdad_de(edad_desde);
        v.setEdad_hasta(edad_hasta);        
    }
    public static Vacante createVacante(final int idvacante)
    {
        final Vacante vacante = new Vacante(idvacante);

        //int index = r.nextInt(ofertas.size());
//        Integer id = (Integer) ofertas.keySet().toArray()[index];
//        String titulo = ofertas.get(id);

        if (idvacante % 2 == 0)
        {
            vacante.setTitulo("profesor de ing.");
        }
        else
        {
            vacante.setTitulo("profesor de ingles");
        }
        //LOG.info("titulo: " + titulo);
        if (idvacante % 2 == 0)
        {
            vacante.setTituloVacante("a");
        }
        else
        {
            vacante.setTituloVacante("b");
        }
        
        creaRangos(vacante);        
        /*if (idvacante % 5 == 0)
        {
            vacante.setEdad_de(20);
            vacante.setEdad_hasta(30);
        }
        else if (idvacante % 5 == 1)
        {
            vacante.setEdad_de(25);
            vacante.setEdad_hasta(50);
        }
        else if (idvacante % 5 == 2)
        {
            vacante.setEdad_de(45);
            vacante.setEdad_hasta(65);
        }
        else if (idvacante % 5 == 3)
        {
            vacante.setEdad_de(30);
            vacante.setEdad_hasta(100);
        }
        else
        {
            vacante.setEdad_de(-1);
            vacante.setEdad_hasta(-1);
        }*/
        

        if (idvacante % 2 == 0)
        {
            vacante.setEmpresa("empresa a");
        }
        else
        {
            vacante.setEmpresa("empresa b");
        }

        if (idvacante % 2 == 0)
        {
            vacante.setUbicacion("ubicacion a");
        }
        else
        {
            vacante.setUbicacion("ubicacion b");
        }

        if (idvacante % 2 == 0)
        {
            vacante.setSalario(1000);
        }
        else
        {
            vacante.setSalario(1500);
        }


        
        if (idvacante % 3 == 0)
        {
            vacante.setFuente(1);
        }
        else if (idvacante % 3 == 1)
        {
            vacante.setFuente(2);
        }
        else
        {
            vacante.setFuente(11);
        }
        
        if (idvacante % 6 == 0)
        {
            vacante.setDiscapacidad("11000");
        }
        else if (idvacante % 6 == 1)
        {
            vacante.setDiscapacidad("11111");
        }
        else if (idvacante % 6 == 2)
        {
            vacante.setDiscapacidad("10000");
        }
        else if (idvacante % 6 == 3)
        {
            vacante.setDiscapacidad("01000");
        }
        else if (idvacante % 6 == 4)
        {
            vacante.setDiscapacidad("00000");
        }
        else
        {
            vacante.setDiscapacidad("00111");
        }

        Calendar cal = Calendar.getInstance();
        if (idvacante % 2 == 0)
        {
            vacante.setFecha(cal.getTime());
        }
        else
        {
            cal.add(Calendar.YEAR, -1);
            vacante.setFecha(cal.getTime());
        }




        vacante.ocupacion = getRandomCatalogo(OCUPACIONES);

        if (idvacante % 10 == 0)
        {
            vacante.ocupacion = 732101;
        }

        final int estado = getRandomCatalogo(ESTADOS);
        vacante.estado = estado;


        final int municipio = getRandomCatalogo(MUNICPIOS);
        vacante.municipio = municipio;



        for (int j = 0; j < 10; j++)
        {
            final int carrera = getRandomCatalogo(CARRERAS);
            vacante.carreras.add(carrera);
        }







        if (idvacante % 2 == 0)
        {
            final List<String> conocimientos = new ArrayList<String>();
            conocimientos.add(CONOCIMIENTO_1);
            conocimientos.add("java j2ee computacion php");
            conocimientos.add("conocimiento 3 áéíó(.");
            conocimientos.add("AUTOCAD ASP.NET [hola]");

            vacante.setConocimientos(conocimientos);
        }
        else
        {
            final List<String> conocimientos = new ArrayList<String>();
            conocimientos.add(CONOC_1);
            conocimientos.add(PHP);
            conocimientos.add(CONOCIMIENTO);
            vacante.setConocimientos(conocimientos);
        }
        return vacante;
    }

    @Test
    public void createVacanteTodasOcupaciones()
    {
        CompassInfo.removeIndice(VACANTE);
        LOG.info("vacantes a generar: " + OCUPACIONES.size());

        for (int i = 1; i <= OCUPACIONES.size(); i += BLOQUE)
        {
            final List<Vacante> vacantes = creaBloque(i);
            try
            {
                instance.create(vacantes);
            }
            catch (Exception e)
            {
                LOG.error(e);
                Assert.fail(e.getMessage());
            }

        }

    }

    @Test
    public void createVacanteTodasCarreras()
    {
        CompassInfo.removeIndice(VACANTE);
        LOG.info("vacantes a generar: " + CARRERAS.size());
        for (int i = 1; i <= CARRERAS.size(); i++)
        {
            final List<Vacante> vacantes = creaBloqueVacanteCarreras(i);
            try
            {
                instance.save(vacantes);
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void createVacanteTodosTitulo()
    {

        CompassInfo.removeIndice(VACANTE);
        final long tini = System.currentTimeMillis();
        LOG.info("vacantes a generar: " + ofertas.size());

        for (Integer i : ofertas.keySet())
        {
            final List<Vacante> vacantes = creaBloqueVacanteTitulo(i);
            try
            {
                instance.create(vacantes);
            }
            catch (Exception e)
            {
                Assert.fail(e.getMessage());
                LOG.error(e);
            }

        }


        final long tfin = System.currentTimeMillis();
        final long dif = tfin - tini;
        LOG.info("---- Terminando de agregar " + ofertas.keySet().size() + " vacantes tiempo: " + dif + " ms ---");


    }

    @Test
    //@Ignore
    public void add()
    {
        CompassInfo.removeIndice(VACANTE);
        System.setProperty("gob.mx.stps.empleo.search.ServerBroadcast", "localhost");
        final long tini = System.currentTimeMillis();



        //int max = 1000000;
        //final int max = 100;
        for (int i = 1; i <= 100; i++)
        {
            try
            {

                final Vacante vacante = createVacante(i);


                instance.save(vacante);

                LOG.info("Indexando vacante " + vacante.id);
            }
            catch (BusquedaException be)
            {
                Assert.fail(be.getMessage());
                LOG.error(be);
            }


        }




        final long tfin = System.currentTimeMillis();
        final long dif = tfin - tini;
        LOG.info("Terminando de agregar vacantes tiempo: " + dif + " ms");



    }

    private static Integer getRandomCatalogo(final Map<Integer, String> catalogo)
    {

        final int index = ramdom.nextInt(catalogo.size());
        return (Integer) catalogo.keySet().toArray()[index];
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
            instance.remove(1);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }


    }
}
