/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.util.dedigitizer;
import org.jlab.io.base.DataEvent;
import java.io.PrintWriter;

/**
 *
 * @author ziegler
 */
public interface IDedigitize {
    
    public void AppendHits(DataEvent event, PrintWriter pw, int eventNb, float startTime);
    
}
