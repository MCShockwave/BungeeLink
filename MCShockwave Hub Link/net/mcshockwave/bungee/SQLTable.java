package net.mcshockwave.bungee;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class SQLTable {
	public static ArrayList<SQLTable>	tables				= new ArrayList<>();

	public static final SQLTable		ADMINS				= new SQLTable("ADMINS");
	public static final SQLTable		BanHistory			= new SQLTable("BanHistory");
	public static final SQLTable		Banned				= new SQLTable("Banned");
	public static final SQLTable		BattleBaneItems		= new SQLTable("BattleBaneItems");
	public static final SQLTable		Coins				= new SQLTable("Coins");
	public static final SQLTable		CurrentChallenges	= new SQLTable("CurrentChallenges");
	public static final SQLTable		Dojo				= new SQLTable("Dojo");
	public static final SQLTable		ForceCosts			= new SQLTable("ForceCosts");
	public static final SQLTable		ForceCooldowns		= new SQLTable("ForceCooldowns");
	public static final SQLTable		Friends				= new SQLTable("Friends");
	public static final SQLTable		IPBans				= new SQLTable("IPBans");
	public static final SQLTable		IPLogs				= new SQLTable("IPLogs");
	public static final SQLTable		JunMODS				= new SQLTable("JunMODS");
	public static final SQLTable		Level				= new SQLTable("Level");
	public static final SQLTable		MinigameMaps		= new SQLTable("MinigameMaps");
	public static final SQLTable		MiscItems			= new SQLTable("MiscItems");
	public static final SQLTable		ModCommands			= new SQLTable("ModCommands");
	public static final SQLTable		MODS				= new SQLTable("MODS");
	public static final SQLTable		Muted				= new SQLTable("Muted");
	public static final SQLTable		MynerimItems		= new SQLTable("MynerimItems");
	public static final SQLTable		NetMultipliers		= new SQLTable("NetMultipliers");
	public static final SQLTable		nickNames			= new SQLTable("nickNames");
	public static final SQLTable		OldUsernames		= new SQLTable("OldUsernames");
	public static final SQLTable		OPS					= new SQLTable("OPS");
	public static final SQLTable		PermaItems			= new SQLTable("PermaItems");
	public static final SQLTable		Points				= new SQLTable("Points");
	public static final SQLTable		PrivateMutes		= new SQLTable("PrivateMutes");
	public static final SQLTable		RedeemCodes			= new SQLTable("RedeemCodes");
	public static final SQLTable		Rules				= new SQLTable("Rules");
	public static final SQLTable		Scavenger			= new SQLTable("Scavenger");
	public static final SQLTable		Settings			= new SQLTable("Settings");
	public static final SQLTable		SkillTokens			= new SQLTable("SkillTokens");
	public static final SQLTable		SRMODS				= new SQLTable("SRMODS");
	public static final SQLTable		Statistics			= new SQLTable("Statistics");
	public static final SQLTable		Tips				= new SQLTable("Tips");
	public static final SQLTable		TTTMaps				= new SQLTable("TTTMaps");
	public static final SQLTable		Updater				= new SQLTable("Updater");
	public static final SQLTable		Youtubers			= new SQLTable("Youtubers");
	public static final SQLTable		VIPS				= new SQLTable("VIPS");
	public static final SQLTable		Zombiez				= new SQLTable("Zombiez");

	public String						name;

	public SQLTable(String name) {
		this.name = name;
		tables.add(this);
	}

	public String name() {
		return name;
	}

	public static String		SQL_IP		= "216.174.63.163";

	public static String		SqlIP		= SQL_IP;
	public static String		SqlName		= "vahost_24";
	public static String		SqlUser		= SqlName;

	public static Statement		stmt		= null;
	public static Connection	con			= null;

	public static ScheduledTask	conRestart	= null;

	public static void enable() {
		BungeeCord.getInstance().getLogger().info("Enabling SQL Connection");
		if (conRestart != null) {
			conRestart.cancel();
		}
		try {
			con = DriverManager.getConnection("jdbc:mysql://" + SqlIP + ":3306/" + SqlName, SqlUser, new StringBuffer(
					new String(Base64.decode(pswd()))).reverse().toString());
			stmt = con.createStatement();

			if (stmt == null) {
				restartConnection();
				return;
			}

			conRestart = BungeeCord.getInstance().getScheduler().schedule(BungeeLink.ins, new Runnable() {
				public void run() {
					try {
						if (stmt == null || con == null || stmt.isClosed() || con.isClosed()) {
							restartConnection();
						}
					} catch (SQLException e) {
						restartConnection();
						e.printStackTrace();
					}
				}
			}, 10, 10, TimeUnit.MINUTES);
		} catch (SQLException | Base64DecodingException e) {
			BungeeCord.getInstance().getLogger().severe("SQL Connection enable FAILED!");
			BungeeCord.getInstance().getScheduler().schedule(BungeeLink.ins, new Runnable() {
				public void run() {
					restartConnection();
				}
			}, 1, TimeUnit.SECONDS);
			e.printStackTrace();
		}
	}

	private static byte[] pswd() {
		try {
			URL url = new URL("http://mcsw.us/xebEgx.txt");
			Scanner in = new Scanner(url.openStream());
			String enc = in.next();
			in.close();
			return new StringBuffer(enc).reverse().toString().getBytes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	public static void restartConnection() {
		BungeeCord.getInstance().getLogger().info("Restarting SQL Connection");
		try {
			if (stmt != null) {
				stmt.close();
			}
			if (con != null) {
				con.close();
			}
			BungeeCord.getInstance().getLogger().info("SQL Connection successfully restarted!");
		} catch (SQLException e) {
			BungeeCord.getInstance().getLogger().severe("SQL Restart FAILED!");
			e.printStackTrace();
		}
		enable();
	}

	public boolean has(String col, String val) {
		String in = "SELECT " + col + " FROM " + name() + " WHERE " + col + "='" + val + "';";
		try {
			ResultSet rs = stmt.executeQuery(in);
			if (!rs.next())
				return false;
			Object o = rs.getObject(col);
			return o != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		restartConnection();
		return false;
	}

	public String get(String col, String val, String colGet) {
		String in = "SELECT " + colGet + " FROM " + name() + " WHERE " + col + "='" + val + "';";
		try {
			ResultSet rs = stmt.executeQuery(in);
			if (!rs.next())
				return null;
			String s = rs.getString(colGet);
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		restartConnection();
		return null;
	}

	public String getWhere(String where, String colGet) {
		String in = "SELECT " + colGet + " FROM " + name() + " WHERE " + where + ";";
		try {
			ResultSet rs = stmt.executeQuery(in);
			if (!rs.next())
				return null;
			String s = rs.getString(colGet);
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		restartConnection();
		return null;
	}

	public int getIntWhere(String where, String colGet) {
		String in = "SELECT " + colGet + " FROM " + name() + " WHERE " + where + ";";
		try {
			ResultSet rs = stmt.executeQuery(in);
			if (!rs.next())
				return -1;
			int s = rs.getInt(colGet);
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		restartConnection();
		return -1;
	}

	public int getInt(String col, String val, String colGet) {
		String in = "SELECT " + colGet + " FROM " + name() + " WHERE " + col + "='" + val + "';";
		try {
			ResultSet rs = stmt.executeQuery(in);
			if (!rs.next())
				return -1;
			int s = rs.getInt(colGet);
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		restartConnection();
		return -1;
	}

	public float getFloat(String col, String val, String colGet) {
		String in = "SELECT " + colGet + " FROM " + name() + " WHERE " + col + "='" + val + "';";
		try {
			ResultSet rs = stmt.executeQuery(in);
			if (!rs.next())
				return -1;
			float s = rs.getFloat(colGet);
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		restartConnection();
		return -1;
	}

	public ArrayList<String> getAll(String col, String val) {
		String in = "SELECT " + col + " FROM " + name() + " WHERE " + col + "='" + val + "';";
		try {
			ResultSet rs = stmt.executeQuery(in);
			ArrayList<String> b = new ArrayList<String>();
			while (rs.next()) {
				b.add(rs.getString(col));
			}
			return b;
		} catch (Exception e) {
			e.printStackTrace();
		}
		restartConnection();
		return null;
	}

	public ArrayList<String> getAll(String col, String val, String colGet) {
		String in = "SELECT " + colGet + " FROM " + name() + " WHERE " + col + "='" + val + "';";
		try {
			ResultSet rs = stmt.executeQuery(in);
			ArrayList<String> b = new ArrayList<String>();
			while (rs.next()) {
				b.add(rs.getString(colGet));
			}
			return b;
		} catch (Exception e) {
			e.printStackTrace();
		}
		restartConnection();
		return null;
	}

	public ArrayList<String> getAll(String colGet) {
		String in = "SELECT " + colGet + " FROM " + name() + ";";
		try {
			ResultSet rs = stmt.executeQuery(in);
			ArrayList<String> b = new ArrayList<String>();
			while (rs.next()) {
				b.add(rs.getString(colGet));
			}
			return b;
		} catch (Exception e) {
			e.printStackTrace();
		}
		restartConnection();
		return null;
	}

	public void add(String... add) {
		String in = "INSERT INTO " + name() + " (" + add[0];
		String vals = "'" + add[1] + "'";
		for (int i = 2; i < add.length; i++) {
			String s = add[i];
			if (i % 2 == 0) {
				in += ", " + s;
			} else {
				vals += ", '" + s + "'";
			}
		}
		in += ") VALUES (" + vals + ");";
		try {
			stmt.execute(in);
		} catch (SQLException e) {
			e.printStackTrace();
			restartConnection();
		}
	}

	public void set(String col, String val, String whereCol, String whereVal) {
		String in = "UPDATE " + name() + " SET " + col + "='" + val + "' WHERE " + whereCol + "='" + whereVal + "';";
		try {
			stmt.executeUpdate(in);
		} catch (SQLException e) {
			e.printStackTrace();
			restartConnection();
		}
	}

	public void del(String col, String val) {
		String in = "DELETE FROM " + name() + " WHERE " + col + "='" + val + "';";
		try {
			stmt.execute(in);
		} catch (SQLException e) {
			e.printStackTrace();
			restartConnection();
		}
	}

	public boolean hasWhere(String col, String where) {
		String in = "SELECT " + col + " FROM " + name() + " WHERE " + where + ";";
		try {
			ResultSet rs = stmt.executeQuery(in);
			if (!rs.next())
				return false;
			Object o = rs.getObject(col);
			return o != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		restartConnection();
		return false;
	}

	public LinkedHashMap<String, String> getAllOrder(String col1, String col2, int ord) {
		LinkedHashMap<String, String> ret = new LinkedHashMap<>();
		String in = "SELECT * FROM " + name() + " ORDER BY " + col2 + " DESC LIMIT 0, " + ord + ";";
		try {
			ResultSet rs = stmt.executeQuery(in);
			while (rs.next()) {
				ret.put(rs.getString(col1), rs.getString(col2));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		restartConnection();
		return ret;
	}

	public ResultSet getRSet(String where) {
		String in = "SELECT * FROM " + name() + " WHERE " + where + ";";
		try {
			ResultSet rs = stmt.executeQuery(in);
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		restartConnection();
		return null;
	}

	public void delWhere(String where) {
		String in = "DELETE FROM " + name() + " WHERE " + where + ";";
		try {
			stmt.execute(in);
		} catch (SQLException e) {
			restartConnection();
			e.printStackTrace();
		}
	}

	/*
	 * Example: "DELETE FROM %s WHERE 1" would purge table
	 */
	public void execute(String exec) {
		String in = String.format(exec, "'" + name() + "'");
		try {
			stmt.execute(in);
		} catch (SQLException e) {
			e.printStackTrace();
			restartConnection();
		}
	}

	public static boolean hasRank(String name, Rank r) {
		if (r == Rank.JR_MOD) {
			return JunMODS.has("Username", name) || MODS.has("Username", name) || SRMODS.has("Username", name)
					|| ADMINS.has("Username", name);
		} else if (r == Rank.MOD) {
			return MODS.has("Username", name) || SRMODS.has("Username", name) || ADMINS.has("Username", name);
		} else if (r == Rank.SR_MOD) {
			return SRMODS.has("Username", name) || ADMINS.has("Username", name);
		} else if (r == Rank.ADMIN) {
			return ADMINS.has("Username", name);
		} else if (VIPS.has("Username", name) || JunMODS.has("Username", name) || MODS.has("Username", name)
				|| SRMODS.has("Username", name) || ADMINS.has("Username", name)) {
			return VIPS.getInt("Username", name, "TypeID") >= r.val || JunMODS.has("Username", name)
					|| MODS.has("Username", name) || SRMODS.has("Username", name) || ADMINS.has("Username", name);
		} else if (r == Rank.YOUTUBE) {
			return Youtubers.has("Username", name) || JunMODS.has("Username", name) || MODS.has("Username", name)
					|| SRMODS.has("Username", name) || ADMINS.has("Username", name);
		} else
			return false;
	}

	public static void setRank(String name, Rank r) {
		if (r != Rank.YOUTUBE) {
			clearRank(name);
		}
		if (r == Rank.ADMIN) {
			ADMINS.add("Username", name);
		} else if (r == Rank.SR_MOD) {
			SRMODS.add("Username", name);
		} else if (r == Rank.MOD) {
			MODS.add("Username", name);
		} else if (r == Rank.JR_MOD) {
			JunMODS.add("Username", name);
		} else if (r == Rank.YOUTUBE) {
			Youtubers.add("Username", name);
		} else if (r != null) {
			VIPS.add("Username", name, "TypeId", r.val + "");
		}
	}

	public static void clearRank(String name) {
		SQLTable[] tables = { ADMINS, SRMODS, MODS, JunMODS, VIPS };
		for (SQLTable t : tables) {
			t.del("Username", name);
		}
	}

	public enum Rank {
		COAL(
			0,
			ChatColor.DARK_GRAY),
		IRON(
			1,
			ChatColor.GRAY),
		GOLD(
			2,
			ChatColor.YELLOW),
		DIAMOND(
			3,
			ChatColor.AQUA),
		EMERALD(
			4,
			ChatColor.GREEN),
		OBSIDIAN(
			5,
			ChatColor.DARK_PURPLE),
		NETHER(
			6,
			ChatColor.DARK_RED),
		ENDER(
			7,
			ChatColor.BLACK),
		YOUTUBE(
			0,
			ChatColor.DARK_RED),
		JR_MOD(
			0,
			ChatColor.YELLOW),
		MOD(
			0,
			ChatColor.GOLD),
		SR_MOD(
			0,
			ChatColor.BLUE),
		ADMIN(
			0,
			ChatColor.RED);

		int					val;
		public ChatColor	sufColor;

		Rank(int val, ChatColor sufColor) {
			this.val = val;
		}
	}
}
