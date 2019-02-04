/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer.detectors;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.tof.hit.ftof.Hit;
import org.jlab.util.dedigitizer.ADegitizer;
import org.jlab.rec.tof.banks.ftof.HitReader;

/**
 *
 * @author ziegler
 */
public class FTOFDedigitized extends ADegitizer {
    public FTOFDedigitized() {
        this.setDetector("FTOF");
        hitRead = new HitReader();
    }
    HitReader hitRead;
    @Override
    public void AppendHits(DataEvent event, PrintWriter pw, int eventNb, float startTime) {
        List<Hit> FTOFHits = new ArrayList<Hit>();
        if(hitRead.get_FTOF1AHits()!=null)
            FTOFHits.addAll(hitRead.get_FTOF1AHits());
        FTOFHits.addAll(hitRead.get_FTOF1BHits());
        FTOFHits.addAll(hitRead.get_FTOF2Hits());
        // 1.1) exit if hit list is empty
        if (FTOFHits != null) {
            for(int i = 0; i < FTOFHits.size(); i++)
                pw.printf("%s\t %d\t %d\t %d\t %d\t %f\t %f\t %f\t\n", this.getDetector(), eventNb, 1, FTOFHits.get(i).get_Panel(), FTOFHits.get(i).get_Paddle(), FTOFHits.get(i).get_t(), FTOFHits.get(i).get_Energy(), startTime);
        }
    }
}
