package org.clas.viewer;

import org.clas.modules.RFsignals;
import org.clas.modules.RFoffsets;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
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
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.TDirectory;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.io.hipo.HipoDataSource;

        
/**
 *
 * @author devita
 */

public class CalibrationViewer implements IDataEventListener, ActionListener, ChangeListener {
    
    List<DetectorPane2D> DetectorPanels   	= new ArrayList<DetectorPane2D>();
    JTabbedPane tabbedpane           		= null;
    JPanel mainPanel            	        = null;
    JMenuBar menuBar                            = null;
    DataSourceProcessorPane processorPane 	= null;

    CodaEventDecoder               decoder = new CodaEventDecoder();
    CLASDecoder                clasDecoder = new CLASDecoder();
    DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
           
    private int canvasUpdateTime   = 2000;
    private int analysisUpdateTime = 100;
    private int moduleSelect       = 0;
    private int runNumber   = 0;
    private int eventNumber = 0;
    String workDir         = null;
    
    public String outPath = ".";
    
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
    
    public CalibrationViewer() {    	
        		
        this.workDir = System.getProperty("user.dir");
        System.out.println("\nCurrent work directory set to:" + this.workDir);

        // create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Load files...");
        menuItem.getAccessibleContext().setAccessibleDescription("Load files");
        menuItem.addActionListener(this);
        file.add(menuItem);        
        file.addSeparator();
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
//        menuItem = new JMenuItem("Upload all histos to the logbook");
//        menuItem.getAccessibleContext().setAccessibleDescription("Upload all histos to the logbook");
//        menuItem.addActionListener(this);
//        file.add(menuItem);        
        menuBar.add(file);
//        
//        JMenu settings = new JMenu("Settings");
//        settings.setMnemonic(KeyEvent.VK_A);
//        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
//        menuItem = new JMenuItem("Set GUI update interval", KeyEvent.VK_T);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
//        menuItem.getAccessibleContext().setAccessibleDescription("Set GUI update interval");
//        menuItem.addActionListener(this);
//        settings.add(menuItem);
//        menuItem = new JMenuItem("Set global z-axis log scale", KeyEvent.VK_L);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
//        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis log scale");
//        menuItem.addActionListener(this);
//        settings.add(menuItem);
//        menuItem = new JMenuItem("Set global z-axis lin scale", KeyEvent.VK_R);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
//        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis lin scale");
//        menuItem.addActionListener(this);
//        settings.add(menuItem);
//        menuBar.add(settings);
         
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
        
        String TriggerDef[] = { "Electron",
        		        "Electron S1","Electron S2","Electron S3","Electron S4","Electron S5","Electron S6",
        		        "HTCC S1","HTCC S2","HTCC S3","HTCC S4","HTCC S5","HTCC S6",
        		        "PCAL S1","PCAL S2","PCAL S3","PCAL S4","PCAL S5","PCAL S6",
        		        "ECAL S1","ECAL S2","ECAL S3","ECAL S4","ECAL S5","ECAL S6",
        		        "Unused","Unused","Unused","Unused","Unused","Unused",
        		        "1K Pulser"};
        		             
        JMenu trigBitsBeam = new JMenu("TriggerBits");
        trigBitsBeam.getAccessibleContext().setAccessibleDescription("Test Trigger Bits");
        
        for (int i=0; i<32; i++) {
        	
            JCheckBoxMenuItem bb = new JCheckBoxMenuItem(TriggerDef[i]);  
            final Integer bit = new Integer(i);
            bb.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                	
                    if(e.getStateChange() == ItemEvent.SELECTED) {
                        for(int k=0; k<19; k++) {
                      	monitors[k].setTriggerMask(bit);
                        }
                    } else {
                        for(int k=0; k<19; k++) {
                     	monitors[k].clearTriggerMask(bit);
                        }
                    };
                }
            });         
            trigBitsBeam.add(bb); 
        	        	
        }
//        menuBar.add(trigBitsBeam);        

        // create main panel
        mainPanel = new JPanel();	
        mainPanel.setLayout(new BorderLayout());
        
      	tabbedpane 	= new JTabbedPane();

        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisUpdateTime);

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
      
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if(e.getActionCommand() == "Adjust fit...") {
            //System.out.println("Adjusting fits for module " + this.modules.get(moduleParSelect).getName());
            this.monitors[moduleSelect].adjustFit();
        }        
        if(e.getActionCommand() == "Load files...") {
            this.readFiles();
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
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "rfCalib_" + df.format(new Date()) + ".hipo";
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
        
//        if(e.getActionCommand()=="Upload all histos to the logbook") {   
//            
//            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
//            String data = outPath + "/output" + "/rfCalib_" + this.runNumber + "_" + df.format(new Date());        
//            File theDir = new File(data);
//            // if the directory does not exist, create it
//            if (!theDir.exists()) {
//                boolean result = false;
//                try{theDir.mkdir();result = true;} 
//                catch(SecurityException se){}        
//                if(result){ System.out.println("Created directory: " + data);}
//            }
//            
//            for(int k=0; k<this.monitors.length; k++) {
//                this.monitors[k].printCanvas(data);
//            }
//            
//            LogEntry entry = new LogEntry("All online monitoring histograms for run number " + this.runNumber, "HBLOG");
//            
//            System.out.println("Starting to upload all monitoring plots");
//            
//            try{
//              entry.addAttachment(data+"/RF_canvas0.png", "RF canvas 1");
//              entry.addAttachment(data+"/RF_canvas1.png", "RF canvas 2");
//              System.out.println("RF plots uploaded");
//
//              long lognumber = entry.submitNow();
//              System.out.println("Successfully submitted log entry number: " + lognumber); 
//            } catch(Exception exc){}
//              
//        }         
        // Table menu bar
        if(e.getActionCommand()=="Save table...") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "rfCalib" + "_" + df.format(new Date());
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
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].setAnalysisParameters(this.rfbucket,this.ncycles,this.tdc2time,this.rfid,this.targetPos);
            }
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
        
    private JLabel getImage(String path,double scale) {
        JLabel label = null;
        Image image = null;
        try {
            URL url = new URL(path);
            image = ImageIO.read(url);
        } catch (IOException e) {
        	e.printStackTrace();
                System.out.println("Picture upload from " + path + " failed");
        }
        ImageIcon imageIcon = new ImageIcon(image);
        double width  = imageIcon.getIconWidth()*scale;
        double height = imageIcon.getIconHeight()*scale;
        imageIcon = new ImageIcon(image.getScaledInstance((int) width,(int) height, Image.SCALE_SMOOTH));
        label = new JLabel(imageIcon);
        return label;
    }
    
    public JPanel  getPanel(){
        return mainPanel;
    }
    
    public long getTriggerWord(DataEvent event) {    	
 	    DataBank bank = event.getBank("RUN::config");	        
        return bank.getLong("trigger", 0);
    }
    
    public long getTriggerPhase(DataEvent event) {    	
 	    DataBank bank = event.getBank("RUN::config");	        
        long timestamp = bank.getLong("timestamp",0);    
        int phase_offset = 1;
        return ((timestamp%6)+phase_offset)%6; // TI derived phase correction due to TDC and FADC clock differences 
    }
    
    private int getRunNumber(DataEvent event) {
        int rNum = this.runNumber;
        DataBank bank = event.getBank("RUN::config");
        if(bank!=null) {
            rNum      = bank.getInt("run", 0);
        }
        return rNum;
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
    	
       // EvioDataEvent decodedEvent = deco.DecodeEvent(event, decoder, table);
        //decodedEvent.show();
        		
        HipoDataEvent hipo = null;
        
        if(event!=null ){
            //event.show();

            if(event instanceof EvioDataEvent){
             	hipo = (HipoDataEvent) clasDecoder.getDataEvent(event);
                DataBank   header = clasDecoder.createHeaderBank(hipo, 0, 0, (float) 0, (float) 0);
                hipo.appendBanks(header);
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
        
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].readDataGroup(dir);
        }
    }

    
    public void printHistosToFile() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String data = outPath + "/rfCalib_" + df.format(new Date());        
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
        
  
    
    private void readFiles() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose input files directory...");
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);
        File workingDirectory = new File(this.workDir);
        fc.setCurrentDirectory(workingDirectory);
        int returnValue = fc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            int nf = 0;
            for (File fd : fc.getSelectedFiles()) {
                if (fd.isFile()) {
                    Integer current = 0;
                    Integer nevents = 0;
                    DataEvent event = null;
                    HipoDataSource hipoReader = new HipoDataSource();
                    hipoReader.open(fd);
                    current = hipoReader.getCurrentIndex();
                    nevents = hipoReader.getSize();
                    System.out.println("\nFILE: " + nf + " " + fd.getName() + " N.EVENTS: " + nevents.toString() + "  CURRENT : " + current.toString());
                    for (int k = 0; k < nevents; k++) {
                        if (hipoReader.hasEvent() == true) {
                            event = hipoReader.getNextEvent();
                        }
                        if (event != null) {
                            this.dataEventAction(event);
                            if (k % 10000 == 0) {
                                System.out.println("Read " + k + " events");
                            }
                        }
                    }
                    hipoReader.close();
                    for (int k = 0; k < this.monitors.length; k++) {
                        this.monitors[k].analyze();
                        this.monitors[k].fillSummary(this.runNumber);
                        this.monitors[k].plotHistos(this.runNumber);
//                            this.monitors[k]..getDetectorCanvas().u
                    }
                    nf++;
                }
            }
//            this.updateTable();
            System.out.println("Task completed");
        }
    }

    @Override
    public void resetEventListener() {
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].resetEventListener();
            this.monitors[k].timerUpdate();
        }      
    }
    
    public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
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
            System.out.println("RF analysis paramters");
            System.out.println("\n\tRF period: " + this.rfbucket);
            System.out.println("\n\tRF cycles: " + this.ncycles);
            System.out.println("\n\tTDC-to-time conversion factor: " + this.tdc2time);
            System.out.println("\n\tRF primary signal ID: " + this.rfid);
            System.out.println("\n\tTarget z position: " + this.targetPos);
        }
    }
    
    public void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.canvasUpdateTime = time;
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].setCanvasUpdate(time);
        }
    }

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
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].timerUpdate();
        }
   }

    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CalibrationViewer viewer = new CalibrationViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(1500, 1000);
        frame.setVisible(true);
    }
       
}