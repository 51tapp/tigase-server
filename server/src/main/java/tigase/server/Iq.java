/*
 * Iq.java
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

package tigase.server;

//~--- non-JDK imports --------------------------------------------------------

import tigase.disco.XMPPService;
import tigase.util.stringprep.TigaseStringprepException;
import tigase.xml.Element;
import tigase.xmpp.StanzaType;
import tigase.xmpp.impl.roster.RosterAbstract;
import tigase.xmpp.jid.JID;

import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * Created: Dec 31, 2009 8:43:21 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class Iq
		extends Packet {

	/** Field description */
	public static final String ELEM_NAME = "iq";

	/** Field description */
	public static final String QUERY_NAME = "query";

	/** Field description */
	public static final String[] IQ_QUERY_PATH = {ELEM_NAME, QUERY_NAME};

	/** Field description */
	public static final String[] IQ_PUBSUB_PATH = {ELEM_NAME, "pubsub"};

	/** Field description */
	public static final String[] IQ_ERROR_PATH = {ELEM_NAME, "error"};

	/** Field description */
	public static final String[] IQ_COMMAND_PATH = {ELEM_NAME, "command"};

	/** Field description */
	public static final String[] IQ_CHAT_PATH = {ELEM_NAME, "chat"};

	/** Field description */
	public static final String[] IQ_BIND_RESOURCE_PATH = {ELEM_NAME, "bind", "resource"};

	/** Field description */
	public static final String[] IQ_BIND_PATH = {ELEM_NAME, "bind"};

	//~--- fields ---------------------------------------------------------------
	private boolean cmd = false;
	private Command command = null;
	private String iqQueryXMLNS = null;
	private boolean serviceDisco = false;
	private String strCommand = null;

	//~--- constructors ---------------------------------------------------------

	public static Packet commandResultForm(Iq packet) throws TigaseStringprepException {
		Packet result = packet.commandResult(Command.DataType.form);

		return result;
	}

	public static Packet commandResultResult(Iq packet) throws TigaseStringprepException {
		Packet result = packet.commandResult(Command.DataType.result);

		return result;
	}

	//~--- methods --------------------------------------------------------------

	/**
	 * Method creates a new <code>Packet</code> instance or <code>Iq</code> instance more specificly with a
	 * roster entry content. TODO: Remove dependency on RosterAbstract class, possibly move the method again to more
	 * proper location but it needs to be accessible from all parts of the application.
	 *
	 * @param iq_type is a <code>String</code> value with the stanza type: 'set', 'get', 'result'.
	 * @param iq_id is a <code>String</code> value with the stanza unique id.
	 * @param from is a <code>JID</code> instance with the packet source address.
	 * @param to is a <code>JID</code> instance with the packet destination address.
	 * @param item_jid is a <code>JID</code> instance with the roster item JID, note in most cases the jid should not
	 * have a resource part, but this method does not cut it off. This is because there are cases when we want to have a
	 * resource part in the roster item.
	 * @param item_name is a <code>String</code> vakue with the roster item name.
	 * @param item_groups is a <code>String[]</code> array with all groups the item belongs to.
	 * @param subscription is a <code>String</code> instance with the item subscription state.
	 * @param item_type is a <code>String</code> of the user item type. This is <code>null</code> in most cases as this
	 * is not part of the XMPP RFC. Some deployments needs some extra information about the roster item type though.
	 *
	 * @return a new <code>Packet</code> instance or <code>Iq</code> instance more specificly with a roster entry
	 * content.
	 */
	public static Iq createRosterPacket(String iq_type, String iq_id, JID from, JID to, JID item_jid, String item_name,
										String[] item_groups, String subscription, String item_type) {
		Element iq = new Element("iq", new String[]{"type", "id"}, new String[]{iq_type, iq_id});

		iq.setXMLNS(CLIENT_XMLNS);
		if (from != null) {
			iq.addAttribute("from", from.toString());
		}
		if (to != null) {
			iq.addAttribute("to", to.toString());
		}

		Element query = new Element("query");

		query.setXMLNS(RosterAbstract.XMLNS);
		iq.addChild(query);

		Element item = new Element("item", new String[]{"jid"}, new String[]{item_jid.toString()});

		if (item_type != null) {
			item.addAttribute("type", item_type);
		}
		if (item_name != null) {
			item.addAttribute(RosterAbstract.NAME, item_name);
		}
		if (subscription != null) {
			item.addAttribute(RosterAbstract.SUBSCRIPTION, subscription);
		}
		if (item_groups != null) {
			for (String gr : item_groups) {
				Element group = new Element(RosterAbstract.GROUP, gr);

				item.addChild(group);
			}
		}
		query.addChild(item);

		return new Iq(iq, from, to);
	}

	public Iq(Element elem) throws TigaseStringprepException {
		super(elem);
		init();
	}

	public Iq(Element elem, JID stanzaFrom, JID stanzaTo) {
		super(elem, stanzaFrom, stanzaTo);
		init();
	}

	public Packet commandResult(Command.DataType cmd_type) {
		Packet result = packetInstance(
				command.createIqCommand(getStanzaTo(), getStanzaFrom(), StanzaType.result, getStanzaId(), strCommand,
										cmd_type), getStanzaTo(), getStanzaFrom());

		result.setPacketFrom(getTo());
		result.setPacketTo(getFrom());

		return result;
	}

	//~--- get methods ----------------------------------------------------------

	@Override
	public Command getCommand() {
		return command;
	}

	public String getIQChildName() {
		List<Element> children = elem.getChildren();

		if ((children != null) && (children.size() > 0)) {
			return children.get(0).getName();
		}

		return null;
	}

	public String getIQXMLNS() {
		if (iqQueryXMLNS == null) {
			iqQueryXMLNS = elem.getXMLNSStaticStr(IQ_QUERY_PATH);
		}

		return iqQueryXMLNS;
	}

	public String getStrCommand() {
		return strCommand;
	}

	@Override
	public boolean isCommand() {
		return cmd;
	}

	@Override
	public boolean isServiceDisco() {
		return serviceDisco;
	}

	@Override
	protected String[] getElNameErrorPath() {
		return IQ_ERROR_PATH;
	}

	//~--- methods --------------------------------------------------------------

	private void init() {
		Element child = elem.getChild("command", Command.XMLNS);

		if (child != null) {
			cmd = true;
			strCommand = child.getAttributeStaticStr("node");
			command = Command.valueof(strCommand);
		}
		serviceDisco = (isXMLNSStaticStr(IQ_QUERY_PATH, XMPPService.INFO_XMLNS) ||
				isXMLNSStaticStr(IQ_QUERY_PATH, XMPPService.ITEMS_XMLNS));
	}
}

//~ Formatted in Tigase Code Convention on 13/02/20
