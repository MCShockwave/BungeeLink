package net.mcshockwave.bungee;

import net.mcshockwave.bungee.SQLTable.Rank;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BungeeLink extends Plugin implements Listener {

	public static BungeeLink		ins;

	Random							rand	= new Random();

	public HashMap<String, String>	replCom	= new HashMap<>();

	@Override
	public void onEnable() {
		ins = this;

		String[] channels = { "MCShockwave", "MCSServer", "MCSTips", "MCSServerPing", "MCSFriendPing",
				"MCSFriendsList", "SendMessage", "MCSTellRank", "MCSPrivMes", "MCSReplyMes" };
		for (String s : channels) {
			getProxy().registerChannel(s);
		}
		this.getProxy().getPluginManager().registerListener(this, this);

		SQLTable.enable();

		getProxy().getScheduler().schedule(this, new Runnable() {
			public void run() {
				for (String si : getProxy().getServers().keySet()) {
					List<String> tipL = SQLTable.Tips.getAll("Server", si, "Tip");
					for (String s : SQLTable.Tips.getAll("Server", "all", "Tip")) {
						tipL.add(s);
					}
					if (si.startsWith("mg")) {
						for (String s : SQLTable.Tips.getAll("Server", "mg", "Tip")) {
							tipL.add(s);
						}
					}
					String[] tips = tipL.toArray(new String[0]);
					if (tips.length > 0 && getProxy().getServers().get(si).getPlayers().size() > 0) {
						sendMessage(tips[rand.nextInt(tips.length)], getProxy().getServers().get(si), "MCSTips");
					}
				}
			}
		}, 2, 2, TimeUnit.MINUTES);

		getProxy().getScheduler().schedule(this, new Runnable() {
			public void run() {
				for (String s : getProxy().getServers().keySet()) {
					ServerInfo si = getProxy().getServerInfo(s);

					sendMessage(s, si, "MCSServerPing");
				}
			}
		}, 2, 2, TimeUnit.MINUTES);

		updateMOTD();
	}

	public void updateMOTD() {
		motd = SQLTable.Settings.get("Setting", "MOTD_Prefix", "Value")
				+ SQLTable.Settings.get("Setting", "MOTD", "Value");

		maxplayers = SQLTable.Settings.getInt("Setting", "MaxPlayers", "Value");
	}

	public void sendMessage(String mes, ServerInfo server, String dataName) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(stream);
		try {
			out.writeUTF(mes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.sendData(dataName, stream.toByteArray());
	}

	public static String	motd		= "";
	public static int		maxplayers	= -1;

	@EventHandler
	public void serverPing(ProxyPingEvent event) {
		ServerPing sp = event.getResponse();
		sp.setDescription(motd);
		sp.setPlayers(new Players(maxplayers, sp.getPlayers().getOnline(), sp.getPlayers().getSample()));
		event.setResponse(sp);
		
		try {
			updateMOTD();
		} catch (Exception e) {
		}
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent event) {

		if (event.getTag().equalsIgnoreCase("SendMessage")) {

			if (!(event.getSender() instanceof Server)) {
				return;
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
			DataInputStream in = new DataInputStream(stream);
			String message = null;
			String player = null;
			try {
				message = in.readUTF();
				player = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (message != null && player != null) {
				ProxiedPlayer pp = getProxy().getPlayer(player);
				if (pp != null) {
					sendMessage(player + "::" + message, pp.getServer().getInfo(), "SendMessage");
				}
			}

		}

		if (event.getTag().equalsIgnoreCase("MCSPrivMes")) {
			if (!(event.getSender() instanceof Server)) {
				return;
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
			DataInputStream in = new DataInputStream(stream);
			String message = null;
			String sender = null;
			String receiver = null;
			try {
				message = in.readUTF();
				sender = in.readUTF();
				receiver = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Collection<ProxiedPlayer> ps = getProxy().getPlayers();

			boolean exact = false;
			for (ProxiedPlayer pp : ps) {
				if (pp.getName().equalsIgnoreCase(receiver)) {
					exact = true;
					break;
				}
			}

			if (!exact) {
				for (ProxiedPlayer pp : ps) {
					if (pp.getName().toLowerCase().startsWith(receiver.toLowerCase())) {
						receiver = pp.getName();
						break;
					}
				}
			}

			if (getProxy().getPlayer(receiver) == null) {
				sendTo(getProxy().getPlayer(sender), "§cPlayer not found: '" + receiver + "'");
				return;
			}
			for (ProxiedPlayer pp : getProxy().getPlayers()) {
				if (pp.getName().equalsIgnoreCase(sender)) {
					sendTo(pp, "§7[You -> " + receiver + "("
							+ getProxy().getPlayer(receiver).getServer().getInfo().getName() + ")] " + message);
					continue;
				}
				if (pp.getName().equalsIgnoreCase(receiver)) {
					sendTo(pp, "§7[" + sender + "(" + getProxy().getPlayer(sender).getServer().getInfo().getName()
							+ ") -> You] " + message);
					continue;
				}
				if (SQLTable.hasRank(pp.getName(), Rank.JR_MOD)) {
					sendTo(pp, "§7[" + sender + "(" + getProxy().getPlayer(sender).getServer().getInfo().getName()
							+ ") -> " + receiver + "(" + getProxy().getPlayer(receiver).getServer().getInfo().getName()
							+ ")] " + message);
				}
			}

			replCom.remove(sender);
			replCom.put(sender, receiver);

			replCom.remove(receiver);
			replCom.put(receiver, sender);

		}

		if (event.getTag().equalsIgnoreCase("MCSReplyMes")) {
			if (!(event.getSender() instanceof Server)) {
				return;
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
			DataInputStream in = new DataInputStream(stream);
			String message = null;
			String sender = null;
			try {
				message = in.readUTF();
				sender = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (!replCom.containsKey(sender) && getProxy().getPlayer(sender) != null) {
				sendTo(getProxy().getPlayer(sender), "§cNo recent conversations found");
				return;
			}
			String receiver = replCom.get(sender);

			if (getProxy().getPlayer(receiver) == null) {
				sendTo(getProxy().getPlayer(sender), "§cPlayer not found: '" + receiver + "'");
				return;
			}
			for (ProxiedPlayer pp : getProxy().getPlayers()) {
				if (pp.getName().equalsIgnoreCase(sender)) {
					sendTo(pp, "§7[You -> " + receiver + "("
							+ getProxy().getPlayer(receiver).getServer().getInfo().getName() + ")] " + message);
					continue;
				}
				if (pp.getName().equalsIgnoreCase(receiver)) {
					sendTo(pp, "§7[" + sender + "(" + getProxy().getPlayer(sender).getServer().getInfo().getName()
							+ ") -> You] " + message);
					continue;
				}
				if (SQLTable.hasRank(pp.getName(), Rank.JR_MOD)) {
					sendTo(pp, "§7[" + sender + "(" + getProxy().getPlayer(sender).getServer().getInfo().getName()
							+ ") -> " + receiver + "(" + getProxy().getPlayer(receiver).getServer().getInfo().getName()
							+ ")] " + message);
				}
			}

			replCom.remove(sender);
			replCom.put(sender, receiver);

			replCom.remove(receiver);
			replCom.put(receiver, sender);
		}

		if (event.getTag().equalsIgnoreCase("MCShockwave")) {

			if (!(event.getSender() instanceof Server)) {
				return;
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
			DataInputStream in = new DataInputStream(stream);
			String message = "";
			try {
				message = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (ServerInfo si : getProxy().getServers().values()) {
				if (si.getPlayers().size() < 1) {
					continue;
				}
				sendMessage(message, si, "MCShockwave");
			}

		}

		if (event.getTag().equalsIgnoreCase("MCSServer")) {

			if (!(event.getSender() instanceof Server)) {
				return;
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
			DataInputStream in = new DataInputStream(stream);
			String message = "";
			try {
				message = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] ss = message.split(":::");

			String mes = ss[1];
			String ser = ss[0];

			ServerInfo si = getProxy().getServerInfo(ser);
			sendMessage(mes, si, "MCSServer");
		}

		if (event.getTag().equalsIgnoreCase("MCSServerPing")) {
			if (!(event.getSender() instanceof Server)) {
				return;
			}

			Server s = (Server) event.getSender();

			ServerInfo si = s.getInfo();
			sendMessage(s.getInfo().getName(), si, "MCSServerPing");
		}

		if (event.getTag().equalsIgnoreCase("MCSFriendPing")) {

			if (!(event.getSender() instanceof Server)) {
				return;
			}

			Server s = (Server) event.getSender();

			ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
			DataInputStream in = new DataInputStream(stream);
			String message = "";
			try {
				message = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] ss = message.split(":::");

			String ps = ss[0];
			String pp = ss[1];

			ProxiedPlayer proP = getProxy().getPlayer(pp);

			if (proP != null) {
				sendMessage("StatCommand::" + ps + "::" + pp + "::" + proP.getServer().getInfo().getName(),
						s.getInfo(), "MCSFriendPing");
			} else {
				sendMessage("StatCommand::" + ps + "::" + pp + "::" + "Offline", s.getInfo(), "MCSFriendPing");
			}
		}

		if (event.getTag().equalsIgnoreCase("MCSTellRank")) {
			if (!(event.getSender() instanceof Server)) {
				return;
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
			DataInputStream in = new DataInputStream(stream);
			String message = "";
			String rank = "";
			try {
				message = in.readUTF();
				rank = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Rank r = Rank.valueOf(rank.toUpperCase());

			for (ProxiedPlayer pp : getProxy().getPlayers()) {
				if (SQLTable.hasRank(pp.getName(), r)) {
					sendTo(pp, message);
				}
			}

		}

		if (event.getTag().equalsIgnoreCase("MCSFriendsList")) {

			if (!(event.getSender() instanceof Server)) {
				return;
			}

			ByteArrayInputStream stream = new ByteArrayInputStream(event.getData());
			DataInputStream in = new DataInputStream(stream);
			String player = "";
			try {
				player = in.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}

			ProxiedPlayer p = getProxy().getPlayer(player);

			ArrayList<String> dobFr = new ArrayList<String>();
			ArrayList<String> hasFr = new ArrayList<String>();
			ArrayList<String> friBy = new ArrayList<String>();

			String pfs = SQLTable.Friends.get("Username", player, "Friends");
			String[] pfss = pfs.split(",");
			if (pfss.length > 1) {
				for (int i = 1; i < pfss.length; i++) {
					hasFr.add(pfss[i]);
				}

				ArrayList<String> fAll = SQLTable.Friends.getAll("Friends");
				ArrayList<String> fdAll = SQLTable.Friends.getAll("Username");
				for (int fi = 0; fi < fAll.size(); fi++) {
					String s = fAll.get(fi);
					for (String ss : s.split(",")) {
						if (ss.equalsIgnoreCase(p.getName())) {
							friBy.add(fdAll.get(fi));
						}
					}
				}

				String[] hasfrie = hasFr.toArray(new String[0]);
				String[] isfriBy = friBy.toArray(new String[0]);
				for (String s : hasfrie) {
					for (String s2 : isfriBy) {
						if (s2.equalsIgnoreCase(s)) {
							dobFr.add(s);
							hasFr.remove(s);
							friBy.remove(s);
						}
					}
				}

				String nfs = "";
				for (String s : dobFr) {
					ChatColor cc = (getProxy().getPlayer(s) != null ? ChatColor.GREEN : ChatColor.RED);
					nfs += ChatColor.DARK_GRAY + ", " + cc + s;
				}
				nfs = nfs.replaceFirst(", ", "");

				String frd = "";
				for (String s : hasFr) {
					ChatColor cc = (getProxy().getPlayer(s) != null ? ChatColor.GREEN : ChatColor.RED);
					frd += ChatColor.DARK_GRAY + ", " + cc + s;
				}
				frd = frd.replaceFirst(", ", "");

				String fri = "";
				for (String s : friBy) {
					ChatColor cc = (getProxy().getPlayer(s) != null ? ChatColor.GREEN : ChatColor.RED);
					fri += ChatColor.DARK_GRAY + ", " + cc + s;
				}
				fri = fri.replaceFirst(", ", "");

				// p.sendMessage(m(" "));
				// p.sendMessage(m(ChatColor.BLACK + "--- " + ChatColor.GOLD +
				// "[Friends]" + ChatColor.BLACK + " ---"));
				// p.sendMessage(m(nfs));
				// p.sendMessage(m(" "));
				//
				// p.sendMessage(m(ChatColor.BLACK + "--- " + ChatColor.GOLD +
				// "[You Friended]" + ChatColor.BLACK + " ---"));
				// p.sendMessage(m(frd));
				// p.sendMessage(m(" "));
				//
				// p.sendMessage(m(ChatColor.BLACK + "--- " + ChatColor.GOLD +
				// "[Friended By]" + ChatColor.BLACK + " ---"));
				// p.sendMessage(m(fri));
				// p.sendMessage(m(fri));

			} else {
				sendTo(p, ChatColor.RED + "You don't have any friends! Add some! /friend [name]");
			}
		}
	}

	@EventHandler
	public void onPlayerConnect(PreLoginEvent event) {
		String ip = event.getConnection().getAddress().getAddress().getHostAddress();
		if (SQLTable.IPBans.has("IP", ip)) {
			event.setCancelled(true);
			event.setCancelReason(String.format(
					"§aBanned by %s: §f%s §b[Permanent]", 
					SQLTable.IPBans.get("IP", ip, "BannedBy"), 
					SQLTable.IPBans.get("IP", ip, "Reason")).replace("Banned", "IP-Banned") 
					+ "      §cIf you feel you were wrongfully banned, appeal on our site at §b§ohttp://forums.mcshockwave.net/");
		}
	}
	
	@EventHandler
	public void onPlayerChangeServer(ServerSwitchEvent event) {
		pingPlayer(event.getPlayer(), event.getPlayer().getServer());
	}

	@EventHandler
	public void onPlayerLeave(PlayerDisconnectEvent event) {
		pingPlayer(event.getPlayer(), null);
	}

	public void pingPlayer(ProxiedPlayer p, Server s) {
		String sname = "";
		if (s == null) {
			sname = "Offline";
		} else
			sname = s.getInfo().getName();
		for (ProxiedPlayer gp : getProxy().getPlayers()) {
			if (SQLTable.Friends.has("Username", gp.getName())) {
				if (SQLTable.Friends.get("Username", gp.getName(), "Friends").contains(p.getName())
						&& SQLTable.Friends.get("Username", p.getName(), "Friends").contains(gp.getName())) {
					sendMessage("StatusChange::" + gp.getName() + "::" + p.getName() + "::" + sname, gp.getServer()
							.getInfo(), "MCSFriendPing");
				}
			}
		}
	}

	public void sendTo(ProxiedPlayer pp, String m) {
		sendMessage(pp.getName() + "::" + m, pp.getServer().getInfo(), "SendMessage");
	}

	//
	// @EventHandler
	// public void onPlayerKickFromServer(ServerKickEvent event) {
	// ProxiedPlayer p = event.getPlayer();
	//
	// if (!event.getKickReason().startsWith(ChatColor.GREEN + "Kicked")) {
	// event.setCancelled(true);
	//
	// ServerInfo hub = getProxy().getServerInfo("hub");
	// p.sendMessage(ChatColor.RED + "Kicked from " +
	// p.getServer().getInfo().getName() + ": " + ChatColor.WHITE
	// + event.getKickReason());
	// p.connect(hub);
	// }
	// }

}
