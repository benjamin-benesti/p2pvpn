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

/*
 * Main.java
 *
 * Created on 29. Oktober 2008, 11:43
 */

package org.p2pvpn.gui;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.Map;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import org.p2pvpn.network.PeerID;
import org.p2pvpn.network.ConnectionManager;
import org.p2pvpn.network.Router;
import org.p2pvpn.network.RoutungTableListener;
import org.p2pvpn.network.UPnPPortForward;
import org.p2pvpn.network.UPnPPortForwardListener;

/**
 *
 * @author  wolfgang
 */
public class InfoWindow extends javax.swing.JFrame implements RoutungTableListener, UPnPPortForwardListener {
	private static final long serialVersionUID = -7583281386025886297L;


	private static final int MAX_LOG_LEN = 10*1000;
	
	private ConnectionManager connectionManager;
    private MainControl mainControl;
	private PeerID addrShown = null;

	private PeerGraph peerGraph;
	
	/** Creates new form Main */
    public InfoWindow(MainControl mainControl) {
        this.mainControl = mainControl;
        this.connectionManager = null;
    	setLocationByPlatform(true);
    	
        initComponents();
		
		peerGraph = new PeerGraph();
		pnlPeerGraph.setLayout(new BorderLayout());
		pnlPeerGraph.add(peerGraph);

        peerTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                for(int i=e.getFirstIndex(); i<=e.getLastIndex(); i++) {
                    if (peerTable1.getSelectionModel().isSelectedIndex(i)) {
                        peerSelected(i);
                        break;
                    }
                }
            }
        });
        try {
            URL url = InfoWindow.class.getClassLoader().getResource("resources/images/info.png");
            setIconImage(new ImageIcon(url).getImage());
        } catch(NullPointerException e) {}
        startLogging();
    }

    void networkHasChanged() {
        connectionManager = mainControl.getConnectionManager();
        if (connectionManager != null) {
            peerTable1.setModel(new PeerTableModel(connectionManager));
            ipTable.setModel(new IPTableModel(connectionManager));
            setLocalInfo(
                    "ID: "+connectionManager.getLocalAddr()+
                    "  Port: "+connectionManager.getServerPort());
            connectionManager.getRouter().addTableListener(InfoWindow.this);
            //connectionManager.getUPnPPortForward().addListener(Main.this);
        }
		peerGraph.setConnectionManager(connectionManager);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        peerInfo = new javax.swing.JTextArea();
        aPanel1 = new javax.swing.JPanel();
        connectBtn1 = new javax.swing.JButton();
        hostConnectText1 = new javax.swing.JTextField();
        localInfo1 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        peerTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ipTable = new javax.swing.JTable();
        pnlPeerGraph = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        logText = new javax.swing.JTextArea();

        setTitle("P2PVPN");

        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setContinuousLayout(true);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Info"));

        peerInfo.setColumns(20);
        peerInfo.setEditable(false);
        peerInfo.setRows(5);
        peerInfo.setText(" ");
        jScrollPane3.setViewportView(peerInfo);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel2);

        aPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Connections"));

        connectBtn1.setText("Connect To");
        connectBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventConnectTo(evt);
            }
        });

        hostConnectText1.setToolTipText("host:port");

        localInfo1.setText(" ");

        peerTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        peerTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(peerTable1);

        javax.swing.GroupLayout aPanel1Layout = new javax.swing.GroupLayout(aPanel1);
        aPanel1.setLayout(aPanel1Layout);
        aPanel1Layout.setHorizontalGroup(
            aPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aPanel1Layout.createSequentialGroup()
                .addGroup(aPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(localInfo1, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE))
                    .addGroup(aPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(connectBtn1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hostConnectText1, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE))
                    .addGroup(aPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)))
                .addContainerGap())
        );
        aPanel1Layout.setVerticalGroup(
            aPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aPanel1Layout.createSequentialGroup()
                .addComponent(localInfo1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(aPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostConnectText1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectBtn1))
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(aPanel1);

        jTabbedPane1.addTab("Connections", jSplitPane1);

        ipTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(ipTable);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Known IPs", jPanel1);

        javax.swing.GroupLayout pnlPeerGraphLayout = new javax.swing.GroupLayout(pnlPeerGraph);
        pnlPeerGraph.setLayout(pnlPeerGraphLayout);
        pnlPeerGraphLayout.setHorizontalGroup(
            pnlPeerGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 564, Short.MAX_VALUE)
        );
        pnlPeerGraphLayout.setVerticalGroup(
            pnlPeerGraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 445, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Peer Graph", pnlPeerGraph);

        logText.setColumns(20);
        logText.setRows(5);
        jScrollPane5.setViewportView(logText);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Log", jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 568, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void eventConnectTo(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventConnectTo
	connectionManager.connectTo(hostConnectText1.getText());
}//GEN-LAST:event_eventConnectTo

	private void peerSelected(int i) {
		if (i<0) {
			return;
		}
		
		addrShown = ((PeerTableModel)peerTable1.getModel()).getPeerID(i);
		tableChanged(null);
	}	

	public void setLocalInfo(String s) {
		localInfo1.setText(s);
	}

	public void tableChanged(Router router) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tableChangedSave();
			}
		});
	}

	public void tableChangedSave() {
		StringBuffer info = new StringBuffer();
		info.append("Info for "+addrShown+"\n\n");
		Map<String, String> map = connectionManager.getRouter().getPeerInfo(addrShown);
		if (map==null) {
			peerInfo.setText("");
			return;
		}

		for(Map.Entry<String, String> e : map.entrySet()) {
			info.append(e.getKey()+"="+e.getValue()+"\n");
		}

		peerInfo.setText(info.toString());
	}

	public void upnpChanged(UPnPPortForward upnp) {
		/*
		InternetGatewayDevice igd = upnp.getIgd();
		if (igd!=null) {
			upnpText.setText("Internet Gateway Device: "+igd.getIGDRootDevice().getModelName()+"\n"+
					"External IP: "+upnp.getExternalIP()+"\n" +
					"Port mapped: "+upnp.isMapped()+"\n" +
					"Error: "+upnp.getError());
		} else {
			upnpText.setText("Internet Gateway Device: not found");
		}
		*/
	}
	
	public void startLogging() {
		LoggingWriter lt = new LoggingWriter();
		lt.setFormatter(new SimpleFormatter());
		
		Logger.getLogger("").addHandler(lt);
	}
	
	class LoggingWriter extends Handler {

		public LoggingWriter() {
			super();
		}
		
		@Override
		public void publish(LogRecord r) {
			try {
				String s = getFormatter().format(r);
				Document d = logText.getDocument();
				d.insertString(d.getLength(), s, null);
				if (d.getLength() > MAX_LOG_LEN) {
					d.remove(0, d.getLength()/2);
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
		

	}
	
	// TODO remove & rename variables
	
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aPanel1;
    private javax.swing.JButton connectBtn1;
    private javax.swing.JTextField hostConnectText1;
    private javax.swing.JTable ipTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel localInfo1;
    private javax.swing.JTextArea logText;
    private javax.swing.JTextArea peerInfo;
    private javax.swing.JTable peerTable1;
    private javax.swing.JPanel pnlPeerGraph;
    // End of variables declaration//GEN-END:variables


    
}
