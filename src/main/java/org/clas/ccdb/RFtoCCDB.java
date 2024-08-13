package org.clas.ccdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.ui.TCanvas;
import org.jlab.jnp.utils.options.OptionParser;

/**
 *
 * @author devita
 */
public class RFtoCCDB {
 
    

    private List<String> offsetsFile = null;
    private static double THRESHOLD = 0.005;
    private static double MAXSIGMA = 0.1;
    private static boolean UPDATE = true;
    private static boolean SKIP = true;
    private static final String FONTNAME = "Arial";

    public RFtoCCDB(List<String> file, double t, double s, boolean update, boolean skip) {
        this.offsetsFile = file;
        THRESHOLD = t;
        MAXSIGMA = s;
        UPDATE = update;
        SKIP = skip;

        System.out.println("\nProcessing file " + this.offsetsFile + " with:\n" +
                "\t - threshold set to " + THRESHOLD + " ns\n"+
                "\t - max sigma set to " + MAXSIGMA + " ns");
    }

    public Map<Integer,RFentry> readOffsets() throws IOException {
        
        Map<Integer,RFentry> rfs = new TreeMap<>();
        
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
                        RFentry rfEntry = new RFentry(run);
                        for(int i=0; i<2; i++) {
                            rfEntry.setRF(i+1,Double.parseDouble(cols[3+i*5].trim()),
                                              Double.parseDouble(cols[4+i*5].trim()),
                                              Double.parseDouble(cols[5+i*5].trim()),
                                              Double.parseDouble(cols[6+i*5].trim()));
                        }
                        rfs.put(run, rfEntry);
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

        return rfs;
    }

    public void findRanges(Map<Integer,RFentry> rfs) {
        // set range lower end
        for(int i=0; i<2; i++) {
            int id = i+1;
            RFentry previous = null;
            for(int run : rfs.keySet()) {
                RFentry rfEntry = rfs.get(run);
                if(rfEntry.getRF(id).isValid()) {
                    if(previous==null) {
                        previous = rfEntry;
                    }
                    if(!rfEntry.getRF(id).equals(previous.getRF(id), THRESHOLD)) {
                        previous = rfEntry;
                    }
                    rfEntry.setRmin(previous.getRun());
                }
            }
        }

        // set range upper limit
        RFentry previous = null;
        int last = 0;
        for(int run : rfs.keySet()) {
            RFentry rfEntry = rfs.get(run);
            last = run;
            if(rfEntry.getRun()==rfEntry.getRmin()) {
                if(previous!=null) {
                    previous.setRmax(rfEntry.getRun()-1);
                }
                previous = rfEntry;
            }
        }
        if(previous!=null) previous.setRmax(last);
    }

    public void printCCDBCommands(Map<Integer,RFentry> rfs) {
        
        for(int run : rfs.keySet()) {
            RFentry rfEntry = rfs.get(run);
            if(rfEntry.getRun()==rfEntry.getRmin()) {
                for(int r=rfEntry.getRmin(); r<=rfEntry.getRmax(); r++) {
                    if(rfs.containsKey(r)) {
                        RFentry rEntry = rfs.get(r);
                        if((rEntry.getRF(1).isValid() && rEntry.getRF(1).isNew(THRESHOLD)) || 
                           (rEntry.getRF(2).isValid() && rEntry.getRF(2).isNew(THRESHOLD)) || 
                            UPDATE) {
                            rfEntry.write2CCDB();
                            break;
                        }
                    }
                }
            }
        }

    }

    public DataGroup plotRanges(Map<Integer,RFentry> rfs) {
           
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        GStyle.getAxisAttributesZ().setLabelFontSize(14);
        GStyle.getAxisAttributesX().setLabelFontName(FONTNAME);
        GStyle.getAxisAttributesY().setLabelFontName(FONTNAME);
        GStyle.getAxisAttributesZ().setLabelFontName(FONTNAME);
        GStyle.getAxisAttributesX().setTitleFontName(FONTNAME);
        GStyle.getAxisAttributesY().setTitleFontName(FONTNAME);
        GStyle.getAxisAttributesZ().setTitleFontName(FONTNAME);
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(2);

        DataGroup dg = new DataGroup(1,3);
        for(int i=0; i<2; i++) {
            GraphErrors offset = new GraphErrors("RFoffset"+(i+1));
            offset.setTitleX("Run Number");
            offset.setTitleY("Offset (ns)");
            offset.setMarkerColor(3+i);
            offset.setMarkerSize(4);
            dg.addDataSet(offset, 0);
            GraphErrors ccdb = new GraphErrors("RFccdb"+(i+1));
            ccdb.setTitleX("Run Number");
            ccdb.setTitleY("Offset (ns)");
            ccdb.setMarkerColor(1);
            ccdb.setMarkerSize(1);
            dg.addDataSet(ccdb, 0);
            GraphErrors delta = new GraphErrors("RFdelta"+(i+1));
            delta.setTitleX("Run Number");
            delta.setTitleY("(Offset - CCDB) (ns)");
            delta.setMarkerColor(5+i);
            delta.setMarkerSize(4);
            dg.addDataSet(delta, 1);
            GraphErrors done = new GraphErrors("RFdone"+(i+1));
            done.setTitleX("Run Number");
            done.setTitleY("Offset (ns)");
            done.setMarkerColor(1);
            done.setMarkerSize(1);
            dg.addDataSet(done, 1);
            GraphErrors sigma = new GraphErrors("RFsigma"+(i+1));
            sigma.setTitleX("Run Number");
            sigma.setTitleY("#sigma (ns)");
            sigma.setMarkerColor(3+i);
            sigma.setMarkerSize(4);
            dg.addDataSet(sigma, 2);
        }
        
        for(int run : rfs.keySet()) {
            RFentry rfEntry = rfs.get(run);
            for(int i=0; i<2; i++) {
                int id = i+1;
                RF rfi = rfEntry.getRF(id);
                if(rfi.isValid() || !SKIP) {
                    dg.getGraph("RFoffset"+id).addPoint(run, rfi.offset, 0, rfi.error);
                    dg.getGraph("RFdelta"+id).addPoint(run, rfi.delta, 0, rfi.error);
                    dg.getGraph("RFsigma"+id).addPoint(run, rfi.sigma, 0, rfi.error);
                    RFentry rfRef = rfs.get(rfEntry.getRmin());
                    dg.getGraph("RFccdb"+id).addPoint(run, rfRef.getRF(id).offset, 0, rfRef.getRF(id).error);   
                    if(rfRef.getStatus()!=0) dg.getGraph("RFdone"+id).addPoint(run, rfRef.getRF(id).delta, 0, rfRef.getRF(id).error);            

                }
            }
        }
        TCanvas canvas = new TCanvas("RF", 1200, 1400);
        canvas.getCanvas().draw(dg);
        return dg;
    }
    
    
//    public void plot() {
//        int ncol = dg.getColumns();
//        int nrow = dg.getRows();
//        DataGroup dgn = new DataGroup(ncol, nrow);
//        for(int i=0; i<nrow*ncol; i++) {
//            for(IDataSet ds : dg.getData(i)) {
//                if(ds.getDataSize(0)>0)
//                    dgn.addDataSet(ds, i);
//            }
//        }
//        TCanvas canvas = new TCanvas("RF", 1200, 1400);
//        canvas.getCanvas().draw(dgn);
//    }

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
            return Math.abs(this.error)<0.01 && Math.abs(this.sigma)<MAXSIGMA;
        }
        
        public boolean isNew(double threshold) {
            return Math.abs(this.delta)>threshold;
        }
        
        public boolean equals(RF o, double threshold) {
            return Math.abs(this.offset-o.offset)<threshold;
        }
        
        @Override
        public String toString() {
            String s = String.format("Offset=%.3f Error=%.3f Delta=%.3f Sigma=%.3f", this.offset, this.error, this.delta, this.sigma);
            return s;
        }
    }
    
    public class RFentry {
        
        private RF[] rf12 = new RF[2];
        private int run;
        private int rmin;
        private int rmax;
        private int status;
        
        public RFentry(int run) {
            this.run = run;
        }
        
        public RFentry(int run, RF rf1, RF rf2) {
            this.run = run;
            this.rf12[0] = rf1;
            this.rf12[1] = rf2;
        }

        public RF getRF(int id) {
            return rf12[id-1];
        }

        public int getRun() {
            return run;
        }

        public int getRmin() {
            return rmin;
        }
        
        public int getRmax() {
            return rmax;
        }
        
        public int getStatus() {
            return status;
        }
        
        public void setRF(int id, double offset, double error, double delta, double sigma) {
            this.rf12[id-1] = new RF(offset, error, delta, sigma);
        }
        
        public void setRmin(int run) {
            if(run>this.rmin) this.rmin = run;
        }
        
        public void setRmax(int run) {
            if(run>0 && (run<this.rmax || this.rmax==0)) this.rmax = run;
        }
    
        public void write2CCDB() {
            FileWriter writer = null;
            try {
                writer = new FileWriter("rf_"+rmin+".txt");
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                for(int i=0; i<2; i++) {
                    bufferedWriter.write(" 1 1 "+(i+1)+" "+rf12[i].offset+" "+rf12[i].sigma+"\n");
                }
                bufferedWriter.close();
                System.out.println("ccdb -c mysql://clas12writer:geom3try@clasdb/clas12 add calibration/eb/rf/offset rf_"+rmin+".txt -r "+rmin+"-"+rmax+" #\"offsets from run "+rmin+"\"");
                status = 1;
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
    }
    
    
    public static void main(String[] args){
        
    
        OptionParser parser = new OptionParser("RFtoCCDB");
        
        // valid options for event-base analysis
        parser.setRequiresInputList(true);
        parser.addOption("-t", "0.005", "minimum offset variation (ns)");
        parser.addOption("-u", "1", "disregard run ranges where new and old constants are within the threshold (0) or update range anyway (1)");
        parser.addOption("-s", "0.1", "maximum sigma value");
        parser.addOption("-x", "1", "do not plot invalid RFs (1/0)");

        parser.parse(args);
        List<String> files = parser.getInputList();
        double threshold = parser.getOption("-t").doubleValue();
        double maxsigma  = parser.getOption("-s").doubleValue();
        boolean update   = parser.getOption("-u").intValue()==1;
        boolean skip     = parser.getOption("-x").intValue()==1;
        
        RFtoCCDB rf = new RFtoCCDB(files, threshold, maxsigma, update, skip);
                
        try {
            Map<Integer,RFentry> rfs = rf.readOffsets();
            rf.findRanges(rfs);
            rf.printCCDBCommands(rfs);
            rf.plotRanges(rfs);
        } catch (IOException ex) {
            Logger.getLogger(RFtoCCDB.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

