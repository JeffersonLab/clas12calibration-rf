package org.clas.ccdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.ui.TCanvas;
import org.jlab.jnp.utils.options.OptionParser;

/**
 *
 * @author devita
 */
public class RFtoCCDB {
 
    

    private List<String> offsetsFile = null;
    private double threshold = 0.005;
    private String fontName = "Arial";
    private DataGroup dg = new DataGroup(1,3);

    public RFtoCCDB(List<String> file, double t) {
        this.offsetsFile = file;
        this.threshold = t;
        
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        GStyle.getAxisAttributesZ().setLabelFontSize(14);
        GStyle.getAxisAttributesX().setLabelFontName(this.fontName);
        GStyle.getAxisAttributesY().setLabelFontName(this.fontName);
        GStyle.getAxisAttributesZ().setLabelFontName(this.fontName);
        GStyle.getAxisAttributesX().setTitleFontName(this.fontName);
        GStyle.getAxisAttributesY().setTitleFontName(this.fontName);
        GStyle.getAxisAttributesZ().setTitleFontName(this.fontName);
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(2);

        for(int i=0; i<2; i++) {
            GraphErrors offset = new GraphErrors("RFoffset"+(i+1));
            offset.setTitleX("Run Number");
            offset.setTitleY("Offset (ns)");
            offset.setMarkerColor(3+i);
            offset.setMarkerSize(3);
            dg.addDataSet(offset, 0);
            GraphErrors ccdb = new GraphErrors("RFccdb"+(i+1));
            ccdb.setTitleX("Run Number");
            ccdb.setTitleY("Offset (ns)");
            ccdb.setMarkerColor(33+i);
            ccdb.setMarkerSize(3);
            dg.addDataSet(ccdb, 0);
            GraphErrors delta = new GraphErrors("RFdelta"+(i+1));
            delta.setTitleX("Run Number");
            delta.setTitleY("Delta (ns)");
            delta.setMarkerColor(5+i);
            delta.setMarkerSize(3);
            dg.addDataSet(delta, 1);
            GraphErrors sigma = new GraphErrors("RFsigma"+(i+1));
            sigma.setTitleX("Run Number");
            sigma.setTitleY("Sigma (ns)");
            sigma.setMarkerColor(3+i);
            sigma.setMarkerSize(3);
            dg.addDataSet(sigma, 2);
        }
        System.out.println("\nProcessing file " + this.offsetsFile + " with threshold set to " + this.threshold);
    }

    public void readOffsets() throws IOException {
        
        Map<Integer,RF[]> rfs = new TreeMap<>();
        
        FileReader fileReader = null;
        try {
            for(String f : offsetsFile) {
                fileReader = new FileReader(f);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = null;
                int old = 0;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] cols = line.split("\\s+");
        //            System.out.println(line);
                    if(cols.length==13) {
                        int run   = Integer.parseInt(cols[2].trim());
                        RF[] rf12 = new RF[2];
                        for(int i=0; i<2; i++) {
                            rf12[i] = new RF(Double.parseDouble(cols[3+i*5].trim()),
                                             Double.parseDouble(cols[4+i*5].trim()),
                                             Double.parseDouble(cols[5+i*5].trim()),
                                             Double.parseDouble(cols[6+i*5].trim()));
                        }
                        if(!rf12[0].isValid() || !rf12[1].isValid()) {
                            continue;
                        } 
                        rfs.put(run, rf12);
                        for(int i=0; i<2; i++) {
                            dg.getGraph("RFoffset"+(i+1)).addPoint(run, rf12[i].offset, 0, rf12[i].error);
                            dg.getGraph("RFdelta"+(i+1)).addPoint(run, rf12[i].delta, 0, rf12[i].error);
                            dg.getGraph("RFsigma"+(i+1)).addPoint(run, rf12[i].sigma, 0, rf12[i].error);
                        }
                    }
                }           
                bufferedReader.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RFtoCCDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileReader.close();
            } catch (IOException ex) {
                Logger.getLogger(RFtoCCDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int first=0;        
        int last=0;        
        for(int run : rfs.keySet()) {
            RF[] rf12 = rfs.get(run);
            if(first==0) {
                first = run;
                last  = run;
            }
            else {
                RF[] previous = rfs.get(first);
                if(rf12[0].equals(previous[0], threshold) && rf12[1].equals(previous[1], threshold)) {
                    last = run;
                }
                else {
                    if(previous[0].isNew(threshold) || previous[1].isNew(threshold)) {
                        this.write2CCDB(first, last, previous);
                    }
                    first = run;
                    last  = run;
                }
            }
            for(int i=0; i<2; i++) {
                dg.getGraph("RFccdb"+(i+1)).addPoint(run, rfs.get(first)[i].offset, 0, rfs.get(first)[i].error);            
            }        
        }
        if(first<last)
            this.write2CCDB(first, last, rfs.get(first));
    }
    
    private void write2CCDB(int min, int max, RF[] rf12) {
        FileWriter writer = null;
        try {
            writer = new FileWriter("rf_"+min+".txt");
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            for(int i=0; i<2; i++) {
                bufferedWriter.write(" 1 1 "+(i+1)+" "+rf12[i].offset+" "+rf12[i].sigma+"\n");
            }
            bufferedWriter.close();
            System.out.println("ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add calibration/eb/rf/offset rf_"+min+".txt -r "+min+"-"+max+" #\"offsets from run "+min+"\"");
        } catch (IOException ex) {
            Logger.getLogger(RFtoCCDB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(RFtoCCDB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void plot() {
        TCanvas vanvas = new TCanvas("RF", 800, 600);
        vanvas.getCanvas().draw(dg);
    }

    public class RF {
        private double offset;
        private double sigma;
        private double error;
        private double delta;
        
        public RF(double offset, double error, double delta, double sigma) {
            this.offset = offset;
            this.error = error;
            this.sigma = sigma;
            this.delta = delta;
        }
        
        public boolean isValid() {
            return Math.abs(error)<0.01 && Math.abs(sigma)<0.1;
        }
        
        public boolean isNew(double threshold) {
            return Math.abs(delta)>threshold;
        }
        
        public boolean equals(RF o, double threshold) {
            return Math.abs(this.offset-o.offset)<threshold;
        }
        
        @Override
        public String toString() {
            String s = String.format("Offset=%.3f Error=%.3f Delta=%.3f Sigma=%.3f", offset, error, delta, sigma);
            return s;
        }
    }
    
    
    public static void main(String[] args){
        
    
        OptionParser parser = new OptionParser("RFtoCCDB");
        
        // valid options for event-base analysis
        parser.setRequiresInputList(true);
        parser.addOption("-t", "0.005", "minimum offset variation (ns)");

        parser.parse(args);
        List<String> files = parser.getInputList();
        double threshold = parser.getOption("-t").doubleValue();
        
        RFtoCCDB rf = new RFtoCCDB(files, threshold);
                
        try {
            rf.readOffsets();
            rf.plot();
        } catch (IOException ex) {
            Logger.getLogger(RFtoCCDB.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

