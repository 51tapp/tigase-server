/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2008 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 * 
 * $Rev$
 * Last modified by $Author$
 * $Date$
 */

package tigase.vhosts;

import java.util.LinkedHashMap;
import java.util.Map;

import java.util.logging.Logger;
import tigase.util.DNSResolver;
import static tigase.conf.Configurable.*;

/**
 * Created: Nov 27, 2008 1:53:58 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev$
 */
public class VhostConfigRepository implements VHostRepository {
	
	private static final Logger log =
					Logger.getLogger("tigase.vhosts.VhostConfigRepository");

	public static final String ANONYMOUS_DOMAINS_PROP_KEY = "anonymous-domains";

	private LinkedHashMap<String, VHostItem> vhosts =
					new LinkedHashMap<String, VHostItem>();

	public void getDefaults(Map<String, Object> defs,
					Map<String, Object> params) {
		String[] hostnames = DNSResolver.getDefHostNames();
		if (params.get(GEN_VIRT_HOSTS) != null) {
			hostnames = ((String) params.get(GEN_VIRT_HOSTS)).split(",");
		}
		defs.put(HOSTNAMES_PROP_KEY, hostnames);
		defs.put(ANONYMOUS_DOMAINS_PROP_KEY, hostnames);
	}

	public void setProperties(Map<String, Object> properties) {
		String[] hostnames = (String[]) properties.get(HOSTNAMES_PROP_KEY);
		if (hostnames != null && hostnames.length > 0) {
			vhosts.clear();
			for (String hostname : hostnames) {
				VHostItem item = new VHostItem(hostname);
				vhosts.put(hostname, item);
			}
		} else {
			log.warning("Virtual hosts list is not set in the configuration file!!");
		}
	}

	public VHostItem getVHost(String domain) {
		return vhosts.get(domain);
	}

	public boolean contains(String domain) {
		return vhosts.keySet().contains(domain);
	}

	public void reload() { }

}
