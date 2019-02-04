/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer.detectors;

import java.io.PrintWriter;
import java.util.List;
import org.jlab.io.base.DataEvent;
import org.jlab.rec.tof.hit.ctof.Hit;
import org.jlab.util.dedigitizer.ADegitizer;
import org.jlab.rec.tof.banks.ctof.HitReader;

/**
 *
 * @author ziegler
 */
public class CTOFDedigitized extends ADegitizer {
    public CTOFDedigitized() {
        this.setDetector("CTOF");
        hitRead = new HitReader();
    }
    HitReader hitRead;
    @Override
    public void AppendHits(DataEvent event, PrintWriter pw, int eventNb, float startTime) {
        List<Hit> CTOFHits = hitRead.get_CTOFHits();

        // 1.1) exit if hit list is empty
        if (CTOFHits != null) {
            for(int i = 0; i < CTOFHits.size(); i++)
                pw.printf("%s\t %d\t %d\t %d\t %d\t %f\t %f\t %f\t\n", this.getDetector(), eventNb, 1, CTOFHits.get(i).get_Panel(), CTOFHits.get(i).get_Paddle(), CTOFHits.get(i).get_t(), CTOFHits.get(i).get_Energy(), startTime);
        }
    }
}
