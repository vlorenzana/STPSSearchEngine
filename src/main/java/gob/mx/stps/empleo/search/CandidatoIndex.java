/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.ArrayList;
import java.util.List;
import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableMetaData;
import org.compass.annotations.SearchableProperty;

/**
 * Candidato indexado
 * @author victor.lorenzana
 */
@Searchable(alias = "candidato")
public final class CandidatoIndex
{

    //private static final Log log = LogFactory.getLog(CandidatoIndex.class);
    /**
     * Constructor vacío, necesario para indexador
     */
    public CandidatoIndex()
    {
    }

    /**
     * Constructor con id de candidato
     * @param idcandidato Id del candidato
     */
    public CandidatoIndex(final long idcandidato)
    {
        this.id = idcandidato;
    }

    /**
     * Constructor para indexar un candidato
     * @param candidato Candidato a indexar     
     */
    public CandidatoIndex(final Candidato candidato)
    {
        this.fuente=candidato.getFuente();
        this.discapacidad=candidato.getDiscapacidad();        
        this.estado = candidato.estado;
        this.municipio = candidato.municipio;
        this.id = candidato.getId();
        this.edad = candidato.getEdad();
        this.salario = candidato.getSalario();
        final String disponibilidad = "a" + candidato.isDisponibilidad();
        value.add(disponibilidad);
        final String dispviajar = "b" + candidato.isDisponibilidadViajarCiudad();
        value.add(dispviajar);
        final String experiencia = "c" + candidato.getExperiencia();
        value.add(experiencia);
        final String horario = "d" + candidato.getHorario();
        value.add(horario);
        final String indicador = "e" + candidato.isIndicardorEstudios();
        value.add(indicador);
        procesaIdiomas(candidato);
        procesaAcademica(candidato);
        procesaConocimientos(candidato);
        procesaHabilidades(candidato);
        indexWords(candidato);
        //analize(temp.toString());
    }

    private void getAcademica(final String text, final Candidato candidato) throws NumberFormatException
    {
        final String data=text.substring(1);        
        final String[] values = data.split("_");
        if (values.length == 3)
        {
            final String scarrera = values[0];
            final int carrera = Integer.parseInt(scarrera);            
            final int grado = Integer.parseInt(values[1].substring(1));
            final String strstatus = values[2].substring(1);            
            final int status = Integer.parseInt(strstatus);
            final InformacionAcademica academica = new InformacionAcademica(carrera, grado, status);
            candidato.getInformacionAcademica().add(academica);
        }

    }

    private void getIdioma(final String text, final Candidato candidato) throws NumberFormatException
    {
        final String data=text.substring(1);        
        final int pos = data.indexOf('_');
        if (pos != -1)
        {
            final String sidioma = data.substring(0, pos);
            final String sdominio = data.substring(pos + 1);
            final Idioma idioma = new Idioma(Integer.parseInt(sidioma), Integer.parseInt(sdominio));
            candidato.getIdiomas().add(idioma);
        }
    }

    private void indexaAcademica(final Candidato candidato, final StringBuilder content)
    {
        for (InformacionAcademica academica : candidato.getInformacionAcademica())
        {
            if (academica.carrera > 0)
            {
                final String carrera = CompassInfo.CARRERAS.get(academica.carrera);
                if (carrera != null)
                {
                    final StringBuilder sbcarrera=new StringBuilder(carrera);
                    cambiaAcentos(sbcarrera);
                    content.append(" ");
                    content.append(sbcarrera.toString());
                    carreras.add(academica.carrera);
                }
            }
        }
    }

    private void indexaConocimiento(final Candidato candidato, final StringBuilder content)
    {
        for (Conocimiento conocimiento : candidato.getConocimientos())
        {
            final String name = conocimiento.name;
            if (name != null)
            {
                content.append(" ");
                content.append(name);
            }
        }
    }

    private void indexaHabilidades(final Candidato candidato, final StringBuilder content)
    {
        for (Habilidad habilidad : candidato.getHabilidades())
        {
            final String name = habilidad.name;
            if (name != null)
            {
                content.append(" ");
                content.append(name);
            }
        }
    }

    private void indexaOcupacion(final Candidato candidato, final StringBuilder content)
    {
        if (candidato.getOcupacion() > 0)
        {
            this.ocupacion = candidato.getOcupacion();
            final String _ocupacion = CompassInfo.OCUPACIONES.get(candidato.getOcupacion());
            if (_ocupacion != null)
            {
                final StringBuilder sbocupacion=new StringBuilder(_ocupacion);
                cambiaAcentos(sbocupacion);
                content.append(" ");
                content.append(sbocupacion.toString());
            }
        }
    }

    private void normalizaPalabras(final StringBuilder content)
    {       
        CompassInfo.replaceAbreviaciones(content);
        CompassInfo.changeBadcharacters(content, false);
        CompassInfo.changeCharacters(content);        
    }

    private void procesaAcademica(final Candidato candidato)
    {
        if (candidato.getInformacionAcademica() != null)
        {
            for (InformacionAcademica informacionAcademica : candidato.getInformacionAcademica())
            {
                final String data = "g" + informacionAcademica.carrera + "_j" + informacionAcademica.grado_estudios + "_k" + informacionAcademica.status;
                value.add(data);
            }
        }
    }

    private void procesaDato(final String text, final Candidato candidato)
    {
        String data = text;
        final char[] chars = data.toCharArray();        
        final char character = chars[0];
        switch (character)
        {
            case 'a':
                data = data.substring(1);
                candidato.setDisponibilidad(Boolean.parseBoolean(data));
                break;
            case 'b':
                data = data.substring(1);
                candidato.setDisponibilidadViajarCiudad(Boolean.parseBoolean(data));
                break;
            case 'c':
                data = data.substring(1);
                candidato.setExperiencia(Integer.parseInt(data));
                break;
            case 'd':
                data = data.substring(1);
                candidato.setHorario(Integer.parseInt(data));
                break;
            case 'e':
                data = data.substring(1);
                candidato.setIndicardorEstudios(Boolean.parseBoolean(data));
                break;
            case 'f':
                getIdioma(data, candidato);
                break;
            case 'g':
                getAcademica(data, candidato);
                break;
            case 'h':
                getConocimientos(data, chars, candidato);
            case 'i':
                getHabilidades(data, chars, candidato);
            default:
                return;
        }
        /*else if(data.startsWith("j"))
        {
        data=data.substring(1);
        candidato.grado_estudios=Integer.parseInt(data);
        }*/

    }

    private void getHabilidades(final String text, final char[] chars, final Candidato candidato)
    {        
        if (chars.length > 2 && chars[1] == '_')
        {
            final String data = text.substring(2);            
            final int pos = data.indexOf('_');
            if (pos != -1)
            {
                final String shabilidad = data.substring(0, pos);
                final int sdominio = Integer.parseInt(data.substring(pos + 1));
                final Habilidad habilidad = new Habilidad(shabilidad, sdominio);
                candidato.getHabilidades().add(habilidad);
            }
        }
    }

    private void procesaIdiomas(final Candidato candidato)
    {
        if (candidato.getIdiomas() != null)
        {
            for (Idioma idioma : candidato.getIdiomas())
            {
                final String data = "f" + idioma.id + "_" + idioma.dominio_id;
                value.add(data);
            }
        }
    }

    private void procesaConocimientos(final Candidato candidato)
    {
        if (candidato.getConocimientos() != null)
        {
            for (Conocimiento conocimiento : candidato.getConocimientos())
            {                
                final StringBuilder name=new StringBuilder(conocimiento.name);
                CompassInfo.changeCharacters(name);
                CompassInfo.replaceAbreviaciones(name);                
                CompassInfo.changeBadcharacters(name, true);                
                CompassInfo.trim(name);                
                for (String token : CompassInfo.tokenizer(name.toString()))
                {

                    final String data = "h_" + token + "_" + conocimiento.experiencia;
                    value.add(data);
                }

            }
        }
    }

    private void procesaHabilidades(final Candidato candidato)
    {
        if (candidato.getHabilidades() != null)
        {
            for (Habilidad habilidad : candidato.getHabilidades())
            {
                final StringBuilder habilidadname = new StringBuilder(habilidad.name);
                CompassInfo.changeCharacters(habilidadname);
                CompassInfo.replaceAbreviaciones(habilidadname);                
                CompassInfo.changeBadcharacters(habilidadname, true);                
                CompassInfo.trim(habilidadname);
                for (String token : CompassInfo.tokenizer(habilidadname.toString()))
                {
                    final String data = "i_" + token + "_" + habilidad.experiencia;
                    value.add(data);
                }


            }
        }
    }

    private void getConocimientos(final String text,final char[] chars, final Candidato candidato)
    {        
        if (chars.length > 2 && chars[1] == '_')
        {
            final String data=text.substring(2);            
            final int pos = data.indexOf('_');
            if (pos != -1)
            {
                final String sconocimiento = data.substring(0, pos);
                final int sdominio = Integer.parseInt(data.substring(pos + 1));
                final Conocimiento conocimiento = new Conocimiento(sconocimiento, sdominio);
                candidato.getConocimientos().add(conocimiento);

            }
        }
    }

    /**
     * Regresa un candidato en base al índice
     * @return Candidato extraído del buscador
     */
    public Candidato get()
    {
        final Candidato candidato = new Candidato();
        candidato.setFuente(fuente);        
        candidato.id = id;
        candidato.estado = estado;
        candidato.municipio = municipio;
        candidato.setDiscapacidad(discapacidad);        
        candidato.setSalario(salario);
        candidato.setEdad(edad);
        candidato.setPalabras(words);
        candidato.setOcupacion(ocupacion);
        for (String data : this.value)
        {
            if (data.length()>1)
            {
                procesaDato(data, candidato);
            }

        }
        return candidato;
    }

    public void indexWords(final Candidato candidato)
    {
        carreras = new ArrayList<Integer>();
        final StringBuilder content = new StringBuilder();
        if (candidato.getPalabras() != null)
        {
            final String _palabras = candidato.getPalabras().replace(':', ' ');
            for (String palabra : CompassInfo.sortedtokenizer(_palabras))
            {
                content.append(" ");
                content.append(palabra);
            }
        }
        indexaAcademica(candidato, content);
        indexaOcupacion(candidato, content);
        indexaConocimiento(candidato, content);
        indexaHabilidades(candidato, content);
        normalizaPalabras(content);


        final List<String> values = CompassInfo.sortedtokenizer(content.toString());

        final StringBuilder contentforvalues = new StringBuilder();
        for (String _value : values)
        {
            if (!"".equals(_value.trim()))
            {

                contentforvalues.append(_value);
                contentforvalues.append(" ");
            }
        }
        words = contentforvalues.toString().trim();
    }

    private void cambiaAcentos(final StringBuilder text)
    {
        CompassInfo.toLowerCase(text);        
        CompassInfo.replace(text,'á', 'a');
        CompassInfo.replace(text,'é', 'e');
        CompassInfo.replace(text,'í', 'i');
        CompassInfo.replace(text,'ó', 'o');
        CompassInfo.replace(text,'ú', 'u');
        CompassInfo.replace(text,'à', 'a');
        CompassInfo.replace(text,'è', 'e');
        CompassInfo.replace(text,'ì', 'i');
        CompassInfo.replace(text,'ò', 'o');
        CompassInfo.replace(text,'ù', 'u');
        CompassInfo.replace(text,'ü', 'u');
        CompassInfo.replace(text,'\r', ' ');
        CompassInfo.replace(text,'\n', ' ');
        CompassInfo.replace(text,',', ' ');
        CompassInfo.replace(text,'[', ' ');
        CompassInfo.replace(text,']', ' ');
        CompassInfo.replace(text,'/', ' ');
        CompassInfo.replace(text,';', ' ');
        CompassInfo.replace(text,':', ' ');
        CompassInfo.replace(text,'-', ' ');
        CompassInfo.replace(text,'(', ' ');
        CompassInfo.replace(text,')', ' ');
        final List<String> values = CompassInfo.sortedtokenizer(text.toString());
        final StringBuilder content = new StringBuilder();
        for (String _value : values)
        {
            content.append(_value);
            content.append(" ");
        }
        text.replace(0, text.length(), content.toString());
        //return content.toString();


    }
    public @SearchableId
    @SearchableMetaData(name = "id")
    /**
     * Id del candidato
     */
    long id = -1;
    public @SearchableProperty(format = "#0000", index = Index.NOT_ANALYZED, name = "EDAD")
    @SearchableMetaData(format = "#0000", index = Index.NOT_ANALYZED, name = "EDAD")
    /**
     * Edad del candidato
     */
    int edad = -1;

    public void setCarreras(final List<Integer> carreras)
    {
        this.carreras = carreras;
    }

    public int getEdad()
    {
        return edad;
    }

    public void setEdad(final int edad)
    {
        this.edad = edad;
    }

    public int getOcupacion()
    {
        return ocupacion;
    }

    public void setOcupacion(final int ocupacion)
    {
        this.ocupacion = ocupacion;
    }

    public double getSalario()
    {
        return salario;
    }

    public void setSalario(final double salario)
    {
        this.salario = salario;
    }

    public List<String> getValue()
    {
        return value;
    }

    public void setValue(final List<String> value)
    {
        this.value = value;
    }

    public String getWords()
    {
        return words;
    }

    public void setWords(final String words)
    {
        this.words = words;
    }
    
    public @SearchableProperty(index = Index.TOKENIZED, name = "ESTADO")
    @SearchableMetaData(index = Index.TOKENIZED, name = "ESTADO")
    /**
     * Estado
     */
    transient int estado;
    public @SearchableProperty(index = Index.TOKENIZED, name = "MUNICIPIO")
    @SearchableMetaData(index = Index.TOKENIZED, name = "MUNICIPIO")
    /**
     * Estado
     */
    transient int municipio;
    
    public @SearchableProperty(index = Index.TOKENIZED, name = "FUENTE")
    @SearchableMetaData(index = Index.TOKENIZED, name = "FUENTE")
    /**
     * FUENTE
     */
    transient int fuente;
    
    public @SearchableProperty(index = Index.TOKENIZED, name = "DISCAPACIDAD")
    @SearchableMetaData(index = Index.TOKENIZED, name = "DISCAPACIDAD")
    String discapacidad;
    
    
    public @SearchableProperty(index = Index.NOT_ANALYZED, name = "VALUE")
    @SearchableMetaData(index = Index.NOT_ANALYZED, name = "VALUE")
    /**
     * Lista de valores a indexar
     */
    List<String> value = new ArrayList<String>();
    public @SearchableProperty(format = "#0000000.00", index = Index.NOT_ANALYZED, name = "SALARIO_PRETENDIDO")
    @SearchableMetaData(format = "#0000000.00", index = Index.NOT_ANALYZED, name = "SALARIO_PRETENDIDO")
            
            
    
    /**
     * Salario pretendido por el candidato
     */
    double salario;
    public @SearchableProperty(index = Index.NOT_ANALYZED, name = "OCUPACIONCANDIDATO")
    @SearchableMetaData(index = Index.NOT_ANALYZED, name = "OCUPACIONCANDIDATO")
    int ocupacion;
    //@SearchableProperty(index = Index.NOT_ANALYZED, name = "lastUpdate")
    //@SearchableMetaData(index = Index.NOT_ANALYZED, name = "lastUpdate")
    //Date lastupdate;
    public @SearchableProperty(index = Index.TOKENIZED, name = "WORDS")
    @SearchableMetaData(index = Index.TOKENIZED, name = "WORDS")
    /**
     * Palabras complementarias a indexar
     */
    String words;
    public @SearchableProperty(index = Index.NOT_ANALYZED, name = "CARRERACANDIDATO")
    @SearchableMetaData(index = Index.NOT_ANALYZED, name = "CARRERACANDIDATO")
    /**
     * Palabras complementarias a indexar
     */
    List<Integer> carreras = new ArrayList<Integer>();

    /**
     * Compara dos objetos
     * @param obj Objeto a comparar
     * @return True si el un CandidatoIndex con el mismo id, false caso contrario
     */
    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final CandidatoIndex other = (CandidatoIndex) obj;
        if (this.id != other.id)
        {
            return false;
        }
        return true;
    }

    /**
     * Regresa el  hashCode basado en el id del candidato
     * @return HashCode basado en el id del candidato
     */
    @Override
    public int hashCode()
    {        
        return (97 * 7 + (int) (this.id ^ (this.id >>> 32)));        
    }

    /**
     * Regresa el id del candidato
     * @return Id del candidato
     */
    @Override
    public String toString()
    {
        return String.valueOf(id);
    }

    /**
     * Regresa el id del candidato
     * @return Id del candidato
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * Modifica el Id del candidato
     * @param idcandidato Id del candidato
     */
    public void setId(final int idcandidato)
    {
        if (idcandidato <= 0)
        {
            throw new IllegalArgumentException("El identificador de un candidato no puede ser cero o negativo");
        }
        this.id = idcandidato;
    }

    public List<Integer> getCarreras()
    {
        return carreras;
    }
    
}
