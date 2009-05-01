/*
    Copyright 2008 Wolfgang Ginolas

    This file is part of P2PVPN.

    P2PVPN is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.p2pvpn.gui;

import java.awt.HeadlessException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.p2pvpn.network.ConnectionManager;
import org.p2pvpn.network.Connector;
import org.p2pvpn.network.ConnectorListener;
import org.p2pvpn.network.PeerID;
import org.p2pvpn.network.TCPConnection;
import org.p2pvpn.network.VPNConnector;
import org.p2pvpn.tools.AdvProperties;
import org.p2pvpn.tools.CryptoUtils;
import org.p2pvpn.tuntap.TunTap;

public class MainControl implements ConnectorListener {
	private static final String DEFAULT_NET_FILE = "default.net";

	private AdvProperties networkCfg;
	private AdvProperties accessCfg;
	
	private ConnectionManager connectionManager;
	private MainWindow mainWindow;
	private TunTap tuntap;
	
	private int serverPort;
	private String name;
	private double sendLimit, recLimit;
	private int sendBufferSize;
	private boolean tcpFlush;
	private String ip;

	private Preferences prefs;
	
	public MainControl(MainWindow mainWindow) {
		tuntap = null;
		connectionManager = null;
		this.mainWindow = mainWindow;
		prefs = Preferences.userNodeForPackage(MainControl.class);
		serverPort = prefs.getInt("serverPort", 0);
		setName(prefs.get("name", "no name"));
		String accessStr = prefs.get("access", null);
		accessCfg = accessStr==null ? null : new AdvProperties(accessStr);
		String netStr = prefs.get("network", null);
		networkCfg = netStr==null ? null : new AdvProperties(netStr);
		ip = prefs.get("ip", "");
		sendLimit = prefs.getDouble("sendLimit", 0);
		recLimit = prefs.getDouble("recLimit", 0);
		sendBufferSize = prefs.getInt("sendBufferSize", TCPConnection.DEFAULT_MAX_QUEUE);
		tcpFlush = prefs.getBoolean("tcpFlush", TCPConnection.DEFAULT_TCP_FLUSH);

		if (accessCfg == null) loadDefaultNet();
	}

	private void loadDefaultNet() {
		try {
			InputStream in = MainControl.class.getClassLoader().getResourceAsStream(DEFAULT_NET_FILE);
			AdvProperties inv = new AdvProperties();
			inv.load(in);
			String netName = inv.getProperty("network.name");
			if (JOptionPane.YES_OPTION ==
					JOptionPane.showConfirmDialog(null,
					"Your P2PVPN is not part of any network.\n" +
					"Do you want to join '" + netName + "'?", "Default Network",
					JOptionPane.YES_NO_OPTION)) {
				AdvProperties[] ps = calcNetworkAccess(inv);
				networkCfg = ps[0];
				accessCfg = ps[1];
				generateRandomIP();
			}
		} catch (IOException iOException) {
		} catch (HeadlessException headlessException) {
		}
	}

	public void start() {
		changeNet(false);
	}
	
	public void connectToNewNet(AdvProperties networkCfg, AdvProperties accessCfg) {
		this.networkCfg = networkCfg;
		this.accessCfg = accessCfg;
		
		if (accessCfg!=null) generateRandomIP();
		
		changeNet(true);
	}
	
	private void generateRandomIP() {
		try {
			Random random = new Random();
			InetAddress net = InetAddress.getByName(accessCfg.getProperty("network.ip.network"));
			InetAddress subnet = InetAddress.getByName(accessCfg.getProperty("network.ip.subnet"));

			byte[] myIPb = new byte[4];
			byte[] netb = net.getAddress();
			byte[] subnetb = subnet.getAddress();

			// TODO don't create a broadcast address
			for (int i = 0; i < 4; i++) {
				myIPb[i] = (byte) (netb[i] ^ ((~subnetb[i]) & (byte)random.nextInt()));
			}

			ip = (0xFF & myIPb[0]) + "." + (0xFF & myIPb[1]) + "." + (0xFF & myIPb[2]) + "." + (0xFF & myIPb[3]);
		} catch (UnknownHostException ex) {
			Logger.getLogger(MainControl.class.getName()).log(Level.SEVERE, null, ex);
			assert false;
		}
	}
	
	private void changeNet(boolean networkChanged) {
		if (connectionManager!=null) connectionManager.close();
		if (accessCfg!=null) {
			try {
				connectionManager = new ConnectionManager(accessCfg, serverPort);

				try {
					VPNConnector vpnc = VPNConnector.getVPNConnector();
					vpnc.setRouter(connectionManager.getRouter());
					tuntap = vpnc.getTunTap();
					setIp(ip);
				} catch (Throwable e) {
					Logger.getLogger("").log(Level.SEVERE, "", e);
					JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				connectionManager.getRouter().setLocalPeerInfo("name", name);
				connectionManager.addIPs(accessCfg);

				connectionManager.getConnector().addListener(this);
				if (!networkChanged) addStoredIPs();

				connectionManager.getSendLimit().setBandwidth(sendLimit);
				connectionManager.getRecLimit().setBandwidth(recLimit);
				connectionManager.setSendBufferSize(sendBufferSize);
				connectionManager.setTCPFlush(tcpFlush);

				prefs.put("access", accessCfg.toString());
				if (networkCfg==null) {
					prefs.remove("network");
				} else {
					prefs.put("network", networkCfg.toString());
				}
				prefs.put("ip", ip);
				prefsFlush();
			} catch (Throwable e) {
				Logger.getLogger("").log(Level.SEVERE, "", e);
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}		
		}
		mainWindow.networkHasChanged();
	}

	private void addStoredIPs() {
		String ipStr = prefs.get("knownIPs", "");
		StringTokenizer ips = new StringTokenizer(ipStr, ";");

		while (ips.hasMoreTokens()) {
			try {
				StringTokenizer st = new StringTokenizer(ips.nextToken(), ":");
				String ip = st.nextToken();
				int port = Integer.parseInt(st.nextToken());
				System.out.println("add "+ip+":"+port);
				connectionManager.getConnector().addIP(ip, port, null, "stored", "", false);
			} catch (NumberFormatException numberFormatException) {
			}
		}
	}

	public void ipListChanged(Connector c) {
		Connector.Endpoint[] es = c.getIPs();
		String ips = "";
		for (int i = 0; i < es.length; i++) {
			if (i > 0) {
				ips = ips + ";";
			}
			ips = ips + es[i].toString();
		}
		prefs.put("knownIPs", ips);
		prefsFlush();
	}



	public String nameForPeer(PeerID peer) {
		if (connectionManager==null) return "";
		String name = connectionManager.getRouter().getPeerInfo(peer, "name");
		if (name==null) return "?";
		return name;
	}
	
	public String descriptionForPeer(PeerID peer) {
		if (connectionManager==null) return "";
		
		StringBuffer result = new StringBuffer();
		
		result.append("<html>");
		result.append("Name: "+nameForPeer(peer));
		
		String ip = connectionManager.getRouter().getPeerInfo(peer, "vpn.ip");
		if (ip!=null) result.append("<br>IP: "+ip);
		
		if (connectionManager.getRouter().isConnectedTo(peer)) {
			result.append("<br>direct connection");
		} else {
			if (!connectionManager.getLocalAddr().equals(peer)) {
				result.append("<br>indirect connection");
			}
		}
		
		result.append("<br>Peer ID: "+peer);
		result.append("</html>");
		
		return result.toString();
	}

	private void prefsFlush() {
		try {
			prefs.flush();
		} catch (BackingStoreException ex) {
			Logger.getLogger("").log(Level.WARNING, null, ex);
		}
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;

		if (tuntap!=null) {
			tuntap.setIP(ip, accessCfg.getProperty("network.ip.subnet"));
			if (connectionManager!=null) {
				connectionManager.getRouter().setLocalPeerInfo("vpn.ip", ip);
			}
			prefs.put("ip", ip);
			prefsFlush();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		if (connectionManager!=null) {
			connectionManager.getRouter().setLocalPeerInfo("name", name);			
		}
		mainWindow.setNodeName(name);
		prefs.put("name", name);
		prefsFlush();
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
		prefs.putInt("serverPort", serverPort);
		prefsFlush();
	}

	public AdvProperties getAccessCfg() {
		return accessCfg;
	}

	public AdvProperties getNetworkCfg() {
		return networkCfg;
	}


	public static AdvProperties genereteAccess(AdvProperties netCfg) {
		PrivateKey netPriv = CryptoUtils.decodeRSAPrivateKey(
				netCfg.getPropertyBytes("secret.network.privateKey", null));

		KeyPair accessKp = CryptoUtils.createEncryptionKeyPair();

		AdvProperties accessCfg = new AdvProperties();
		accessCfg.setProperty("access.expiryDate", "none");
		accessCfg.setPropertyBytes("access.publicKey", accessKp.getPublic().getEncoded());
		accessCfg.sign("access.signature", netPriv);
		accessCfg.setPropertyBytes("secret.access.privateKey", accessKp.getPrivate().getEncoded());

		accessCfg.putAll(netCfg.filter("secret", true));

		return accessCfg;
	}
	
	public static AdvProperties[] calcNetworkAccess(AdvProperties inv) {
		AdvProperties net;
		AdvProperties access;

		if (inv.getPropertyBytes("secret.network.privateKey", null)==null) {
			net = null;
			access = inv;
		} else {
			net = inv;
			access = MainControl.genereteAccess(net);
		}

		return new AdvProperties[] {net, access};
	}

	public double getRecLimit() {
		return recLimit;
	}

	public void setRecLimit(double recLimit) {
		this.recLimit = recLimit;
		if (connectionManager!=null) connectionManager.getRecLimit().setBandwidth(recLimit);
		prefs.putDouble("recLimit", recLimit);
		prefsFlush();
	}

	public double getSendLimit() {
		return sendLimit;
	}

	public void setSendLimit(double sendLimit) {
		this.sendLimit = sendLimit;
		if (connectionManager!=null) connectionManager.getSendLimit().setBandwidth(sendLimit);
		prefs.putDouble("sendLimit", sendLimit);
		prefsFlush();
	}

	public int getSendBufferSize() {
		return sendBufferSize;
	}

	public void setSendBufferSize(int sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
		if (connectionManager!=null) connectionManager.setSendBufferSize(sendBufferSize);
		prefs.putInt("sendBufferSize", sendBufferSize);
		prefsFlush();
	}

	public boolean isTCPFlush() {
		return tcpFlush;
	}

	public void setTCPFlush(boolean tcpFlush) {
		this.tcpFlush = tcpFlush;
		if (connectionManager!=null) connectionManager.setTCPFlush(tcpFlush);
		prefs.putBoolean("tcpFlush", tcpFlush);
		prefsFlush();
	}
}
