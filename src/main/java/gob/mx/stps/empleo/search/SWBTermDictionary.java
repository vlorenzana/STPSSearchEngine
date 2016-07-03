/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Diccionario de términos. Almacena un mapa términos para fines de etiquetado.
 * @author Hasdai Pacheco {haxdai@gmail.com}
 */
public final class SWBTermDictionary
{

    private static final Locale LOCALE = new Locale("es", "MX");
    private transient final Map<String, Term> terms = Collections.synchronizedMap(new HashMap<String, Term>());
    private int maxwordlen;
    private transient final boolean icase;

    /**
     * Crea una nueva instancia del diccionario de términos.
     * @param ignoreCase indica si se ignorará si las palabras están en
     * mayúsculas o minúsculas.
     */
    public SWBTermDictionary(final boolean ignoreCase)
    {
        maxwordlen = 0;
        icase = ignoreCase;
    }

    public boolean isIcase()
    {
        return icase;
    }

    public Map<String, Term> getTerms()
    {
        return terms;
    }

    public int getMaxwordlen()
    {
        return maxwordlen;
    }

    public void setMaxwordlen(final int maxwordlen)
    {
        this.maxwordlen = maxwordlen;
    }

    /**
     * Agrega un término existente al diccionario.
     * @param tkey palabra que servirá como llave del término en el diccionario.
     * @param term término a agregar al diccionario.
     */
    public void addTerm(final String tkey, final Term term)
    {
        if (tkey == null || term == null || term.getText() == null || "".equals(tkey.trim()) || "".equals(term.getText().trim()))
        {
            return;
        }
        terms.put(tkey, term);
        if (term.getText().length() > maxwordlen)
        {
            maxwordlen = term.getText().length();
        }
    }

    /**
     * Crea un término nuevo y lo agrega al
     * diccionario.
     * @param termText texto (forma léxica) del término.
     * @param termLemma lemma o raíz del término.
     */
    public void addTerm(final String termText, final String termLemma)
    {
        if (termText == null || termLemma == null || "".equals(termText.trim()) || "".equals(termLemma.trim()))
        {
            return;
        }
        String termToInsert = termText;
        if (icase)
        {
            termToInsert = termToInsert.toLowerCase(LOCALE);
        }
        final Term term = new Term(termToInsert, termLemma);
        terms.put(termToInsert, term);
        if (termToInsert.length() > maxwordlen)
        {
            maxwordlen = termToInsert.length();
        }
    }

    /**
     * Crea un término nuevo y lo agrega al diccionario.
     * @param termText texto (forma léxica) del término.
     * @param termLemma lemma o raíz del término.
     * @param tid Identificador del término.
     */
    public void addTerm(final String termText, final String termLemma, final int tid, final String value)
    {
        if (termText == null || termLemma == null || "".equals(termText.trim()) || "".equals(termLemma.trim()))
        {
            return;
        }
        String stringTerm = termText;
        if (icase)
        {
            stringTerm = stringTerm.toLowerCase(LOCALE);
        }
        final Term term = new Term(stringTerm, termLemma, tid, value);
        terms.put(stringTerm, term);
        if (stringTerm.length() > maxwordlen)
        {
            maxwordlen = stringTerm.length();
        }
    }

    /**
     * Obtiene un término del diccionario.
     * @param termText llave del término a obtener.
     * @return Termino contrado
     */
    public Term getTerm(final String termText)
    {
        String stringTerm = termText;
        if (icase)
        {
            stringTerm = stringTerm.toLowerCase(LOCALE);
        }
        return terms.get(stringTerm);
    }

    /**
     * Ontiene la longitud del término más largo en el diccionario.
     * @return Longitud del término más largo en el diccionario.
     */
    public int getMaxWordLength()
    {
        return maxwordlen;
    }
}
