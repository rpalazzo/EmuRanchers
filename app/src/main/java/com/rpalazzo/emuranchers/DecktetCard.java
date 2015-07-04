/* Copyright Robert Palazzo 2015
 * Copying this propritary source code without express
 * written permission from the author is prohibited.
 */

package com.rpalazzo.emuranchers;
import android.graphics.Bitmap;


public class DecktetCard {
	
	// The rank and suit of each Decktet card is encoded in an integer.
	
	// The least significant byte is the rank.
	// Rank of PAWN, COURT, CROWN specified at http://wiki.decktet.com/the-extended-deck
	public static final int ACE		= 0x00000001;
	// 						  2		= 0x00000002
	// 						  3		= 0x00000003
	// 						...
	// 						  9		= 0x00000009
	public static final int PAWN	= 0x0000000A;
	public static final int COURT	= 0x0000000B;
	public static final int CROWN	= 0x0000000C;

	// The middle 6 bytes are used as a bitmap representing suits.
	// Suit order specified at http://wiki.decktet.com/structure.
	public static final int MOONS	= 0x01000000;
	public static final int SUNS	= 0x00100000;
	public static final int WAVES	= 0x00010000;
	public static final int LEAVES	= 0x00001000;
	public static final int WYRMS	= 0x00000100;
	public static final int KNOTS	= 0x00000010;
	
	// The high order byte for the Excuse
	public static final int EXCUSE	= 0x10000000;
	

	private static final int RANK_MASK = 0x0000000F;
	private static final int SUIT_MASK = 0x0FFFFFF0;
	
	private int id;
	private Bitmap bmp;
	
	public DecktetCard() {
		super();
	}
	
	public DecktetCard(int id, Bitmap bmp) {
		super();
		this.id = id;
		this.bmp = bmp;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Bitmap getBmp() {
		return bmp;
	}

	public void setBmp(Bitmap bmp) {
		this.bmp = bmp;
	}

	public int getRank() {
		return  (this.id & RANK_MASK);
	}
	
	public int getSuits() {
		return (this.id & SUIT_MASK);
	}
	
	public String getString() {
		String retString = "";
		switch (this.getId() & RANK_MASK) {
		case ACE:
			retString = "Ace "; //TODO: use resource? -- currently only called by LogCards (not seen by user)
			break;
		case PAWN:
			retString = "Pawn ";
			break;
		case COURT:
			retString = "Court ";
			break;
		case CROWN:
			retString = "Crown ";
			break;
		case 0: 
			retString = "Excuse";
			break;
		default:
			retString = Integer.toString(this.getId() & RANK_MASK);	
			retString += " ";
		}
		
		
		if (isMoon()) {
			retString += "Moons "; //TODO: use resource? -- currently only called by LogCards (not seen by user)
		}
		if (isSun()) {
			retString += "Suns ";
		}
		if (isWave()) {
			retString += "Waves ";
		}
		if (isLeaf()) {
			retString += "Leaves ";
		}
		if (isWyrm()) {
			retString += "Wyrms ";
		}
		if (isKnot()) {
			retString += "Knots ";
		}
		
		return retString;
	}
	
	public boolean isMoon() {
		if ((this.id & MOONS) == MOONS)
			return true;
		else 
			return false;
	}
	
	public boolean isSun() {
		if ((this.id & SUNS) == SUNS)
			return true;
		else 
			return false;
	}
	
	public boolean isWave() {
		if ((this.id & WAVES) == WAVES)
			return true;
		else 
			return false;
	}
	
	public boolean isLeaf() {
		if ((this.id & LEAVES) == LEAVES)
			return true;
		else 
			return false;
	}
	
	public boolean isWyrm() {
		if ((this.id & WYRMS) == WYRMS)
			return true;
		else 
			return false;
	}
	
	public boolean isKnot() {
		if ((this.id & KNOTS) == KNOTS)
			return true;
		else 
			return false;
	}
	
	public boolean isExcuse(DecktetCard card) {
		if (card.id == EXCUSE)
			return true;
		else 
			return false;
	}
}
