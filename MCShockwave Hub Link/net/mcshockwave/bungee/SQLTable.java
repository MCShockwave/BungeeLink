package net.mcshockwave.bungee;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class SQLTable {
	public static final SQLTable	ADMINS			= new SQLTable("ADMINS");
	public static final SQLTable	BanHistory		= new SQLTable("BanHistory");
	public static final SQLTable	Banned			= new SQLTable("Banned");
	public static final SQLTable	Coins			= new SQLTable("Coins");
	public static final SQLTable	Dojo			= new SQLTable("Dojo");
	public static final SQLTable	ForceCooldowns	= new SQLTable("ForceCooldowns");
	public static final SQLTable	Friends			= new SQLTable("Friends");
	public static final SQLTable	JunMODS			= new SQLTable("JunMODS");
	public static final SQLTable	Level			= new SQLTable("Level");
	public static final SQLTable	MiscItems		= new SQLTable("MiscItems");
	public static final SQLTable	ModCommands		= new SQLTable("ModCommands");
	public static final SQLTable	MODS			= new SQLTable("MODS");
	public static final SQLTable	Muted			= new SQLTable("Muted");
	public static final SQLTable	MynerimItems	= new SQLTable("MynerimItems");
	public static final SQLTable	nickNames		= new SQLTable("nickNames");
	public static final SQLTable	PermaItems		= new SQLTable("PermaItems");
	public static final SQLTable	Points			= new SQLTable("Points");
	public static final SQLTable	PrivateMutes	= new SQLTable("PrivateMutes");
	public static final SQLTable	RedeemCodes		= new SQLTable("RedeemCodes");
	public static final SQLTable	Rules			= new SQLTable("Rules");
	public static final SQLTable	Scavenger		= new SQLTable("Scavenger");
	public static final SQLTable	Settings		= new SQLTable("Settings");
	public static final SQLTable	SkillTokens		= new SQLTable("SkillTokens");
	public static final SQLTable	Statistics		= new SQLTable("Statistics");
	public static final SQLTable	Tips			= new SQLTable("Tips");
	public static final SQLTable	Updater			= new SQLTable("Updater");
	public static final SQLTable	Youtubers		= new SQLTable("Youtubers");
	public static final SQLTable	VIPS			= new SQLTable("VIPS");
	public static final SQLTable	Zombiez			= new SQLTable("Zombiez");

	public String					name;

	public SQLTable(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public static String		SqlIP		= "127.0.0.1";
	public static String		SqlName		= "vahost_24";
	public static String		SqlUser		= SqlName;

	// DONT LOOK AT THIS PLEASEEEEE
	public static String		SqlPass		= "24eilrahC";
	// TURN AWAY NOW!!!!

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
					SqlPass).reverse().toString());
			stmt = con.createStatement();

			conRestart = BungeeCord.getInstance().getScheduler().schedule(BungeeLink.ins, new Runnable() {
				public void run() {
					restartConnection();
				}
			}, 10, 10, TimeUnit.MINUTES);
		} catch (SQLException e) {
			BungeeCord.getInstance().getLogger().severe("SQL Connection enable FAILED!");
			enable();
			e.printStackTrace();
		}
	}

	public static void restartConnection() {
		BungeeCord.getInstance().getLogger().info("Restarting SQL Connection");
		try {
			con.close();
			stmt.close();
			enable();
			BungeeCord.getInstance().getLogger().info("SQL Connection successfully restarted!");
		} catch (SQLException e) {
			BungeeCord.getInstance().getLogger().severe("SQL Restart FAILED!");
			enable();
			e.printStackTrace();
		}
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
		}
	}

	public void set(String col, String val, String whereCol, String whereVal) {
		String in = "UPDATE " + name() + " SET " + col + "='" + val + "' WHERE " + whereCol + "='" + whereVal + "';";
		try {
			stmt.executeUpdate(in);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void del(String col, String val) {
		String in = "DELETE FROM " + name() + " WHERE " + col + "='" + val + "';";
		try {
			stmt.execute(in);
		} catch (SQLException e) {
			e.printStackTrace();
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
		return null;
	}

	public void delWhere(String where) {
		String in = "DELETE FROM " + name() + " WHERE " + where + ";";
		try {
			stmt.execute(in);
		} catch (SQLException e) {
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
		}
	}

	public static boolean hasRank(String name, Rank r) {
		if (r == Rank.JR_MOD) {
			return JunMODS.has("Username", name) || MODS.has("Username", name) || ADMINS.has("Username", name);
		} else if (r == Rank.MOD) {
			return MODS.has("Username", name) || ADMINS.has("Username", name);
		} else if (r == Rank.ADMIN) {
			return ADMINS.has("Username", name);
		} else if (VIPS.has("Username", name) || JunMODS.has("Username", name) || MODS.has("Username", name)
				|| ADMINS.has("Username", name)) {
			return VIPS.getInt("Username", name, "TypeID") >= r.val || JunMODS.has("Username", name)
					|| MODS.has("Username", name) || ADMINS.has("Username", name);
		} else
			return false;
	}

	public static void setRank(String name, Rank r) {
		clearRank(name);
		if (r == Rank.ADMIN) {
			ADMINS.add("Username", name);
		} else if (r == Rank.MOD) {
			MODS.add("Username", name);
		} else if (r == Rank.JR_MOD) {
			JunMODS.add("Username", name);
		} else if (r != null) {
			VIPS.add("Username", name, "TypeId", r.val + "");
		}
	}

	public static void clearRank(String name) {
		SQLTable[] tables = { ADMINS, MODS, JunMODS, VIPS, Youtubers };
		for (SQLTable t : tables) {
			t.del("Username", name);
		}
	}

	public enum Rank {
		COAL(
			0,
			ChatColor.DARK_GRAY,
			ChatColor.BOLD + "C" + ChatColor.RESET),
		IRON(
			1,
			ChatColor.GRAY,
			ChatColor.BOLD + "I" + ChatColor.RESET),
		GOLD(
			2,
			ChatColor.YELLOW,
			ChatColor.BOLD + "G" + ChatColor.RESET),
		DIAMOND(
			3,
			ChatColor.AQUA,
			ChatColor.BOLD + "D" + ChatColor.RESET),
		EMERALD(
			4,
			ChatColor.GREEN,
			ChatColor.BOLD + "E" + ChatColor.RESET),
		OBSIDIAN(
			5,
			ChatColor.DARK_PURPLE,
			ChatColor.BOLD + "O" + ChatColor.RESET),
		NETHER(
			6,
			ChatColor.DARK_RED,
			ChatColor.BOLD + "N" + ChatColor.RESET),
		ENDER(
			7,
			ChatColor.BLACK,
			ChatColor.BOLD + "E" + ChatColor.RESET),
		JR_MOD(
			0,
			ChatColor.GOLD,
			"Jr. Mod"),
		MOD(
			0,
			ChatColor.GOLD,
			"Mod"),
		ADMIN(
			0,
			ChatColor.RED,
			"Admin");

		int	val;

		Rank(int val, ChatColor sufColor, String sufChar) {
			this.val = val;
		}
	}
}
