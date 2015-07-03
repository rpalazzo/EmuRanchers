/* Copyright Robert Palazzo 2015
 * Copying this propritary source code without express
 * written permission from the author is prohibited.
 */

package com.rpalazzo.emuranchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class PlayView extends View {
	
	// screen, card, and buffer sizes
	private int screenW;
	private int screenH;
	private int scaledCardW;
	private int scaledCardH;
	private int bufferX;
	private int bufferY;
	
	//Screen density scaling
	private float scale;
	private Paint paintColor;
	
	// x-y coordinate positions
	private int[] columnXCoord = new int[6];
	private int cpuAreaYCoord;
	private int discardAreaYCoord;   
	private int playerAreaYCoord;
	private int playerHandYCoord;

	// groups of cards
	ArrayList<DecktetCard> drawPile;
	ArrayList<DecktetCard> playerHand;
	ArrayList<DecktetCard> cpuHand;
	ArrayList<DecktetCard> discardPile;
	ArrayList<Emu<DecktetCard>> playerEmus;
	ArrayList<Emu<DecktetCard>> cpuEmus;
	int lastEmuIndex;
	
	Bitmap cardBack;
	Bitmap cardTarget;
	
	private int movingCardIndex = -1;
	private int movingX;
	private int movingY;
	
	private final static int PLAYER_PLAY = 1;
	private final static int PLAYER_DRAW = 2;
	private final static int CPU_PLAY = 3;
	private final static int YEAR_END = 4;
	private int game_phase;
	
	private final static int UNDETERMINED_WHO_STARTS = 0;
	private final static int PLAYER_STARTS = 1;
	private final static int CPU_STARTS = 2;
	private int NextYearStarter = UNDETERMINED_WHO_STARTS;  
	
	private int numberYearsInGame = 4; 
	private int yearsPlayed = 0;
	
	private int playerScore = 0;
	private int cpuScore = 0;
	
	private Context context;
	
	public PlayView(Context c) {
		super(c);	
		Log.v("PlayView","Enter PlayView()");
		
		setBackgroundResource(R.drawable.greentexture);
		
		context = c;
		
		scale = context.getResources().getDisplayMetrics().density;
		paintColor = new Paint();
		paintColor.setAntiAlias(true);
		paintColor.setColor(Color.WHITE);
		paintColor.setStyle(Paint.Style.STROKE);
		paintColor.setTextAlign(Paint.Align.LEFT);
		paintColor.setTextSize(scale*12);
		
		//Get numberYearsInGame from settings
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String strNumberYearsInGame = sharedPref.getString("numberofyears_key", context.getString(R.string.numberofyears_default));
		numberYearsInGame = Integer.parseInt(strNumberYearsInGame);
				
	
		Log.v("PlayView","Exit PlayView()");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		//Log.v("PlayView","Enter onTouchEvent()");
		
		int eventaction = (int)event.getAction();
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		switch (eventaction) {
		
		case MotionEvent.ACTION_DOWN:
			if (game_phase == PLAYER_PLAY || game_phase == YEAR_END) {
				for (int i = 0; i < 6; i++) {
					if (inIthColumn(x,i) && inPlayerHand(y)) {
						if (playerHand.size() >= i+1) { // during YEAR_END size could be less than i
							movingCardIndex = i;
							movingX = x - scaledCardW/2;  
							movingY = y - scaledCardH/2;
							break;
						}
					}
				}
			}
			break;
			
		case MotionEvent.ACTION_MOVE:
			movingX = x - scaledCardW/2;  
			movingY = y - scaledCardH/2;
			break;
			
		case MotionEvent.ACTION_UP:
			
			// Add card to PlayersEmu
			if ( movingCardIndex > -1 && inPlayerArea(y)) {	
				int emuIndex = -1;
				for (int i=0; i <= 5; i++) {
					if (inIthColumn(x, i)) {
						emuIndex = i;
						break;
					}
				}			
				
				int check = Emu.NO_ERROR;
				
				if ( (game_phase == YEAR_END) && (playerEmus.get(emuIndex).size() == 0) ) {
					check = Emu.ERROR_EOY_HATCH;
				}
				else { 
					check = playerEmus.get(emuIndex).isValidCard(playerHand.get(movingCardIndex)); 
					if (check == Emu.NO_ERROR){
						playerEmus.get(emuIndex).add(playerHand.remove(movingCardIndex));
						lastEmuIndex = emuIndex;
						if (game_phase == PLAYER_PLAY) {
							game_phase = PLAYER_DRAW;
						}
						else if (playerHand.size() == 0) {// game_phase is YEAR_END
							CalculateScore();
						}
					}
				}
				if (check != Emu.NO_ERROR)  {
					ToastError(check);
				}
			}
			
			// Add card to DiscardPile
			else if ( movingCardIndex > -1 && inDiscardPile(x,y) ) {
				discardPile.add(playerHand.remove(movingCardIndex));
				if (game_phase == PLAYER_PLAY) {
					game_phase = PLAYER_DRAW;
				}
				else if (playerHand.size() == 0) {// game_phase is YEAR_END
					CalculateScore();
				}
			}
			
			// Rearrange Player Hand
			else if (movingCardIndex > -1 && inPlayerHand(y)) {
				for (int i = 0; i < playerHand.size(); i++) {
					if (inIthColumn(x, i)) {
						playerHand.add(i, playerHand.remove(movingCardIndex));
					}
				}
			}
			
			else if (game_phase == PLAYER_DRAW) {	
				if (inDrawPile(x,y)) {
						DrawCard(playerHand);
						game_phase = CPU_PLAY;
						CpuPlay();
				}
				else if (inDiscardPile(x,y) && discardPile.size() > 0) {
						playerHand.add(discardPile.remove(discardPile.size()-1));
						game_phase = CPU_PLAY;
						CpuPlay();
				}
			/*	else if (x > columnXCoord[4] && x < columnXCoord[4] + scaledCardW) { //TODO: button for Undo
					playerHand.add(playerEmus.get(lastEmuIndex).remove(playerEmus.get(lastEmuIndex).size()-1));
					game_phase = PLAYER_PLAY;
					/*
					 * TODO: Undo bugs:
					 *   1. Emu needs to unroll restrictions (rank or suit) based on last card. (Or remove convenicence state varaibles)
					 *   2. If last play was to discard, then undo will  
					 *
				}*/
			}
			movingCardIndex = -1;
			break;
		}		
		
		
		invalidate();
		//Log.v("PlayView","Exit onTouchEvent()");
		return true;
	}
	
	
	private boolean inDiscardPile(int x, int y) {
		if (x > columnXCoord[3] - bufferX/2 &&
			x < columnXCoord[3] + scaledCardW + bufferX/2 &&
			y > discardAreaYCoord - bufferY/2 &&
			y < discardAreaYCoord + scaledCardH + bufferY/2) {
			return true;
		}
		else return false;
	}
	
	private boolean inDrawPile(int x, int y) {
		if ( x > columnXCoord[2] - bufferX/2 && 
			 x < columnXCoord[2] + scaledCardW + bufferX/2  &&
			 y > discardAreaYCoord - bufferY/2 && 
			 y < discardAreaYCoord + scaledCardH + bufferY/2) {
			return true;
		}
		else return false;
	}
	
	private boolean inPlayerArea(int y) {
		if (y > playerAreaYCoord - bufferY/2 &&
			y < playerAreaYCoord + 2.8*scaledCardH + bufferY/2) {
			return true;
		}
		else return false;
	}
	
	private boolean inPlayerHand(int y) {
		if ((y > playerHandYCoord + bufferY/2) && 
			(y < playerHandYCoord + scaledCardH + bufferY/2)){
			return true;
		}
		else return false;
	}
	
	private boolean inIthColumn(int x, int i) {
		if ((x > i*(scaledCardW + bufferX) + bufferX/2) && 
			(x < ((i+1)*(scaledCardW + bufferX) + bufferX/2 ))) {
			return true;
		}
		else return false;
	}
	
	private void ToastError(int error) {
		String errorMsg = "";
		switch (error) {	
		
		case Emu.ERROR_DUP_RANK:
			errorMsg = getResources().getString(R.string.error_dup_rank);
			break;
		case Emu.ERROR_DUP_WILD:
			errorMsg = getResources().getString(R.string.error_dup_wild);
			break;
		case Emu.ERROR_SUIT_MATCH:
			errorMsg = getResources().getString(R.string.error_suit_match);
			break;
		case Emu.ERROR_TOO_BIG:
			errorMsg = getResources().getString(R.string.error_too_big);
			break;
		case Emu.ERROR_TOO_SMALL:
			errorMsg = getResources().getString(R.string.error_too_small);
			break;
		case Emu.ERROR_WILD_HATCH:
			errorMsg = getResources().getString(R.string.error_wild_hatch);
			break;
		case Emu.ERROR_EOY_HATCH:
			errorMsg = getResources().getString(R.string.error_eoy_hatch);
			break;	
		}
		
		Toast toast;	
		toast = Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 0, discardAreaYCoord);  //TODO: determine correct y-offset
		toast.show();
	}
	
	private void CalculateScore() {
		Log.v("PlayView:", "Starting CalculateScore()");
		
		int yearTotal = 0;
		
		ScoreBlob playerBlob = new ScoreBlob();
		String scoreString = new String();
				
		
		for (int i = 0; i < playerEmus.size(); i++) {
			
			String payCards = "";
			String profCards = "";
			int numBonusCards = 0;
			int profit = 0;
			
			playerBlob = playerEmus.get(i).getScoreBlob();
			if (playerBlob != null) {
				
				payCards = playerBlob.getPaymentCards();
				profCards = playerBlob.getProfitCards();
				numBonusCards = playerBlob.getNumberAcesAndCrowns();
				profit = playerBlob.getProfit();
			
				
				Log.i("PlayView:", "Emu[" + i + "]: Payment Cards: " + payCards);
				Log.i("PlayView:", "Emu[" + i + "]: Profit Cards: " + profCards);
				Log.i("PlayView:", "Emu[" + i + "]: Profit before bonus: " + profit);
				
				
				scoreString += "-: ";
				if (payCards.length() > 0) {
					scoreString += payCards;
				}
				else {
					scoreString += "0";
				}
				scoreString += "  +: ";
				scoreString += profCards;
				scoreString += "  ?: ";
				if ( profit < 0 ) {
					if (numBonusCards == 0 ){
						scoreString += "0";
					}
					else if (numBonusCards == 1 ){
						scoreString += "-5";
					}
					else if (numBonusCards == 2 ){
						scoreString += "-5,-5";
					}
				}
				else {
					if (numBonusCards == 0 ){
						scoreString += "0";
					}
					else if (numBonusCards == 1 ){
						scoreString += "+5";
					}
					else if (numBonusCards == 2 ){
						scoreString += "+5,+5";
					}
				}
				//scoreString += numBonusCards;
				scoreString += "  Tot: ";
				scoreString += profit;
				scoreString += "\n";
				
				yearTotal += profit;
			}
			 
		}
		
		playerScore += yearTotal;
		//cpuScore += cpuYearTotal;  //TODO:
		

		scoreString += "\n Total for year: ";
		scoreString += yearTotal;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Year Scoring Summary")//R.string.dialog_title);
			.setMessage("Your score: \n" + scoreString )//R.string.dialog_message)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id) {
		    		   // User clicked OK button
		    		 
		    		    
		    		   yearsPlayed += 1;
		    		   if (yearsPlayed < numberYearsInGame) {
		    			   StartNewYear();
		    		   }
		    		   else {
		    			   Toast toast;	
		    				toast = Toast.makeText(getContext(), "GAME OVER", Toast.LENGTH_LONG);
		    				toast.setGravity(Gravity.TOP, 0, discardAreaYCoord);  
		    				toast.show();
		    		   }
		    		   
		    		 
		    	   }
    	    });
		AlertDialog dialog = builder.create();
		
		Window window = dialog.getWindow();
	    window.setGravity(Gravity.TOP);
		
		dialog.show();
		Log.v("PlayView:", "Exitinging CalculateScore()");
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.v("PlayView","Enter onDraw()");
		
		// CPU's hand
		for (int i=0; i<cpuHand.size(); i++) {
//			canvas.drawBitmap(cardBack, (float) columnXCoord[0]+(5*i), (float) cpuAreaYCoord, null);
			canvas.drawBitmap(cpuHand.get(i).getBmp(), (float) columnXCoord[0], (float) (cpuAreaYCoord+scaledCardH*0.2*i), null);
		}
		
		// CPU's emus
		for (int i=0; i<cpuEmus.size(); i++) {
			int j=0;
			while (j < cpuEmus.get(i).size()) {
				canvas.drawBitmap(cpuEmus.get(i).get(j).getBmp(), 
						(float) columnXCoord[i], 
						(float)(cpuAreaYCoord + scaledCardH*0.2*j), null);
				j++;
			}
		}
	
		// Text data
		canvas.drawText("Computer score: " + Integer.toString(cpuScore), columnXCoord[0], 
				discardAreaYCoord + 1 * paintColor.getTextSize(), paintColor);
		canvas.drawText("Cards left: " + Integer.toString(drawPile.size()),	columnXCoord[0],
				discardAreaYCoord + 2 * paintColor.getTextSize(), paintColor);
		canvas.drawText("Year " + (yearsPlayed+1) + " of " + numberYearsInGame,	columnXCoord[0],
				discardAreaYCoord + 3 * paintColor.getTextSize(), paintColor);
		canvas.drawText("My score: " + Integer.toString(playerScore), columnXCoord[0],
				discardAreaYCoord + 4 * paintColor.getTextSize(), paintColor);  
						
		// Draw pile
		if (!drawPile.isEmpty()) {
			canvas.drawBitmap(cardBack, 
					(float) columnXCoord[2], 
					(float) discardAreaYCoord, null );
		}
		
		// Discard pile
		if (discardPile.isEmpty()) {
			canvas.drawBitmap(cardTarget, 
					(float) columnXCoord[3], 
					(float) discardAreaYCoord, null );
		}
		else {
			canvas.drawBitmap(discardPile.get(discardPile.size()-1).getBmp(), 
					(float) columnXCoord[3], 
					(float) discardAreaYCoord, null );
		}
		
		// Player's emus
		for (int i=0; i<playerEmus.size(); i++) {
			canvas.drawBitmap(cardTarget, columnXCoord[i], playerAreaYCoord, null);
		}
		for (int i=0; i<playerEmus.size(); i++) {
			int j=0;
			while (j < playerEmus.get(i).size()) {
				canvas.drawBitmap(playerEmus.get(i).get(j).getBmp(), 
						(float) columnXCoord[i], 
						(float)(playerAreaYCoord + scaledCardH*0.2*j), null);
				j++;
			}
		}
		
		// Player's hand (including moving cards)
		for (int i=0; i<playerHand.size(); i++) {
			if (i != movingCardIndex) {
				canvas.drawBitmap(playerHand.get(i).getBmp(), 
						(float) columnXCoord[i], 
						(float) playerHandYCoord, null);			
			}
		}
		if (movingCardIndex > -1) {
			canvas.drawBitmap(playerHand.get(movingCardIndex).getBmp(), movingX, movingY, null);
		}
		
		Log.v("PlayView","Exit onDraw()");
	}
	
	@Override
	public void onSizeChanged (int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		Log.v("PlayView","Enter onSizeChanged()");
		
		/* 
		 * Screen Vertical Layout
		 *  ---------
		 * |    bufferY
		 * |
		 * | cpuArea = 1 card high + up to 9 stacked cards showing only 20% = 1 + 9*0.2 = 2.8
		 * |
		 * |    bufferY
		 * |
		 * | DiscardArea = 1 card high
		 * |
		 * |    bufferY
		 * |
		 * | playerArea =  1 card high + up to 9 stacked cards = 2.8 as above
		 * |
		 * |    bufferY
		 * | 
		 * | playerHand = 1 card high
		 * |
		 * |    bufferY
		 *  -----------
		 * 
		 *  Total height = 5*bufferY + 2*2.8 + 2*1 = 5 buffers + 7.6 cards
		 * 
		 */
	
		screenW = w;
		screenH = h;
		bufferY = (int)(.006 * screenH); //arbitrarily chose 0.6%
					
		Double d = ( screenH - (5*bufferY) ) / 7.6; //see layout above for constants
		scaledCardH = d.intValue();
		scaledCardW = (int) (scaledCardH*5/7); // Standard Poker card is 5:7 Width:Length
		
		bufferX = (int) ((screenW - 6*scaledCardW) / 7); // 6 columns and 7 buffers 
		
		// X-Coordinate milestones
		for (int i=0; i<6; i++) {
			columnXCoord[i] = bufferX + (scaledCardW+bufferX) * i;
		}
		
		// Y-Coordinate milestones
		cpuAreaYCoord = bufferY;
		d = cpuAreaYCoord + 2.8*scaledCardH + bufferY; //2.8 = 1 card + 9 cards * 0.2 of stacked card showing
		discardAreaYCoord = d.intValue();
		playerAreaYCoord = 	discardAreaYCoord + scaledCardH + bufferY;
		d = playerAreaYCoord + 2.8*scaledCardH + bufferY;
		playerHandYCoord = d.intValue();
		
		StartNewYear();
		
		Log.v("PlayView","Exit onSizeChanged()");
	}
	
	public void StartNewYear() {
		Log.v("PlayView","Enter StartNewGame()");

		drawPile 	= new ArrayList<DecktetCard>( );
		playerHand 	= new ArrayList<DecktetCard>( );
		cpuHand 	= new ArrayList<DecktetCard>( );
		discardPile = new ArrayList<DecktetCard>( );
		playerEmus 	= new ArrayList<Emu<DecktetCard>>( );
		cpuEmus		= new ArrayList<Emu<DecktetCard>>( );
		
		Emu playerEmu0 = new Emu();
		Emu playerEmu1 = new Emu();
		Emu playerEmu2 = new Emu();
		Emu playerEmu3 = new Emu();
		Emu playerEmu4 = new Emu();
		Emu playerEmu5 = new Emu();
		playerEmus.add(playerEmu0);
		playerEmus.add(playerEmu1);
		playerEmus.add(playerEmu2);
		playerEmus.add(playerEmu3);
		playerEmus.add(playerEmu4);
		playerEmus.add(playerEmu5);
		
		Emu cpuEmu0 = new Emu();
		Emu cpuEmu1 = new Emu();
		Emu cpuEmu2 = new Emu();
		Emu cpuEmu3 = new Emu();
		Emu cpuEmu4 = new Emu();
		Emu cpuEmu5 = new Emu();
		cpuEmus.add(cpuEmu0);
		cpuEmus.add(cpuEmu1);
		cpuEmus.add(cpuEmu2);
		cpuEmus.add(cpuEmu3);
		cpuEmus.add(cpuEmu4);
		cpuEmus.add(cpuEmu5);
		
		    
		//Get optionalcards from settings
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String optionalCards = sharedPref.getString("optionalcards_key", context.getString(R.string.optionalcards_default));
		
		boolean useExcuse = false;
		boolean usePawns = false;
		boolean useCourts = false;
		
		if (optionalCards.equals("Standard")) {
			// do nothing
		}
		else if (optionalCards.equals("Standard + excuse")) {
			useExcuse = true;
		}
		else if (optionalCards.equals("Standard + pawns")) {
			usePawns = true;
		}
		else if (optionalCards.equals("Standard + pawns + excuse")) {
			useExcuse = true;
			usePawns = true;
		}
		else if (optionalCards.equals("Standard + pawns + courts")) {
			usePawns = true;
			useCourts = true;
		}
		else if (optionalCards.equals("Standard + pawns + courts + excuse")) {
			usePawns = true;
			useCourts = true;
			useExcuse = true;
		}
		else {
			Log.e("PlayView", "Invalid optionalCards read from preferences.");
		}
		
		CreateDecktetDeck(drawPile, usePawns, useCourts, useExcuse);
		
		Collections.shuffle(drawPile);
		//LogCards(drawPile);
		
		//Deal the hands
		for (int i=0; i<6; i++) {
			DrawCard(playerHand);
			DrawCard(cpuHand);			
		}
		//LogCards(playerHand);
		//LogCards(cpuHand);
		
		
		// Player who started last year, goes second this year OR toss coin for first year
		if (NextYearStarter == CPU_STARTS) {
			game_phase = CPU_PLAY;
			NextYearStarter = PLAYER_STARTS;
			invalidate();
			CpuPlay();
		}
		else if (NextYearStarter == PLAYER_STARTS) {
			game_phase = PLAYER_PLAY;
			NextYearStarter = CPU_STARTS;
			invalidate();
		}
		else {
		
			Toast toast;	
					
			boolean coin_toss = new Random().nextBoolean();
			if (coin_toss == true) {
				game_phase = PLAYER_PLAY;
				toast = Toast.makeText(getContext(), R.string.player_starts, Toast.LENGTH_SHORT);
				NextYearStarter = CPU_STARTS;
			}
			else {
				game_phase = CPU_PLAY;
				CpuPlay();
				toast = Toast.makeText(getContext(), R.string.cpu_starts, Toast.LENGTH_SHORT);
				NextYearStarter = PLAYER_STARTS;
			}
			
			toast.setGravity(Gravity.TOP, 0, discardAreaYCoord);  
			toast.show();
		}
		
		
		Log.v("PlayView","Exit StartNewGame()");
	}
	
	private void CpuPlay() {
		
		Log.v("PlayView:", "Starting CpuPlay()");
		
		int rand = new Random().nextInt(5); //0-4
		if (rand == 4){
			discardPile.add(cpuHand.remove(0));
		}
		else if(cpuEmus.get(rand+2).size() < 10) {
			cpuEmus.get(2+rand).add(cpuHand.remove(0));
		}
		
		DrawCard(cpuHand);
		if (game_phase != YEAR_END) {
			game_phase = PLAYER_PLAY;
		}
		
		Log.v("PlayView:", "Exiting CpuPlay()");
	}

	private void DrawCard(ArrayList<DecktetCard> hand) {
		if (drawPile.size() > 0) {
			hand.add(drawPile.remove(0));
		}
		if (drawPile.size() == 0) {
			EndYear();
		}
	}
	
	private void EndYear() {
		
		Log.v("PlayView:", "Starting EndYear()");
		
		Toast toast;	
		toast = Toast.makeText(getContext(), R.string.eoy_message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 0, discardAreaYCoord);  
		toast.show();
		
		game_phase = YEAR_END;
		
		Log.v(getClass().getName(), "Ending EndYear()");
	}
	
	private void LogCards(ArrayList<DecktetCard> cards) {
		for (int i=0; i < cards.size(); i++) {
			Log.i(getClass().getName(), "card " + i + ": " + cards.get(i).getString());
		}
	}

	

	// Create DecktetDeck using all cards
	public void CreateDecktetDeck(ArrayList<DecktetCard> deck) {
		this.CreateDecktetDeck(deck, true, true, true);
	}
	
	// Create DecktetDeck specifying inclusion of pawns, courts, and excuse
	public void CreateDecktetDeck(ArrayList<DecktetCard> deck, boolean includePawns, boolean includeCourts, boolean includeExcuse) {
		Log.v("PlayView","Enter CreateDecktetDeck()");	
	
		Bitmap tempBitmap;
		Bitmap scaledBitmap;
		
		// Ace of Moons
		DecktetCard dc_ace_moons = new DecktetCard();
		dc_ace_moons.setId(DecktetCard.ACE | DecktetCard.MOONS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.ace_moons);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_ace_moons.setBmp(scaledBitmap);
		deck.add(dc_ace_moons);
		
		// Ace of Suns
		DecktetCard dc_ace_suns = new DecktetCard();
		dc_ace_suns.setId(DecktetCard.ACE | DecktetCard.SUNS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.ace_suns);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_ace_suns.setBmp(scaledBitmap);
		deck.add(dc_ace_suns);
		
		// Ace of Waves
		DecktetCard dc_ace_waves = new DecktetCard();
		dc_ace_waves.setId(DecktetCard.ACE | DecktetCard.WAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.ace_waves);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_ace_waves.setBmp(scaledBitmap);
		deck.add(dc_ace_waves);	
		
		// ace_leaves
		DecktetCard dc_ace_leaves = new DecktetCard();
		dc_ace_leaves.setId(DecktetCard.ACE | DecktetCard.LEAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.ace_leaves);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_ace_leaves.setBmp(scaledBitmap);
		deck.add(dc_ace_leaves);
		
		// ace_wyrms
		DecktetCard dc_ace_wyrms = new DecktetCard();
		dc_ace_wyrms.setId(DecktetCard.ACE | DecktetCard.WYRMS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.ace_wyrms);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_ace_wyrms.setBmp(scaledBitmap);
		deck.add(dc_ace_wyrms);

		// ace_knots
		DecktetCard dc_ace_knots = new DecktetCard();
		dc_ace_knots.setId(DecktetCard.ACE | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.ace_knots);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_ace_knots.setBmp(scaledBitmap);
		deck.add(dc_ace_knots);

		// author
		DecktetCard dc_author = new DecktetCard();
		dc_author.setId(2 | DecktetCard.MOONS | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a2_author);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_author.setBmp(scaledBitmap);
		deck.add(dc_author);

		// desert
		DecktetCard dc_desert = new DecktetCard();
		dc_desert.setId(2 | DecktetCard.SUNS | DecktetCard.WYRMS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a2_desert);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_desert.setBmp(scaledBitmap);
		deck.add(dc_desert);
		
		// origin
		DecktetCard dc_origin = new DecktetCard();
		dc_origin.setId(2 | DecktetCard.WAVES | DecktetCard.LEAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a2_origin);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_origin.setBmp(scaledBitmap);
		deck.add(dc_origin);
		
		// journey
		DecktetCard dc_journey = new DecktetCard();
		dc_journey.setId(3 | DecktetCard.MOONS | DecktetCard.WAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a3_journey);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_journey.setBmp(scaledBitmap);
		deck.add(dc_journey);

		// savage
		DecktetCard dc_savage = new DecktetCard();
		dc_savage.setId(3 | DecktetCard.LEAVES | DecktetCard.WYRMS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a3_savage);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_savage.setBmp(scaledBitmap);
		deck.add(dc_savage);

		// painter
		DecktetCard dc_painter = new DecktetCard();
		dc_painter.setId(3 | DecktetCard.SUNS | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a3_painter);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_painter.setBmp(scaledBitmap);
		deck.add(dc_painter);

		// mountain
		DecktetCard dc_mountain = new DecktetCard();
		dc_mountain.setId(4 | DecktetCard.MOONS | DecktetCard.SUNS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a4_mountain);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_mountain.setBmp(scaledBitmap);
		deck.add(dc_mountain);

		// sailor
		DecktetCard dc_sailor = new DecktetCard();
		dc_sailor.setId(4 | DecktetCard.WAVES | DecktetCard.LEAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a4_sailor);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_sailor.setBmp(scaledBitmap);
		deck.add(dc_sailor);

		// battle
		DecktetCard dc_battle = new DecktetCard();
		dc_battle.setId(4 | DecktetCard.WYRMS | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a4_battle);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_battle.setBmp(scaledBitmap);
		deck.add(dc_battle);

		// forest
		DecktetCard dc_forest = new DecktetCard();
		dc_forest.setId(5 | DecktetCard.MOONS | DecktetCard.LEAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a5_forest);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_forest.setBmp(scaledBitmap);
		deck.add(dc_forest);

		// discovery
		DecktetCard dc_discovery = new DecktetCard();
		dc_discovery.setId(5 | DecktetCard.SUNS | DecktetCard.WAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a5_discovery);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_discovery.setBmp(scaledBitmap);
		deck.add(dc_discovery);

		//5 soldier
		DecktetCard dc_soldier = new DecktetCard();
		dc_soldier.setId(5 | DecktetCard.WYRMS | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a5_soldier);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_soldier.setBmp(scaledBitmap);
		deck.add(dc_soldier);

		//6 lunatic
		DecktetCard dc_lunatic = new DecktetCard();
		dc_lunatic.setId(6 | DecktetCard.MOONS | DecktetCard.WAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a6_lunatic);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_lunatic.setBmp(scaledBitmap);
		deck.add(dc_lunatic);

		//6 penitent
		DecktetCard dc_penitent = new DecktetCard();
		dc_penitent.setId(6 | DecktetCard.SUNS | DecktetCard.WYRMS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a6_penitent);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_penitent.setBmp(scaledBitmap);
		deck.add(dc_penitent);

		//6 market
		DecktetCard dc_market = new DecktetCard();
		dc_market.setId(6 | DecktetCard.LEAVES | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a6_market);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_market.setBmp(scaledBitmap);
		deck.add(dc_market);

		//7 chance_meeting
		DecktetCard dc_chance_meeting = new DecktetCard();
		dc_chance_meeting.setId(7 | DecktetCard.MOONS | DecktetCard.LEAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a7_chance_meeting);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_chance_meeting.setBmp(scaledBitmap);
		deck.add(dc_chance_meeting);
		
		//7 castle
		DecktetCard dc_castle = new DecktetCard();
		dc_castle.setId(7 | DecktetCard.SUNS | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a7_castle);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_castle.setBmp(scaledBitmap);
		deck.add(dc_castle);


		//7 cave
		DecktetCard dc_cave = new DecktetCard();
		dc_cave.setId(7 | DecktetCard.WAVES | DecktetCard.WYRMS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a7_cave);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_cave.setBmp(scaledBitmap);
		deck.add(dc_cave);

		
		// diplomat
		DecktetCard dc_diplomat = new DecktetCard();
		dc_diplomat.setId(8 | DecktetCard.MOONS | DecktetCard.SUNS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a8_diplomat);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_diplomat.setBmp(scaledBitmap);
		deck.add(dc_diplomat);

		// mill
		DecktetCard dc_mill = new DecktetCard();
		dc_mill.setId(8 | DecktetCard.WAVES | DecktetCard.LEAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a8_mill);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_mill.setBmp(scaledBitmap);
		deck.add(dc_mill);

		// betrayal
		DecktetCard dc_betrayal = new DecktetCard();
		dc_betrayal.setId(8 | DecktetCard.WYRMS | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a8_betrayal);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_betrayal.setBmp(scaledBitmap);
		deck.add(dc_betrayal);

		// pact
		DecktetCard dc_pact = new DecktetCard();
		dc_pact.setId(9 | DecktetCard.MOONS | DecktetCard.SUNS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a9_pact);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_pact.setBmp(scaledBitmap);
		deck.add(dc_pact);

		// merchant
		DecktetCard dc_merchant = new DecktetCard();
		dc_merchant.setId(9 | DecktetCard.LEAVES | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a9_merchant);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_merchant.setBmp(scaledBitmap);
		deck.add(dc_merchant);

		// darkness
		DecktetCard dc_darkness = new DecktetCard();
		dc_darkness.setId(9 | DecktetCard.WAVES | DecktetCard.WYRMS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.a9_darkness);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_darkness.setBmp(scaledBitmap);
		deck.add(dc_darkness);

		if (includePawns) {
			
			// pawn harvest
			DecktetCard dc_harvest = new DecktetCard();
			dc_harvest.setId(DecktetCard.PAWN | DecktetCard.MOONS | 
					DecktetCard.SUNS | DecktetCard.LEAVES);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.pawn_harvest);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_harvest.setBmp(scaledBitmap);
			deck.add(dc_harvest);
	
			// pawn watchman
			DecktetCard dc_watchman = new DecktetCard();
			dc_watchman.setId(DecktetCard.PAWN | DecktetCard.MOONS | 
					DecktetCard.WYRMS | DecktetCard.KNOTS);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.pawn_watchman);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_watchman.setBmp(scaledBitmap);
			deck.add(dc_watchman);
	
			// pawn light_keeper
			DecktetCard dc_light_keeper = new DecktetCard();
			dc_light_keeper.setId(DecktetCard.PAWN | DecktetCard.SUNS | 
					DecktetCard.WAVES | DecktetCard.KNOTS);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.pawn_light_keeper);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_light_keeper.setBmp(scaledBitmap);
			deck.add(dc_light_keeper);
	
			// pawn borderland
			DecktetCard dc_borderland = new DecktetCard();
			dc_borderland.setId(DecktetCard.PAWN | DecktetCard.WAVES | 
					DecktetCard.LEAVES | DecktetCard.WYRMS);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.pawn_borderland);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_borderland.setBmp(scaledBitmap);
			deck.add(dc_borderland);
		}
		
		if (includeCourts) {
	
			// court consul
			DecktetCard dc_consul = new DecktetCard();
			dc_consul.setId(DecktetCard.COURT | DecktetCard.MOONS | 
					DecktetCard.WAVES | DecktetCard.KNOTS);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.court_consul);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_consul.setBmp(scaledBitmap);
			deck.add(dc_consul);
	
			// court rite
			DecktetCard dc_rite = new DecktetCard();
			dc_rite.setId(DecktetCard.COURT | DecktetCard.MOONS | 
					DecktetCard.LEAVES | DecktetCard.WYRMS);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.court_rite);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_rite.setBmp(scaledBitmap);
			deck.add(dc_rite);
	
			// court window
			DecktetCard dc_window = new DecktetCard();
			dc_window.setId(DecktetCard.COURT | DecktetCard.SUNS | 
					DecktetCard.LEAVES | DecktetCard.KNOTS);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.court_window);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_window.setBmp(scaledBitmap);
			deck.add(dc_window);
	
			// court island
			DecktetCard dc_island = new DecktetCard();
			dc_island.setId(DecktetCard.COURT | DecktetCard.SUNS | 
					DecktetCard.WAVES | DecktetCard.WYRMS);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.court_island);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_island.setBmp(scaledBitmap);
			deck.add(dc_island);
		}
		
		// crown_huntress
		DecktetCard dc_crown_huntress = new DecktetCard();
		dc_crown_huntress.setId(DecktetCard.CROWN | DecktetCard.MOONS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.crown_huntress);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_crown_huntress.setBmp(scaledBitmap);
		deck.add(dc_crown_huntress);

		// crown_bard
		DecktetCard dc_crown_bard = new DecktetCard();
		dc_crown_bard.setId(DecktetCard.CROWN | DecktetCard.SUNS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.crown_bard);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_crown_bard.setBmp(scaledBitmap);
		deck.add(dc_crown_bard);

		// crown_sea
		DecktetCard dc_crown_sea = new DecktetCard();
		dc_crown_sea.setId(DecktetCard.CROWN | DecktetCard.WAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.crown_sea);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_crown_sea.setBmp(scaledBitmap);
		deck.add(dc_crown_sea);

		// crown_end
		DecktetCard dc_crown_end = new DecktetCard();
		dc_crown_end.setId(DecktetCard.CROWN | DecktetCard.LEAVES);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.crown_end);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_crown_end.setBmp(scaledBitmap);
		deck.add(dc_crown_end);

		// crown_calamity
		DecktetCard dc_crown_calamity = new DecktetCard();
		dc_crown_calamity.setId(DecktetCard.CROWN | DecktetCard.WYRMS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.crown_calamity);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_crown_calamity.setBmp(scaledBitmap);
		deck.add(dc_crown_calamity);

		// crown_windfall
		DecktetCard dc_crown_windfall = new DecktetCard();
		dc_crown_windfall.setId(DecktetCard.CROWN | DecktetCard.KNOTS);
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.crown_windfall);
		scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);
		dc_crown_windfall.setBmp(scaledBitmap);
		deck.add(dc_crown_windfall);					
		
		if (includeExcuse) {
			// Excuse
			DecktetCard dc_excuse = new DecktetCard();
			dc_excuse.setId(DecktetCard.EXCUSE);
			tempBitmap = BitmapFactory.decodeResource(
					this.getResources(), R.drawable.excuse);
			scaledBitmap = Bitmap.createScaledBitmap(tempBitmap, 
					scaledCardW, scaledCardH, false);
			dc_excuse.setBmp(scaledBitmap);
			deck.add(dc_excuse);
		}
		
		// Also, create card back and target bitmaps while we're here
		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.back);
		cardBack = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);

		tempBitmap = BitmapFactory.decodeResource(
				this.getResources(), R.drawable.target);
		cardTarget = Bitmap.createScaledBitmap(tempBitmap, 
				scaledCardW, scaledCardH, false);

		
		Log.v("PlayView","Exit CreateDecktetDeck()");
	}
	
}
