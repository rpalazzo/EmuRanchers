/* Copyright Robert Palazzo 2015
 * Copying this propritary source code without express
 * written permission from the author is prohibited.
 */

package com.rpalazzo.emuranchers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class RulesActivity extends Activity {
	
	 WebView webView;
	 
	 String mimeType = "text/html";
	 String encoding = "utf-8";
	 String htmlText = "<html> <strong>Emu Ranchers Rules</strong> " +
	 "<blockquote> <p>It is a hardscrabble life for emu ranchers like yourself, raising exotic birds for foreign markets. With the high cost for every bird you hatch, it probably won't be worth it in the end. You can't tell at the beginning of the year which " +
	 "of the six bird varieties will do best, and beware the birds with exceptional plumage! If they thrive, they can be worth big money; but if they go wrong, they can bankrupt your ranch.</p> </blockquote> <p><strong>Summary of the game:</strong> Each bird " +
	 "on your ranch is represented by a stack of cards. Cards in a single bird stack must all share a suit and must be in rank order. At the end of the hand, the value of a bird is determined by the total of the number cards in the stack. If the total is too " +
	 "low, then you can end up losing money on a bird. An Ace or Crown makes the stack worth more: more profit if you can cover expenses, but more loss if you can't. The object, naturally enough, is to raise profitable birds.<br /></p> <h2><span>Setup</span>" +
	 "</h2> <p>Deal six cards to each player. The remaining cards form the draw pile. There is no discard pile at the beginning.</p><p>Play alternates until the last card is drawn from the draw pile.</p> <h2><span>Game play</span></h2> <p>On your turn, you may " +
	 "do one of the following: hatch a new bird, by starting a new stack; grow a bird, by adding a card to an existing stack; or discard a card. After that, you draw one card.</p><ul><li><strong>Hatch a new bird:</strong> You may start a new bird stack by selecting " +
	 "a card from your hand and playing it face up in front of you.</li></ul><ul><li><strong>Grow a bird:</strong> You may grow a bird by adding a card from your hand to the top of a stack already in front of you. All of the cards in the stack must share a single " +
	 "suit, although of course number cards will each have another suit as well. Stacks must be in either increasing or decreasing order, although you may skip ranks.</li></ul><p><strong>Example:</strong> The stack for a blue bird might be comprised of the Ace, 2, " +
	 "and 4 of Waves. You may only play a Wave of rank 5 or more on that bird.</p><p>For the purpose of stack order, Aces are below 1s and Crowns are above 9s. If you hatch a bird with a number card, you do not need to declare which suit the stack will follow " +
	 "or which direction it will go; this will eventually be determined by cards you play when growing the bird.</p><p><strong>Example:</strong> You hatch a bird with the 7 of Suns and Knots. You may either make it an orange bird (by growing it with a Sun card) " +
	 "or a yellow bird (by growing it with a Knot). The first time you grow it, you may grow up (by playing a card rank 8 or more) or grow down (by playing 6 or less). If you grow the bird with the 6 of Suns and Wyrms, then you are committed to an orange bird " +
	 "growing down.</p><ul> <li><strong>Discard:</strong> If you don't want to play any of the cards in your hand, you may select and discard one card. Put it on the top of the discard pile, starting the pile if necessary.</li></ul><ul><li><strong>Draw:</strong> " +
	 "If you discarded, take the top card of the draw pile. Otherwise, you may take either the top card of the draw pile or the top card of the discard pile (if any).</li></ul><p>After you draw, your turn is over. If there are still cards in the draw pile, it is " +
	 "now your opponent's turn.</p><h2><span>Year end</span></h2><p>When the last card is drawn from the draw pile, the year ends.</p><p>After the year is over, you may play cards from your hands onto birds that you already have in play. However, you " +
	 "may not hatch new birds or draw cards after the year has ended. Since there is no further player interaction after the year end, you may lay down remaining cards without waiting for other players. Then discard any cards that you are not able to play.<br />" +
	 "<h2>Scoring</h2></p><p>Total up the number cards in the bird stack. If the total is less than 18, then you lose money on the bird. If the total is 18 or more, then you may make a profit.</p><ul><li><strong>Losing money:</strong> You lose points equal to the " +
	 "difference between the bird's total and the upkeep cost of 18. If the number cards total to 15, for example, you lose 3 points. Furthermore, you lose 5 points if there is an Ace or Crown in the stack; 10 if there are both an Ace and a Crown.</li></ul><p>" +
	 "<strong>Example:</strong> At the end of the year, the stack for your orange bird is the Crown, 9, and 8 of Suns. You lose 6 points (18-9-8=1 for the number cards plus 5 for the Crown).</p><ul><li><strong>Possible profit:</strong> If the total of the number " +
	 "cards is 18 or more, you still need to pay upkeep for the bird: Discard number cards from the stack that total at least 18. You do not 'get change' for cards if you discard more than 18. If there are any cards remaining - even just an Ace or Crown - you earn " +
	 "some profit.</li></ul><p>For profit, you score the total of any remaining number cards. Furthermore, you gain 5 points if there is an Ace or Crown in the stack; 10 if there are both an Ace and a Crown.</p><p><strong>Example:</strong> At the end of the year, " +
	 "the stack for your blue bird is the Ace, 2, 3, 5, 6, 8, and Crown of Waves. You discard the 2, 3, 5, and 8 to pay upkeep; this totals exactly 18. This leaves the Ace, 6, and Crown. You score 16 points (6 for the number card plus 10 for the Ace/Crown combo).</p>" +
	 "<p>Your score for the year is equal to the total value of your birds.</p><p>Shuffle the cards and deal a new year. The player who went first should go second in the next year. Since there is some disadvantage to going first, it's best to tally the score across " +
	 "an even number of years. </p><h2><span>The extended deck</span></h2><p>If you want to spice up the game, you can add in the Excuse, the Pawns, or the Courts. Just shuffle them in at the " +
	 "beginning of the game.</p><p><strong>The Excuse:</strong> If you have the Excuse in your hand at the end of the year, you may discard one of your birds that would lose " +
	 "money rather than scoring it. If you have no losing birds, then the Excuse has no effect.</p><p><strong>Pawns or Courts:</strong> A Pawn or Court is a limited wild card. It may be played as any number rank, but only to grow a bird that matches one of the Pawn's " +
	 "suits. It may not be played as an Ace or Crown, nor may it be played to hatch a bird.</p>" +
	 "</ul><h2><span>Credits</span></h2><p><strong>Design:</strong> P.D. Magnus</p><p><strong>Playtesting:</strong> Cristyn Magnus</p><p>This game was inspired by Jeff Warrender, who suggested playing something like Reiner Knizia's <em>Lost Cities</em> with the Decktet. " +
	 "</p><p><small><em>The content of this page is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License</em></small></p></body></html>";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rules);
		  
		webView = (WebView)findViewById(R.id.webview);
		webView.loadData(htmlText, mimeType, encoding);
		webView.getSettings().setBuiltInZoomControls(true);
		//webView.getSettings().setDisplayZoomControls(false);
		
		webView.setWebViewClient(new WebViewClient(){
		    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		        if (url != null && url.startsWith("http://")) {
	            view.getContext().startActivity(
	                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	            return true;
	        } else {
	            return false;
	        }
	    }
	});

	}
}
