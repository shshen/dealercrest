package com.dealercrest;

import com.dealercrest.cli.ShellExecutor;
import com.dealercrest.cmd.RootCmd;

public class DealerCliMain {
    
    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("DealerCrestMain");

        ShellExecutor shell = new ShellExecutor();
        shell.run(new RootCmd(), args);
    }
    
}
