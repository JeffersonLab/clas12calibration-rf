package org.clas.viewer;

import org.clas.modules.RFsignals;
import org.clas.modules.RFoffsets;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jlab.detector.decode.CLASDecoder;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.logging.DefaultLogger;
import org.jlab.utils.options.OptionParser;

        
/**
 *
 * @author devita
 */

public class CalibrationViewer implements IDataEventListener, ActionListener, ChangeListener {
    
    JTabbedPane tabbedpane           		= null;
    JPanel mainPanel            	        = null;
    JMenuBar menuBar                            = null;
    DataSourceProcessorPane processorPane 	= null;

    CLASDecoder                clasDecoder = new CLASDecoder();
           
    private int canvasUpdateTime   = 2000;
    private final int analysisNeventUpdate = 10000;
    private int moduleSelect       = 0;
    private int runNumber   = 0;
    private int eventNumber = 0;
    private List<String> inputFiles = null;
    private int currentFile = 0;
    private boolean saveResults = true;
    private boolean quitWhenDone = true;
    private String workDir  = null;    
    private String outPath = ".";
    
    // detector monitors
    CalibrationModule[] monitors = {
                new RFoffsets("rfOffsets"),         
                new RFsignals("rfSignals")          
    };

    private double tdc2time  = 0.023436; // ns/ch
    private double rfbucket  = 4.008;    // ns
    private int    ncycles   = 32;
    private int    rfid      = 1;
    private double targetPos = -3;      // cm
    
    
    public CalibrationViewer(String configuration, boolean save, boolean quit) {    	
        		
        this.setAnalysisParameters(configuration);
        this.saveResults = save;
        this.quitWhenDone = quit;
        this.workDir = System.getProperty("user.dir");
        System.out.println("\nCurrent work directory set to:" + this.workDir);

        // create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Open histograms file");
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms to file");
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms as png");
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms as png");
        menuItem.addActionListener(this);
        file.add(menuItem);      
        menuBar.add(file);

         
        JMenu histo = new JMenu("Histograms");
        menuItem = new JMenuItem("Adjust fit...");
        menuItem.getAccessibleContext().setAccessibleDescription("Adjust fit parameters and range");
        menuItem.addActionListener(this);
        histo.add(menuItem);        
        menuItem = new JMenuItem("View all");
        menuItem.getAccessibleContext().setAccessibleDescription("View all histograms for current module");
        menuItem.addActionListener(this);
        histo.add(menuItem);
        this.menuBar.add(histo);                

        JMenu settings = new JMenu("Settings");
        menuItem = new JMenuItem("Set RF parameters...");
        menuItem.getAccessibleContext().setAccessibleDescription("Set RF signal parameters");
        menuItem.addActionListener(this);
        settings.add(menuItem);        
        this.menuBar.add(settings);                

        JMenu table = new JMenu("Table");
        table.getAccessibleContext().setAccessibleDescription("Table operations");
        menuItem = new JMenuItem("Save table...");
        menuItem.getAccessibleContext().setAccessibleDescription("Save table content to file");
        menuItem.addActionListener(this);
        table.add(menuItem);
        menuItem = new JMenuItem("Clear table");
        menuItem.getAccessibleContext().setAccessibleDescription("Clear table content");
        menuItem.addActionListener(this);
//        table.add(menuItem);
        menuItem = new JMenuItem("Update table");
        menuItem.getAccessibleContext().setAccessibleDescription("Update table content");
        menuItem.addActionListener(this);
        table.add(menuItem);
        menuBar.add(table);

        // create main panel
        mainPanel = new JPanel();	
        mainPanel.setLayout(new BorderLayout());
        
      	tabbedpane 	= new JTabbedPane();

        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisNeventUpdate);
//        processorPane.setDelay(1000);

        mainPanel.add(tabbedpane);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
    
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        
        for(int k =0; k<this.monitors.length; k++) {
                this.tabbedpane.add(this.monitors[k].getCalibrationPanel(), this.monitors[k].getName());
                        
        }
        this.tabbedpane.addChangeListener(this);
        
        this.processorPane.addEventListener(this);
        
        this.setCanvasUpdate(canvasUpdateTime);
        
    }
      
    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if(e.getActionCommand() == "Adjust fit...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.monitors[moduleSelect].adjustFit();
        }       
//        if(e.getActionCommand()=="Set GUI update interval") {
//            this.chooseUpdateInterval();
//        }
        if(e.getActionCommand()=="Open histograms file") {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(System.getProperty("user.dir"));  
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName);
        }        
        if(e.getActionCommand()=="Print histograms as png") {
            this.printHistosToFile();
        }
        if(e.getActionCommand()=="Create histogram PDF") {
            this.createHistoPDF();
        }
        if(e.getActionCommand()=="Save histograms to file") {
            String fileName = this.getFilenameFromDate() + ".hipo";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(System.getProperty("user.dir"));   
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveHistosToFile(fileName);
        }
        
        // Table menu bar
        if(e.getActionCommand()=="Save table...") {
            String fileName = this.getFilenameFromDate();
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(this.workDir);
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].saveTable(fileName);
            }
        }
        if(e.getActionCommand()=="Clear table") {
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].resetTable();
            }
        }                     
        if(e.getActionCommand()=="Update table") {
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].updateTable();
            }
        }                     
        if(e.getActionCommand()=="Set RF parameters...") {
            this.setAnalysisParameters();
        }
         if(e.getActionCommand() == "View all") {
            this.monitors[moduleSelect].showPlots();
        }        
    }

    public void chooseUpdateInterval() {
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "GUI update interval (ms)",
                    " ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "1000");
        if(s!=null){
            int time = 1000;
            try { 
                time= Integer.parseInt(s);
            } catch(NumberFormatException e) { 
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if(time>0) {
                this.setCanvasUpdate(time);
            }
            else {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
        }
    }
    
        
    @Override
    public void dataEventAction(DataEvent event) {
    	
       // EvioDataEvent decodedEvent = deco.DecodeEvent(event, decoder, table);
        //decodedEvent.show();
        		
        HipoDataEvent hipo = null;
        
        if(event!=null ){
            //event.show();

            if(event instanceof EvioDataEvent){
                System.out.println("Unsupported data format, exiting");
             	System.exit(1);
            } 
            else {
                hipo = (HipoDataEvent) event;    
            }
            
            int rNum = this.runNumber;
            int eNum = this.eventNumber;
            if(event.hasBank("RUN::config")) {
                DataBank bank = event.getBank("RUN::config");
                 rNum      = bank.getInt("run", 0);
                 eNum      = bank.getInt("event", 0);
            }
            if(rNum!=0 && this.runNumber != rNum) {
                Logger.getLogger("org.freehep.math.minuit").setLevel(Level.WARNING);
                this.runNumber = rNum;
                for(int k=0; k<this.monitors.length; k++) {
                    this.monitors[k].setRunNumber(this.runNumber);
                }
                for(int k=0; k<this.monitors.length; k++) {
                    this.monitors[k].createHistos(this.runNumber);
                    this.monitors[k].plotHistos(this.runNumber);
                }
            } 
            this.eventNumber = eNum;
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].setEventNumber(this.eventNumber);
            }
            
            for(int k=0; k<this.monitors.length; k++) {
//                this.monitors[k].setTriggerPhase(getTriggerPhase(hipo));
//                this.monitors[k].setTriggerWord(getTriggerWord(hipo));        	    
                this.monitors[k].dataEventAction(hipo);
            }      
            
            if(event.getType() == DataEventType.EVENT_STOP) {
                System.out.println();
                this.processNextFile();
            }
	}
    }

    public void loadHistosFromFile(String fileName) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();
        Logger.getLogger("org.freehep.math.minuit").setLevel(Level.WARNING);        
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].readDataGroup(dir);
        }
    }

    
    public void printHistosToFile() {
        String data = this.getFilenameFromDate();        
        File theDir = new File(data);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;
            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
            System.out.println("Created directory: " + data);
            }
        }
        
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].printCanvas(data);
        }
        
        System.out.println("Histogram pngs succesfully saved in: " + data);
    }
    
    private String getFilenameFromDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh.mm.ss_aa");
        String fileName = outPath + "/rfCalib_" + df.format(new Date());        
        return fileName;
    }
    
    public void createHistoPDF() {
        /*
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String data = System.getProperty("user.dir") + "/output" + "/rfCalib_" + this.runNumber + "_" + df.format(new Date());        
        File theDir = new File(data);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;
            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
            System.out.println("Created directory: " + data);
            }
        }
        
        String fileName = data + "/clas12_canvas.pdf";
        System.out.println(fileName);
        
       // this.CLAS12Canvas.getCanvas("CLAS12-summary").save(fileName);
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].printCanvas(data);
        }
        */
    }
    
    private void processNextFile() {
        if(currentFile<inputFiles.size()-1) {
            this.currentFile++;
            this.processorPane.openAndRun(inputFiles.get(this.currentFile));
        }
        else if(saveResults) {
            this.saveHistosAndTables();
            if(quitWhenDone) {
                System.exit(0);
            }
        }
    }

    public void processFiles(List<String> filenames) {
        this.inputFiles = filenames;
        if(this.inputFiles!=null && !inputFiles.isEmpty()) {
            this.currentFile = 0;
            this.processorPane.openAndRun(inputFiles.get(0));
        }
    }

    @Override
    public void resetEventListener() {
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].resetEventListener();
            this.monitors[k].timerUpdate();
        }      
    }
    
    private void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }
        
    private void saveTablesToFile(String fileName) {
        // TXT table summary FILE //
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].saveTable(fileName);
        }
    }
    
    public void saveHistosAndTables() {
        String fileName = this.getFilenameFromDate();
        this.saveHistosToFile(fileName+".hipo");
        this.saveTablesToFile(fileName);
    }
        
    public final void setAnalysisParameters(String configuration) {

        if(configuration!=null && !configuration.isEmpty()) {
            String[] pars = configuration.split(":");
            if(pars.length!=5) {
                System.out.println("\nWARNING: Invalid configuration string, keeping defaults");
            }
            else {
                this.rfbucket  = Double.parseDouble(pars[0].trim());
                this.ncycles   = Integer.parseInt(pars[1].trim());
                this.tdc2time  = Double.parseDouble(pars[2].trim());
                this.rfid      = Integer.parseInt(pars[3].trim());
                this.targetPos = Double.parseDouble(pars[4].trim());
                for (CalibrationModule monitor : this.monitors) {
                    monitor.setAnalysisParameters(this.rfbucket, this.ncycles, this.tdc2time, this.rfid, this.targetPos);
                }
                this.printAnalysisParameters();
            }
        }      
    }

    public void setAnalysisParameters() {
        JTextField rfFrequency = new JTextField(5);
	JTextField rfCycles    = new JTextField(5);
	JTextField tdc2Time    = new JTextField(5);
	JTextField rfID        = new JTextField(5);
	JTextField targetPOS   = new JTextField(5);
	
        
	JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5,2));            
           
        panel.add(new JLabel("RF period"));
        rfFrequency.setText(Double.toString(this.rfbucket));
        panel.add(rfFrequency);
        panel.add(new JLabel("RF cycles"));
        rfCycles.setText(Integer.toString(this.ncycles));
        panel.add(rfCycles);
        panel.add(new JLabel("TDC2time"));
        tdc2Time.setText(Double.toString(this.tdc2time));
        panel.add(tdc2Time);
        panel.add(new JLabel("RF primary ID"));
        rfID.setText(Integer.toString(this.rfid));
        panel.add(rfID);
        panel.add(new JLabel("Target z position"));
        targetPOS.setText(Double.toString(this.targetPos));
        panel.add(targetPOS);
        
        int result = JOptionPane.showConfirmDialog(null, panel, "Analysis parameters", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {        
            if (!rfFrequency.getText().isEmpty()) {
                this.rfbucket = Double.parseDouble(rfFrequency.getText());
            } 
            if (!rfCycles.getText().isEmpty()) {
                this.ncycles = Integer.parseInt(rfCycles.getText());
            } 
            if (!tdc2Time.getText().isEmpty()) {
                this.tdc2time = Double.parseDouble(tdc2Time.getText());
            } 
            if (!rfID.getText().isEmpty()) {
                this.rfid = Integer.parseInt(rfID.getText());
            } 
            if (!targetPOS.getText().isEmpty()) {
                this.targetPos = Double.parseDouble(targetPOS.getText());
            } 
        }
        for (CalibrationModule monitor : this.monitors) {
            monitor.setAnalysisParameters(this.rfbucket, this.ncycles, this.tdc2time, this.rfid, this.targetPos);
        }
        this.printAnalysisParameters();

    }
    
    private void printAnalysisParameters() {
        System.out.println("RF analysis paramters");
        System.out.println("\tRF period: " + this.rfbucket);
        System.out.println("\tRF cycles: " + this.ncycles);
        System.out.println("\tTDC-to-time conversion factor: " + this.tdc2time);
        System.out.println("\tRF primary signal ID: " + this.rfid);
        System.out.println("\tTarget z position: " + this.targetPos);
    }
    
    public final void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.canvasUpdateTime = time;
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].setCanvasUpdate(time);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
        if(this.tabbedpane.getSelectedIndex()<this.monitors.length) {
            this.moduleSelect = this.tabbedpane.getSelectedIndex();
        }
//        System.out.println("Tab changed to " + sourceTabbedPane.getTitleAt(index) + " with module index " + this.moduleSelect);
    }
    
    @Override
    public void timerUpdate() {
//        System.out.println("Time to update ...");
        Logger.getLogger("org.freehep.math.minuit").setLevel(Level.WARNING);
        for(int k=0; k<this.monitors.length; k++) {
            if(this.monitors[k].getNumberOfEvents()>0) this.monitors[k].timerUpdate();
        }
//        System.out.print("\r" + this.processorPane.getStatus().getText()); // should be updated in common tools
    }

    public static void main(String[] args){
        
        OptionParser parser = new OptionParser("rfCalib");
       
        parser.addOption("-c", "4.008:32:0.02346:1:-3.0" , "RF configuration as a colon-separated list of strings:\n" +
                                                           "\t\t  (RF period (ns)\n" + 
                                                           "\t\t  Number of cycles or downsampling\n" + 
                                                           "\t\t  TDC-to-time conversion factor (1/ns)\n" + 
                                                           "\t\t  Primary RF signal id\n" + 
                                                           "\t\t  Target position (cm)\n");
        parser.addOption("-s", "1", "save constants and histograms (0=false; 1=true)");
        parser.addOption("-w", "1", "open GUI window (0=false; 1=true)");
        
        parser.parse(args);
        
        List<String> inputFiles = parser.getInputList();
        
        String configuration   = parser.getOption("-c").stringValue();
        boolean saveConstants  = parser.getOption("-s").intValue()!=0;
        boolean openWindow     = parser.getOption("-w").intValue()==1;
        
        if(!openWindow) System.setProperty("java.awt.headless", "true");

        CalibrationViewer viewer = new CalibrationViewer(configuration, saveConstants, !openWindow);
        viewer.processorPane.setVerbose(true);
        
        DefaultLogger.debug();

        if(openWindow) {
            JFrame frame = new JFrame("Calibration");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(viewer.mainPanel);
            frame.setJMenuBar(viewer.menuBar);
            frame.setSize(1500, 1000);
            frame.setVisible(true);
        }
        if(!inputFiles.isEmpty()) {
            System.out.println("\nProcessing file list provided from command line");
            viewer.processFiles(inputFiles);
        }
    }
}