/* Copyright Robert Palazzo 2015
 * Copying this propritary source code without express
 * written permission from the author is prohibited.
 */

package com.rpalazzo.emuranchers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import android.util.Log;

public class Emu<E> extends ArrayList<E> {
	
	
	private static final int GROWING_UP = 1;
	private static final int GROWING_DOWN = -1;
	private static final int GROWING_UNDETERMINED = 0;	
	private int growthDirection = GROWING_UNDETERMINED;
	
	private static final int WILD_CARD = -1;
	
		
	private int validSuits = DecktetCard.MOONS | DecktetCard.SUNS |
							 DecktetCard.WAVES | DecktetCard.LEAVES | 
							 DecktetCard.WYRMS | DecktetCard.KNOTS;
	
	
	//private static final int NO_RANK = -1;
	private int mostRecentRank; // = NO_RANK;
	
	private int requiredGaps = 0;
	private boolean hasAce = false;
	private boolean hasCrown = false;
	
	private static final int EMU_COST = 18;
	private int sumInStack = 0;
	private Stack<Integer> tempStack = new Stack<Integer>();
	private Stack<Integer> mostProfitableStack = new Stack<Integer>();
	
	private static final int SIZE_OF_SCORE_ARRAY = 10;
	private int[] ScoreArray = new int[SIZE_OF_SCORE_ARRAY]; // [A,2,3,4,5,6,7,8,9,C] 
	
	private HashMap hashMap;
	
	
	// Errors
	public static final int NO_ERROR = 0;
	public static final int ERROR_SUIT_MATCH = 1;
	public static final int ERROR_WILD_HATCH = 2;
	public static final int ERROR_TOO_SMALL = 3;
	public static final int ERROR_TOO_BIG = 4;
	public static final int ERROR_DUP_RANK = 5;
	public static final int ERROR_DUP_WILD = 6;
	public static final int ERROR_EOY_HATCH = 7;
	

	
	public Emu() {
		super();
		Log.i("Emu:", "Emu()");
		hashMap = new HashMap();
		populateHashMap();
	}

	/*
	@Override
	public boolean add(E e) {  //TODO: comment out this overriden function; it is used only for debugging setState()
		boolean retVal = super.add(e);
		Log.e("Emu:", "add() - should not call outside of debugging");
		//setState();
		return retVal;
	}
	*/
	
	public int isValidCard(E e) {

		Log.i("Emu:", "Starting isValidCard()");
		
		DecktetCard dc = (DecktetCard) e;
		int suits = dc.getSuits();
		int rank = dc.getRank();
		// NB: do not use ranks of PAWNS(10), COURTS(11), or CROWNS(12) for relative rank comparisons
		
		
		// Change temp values as we progress thru function.
		// Only if we pass thru with no errors, do we update member variables
		int tempGrowthDirection = growthDirection;
		int tempMostRecentRank = mostRecentRank;
		int tempRequiredGaps = requiredGaps;
		int tempValidSuits = validSuits;
		
		
		if (this.size() == 0) {
			if (rank == DecktetCard.COURT || rank == DecktetCard.PAWN || dc.isExcuse(dc)) {
				// Pawns or Courts may not be played to hatch a bird.  Any other card is valid to hatch.
				// The Excuse may never be played in a stack, so prevent hatch here and suit match will prevent subsequent plays.
				return ERROR_WILD_HATCH;
			}
			else { //not wild
				tempValidSuits = suits;
				tempMostRecentRank = rank;  //override below for rank = CROWN
				tempRequiredGaps = 0;
				
				if (rank == DecktetCard.CROWN){
					tempGrowthDirection = GROWING_DOWN;
					tempMostRecentRank = 10;
				}
				else if (rank == DecktetCard.ACE) {
					tempGrowthDirection = GROWING_UP;
				}
			}
		}
		
		else { // size != 0
			
			// CHECK FOR VALID SUIT(s)
			if ((suits & validSuits) > 0) {
				tempValidSuits = suits & validSuits;
			}
			else return ERROR_SUIT_MATCH;
			
			
			// CHECK FOR VALID RANK
			switch (growthDirection) {
				case GROWING_UP:
					if (rank == DecktetCard.COURT || rank == DecktetCard.PAWN) { // wild
						if (mostRecentRank + requiredGaps >= 9) { return ERROR_DUP_WILD; } // e.g., case of 5 then PAWN; next card must be <4 or >6 (requiredGap = 1).  With two PAWNs required gap is 2. 
						else { tempRequiredGaps += 1;	}
					}
					else { // not wild
						if (rank <= mostRecentRank + requiredGaps) { return ERROR_TOO_SMALL; } // can always play a CROWN
						else { 
							if (rank == DecktetCard.CROWN) {
								tempMostRecentRank = 10;
							}
							else {
								tempMostRecentRank = rank;
							}
							tempRequiredGaps = 0;
						}
					}
					break;
				case GROWING_DOWN:
					if (rank == DecktetCard.COURT || rank == DecktetCard.PAWN) { // wild
						if (mostRecentRank - requiredGaps <= 2) { return  ERROR_DUP_WILD; }
						else { tempRequiredGaps += 1; } 
					}
					else { // not wild
						if (rank >= mostRecentRank - requiredGaps) { return ERROR_TOO_BIG; }
						else { 
							tempMostRecentRank = rank;
							tempRequiredGaps = 0;
						}
					}
					break;
				case GROWING_UNDETERMINED:
					if (rank == DecktetCard.COURT || rank == DecktetCard.PAWN) { // wild
						/*  As long as growingDirection is undetermined,
						 *  it is always possible to play at least 4
						 *  (the max available in any one suit) wild cards.
						 *  e.g.,  5 * * * * CROWN, 6 * * * * ACE
						 */
						tempRequiredGaps += 1;
					}
					else { // not wild
						if (rank > mostRecentRank + requiredGaps) {
							tempGrowthDirection = GROWING_UP;
						}
						else if (rank < mostRecentRank - requiredGaps) {
							tempGrowthDirection = GROWING_DOWN;
						}
						else { // rank == mostRecentRank
							return ERROR_DUP_RANK;
						}
						tempMostRecentRank = rank; //override below for Crown
						tempRequiredGaps = 0;
						
						if (rank == DecktetCard.CROWN) {
							tempMostRecentRank = 10;
						}
					}
					break;
			}
		}	
		
		//Made it this far without returning an error, OK to now change member variables 
		growthDirection = tempGrowthDirection;
		mostRecentRank = tempMostRecentRank;
		requiredGaps = tempRequiredGaps ;
		validSuits = tempValidSuits;	
		
		Log.i("Emu:", "Exiting isValidCard()");
		
		return NO_ERROR;
	}
	
	public void setState() {
		/*
		 * This function iterates through the ArrayList of cards setting the following states used in other functions
		 *  1. ScoreArray				x
		 *  2. growthDirection			x
		 *  3. validSuits				x
		 *  4. current requiredGaps		x
		 *  5. mostRecentRank			x
		 *  6. hasCrown					x
		 *  7. hasAce					x
		 *  
		 *  Use after deserialize to avoid having to serialize all this crap. 
		 *  Also called prior to checking the score.
		 *  It might have been more clear to have separate functions, 
		 *  but processing is so similar, they are combined.
		 */
		
		Log.i("Emu:", "Starting setState()");
		
		DecktetCard dc = new DecktetCard();
		int rank=0;
		
		for (int i = 0; i < this.size(); i++) {
			
			Log.v(" SetState():","TOP LOOP i = " + i);
			
			dc = (DecktetCard) this.get(i);
			rank = dc.getRank();
			
			//SET validSuits (depends on suit of card and validSuits)
			validSuits = dc.getSuits() & validSuits;
			if ((validSuits & DecktetCard.MOONS) == DecktetCard.MOONS)  { Log.v(" SetState():","validSuits = MOONS");}
			if ((validSuits & DecktetCard.SUNS) == DecktetCard.SUNS)  { Log.v(" SetState():","validSuits = SUNS");}
			if ((validSuits & DecktetCard.WAVES) == DecktetCard.WAVES)  { Log.v(" SetState():","validSuits = WAVES");}
			if ((validSuits & DecktetCard.LEAVES) == DecktetCard.LEAVES)  { Log.v(" SetState():","validSuits = LEAVES");}
			if ((validSuits & DecktetCard.WYRMS) == DecktetCard.WYRMS)  { Log.v(" SetState():","validSuits = WYRMS");}
			if ((validSuits & DecktetCard.KNOTS) == DecktetCard.KNOTS)  { Log.v(" SetState():","validSuits = KNOTS");}
			

			// SET hasAce, hasCrown, requiredGaps, ScoreArray, mostRecentRank (depends on rank)
			if (rank == DecktetCard.PAWN || rank == DecktetCard.COURT) { 
				ScoreArray[i] = WILD_CARD;
				requiredGaps += 1; 
				}
			else if (rank == DecktetCard.ACE) { 
				hasAce = true; 
				ScoreArray[i] = 1;
				mostRecentRank = 1;
				requiredGaps = 0;
				}
			else if (rank == DecktetCard.CROWN) { 
				hasCrown = true;
				ScoreArray[i] = 10;
				mostRecentRank = 10;
				requiredGaps = 0;
				}
			else { // 2-9
				ScoreArray[i] = rank;
				mostRecentRank = rank;
				requiredGaps = 0;
			}		

			
			Log.v(" SetState():","mostRecentRank before check = " + mostRecentRank);
			
			// SET growthDirection (depends on mostRecentRank, rank, **requiredGaps**)
			if (growthDirection == GROWING_UNDETERMINED) { 
				
				Log.v(" SetState():","previously GROWING_UNDETERMINED");
			
				if (rank == DecktetCard.CROWN) {
					growthDirection = GROWING_DOWN;  
					Log.v(" SetState():","GROWING_DOWN on Crown");
				}
				else if (rank == DecktetCard.PAWN || rank == DecktetCard.COURT) {
					if (mostRecentRank + requiredGaps > 9) {
						growthDirection = GROWING_DOWN;
						Log.v(" SetState():","GROWING_DOWN on Wild limit");
					}
					else if (mostRecentRank - requiredGaps < 2) {
						growthDirection = GROWING_UP;
						Log.v(" SetState():","GROWING_UP on Wild limit");
					}
					else {
						Log.v(" SetState():","Wild card does not change Growth Direction"+growthDirection);
					}
				}
				else if (rank > mostRecentRank) {
					growthDirection = GROWING_UP;
					Log.v(" SetState():","GROWING_UP");
				}
				else if (rank < mostRecentRank){
					growthDirection = GROWING_DOWN;
					Log.v(" SetState():","GROWING_DOWN");
				}
				
			}
			
			
		} // end TOP for loop

		Log.v(" SetState():","mostRecentRank = " + mostRecentRank);
				
		
		// Loop through ScoreArray replacing any WILD_CARD values		
		if (growthDirection == GROWING_DOWN) {
			for (int i=0; i < ScoreArray.length; i++) {
				if (ScoreArray[i] == WILD_CARD) { 
					if (i==0) {Log.wtf(" SetState():", "ScoreArray[0] is wild");}
					ScoreArray[i] = ScoreArray[i-1] - 1; //N.B. the first card cannot be wild so we can always index i-1
				}
			}
		}
		else { // if growthDirection is UP or UNDETERMINED go "backward" from high to low
			for (int i = ScoreArray.length - 1; i > 0; i--) {  
				//Log.v(" SetState():","i = " + i);
				if (ScoreArray[i] == WILD_CARD) {
					if (ScoreArray[i+1] == 0){
						ScoreArray[i] = 9; // if the top card is wild, assign it the highest rank (9)
					}
					else {
						ScoreArray[i] = ScoreArray[i+1] - 1;
					}
				}
			}
		}
		
		/*
		 *    index:  0  1  2  3  4  5  6  7
		 *    examp:  2  4  *  6  7  0  0  0
		 *    examp:  2  *  *  6  7  0  0  0
		 *    examp:  2  4  5  6  7  *  0  0
		 *    examp:  5  *  *  
		 *    examp:  9  *  0  0  0  0  0  0
		 */
		
		for (int i=0; i < ScoreArray.length; i++) {
			Log.v(" SetState():", "ScoreArray[" + i +"] = " + ScoreArray[i]);
		}
		
		Log.i("Emu:", "Exiting setState()");
	}
	
	public ScoreBlob getScoreBlob() {
		
		Log.i("Emu:", "Starting getScoreBlob()");
		
		setState();  // setState must be called first to populate ScoreArray[]
		
		if (arrayToString(ScoreArray) == "") {
			Log.i("Emu:", "Emu has no cards");
			return null;
		}
	
		int profit = 0;
		int gross = 0;
		int numBonusCards = 0;
		ScoreBlob blob = new ScoreBlob();
		
		for (int i = 0; i < ScoreArray.length; i++) {
			if (ScoreArray[i] > 1 && ScoreArray[i] < 10) {  // ignore Crowns and Aces
				gross += ScoreArray[i];
			}
		}
		Log.i("getScoreBlob:", "gross before adjustments = " + gross);
		
		if (gross < EMU_COST) {
			
			profit = gross - EMU_COST;
			
			blob.setPaymentCards(arrayToString(ScoreArray));
			blob.setProfitCards("" + profit);
			
			if (hasAce) {
				profit -= 5;
				numBonusCards += 1;
			}
			if (hasCrown) {
				profit -= 5;
				numBonusCards += 1;
			}
						
			blob.setNumberAcesAndCrowns(numBonusCards);
			blob.setProfit(profit);
			
			Log.i("getValue:", "Losing: profit after Ace/Crown adjustments = " + profit);
		}
		
		else { // gross >= EMU_COST
			
			Log.i("Emu", "ScoreArray[0] = " + ScoreArray[0]);
			
			blob = (ScoreBlob) hashMap.get(arrayToKey(ScoreArray));  //hashMap returns PaymentCards, ProfitCards, & Profit
			if (blob == null) {
				Log.e("Emu", "hashMap.get() returned NULL");
			}
			else {
				profit = blob.getProfit();
				
				if (hasAce) {
					profit += 5;
					numBonusCards += 1;
				}
				if (hasCrown) {
					profit += 5;
					numBonusCards += 1;
				}
				blob.setProfit(profit);
				blob.setNumberAcesAndCrowns(numBonusCards);
			}		
		}
		
		Log.i("Emu:", "Exiting getScoreBlob()");
		
		return blob;  
	}
	
	private int arrayToKey(int[] array) {
		
		Log.i("Emu:", "Starting arrayToKey()");
		
		int key = 0;
		
		for (int i = 0; i < array.length; i++) {
			switch(array[i]) {
			case 9:
				key += 90000000;
				break;
			case 8:
				key += 8000000;
				break;
			case 7:
				key += 700000;
				break;
			case 6: 
				key += 60000;
				break;
			case 5:
				key += 5000;
				break;
			case 4:
				key += 400;
				break;
			case 3:
				key += 30;
				break;
			case 2:
				key += 2;
				break;
			case 0:   // ignore 0
			case 1:   // ignore 1; aces are tracked in 'hasAce'
			case 10:  // ignore 10; crowns are tracked in 'hasCrown'
				break;
			default:
				Log.e("Emu:", "bad value in ScoreArray");
			}
			//key += Math.pow(10, i) * array[array.length - 1 - i];  // assumes zero place holders
		}
		
		Log.i("Emu:", "Exiting arrayToKey()");
		
		return key;
	}
	
	private String arrayToString(int[] array) {
		
		Log.i("Emu:", "Starting arrayToString()");
		
		String str = "";
		int rank = 0;
		
		for (int i = 0; i < array.length; i++) {
			rank = array[i];
			if (rank > 1 && rank < 10) {
				if (str.length() > 0) {
					str += ",";
				}
				str += rank;
			}
		}
				
		Log.i("Emu:", "Exiting arrayToString()");
		
		return str;
	}
	
	private void populateHashMap() {
		
		
		Log.i("Emu:", "Starting populateHashMap()");
		
		//ScoreBlob tempBlob = new ScoreBlob("9,7,2", "8,6,5,4,3", 26);
		//Log.w("Emu:", "tempBlob.getProfitCards = " + tempBlob.getProfitCards());
		//hashMap.put(98765432, (ScoreBlob) new ScoreBlob("9,7,2", "8,6,5,4,3", 26));
		//hashMap.put(98765432, tempBlob);
		hashMap.put(98765430, new ScoreBlob("9,7,2", "8,6,5,4,3", 26));
		hashMap.put(98765430, new ScoreBlob("9,6,3", "8,7,5,4", 24));
		hashMap.put(98765402, new ScoreBlob("9,7,2", "8,6,5,4", 23));
		hashMap.put(98765400, new ScoreBlob("9,5,4", "8,7,6", 21));
		hashMap.put(98765032, new ScoreBlob("9,7,2", "8,6,5,3", 22));
		hashMap.put(98765030, new ScoreBlob("9,6,3", "8,7,5", 20));
		hashMap.put(98765002, new ScoreBlob("9,7,2", "8,6,5", 19));
		hashMap.put(98765000, new ScoreBlob("7,6,5", "9,8", 17));
		hashMap.put(98760432, new ScoreBlob("9,7,2", "8,6,4,3", 21));
		hashMap.put(98760430, new ScoreBlob("9,6,3", "8,7,4", 19));
		hashMap.put(98760402, new ScoreBlob("9,7,2", "8,6,4", 18));
		hashMap.put(98760400, new ScoreBlob("8,6,4", "9,7", 16));
		hashMap.put(98760032, new ScoreBlob("9,7,2", "8,6,3", 17));
		hashMap.put(98760030, new ScoreBlob("9,6,3", "8,7", 15));
		hashMap.put(98760002, new ScoreBlob("9,7,2", "8,6", 14));
		hashMap.put(98760000, new ScoreBlob("8,7,6", "9", 9));
		hashMap.put(98705432, new ScoreBlob("9,7,2", "8,5,4,3", 20));
		hashMap.put(98705430, new ScoreBlob("9,5,4", "8,7,3", 18));
		hashMap.put(98705402, new ScoreBlob("9,7,2", "8,5,4", 17));
		hashMap.put(98705400, new ScoreBlob("9,5,4", "8,7", 15));
		hashMap.put(98705032, new ScoreBlob("9,7,2", "8,5,3", 16));
		hashMap.put(98705030, new ScoreBlob("8,7,3", "9,5", 14));
		hashMap.put(98705002, new ScoreBlob("9,7,2", "8,5", 13));
		hashMap.put(98705000, new ScoreBlob("8,7,5", "9", 9));
		hashMap.put(98700432, new ScoreBlob("9,7,2", "8,4,3", 15));
		hashMap.put(98700430, new ScoreBlob("8,7,3", "9,4", 13));
		hashMap.put(98700402, new ScoreBlob("9,7,2", "8,4", 12));
		hashMap.put(98700400, new ScoreBlob("8,7,4", "9", 9));
		hashMap.put(98700032, new ScoreBlob("9,7,2", "8,3", 11));
		hashMap.put(98700030, new ScoreBlob("8,7,3", "9", 9));
		hashMap.put(98700002, new ScoreBlob("9,7,2", "8", 8));
		hashMap.put(98700000, new ScoreBlob("9,8,7", "", 0));
		hashMap.put(98065432, new ScoreBlob("9,6,3", "8,5,4,2", 19));
		hashMap.put(98065430, new ScoreBlob("9,6,3", "8,5,4", 17));
		hashMap.put(98065402, new ScoreBlob("9,5,4", "8,6,2", 16));
		hashMap.put(98065400, new ScoreBlob("9,5,4", "8,6", 14));
		hashMap.put(98065032, new ScoreBlob("9,6,3", "8,5,2", 15));
		hashMap.put(98065030, new ScoreBlob("9,6,3", "8,5", 13));
		hashMap.put(98065002, new ScoreBlob("9,8,2", "6,5", 11));
		hashMap.put(98065000, new ScoreBlob("8,6,5", "9", 9));
		hashMap.put(98060432, new ScoreBlob("9,6,3", "8,4,2", 14));
		hashMap.put(98060430, new ScoreBlob("9,6,3", "8,4", 12));
		hashMap.put(98060402, new ScoreBlob("8,6,4", "9,2", 11));
		hashMap.put(98060400, new ScoreBlob("8,6,4", "9", 9));
		hashMap.put(98060032, new ScoreBlob("9,6,3", "8,2", 10));
		hashMap.put(98060030, new ScoreBlob("9,6,3", "8", 8));
		hashMap.put(98060002, new ScoreBlob("9,8,2", "6", 6));
		hashMap.put(98060000, new ScoreBlob("9,8,6", "", 0));
		hashMap.put(98005432, new ScoreBlob("9,5,4", "8,3,2", 13));
		hashMap.put(98005430, new ScoreBlob("9,5,4", "8,3", 11));
		hashMap.put(98005402, new ScoreBlob("9,5,4", "8,2", 10));
		hashMap.put(98005400, new ScoreBlob("9,5,4", "8", 8));
		hashMap.put(98005032, new ScoreBlob("8,5,3,2", "9", 9));
		hashMap.put(98005030, new ScoreBlob("9,8,3", "5", 5));
		hashMap.put(98005002, new ScoreBlob("9,8,2", "5", 5));
		hashMap.put(98005000, new ScoreBlob("9,8,5", "", 0));
		hashMap.put(98000432, new ScoreBlob("9,4,3,2", "8", 8));
		hashMap.put(98000430, new ScoreBlob("9,8,3", "4", 4));
		hashMap.put(98000402, new ScoreBlob("9,8,2", "4", 4));
		hashMap.put(98000400, new ScoreBlob("9,8,4", "", 0));
		hashMap.put(98000032, new ScoreBlob("9,8,2", "3", 3));
		hashMap.put(98000030, new ScoreBlob("9,8,3", "", 0));
		hashMap.put(98000002, new ScoreBlob("9,8,2", "", 0));
		hashMap.put(90765432, new ScoreBlob("9,7,2", "6,5,4,3", 18));
		hashMap.put(90765430, new ScoreBlob("9,6,3", "7,5,4", 16));
		hashMap.put(90765402, new ScoreBlob("9,7,2", "6,5,4", 15));
		hashMap.put(90765400, new ScoreBlob("9,5,4", "7,6", 13));
		hashMap.put(90765032, new ScoreBlob("9,7,2", "6,5,3", 14));
		hashMap.put(90765030, new ScoreBlob("9,6,3", "7,5", 12));
		hashMap.put(90765002, new ScoreBlob("9,7,2", "6,5", 11));
		hashMap.put(90765000, new ScoreBlob("7,6,5", "9", 9));
		hashMap.put(90760432, new ScoreBlob("9,7,2", "6,4,3", 13));
		hashMap.put(90760430, new ScoreBlob("9,6,3", "7,4", 11));
		hashMap.put(90760402, new ScoreBlob("9,7,2", "6,4", 10));
		hashMap.put(90760400, new ScoreBlob("9,6,4", "7", 7));
		hashMap.put(90760032, new ScoreBlob("7,6,3,2", "9", 9));
		hashMap.put(90760030, new ScoreBlob("9,6,3", "7", 7));
		hashMap.put(90760002, new ScoreBlob("9,7,2", "6", 6));
		hashMap.put(90760000, new ScoreBlob("9,7,6", "", 0));
		hashMap.put(90705432, new ScoreBlob("9,7,2", "5,4,3", 12));
		hashMap.put(90705430, new ScoreBlob("9,5,4", "7,3", 10));
		hashMap.put(90705402, new ScoreBlob("9,7,2", "5,4", 9));
		hashMap.put(90705400, new ScoreBlob("9,5,4", "7", 7));
		hashMap.put(90705032, new ScoreBlob("9,7,2", "5,3", 8));
		hashMap.put(90705030, new ScoreBlob("9,7,3", "5", 5));
		hashMap.put(90705002, new ScoreBlob("9,7,2", "5", 5));
		hashMap.put(90705000, new ScoreBlob("9,7,5", "", 0));
		hashMap.put(90700432, new ScoreBlob("9,7,2", "4,3", 7));
		hashMap.put(90700430, new ScoreBlob("9,7,3", "4", 4));
		hashMap.put(90700402, new ScoreBlob("9,7,2", "4", 4));
		hashMap.put(90700400, new ScoreBlob("9,7,4", "", 0));
		hashMap.put(90700032, new ScoreBlob("9,7,2", "3", 3));
		hashMap.put(90700030, new ScoreBlob("9,7,3", "", 0));
		hashMap.put(90700002, new ScoreBlob("9,7,2", "", 0));
		hashMap.put(90065432, new ScoreBlob("9,6,3", "5,4,2", 11));
		hashMap.put(90065430, new ScoreBlob("9,6,3", "5,4", 9));
		hashMap.put(90065402, new ScoreBlob("9,5,4", "6,2", 8));
		hashMap.put(90065400, new ScoreBlob("9,5,4", "6", 6));
		hashMap.put(90065032, new ScoreBlob("9,6,3", "5,2", 7));
		hashMap.put(90065030, new ScoreBlob("9,6,3", "5", 5));
		hashMap.put(90065002, new ScoreBlob("9,6,5", "2", 2));
		hashMap.put(90065000, new ScoreBlob("9,6,5", "", 0));
		hashMap.put(90060432, new ScoreBlob("9,6,3", "4,2", 6));
		hashMap.put(90060430, new ScoreBlob("9,6,3", "4", 4));
		hashMap.put(90060402, new ScoreBlob("9,6,4", "2", 2));
		hashMap.put(90060400, new ScoreBlob("9,6,4", "", 0));
		hashMap.put(90060032, new ScoreBlob("9,6,3", "2", 2));
		hashMap.put(90060030, new ScoreBlob("9,6,3", "", 0));
		hashMap.put(90005432, new ScoreBlob("9,5,4", "3,2", 5));
		hashMap.put(90005430, new ScoreBlob("9,5,4", "3", 3));
		hashMap.put(90005402, new ScoreBlob("9,5,4", "2", 2));
		hashMap.put(90005400, new ScoreBlob("9,5,4", "", 0));
		hashMap.put(90005032, new ScoreBlob("9,5,3,2", "", 0));
		hashMap.put(90000432, new ScoreBlob("9,4,3,2", "", 0));
		hashMap.put(8765432, new ScoreBlob("8,7,3", "6,5,4,2", 17));
		hashMap.put(8765430, new ScoreBlob("8,7,3", "6,5,4", 15));
		hashMap.put(8765402, new ScoreBlob("8,6,4", "7,5,2", 14));
		hashMap.put(8765400, new ScoreBlob("8,6,4", "7,5", 12));
		hashMap.put(8765032, new ScoreBlob("8,5,3,2", "7,6", 13));
		hashMap.put(8765030, new ScoreBlob("8,7,3", "6,5", 11));
		hashMap.put(8765002, new ScoreBlob("7,6,5", "8,2", 10));
		hashMap.put(8765000, new ScoreBlob("7,6,5", "8", 8));
		hashMap.put(8760432, new ScoreBlob("8,7,3", "6,4,2", 12));
		hashMap.put(8760430, new ScoreBlob("8,7,3", "6,4", 10));
		hashMap.put(8760402, new ScoreBlob("8,6,4", "7,2", 9));
		hashMap.put(8760400, new ScoreBlob("8,6,4", "7", 7));
		hashMap.put(8760032, new ScoreBlob("7,6,3,2", "8", 8));
		hashMap.put(8760030, new ScoreBlob("8,7,3", "6", 6));
		hashMap.put(8760002, new ScoreBlob("8,7,6", "2", 2));
		hashMap.put(8760000, new ScoreBlob("8,7,6", "", 0));
		hashMap.put(8705432, new ScoreBlob("8,7,3", "5,4,2", 11));
		hashMap.put(8705430, new ScoreBlob("8,7,3", "5,4", 9));
		hashMap.put(8705402, new ScoreBlob("7,5,4,2", "8", 8));
		hashMap.put(8705400, new ScoreBlob("8,7,4", "5", 5));
		hashMap.put(8705032, new ScoreBlob("8,7,3", "5,2", 7));
		hashMap.put(8705030, new ScoreBlob("8,7,3", "5", 5));
		hashMap.put(8705002, new ScoreBlob("8,7,5", "2", 2));
		hashMap.put(8705000, new ScoreBlob("8,7,5", "", 0));
		hashMap.put(8700432, new ScoreBlob("8,7,3", "4,2", 6));
		hashMap.put(8700430, new ScoreBlob("8,7,3", "4", 4));
		hashMap.put(8700402, new ScoreBlob("8,7,4", "2", 2));
		hashMap.put(8700400, new ScoreBlob("8,7,4", "", 0));
		hashMap.put(8700032, new ScoreBlob("8,7,3", "2", 2));
		hashMap.put(8700030, new ScoreBlob("8,7,3", "", 0));
		hashMap.put(8065432, new ScoreBlob("8,6,4", "5,3,2", 10));
		hashMap.put(8065430, new ScoreBlob("8,6,", "5,3", 8));
		hashMap.put(8065402, new ScoreBlob("8,6,4", "5,2", 7));
		hashMap.put(8065400, new ScoreBlob("8,6,4", "5", 5));
		hashMap.put(8065032, new ScoreBlob("8,5,3,2", "6", 6));
		hashMap.put(8065030, new ScoreBlob("8,6,5", "3", 3));
		hashMap.put(8065002, new ScoreBlob("8,6,5", "2", 2));
		hashMap.put(8065000, new ScoreBlob("8,6,5", "", 0));
		hashMap.put(8060432, new ScoreBlob("8,6,4", "3,2", 5));
		hashMap.put(8060430, new ScoreBlob("8,6,4", "3", 3));
		hashMap.put(8060402, new ScoreBlob("8,6,4", "2", 2));
		hashMap.put(8060400, new ScoreBlob("8,6,4", "", 0));
		hashMap.put(8060032, new ScoreBlob("8,6,3,2", "", 0));
		hashMap.put(8005432, new ScoreBlob("8,5,3,2", "4", 4));
		hashMap.put(8005430, new ScoreBlob("8,5,4,3", "", 0));
		hashMap.put(8005402, new ScoreBlob("8,5,4,2", "", 0));
		hashMap.put(8005032, new ScoreBlob("8,5,3,2", "", 0));
		hashMap.put(765432, new ScoreBlob("7,6,5", "4,3,2", 9));
		hashMap.put(765430, new ScoreBlob("7,6,5", "4,3", 7));
		hashMap.put(765402, new ScoreBlob("7,6,5", "4,2", 6));
		hashMap.put(765400, new ScoreBlob("7,6,5", "4", 4));
		hashMap.put(765032, new ScoreBlob("7,6,5", "3,2", 5));
		hashMap.put(765030, new ScoreBlob("7,6,5", "3", 3));
		hashMap.put(765002, new ScoreBlob("7,6,5", "2", 2));
		hashMap.put(765000, new ScoreBlob("7,6,5", "", 0));
		hashMap.put(760432, new ScoreBlob("7,6,3,2", "4", 4));
		hashMap.put(760430, new ScoreBlob("7,6,4,3", "", 0));
		hashMap.put(760402, new ScoreBlob("7,6,4,2", "", 0));
		hashMap.put(760032, new ScoreBlob("7,6,3,2", "", 0));
		hashMap.put(705432, new ScoreBlob("7,5,4,2", "3", 3));
		hashMap.put(705430, new ScoreBlob("7,5,4,3", "", 0));
		hashMap.put(705402, new ScoreBlob("7,5,4,2", "", 0));
		hashMap.put(65432, new ScoreBlob("6,5,4,3", "2", 2));
		hashMap.put(65430, new ScoreBlob("6,5,4,3", "", 0));
		Log.i("Emu:", "Exiting populateHashMap()");
	}
	
	/*
    public void populateSubset(int[] data, int fromIndex, int endIndex) {

    	// http://codereview.stackexchange.com/questions/36214/find-all-subsets-of-an-int-array-whose-sums-equal-a-given-target
    	Log.i("populateSubset:", "size data: " + data.length + " from: " + fromIndex + " to: " + endIndex);
    	
        if (sumInStack >= EMU_COST) {
            record(tempStack);
        }

        for (int currentIndex = fromIndex; currentIndex < endIndex; currentIndex++) {

            if (sumInStack + data[currentIndex] <= EMU_COST) {
                tempStack.push(data[currentIndex]);
                sumInStack += data[currentIndex];

                populateSubset(data, currentIndex + 1, endIndex);
                sumInStack -= (Integer) tempStack.pop();
            }
        }
    }
    
    private void record(Stack<Integer> stack) {
    	StringBuilder sb = new StringBuilder(255);
    	while (!stack.isEmpty()) {
    		sb.append(stack.pop());
    		sb.append(" ");
    	}
   
    	Log.i(" SetState():", "Profitable stack = " + sb);
    	
    }
    
    private int stackSum(Stack<Integer> stack) {
    	int sum = 0;
    	while (!stack.isEmpty()){
    		sum += stack.pop();
    	}
    	return sum;
    }
	*/
}
