package com.rutas.java;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.google.android.gms.maps.model.LatLng;

public class KmlHandler extends DefaultHandler{
	//Esta clase permite leer un KML y extraer un vector de coordenadas 
	private StringBuffer buffer;
	private boolean coordinates;
	private boolean lineString;

	/**
	 * Get Latitude, longitude and Altitude
	 * @return Path
     */
	public Path getFullPath(){
		Path currentPath = null;
		if (buffer.length() > 0){
			currentPath = new Path();
			String[] puntos = buffer.toString().split(" ");
			int tam = puntos.length - 1;
			for (int i = 0; i < tam; i++){
				String[] coordenadas = puntos[i].trim().split(",");
				double longit = Double.parseDouble(coordenadas[0]);
				double latit = Double.parseDouble(coordenadas[1]);
				long altura = (long) Double.parseDouble(coordenadas[2]);
				LatLng geopunto = new LatLng(latit , longit);
				currentPath.addItem(geopunto, altura);
			}
		}
		return currentPath;
	}

	public ArrayList<LatLng> getPath(){
		
		ArrayList<LatLng> camino = null;
		if (buffer.length() > 0){
			camino = new ArrayList<LatLng>();
			String[] puntos = buffer.toString().split(" ");
			int tam = puntos.length - 1;
			for (int i = 0; i < tam; i++){
				String[] coordenadas = puntos[i].trim().split(",");
				double longit = Double.parseDouble(coordenadas[0]);
				double latit = Double.parseDouble(coordenadas[1]);
				LatLng geopunto = new LatLng(latit , longit);
				camino.add(geopunto);
			}
	}
		return camino;
	}
	
	public ArrayList<Integer> getAltitud(){
		ArrayList<Integer> altitud = null;
		if (buffer.length() > 0){
			altitud = new ArrayList<Integer>();
			String[] puntos = buffer.toString().split(" ");
			int tam = puntos.length - 1;
			for (int i = 0; i < tam; i++){
				String[] coordenadas = puntos[i].trim().split(",");
				int altura = (int) Double.parseDouble(coordenadas[2]);
				altitud.add(altura);
			}
		}
		return altitud;
	}
	@Override
	public void startDocument (){
		buffer = new StringBuffer();
		coordinates = false;
		lineString = false;
	}
	
	@Override
	public void endDocument(){
		//De momento nada
	}

	@Override
	public void startElement (String uri, String localName, String qName, Attributes attributes){
		
		if (localName.contentEquals("LineString"))
			lineString = true;
		if (localName.contentEquals("coordinates"))
			coordinates = true;
	}
	
	@Override
	public void endElement (String uri, String localName, String qName){
		
		if(localName.contentEquals("LineString"))
			lineString = false;
		if (localName.contentEquals("coordinates"))
			coordinates = false;
	}
	
	@Override
	public void characters (char[] ch, int start, int length){
		if ((this.coordinates) && (this.lineString)&& (length > 1))
				buffer.append(ch, start, length);	//TODO esta fallando en cadenas largas
		}
}
