/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Etiquetador de entidades basado en diccionario plano.
 * @author Hasdai Pacheco {haxdai@gmail.com}
 */
public final class EntityTagger
{

    private transient final SWBTermDictionary lex;
    private transient final List<String> lemmas;
    private transient final List<Term> matchedTerms;
    private transient final List<Term> preprocessTerms;
    private transient final List<Term> postprocessTerms;

    /**
     * Crea una nueva instancia del etiquetador de entidades.
     * @param dict diccionario de palabras a usar.
     */
    public EntityTagger(final SWBTermDictionary dict)
    {
        lemmas = new ArrayList<String>();
        matchedTerms = new ArrayList<Term>();
        preprocessTerms = new ArrayList<Term>();
        postprocessTerms = new ArrayList<Term>();
        preprocessTerms.add(new Term("\\s\\d+\\s", " [$/NUM] "));  // numeros enterios
        preprocessTerms.add(new Term("\\s+\\d+(\\.\\d+)\\s+", " [$/DEC] "));  // numeros decimales        
        preprocessTerms.add(new Term("\\s+[\\$]?\\d+(\\.\\d+)?\\s+", " [$/CURR] ")); //moneda
        preprocessTerms.add(new Term("\\s+[\\$]?\\d{1,3},?\\d{1,3},?\\d+(\\.\\d+)?\\s+", " [$/MON] ")); //moneda
        //preprocessTerms.add(new Term("\\s+[\\$]?\\d+(\\.\\d+)?", " [$:NUM] "));
        lex = dict;
    }

    /**
     * Etiqueta la cadena de entrada con la información del diccionario.
     * @param input cadena a etiquetar.
     * @return Cadena etiquetada
     * @throws IOException Error en caso que no poder leer la cadena
     */
    public String tagEntities(final String input) throws IOException
    {
        final List<Token> tokens = new ArrayList<Token>();
        lemmas.clear();
        matchedTerms.clear();
        //String str = " " + input.trim() + " ";
        final StringBuilder str=new StringBuilder(input.trim());
        str.insert(0, " ");
        str.append(" ");
        getTokens(input, tokens);
        tagTokens(tokens, str);
        doPreprocessTerms(str);
        doPostprocessTerms(str);

        final String words[] = str.toString().split(" ");
        for (int i = 0; i < words.length; i++)
        {
            final String string = words[i].trim();
            if (string.isEmpty() || string.toCharArray()[0]=='[')
            {                
                if (!string.isEmpty())
                {
                    final String strTerm = string.replaceAll("\\[", "").replaceAll("]", "");                    
                    final String[] parts = strTerm.split("/");
                    final String termText = parts[0].replaceAll("_", " ");
                    final Term term = lex.getTerm(termText);
                    if (term == null)
                    {
                        if (parts.length > 0)
                        {
                            final Term newterm = new Term(termText, parts[1]);
                            matchedTerms.add(newterm);
                        }
                        
                        
                    }
                    else
                    {
                        final Term newterm = new Term(termText, parts[1], term.getId(), term.getOriginal());
                        matchedTerms.add(newterm);
                    }

                }
            }
            else
            {
                final Term newterm = new Term(string, null, -1, string);
                matchedTerms.add(newterm);
            }
        }
        //return str.trim().replaceAll("_", " ");
        return str.toString().trim();
    }

    private void tagTokens(final List<Token> tokens,final StringBuilder str)
    {
        for (int i = tokens.size() - 1; i >= 0; i--)
        {
            final String tokenstring = tokens.get(i).getText().replace('.', ' ').trim();            
            final Term term = lex.getTerm(tokenstring);
            if (term != null && term.getText().equalsIgnoreCase(tokenstring))
            {
                tagEntity(tokenstring, str, term);
            }
        }
        
    }
    
    /*private String tagTokens(final List<Token> tokens, String str)
    {
        for (int i = tokens.size() - 1; i >= 0; i--)
        {
            final String tokenstring = tokens.get(i).getText().replace('.', ' ').trim();            
            final Term term = lex.getTerm(tokenstring);
            if (term != null && term.getText().equalsIgnoreCase(tokenstring))
            {
                str = tagEntity(tokenstring, str, term);
            }
        }
        return str;
    }*/

    private void getTokens(final String input, final List<Token> tokens) throws IOException
    {
        final NgramWordTokenizer ngwt = new NgramWordTokenizer(new StringReader(input), 1, lex.getMaxWordLength());
        Token token = ngwt.next(new Token());        
        while (token != null)
        {
            tokens.add(new Token(token.getText(), token.getStart(), token.getEnd()));
            token = ngwt.next(token);
        }
    }

    private void doPostprocessTerms(final StringBuilder str)
    {
        for (Term t : postprocessTerms)
        {
            final StringBuffer sbf = new StringBuffer();
            replacePattern(str, t.getText(), true, t.getLemma(), sbf);
            //str = sbf.toString();
        }
        //return str;
    }

    private void doPreprocessTerms(final StringBuilder str)
    {
        for (Term t : preprocessTerms)
        {
            final StringBuffer sbf = new StringBuffer();
            replacePattern(str, t.getText(), true, t.getLemma(), sbf);
            //str = sbf.toString();
        }
        //return str;
    }
    
    

    private void replace(final StringBuilder text, final String match, final String strreplace)
    {
        int pos = text.indexOf(match);
        while (pos != -1)
        {
            text.replace(pos, pos + match.length(), strreplace);
            pos = text.indexOf(match, pos + match.length());
        }
    }
    public void tagEntity(final String replace, final StringBuilder text, final Term term)
    {
        int pos = text.indexOf(replace);
        while (pos != -1)
        {
            if (isTaged(pos, text.toString()))
            {
                final int posfin = text.toString().indexOf(']', pos);
                pos = text.indexOf(replace, posfin);
            }
            else
            {
                final String ini = text.substring(0, pos);
                final String token = text.substring(pos, pos + replace.length());
                final String tokeToReplace = "[" + token.replace(' ', '_') + "/" + term.getLemma() + "] ";
                //final String end = text.substring(pos + replace.length() + 1);
                //text = ini + " " + tokeToReplace + end;
                replace(text, token, tokeToReplace);
                pos = text.indexOf(replace, ini.length() + tokeToReplace.length());
            }
        }
        //return text;

    }
    /*public String tagEntity(final String replace, String text, final Term term)
    {
        int pos = text.indexOf(replace);
        while (pos != -1)
        {
            if (isTaged(pos, text))
            {
                final int posfin = text.indexOf(']', pos);
                pos = text.indexOf(replace, posfin);
            }
            else
            {
                final String ini = text.substring(0, pos);
                final String token = text.substring(pos, pos + replace.length());
                final String tokeToReplace = "[" + token.replace(' ', '_') + "/" + term.getLemma() + "] ";
                final String end = text.substring(pos + replace.length() + 1);
                text = ini + " " + tokeToReplace + end;
                pos = text.indexOf(replace, ini.length() + tokeToReplace.length());
            }
        }
        return text;

    }*/

    private boolean isTaged(final int pos, final String str)
    {
        
        final int posCierra = str.indexOf(']', pos);
        final int posAbre = str.indexOf('[', pos);
        boolean tagged=false;
        if (posCierra == -1 && posAbre == -1) // no encuentra nada
        {
            tagged=false;
        }
        else if (posCierra >= 0 && posAbre == -1) // adelante sólo hay un ]
        {
            tagged=true;
        }
        else if (posCierra == -1 && posAbre >= 0) // adelante sólo hay un [
        {
            tagged=false;
        }
        else   // adelante sólo hay un ] y un [
        {
            tagged= posCierra < posAbre;
        }
        return tagged;
    }

    /**
     * Obtiene la lista de palabras reconocidas.
     * @return Lista de palabras reconocidas.
     */
    public List<String> getMatchedLemmas()
    {
        return lemmas;
    }

    /**
     * Obtiene un iterador a la lista de términos reconocidos.
     * @return Lista de términos reconocidos.
     */
    public Iterator<Term> listMatchedTerms()
    {
        return matchedTerms.iterator();
    }

    /**
     * Reemplaza en la cadena de entrada un patrón con un texto predefinido.
     * @param text cadena sobre la cual se hará el reemplazo.
     * @param pattern patrón a reconocer en la cadena.
     * @param asRegex indica si el patrón se buscará como una expresión regular.
     * @param replacement cadena de reemplazo para el patrón.
     * @param res buffer en el que se almacenará la cadena procesada.
     * @return True, si encontro un patron que remplazar, false caso contrario
     */
    private boolean replacePattern(final StringBuilder text, final String pattern, final boolean asRegex, final String replacement, final StringBuffer res)
    {
        if (text == null || text.toString().trim().equals(""))
        {
            return false;
        }
        boolean found = false;
        final String regex = pattern;
        final Matcher matcher = Pattern.compile(regex, 0).matcher(text);

        while (matcher.find())
        {
            found = true;
            int start=matcher.start();
            int end=matcher.end();
            final String group = matcher.group();
            String repl = replacement;

            if (asRegex)
            {
                repl = replacement.replace("$", group.trim());
                matcher.appendReplacement(res, Matcher.quoteReplacement(repl));
                text.replace(start, end, repl);
            }
            else
            {
                matcher.appendReplacement(res, repl);
                text.replace(start, end, repl);
            }
        }
        //matcher.appendTail(res);
        
        
        return found;
    }

    /**
     * Agrega un término a la lista de preprocesamiento.
     * @param term término.
     */
    public void addPreprocessTerm(final Term term)
    {
        preprocessTerms.add(term);
    }

    /**
     * Elimina un término de la lista de preprocesamiento.
     * @param term término.
     * @return Termino eliminado
     */
    public Term removePreprocessTerm(final Term term)
    {
        return preprocessTerms.remove(preprocessTerms.indexOf(term));
    }

    /**
     * Agrega un término a la lista de postprocesamiento.
     * @param term término
     */
    public void addPostPtocessTerm(final Term term)
    {
        postprocessTerms.add(term);
    }

    /**
     * Elimina un término de la lista de postprocesamiento.
     * @param term término
     * @return El término eliminado
     */
    public Term removePostprocessTerm(final Term term)
    {
        return postprocessTerms.remove(postprocessTerms.indexOf(term));
    }
}
