package org.geoserver.kml.decorator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.styling.SLD;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.expression.Expression;

import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public class PlacemarkNameDecoratorFactory implements KmlDecoratorFactory {

    @Override
    public KmlDecorator getDecorator(Class<? extends Feature> featureClass,
            KmlEncodingContext context) {
        if (Placemark.class.isAssignableFrom(featureClass) && context.isDescriptionEnabled()) {
            return new PlacemarkNameDecorator();
        } else {
            return null;
        }
    }

    static class PlacemarkNameDecorator implements KmlDecorator {
        static final Logger LOGGER = Logging.getLogger(PlacemarkNameDecorator.class);
        
        @Override
        public Feature decorate(Feature feature, KmlEncodingContext context) {
            Placemark pm = (Placemark) feature;

            // try with the template
            SimpleFeature sf = context.getCurrentFeature();
            String title = null;
            try {
                title = context.getTemplate().title(sf);
            } catch(IOException e) {
                String msg = "Error occured processing 'title' template.";
                LOGGER.log(Level.WARNING, msg, e);
            }

            // if we got nothing, set the title to the ID, but also try the text symbolizers
            if (title == null || "".equals(title)) {
                title = sf.getID();
                StringBuffer label = new StringBuffer();

                for (Symbolizer sym : context.getCurrentSymbolizers()) {
                    if (sym instanceof TextSymbolizer) {
                        Expression e = SLD.textLabel((TextSymbolizer) sym);
                        String value = e.evaluate(feature, String.class);

                        if ((value != null) && !"".equals(value.trim())) {
                            label.append(value);
                        }
                    }
                }

                if (label.length() > 0) {
                    title = label.toString();
                }
            }

            pm.setName(title);
            return pm;
        }

    }

}
