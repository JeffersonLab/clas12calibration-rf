package org.clas.modules;

import java.util.ArrayList;
import java.util.Arrays;
import org.clas.viewer.CalibrationModule;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.groups.IndexedTable;


public class RFsignals extends CalibrationModule {
    
    private int ntime = 500;
    IndexedTable rfConfig = null;
    
    public RFsignals(String name) {
        super(name);
        
        this.getCcdb().setVariation("default");
        this.getCcdb().init(Arrays.asList(new String[]{"/calibration/eb/rf/config"}));

        this.setDetectorTabNames("RF TDCs","RF Time","RF Timeline","RF fADC");
        this.init("\u0394TDC1:\u0394TDC2:TDC2Time1:TDC2Time2:\u0394RF:\u0394\u2329RF\u232A:\u03C3(\u0394\u2329RF\u232A)");
        this.getCalibrationTable().addConstraint(9, 0.01, 0.05);
    }

    
    @Override
    public void createHistos(int run) {
        // create histograms
        System.out.println("Creating histograms for run " + run);
        this.setNumberOfEvents(0);
        int tdcMin = (int) (ncycles*rfbucket/tdc2time)-100;
        int tdcMax = (int) (ncycles*rfbucket/tdc2time)+100;
//        H1F summary = new H1F("summary","summary",6,0.5,6.5);
//        summary.setTitleX("sector");
//        summary.setTitleY("DC hits");
//        summary.setFillColor(33);
//        DataGroup sum = new DataGroup(1,1);
//        sum.addDataSet(summary, 0);
//        this.setDetectorSummary(sum);
        H1F rf1 = new H1F("rf1_"+run,"rf1_"+run, 100,0.,120000);
        rf1.setTitleX("RF1 tdc");
        rf1.setTitleY("Counts");
        rf1.setFillColor(3);
        H1F rf2 = new H1F("rf2_"+run,"rf2_"+run, 100,0.,120000);
        rf2.setTitleX("RF2 tdc");
        rf2.setTitleY("Counts");
        rf2.setFillColor(4);
        H1F rfdiff = new H1F("rfdiff_"+run,"rfdiff_"+run, 240, 0., (int) this.rfbucket);
        rfdiff.setTitleX("RF diff");
        rfdiff.setTitleY("Counts");
        F1D fdiff = new F1D("fdiff_"+run,"[amp]*gaus(x,[mean],[sigma])", 0., this.rfbucket);
        fdiff.setParameter(0, 0);
        fdiff.setParameter(1, 0);
        fdiff.setParameter(2, 1.0);
        fdiff.setLineWidth(2);
        fdiff.setLineColor(2);
        fdiff.setOptStat("1111");
        H1F rfdiffAve = new H1F("rfdiffAve_" + run,"rfdiffAve_"+run, 480, 0., this.rfbucket);
        rfdiffAve.setTitleX("RF diff");
        rfdiffAve.setTitleY("Counts");
        F1D fdiffAve = new F1D("fdiffAve_"+run,"[amp]*gaus(x,[mean],[sigma])", 0., this.rfbucket);
        fdiffAve.setParameter(0, 0);
        fdiffAve.setParameter(1, 0);
        fdiffAve.setParameter(2, 1.0);
        fdiffAve.setLineWidth(2);
        fdiffAve.setLineColor(2);
        fdiffAve.setOptStat("1111");
        H1F rf1rawdiff = new H1F("rf1rawdiff_"+run,"rf1rawdiff_"+run, 100, tdcMin, tdcMax);
        rf1rawdiff.setTitleX("RF1 diff");
        rf1rawdiff.setTitleY("Counts");
        F1D f1rawdiff = new F1D("f1rawdiff_"+run,"[amp]*gaus(x,[mean],[sigma])", tdcMin, tdcMax);
        f1rawdiff.setParameter(0, 0);
        f1rawdiff.setParameter(1, 0);
        f1rawdiff.setParameter(2, 1.0);
        f1rawdiff.setLineWidth(2);
        f1rawdiff.setLineColor(2);
        f1rawdiff.setOptStat("1111");
        H1F rf2rawdiff = new H1F("rf2rawdiff_"+run,"rf2rawdiff_"+run, 100, tdcMin, tdcMax);
        rf2rawdiff.setTitleX("RF2 diff");
        rf2rawdiff.setTitleY("Counts");
        F1D f2rawdiff = new F1D("f2rawdiff_"+run,"[amp]*gaus(x,[mean],[sigma])", tdcMin, tdcMax);
        f2rawdiff.setParameter(0, 0);
        f2rawdiff.setParameter(1, 0);
        f2rawdiff.setParameter(2, 1.0);
        f2rawdiff.setLineWidth(2);
        f2rawdiff.setLineColor(2);
        f2rawdiff.setOptStat("1111");
        H2F rf1rawdiffrf1 = new H2F("rf1rawdiffrf1_"+run,"rf1rawdiffrf1_"+run, 100,0.,120000, 25, tdcMin, tdcMax);
        rf1rawdiffrf1.setTitleX("RF1 tdc");
        rf1rawdiffrf1.setTitleY("RF1 diff");
        H2F rf2rawdiffrf2 = new H2F("rf2rawdiffrf2_"+run,"rf2rawdiffrf2_"+run, 100,0.,120000, 25, tdcMin, tdcMax);
        rf2rawdiffrf2.setTitleX("RF2 tdc");
        rf2rawdiffrf2.setTitleY("RF2 diff");
        H1F rf1diff = new H1F("rf1diff_"+run,"rf1diff_"+run, 160, this.period-2, this.period+2);
        rf1diff.setTitleX("RF1 diff (ns)");
        rf1diff.setTitleY("Counts");
        F1D f1diff = new F1D("f1diff_"+run,"[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f1diff.setParameter(0, 0);
        f1diff.setParameter(1, 0);
        f1diff.setParameter(2, 1.0);
        f1diff.setLineWidth(2);
        f1diff.setLineColor(2);
        f1diff.setOptStat("1111");
        H1F rf2diff = new H1F("rf2diff_"+run,"rf2diff_"+run, 160, this.period-2, this.period+2);
        rf2diff.setTitleX("RF2 diff (ns)");
        rf2diff.setTitleY("Counts");
        F1D f2diff = new F1D("f2diff_"+run,"[amp]*gaus(x,[mean],[sigma])", tdcMin, tdcMax);
        f2diff.setParameter(0, 0);
        f2diff.setParameter(1, 0);
        f2diff.setParameter(2, 1.0);
        f2diff.setLineWidth(2);
        f2diff.setLineColor(2);
        f2diff.setOptStat("1111");
        H2F timeRF1 = new H2F("timeRF1_"+run,"timeRF1_"+run,100,0.,this.period, 200, 0., this.rfbucket);
        timeRF1.setTitleX("RF1 (ns)");
        timeRF1.setTitleY("RF diff (ns)");
        H2F timeRF2 = new H2F("timeRF2_"+run,"timeRF2_"+run,100,0.,this.period, 200, 0., this.rfbucket);
        timeRF2.setTitleX("RF2 (ns)");
        timeRF2.setTitleY("RF diff (ns)");
        GraphErrors  rf1Timeline = new GraphErrors("rf1Timeline_"+run);
        rf1Timeline.setTitle("RF1 Timeline"); //  title
        rf1Timeline.setTitleX("Event Number"); // X axis title
        rf1Timeline.setTitleY("RF1");   // Y axis title
        rf1Timeline.setMarkerColor(44); // color from 0-9 for given palette
        rf1Timeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rf2Timeline = new GraphErrors("rf2Timeline_"+run);
        rf2Timeline.setTitle("RF2 Timeline"); //  title
        rf2Timeline.setTitleX("Event Number"); // X axis title
        rf2Timeline.setTitleY("RF2");   // Y axis title
        rf2Timeline.setMarkerColor(44); // color from 0-9 for given palette
        rf2Timeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rfTimeline = new GraphErrors("rfTimeline_"+run);
        rfTimeline.setTitle("RF Timeline"); //  title
        rfTimeline.setTitleX("Event Number"); // X axis title
        rfTimeline.setTitleY("RF");   // Y axis title
        rfTimeline.setMarkerColor(44); // color from 0-9 for given palette
        rfTimeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rfAveTimeline = new GraphErrors("rfAveTimeline_"+run);
        rfAveTimeline.setTitle("<RF> Timeline"); //  title
        rfAveTimeline.setTitleX("Event Number"); // X axis title
        rfAveTimeline.setTitleY("<RF>");   // Y axis title
        rfAveTimeline.setMarkerColor(44); // color from 0-9 for given palette
        rfAveTimeline.setMarkerSize(5);  // size in points on the screen
        H1F rf1difftmp = new H1F("rf1difftmp_"+run,"rf1difftmp_"+run, 160, this.period-2, this.period+2);
        H1F rf2difftmp = new H1F("rf2difftmp_"+run,"rf2difftmp_"+run, 160, this.period-2, this.period+2);
        H1F rfdifftmp = new H1F("rfdifftmp_"+run,"rfdifftmp_"+run, 200, 0., this.rfbucket);
        H1F rfdiffAvetmp = new H1F("rfdiffAvetmp_"+run,"rfdiffAvetmp_"+run, 480, 0., this.rfbucket);
        H1F rf1fADC = new H1F("rf1fADC_"+run,"rf1fADC_"+run, 100,0.,400);
        rf1fADC.setTitleX("RF1 tdc");
        rf1fADC.setTitleY("Counts");
        rf1fADC.setFillColor(33);
        H1F rf2fADC = new H1F("rf2fADC_"+run,"rf2fADC_"+run, 100,0.,400);
        rf2fADC.setTitleX("RF2 tdc");
        rf2fADC.setTitleY("Counts");
        rf2fADC.setFillColor(36);
        H1F rf1fADCadc = new H1F("rf1fADCadc_"+run,"rf1fADCadc_"+run, 100,0.,60000);
        rf1fADCadc.setTitleX("RF1 adc");
        rf1fADCadc.setTitleY("Counts");
        rf1fADCadc.setFillColor(33);
        H1F rf2fADCadc = new H1F("rf2fADCadc_"+run,"rf2fADCadc_"+run, 100,0.,60000);
        rf2fADCadc.setTitleX("RF2 adc");
        rf2fADCadc.setTitleY("Counts");
        rf2fADCadc.setFillColor(36);
        H1F rffADCdiff = new H1F("rffADCdiff_"+run,"rffADCdiff_"+run, 400, 0., this.rfbucket);
        rffADCdiff.setTitleX("RF diff");
        rffADCdiff.setTitleY("Counts");
        H1F rffADCdifftmp = new H1F("rffADCdifftmp_"+run,"rffADCdifftmp_"+run, 400, 0., this.rfbucket);
        rffADCdifftmp.setTitleX("RF diff");
        rffADCdifftmp.setTitleY("Counts");
        F1D ffADCdiff = new F1D("ffADCdiff_"+run,"[amp]*gaus(x,[mean],[sigma])", 0., this.rfbucket);
        ffADCdiff.setParameter(0, 0);
        ffADCdiff.setParameter(1, 0);
        ffADCdiff.setParameter(2, 1.0);
        ffADCdiff.setLineWidth(2);
        ffADCdiff.setLineColor(2);
        ffADCdiff.setOptStat("1111");
        GraphErrors  rffADCTimeline = new GraphErrors("rffADCTimeline_"+run);
        rffADCTimeline.setTitle("RF fADC Timeline"); //  title
        rffADCTimeline.setTitleX("Event Number"); // X axis title
        rffADCTimeline.setTitleY("RF");   // Y axis title
        rffADCTimeline.setMarkerColor(22); // color from 0-9 for given palette
        rffADCTimeline.setMarkerSize(5);  // size in points on the screen
 
        DataGroup dg = new DataGroup(1,26);
        dg.addDataSet(rf1, 0);
        dg.addDataSet(rf1rawdiff,1);
        dg.addDataSet(f1rawdiff, 1);
        dg.addDataSet(rf1rawdiffrf1,2);
        dg.addDataSet(rf2, 3);
        dg.addDataSet(rf2rawdiff,4);
        dg.addDataSet(f2rawdiff, 4);
        dg.addDataSet(rf2rawdiffrf2,5);        
        dg.addDataSet(rfdiff, 6);
        dg.addDataSet(fdiff,  6);
        dg.addDataSet(rf1diff,7);
        dg.addDataSet(f1diff, 7);
        dg.addDataSet(rf2diff,8);
        dg.addDataSet(f2diff, 8);
        dg.addDataSet(rfdiffAve, 9);
        dg.addDataSet(fdiffAve,  9);
        dg.addDataSet(timeRF1, 10);
        dg.addDataSet(timeRF2, 11);
        dg.addDataSet(rf1Timeline, 12);
        dg.addDataSet(rf2Timeline, 13);
        dg.addDataSet(rfTimeline, 14);
        dg.addDataSet(rfAveTimeline, 15);
        dg.addDataSet(rf1difftmp,16);
        dg.addDataSet(rf2difftmp,17);
        dg.addDataSet(rfdifftmp,18);
        dg.addDataSet(rfdiffAvetmp,19);
        dg.addDataSet(rf1fADC,20);
        dg.addDataSet(rf2fADC,21);
        dg.addDataSet(rf1fADCadc,22);
        dg.addDataSet(rf2fADCadc,23);
        dg.addDataSet(rffADCdiff,24);
        dg.addDataSet(rffADCdifftmp,24);
        dg.addDataSet(ffADCdiff,24);
        dg.addDataSet(rffADCTimeline, 25);
        this.getDataGroup().put(run,dg);
    }
        
    @Override
    public void plotHistos(int run) {
        // initialize canvas and plot histograms
        System.out.println("Plotting histograms for run " + run);
        this.getCalibrationCanvas().getCanvas("RF TDCs").divide(3, 2);
        this.getCalibrationCanvas().getCanvas("RF TDCs").setGridX(false);
        this.getCalibrationCanvas().getCanvas("RF TDCs").setGridY(false);
        this.getCalibrationCanvas().getCanvas("RF TDCs").cd(0);
        this.getCalibrationCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().get(run).getH1F("rf1_"+run));
        this.getCalibrationCanvas().getCanvas("RF TDCs").cd(1);
        this.getCalibrationCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().get(run).getH1F("rf1rawdiff_"+run));
        this.getCalibrationCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().get(run).getF1D("f1rawdiff_"+run),"same");
        this.getCalibrationCanvas().getCanvas("RF TDCs").cd(2);
        this.getCalibrationCanvas().getCanvas("RF TDCs").getPad(2).getAxisZ().setLog(true);
        this.getCalibrationCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().get(run).getH2F("rf1rawdiffrf1_"+run));
        this.getCalibrationCanvas().getCanvas("RF TDCs").cd(3);
        this.getCalibrationCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().get(run).getH1F("rf2_"+run));
        this.getCalibrationCanvas().getCanvas("RF TDCs").cd(4);
        this.getCalibrationCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().get(run).getH1F("rf2rawdiff_"+run));
        this.getCalibrationCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().get(run).getF1D("f2rawdiff_"+run),"same");
        this.getCalibrationCanvas().getCanvas("RF TDCs").cd(5);
        this.getCalibrationCanvas().getCanvas("RF TDCs").getPad(5).getAxisZ().setLog(true);
        this.getCalibrationCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().get(run).getH2F("rf2rawdiffrf2_"+run));
        this.getCalibrationCanvas().getCanvas("RF TDCs").update();
        this.getCalibrationCanvas().getCanvas("RF Time").divide(3, 2);
        this.getCalibrationCanvas().getCanvas("RF Time").setGridX(false);
        this.getCalibrationCanvas().getCanvas("RF Time").setGridY(false);
        this.getCalibrationCanvas().getCanvas("RF Time").cd(0);
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getH1F("rfdiff_"+run));
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getF1D("fdiff_"+run),"same");
        this.getCalibrationCanvas().getCanvas("RF Time").cd(1);
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getH1F("rf1diff_"+run));
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getF1D("f1diff_"+run),"same");
        this.getCalibrationCanvas().getCanvas("RF Time").cd(2);
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getH1F("rf2diff_"+run));
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getF1D("f2diff_"+run),"same");
        this.getCalibrationCanvas().getCanvas("RF Time").cd(3);
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getH1F("rfdiffAve_"+run));
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getF1D("fdiffAve_"+run),"same");
        this.getCalibrationCanvas().getCanvas("RF Time").cd(4);
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getH2F("timeRF1_"+run));
        this.getCalibrationCanvas().getCanvas("RF Time").cd(5);
        this.getCalibrationCanvas().getCanvas("RF Time").draw(this.getDataGroup().get(run).getH2F("timeRF2_"+run));
        this.getCalibrationCanvas().getCanvas("RF Time").update();
        this.getCalibrationCanvas().getCanvas("RF Timeline").divide(2, 2);
        this.getCalibrationCanvas().getCanvas("RF Timeline").setGridX(false);
        this.getCalibrationCanvas().getCanvas("RF Timeline").setGridY(false);
        if(this.getDataGroup().get(run).getGraph("rf1Timeline_"+run).getVectorX().size()>=2) {
            this.getCalibrationCanvas().getCanvas("RF Timeline").cd(0);
            this.getCalibrationCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().get(run).getGraph("rf1Timeline_"+run));
            this.getCalibrationCanvas().getCanvas("RF Timeline").cd(1);
            this.getCalibrationCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().get(run).getGraph("rf2Timeline_"+run));
            this.getCalibrationCanvas().getCanvas("RF Timeline").cd(2);
            this.getCalibrationCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().get(run).getGraph("rfTimeline_"+run));
            this.getCalibrationCanvas().getCanvas("RF Timeline").cd(3);
            this.getCalibrationCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().get(run).getGraph("rfAveTimeline_"+run));               
        }
        this.getCalibrationCanvas().getCanvas("RF Timeline").update();
        this.getCalibrationCanvas().getCanvas("RF fADC").divide(3, 2);
        this.getCalibrationCanvas().getCanvas("RF fADC").setGridX(false);
        this.getCalibrationCanvas().getCanvas("RF fADC").setGridY(false);
        this.getCalibrationCanvas().getCanvas("RF fADC").cd(0);
        this.getCalibrationCanvas().getCanvas("RF fADC").draw(this.getDataGroup().get(run).getH1F("rf1fADC_"+run));
        this.getCalibrationCanvas().getCanvas("RF fADC").cd(1);
        this.getCalibrationCanvas().getCanvas("RF fADC").draw(this.getDataGroup().get(run).getH1F("rf1fADCadc_"+run));
        this.getCalibrationCanvas().getCanvas("RF fADC").cd(2);
        this.getCalibrationCanvas().getCanvas("RF fADC").draw(this.getDataGroup().get(run).getH1F("rffADCdiff_"+run));
        this.getCalibrationCanvas().getCanvas("RF fADC").draw(this.getDataGroup().get(run).getF1D("ffADCdiff_"+run),"same");
        this.getCalibrationCanvas().getCanvas("RF fADC").cd(3);
        this.getCalibrationCanvas().getCanvas("RF fADC").draw(this.getDataGroup().get(run).getH1F("rf2fADC_"+run));
        this.getCalibrationCanvas().getCanvas("RF fADC").cd(4);
        this.getCalibrationCanvas().getCanvas("RF fADC").draw(this.getDataGroup().get(run).getH1F("rf2fADCadc_"+run));
        if(this.getDataGroup().get(run).getGraph("rffADCTimeline_"+run).getVectorX().size()>=2) {
            this.getCalibrationCanvas().getCanvas("RF fADC").cd(5);
            this.getCalibrationCanvas().getCanvas("RF fADC").draw(this.getDataGroup().get(run).getGraph("rffADCTimeline_"+run));
        }
        this.getCalibrationCanvas().getCanvas("RF fADC").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        
        int run = this.getRunNumber();
        
        if(event.hasBank("RUN::config")){
	    DataBank head = event.getBank("RUN::config");
            int runNumber    = head.getInt("run", 0);
            if(runNumber==0) return;
            rfConfig = this.getCcdb().getConstants(runNumber, "/calibration/eb/rf/config");
            double run_tdc2Time = rfConfig.getDoubleValue("tdc2time",1,1,1);
            double run_rfbucket = rfConfig.getDoubleValue("clock",1,1,1);
            int    run_ncycles  = rfConfig.getIntValue("cycles",1,1,1);
            if(run_tdc2Time != this.tdc2time || run_rfbucket != this.rfbucket || run_ncycles != this.ncycles) {
                this.tdc2time = run_tdc2Time;
                this.rfbucket = run_rfbucket;
                this.ncycles  = run_ncycles;                
                this.period   = rfbucket*ncycles;
                this.resetEventListener();
                System.out.println("RF config parameter changed to: \n\t tdc2time = " + this.tdc2time + "\n\t rf bucket = " + this.rfbucket + "\n\t n. of cycles = " + this.ncycles);
            }
//            System.out.println();
        }        
        
        // process event info and save into data group
        ArrayList<Integer> rf1 = new ArrayList();
        ArrayList<Integer> rf2 = new ArrayList();
        if(event.hasBank("RF::tdc")==true){
            DataBank  bank = event.getBank("RF::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); 
                if(order==2) {
                    if(comp==1) {
                        this.getDataGroup().get(run).getH1F("rf1_"+run).fill(TDC*1.0);
                        rf1.add(TDC);
                    }
                    else {
                        this.getDataGroup().get(run).getH1F("rf2_"+run).fill(TDC*1.0);
                        rf2.add(TDC);
                    }
                }
            }
        }
//        System.out.println(rf1.size() + " " +rf2.size() + " " + run);
        for(int i=0; i<rf1.size()-1; i++) {
            this.getDataGroup().get(run).getH1F("rf1rawdiff_"+run).fill((rf1.get(i+1)-rf1.get(i))*1.0);
            this.getDataGroup().get(run).getH2F("rf1rawdiffrf1_"+run).fill(rf1.get(i),(rf1.get(i+1)-rf1.get(i))*1.0);
            this.getDataGroup().get(run).getH1F("rf1diff_"+run).fill((rf1.get(i+1)-rf1.get(i))*tdc2time);
            this.getDataGroup().get(run).getH1F("rf1difftmp_"+run).fill((rf1.get(i+1)-rf1.get(i))*tdc2time);
        }
        for(int i=0; i<rf2.size()-1; i++) {
            this.getDataGroup().get(run).getH1F("rf2rawdiff_"+run).fill((rf2.get(i+1)-rf2.get(i))*1.0);
            this.getDataGroup().get(run).getH2F("rf2rawdiffrf2_"+run).fill(rf2.get(i),(rf2.get(i+1)-rf2.get(i))*1.0);
            this.getDataGroup().get(run).getH1F("rf2diff_"+run).fill((rf2.get(i+1)-rf2.get(i))*tdc2time);
            this.getDataGroup().get(run).getH1F("rf2difftmp_"+run).fill((rf2.get(i+1)-rf2.get(i))*tdc2time);
        }

        if(rf1.size()==rf2.size() || true) {
            double rfTime1 = 0;
            double rfTime2 = 0;
            int npairs = Math.min(rf1.size(),rf2.size());
            for(int i=0; i<npairs; i++) {
                double rfTimei = ((rf1.get(i)-rf2.get(i))*tdc2time + (1000*rfbucket)) % rfbucket;
                this.getDataGroup().get(run).getH1F("rfdiff_"+run).fill(rfTimei);
                this.getDataGroup().get(run).getH1F("rfdifftmp_"+run).fill(rfTimei);
                rfTime1 += ((rf1.get(i)*tdc2time) % period);
                rfTime2 += ((rf2.get(i)*tdc2time) % period);
            }
            rfTime1 /=npairs;
            rfTime2 /=npairs;            
            double rfTime = (rfTime1-rfTime2 + 1000*rfbucket) % rfbucket;
            this.getDataGroup().get(run).getH1F("rfdiffAve_"+run).fill(rfTime);
            this.getDataGroup().get(run).getH1F("rfdiffAvetmp_"+run).fill(rfTime);
            this.getDataGroup().get(run).getH2F("timeRF1_"+run).fill(rfTime1,rfTime);
            this.getDataGroup().get(run).getH2F("timeRF2_"+run).fill(rfTime2,rfTime);            
        }
        if(this.getDataGroup().get(run).getH1F("rfdiffAvetmp_"+run).getEntries()>=ntime){
            H1F rf1diff   = this.getDataGroup().get(run).getH1F("rf1difftmp_"+run);
            H1F rf2diff   = this.getDataGroup().get(run).getH1F("rf2difftmp_"+run);
            H1F rfdiff    = this.getDataGroup().get(run).getH1F("rfdifftmp_"+run);
            H1F rfdiffAve = this.getDataGroup().get(run).getH1F("rfdiffAvetmp_"+run);
            this.getDataGroup().get(run).getGraph("rf1Timeline_"+run).addPoint(this.getEventNumber(), rf1diff.getMean() , 0, rf1diff.getRMS()/Math.sqrt(rf1diff.getEntries()));
            this.getDataGroup().get(run).getGraph("rf2Timeline_"+run).addPoint(this.getEventNumber(), rf2diff.getMean() , 0, rf2diff.getRMS()/Math.sqrt(rf2diff.getEntries()));
            this.getDataGroup().get(run).getGraph("rfTimeline_"+run).addPoint(this.getEventNumber(), rfdiff.getMean() , 0, rfdiff.getRMS()/Math.sqrt(rfdiff.getEntries()));
            this.getDataGroup().get(run).getGraph("rfAveTimeline_"+run).addPoint(this.getEventNumber(), rfdiffAve.getMean() , 0, rfdiffAve.getRMS()/Math.sqrt(rfdiffAve.getEntries()));
            rf1diff.reset();
            rf2diff.reset();
            rfdiff.reset();
            rfdiffAve.reset();
        }
        if(event.hasBank("RF::adc")==true){
            DataBank  bank = event.getBank("RF::adc");
            int rows = bank.rows();
            double rf1time=0;
            double rf2time=0;
            double rf1adc=0;
            double rf2adc=0;
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       adc = bank.getInt("ADC",i);
                double   time = bank.getFloat("time",i);
                int     order = bank.getByte("order",i); 
                if(order==0) {
                    if(comp==3) {
                        this.getDataGroup().get(run).getH1F("rf1fADC_"+run).fill(time);
                        rf1time = time;
                        rf1adc  = adc;
                    }
                    else {
                        this.getDataGroup().get(run).getH1F("rf2fADC_"+run).fill(time);
                        rf2time = time;
                        rf2adc  = adc;
                     }
                }
            }
            if(rf1time>0 && rf2time>0) {
                double rftime = (rf1time-rf2time + 1000*rfbucket) % rfbucket;
                this.getDataGroup().get(run).getH1F("rf1fADCadc_"+run).fill(rf1adc);
                this.getDataGroup().get(run).getH1F("rf2fADCadc_"+run).fill(rf2adc);
                this.getDataGroup().get(run).getH1F("rffADCdiff_"+run).fill(rftime);
                this.getDataGroup().get(run).getH1F("rffADCdifftmp_"+run).fill(rftime);
            }
        }
        if(this.getDataGroup().get(run).getH1F("rffADCdifftmp_"+run).getEntries()>=ntime){
            H1F rfdiff    = this.getDataGroup().get(run).getH1F("rffADCdifftmp_"+run);
            this.getDataGroup().get(run).getGraph("rffADCTimeline_"+run).addPoint(this.getEventNumber(), rfdiff.getMean() , 0, rfdiff.getRMS()/Math.sqrt(rfdiff.getEntries()));
            rfdiff.reset();
        }

    }
    
    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), 9);
    }
    
    @Override
    public void timerUpdate() {
        this.analyze();
    }
    
    @Override
    public void analyze() {
//        System.out.println("Updating RF for run " + run);       
        int run = this.getRunNumber();

        if(this.getDataGroup().get(run).getGraph("rf1Timeline_"+run).getVectorX().size()>=2) {
            this.getCalibrationCanvas().getCanvas("RF Timeline").cd(0);
            this.getCalibrationCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().get(run).getGraph("rf1Timeline_"+run));
            this.getCalibrationCanvas().getCanvas("RF Timeline").cd(1);
            this.getCalibrationCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().get(run).getGraph("rf2Timeline_"+run));
            this.getCalibrationCanvas().getCanvas("RF Timeline").cd(2);
            this.getCalibrationCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().get(run).getGraph("rfTimeline_"+run));
            this.getCalibrationCanvas().getCanvas("RF Timeline").cd(3);
            this.getCalibrationCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().get(run).getGraph("rfAveTimeline_"+run));               
        }
        if(this.getDataGroup().get(run).getGraph("rffADCTimeline_"+run).getVectorX().size()>=2) {
            this.getCalibrationCanvas().getCanvas("RF fADC").cd(5);
            this.getCalibrationCanvas().getCanvas("RF fADC").draw(this.getDataGroup().get(run).getGraph("rffADCTimeline_"+run));
        }
        
        this.fitRF(this.getDataGroup().get(run).getH1F("rf1rawdiff_"+run),this.getDataGroup().get(run).getF1D("f1rawdiff_"+run));
        this.fitRF(this.getDataGroup().get(run).getH1F("rf2rawdiff_"+run),this.getDataGroup().get(run).getF1D("f2rawdiff_"+run));
        this.fitRF(this.getDataGroup().get(run).getH1F("rf1diff_"+run),   this.getDataGroup().get(run).getF1D("f1diff_"+run));
        this.fitRF(this.getDataGroup().get(run).getH1F("rf2diff_"+run),   this.getDataGroup().get(run).getF1D("f2diff_"+run));
        this.fitRF(this.getDataGroup().get(run).getH1F("rfdiff_"+run),    this.getDataGroup().get(run).getF1D("fdiff_"+run));
        this.fitRF(this.getDataGroup().get(run).getH1F("rfdiffAve_"+run), this.getDataGroup().get(run).getF1D("fdiffAve_"+run));
        this.fitRF(this.getDataGroup().get(run).getH1F("rffADCdiff_"+run), this.getDataGroup().get(run).getF1D("ffADCdiff_"+run));
        double rfMean = this.getDataGroup().get(run).getH1F("rfdiffAve_"+run).getMean();
        if(this.getCalibrationCanvas().getCanvas("RF Time").getPad(3)!=null) this.getCalibrationCanvas().getCanvas("RF Time").getPad(3).getAxisX().setRange(rfMean-0.5,rfMean+0.5);
        if(this.getCalibrationCanvas().getCanvas("RF Time").getPad(4)!=null) this.getCalibrationCanvas().getCanvas("RF Time").getPad(4).getAxisY().setRange(rfMean-0.5,rfMean+0.5);
        if(this.getCalibrationCanvas().getCanvas("RF Time").getPad(5)!=null) this.getCalibrationCanvas().getCanvas("RF Time").getPad(5).getAxisY().setRange(rfMean-0.5,rfMean+0.5);

        this.updateTable(run);
    }

    public void fitRF(H1F hirf, F1D f1rf) {
        double mean  = hirf.getDataX(hirf.getMaximumBin());
        double amp   = hirf.getBinContent(hirf.getMaximumBin());
        double sigma = hirf.getRMS();
        f1rf.setParameter(0, amp);
        f1rf.setParameter(1, mean);
        f1rf.setParameter(2, sigma);
        f1rf.setRange(mean-3.*sigma,mean+3.*sigma);
        DataFitter.fit(f1rf, hirf, "Q"); //No options uses error for sigma 
        hirf.setFunction(null);
//        System.out.println(f1rf.parameter(1) + "+/-" + f1rf.parameter(1).error() + " " + f1rf.parameter(2)) ;
    }

    @Override
    public void updateTable(int run) {
        if(!this.getCalibrationTable().hasEntry(0,0,run) )this.getCalibrationTable().addEntry(0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().get(run).getF1D("f1rawdiff_"+run).getParameter(1), "\u0394TDC1", 0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().get(run).getF1D("f2rawdiff_"+run).getParameter(1), "\u0394TDC2", 0,0,run);
        this.getCalibrationTable().setDoubleValue(period/this.getDataGroup().get(run).getF1D("f1rawdiff_"+run).getParameter(1), "TDC2Time1", 0,0,run);
        this.getCalibrationTable().setDoubleValue(period/this.getDataGroup().get(run).getF1D("f2rawdiff_"+run).getParameter(1), "TDC2Time2", 0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().get(run).getF1D("fdiff_"+run).getParameter(1), "\u0394RF", 0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().get(run).getF1D("fdiffAve_"+run).getParameter(1), "\u0394\u2329RF\u232A", 0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().get(run).getF1D("fdiffAve_"+run).getParameter(2), "\u03C3(\u0394\u2329RF\u232A)", 0,0,run);
        getCalibrationTable().fireTableDataChanged();        
    }
    
//    @Override
//    public void readDataGroup(TDirectory dir) {
//    }
//    @Override
//    public void writeDataGroup(TDirectory dir) {
//    }
}
