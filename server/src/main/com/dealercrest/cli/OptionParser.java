package com.dealercrest.cli;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public final class OptionParser {

    public List<Option> parse(Command cmd) {
        List<Option> options = new ArrayList<>();

        // 1. Add unique options from the command itself
        Option[] optionArray = cmd.options();
        if ( optionArray!= null ) {
            for(Option o: optionArray) {
                options.add(o);
            }
        }

        try {
            // 2. Add options from each Mixin class
            for (Class<? extends Annotation> mixinClass : cmd.mixins()) {
                // Since it's an annotation class, we look at its declared methods
                // specifically the 'value()' method which holds our @Option[] array
                // Annotation mixinInstance = mixinClass.getAnnotation(mixinClass); 
                
                // Note: In Java, to get the default value of an annotation's member 
                // without an instance on a field, you use reflection on the method:
                Option[] mixinOptions = (Option[]) mixinClass
                    .getMethod("value")
                    .getDefaultValue();

                if (mixinOptions != null) {
                    for(Option o: mixinOptions) {
                        options.add(o);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return options;
    }

}

