package org.lsmr.selfcheckout.customer;


import java.math.BigDecimal;
import java.util.HashMap;

import org.lsmr.selfcheckout.Barcode;
import org.lsmr.selfcheckout.devices.AbstractDevice;
import org.lsmr.selfcheckout.devices.ReceiptPrinter;
import org.lsmr.selfcheckout.devices.SelfCheckoutStation;
import org.lsmr.selfcheckout.devices.observers.AbstractDeviceObserver;
import org.lsmr.selfcheckout.devices.observers.ReceiptPrinterObserver;

public class ReceiptPrinterController {

	private final SelfCheckoutStation checkoutStation;
	private RPC rpc;
	private ScanItemController scanControl;
	private PaymentController payControl;
	private HashMap<Barcode, BigDecimal> barcodePrice;
	private HashMap<Barcode, String> barcodeDescription;
	private String receiptMessage;
	
	public ReceiptPrinterController(SelfCheckoutStation cs, HashMap<Barcode, BigDecimal> barcodePrice,
			HashMap<Barcode, String> barcodeDescription) {
		
		checkoutStation = cs;
		rpc = new RPC();
		this.scanControl = null;
		this.payControl = null;
		this.barcodePrice = barcodePrice;
		this.barcodeDescription = barcodeDescription;
		receiptMessage = "";
		
		//Add ink and paper to the printer
		checkoutStation.printer.addPaper(8);
		checkoutStation.printer.addInk(15);
	
		//Register observers to the scanner
		checkoutStation.printer.attach(rpc);
		
	}
	
	//Calls the printer to print item description and price
	public void printReceipt() {
		receiptMessage = "";
		for(Barcode barcode: scanControl.getScannedItemList()) {
			receiptMessage = receiptMessage +
					barcodeDescription.get(barcode) + " " 
					+ barcodePrice.get(barcode) + "\n";
		}
		receiptMessage = receiptMessage + "Total Price: " + payControl.getInitialValueOfCart();
		
		if(payControl.hasMembership()) {
			receiptMessage = receiptMessage + "\n\n" + payControl.getMembershipNo();
		}
	
		for(int i =0; i < receiptMessage.length(); i++) {
			checkoutStation.printer.print(receiptMessage.charAt(i));		
		}
	}
	
	//Returns the receipts message
	public String getReceipt() {
		return receiptMessage;
	}
	
	//Connect Receipt Printer controller with other controllers
	public void setControllers(ScanItemController scanControl,PaymentController payControl ) {
		this.scanControl = scanControl;
		this.payControl = payControl;
	}
	
	
	public class RPC implements ReceiptPrinterObserver {

		@Override
		public void enabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore
			
		}

		@Override
		public void disabled(AbstractDevice<? extends AbstractDeviceObserver> device) {
			// Ignore
			
		}

		@Override
		public void outOfPaper(ReceiptPrinter printer) {
			checkoutStation.printer.addPaper(8);
		}

		@Override
		public void outOfInk(ReceiptPrinter printer) {
			checkoutStation.printer.addInk(15);
		}

		@Override
		public void paperAdded(ReceiptPrinter printer) {
			// Ignore
			
		}

		@Override
		public void inkAdded(ReceiptPrinter printer) {
			// Ignore
			
		}
			
	}
}
