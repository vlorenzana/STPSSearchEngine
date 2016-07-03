package gob.mx.stps.empleo.search;

import java.util.Locale;

/**
 * Clase que representa un término, es decir, un conjunto de palabras con algún
 * significado y un lema o raíz que puede ser usado como etiqueta.
 * @author Hasdai Pacheco {haxdai@gmail.com}
 */
public final class Term
{
    private static final Locale LOCALE = new Locale("es", "MX");
    private transient final String termLemma;
    private transient String ttext;
    private transient boolean regexp;
    private transient int termId;
    private transient String original;
    public transient String varname;
    //private transient String natural;

    /**
     * Crea una nueva instancia de un Término.
     * @param text texto (forma léxica) del término.
     * @param lemma raíz de la palabra o lemma.
     */
    public Term(final String text, final String lemma)
    {
        termLemma = lemma;
        ttext = text;
        if (ttext != null)
        {
            ttext = ttext.trim();
        }
    }

    public void setText(final String text)
    {
        this.ttext = text;
    }

    public String getOriginal()
    {
        return original;
    }

    /**
     * Crea una nueva instancia de un Término.
     * @param text texto (forma léxica) del término.
     * @param lemma raíz de la palabra o lemma.
     * @param tid identificador único del término.
     */
    public Term(final String text, final String lemma, final int tid, final String original)
    {
        this.original = original.toLowerCase(LOCALE);
        termLemma = lemma;
        ttext = text;
        if (ttext != null)
        {
            ttext = ttext.trim();
        }
        termId = tid;
    }

    /**
     * Crea una nueva instancia de un Término.
     * @param text texto (forma léxica) del término.
     * @param lemma raíz de la palabra o lemma.
     * @param regexp indica si el texto del término será usado como expresión regular.
     */
    public Term(final String text, final String lemma, final boolean regexp)
    {
        termLemma = lemma;
        ttext = text;
        if (ttext != null)
        {
            ttext = ttext.trim();
        }
        this.regexp = regexp;
    }

    /**
     * Crea una nueva instancia de un Término.
     * @param text texto (forma léxica) del término.
     * @param lemma raíz de la palabra o lemma.
     * @param regexp indica si el texto del término será usado como expresión regular.
     * @param tid identificador único del término.
     */
    public Term(final String text, final String lemma, final boolean regexp, final int tid)
    {
        termLemma = lemma;
        ttext = text;
        if (ttext != null)
        {
            ttext = ttext.trim();
        }
        this.regexp = regexp;
        termId = tid;
    }

    /**
     * Obtiene la raíz o lemma del término.
     * @return Cadena que representa el tipo de termino
     */
    public String getLemma()
    {
        return termLemma;
    }

    /**
     * Obtiene el texto (forma léxica) del texto
     * @return Obtiene el texto reconocido
     */
    public String getText()
    {
        return ttext;
    }

    /**
     * Indica si el término será tratado como una expresión regular.
     * @return True is el termino es una expresion regular
     */
    public boolean isRegexp()
    {
        return regexp;
    }

    /**
     * Establece el identificador del término.
     * @param termId identificador del término.
     */
    public void setId(final int termId)
    {
        this.termId = termId;
    }

    /**
     * Obtiene el identificador del término.
     * @return Id del termino
     */
    public int getId()
    {
        return termId;
    }

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
        final Term other = (Term) obj;
        if ((this.ttext == null) ? (other.ttext != null) : !this.ttext.equals(other.ttext))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 23 * hash + (this.ttext == null ? 0 : this.ttext.hashCode());
        return hash;
    }

    @Override
    public String toString()
    {
        return "Term{" + "ttext=" + ttext + '}';
    }
}