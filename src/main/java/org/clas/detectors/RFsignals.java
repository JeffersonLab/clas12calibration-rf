package org.clas.detectors;

import java.util.ArrayList;
import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;


public class RFsignals extends DetectorMonitor {
    
    private int ntime = 500;
    
    public RFsignals(String name) {
        super(name);
        this.setDetectorTabNames("RF TDCs","RF Time","RF Timeline","RF fADC");
        this.init("\u0394TDC1:\u0394TDC2:TDC2Time1:TDC2Time2:\u0394RF:\u0394\u2329RF\u232A:\u03C3(\u0394\u2329RF\u232A)");
    }

    
    @Override
    public void createHistos(int run) {
        // create histograms
        System.out.println("Creating histograms for run " + run);
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",6,0.5,6.5);
        summary.setTitleX("sector");
        summary.setTitleY("DC hits");
        summary.setFillColor(33);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H1F rf1 = new H1F("rf1","rf1", 100,0.,120000);
        rf1.setTitleX("RF1 tdc");
        rf1.setTitleY("Counts");
        rf1.setFillColor(3);
        H1F rf2 = new H1F("rf2","rf2", 100,0.,120000);
        rf2.setTitleX("RF2 tdc");
        rf2.setTitleY("Counts");
        rf2.setFillColor(4);
        H1F rfdiff = new H1F("rfdiff","rfdiff", 160, 2.,5.);
        rfdiff.setTitleX("RF diff");
        rfdiff.setTitleY("Counts");
        F1D fdiff = new F1D("fdiff","[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        fdiff.setParameter(0, 0);
        fdiff.setParameter(1, 0);
        fdiff.setParameter(2, 1.0);
        fdiff.setLineWidth(2);
        fdiff.setLineColor(2);
        fdiff.setOptStat("1111");
        H1F rfdiffAve = new H1F("rfdiffAve_" + run,"rfdiffAve", 480, 1.,4.);
        rfdiffAve.setTitleX("RF diff");
        rfdiffAve.setTitleY("Counts");
        F1D fdiffAve = new F1D("fdiffAve_"+run,"[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        fdiffAve.setParameter(0, 0);
        fdiffAve.setParameter(1, 0);
        fdiffAve.setParameter(2, 1.0);
        fdiffAve.setLineWidth(2);
        fdiffAve.setLineColor(2);
        fdiffAve.setOptStat("1111");
        H1F rf1rawdiff = new H1F("rf1rawdiff","rf1rawdiff", 100, 6800.,6900.);
        rf1rawdiff.setTitleX("RF1 diff");
        rf1rawdiff.setTitleY("Counts");
        F1D f1rawdiff = new F1D("f1rawdiff","[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f1rawdiff.setParameter(0, 0);
        f1rawdiff.setParameter(1, 0);
        f1rawdiff.setParameter(2, 1.0);
        f1rawdiff.setLineWidth(2);
        f1rawdiff.setLineColor(2);
        f1rawdiff.setOptStat("1111");
        H1F rf2rawdiff = new H1F("rf2rawdiff","rf2rawdiff", 100, 6800.,6900.);
        rf2rawdiff.setTitleX("RF2 diff");
        rf2rawdiff.setTitleY("Counts");
        F1D f2rawdiff = new F1D("f2rawdiff","[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f2rawdiff.setParameter(0, 0);
        f2rawdiff.setParameter(1, 0);
        f2rawdiff.setParameter(2, 1.0);
        f2rawdiff.setLineWidth(2);
        f2rawdiff.setLineColor(2);
        f2rawdiff.setOptStat("1111");
        H2F rf1rawdiffrf1 = new H2F("rf1rawdiffrf1","rf1rawdiffrf1", 100,0.,120000, 25, 6800.,6900.);
        rf1rawdiffrf1.setTitleX("RF1 tdc");
        rf1rawdiffrf1.setTitleY("RF1 diff");
        H2F rf2rawdiffrf2 = new H2F("rf2rawdiffrf2","rf2rawdiffrf2", 100,0.,120000, 25, 6800.,6900.);
        rf2rawdiffrf2.setTitleX("RF2 tdc");
        rf2rawdiffrf2.setTitleY("RF2 diff");
        H1F rf1diff = new H1F("rf1diff","rf1diff", 160, this.period-2, this.period+2);
        rf1diff.setTitleX("RF1 diff (ns)");
        rf1diff.setTitleY("Counts");
        F1D f1diff = new F1D("f1diff","[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f1diff.setParameter(0, 0);
        f1diff.setParameter(1, 0);
        f1diff.setParameter(2, 1.0);
        f1diff.setLineWidth(2);
        f1diff.setLineColor(2);
        f1diff.setOptStat("1111");
        H1F rf2diff = new H1F("rf2diff","rf2diff", 160, 158.,162.);
        rf2diff.setTitleX("RF2 diff (ns)");
        rf2diff.setTitleY("Counts");
        F1D f2diff = new F1D("f2diff","[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f2diff.setParameter(0, 0);
        f2diff.setParameter(1, 0);
        f2diff.setParameter(2, 1.0);
        f2diff.setLineWidth(2);
        f2diff.setLineColor(2);
        f2diff.setOptStat("1111");
        H2F timeRF1 = new H2F("timeRF1","timeRF1",100,0.,240, 200, 1., 4.);
        timeRF1.setTitleX("RF1 (ns)");
        timeRF1.setTitleY("RF diff (ns)");
        H2F timeRF2 = new H2F("timeRF2","timeRF2",100,0.,240, 200, 1., 4.);
        timeRF2.setTitleX("RF2 (ns)");
        timeRF2.setTitleY("RF diff (ns)");
        GraphErrors  rf1Timeline = new GraphErrors("rf1Timeline");
        rf1Timeline.setTitle("RF1 Timeline"); //  title
        rf1Timeline.setTitleX("Event Number"); // X axis title
        rf1Timeline.setTitleY("RF1");   // Y axis title
        rf1Timeline.setMarkerColor(44); // color from 0-9 for given palette
        rf1Timeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rf2Timeline = new GraphErrors("rf2Timeline");
        rf2Timeline.setTitle("RF2 Timeline"); //  title
        rf2Timeline.setTitleX("Event Number"); // X axis title
        rf2Timeline.setTitleY("RF2");   // Y axis title
        rf2Timeline.setMarkerColor(44); // color from 0-9 for given palette
        rf2Timeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rfTimeline = new GraphErrors("rfTimeline");
        rfTimeline.setTitle("RF Timeline"); //  title
        rfTimeline.setTitleX("Event Number"); // X axis title
        rfTimeline.setTitleY("RF");   // Y axis title
        rfTimeline.setMarkerColor(44); // color from 0-9 for given palette
        rfTimeline.setMarkerSize(5);  // size in points on the screen
        GraphErrors  rfAveTimeline = new GraphErrors("rfAveTimeline");
        rfAveTimeline.setTitle("<RF> Timeline"); //  title
        rfAveTimeline.setTitleX("Event Number"); // X axis title
        rfAveTimeline.setTitleY("<RF>");   // Y axis title
        rfAveTimeline.setMarkerColor(44); // color from 0-9 for given palette
        rfAveTimeline.setMarkerSize(5);  // size in points on the screen
        H1F rf1difftmp = new H1F("rf1difftmp","rf1difftmp", 160, 158.,162.);
        H1F rf2difftmp = new H1F("rf2difftmp","rf2difftmp", 160, 158.,162.);
        H1F rfdifftmp = new H1F("rfdifftmp","rfdifftmp", 160, 2.,5.);
        H1F rfdiffAvetmp = new H1F("rfdiffAvetmp","rfdiffAvetmp", 480, 1.,4.);
        H1F rf1fADC = new H1F("rf1fADC","rf1fADC", 100,0.,400);
        rf1fADC.setTitleX("RF1 tdc");
        rf1fADC.setTitleY("Counts");
        rf1fADC.setFillColor(33);
        H1F rf2fADC = new H1F("rf2fADC","rf2fADC", 100,0.,400);
        rf2fADC.setTitleX("RF2 tdc");
        rf2fADC.setTitleY("Counts");
        rf2fADC.setFillColor(36);
        H1F rf1fADCadc = new H1F("rf1fADCadc","rf1fADCadc", 100,0.,10000);
        rf1fADCadc.setTitleX("RF1 adc");
        rf1fADCadc.setTitleY("Counts");
        rf1fADCadc.setFillColor(33);
        H1F rf2fADCadc = new H1F("rf2fADCadc","rf2fADCadc", 100,0.,40000);
        rf2fADCadc.setTitleX("RF2 adc");
        rf2fADCadc.setTitleY("Counts");
        rf2fADCadc.setFillColor(36);
        H1F rffADCdiff = new H1F("rffADCdiff","rffADCdiff", 1000, 55, 65);
        rffADCdiff.setTitleX("RF diff");
        rffADCdiff.setTitleY("Counts");
        H1F rffADCdifftmp = new H1F("rffADCdifftmp","rffADCdifftmp", 1000, 55, 65);
        rffADCdifftmp.setTitleX("RF diff");
        rffADCdifftmp.setTitleY("Counts");
        F1D ffADCdiff = new F1D("ffADCdiff","[amp]*gaus(x,[mean],[sigma])", 55, 65);
        ffADCdiff.setParameter(0, 0);
        ffADCdiff.setParameter(1, 0);
        ffADCdiff.setParameter(2, 1.0);
        ffADCdiff.setLineWidth(2);
        ffADCdiff.setLineColor(2);
        ffADCdiff.setOptStat("1111");
        GraphErrors  rffADCTimeline = new GraphErrors("rffADCTimeline");
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
        this.getDataGroup().add(dg, 0,0,run);
    }
        
    @Override
    public void plotHistos(int run) {
        // initialize canvas and plot histograms
        System.out.println("Plotting histograms for run " + run);
        this.getDetectorCanvas().getCanvas("RF TDCs").divide(3, 2);
        this.getDetectorCanvas().getCanvas("RF TDCs").setGridX(false);
        this.getDetectorCanvas().getCanvas("RF TDCs").setGridY(false);
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(0);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf1"));
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(1);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf1rawdiff"));
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,run).getF1D("f1rawdiff"),"same");
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(2);
        this.getDetectorCanvas().getCanvas("RF TDCs").getPad(2).getAxisZ().setLog(true);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,run).getH2F("rf1rawdiffrf1"));
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(3);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf2"));
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(4);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf2rawdiff"));
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,run).getF1D("f2rawdiff"),"same");
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(5);
        this.getDetectorCanvas().getCanvas("RF TDCs").getPad(5).getAxisZ().setLog(true);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,run).getH2F("rf2rawdiffrf2"));
        this.getDetectorCanvas().getCanvas("RF TDCs").update();
        this.getDetectorCanvas().getCanvas("RF Time").divide(3, 2);
        this.getDetectorCanvas().getCanvas("RF Time").setGridX(false);
        this.getDetectorCanvas().getCanvas("RF Time").setGridY(false);
        this.getDetectorCanvas().getCanvas("RF Time").cd(0);
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getH1F("rfdiff"));
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getF1D("fdiff"),"same");
        this.getDetectorCanvas().getCanvas("RF Time").cd(1);
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf1diff"));
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getF1D("f1diff"),"same");
        this.getDetectorCanvas().getCanvas("RF Time").cd(2);
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf2diff"));
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getF1D("f2diff"),"same");
        this.getDetectorCanvas().getCanvas("RF Time").cd(3);
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getH1F("rfdiffAve_"+run));
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getF1D("fdiffAve_"+run),"same");
        this.getDetectorCanvas().getCanvas("RF Time").cd(4);
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getH2F("timeRF1"));
        this.getDetectorCanvas().getCanvas("RF Time").cd(5);
        this.getDetectorCanvas().getCanvas("RF Time").draw(this.getDataGroup().getItem(0,0,run).getH2F("timeRF2"));
        this.getDetectorCanvas().getCanvas("RF Time").update();
        this.getDetectorCanvas().getCanvas("RF Timeline").divide(2, 2);
        this.getDetectorCanvas().getCanvas("RF Timeline").setGridX(false);
        this.getDetectorCanvas().getCanvas("RF Timeline").setGridY(false);
        if(this.getDataGroup().getItem(0,0,run).getGraph("rf1Timeline").getVectorX().size()>=2) {
            this.getDetectorCanvas().getCanvas("RF Timeline").cd(0);
            this.getDetectorCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().getItem(0,0,run).getGraph("rf1Timeline"));
            this.getDetectorCanvas().getCanvas("RF Timeline").cd(1);
            this.getDetectorCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().getItem(0,0,run).getGraph("rf2Timeline"));
            this.getDetectorCanvas().getCanvas("RF Timeline").cd(2);
            this.getDetectorCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().getItem(0,0,run).getGraph("rfTimeline"));
            this.getDetectorCanvas().getCanvas("RF Timeline").cd(3);
            this.getDetectorCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().getItem(0,0,run).getGraph("rfAveTimeline"));               
        }
        this.getDetectorCanvas().getCanvas("RF Timeline").update();
        this.getDetectorCanvas().getCanvas("RF fADC").divide(3, 2);
        this.getDetectorCanvas().getCanvas("RF fADC").setGridX(false);
        this.getDetectorCanvas().getCanvas("RF fADC").setGridY(false);
        this.getDetectorCanvas().getCanvas("RF fADC").cd(0);
        this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf1fADC"));
        this.getDetectorCanvas().getCanvas("RF fADC").cd(1);
        this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf1fADCadc"));
        this.getDetectorCanvas().getCanvas("RF fADC").cd(2);
        this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,run).getH1F("rffADCdiff"));
        this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,run).getF1D("ffADCdiff"),"same");
        this.getDetectorCanvas().getCanvas("RF fADC").cd(3);
        this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf2fADC"));
        this.getDetectorCanvas().getCanvas("RF fADC").cd(4);
        this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,run).getH1F("rf2fADCadc"));
        if(this.getDataGroup().getItem(0,0,run).getGraph("rffADCTimeline").getVectorX().size()>=2) {
            this.getDetectorCanvas().getCanvas("RF fADC").cd(5);
            this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,run).getGraph("rffADCTimeline"));
        }
        this.getDetectorCanvas().getCanvas("RF fADC").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        
        int run = this.getRunNumber();
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
                        this.getDataGroup().getItem(0,0,run).getH1F("rf1").fill(TDC*1.0);
                        rf1.add(TDC);
                    }
                    else {
                        this.getDataGroup().getItem(0,0,run).getH1F("rf2").fill(TDC*1.0);
                        rf2.add(TDC);
                    }
                }
            }
        }
//        System.out.println(rf1.size() + " " +rf2.size() + " " + run);
        for(int i=0; i<rf1.size()-1; i++) {
            this.getDataGroup().getItem(0,0,run).getH1F("rf1rawdiff").fill((rf1.get(i+1)-rf1.get(i))*1.0);
            this.getDataGroup().getItem(0,0,run).getH2F("rf1rawdiffrf1").fill(rf1.get(i),(rf1.get(i+1)-rf1.get(i))*1.0);
            this.getDataGroup().getItem(0,0,run).getH1F("rf1diff").fill((rf1.get(i+1)-rf1.get(i))*tdc2time);
            this.getDataGroup().getItem(0,0,run).getH1F("rf1difftmp").fill((rf1.get(i+1)-rf1.get(i))*tdc2time);
        }
        for(int i=0; i<rf2.size()-1; i++) {
            this.getDataGroup().getItem(0,0,run).getH1F("rf2rawdiff").fill((rf2.get(i+1)-rf2.get(i))*1.0);
            this.getDataGroup().getItem(0,0,run).getH2F("rf2rawdiffrf2").fill(rf2.get(i),(rf2.get(i+1)-rf2.get(i))*1.0);
            this.getDataGroup().getItem(0,0,run).getH1F("rf2diff").fill((rf2.get(i+1)-rf2.get(i))*tdc2time);
            this.getDataGroup().getItem(0,0,run).getH1F("rf2difftmp").fill((rf2.get(i+1)-rf2.get(i))*tdc2time);
        }

        if(rf1.size()==rf2.size()) {
            double rfTime1 = 0;
            double rfTime2 = 0;
            for(int i=0; i<rf1.size(); i++) {
                this.getDataGroup().getItem(0,0,run).getH1F("rfdiff").fill((rf1.get(i)-rf2.get(i))*tdc2time);
                this.getDataGroup().getItem(0,0,run).getH1F("rfdifftmp").fill((rf1.get(i)-rf2.get(i))*tdc2time);
                rfTime1 += rf1.get(i)*tdc2time - i*period;
                rfTime2 += rf2.get(i)*tdc2time - i*period;
            }
            rfTime1 /=rf1.size();
            rfTime2 /=rf2.size();            
            this.getDataGroup().getItem(0,0,run).getH1F("rfdiffAve_"+run).fill(rfTime1-rfTime2);
            this.getDataGroup().getItem(0,0,run).getH1F("rfdiffAvetmp").fill(rfTime1-rfTime2);
            this.getDataGroup().getItem(0,0,run).getH2F("timeRF1").fill(rfTime1,rfTime1-rfTime2);
            this.getDataGroup().getItem(0,0,run).getH2F("timeRF2").fill(rfTime2,rfTime1-rfTime2);            
        }
        if(this.getDataGroup().getItem(0,0,run).getH1F("rfdiffAvetmp").getEntries()>=ntime){
            H1F rf1diff   = this.getDataGroup().getItem(0,0,run).getH1F("rf1difftmp");
            H1F rf2diff   = this.getDataGroup().getItem(0,0,run).getH1F("rf2difftmp");
            H1F rfdiff    = this.getDataGroup().getItem(0,0,run).getH1F("rfdifftmp");
            H1F rfdiffAve = this.getDataGroup().getItem(0,0,run).getH1F("rfdiffAvetmp");
            this.getDataGroup().getItem(0,0,run).getGraph("rf1Timeline").addPoint(this.getEventNumber(), rf1diff.getMean() , 0, rf1diff.getRMS()/Math.sqrt(rf1diff.getEntries()));
            this.getDataGroup().getItem(0,0,run).getGraph("rf2Timeline").addPoint(this.getEventNumber(), rf2diff.getMean() , 0, rf2diff.getRMS()/Math.sqrt(rf2diff.getEntries()));
            this.getDataGroup().getItem(0,0,run).getGraph("rfTimeline").addPoint(this.getEventNumber(), rfdiff.getMean() , 0, rfdiff.getRMS()/Math.sqrt(rfdiff.getEntries()));
            this.getDataGroup().getItem(0,0,run).getGraph("rfAveTimeline").addPoint(this.getEventNumber(), rfdiffAve.getMean() , 0, rfdiffAve.getRMS()/Math.sqrt(rfdiffAve.getEntries()));
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
                        this.getDataGroup().getItem(0,0,run).getH1F("rf1fADC").fill(time);
                        rf1time = time;
                        rf1adc  = adc;
                    }
                    else {
                        this.getDataGroup().getItem(0,0,run).getH1F("rf2fADC").fill(time);
                        rf2time = time;
                        rf2adc  = adc;
                     }
                }
            }
            if(rf1time>0 && rf2time>0) {
                this.getDataGroup().getItem(0,0,run).getH1F("rf1fADCadc").fill(rf1adc);
                this.getDataGroup().getItem(0,0,run).getH1F("rf2fADCadc").fill(rf2adc);
                this.getDataGroup().getItem(0,0,run).getH1F("rffADCdiff").fill(rf1time-rf2time);
                this.getDataGroup().getItem(0,0,run).getH1F("rffADCdifftmp").fill(rf1time-rf2time);
            }
        }
        if(this.getDataGroup().getItem(0,0,run).getH1F("rffADCdifftmp").getEntries()>=ntime){
            H1F rfdiff    = this.getDataGroup().getItem(0,0,run).getH1F("rffADCdifftmp");
            this.getDataGroup().getItem(0,0,run).getGraph("rffADCTimeline").addPoint(this.getEventNumber(), rfdiff.getMean() , 0, rfdiff.getRMS()/Math.sqrt(rfdiff.getEntries()));
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

        if(this.getDataGroup().getItem(0,0,run).getGraph("rf1Timeline").getVectorX().size()>=2) {
            this.getDetectorCanvas().getCanvas("RF Timeline").cd(0);
            this.getDetectorCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().getItem(0,0,run).getGraph("rf1Timeline"));
            this.getDetectorCanvas().getCanvas("RF Timeline").cd(1);
            this.getDetectorCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().getItem(0,0,run).getGraph("rf2Timeline"));
            this.getDetectorCanvas().getCanvas("RF Timeline").cd(2);
            this.getDetectorCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().getItem(0,0,run).getGraph("rfTimeline"));
            this.getDetectorCanvas().getCanvas("RF Timeline").cd(3);
            this.getDetectorCanvas().getCanvas("RF Timeline").draw(this.getDataGroup().getItem(0,0,run).getGraph("rfAveTimeline"));               
        }
        if(this.getDataGroup().getItem(0,0,run).getGraph("rffADCTimeline").getVectorX().size()>=2) {
            this.getDetectorCanvas().getCanvas("RF fADC").cd(5);
            this.getDetectorCanvas().getCanvas("RF fADC").draw(this.getDataGroup().getItem(0,0,run).getGraph("rffADCTimeline"));
        }
        
        this.fitRF(this.getDataGroup().getItem(0,0,run).getH1F("rf1rawdiff"),this.getDataGroup().getItem(0,0,run).getF1D("f1rawdiff"));
        this.fitRF(this.getDataGroup().getItem(0,0,run).getH1F("rf2rawdiff"),this.getDataGroup().getItem(0,0,run).getF1D("f2rawdiff"));
        this.fitRF(this.getDataGroup().getItem(0,0,run).getH1F("rf1diff"),   this.getDataGroup().getItem(0,0,run).getF1D("f1diff"));
        this.fitRF(this.getDataGroup().getItem(0,0,run).getH1F("rf2diff"),   this.getDataGroup().getItem(0,0,run).getF1D("f2diff"));
        this.fitRF(this.getDataGroup().getItem(0,0,run).getH1F("rfdiff"),    this.getDataGroup().getItem(0,0,run).getF1D("fdiff"));
        this.fitRF(this.getDataGroup().getItem(0,0,run).getH1F("rfdiffAve_"+run), this.getDataGroup().getItem(0,0,run).getF1D("fdiffAve_"+run));
        double rfMean = this.getDataGroup().getItem(0,0,run).getH1F("rfdiffAve_"+run).getMean();
        this.getDetectorCanvas().getCanvas("RF Time").getPad(3).getAxisX().setRange(rfMean-0.5,rfMean+0.5);
        this.getDetectorCanvas().getCanvas("RF Time").getPad(4).getAxisY().setRange(rfMean-0.5,rfMean+0.5);
        this.getDetectorCanvas().getCanvas("RF Time").getPad(5).getAxisY().setRange(rfMean-0.5,rfMean+0.5);

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
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(0,0,run).getF1D("f1rawdiff").getParameter(1), "\u0394TDC1", 0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(0,0,run).getF1D("f2rawdiff").getParameter(1), "\u0394TDC2", 0,0,run);
        this.getCalibrationTable().setDoubleValue(period/this.getDataGroup().getItem(0,0,run).getF1D("f1rawdiff").getParameter(1), "TDC2Time1", 0,0,run);
        this.getCalibrationTable().setDoubleValue(period/this.getDataGroup().getItem(0,0,run).getF1D("f2rawdiff").getParameter(1), "TDC2Time2", 0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(0,0,run).getF1D("fdiff").getParameter(1), "\u0394RF", 0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(0,0,run).getF1D("fdiffAve_"+run).getParameter(1), "\u0394\u2329RF\u232A", 0,0,run);
        this.getCalibrationTable().setDoubleValue(this.getDataGroup().getItem(0,0,run).getF1D("fdiffAve_"+run).getParameter(2), "\u03C3(\u0394\u2329RF\u232A)", 0,0,run);
        getCalibrationTable().fireTableDataChanged();        
    }
    
    @Override
    public void readDataGroup(TDirectory dir) {
    }
    @Override
    public void writeDataGroup(TDirectory dir) {
    }
}
