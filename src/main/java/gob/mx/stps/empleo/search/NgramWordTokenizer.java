/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Hasdai Pacheco {haxdai@gmail.com}
 */
public final class NgramWordTokenizer
{

    public static final int DEFAULT_MIN_NGRAM_SIZE = 1;
    public static final int DEFAULT_MAX_NGRAM_SIZE = 2;
    private transient String sinStr;
    private transient int minWordsInToken;
    private transient int maxWordsInToken;
    private transient int gramSize;
    private transient int inLen;
    private transient List<Token> chunks;
    private transient int pos = 0;
    private transient boolean started = false;
    private transient final String[] listDelimiters =
    {
        " ", ","
    };
    private transient Reader input;

    public NgramWordTokenizer(final Reader input)
    {
        this(input, DEFAULT_MIN_NGRAM_SIZE, DEFAULT_MAX_NGRAM_SIZE);
    }

    public NgramWordTokenizer(final Reader input, final int minWords, final int maxWords)
    {
        this.input = input;
        if (minWords < 1)
        {
            throw new IllegalArgumentException("minWordsInToken must be greater than zero");
        }

        if (minWords > maxWords)
        {
            throw new IllegalArgumentException("minWordsInToken must not be greater than maxWordsInToken");
        }
        minWordsInToken = minWords;
        maxWordsInToken = maxWords;
    }

    public Token next(final Token reusableToken) throws IOException
    {
        assert reusableToken != null;
        if (!started)
        {
            started = true;
            gramSize = minWordsInToken;
            final char[] chars = new char[1024];
            input.read(chars);
            sinStr = new String(chars).trim();
            chunks = getChunks();
            inLen = chunks.size();
        }

        if (pos + gramSize > inLen)
        {
            pos = 0;
            gramSize++;
            if (gramSize > maxWordsInToken)
            {
                return null;
            }
            if (pos + gramSize > inLen)
            {
                return null;
            }
        }

        final int tokenStart = chunks.get(pos).getStart();
        int tokenEnd = chunks.get(pos).getEnd();
        final StringBuilder tokenString=new StringBuilder();
        for (int i = pos; i < pos + gramSize; i++)
        {
            tokenString.append(chunks.get(i).getText());
            tokenString.append(" ");            
            tokenEnd = chunks.get(i).getEnd();
        }
        
        /*String tokenString = "";
        for (int i = pos; i < pos + gramSize; i++)
        {
            tokenString += chunks.get(i).getText() + " ";
            tokenEnd = chunks.get(i).getEnd();
        }
        tokenString = tokenString.trim();*/
        pos++;

        return reusableToken.reinit(tokenString.toString().trim(), tokenStart, tokenEnd);
    }

    private List<Token> getChunks()
    {
        final List<Token> ret = new ArrayList<Token>();
        String c_inStr = sinStr;

        for (int i = 0; i < listDelimiters.length; i++)
        {
            c_inStr = c_inStr.replaceAll(listDelimiters[i], " " + listDelimiters[i] + " ");
        }

        final List<String> taggeds = Arrays.asList(c_inStr.split("[\\s]+"));
        int ipos = 0;
        for (String s : taggeds)
        {
            ipos = c_inStr.indexOf(s, ipos);
            ret.add(new Token(s, ipos, ipos + s.length() - 1));
            ipos += s.length() + 1;
        }

        return ret;
    }

    /*public void setListDelimiters(String[] delimiters)
    {
    listDelimiters = delimiters;
    }*/
}
