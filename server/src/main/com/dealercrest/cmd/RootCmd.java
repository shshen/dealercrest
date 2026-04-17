package com.dealercrest.cmd;

import com.dealercrest.cli.Command;
import com.dealercrest.cli.HelpPrinter;
import com.dealercrest.cli.Include;
import com.dealercrest.cli.Option;
import com.dealercrest.cli.RunContext;

@Command(name = "#", desc = "App Launcher", mixins = { HelpMixin.class }, options = {
        @Option(shortName = "v", longName = "version", desc = "Show client version information", type = boolean.class) })
@Include({ ServerCmd.class })
public class RootCmd {

    public void execute(RunContext config) {
        if (config.getBool("version")) {
            System.out.println("version is: 2026.1.0");
        } else {
            HelpPrinter help = new HelpPrinter();
            help.print(getClass(), config.getPath());
        }
    }

}
