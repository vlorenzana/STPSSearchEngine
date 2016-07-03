/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conjunto de terminos
 * @author victor.lorenzana
 */
public final class SetTerminos
{

    private transient final SWBTermDictionary dictionary = new SWBTermDictionary(true);
    /**
     * Variable de mensajes
     */
    //private static final Log log = LogFactory.getLog(SetTerminos.class);
    /**
     * Lista de términos ambiguos
     */
    private transient final Map<String, List<Term>> ambiguos = Collections.synchronizedMap(new HashMap<String, List<Term>>());
    /**
     * Lista de terminos
     */
    private transient final Set<Term> terms = Collections.synchronizedSet(new HashSet<Term>());

    /**
     * Obtiene el listado de terminos
     * @return Listado de terminos exietnetes
     */
    public Set<Term> getTerms()
    {
        return terms;
    }

    /**
     * Indica si esta vácia la lista de terminos
     * @return True, si la lista es vácia, false en caso contrario
     */
    public boolean isEmpty()
    {
        return terms.isEmpty();
    }

    public boolean addTerm(final String text, final String lemma)
    {
        return add(new Term(text, lemma, 0, text), false, true);
    }

    public boolean addTerm(final String text, final String lemma, final int tid, final String original)
    {
        return add(new Term(text, lemma, tid, original), false, true);
    }

    public boolean addTerm(final Term term)
    {
        return add(term, false, true);
    }

    /**
     * Agrega un termino al listado
     * @param term Termino a agregar
     * @return True, si se pudo agregar, false caso contrario
     */
    public boolean add(final Term term, final boolean variante, final boolean normaliza)
    {
        if (term == null || term.getText() == null || term.getLemma() == null || "".equals(term.getText().trim()) || "".equals(term.getLemma().trim()))
        {
            return false;
        }
        final String original_name = checkTerm(term, normaliza);

        if (variante && addVariante(term))
        {
            return false;
        }
        if (terms.contains(term))
        {
            add(term);
//            else
//            {
//                //log.warn("El termino con texto:" + term.getText() + " lemma:" + term.getLemma() + " id: " + term.getId() + " NO se agregó a la tabla de ambiguedades ya el el lemma y el id esta repetido");
//            }
            return true;
        }
        else
        {
            return addAmbiguo(term, variante, original_name);
        }

    }

    private String checkTerm(final Term term, final boolean normaliza)
    {
        final String original_name = term.getText();
        String text = term.getText();
        if (normaliza)
        {
            text = CompassInfo.normaliza(text);
        }
        term.setText(text);
        return original_name;
    }

    private boolean addAmbiguo(final Term term, final boolean variante, final String original_name)
    {
        final List<Term> terminos = new ArrayList<Term>();
        terminos.add(term);
        dictionary.addTerm(term.getText(), term);
        ambiguos.put(term.getText(), terminos);

        if (variante)
        {
            addVariantes(term, original_name);
        }

        return terms.add(term);
    }

    private void add(final Term term)
    {
        boolean add = false;
        for (Term term2 : this.getTerms(term))
        {
            if (!(term.getLemma().equalsIgnoreCase(term2.getLemma()) && term.getId() == term2.getId()))
            {
                add = true;
                break;
            }

        }
        if (add)
        {
            final List<Term> terminos = ambiguos.get(term.getText());
            terminos.add(term);
        }
    }

    private void addVariantes(final Term term, final String original_name)
    {
        term.varname = original_name;                
        final String original = CompassInfo.normaliza(term.getOriginal());
        final List<Term> terminosOriginal = ambiguos.get(original);
        if (terminosOriginal != null)
        {
            terminosOriginal.add(term);
            ambiguos.put(original, terminosOriginal);
        }
    }

    private boolean addVariante(final Term term)
    {
        if (term.getLemma().equals(Campos.OCUPACION.toString()))
        {
            final String textOcupacion = term.getText();
            final boolean exists = CompassInfo.OCUPACIONNORM.contains(textOcupacion);
            if (exists)
            {
                return true;
            }
        }
        if (term.getLemma().equals(Campos.CARRERA_O_ESPECIALIDAD.toString()))
        {
            final String textCarrera = term.getText();
            final boolean exists = CompassInfo.CARRERASNORM.contains(textCarrera);
            if (exists)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Indica si un terminos tiene ambiguedades
     * @param term Terminos a verificar
     * @return True, si tiene ambiguedades, false caso contrario
     */
    public boolean isAmbiguo(final Term term)
    {
        return ambiguos.get(term.getText()) != null || ambiguos.get(term.getText()).size() > 1;
    }

    /**
     * Regresa la lista de terminos ambiguos
     * @param term Terminos a revisar
     * @return Lista de terminos ambiguos
     */
    public Term[] getTerms(final Term term)
    {
        final List<Term> ambiguos_ = ambiguos.get(term.getText());
        final List<Term> temporal = new ArrayList<Term>();
        if (ambiguos_ != null)
        {
            temporal.addAll(ambiguos_);
            return temporal.toArray(new Term[temporal.size()]);
        }
        return new Term[0];
    }

    public SWBTermDictionary getSWBTermDictionary()
    {
        return dictionary;
    }
}
