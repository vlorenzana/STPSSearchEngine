/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

/**
 * Representa un token, es decir, una palabra y su información asociada.
 * @author Hasdai Pacheco {haxdai@gmail.com}
 */
public final class Token
{

    private transient String tokenText;
    private transient int tokenStart;
    private transient int tokenEnd;
    private transient int tokenSize;

    /**
     * Crea una nueva instancia de Token.
     */
    public Token()
    {
    }

    /**
     * Crea una nueva instancia de Token.
     * @param text texto del token
     * @param start índice del inicio del token en la cadena
     * @param end índice del fin del token en la cadena
     */
    public Token(final String text, final int start, final int end)
    {
        reinit(text, start, end);
    }

    /**
     * Devuelve el texto del token
     * @return Texto del token
     */
    public String getText()
    {
        return tokenText;
    }

    /**
     * Establece el texto del token
     * @param text
     */
    public void setText(final String text)
    {
        tokenText = text;
    }

    /**
     * Obtiene el índice dentro de la cadena donde inicia el token
     * @return Indice dentro de la cadena donde inicia el token
     */
    public int getStart()
    {
        return tokenStart;
    }

    /**
     * Establece el índice dentro de la cadena donde inicia el token
     * @param start índice de inicio del token
     */
    public void setStart(final int start)
    {
        tokenStart = start;
        tokenSize = tokenEnd - tokenStart + 1;
    }

    /**
     * Obtiene el índice dentro de la cadena donde termina el token
     * @return Regresa el indice final del termino reconocido dentro de la cadena
     */
    public int getEnd()
    {
        return tokenEnd;
    }

    /**
     * Establece el índice dentro de la cadena donde termina el token
     * @param end índice de fin del token
     */
    public void setEnd(final int end)
    {
        tokenEnd = end;
        tokenSize = tokenEnd - tokenStart + 1;
    }

    /**
     * Obtiene la longitud del token
     * @return Longitud del token
     */
    public int getSize()
    {
        return tokenSize;
    }

    /**
     * Establece la longitud del token
     * @param size
     */
    public void setSize(final int size)
    {
        tokenSize = size;
    }

    /**
     * Reinicia un token para poder reutilizarlo
     * @param text texto del token
     * @param start índice de inicio del token
     * @param end índice del final del token
     * @return token con nuevos valores
     */
    public Token reinit(final String text, final int start, final int end)
    {
        tokenStart = start;
        tokenEnd = end;
        tokenText = text;
        tokenSize = text.length();

        return this;
    }
}
