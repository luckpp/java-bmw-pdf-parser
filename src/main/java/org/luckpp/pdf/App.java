package org.luckpp.pdf;

import java.io.IOException;

public class App 
{
    public static final String SRC = "H:\\Alfons\\";

    public static void main(String[] args)  {
        try {
            BmwParser.processDirecory(SRC);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
