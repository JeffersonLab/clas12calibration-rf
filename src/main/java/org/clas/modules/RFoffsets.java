package org.clas.modules;

import java.util.ArrayList;
import org.clas.tools.AdjustFit;
import org.clas.viewer.CalibrationModule;
import org.jlab.clas.pdg.PhysicsConstants;
import org.jlab.clas.physics.Particle;
import org.jlab.detector.base.DetectorType;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

public class RFoffsets extends CalibrationModule {


    public RFoffsets(String name) {
        super(name);
        this.setDetectorTabNames("RF Offsets");
        this.init("RF1offset:\u03B4(RF1offset):RF1sigma:\u03B4(RF1sigma):RF2offset:\u03B4(RF2offset):RF2sigma:\u03B4(RF2sigma)");
        this.getCalibrationTable().addConstraint(5, 0.03, 0.2);
        this.getCalibrationTable().addConstraint(9, 0.03, 0.2);
    }

    @Override
    public void adjustFit() {
        int run = this.getViewRun();
        if(run==0) run = this.getRunNumber();
        System.out.println("Adjusting fit for run " + run);
        H1F hrf = this.getDataGroup().getItem(0, 0, run).getH1F("rf" + this.rfid + "center_" + run);
        F1D fun  = this.getDataGroup().getItem(0, 0, run).getF1D("f" + this.rfid + "_" + run);
        AdjustFit cfit = new AdjustFit(hrf, fun, "LRQ");
        this.getCalibrationCanvas().getCanvas("RF Offsets").update();
        this.updateTable(run);
    }

    @Override
    public void createSummary() {
        GraphErrors grf1mean = new GraphErrors("grf1mean");
        grf1mean.setTitle("RF offset");
        grf1mean.setTitleX("Run Number");
        grf1mean.setTitleY("RF offset (ns)");
        grf1mean.setMarkerColor(3);
        grf1mean.setMarkerSize(3);
        GraphErrors grf2mean = new GraphErrors("grf2mean");
        grf2mean.setTitle("RF offset");
        grf2mean.setTitleX("Run Number");
        grf2mean.setTitleY("RF offset (ns)");
        grf2mean.setMarkerColor(4);
        grf2mean.setMarkerSize(3);
        GraphErrors grf1sigma = new GraphErrors("grf1sigma");
        grf1sigma.setTitle("RF sigma");
        grf1sigma.setTitleX("Run Number");
        grf1sigma.setTitleY("RF sigma (ns)");
        grf1sigma.setMarkerColor(3);
        grf1sigma.setMarkerSize(3);
        GraphErrors grf2sigma = new GraphErrors("grf2sigma");
        grf2sigma.setTitle("RF sigma");
        grf2sigma.setTitleX("Run Number");
        grf2sigma.setTitleY("RF sigma (ns)");
        grf2sigma.setMarkerColor(4);
        grf2sigma.setMarkerSize(3);
        DataGroup dg = new DataGroup(2, 1);
        this.setDetectorSummary(dg);
        this.getCalibrationSummary().addDataSet(grf1mean, 0);
        this.getCalibrationSummary().addDataSet(grf2mean, 0);
        this.getCalibrationSummary().addDataSet(grf1sigma, 1);
        this.getCalibrationSummary().addDataSet(grf2sigma, 1);
    }
    @Override
    public void createHistos(int run) {
        // create histograms
        System.out.println("Creating histograms for run " + run);
        this.setNumberOfEvents(0);
        int nbin = (int) (this.rfbucket/0.01);
        H1F rf1 = new H1F("rf1_"+run, "rf1_"+run, nbin, -this.rfbucket/2, this.rfbucket/2);
        rf1.setTitleX("RF1 offset");
        rf1.setTitleY("Counts");
        rf1.setFillColor(33);
        H1F rf2 = new H1F("rf2_"+run, "rf2_"+run, nbin, -this.rfbucket/2, this.rfbucket/2);
        rf2.setTitleX("RF2 offset");
        rf2.setTitleY("Counts");
        rf2.setFillColor(3);
        H1F rf1center = new H1F("rf1center_"+run, "rf1center_"+run, nbin, -this.rfbucket/2, this.rfbucket/2);
        rf1center.setTitleX("RF1 offset");
        rf1center.setTitleY("Counts");
        rf1center.setFillColor(33);
        H1F rf2center = new H1F("rf2center_"+run, "rf2center_"+run, nbin, -this.rfbucket/2, this.rfbucket/2);
        rf2center.setTitleX("RF2 offset");
        rf2center.setTitleY("Counts");
        rf2center.setFillColor(4);
        F1D f1 = new F1D("f1_"+run, "[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f1.setParameter(0, 0);
        f1.setParameter(1, 0);
        f1.setParameter(2, 0.2);
        f1.setLineWidth(2);
        f1.setLineColor(2);
        f1.setOptStat("1111");
        F1D f2 = new F1D("f2_"+run, "[amp]*gaus(x,[mean],[sigma])", -5.0, 5.0);
        f2.setParameter(0, 0);
        f2.setParameter(1, 0);
        f2.setParameter(2, 0.2);
        f2.setLineWidth(2);
        f2.setLineColor(2);
        f2.setOptStat("1111");
        DataGroup dg = new DataGroup(2, 1);
        dg.addDataSet(rf1, 0);
        dg.addDataSet(rf2, 1);
        dg.addDataSet(rf1center, 0);
        dg.addDataSet(rf2center, 1);
        dg.addDataSet(f1, 0);
        dg.addDataSet(f2, 1);
        this.getDataGroup().add(dg, 0, 0, run);
    }

    @Override
    public void plotHistos(int run) {
        // initialize canvas and plot histograms
        System.out.println("Plotting histograms for run " + run);
        this.getCalibrationCanvas().getCanvas("RF Offsets").divide(2, 2);
        this.getCalibrationCanvas().getCanvas("RF Offsets").setGridX(false);
        this.getCalibrationCanvas().getCanvas("RF Offsets").setGridY(false);
        this.getCalibrationCanvas().getCanvas("RF Offsets").cd(0);
        this.getCalibrationCanvas().getCanvas("RF Offsets").draw(this.getDataGroup().getItem(0, 0, run).getH1F("rf1center_"+run));
        this.getCalibrationCanvas().getCanvas("RF Offsets").draw(this.getDataGroup().getItem(0, 0, run).getF1D("f1_"+run), "same");
        this.getCalibrationCanvas().getCanvas("RF Offsets").cd(1);
        this.getCalibrationCanvas().getCanvas("RF Offsets").draw(this.getDataGroup().getItem(0, 0, run).getH1F("rf2center_"+run));
        this.getCalibrationCanvas().getCanvas("RF Offsets").draw(this.getDataGroup().getItem(0, 0, run).getF1D("f2_"+run), "same");
        if(this.getCalibrationSummary().getGraph("grf1mean").getDataSize(0)>1) {
            this.getCalibrationCanvas().getCanvas("RF Offsets").cd(2);
            this.getCalibrationCanvas().getCanvas("RF Offsets").draw(this.getCalibrationSummary().getGraph("grf1mean"));
            this.getCalibrationCanvas().getCanvas("RF Offsets").draw(this.getCalibrationSummary().getGraph("grf2mean"), "same");
            this.getCalibrationCanvas().getCanvas("RF Offsets").getPad(2).getAxisY().setRange(-this.rfbucket/2, this.rfbucket/2);
            this.getCalibrationCanvas().getCanvas("RF Offsets").cd(3);
            this.getCalibrationCanvas().getCanvas("RF Offsets").draw(this.getCalibrationSummary().getGraph("grf1sigma"));
            this.getCalibrationCanvas().getCanvas("RF Offsets").draw(this.getCalibrationSummary().getGraph("grf2sigma"), "same");
            this.getCalibrationCanvas().getCanvas("RF Offsets").getPad(3).getAxisY().setRange(0.04, 0.1);
        }
        this.getCalibrationCanvas().getCanvas("RF Offsets").update();
    }

    @Override
    public void processEvent(DataEvent event) {

        // process event info and save into data group
        ArrayList<Integer> rf1 = new ArrayList();
        ArrayList<Integer> rf2 = new ArrayList();
        DataBank bankRF = null;
        DataBank bankRec = null;
        DataBank bankScint = null;
        if (event.hasBank("RUN::rf")) {
            bankRF = event.getBank("RUN::rf");
        }
        if (event.hasBank("REC::Particle")) {
            bankRec = event.getBank("REC::Particle");
        }
        if (event.hasBank("REC::Scintillator")) {
            bankScint = event.getBank("REC::Scintillator");
        }

        if (bankRF != null && bankRec != null && bankScint != null) {
            int nrows = bankRec.rows();
            if (nrows > 0 && bankRec.getInt("pid", 0) == 11) {       // only used events with electron identified
                Particle recEl = new Particle(
                        11,
                        bankRec.getFloat("px", 0),
                        bankRec.getFloat("py", 0),
                        bankRec.getFloat("pz", 0),
                        bankRec.getFloat("vx", 0),
                        bankRec.getFloat("vy", 0),
                        bankRec.getFloat("vz", 0));
                double time = 0;
                double path = 0;
                int paddle = 0;
                for (int j = 0; j < bankScint.rows(); j++) {
                    if (bankScint.getShort("pindex", j) == 0 && bankScint.getByte("detector", j) == DetectorType.FTOF.getDetectorId() && bankScint.getByte("layer", j) == 2) {
                        time = bankScint.getFloat("time", j);
                        path = bankScint.getFloat("path", j);
                        paddle = bankScint.getShort("component", j);
                    }
                }
                if (time > 0 & path > 0 && paddle > 10 && paddle < 25) {
                    double startTime = time - path / PhysicsConstants.speedOfLight();
                    for (int k = 0; k < bankRF.rows(); k++) {
                        int id = bankRF.getInt("id", k);
                        double dt = (startTime - bankRF.getFloat("time", k) - recEl.vz()/PhysicsConstants.speedOfLight() + 120.5 * this.rfbucket) % this.rfbucket - this.rfbucket/2;
                        this.getDataGroup().getItem(0, 0, this.getRunNumber()).getH1F("rf" + id + "_" + this.getRunNumber()).fill(dt);
                    }
                }
            }
        }
    }
    
    @Override
    public void setCanvasBookData() {
        this.getCanvasBook().setData(this.getDataGroup(), this.rfid-1);
    }
    
    @Override
    public void timerUpdate() {
        this.analyze();
    }

    @Override
    public void analyze() {
//        System.out.println("Updating RF for run " + this.getRunNumber());
        int run = this.getRunNumber();

        this.fitRF(this.getDataGroup().getItem(0, 0, this.getRunNumber()).getH1F("rf1_"+run),
                this.getDataGroup().getItem(0, 0, this.getRunNumber()).getH1F("rf1center_"+run),
                this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f1_"+run));
        this.fitRF(this.getDataGroup().getItem(0, 0, this.getRunNumber()).getH1F("rf2_"+run),
                this.getDataGroup().getItem(0, 0, this.getRunNumber()).getH1F("rf2center_"+run),
                this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f2_"+run));
        if (!this.getCalibrationTable().hasEntry(0, 0, this.getRunNumber())) {
            this.getCalibrationTable().addEntry(0, 0, this.getRunNumber());
        }

        this.updateTable(run);
    }

    @Override
    public void fillSummary() {
        System.out.println("Filling summary...");
        int run = this.getRunNumber();

        double rf1mean = this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f1_"+run).getParameter(1);
        double rf1meanerror = this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f1_"+run).parameter(1).error();
        double rf1sigma = Math.abs(this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f1_"+run).getParameter(2));
        double rf1sigmaerror = this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f1_"+run).parameter(2).error();
        double rf2mean = this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f2_"+run).getParameter(1);
        double rf2meanerror = this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f2_"+run).parameter(1).error();
        double rf2sigma = Math.abs(this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f2_"+run).getParameter(2));
        double rf2sigmaerror = this.getDataGroup().getItem(0, 0, this.getRunNumber()).getF1D("f2_"+run).parameter(2).error();
        this.getCalibrationSummary().getGraph("grf1mean").addPoint(this.getRunNumber(), rf1mean, 0, rf1meanerror);
        this.getCalibrationSummary().getGraph("grf2mean").addPoint(this.getRunNumber(), rf2mean, 0, rf2meanerror);
        this.getCalibrationSummary().getGraph("grf1sigma").addPoint(this.getRunNumber(), rf1sigma, 0, rf1sigmaerror);
        this.getCalibrationSummary().getGraph("grf2sigma").addPoint(this.getRunNumber(), rf2sigma, 0, rf1sigmaerror);        
    }
    
    public void fitRF(H1F hirf, H1F hirfcenter, F1D f1rf) {

        // move the histogram content to +/- half beam bucket around the peak
        hirfcenter.reset();
        int maxBin = hirf.getMaximumBin();
        double maxPos = hirf.getXaxis().getBinCenter(maxBin);
        int startBin = hirf.getXaxis().getBin(rfbucket * -0.5);
        int endBin = hirf.getXaxis().getBin(rfbucket * 0.5);

        for (int rawBin = startBin; rawBin < endBin; rawBin++) {
            double rawBinCenter = hirf.getXaxis().getBinCenter(rawBin);
            int fineHistOldBin = hirfcenter.getXaxis().getBin(rawBinCenter);
            int fineHistNewBin = hirfcenter.getXaxis().getBin(rawBinCenter);
            double newBinCenter = 0.0;

            if (rawBinCenter > maxPos + 0.5 * rfbucket) {
                newBinCenter = rawBinCenter - rfbucket;
                fineHistNewBin = hirfcenter.getXaxis().getBin(newBinCenter);
            }
            if (rawBinCenter < maxPos - 0.5 * rfbucket) {
                newBinCenter = rawBinCenter + rfbucket;
                fineHistNewBin = hirfcenter.getXaxis().getBin(newBinCenter);
            }

            hirfcenter.setBinContent(fineHistOldBin, 0.0);
            hirfcenter.setBinContent(fineHistNewBin, hirf.getBinContent(rawBin));
        }
        double mean = hirfcenter.getDataX(hirfcenter.getMaximumBin());
        double amp = hirfcenter.getBinContent(hirfcenter.getMaximumBin());
        double sigma = hirfcenter.getRMS();
        f1rf.setParameter(0, amp);
        f1rf.setParameter(1, mean);
        f1rf.setParameter(2, sigma);
        double rmax = Math.min(mean + 2. * Math.abs(sigma),  this.rfbucket/2);
        double rmin = Math.max(mean - 2. * Math.abs(sigma), -this.rfbucket/2);
        f1rf.setRange(rmin, rmax);
        DataFitter.fit(f1rf, hirfcenter, "Q"); //No options uses error for sigma 
        hirfcenter.setFunction(null);
        mean = f1rf.getParameter(1);
        sigma = f1rf.getParameter(2);
        rmax = Math.min(mean + 2. * Math.abs(sigma),  this.rfbucket/2);
        rmin = Math.max(mean - 2. * Math.abs(sigma), -this.rfbucket/2);
        f1rf.setRange(rmin, rmax);
//        System.out.println(mean + " " + sigma + " " + rmin + " " + rmax);
        DataFitter.fit(f1rf, hirfcenter, "Q"); //No options uses error for sigma 
        hirfcenter.setFunction(null);
    }

    @Override
    public void updateTable(int run) {
        double rf1mean = this.getDataGroup().getItem(0, 0, run).getF1D("f1_"+run).getParameter(1);
        double rf1meanerror = this.getDataGroup().getItem(0, 0, run).getF1D("f1_"+run).parameter(1).error();
        double rf1sigma = Math.abs(this.getDataGroup().getItem(0, 0, run).getF1D("f1_"+run).getParameter(2));
        double rf1sigmaerror = this.getDataGroup().getItem(0, 0, run).getF1D("f1_"+run).parameter(2).error();
        double rf2mean = this.getDataGroup().getItem(0, 0,run).getF1D("f2_"+run).getParameter(1);
        double rf2meanerror = this.getDataGroup().getItem(0, 0, run).getF1D("f2_"+run).parameter(1).error();
        double rf2sigma = Math.abs(this.getDataGroup().getItem(0, 0, run).getF1D("f2_"+run).getParameter(2));
        double rf2sigmaerror = this.getDataGroup().getItem(0, 0, run).getF1D("f2_"+run).parameter(2).error();

        this.getCalibrationTable().setDoubleValue(rf1mean, "RF1offset", 0, 0, run);
        this.getCalibrationTable().setDoubleValue(rf1meanerror, "\u03B4(RF1offset)", 0, 0, run);
        this.getCalibrationTable().setDoubleValue(rf1sigma, "RF1sigma", 0, 0, run);
        this.getCalibrationTable().setDoubleValue(rf1sigmaerror, "\u03B4(RF1sigma)", 0, 0, run);
        this.getCalibrationTable().setDoubleValue(rf2mean, "RF2offset", 0, 0, run);
        this.getCalibrationTable().setDoubleValue(rf2meanerror, "\u03B4(RF2offset)", 0, 0, run);
        this.getCalibrationTable().setDoubleValue(rf2sigma, "RF2sigma", 0, 0, run);
        this.getCalibrationTable().setDoubleValue(rf2sigmaerror, "\u03B4(RF2sigma)", 0, 0, run);
        getCalibrationTable().fireTableDataChanged();   
//        System.out.println(rf1mean);
    }
}
