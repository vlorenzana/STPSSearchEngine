/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author victor.lorenzana
 */
public class Prueba
{

    private static final Map<Integer, String> GRADOESTUDIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> CARRERAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> IDIOMAS = new HashMap<Integer, String>();
    private static final Map<Integer, String> DOMINIOS = new HashMap<Integer, String>();
    private static final Map<Integer, String> EXPERIENCIA = new HashMap<Integer, String>();
    private static final Map<Integer, String> HORARIOCATALOGO = new HashMap<Integer, String>();
    private static final Map<Integer, String> ESTATUSESTUDIO = new HashMap<Integer, String>();
    private static final Map<Integer, String> OCUPACIONES = new HashMap<Integer, String>();
    private static SecureRandom random = new SecureRandom();
    private static final int ini = 1;
    private static final int fin = 30000;
    private static final int TOTAL = 10000;
    private static final int TAMBLOQUE = 1000;
    private static CandidatoSearch candidatoSearch = null;

    private static Integer getRandomCatalogo(final Map<Integer, String> catalogo)
    {
        final int index = random.nextInt(catalogo.size());
        return (Integer) catalogo.keySet().toArray()[index];
    }

    private static List<Candidato> createBach(final int ini, final int fin)
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
        Conocimiento conocimiento = new Conocimiento("conocimiento 1 Se considera microempresa a la organización de negocios que cuenta con 1 a 10 trabajadores en su lista de integrantes. Independientemente del giro de la empresa (comercio, industria o servicios), las microempresas representan uno de los motores de crecimiento económico más importantes del país. De acuerdo con estadísticas del INEGI, las microempresas:", 2);
        conocimientos.add(conocimiento);
        conocimiento = new Conocimiento("java j2ee", 2);
        conocimientos.add(conocimiento);
        conocimiento = new Conocimiento("[Indica el conocimiento] conocimiento áéíóú.) 3 Una vez concluida tu carrera universitaria es posible que se te presenten las alternativas de iniciar tu vida laboral o de buscar el apoyo necesario para continuar tu preparación. Ambas opciones ofrecen grandes ventajas para tu formación y consolidación profesional, por lo que resulta necesario que definas tus prioridades y persigas tus metas. En este canal se presenta un panorama general de las alternativas disponibles para ti ahora que has concluido tu carrera universitaria.", 3);
        conocimientos.add(conocimiento);
        candidato.setConocimientos(conocimientos);

        final ArrayList<Habilidad> habilidades = new ArrayList<Habilidad>();
        Habilidad habilidad = new Habilidad("habilidad 1 Reclutamiento y selección a todos los niveles aplicación de pruebas psicometricas conocimiento de las diferentes fuentes de reclutamiento entrevista inicial y profunda atracción de talento para puestos operativos, administrativos y gerencias armado de expedientes acostumbrado a trabajar bajo presión", 2);
        habilidades.add(habilidad);
        habilidad = new Habilidad("Técnico de prueba para importante empresa electrónica su función de trabajo es comprobar el funcionamiento del producto para su mayor calidad habilidad 2", 2);
        habilidades.add(habilidad);
        habilidad = new Habilidad("habilidad 3 En esta sección, se ofrece información relacionada con las carreras profesionales, desde una breve descripción de los planes de estudio hasta estadísticas específicas por cada una de las áreas de especialización.No olvides que la información es tu principal herramienta para realizar una elección adecuada y aprovechar al máximo tus capacidades.", 2);
        habilidades.add(habilidad);
        candidato.setHabilidades(habilidades);
        return candidato;
    }

    public static void main(final String[] args)
    {
        System.setProperty("org.compass.needle.coherence.localcache","C:/indicescoherence");
        System.setProperty("tangosol.coherence.distributed.localstorage", "true");
        for (int i = 1; i <= 1000; i++)
        {
            GRADOESTUDIOS.put(i, "grado " + i);
            CARRERAS.put(i, "carrera " + i);
            IDIOMAS.put(i, "idiomas " + i);
            DOMINIOS.put(i, "dominios " + i);
            EXPERIENCIA.put(i, "experiencia1 " + i);
            HORARIOCATALOGO.put(i, "horario " + i);
            ESTATUSESTUDIO.put(i, "estatus " + i);
            OCUPACIONES.put(i, "ocupacion " + i);
        }
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
        //CompassInfo.removeIndice("candidato");
        long icandidatos = candidatoSearch.getCount();
        System.out.println("Candidatos registrados: " + icandidatos);
        final List<Candidato> createBach = new ArrayList<Candidato>();
        String op = null;
        if (args.length >= 1)
        {
            op = args[0];
        }
        if ("block".equals(op) && args.length >= 2)
        {
            int block = Integer.parseInt(args[1]);
            System.out.println("Iniciando guardado de "+block+" candidatos");
            for (int i = ini; i <= block; i++)
            {
                createBach.add(createCandidato(i));
            }
            try
            {
                long tini = System.currentTimeMillis();
                candidatoSearch.save(createBach);
                final long tfin = System.currentTimeMillis();
                final long dif = tfin - tini;
                //if (dif > 5000)
                {
                    System.out.println("Tiempo guardando " + block + " candidatos " + dif + " ms");
                    return;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace(System.out);
            }
        }

        for (int i = ini; i <= fin; i++)
        {
            createBach.add(createCandidato(i));
        }
        if ("all".equals(op))
        {
            try
            {
                long tini = System.currentTimeMillis();
                candidatoSearch.save(createBach);
                final long tfin = System.currentTimeMillis();
                final long dif = tfin - tini;
                //if (dif > 5000)
                {
                    System.out.println("Tiempo guardando candidatos " + dif + " ms");
                    return;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace(System.out);
            }
        }
        if ("help".equals(op))
        {
            //C:\Users\victor.lorenzana\proys\stps\STPSSearchIndexer\dist>java -cp STPSSearchIndexer.jar;lib/*.jar gob.mx.stps.empleo.search.Prueba
            System.out.println("Uso C:/Users/victor.lorenzana/proys/stps/STPSSearchIndexer/dist>java -Dtangosol.coherence.cacheconfig=../cache_coherence.xml -cp STPSSearchIndexer.jar;lib/*.jar gob.mx.stps.empleo.search.Prueba [one | seach all]");
            return;
        }
        if ("search".equals(op))
        {
            while (true)
            {
                Result result = null;
                try
                {

                    result = candidatoSearch.search("habilidad");

                    while (result.hasNextElement())
                    {
                        result.nextElement();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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
        if ("one".equals(op) || args.length == 0)
        {
            for (int j = 0; j < 1; j++)
            {
                System.out.println("Iteración " + j + " de guardado de " + createBach.size() + " candidatos");
                for (Candidato candidato : createBach)
                {

                    try
                    {
                        long tini = System.currentTimeMillis();
                        candidatoSearch.save(candidato);
                        final long tfin =
                                System.currentTimeMillis();
                        final long dif = tfin - tini;
                        //if (dif > 5000) 
                        {
                            System.out.println("Tiempo guardando candidato " + candidato.getId() + " " + dif + " ms");
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace(System.out);
                    }

                }
            }
        }



    }
}
