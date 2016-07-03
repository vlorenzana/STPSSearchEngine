/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search.test;

import gob.mx.stps.empleo.search.CarrerasOcupacionSimilares;
import gob.mx.stps.empleo.search.Catalogo;
import gob.mx.stps.empleo.search.CompassInfo;
import gob.mx.stps.empleo.search.Term;
import gob.mx.stps.empleo.search.VacanteSearch;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author victor.lorenzana
 */
public class UtilTest
{

    private static final String IDCATALOGO = "id_catalogo_opcion";
    private static final String OPCION = "opcion";
    private static VacanteSearch instance = null;
    private static final Map<Integer, String> GRADOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> CARRERAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> IDIOMAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> DOMINIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> EXPERIENCIA = new HashMap<Integer, String>();
    private static final Map<Integer, String> HORARIO = new HashMap<Integer, String>();
    private static final Map<Integer, String> OCUPACION = new HashMap<Integer, String>();
    private static final Map<Integer, String> STATUS = new HashMap<Integer, String>();
    private static final Log LOG = LogFactory.getLog(UtilTest.class);

    @BeforeClass
    public static void setUpClass()
    {
        final long tini = System.currentTimeMillis();
        final Catalogo catalogo = new Catalogo();



        Connection con = null;
        try
        {
            //Class.forName("oracle.jdbc.OracleDriver");
            Class.forName("org.gjt.mm.mysql.Driver");
            LOG.info("llenado catalogos...");

            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/empleo", "root", "root");
            llenaHorario(con);
            llenaCarreras(con);
            llenaOcupaciones(con);
            llenaIdiomas(con);
            llenaDominios(con);
            llenaStatus(con);
            llenaGrados(con);
            llenaExperiencia(con);
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

        GRADOS.put(1, "SIN INSTRUCCIÓN");
        GRADOS.put(2, "SABER LEER Y ESCRIBIR");
        GRADOS.put(3, "PRIMARIA");
        GRADOS.put(5, "SECUNDARIA/SEC. TÉCNICA");
        GRADOS.put(6, "CARRERA COMERCIAL");
        GRADOS.put(7, "CARRERA TÉCNICA");
        GRADOS.put(8, "PROFESIONAL TÉCNICO");
        GRADOS.put(9, "PREPA O VOCACIONAL");
        GRADOS.put(10, "T. SUPERIOR UNIVERSITARIO");
        GRADOS.put(11, "LICENCIATURA");
        GRADOS.put(12, "MAESTRÍA");
        GRADOS.put(13, "DOCTORADO");

        final long tfin = System.currentTimeMillis();
        final long dif = (tfin - tini);
        LOG.info("Fin de llenado catalogos... tiempo: " + dif + " ms");



        catalogo.carrera_especialidad_catalogo.putAll(CARRERAS);
        catalogo.dominios.putAll(DOMINIOS);
        catalogo.experiencia_catalogo.putAll(EXPERIENCIA);
        catalogo.grado_estudios.putAll(GRADOS);
        catalogo.horario_catalogo.putAll(HORARIO);
        catalogo.idiomas_catalogo.putAll(IDIOMAS);
        catalogo.ocupacion_catalogo.putAll(OCUPACION);
        catalogo.status_estudio_catalogo.putAll(STATUS);

        instance = new VacanteSearch(catalogo);

    }

    private static void llenaExperiencia(final Connection con) throws SQLException
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
            STATUS.put(idcatalogo, desc);

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
            OCUPACION.put(idcatalogo, desc);

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

    private static void llenaHorario(final Connection con) throws SQLException
    {
        //con = DriverManager.getConnection("jdbc:oracle:thin:@200.38.177.133:1531:EMPLEOQA", "desa1", "desa1");
        final Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

        //ResultSet rs = stmt.executeQuery("select opcion,id_catalogo_opcion from empleov2_app.catalogo_opcion where id_catalogo=15");
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

    @Test
    public void cleanTerm()
    {
        final String aves = "aves";        
        final String normalizado=CompassInfo.normaliza(aves);
        Assert.assertEquals(aves, normalizado);        
    }

   

    /*@Test
    public void testEqualsFamLex()
    {
        final String test1 = CompassInfo.steam("dibujo");
        final String test2 = CompassInfo.steam("carteras");
        final boolean equals = test1.equals(test2);
        LOG.info("igual: " + equals + " raiz1: " + test1 + " raiz2: " + test2);

    }*/

    

    @Test
    public void checkCarerras()
    {
        for (CarrerasOcupacionSimilares set : CompassInfo.getCarrerasSimilares())
        {
            for (Integer icarrera : set)
            {
                if (!CompassInfo.CARRERAS.containsKey(icarrera))
                {
                    LOG.info("La carrera: " + icarrera + " no se encontró");
                }
            }
        }
    }

    @Test
    public void chechOcupaciones()
    {
        for (CarrerasOcupacionSimilares set : CompassInfo.getOcupacionSimilares())
        {
            for (Integer iocupacion : set)
            {
                if (!CompassInfo.OCUPACIONES.containsKey(iocupacion))
                {
                    LOG.info("La ocupación: " + iocupacion + " no se encontró");
                }
            }
        }
    }

    @Test
    public void chechTerminos()
    {
        
        for (Set<String> variantes : CompassInfo.getVariantes())
        {
            final Set<String> lvariantes = new HashSet<String>();
            final Set<String> terminos = new HashSet<String>();
            for (String variante : variantes)
            {
                //String orginal_variante = variante;
                final String _variante = CompassInfo.normaliza(variante);
                if (isTermino(_variante, variante))
                {
                    terminos.add(variante);
                }
                else
                {
                    lvariantes.add(variante);
                }
            }
            if (terminos.isEmpty())
            {
                muestraNoTerminos(variantes);
            }
            /*if (lvariantes.isEmpty())
            {
                final StringBuilder content = new StringBuilder();
                for (String variante : variantes)
                {
                    content.append(variante);
                    content.append(",");
                }
                //LOG.info("El conjunto: " + sb + " no tiene variantes asociados");
            }*/
            /*if (terminos.size() > 1)
            {
                final StringBuilder content = new StringBuilder();
                for (String variante : variantes)
                {
                    content.append(variante);
                    content.append(",");
                }
                final StringBuilder sb_terminos = new StringBuilder();
                for (String termino : terminos)
                {
                    sb_terminos.append(termino);
                    sb_terminos.append(",");
                }
                //LOG.info("El conjunto: " + sb + " tiene muchos terminos asociados, terminos encontrados: " + sb_terminos.toString());
            }*/
        }
    }

    private void muestraNoTerminos(final Set<String> variantes)
    {
        final StringBuilder content = new StringBuilder();
        for (String variante : variantes)
        {
            content.append(variante);
            content.append(",");
        }
        LOG.info("El conjunto: " + content + " no tiene terminos asociados");
    }

    private boolean isTermino(final String value, final String original)
    {
        final Term term = instance.getSWBTermDictionary().getTerm(value);
        if (term == null)
        {
            return false;
        }
        else
        {
            String orinor = CompassInfo.cleanTerm(term.getOriginal());
            orinor = CompassInfo.changeCharacters(orinor);
            orinor = CompassInfo.extractParentesis(orinor);
            if (orinor.equals(original))
            {
                return true;
            }
            else
            {
                final Term[] terms = VacanteSearch.getTerms(term);
                if (terms != null && terms.length > 0)
                {
                    for (Term ambiguo : terms)
                    {
                        orinor = CompassInfo.cleanTerm(ambiguo.getOriginal());
                        orinor = CompassInfo.changeCharacters(orinor);
                        orinor = CompassInfo.extractParentesis(orinor);
                        if (orinor.equals(original))
                        {
                            return true;
                        }
                    }
                }
                return false;
            }

        }
    }
}
