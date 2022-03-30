package org.lsmr.selfcheckout.customer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.BarcodeScanner;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.BarcodeScannerObserver;


public class ScanItemController  {
	 

	private final SelfCheckoutStation checkoutStation; 
	private BigDecimal valueOfCart; //total cost of item field
	private Double weightOfCart;
	private HashMap<Barcode, BigDecimal> barcodePrice; //Map barcode to price
	private HashMap<Barcode, Double> barcodeWeight;
	private SIC sic;
	private List<Barcode> scannedItemList;  
	private BaggingAreaController bagAreaControl;	

	

	//Constructor
	public ScanItemController(SelfCheckoutStation cs, HashMap<Barcode, BigDecimal> barcodePrice, 
		HashMap<Barcode, Double> barcodeWeight) {
		checkoutStation = cs;
		valueOfCart = new BigDecimal(0);
		weightOfCart = 0.0;
		sic = new SIC();
		this.barcodePrice = barcodePrice;
		this.barcodeWeight = barcodeWeight;
		this.bagAreaControl = null;
		scannedItemList = new ArrayList<Barcode>();
		
		
		
		//Register observers in the scanner
		checkoutStation.mainScanner.attach(sic);
		checkoutStation.handheldScanner.attach(sic);
		
	}
	
	//Gives ScanItemController access to BaggingAreaController
	public void setBagAreaControl(BaggingAreaController bAController)
	{
		this.bagAreaControl = bAController;
	}
	
	//Returns the number of item scanned
	public int numOfScannedItems() {
		return scannedItemList.size();
	}
	
	public List<Barcode> getScannedItemList(){
		return scannedItemList;
	}
	
	//Remove item from scan
	//Decrease the scanner expected weight of cart
	//Enable the scanner if expected weight of cart and actual weight of cart matches
	public void unScanItem(Barcode barcode) {
		int index = scannedItemList.indexOf(barcode);
		if(index == -1)
			return;
		scannedItemList.remove(index);
		weightOfCart = weightOfCart - barcodeWeight.get(barcode);
		valueOfCart = valueOfCart.subtract(barcodePrice.get(barcode));
		
		if(bagAreaControl.getWeightOfCart() == weightOfCart) {
			checkoutStation.mainScanner.enable();
			checkoutStation.handheldScanner.enable();
		}
	}
	
	
	//Barcode Scanner Observer Implementation
	public class SIC implements BarcodeScannerObserver {
		
		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> devicenop) {
			// Ignore	
		}

		//Retrieve the bar code and use hash map to retrieve price
		//Update the value of the cart
		@Override
		public void barcodeScanned(BarcodeScanner barcodeScanner, Barcode barcode) {
			if(weightOfCart + barcodeWeight.get(barcode) <= checkoutStation.baggingArea.getWeightLimit()) {
				weightOfCart = weightOfCart + barcodeWeight.get(barcode);	
			}

			valueOfCart = valueOfCart.add(barcodePrice.get(barcode));
			scannedItemList.add(barcode);
			
			
			//Once an item is scanned, disable the scanner
			//If expected weight of cart (determined by scanner)
			//Is not the same of actual weigh of cart (determined by electronic scale)
			if(bagAreaControl.getWeightOfCart() != weightOfCart) {
				checkoutStation.mainScanner.disable();
				checkoutStation.handheldScanner.disable();
				
			}else {
				checkoutStation.mainScanner.enable();
				checkoutStation.handheldScanner.enable();
			}
			
			double begin = System.currentTimeMillis();
			bagAreaControl.setBeginTime(begin);
		}
	}
	
	
	
	public BigDecimal getValueOfCart() {
		return valueOfCart;
	}
	
	public Double getWeightOfCart() {
		return weightOfCart;
	}

	public HashMap<Barcode, BigDecimal> getBarcodePrice(){
		return this.barcodePrice;
	}

	public HashMap<Barcode, Double> getBarcodeWeight(){
		return this.barcodeWeight;
	}

}