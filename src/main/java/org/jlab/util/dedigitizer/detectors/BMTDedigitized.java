/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer.detectors;

import java.io.PrintWriter;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.cvt.hit.ADCConvertor;
import org.jlab.util.dedigitizer.ADegitizer;

/**
 *
 * @author ziegler
 */
public class BMTDedigitized extends ADegitizer {
    public BMTDedigitized() {
        this.setDetector("BMT");
    }
    ADCConvertor adcConv;
    @Override
    public void AppendHits(DataEvent event, PrintWriter pw, int eventNb, float startTime) {
        if (event.hasBank("BMT::adc") == false) {
          
            return;
        }


        DataBank bankDGTZ = event.getBank("BMT::adc");

        int rows = bankDGTZ.rows();;

        int[] id = new int[rows];
        int[] sector = new int[rows];
        int[] layer = new int[rows];
        int[] component = new int[rows];
        int[] ADC = new int[rows];

        if (event.hasBank("BMT::adc") == true) {
            //bankDGTZ.show();
            for (int i = 0; i < rows; i++) {

                id[i] = i + 1;
                sector[i] = bankDGTZ.getInt("sector", i);
                layer[i] = bankDGTZ.getInt("layer", i);
                component[i] = bankDGTZ.getInt("component", i);
                ADC[i] = bankDGTZ.getInt("ADC", i);

               
                // if the strip is out of range skip
                if (component[i] < 1) {
                    continue;
                }
                
                pw.printf("%s\t %d\t %d\t %d\t %d\t %f\t %f\t %f\t\n", this.getDetector(), eventNb, sector[i], layer[i], component[i], 0.0, adcConv.BMTADCtoDAQ(ADC[i]), startTime);
            }
        }
    }
}
