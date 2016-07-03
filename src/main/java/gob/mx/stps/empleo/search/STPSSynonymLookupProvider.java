/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gob.mx.stps.empleo.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.engine.analyzer.synonym.SynonymLookupProvider;

/**
 * Clase que provee sinónimos de palabras
 * @author victor.lorenzana
 */
public final class STPSSynonymLookupProvider implements SynonymLookupProvider
{

    /**
     * Variable de mensajes
     */
    private static final Log LOG = LogFactory.getLog(STPSSynonymLookupProvider.class);

    /**
     * Constructor por defecto
     */
    public STPSSynonymLookupProvider()
    {
        LOG.info("Iniciando STPSSynonymLookupProvider");
    }

    /**
     * Función que regresa una lista de sinonimos de una palabra
     * @param word Palabra a buscar
     * @return Lista de sinonimos
     */
    @Override
    public String[] lookupSynonyms(final String word)
    {

        final Set<String> lookupSynonyms = new HashSet<String>();
        final String[] sinonimos = CompassInfo.getSinonimos(word);


        if (sinonimos != null && sinonimos.length > 0)
        {
            final List<String> lsinonimos = Arrays.asList(sinonimos);
            lookupSynonyms.addAll(lsinonimos);
        }
        Set<String> abrev = CompassInfo.ABREVIACIONESNOM.get(word);
        if (abrev != null)
        {
            final Set<String> temp = new HashSet<String>();
            for (String _abrev : abrev)
            {
                temp.add(_abrev.concat("."));                
            }
            abrev=temp;
        }

        if (abrev != null)
        {
            lookupSynonyms.addAll(abrev);
        }
        /*String[] sinonimos_ref = CompassInfo.getSinonimosReferenciales(word);
        if (sinonimos_ref != null && sinonimos_ref.length > 0)
        {
            List<String> lsinonimos = Arrays.asList(sinonimos_ref);
            lookupSynonyms.addAll(lsinonimos);
        }*/
        return lookupSynonyms.toArray(new String[lookupSynonyms.size()]);
    }

    @Override
    public void configure(final CompassSettings settings) throws CompassException
    {
        LOG.info("Leyendo settings STPSSynonymLookupProvider");        
    }

    
}
