/*
    Copyright 2008, 2009 Wolfgang Ginolas

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
package org.p2pvpn.network.bittorrent;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;
import org.p2pvpn.network.bittorrent.bencode.Bencode;
import org.p2pvpn.network.bittorrent.bencode.BencodeMap;
import org.p2pvpn.network.bittorrent.bencode.BencodeObject;
import org.p2pvpn.network.bittorrent.bencode.BencodeString;

public class DHT {
	private static final int PACKET_LEN = 10*1024;
	private static final BigInteger MASK = new BigInteger("1").shiftLeft(160);

	private DatagramSocket dSock;

	private BencodeString id =       new BencodeString("01234567890123456789");
	private BencodeString searchID = new BencodeString("abcdefghijabcdefghij");
	private BigInteger searchIDInt = unsigned(new BigInteger(searchID.getBytes()));

	private PriorityBlockingQueue<Contact> peerQueue;

	public DHT(DatagramSocket dSock) {
		this.dSock = dSock;

		peerQueue = new PriorityBlockingQueue<Contact>(10, new Comparator<Contact>() {
			public int compare(Contact o1, Contact o2) {
				BigInteger d1 = searchDist(o1);
				BigInteger d2 = searchDist(o2);
				return d1.compareTo(d2);
			}
		});

		new Thread(new Runnable() {
			public void run() {
				recvThread();
			}
		}).start();

		testPing();
	}

	private BigInteger searchDist(Contact c) {
		return unsigned(searchIDInt.xor(c.getId()));
	}

	public static BigInteger unsigned(BigInteger i) {
		if (i.signum()<0) {
			return MASK.add(i);
		} else return i;
	}

	private void ping(SocketAddress addr) throws IOException {
		BencodeMap m = new BencodeMap();
		m.put(new BencodeString("t"), new BencodeString("ping"));
		m.put(new BencodeString("y"), new BencodeString("q"));
		m.put(new BencodeString("q"), new BencodeString("ping"));
		BencodeMap a = new BencodeMap();
		a.put(new BencodeString("id"), id);
		m.put(new BencodeString("a"), a);
		System.out.println("ping: "+addr);
		sendPacket(addr, m);
	}

	private void getPeers(Contact c) throws IOException {
		BencodeMap m = new BencodeMap();
		m.put(new BencodeString("t"), new BencodeString("get_peers"));
		m.put(new BencodeString("y"), new BencodeString("q"));
		m.put(new BencodeString("q"), new BencodeString("get_peers"));
		BencodeMap a = new BencodeMap();
		a.put(new BencodeString("id"), id);
		a.put(new BencodeString("info_hash"), searchID);
		m.put(new BencodeString("a"), a);
		//System.out.println("get_peers: "+c);
		sendPacket(c.getAddr(), m);
	}

	private void testPing() {
		try {
			while (true) {
				Thread.sleep(1000);
				if (peerQueue.isEmpty()) {
					ping(new InetSocketAddress("router.utorrent.com", 6881));
				} else {
					Vector<Contact> best = new Vector<Contact>();
					{
						Contact c;
						while (best.size()<16 && null!=(c=peerQueue.poll())) {
							if (!best.contains(c)) {
								best.add(c);
								System.out.println("best: dist: "+searchDist(c).bitLength()+"  peer: "+c);
							}
						}
						System.out.println();
					}
					for (Contact c : best) {
						getPeers(c);
						peerQueue.add(c);
					}
				}
			}
			//sendPacket(new InetSocketAddress("router.bittorrent.com", 6881), m);
			//sendPacket(new InetSocketAddress("192.168.0.102", 51413), m);
		} catch (Exception ex) {
			ex.printStackTrace(); // TODO
		}
	}

	private void sendPacket(SocketAddress addr, BencodeObject o) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		//System.out.println("send "+addr+": "+o);
		o.write(out);
		byte[] buf = out.toByteArray();
		//System.out.println("test1: "+new String(buf));
		//BencodeObject to = Bencode.parseBencode(new ByteArrayInputStream(buf));
		//System.out.println("test2: "+to);
		DatagramPacket p = new DatagramPacket(buf, buf.length, addr);
		dSock.send(p);
	}

	private void addQueue(Contact c) {
		//System.out.println("add: dist: "+searchDist(c).bitLength()+"  peer: "+c);
		peerQueue.add(c);
	}

	private void recvPacket(DatagramPacket p) {
		try {
			BencodeObject o = Bencode.parseBencode(new ByteArrayInputStream(p.getData(), 0, p.getLength()));
			//System.out.println("recv: "+o);
			if (((BencodeMap)o).get(new BencodeString("y")).equals(new BencodeString("r"))) {
				String t = ((BencodeMap)o).get(new BencodeString("t")).toString();
				BencodeMap r = (BencodeMap)((BencodeMap)o).get(new BencodeString("r"));
				//System.out.println("t: "+t);
				if (t.equals("ping")) {
					BencodeString rid = (BencodeString)r.get(new BencodeString("id"));
					addQueue(new Contact(rid.getBytes(), p.getSocketAddress()));
				}
				if (t.equals("get_peers")) {
					BencodeString rid = (BencodeString)r.get(new BencodeString("id"));
					BencodeString nodes = (BencodeString)r.get(new BencodeString("nodes"));
					if (nodes!=null) {
						byte[] bs = nodes.getBytes();
						for(int i=0; i+25<bs.length; i+=26) {
							addQueue(new Contact(bs, i));
						}
					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace(); // TODO
		}
	}

	private void recvThread() {
		try {
			byte[] buf = new byte[PACKET_LEN];
			DatagramPacket p = new DatagramPacket(buf, PACKET_LEN);
			while (true) {
				dSock.receive(p);
				recvPacket(p);
			}
		} catch (IOException iOException) {
			iOException.printStackTrace(); // TODO
		}
	}

	public static void main(String args[]) {
		try {
			new DHT(new DatagramSocket(12345));
		} catch (SocketException ex) {
			ex.printStackTrace();
		}
	}

	public BigInteger getID() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}