/*
 * BindResourceTest.java
 *
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.xmpp.impl;

import tigase.TestLogger;

import tigase.server.Packet;

import tigase.xmpp.jid.JID;
import tigase.xmpp.NoConnectionIdException;
import tigase.xmpp.NotAuthorizedException;
import tigase.xmpp.XMPPResourceConnection;

import tigase.util.stringprep.TigaseStringprepException;
import tigase.xml.Element;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Wojtek
 */
public class BindResourceTest extends ProcessorTestCase {

	private static final Logger log = TestLogger.getLogger(BindResourceTest.class);

	private BindResource bindResource;

	public BindResourceTest() {
	}

	@Before
	@Override
	public void setUp() throws Exception {
		bindResource = new BindResource();
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
		bindResource = null;
		super.tearDown();
	}

	@Test
	public void testPreProcess() throws TigaseStringprepException, NotAuthorizedException, NoConnectionIdException {
		JID senderJid = JID.jidInstance("sender@example.com/res-1");
		JID recipientJid = JID.jidInstance("recipient@example.com/res-2");
		XMPPResourceConnection senderSession = getSession(JID.jidInstance("c2s@example.com/" + UUID.randomUUID().toString()),
																											senderJid);

		Element messageEl = new Element("message",
				new String[] { "from" },
				new String[] { senderJid.toString() });
		Packet p = Packet.packetInstance(messageEl);
		p.setPacketFrom(senderSession.getConnectionId());

		Map<String,Object> settings = new HashMap<>();
		Queue<Packet> results = new ArrayDeque<>();

		// message / non-presence
		log.log(Level.FINE, p.getElement() +"" );
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(senderJid, p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n" );

		p.getElement().setAttribute( "from", senderJid.getBareJID().toString());
		log.log(Level.FINE,  p.getElement() +"" );
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(senderJid, p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n");

		p.getElement().removeAttribute("from");
		log.log(Level.FINE,  p.getElement()  +"");
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(senderJid, p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n"  );

		// presence -- non-sub
		log.log(Level.FINE,  "====="  );

		Element presenceEl = new Element("presence",
				new String[] { "from" },
				new String[] { senderJid.toString() });
		p = Packet.packetInstance(presenceEl);
		p.setPacketFrom(senderSession.getConnectionId());

		log.log(Level.FINE,  p.getElement() +"" );
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(senderJid, p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n" );

		p.getElement().setAttribute( "from", senderJid.getBareJID().toString());
		log.log(Level.FINE,  p.getElement() +"" );
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(senderJid, p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n");

		p.getElement().removeAttribute("from");
		log.log(Level.FINE,  p.getElement()  +"");
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(senderJid, p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n"  );

		// presence -- sub
		log.log(Level.FINE,  "====="  );

		p.getElement().setAttribute( "type", "subscribe");
		p.getElement().setAttribute( "from", senderJid.toString());
		p = Packet.packetInstance(p.getElement());
		p.setPacketFrom(senderSession.getConnectionId());

		log.log(Level.FINE,  p.getElement() +"" );
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(JID.jidInstance( senderJid.getBareJID()), p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n" );

		p.getElement().setAttribute( "from", senderJid.getBareJID().toString());
		log.log(Level.FINE,  p.getElement() +"" );
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(JID.jidInstance( senderJid.getBareJID()), p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n");

		p.getElement().removeAttribute("from");
		log.log(Level.FINE,  p.getElement()  +"");
		assertFalse(bindResource.preProcess(p, senderSession, null, results, settings));
		assertEquals(0, results.size());
		assertEquals(JID.jidInstance( senderJid.getBareJID()), p.getStanzaFrom());
		log.log(Level.FINE,  p.getStanzaFrom() +"\n"  );


	}

}
