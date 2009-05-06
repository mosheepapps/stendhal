package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.ados.swamp.DeathmatchRecruiterNPC;
import marauroa.common.Log4J;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;

public class AdosDeathmatchTest {
    
        public static final StendhalRPZone ados_wall_n = new StendhalRPZone("0_ados_wall_n", 200, 200);
	public static final StendhalRPZone zone = new StendhalRPZone("dmTestZone");

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		PlayerTestHelper.generateNPCRPClasses();
		MockStendlRPWorld.get();
		MockStendlRPWorld.get().addRPZone(ados_wall_n);
		final DeathmatchRecruiterNPC configurator =  new DeathmatchRecruiterNPC();
		configurator.configureZone(zone, null);
		// some of the recruiter responses are defined in the quest not the configurator
		new AdosDeathmatch().addToWorld();	
	}
	
	@AfterClass
	public static void teardownAfterClass() throws Exception {
		MockStendlRPWorld.reset();
	}
	
	
	@Test
	public void testRecruiter() throws Exception {
		final SpeakerNPC recruiter = SingletonRepository.getNPCList().get("Thonatus");
		assertNotNull(recruiter);
		assertNotNull(zone);
		assertNotNull(ados_wall_n);
		final Player dmPlayer = PlayerTestHelper.createPlayer("dmPlayer");
		final Engine en = recruiter.getEngine();
		assertEquals(ConversationStates.IDLE, en.getCurrentState());
		en.step(dmPlayer, "hi");
		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
		assertEquals("Hey there. You look like a reasonable fighter.", recruiter.get("text"));
		
		en.step(dmPlayer, "job");
		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
		assertEquals("I'm recruiter for the Ados #deathmatch.", recruiter.get("text"));
		
		en.step(dmPlayer, "deathmatch");
		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
		assertEquals("Many dangerous creatures will attack you in the deathmatch arena. It is only for strong #heroes.", recruiter.get("text"));
		
		
 		dmPlayer.setLevel(19);
 		en.step(dmPlayer, "challenge");
 		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
 		assertEquals("Sorry, you are too weak for the #deathmatch now, come back when you have at least level 20.", recruiter.get("text"));
 		recruiter.remove("text");
		
		en.step(dmPlayer, "bye");
		assertEquals(ConversationStates.IDLE, en.getCurrentState());
		assertEquals("I hope you will enjoy the Ados Deathmatch!", recruiter.get("text"));
		

		
		dmPlayer.setLevel(20);
		//assertNotNull(dmPlayer.getZone());
		en.step(dmPlayer, "hi");
		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
		assertEquals("Hey there. You look like a reasonable fighter.", recruiter.get("text"));
		en.step(dmPlayer, "challenge");
		
		assertEquals(ConversationStates.IDLE, en.getCurrentState());
	       	assertNull(recruiter.get("text"));
		assertNotNull(dmPlayer.getZone());
		// no players already in zone, send straight in
		assertEquals(ados_wall_n, dmPlayer.getZone());
		assertEquals(100, dmPlayer.getX());
		
		en.setCurrentState(ConversationStates.IDLE);
		
		final Player joiner = PlayerTestHelper.createPlayer("dmPlayer");
		joiner.setLevel(19);
		en.step(joiner, "hi");
		recruiter.remove("text");
		en.step(joiner, "challenge");
		recruiter.remove("text");
		assertEquals(null, joiner.getZone());
		joiner.setLevel(20);
		
		en.step(joiner, "challenge");
		assertEquals("There are heroes battling right now in the deathmatch, so it may be dangerous there. Do you want to join dmPlayer?", recruiter.get("text"));
		en.step(joiner, "yes");
		assertEquals(ados_wall_n, joiner.getZone());
		
	}

	
}
