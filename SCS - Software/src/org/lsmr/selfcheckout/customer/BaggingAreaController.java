package org.lsmr.selfcheckout.customer;

import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.ElectronicScale;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.SimulationException;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.ElectronicScaleObserver;

public class BaggingAreaController {

	private final SelfCheckoutStation checkoutStation;
	private BAC bac;
	private double weightOfCart;
	private ScanItemController scanItemControl;
	private int numOfItemsInBaggingArea;
	private double previousWeightOfCart;
	private long begin;
	
	
	//Constructor
	public BaggingAreaController(SelfCheckoutStation cs) {
		
		checkoutStation = cs;
		bac = new BAC();
		weightOfCart = 0;
		this.scanItemControl = null;
		numOfItemsInBaggingArea =0;
		
		
		//Register observers to the scanner
		checkoutStation.baggingArea.attach(bac);
		
	}
	
	//Connect bagging area control to scan item control
	public void setScanItemControl(ScanItemController sIController)
	{
		this.scanItemControl = sIController;
	}
	
	public int getNumOfItemsInBaggingArea() {
		return numOfItemsInBaggingArea;
	}
	
	public void getBeginTime(long begin) {
		this.begin = begin;
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
			if(scanItemControl.getWeightOfCart() == weightOfCart) {
				checkoutStation.mainScanner.enable();
				checkoutStation.handheldScanner.enable();
			}else {
				checkoutStation.mainScanner.disable();
				checkoutStation.handheldScanner.disable();
			}
			
			long end = System.currentTimeMillis();
			if (end - begin > 5000) {
				throw new SimulationException("Fail to place the item in the bagging area within the required time");
			}	
		}

		//Disable bar code scanner
		@Override
		public void overload(ElectronicScale scale) {
			checkoutStation.mainScanner.disable();
			checkoutStation.handheldScanner.disable();
		}

		//Enable bar code scanner
		@Override
		public void outOfOverload(ElectronicScale scale) {
			checkoutStation.mainScanner.enable();	
			checkoutStation.handheldScanner.enable();
		}
	}
	
	
	public double getWeightOfCart() {
		return weightOfCart;
	}

}