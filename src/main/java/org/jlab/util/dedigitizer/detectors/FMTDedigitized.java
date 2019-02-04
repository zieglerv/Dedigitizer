/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer.detectors;

import java.io.PrintWriter;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.util.dedigitizer.ADegitizer;

/**
 *
 * @author ziegler
 */
public class FMTDedigitized extends ADegitizer {
    public FMTDedigitized() {
        this.setDetector("FMT");
    }
   
    @Override
    public void AppendHits(DataEvent event, PrintWriter pw, int eventNb, float startTime) {
        if (event.hasBank("FMT::adc") == false) {
          
            return;
        }

        DataBank bankDGTZ = event.getBank("FMT::adc");

        int rows = bankDGTZ.rows();;

        int[] sector = new int[rows];
        int[] layer = new int[rows];
        int[] component = new int[rows];
        float [] ADC = new float[rows];

        if (event.hasBank("FMT::adc") == true) {
            //bankDGTZ.show();
            for (int i = 0; i < rows; i++) {

                sector[i] = bankDGTZ.getInt("sector", i);
                layer[i] = bankDGTZ.getInt("layer", i);
                component[i] = bankDGTZ.getInt("component", i);
                ADC[i] = (float) bankDGTZ.getInt("ADC", i);

               
                // if the strip is out of range skip
                if (component[i] < 1) {
                    continue;
                }
                
                pw.printf("%s\t %d\t %d\t %d\t %d\t %f\t %f\t %f\t\n", this.getDetector(), eventNb, sector[i], layer[i], component[i], 0.0, ADC[i], startTime);
            }
        }
    }
}
