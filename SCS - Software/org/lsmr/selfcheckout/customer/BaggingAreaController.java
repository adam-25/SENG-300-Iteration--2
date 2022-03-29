package org.lsmr.selfcheckout.customer;

import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.ElectronicScaleObserver;

public class BaggingAreaController {

	private final SelfCheckoutStation checkoutStation;
	private BAC bac;
	private double weightOfCart;
	private ScanItemController scanItemControl;
	private int numOfItemsInBaggingArea;
	private double previousWeightOfCart;
	
	
	//Constructor
	public BaggingAreaController(SelfCheckoutStation cs) {
		
		checkoutStation = cs;
		bac = new BAC();
		weightOfCart = 0;
		this.scanItemControl = null;
		numOfItemsInBaggingArea =0;
		
		
		//Register observers to the scanner
		checkoutStation.scale.attach(bac);
		
	}
	
	//Connect bagging area control to scan item control
	public void setScanItemControl(ScanItemController sIController)
	{
		this.scanItemControl = sIController;
	}
	
	public int getNumOfItemsInBaggingArea() {
		return numOfItemsInBaggingArea;
	}
	
	
	
	private class BAC implements ElectronicScaleObserver{
		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore
		
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore
			
		}

		
		@Override
		public void weightChanged(ElectronicScale scale, double weightInGrams) {
			previousWeightOfCart = weightOfCart;
			weightOfCart = weightInGrams;
			 
			//Alter number of items in bagging area based on weight changed
			if(weightOfCart > previousWeightOfCart) {
				numOfItemsInBaggingArea++;
			}else {
				numOfItemsInBaggingArea--;
			}
			
			//Once item has been placed in bagging area, enable the scanner
			//If expected weight of cart (determined by scanner)
			//Is the same of actual weigh of cart (determined by electronic scale)
			if(scanItemControl.getWeightOfCart() == weightOfCart && weightOfCart - scanItemControl.getWeightOfCart() < scanItemControl.getSensitivity()){
				checkoutStation.scanner.enable();
			}else {
				checkoutStation.scanner.disable();
			}
			
		}

		//Disable bar code scanner
		@Override
		public void overload(ElectronicScale scale) {
			checkoutStation.scanner.disable();
		}

		//Enable bar code scanner
		@Override
		public void outOfOverload(ElectronicScale scale) {
			checkoutStation.scanner.enable();	
		}
		
	}
	
	
	
	public double getWeightOfCart() {
		return weightOfCart;
	}

	public void attendantVeritfyBag(){
		BigDecimal bagPrice = new BigDecimal(0);
		Numeral[] nBag = {Numeral.one, Numeral.two, Numeral.three, Numeral.four};
		Barcode barcodeBag = new Barcode(nBag);
		scanItemControl.barcodePrice.put(barcodeBag, bagPrice);
		double bagWeight = weightOfCart - previousWeightOfCart;
		scanItemControl.barcodeWeight.put(barcodeBag, bagWeight);
		BarcodedItem bagItem = new BarcodedItem(barcodeBag, bagWeight);
		checkoutStation.scan(bagItem);
	}
	
	
	

}
