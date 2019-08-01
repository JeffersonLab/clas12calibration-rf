package org.clas.viewer;

import org.clas.tools.CanvasBook;
import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.IDataEventListener;


public class CalibrationModule implements CalibrationConstantsListener, IDataEventListener {    
    
    private final String           monitorName;
    private ArrayList<String>      calibrationTabNames  = new ArrayList();
    private Map<Integer,DataGroup> calibrationData      = new LinkedHashMap<Integer,DataGroup>();
    private DataGroup              calibrationSummary   = null;
    private JPanel                 calibrationPanel     = null;
    private EmbeddedCanvasTabbed   calibrationCanvas    = null;
    private CalibrationConstantsView      calibTable    = new CalibrationConstantsView();
    private CalibrationConstants      calibConstants    = null;
    private ConstantsManager                    ccdb    = new ConstantsManager(); 
    private CanvasBook                    canvasBook    = new CanvasBook();
    private int                       numberOfEvents;

    
    public int bitsec = 0;
    public long trigger = 0;
    public long triggerPhase = 0;
    public int trigFD = 0;
    public int trigCD = 0;
    
    public boolean testTrigger = false;
    public boolean TriggerBeam[] = new boolean[32];
    public int TriggerMask = 0;
    
    private int runNumber   = 0;
    private int eventNumber = 0;
    private int viewRun     = 0;
    
    public double tdc2time  = 0.023436;
    public double rfbucket  = 4.008;
    public int    ncycles   = 32;
    public int    rfid      = 1;
    public double period    = rfbucket*ncycles;
    public double targetPos = -3;

    
    public CalibrationModule(String name){
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        GStyle.getAxisAttributesZ().setLabelFontSize(14);
        GStyle.setPalette("kDefault");
        GStyle.getAxisAttributesX().setLabelFontName("Avenir");
        GStyle.getAxisAttributesY().setLabelFontName("Avenir");
        GStyle.getAxisAttributesZ().setLabelFontName("Avenir");
        GStyle.getAxisAttributesX().setTitleFontName("Avenir");
        GStyle.getAxisAttributesY().setTitleFontName("Avenir");
        GStyle.getAxisAttributesZ().setTitleFontName("Avenir");
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(1);
                
                
        this.monitorName = name;
        this.calibrationPanel  = new JPanel();
        this.calibrationCanvas = new EmbeddedCanvasTabbed();
        this.numberOfEvents = 0;
        
    }

    
    public void adjustFit() {
        System.out.println("\nAdjust-fit function not implemented for current module");
    }

    public void analyze() {
        // analyze detector data at the end of data processing
    }

    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
//        System.out.println("Well. it's working " + col + "  " + row);
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
//        System.out.println(str_sector + " " + str_layer + " " + str_component);
        
        int sector    = Integer.parseInt(str_sector);
        int layer     = Integer.parseInt(str_layer);
        int component = Integer.parseInt(str_component);
        
        this.plotHistos(component);
        this.viewRun = component;
    }
    
    public void createHistos(int run) {
        // initialize canvas and create histograms
    }
    
    public void createSummary() {
        // initialize canvas and create histograms
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
        if (!testTriggerMask()) return;
        this.setNumberOfEvents(this.getNumberOfEvents()+1);
        if (event.getType() == DataEventType.EVENT_START) {
//            resetEventListener();
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_SINGLE) {
            processEvent(event);
            plotEvent(event);
	} else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_STOP) {
            processEvent(event);
            analyze();
            fillSummary(this.getRunNumber());
            plotHistos(this.getRunNumber());
	}
    }

    public void drawDetector() {
    
    }
    
    public ConstantsManager getCcdb() {
        return ccdb;
    }

    public void setTriggerPhase(long phase) {
    	   this.triggerPhase = phase;
    }
    
    public long getTriggerPhase() {
    	    return this.triggerPhase;
    }
    
    public void setTriggerWord(long trig) {
    	   this.trigger = trig;
    }
    
    public void setTestTrigger(boolean test) {
    	   this.testTrigger = test;
    }
    
    public int     getFDTrigger()            {return (int)(this.trigger)&0x000000000ffffffff;}
    public int     getCDTrigger()            {return (int)(this.trigger>>32)&0x00000000ffffffff;}
    public boolean isGoodFD()                {return  getFDTrigger()>0;}    
    public boolean isTrigBitSet(int bit)     {int mask=0; mask |= 1<<bit; return isTrigMaskSet(mask);}
    public boolean isTrigMaskSet(int mask)   {return (getFDTrigger()&mask)!=0;}
    public boolean isGoodECALTrigger(int is) {return (testTrigger)? is==getECALTriggerSector():true;}    
    public int           getElecTrigger()    {return getFDTrigger()&0x1;}
    public int     getElecTriggerSector()    {return (int) (isGoodFD() ? Math.log10(getFDTrigger()>>1)/0.301+1:0);} 
    public int     getECALTriggerSector()    {return (int) (isGoodFD() ? Math.log10(getFDTrigger()>>19)/0.301+1:0);}       
    public int     getPCALTriggerSector()    {return (int) (isGoodFD() ? Math.log10(getFDTrigger()>>13)/0.301+1:0);}       
    public int     getHTCCTriggerSector()    {return (int) (isGoodFD() ? Math.log10(getFDTrigger()>>7)/0.301+1:0);} 
    
    public int    getTriggerMask()        {return this.TriggerMask;}
    public void   setTriggerMask(int bit) {this.TriggerMask|=(1<<bit);}  
    public void clearTriggerMask(int bit) {this.TriggerMask&=~(1<<bit);}  
    public boolean testTriggerMask()      {return this.TriggerMask!=0 ? isTrigMaskSet(this.TriggerMask):true;}
    public boolean isGoodTrigger(int bit) {return TriggerBeam[bit] ? isTrigBitSet(bit):true;}


    public List<CalibrationConstants> getCalibrationConstants() {
	return Arrays.asList(calibConstants);
    }
    
    public CalibrationConstants getCalibrationTable() {
	return calibConstants;
    }
    
    public CalibrationConstantsView getCalibTable() {
        return calibTable;
    }

    public CanvasBook getCanvasBook() {
        return canvasBook;
    }
   
    public EmbeddedCanvasTabbed getCalibrationCanvas() {
        return calibrationCanvas;
    }
    
    public ArrayList<String> getCalibrationTabNames() {
        return calibrationTabNames;
    }

    public Map<Integer,DataGroup>  getDataGroup(){
        return calibrationData;
    }

    public String getName() {
        return monitorName;
    }
    
    public JPanel getCalibrationPanel() {
        return calibrationPanel;
    }
    
    public DataGroup getCalibrationSummary() {
        return calibrationSummary;
    }
    
    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    public int getEventNumber() {
        return eventNumber;
    }

    public int getRunNumber() {
        return runNumber;
    }

    public int getViewRun() {
        return viewRun;
    }
    
    public boolean hasCalibrationSummary() {
        if(this.calibrationSummary!=null) return true;
        else return false;
    }

    public void fillSummary(int run) {
        
    }

    public void init(String Constants) {
        // initialize monitoring application
        getCalibrationPanel().setLayout(new BorderLayout());
        this.calibConstants = new CalibrationConstants(3,Constants);
        this.calibConstants.setName(this.monitorName);
	this.calibConstants.setPrecision(4);
        this.calibTable.addConstants(this.getCalibrationConstants().get(0),this);
        JSplitPane   splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBottomComponent(getCalibTable());
        splitPane.setTopComponent(getCalibrationCanvas());
        splitPane.setDividerLocation(0.75);        
        splitPane.setResizeWeight(0.75);
        getCalibrationPanel().add(splitPane,BorderLayout.CENTER);  
        this.createSummary();
    }
    
    public void processEvent(DataEvent event) {
        // process event
    }
    
    public void plotEvent(DataEvent event) {
        // process event
    }
    
    public void plotHistos(int run) {

    }
    
    public void printCanvas(String dir) {
        // print canvas to files
        int run = this.getViewRun();
        if(run==0) run = this.getRunNumber();
        for(int tab=0; tab<this.calibrationTabNames.size(); tab++) {
            String fileName = dir + "/" + this.monitorName + "_" + run + "_canvas" + tab + ".png";
            System.out.println(fileName);
            this.calibrationCanvas.getCanvas(this.calibrationTabNames.get(tab)).save(fileName);
        }
    }
    
    @Override
    public void resetEventListener() {
        System.out.println("Resetting " + this.getName() + " histogram for run " + this.getRunNumber());
        this.createHistos(this.getRunNumber());
        this.plotHistos(this.getRunNumber());
    }
    
    
    public void resetHistos() {
        // initialize canvas and create histograms
       this.getDataGroup().clear();
    }
    
    public void resetTable() {
       for(int i=0; i<this.getCalibrationTable().getRowCount(); i++) {
            this.getCalibrationTable().removeRow(i);
        }
    }
    
    public void setCanvasUpdate(int time) {
        for(int tab=0; tab<this.calibrationTabNames.size(); tab++) {
            this.calibrationCanvas.getCanvas(this.calibrationTabNames.get(tab)).initTimer(time);
        }
    }
    
    public void setDetectorCanvas(EmbeddedCanvasTabbed canvas) {
        this.calibrationCanvas = canvas;
    }
    
    public void setDetectorTabNames(String... names) {
        for(String name : names) {
            this.calibrationTabNames.add(name);
        }
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed(names);
        this.setDetectorCanvas(canvas);
    }
 
    public void setDetectorSummary(DataGroup group) {
        this.calibrationSummary = group;
    }
    
    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public void setEventNumber(int eventNumber) {
        this.eventNumber = eventNumber;
    }

    public void setRunNumber(int runNumber) {
        this.runNumber = runNumber;
    }

    @Override
    public void timerUpdate() {
        
    }
 
    public void readDataGroup(TDirectory dir) {
        String folder = this.getName() + "/";
        System.out.println("Reading from: " + folder);
//        DataGroup sum = this.getCalibrationSummary();
//        int nrows = sum.getRows();
//        int ncols = sum.getColumns();
//        int nds   = nrows*ncols;
//        DataGroup newSum = new DataGroup(ncols,nrows);
//        for(int i = 0; i < nds; i++){
//            List<IDataSet> dsList = sum.getData(i);
//            for(IDataSet ds : dsList){
//                System.out.println("\t --> " + ds.getName());
//                newSum.addDataSet(dir.getObject(folder, ds.getName()),i);
//            }
//        }            
//        this.setDetectorSummary(newSum);
        Map<Integer, DataGroup> map = this.getDataGroup();
        System.out.println(dir.getCompositeObjectList(dir));
        for(String path : dir.getCompositeObjectList(dir)) {
            String[] tokens = path.split("/");
            if(tokens.length==4) {
                int run = Integer.valueOf(tokens[2]);
                if(!this.getDataGroup().containsKey(run)) {
                    this.createHistos(run);
                }
                DataGroup group = map.get(run);
                String subFolder = folder + run + "/";
                int nrows = group.getRows();
                int ncols = group.getColumns();
                int nds   = nrows*ncols;
                DataGroup newGroup = new DataGroup(ncols,nrows);
                for(int i = 0; i < nds; i++){
                    List<IDataSet> dsList = group.getData(i);
                    for(IDataSet ds : dsList){
                        System.out.println("\t --> " + ds.getName());
                        newGroup.addDataSet(dir.getObject(subFolder, ds.getName()),i);
                    }
                }
                map.replace(run, newGroup);
                this.setRunNumber(run);
                this.plotHistos(run);
                this.analyze();
                this.fillSummary(run);
            }
        }
    }
    
    public void saveTable(String name) {
       try {
            // Open the output file
            File outputFile = new File(name + "." + this.getName() + ".txt");
            FileWriter outputFw = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter outputBw = new BufferedWriter(outputFw);

            for (int i = 0; i < this.calibConstants.getRowCount(); i++) {
                String line = new String();
                for (int j = 0; j < this.calibConstants.getColumnCount(); j++) {
                    line = line + this.calibConstants.getValueAt(i, j);
                    if (j < this.calibConstants.getColumnCount() - 1) {
                        line = line + " ";
                    }
                }
                outputBw.write(line);
                outputBw.newLine();
            }
            outputBw.close();
            System.out.println("Constants saved to'" + name);
        } catch (IOException ex) {
            System.out.println(
                    "Error writing file '"
                    + name + "'");
            // Or we could just do this: 
            ex.printStackTrace();
        }
    }
    
    public void updateTable() {

        for (int i = 0; i < this.calibConstants.getRowCount(); i++) {
            String line = new String();
            line = line + this.calibConstants.getValueAt(i, 2);
            int run = Integer.parseInt(line);
            this.updateTable(run);
        }
    }
    
    public void updateTable(int run) {

     }

    public void setAnalysisParameters(double rfbucket, int ncycles, double tdc2time, int rfid, double targetPos) {
        this.rfbucket  = rfbucket;
        this.ncycles   = ncycles;
        this.tdc2time  = tdc2time;
        this.rfid      = rfid;
        this.period    = rfbucket*ncycles;
        this.targetPos = targetPos;
    }
    
    public void setCanvasBookData() {
        
    }
    
    public void showPlots() {
        this.setCanvasBookData();
        if(this.canvasBook.getCanvasDataSets().size()!=0) {
            JFrame frame = new JFrame(this.getName());
            frame.setSize(1000, 800);        
            frame.add(canvasBook);
            // frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
        else {
        System.out.println("Function not implemented in current module");            
        }
    }
    
    public void writeDataGroup(TDirectory dir) {
        String folder = "/" + this.getName();
        dir.mkdir(folder);
        dir.cd(folder);
        if(this.hasCalibrationSummary()) {
            DataGroup sum = this.getCalibrationSummary();
            int nrows = sum.getRows();
            int ncols = sum.getColumns();
            int nds   = nrows*ncols;
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = sum.getData(i);
                for(IDataSet ds : dsList){
                    System.out.println("\t --> " + ds.getName());
                    dir.addDataSet(ds);
                }
            }
        }
        Map<Integer, DataGroup> map = this.getDataGroup();
        for( Map.Entry<Integer, DataGroup> entry : map.entrySet()) {
            int       key   = entry.getKey();
            DataGroup group = entry.getValue();
            String subFolder = folder + "/" + key;
            dir.mkdir(subFolder);
            dir.cd(subFolder);
            int nrows = group.getRows();
            int ncols = group.getColumns();
            int nds   = nrows*ncols;
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    System.out.println("\t --> " + ds.getName());
                    dir.addDataSet(ds);
                }
            }
        }
    }
        
}
