/*  Package Jabber Server
 *  Copyright (C) 2001-2007
 *  "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */
package tigase.server.ssender;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import tigase.conf.Configurable;
import tigase.server.AbstractMessageReceiver;
import tigase.server.Packet;
import tigase.server.ServerComponent;
import tigase.xml.DomBuilderHandler;
import tigase.xml.Element;
import tigase.xml.SimpleParser;
import tigase.xml.SingletonFactory;

/**
 * <code>StanzaSender</code> class implements simple cyclic tasks management
 * mechanism. You can specify as many tasks in configuration as you need.
 * <p>
 * These tasks are designed to pull XMPP stanzas from specific data source like
 * SQL database, directory in the filesystem and so on. Each of these tasks must
 * extend <code>tigase.server.ssende.SenderTask</code> abstract class.
 * Look in specific tasks implementation for more detailed description how
 * to use them.
 * <p>
 * Created: Fri Apr 20 11:11:25 2007
 * </p>
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class StanzaSender extends AbstractMessageReceiver
	implements Configurable, StanzaHandler {

	public static final String INTERVAL_PROP_KEY = "default-interval";
	public static final long INTERVAL_PROP_VAL = 10;
	public static final String STANZA_LISTENERS_PROP_KEY = "stanza-listeners";
	public static final String TASK_CLASS_PROP_KEY = "class-name";
	public static final String TASK_INIT_PROP_KEY = "init-string";
	public static final String TASK_INTERVAL_PROP_KEY = "interval";
	public static final String JDBC_TASK_NAME = "jdbc";
	public static final String JDBC_TASK_CLASS = "tigase.server.ssender.JDBCTask";
	public static final String JDBC_TASK_INIT =
		"jdbc:mysql://localhost/tigase?user=root&password=mypass&table=xmpp_stanza";
	public static final long JDBC_INTERVAL = 10;
	public static final String FILE_TASK_NAME = "file";
	public static final String FILE_TASK_CLASS = "tigase.server.ssender.FileTask";
	public static final String FILE_TASK_INIT =
		File.separator + "var" + File.separator + "spool" + File.separator +
		"jabber" + File.separator + "*.stanza";
	public static final long FILE_INTERVAL = 10;
	public static final String[] STANZA_LISTENERS_PROP_VAL =
	{JDBC_TASK_NAME, FILE_TASK_NAME};
	public static final String TASK_ACTIVE_PROP_KEY = "active";
	public static final boolean TASK_ACTIVE_PROP_VAL = false;

	/**
   * Variable <code>log</code> is a class logger.
   */
  private static final Logger log =
    Logger.getLogger("tigase.server.ssender.StanzaSender");

	private static final SimpleParser parser =
		SingletonFactory.getParserInstance();
	private long interval = INTERVAL_PROP_VAL;
	private Map<String, SenderTask> tasks_list = new HashMap<String, SenderTask>();
	private Timer tasks = new Timer("StanzaSender", true);

	// Implementation of tigase.server.ServerComponent

	/**
	 * Describe <code>release</code> method here.
	 *
	 */
	public void release() {
		super.release();
	}

	private String myDomain() {
		return getName() + "." + getDefHostName();
	}

	/**
	 * Describe <code>processPacket</code> method here.
	 *
	 * @param packet a <code>Packet</code> value
	 */
	public void processPacket(final Packet packet) {
		// do nothing, this component is to send packets not to receive
		// (for now)
	}

	// Implementation of tigase.conf.Configurable

	/**
	 * Describe <code>setProperties</code> method here.
	 *
	 * @param props a <code>Map</code> value
	 */
	public void setProperties(final Map<String, Object> props) {
		super.setProperties(props);
		interval = (Long)props.get(INTERVAL_PROP_KEY);
		String[] config_tasks = (String[])props.get(STANZA_LISTENERS_PROP_KEY);
		for (String task_name: config_tasks) {

			// Reconfiguration code. Turn-off old task with that name.
			SenderTask old_task = tasks_list.get(task_name);
			if (old_task != null) {
				old_task.cancel();
			}

			if ((Boolean)props.get(task_name + "/" + TASK_ACTIVE_PROP_KEY)) {
				String task_class =
					(String)props.get(task_name + "/" + TASK_CLASS_PROP_KEY);
				String task_init =
					(String)props.get(task_name + "/" + TASK_INIT_PROP_KEY);
				long task_interval =
					(Long)props.get(task_name + "/" + TASK_INTERVAL_PROP_KEY);
				try {
					SenderTask task = (SenderTask)Class.forName(task_class).newInstance();
					task.setName(task_name + "@" + myDomain());
					task.init(this, task_init);

					// Install new task
					tasks_list.put(task_name, task);
					tasks.scheduleAtFixedRate(task, task_interval*SECOND,
						task_interval*SECOND);
				} catch (Exception e) {
					log.log(Level.SEVERE, "Can not initialize stanza listener: ", e);
				}
			}
		}
	}

	/**
	 * Describe <code>getDefaults</code> method here.
	 *
	 * @param params a <code>Map</code> value
	 * @return a <code>Map</code> value
	 */
	public Map<String, Object> getDefaults(final Map<String, Object> params) {
		Map<String, Object> defs = super.getDefaults(params);
		defs.put(INTERVAL_PROP_KEY, INTERVAL_PROP_VAL);
		defs.put(STANZA_LISTENERS_PROP_KEY, STANZA_LISTENERS_PROP_VAL);

		if ((Boolean)params.get(GEN_TEST)) {
			defs.put(FILE_TASK_NAME + "/" + TASK_ACTIVE_PROP_KEY, true);
		} else {
			defs.put(FILE_TASK_NAME + "/" + TASK_ACTIVE_PROP_KEY, TASK_ACTIVE_PROP_VAL);
		}
		defs.put(FILE_TASK_NAME + "/" + TASK_CLASS_PROP_KEY, FILE_TASK_CLASS);
		defs.put(FILE_TASK_NAME + "/" + TASK_INIT_PROP_KEY, FILE_TASK_INIT);
		defs.put(FILE_TASK_NAME + "/" + TASK_INTERVAL_PROP_KEY, FILE_INTERVAL);

		if ((Boolean)params.get(GEN_TEST)) {
			defs.put(JDBC_TASK_NAME + "/" + TASK_ACTIVE_PROP_KEY, true);
		} else {
			defs.put(JDBC_TASK_NAME + "/" + TASK_ACTIVE_PROP_KEY, TASK_ACTIVE_PROP_VAL);
		}
		defs.put(JDBC_TASK_NAME + "/" + TASK_CLASS_PROP_KEY, JDBC_TASK_CLASS);
		defs.put(JDBC_TASK_NAME + "/" + TASK_INIT_PROP_KEY, JDBC_TASK_INIT);
		defs.put(JDBC_TASK_NAME + "/" + TASK_INTERVAL_PROP_KEY, JDBC_INTERVAL);
		return defs;
	}

	public void handleStanza(String stanza) {
		parseXMLData(stanza);
	}

	public void handleStanzas(Queue<Packet> results) {
		addOutPackets(results);
	}

	private void parseXMLData(String data) {
		DomBuilderHandler domHandler = new DomBuilderHandler();
		parser.parse(domHandler, data.toCharArray(), 0, data.length());
		Queue<Element> elems = domHandler.getParsedElements();
		while (elems != null && elems.size() > 0) {
			Packet result = new Packet(elems.poll());
			addOutPacket(result);
		}
	}

}
