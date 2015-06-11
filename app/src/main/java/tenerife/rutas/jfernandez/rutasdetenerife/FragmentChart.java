package tenerife.rutas.jfernandez.rutasdetenerife;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by jfernandez on 09/06/2015.
 */
public class FragmentChart extends Fragment {
    private XYPlot plot;
    private Handler handlerGraf;
    private String xmlPath;
    private float dist = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle b = getArguments();
        dist = b.getFloat(getString(R.string.VALUE_DIST), 0);
        Log.v("distance", ""+dist);
        xmlPath = b.getString(getString(R.string.VALUE_XML_ROUTE),"");
        View v = inflater.inflate(R.layout.info_chart, container, false);
        TextView tvChart = (TextView)v.findViewById(R.id.tvInfoChart);
        tvChart.setText(xmlPath);
        //getActivity().getAssets().open(xmlPath);
        plot = (XYPlot) v.findViewById(R.id.mySimpleXYPlot);

        handlerGraf = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                if (msg.obj != null){
                    final ArrayList<Integer> serieY = (ArrayList<Integer>) msg.obj;
                    //double valor = 0.0;
                    dist = dist / (float)serieY.size();
                    ArrayList<Double> serieX = new ArrayList<Double>();
                    double sum = 0;
                    int tam = serieY.size();
                    double max = 0;
                    double min = 3900;
                    for (int i = 0; i < tam;i++){
                        double var = serieY.get(i);
                        if (var > max){
                            max = var;
                        }
                        if (var < min){
                            min = var;
                        }
                        serieX.add(sum);
                        sum += dist;
                    }
                    double upperRangeBoundary = 1500;
                    double lowRangeBoundary = 500;
                    if (max > upperRangeBoundary){
                        upperRangeBoundary = max + 400;
                    }
                    if (min < lowRangeBoundary){
                        lowRangeBoundary = 0;
                    }
                    XYSeries serie = new SimpleXYSeries(serieX, serieY,"Desnivel");
                    LineAndPointFormatter series1Format = new LineAndPointFormatter(
                                    Color.rgb(0, 200, 0),                   // line color
                                    Color.rgb(0, 100, 0),                   // point color
                                    Color.argb(150, 150, 190, 150),
                                    null);
                    series1Format.getLinePaint().setStrokeWidth(2);
                    series1Format.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

                    plot.addSeries(serie, series1Format);
                    plot.setRangeBoundaries(lowRangeBoundary, upperRangeBoundary, BoundaryMode.FIXED);
                    plot.setTicksPerRangeLabel(1);
                    plot.setTicksPerDomainLabel(3);
                    plot.setRangeLabel("altitud");
                    plot.setDomainLabel("distancia");
                    plot.getGraphWidget().setDomainLabelOrientation(-45);

                    plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
                    plot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
                    plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
                    plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
                    plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.WHITE);
                    plot.getBackgroundPaint().setColor(Color.TRANSPARENT);
                    plot.getBorderPaint().setColor(getResources().getColor(R.color.lightGreen));
                    plot.getTitleWidget().getLabelPaint().setColor(Color.WHITE);
                    plot.getLegendWidget().getTextPaint().setColor(Color.WHITE);
                    plot.getRangeLabelWidget().getLabelPaint().setColor(Color.WHITE);
                }
            }
        };




       /* Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
        Number[] series2Numbers = {4, 6, 3, 8, 2, 10};
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");
        // same as above
        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        // same as above:
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);
        plot.addSeries(series2, series2Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);*/
        return v;
    }

    @Override
    public void onStart() {
        Log.v("OnStart", "OnStart");
        super.onStart();
        if (!xmlPath.contentEquals("")){
            Thread myThread = new Thread(new Runnable(){
                public void run(){
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    try {
                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        Message mensaje = new Message();

                        InputStream myInput;
                        /*if (!isInAssets(miXML)){
                            //File f = new File(Environment.getExternalStorageDirectory()+"/rutas/"+ xmlPath);
                            File f = new File("/data/data/com.rutas.java/files/", miXML);
                            myInput = new FileInputStream(f);
                        }
                        else*/
                        myInput = getActivity().getApplicationContext().getAssets().open(xmlPath);

                        InputSource archivo = new InputSource(myInput);
                        archivo.setEncoding("UTF-8");

                        String filenameArray[] = xmlPath.split("\\.");
                        String extension = filenameArray[filenameArray.length-1];

                        if (extension.toLowerCase().contentEquals("kml")){
                            KmlHandler kml = new KmlHandler();
                            xr.setContentHandler(kml);
                            xr.parse(archivo);
                            mensaje.obj = kml.getAltitud();
                        }else{
                            GpxHandler gpx = new GpxHandler(false);
                            xr.setContentHandler(gpx);
                            xr.parse(archivo);
                            mensaje.obj = gpx.getAltitud();
                        }
                        handlerGraf.sendMessage(mensaje);
                        myInput.close();
                    } catch (ParserConfigurationException e) {
                        Log.d("Parser ", e.toString());
                        e.printStackTrace();
                    } catch (SAXException e) {
                        Log.d("SAX ", e.toString());
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d("InputStream ", e.toString());
                        e.printStackTrace();
                    }
                }});
            myThread.start();
        }
    }
}
