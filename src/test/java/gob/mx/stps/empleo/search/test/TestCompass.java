/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search.test;

import gob.mx.stps.empleo.search.CompassInfo;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.miscellaneous.SingleTokenTokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author victor.lorenzana
 */
public class TestCompass
{
    private static final String ADMINISTRADOR_6500_PESOS_2_AÑOS_DE_EXPERI = "Administrador $6500 pesos 2 años de experiencia ";
    
    private static final DecimalFormat CURRENCY = new DecimalFormat("$###,###,###", new DecimalFormatSymbols(new Locale("es", "MX")));
    private static final DecimalFormat CURRENCY2 = new DecimalFormat("$#########", new DecimalFormatSymbols(new Locale("es", "MX")));

    private static final Log LOG = LogFactory.getLog(TestCompass.class);

    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    /*private boolean add(final String data)
    {
        if (!Character.isDigit(data.charAt(0)))
        {
            return true;
        }
        final int pos = data.indexOf('|');
        if (pos == -1)
        {
            return true;
        }
        else
        {
            final String test = data.substring(0, pos);
            try
            {
                Integer.parseInt(test);
            }
            catch (NumberFormatException nfe)
            {
                return true;
            }
        }
        return false;
    }*/

    @Test
    public void test()
    {
        String word = "cartera";
        SingleTokenTokenStream tokens = new SingleTokenTokenStream(new org.apache.lucene.analysis.Token(word, 0, word.length()));
        SnowballFilter filter = new SnowballFilter(tokens, "Spanish");
        try
        {
            String raizword = filter.next().term();
            LOG.info("raiz: " + raizword);
            word = raizword;
            tokens = new SingleTokenTokenStream(new org.apache.lucene.analysis.Token(word, 0, word.length()));
            filter = new SnowballFilter(tokens, "Spanish");
            raizword = filter.next().term();
            LOG.info("raiz: " + raizword);
            word = raizword;
            tokens = new SingleTokenTokenStream(new org.apache.lucene.analysis.Token(word, 0, word.length()));
            filter = new SnowballFilter(tokens, "Spanish");
            raizword = filter.next().term();
            LOG.info("raiz: " + raizword);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());            
            LOG.error(e);
        }
    }

    /*@Test
    public void addData()
    {
        Connection con = null;
        try
        {
            //Class.forName("oracle.jdbc.OracleDriver");
            Class.forName("org.gjt.mm.mysql.Driver");


            //con = DriverManager.getConnection("jdbc:oracle:thin:@200.38.177.133:1531:EMPLEOQA", "desa1", "desa1");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/empleo", "root", "root");


            final FileInputStream input = new FileInputStream(new File("c:\\EMPLEOV2_APP_Oferta_empleo.txt"));
            final DataInputStream datain = new DataInputStream(input);
            final PreparedStatement ptdelete = con.prepareStatement("delete from oferta_empleo");
            ptdelete.executeUpdate();
            ptdelete.close();
            String line = datain.readLine();
            while (line != null)
            {
                String nextline = datain.readLine();
                if (line.length() > 0 && line.toCharArray()[0] != '#')
                {
                    if (nextline != null)
                    {
                        while (nextline.trim().equals(""))
                        {
                            nextline = datain.readLine();
                        }
                        try
                        {
                            while (add(nextline))
                            {
                                line += " " + nextline;
                                nextline = datain.readLine();
                                if (nextline == null)
                                {
                                    break;
                                }
                                else
                                {
                                    while (nextline.trim().equals(""))
                                    {
                                        nextline = datain.readLine();
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            LOG.error(e);
                            LOG.info(line);
                            LOG.info(nextline);
                        }
                    }
                    final StringTokenizer separatorPipe = new StringTokenizer(line, "|");
                    final List<String> _values = new ArrayList<String>();
                    while (separatorPipe.hasMoreTokens())
                    {
                        _values.add(separatorPipe.nextToken());
                    }
                    final String[] values = _values.toArray(new String[_values.size()]);
                    try
                    {
                        final int idoferta = Integer.parseInt(values[0]);
                        final String title = values[2];
                        final int status = Integer.parseInt(values[values.length - 2]);
                        final PreparedStatement consulta = con.prepareStatement("insert into oferta_empleo (id_oferta_empleo,titulo_oferta,estatus) values(?,?,?)");
                        consulta.setInt(1, idoferta);
                        consulta.setString(2, title);
                        consulta.setInt(3, status);
                        consulta.executeUpdate();
                        consulta.close();
                    }
                    catch (Exception e)
                    {
                        LOG.error(e);
                        LOG.info(line);
                        LOG.info(nextline);
                    }
                }
                line = nextline;
            }


        }
        catch (Exception e)
        {                        
            LOG.error(e);
            Assert.fail(e.getMessage());
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
                    LOG.error(e2);
                }
            }
        }
    }*/

    @Test
    public void replace()
    {
        String search = "administración,admon.,admon.";
        search = search.replace(',', ' ');
        search = CompassInfo.replace(search, " " + "admon." + " ", " " + "administrador" + " ");
    }

    @Test
    public void match()
    {
        
        //final String value = ;
        final Pattern pattern = Pattern.compile("\\s+[\\$]?\\d{1,3},?\\d{1,3},?\\d+(\\.\\d+)?\\s+");
        final Matcher matcher = pattern.matcher(ADMINISTRADOR_6500_PESOS_2_AÑOS_DE_EXPERI);

        final StringBuilder content = new StringBuilder();
        int ini = 0;
        while (matcher.find())
        {
            final int start = matcher.start();
            final int end = matcher.end();
            content.append(ADMINISTRADOR_6500_PESOS_2_AÑOS_DE_EXPERI.subSequence(ini, start));
            ini = end;
            final String text = ADMINISTRADOR_6500_PESOS_2_AÑOS_DE_EXPERI.substring(start, end).trim();
            final StringBuilder textToParse = new StringBuilder(text);
            if (!text.isEmpty() && text.toCharArray()[0] != '$')
            {
                textToParse.insert(0, '$');
            }
            try
            {
                final Number number = CURRENCY.parse(textToParse.toString());
                content.append(" ").append(CURRENCY2.format(number)).append(" ");
            }
            catch (Exception e)
            {
                LOG.error(e, e);
                Assert.fail(e.getMessage());
            }
        }

        content.append(ADMINISTRADOR_6500_PESOS_2_AÑOS_DE_EXPERI.substring(ini));

    }
}
