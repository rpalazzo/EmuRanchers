/* Copyright Robert Palazzo 2015
 * Copying this propritary source code without express
 * written permission from the author is prohibited.
 */

package com.rpalazzo.emuranchers;

public class ScoreBlob {
	
	public String paymentCards;
	public String profitCards;
	public int numberAcesAndCrowns;
	public int profit;
	
	
	public ScoreBlob() {
		super();
	}
	
	/*public ScoreBlob(String paymentCards, String profitCards, int numberAcesAndCrowns, int profit) {
		//super();
		this.paymentCards = paymentCards;
		this.profitCards = profitCards;
		this.numberAcesAndCrowns = numberAcesAndCrowns;
		this.profit = profit;
	}*/
	
	public ScoreBlob(String paymentCards, String profitCards, int profit) {
		//super();
		this.paymentCards = paymentCards;
		this.profitCards = profitCards;
		this.profit = profit;
	}
	
	public String getPaymentCards() {
		return paymentCards;
	}
	public void setPaymentCards(String paymentCards) {
		this.paymentCards = paymentCards;
	}
	public String getProfitCards() {
		return profitCards;
	}
	public void setProfitCards(String profitCards) {
		this.profitCards = profitCards;
	}
	public int getNumberAcesAndCrowns() {
		return numberAcesAndCrowns;
	}
	public void setNumberAcesAndCrowns(int numberAcesAndCrowns) {
		this.numberAcesAndCrowns = numberAcesAndCrowns;
	}
	public int getProfit() {
		return profit;
	}
	public void setProfit(int profit) {
		this.profit = profit;
	}
}
