package me.iron.newscaster.DBMS;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.Faction;

import javax.annotation.Nullable;
import java.io.File;
import java.sql.*;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 13.12.2021
 * TIME: 22:17
 */
public class Manager {
    private static String maxNIOSize = "";
    public static Connection connection;

    public static void main(String[] args) {
        try {
            initTable();
            deleteTables();
            createTable();
            addEntries();

            System.out.println(resultToString(getObjectsSimple()));
            System.out.println("attacks pretty");
            System.out.println(resultToString(getAttacksPretty()));
            System.out.println("killers(dbid,name,kills)");
            System.out.println(resultToString(getKillers()));
            System.out.println("get kills of ship-8 (DBID = 8)");
            System.out.println(resultToString(getKills(8,null)));
            System.out.println("done");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void initTable() throws SQLException {
        connection = getConnection();
        createTable();
    }

    private static void createTable() throws SQLException {
        Statement s = connection.createStatement();

        //create table
        s.executeUpdate("CREATE TABLE IF NOT EXISTS objects (\n" +
                "\tship_snapshot bigint primary key identity,"+
                "\tDBID bigint,\n" +
                "\tfaction bigint,\n" +
                "\tname varchar(50)\n" +
                "\t);\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS attacks (\n" +
                "\tvictim_snapshot bigint,"+
                "\tvictim_killed boolean,\n" +
                "\tattacker_snapshot bigint,\n" +
                "\tmillis bigint,\n" +
                "\tx int,"+
                "\ty int,"+
                "\tz int,"+
                "\tCONSTRAINT pk_attack PRIMARY KEY(victim_snapshot,attacker_snapshot,millis,x,y,z)\n" +
                ")");

        s.close();
    }

    private static void deleteTables() throws SQLException {
        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS objects;\n" +
                "DROP TABLE IF EXISTS attacks;");
    }

    private static String getDbPath() {
        return "."+File.separator+"server-database"+File.separator;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:file:" + getDbPath() + ";shutdown=true" + maxNIOSize, "SA", "");
    }

    /**
     * turns table into a string, formatted like a table
     * @param r
     * @return
     * @throws SQLException
     */
    public static String resultToString(ResultSet r) throws SQLException {

        StringBuilder b = new StringBuilder();
        for (int i = 1; i < r.getMetaData().getColumnCount()+1; i++) {
            if (i > 1)
                b.append(" ");
            b.append(r.getMetaData().getColumnLabel(i));

        }
        b.append("\n-----------------------------------\n");
        while (r.next()) {
            for (int i = 1; i <= r.getMetaData().getColumnCount(); i++) {

                if (i > 1) b.append("  ");
                String columnValue = r.getString(i);
                b.append(columnValue);//+ " " + r.getMetaData().getColumnName(i));
            }
            b.append("\n");
        }
        return b.toString();
    }

    /**
     * add an attack event to dbms
     * @param victimID
     * @param victimFactionID
     * @param victimName
     * @param killed
     * @param attackerID
     * @param attackerFactionId
     * @param attackerName
     * @param sector
     * @param millis
     */
    public static void addAttack(long victimID, int victimFactionID, String victimName,
                                 boolean killed,
                                 long attackerID, int attackerFactionId, String attackerName,
                                 Vector3i sector, long millis) {
        try {
            Statement s = connection.createStatement();

            //add victim
            long v_snap = addOrUpdateObject(victimID,victimFactionID,victimName);
            //add attacker
            long a_snap = addOrUpdateObject(attackerID,attackerFactionId,attackerName);

            //get snapshot IDs of victim and attacker
            PreparedStatement p = connection.prepareStatement("Insert into attacks(victim_snapshot, victim_killed, attacker_snapshot, millis, x,y,z) values (?,?,?,?,?,?,?);");
            p.setLong(1,v_snap);
            p.setBoolean(2,killed);
            p.setLong(3,a_snap);
            p.setLong(4,millis); //millis
            p.setInt(5,sector.x);//x
            p.setInt(6,sector.y);//y
            p.setInt(7,sector.z);//z
            p.executeUpdate();
            s.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * will create a ship snapshot in the DB, or update if one matches DBID and factionID
     * @param DBID vanilla DB id of the segmentcontroller
     * @param factionID .
     * @param name ships name
     * @return intern snapshot id used for identifiying snapshot in the attack entry, -1 if not existing
     * @throws SQLException
     */
    private static long addOrUpdateObject(long DBID, int factionID, String name) throws SQLException {
        PreparedStatement p = connection.prepareStatement("select o.* from objects as o where o.dbid = ? and o.faction = ?;");
        p.setLong(1,DBID);
        p.setInt(2,factionID);

        ResultSet r = p.executeQuery();

        if (r.next()) { //object exists
            p = connection.prepareStatement("Update objects as o SET name = ? where o.dbid = ? and o.faction = ?;");
        }else { //make new object
            p = connection.prepareStatement("Insert into objects(name, dbid, faction) values (?,?,?);");
        }
        p.setString(1,name);
        p.setLong(2,DBID);
        p.setInt(3,factionID);
        p.executeUpdate();

        p = connection.prepareStatement("Select o.ship_snapshot from objects as o where o.DBID = ? and o.faction = ?;");
        p.setLong(1,DBID);
        p.setInt(2,factionID);
        r = p.executeQuery();
        if (r.next())
            return r.getLong(1);
        else {
            new NullPointerException("ship snapshot wasnt found in objects DB").printStackTrace();
            return -1;

        }
    }

    /**
     * debug method, generates attacks
     * @throws SQLException
     */
    private static void addEntries() throws SQLException {
        Random r = new Random(420);
        for (int i = 0; i < 1000; i++) {
            int a_id = r.nextInt(10);
            int v_id = r.nextInt(10);
            addAttack(v_id,
                    10000+r.nextInt(5),
                    "ship-"+v_id,

                    r.nextBoolean(),
                    a_id,
                    10000+r.nextInt(5),
                    "ship_"+ a_id,
                    new Vector3i(r.nextInt()%100,r.nextInt()%100,r.nextInt()%100),
                    i
            );
        }
    }

    public static ResultSet getAttacksPretty() throws SQLException {
        String query = "Select event.millis, agg.name as Attacker, vic.name as Victim,concat(event.x,',',event.y,',',event.z) as sector \n" +
                "From attacks as event, objects as agg, objects as vic\n" +
                "WHERE event.victim_snapshot = vic.ship_snapshot and event.attacker_snapshot = agg.ship_snapshot order by vic.name;";
        return getConnection().createStatement().executeQuery(query);
    }

    public static ResultSet getObjectsSimple() throws SQLException {
        String query = "Select o.*\n" +
                "From objects as o\n";
        return getConnection().createStatement().executeQuery(query);
    }

    /**
     * gets all ships that have killed an entity and the amount they killed.
     * @return
     */
    public static ResultSet getKillers() throws SQLException {
        String q="Select distinct o.name, o.faction, a.kills\n" +
                "from " +
                "(Select agg.dbid, agg.faction, count(*) as kills " +
                "from attacks as att, " +
                "objects as agg " +
                "where att.attacker_snapshot = agg.ship_snapshot " +
                "group by agg.dbid, agg.faction) as a,\n" +
                "\tobjects as o\n" +
                "where a.dbid = o.dbid and a.faction = o.faction " +
                "order by a.kills desc;";
        return connection.createStatement().executeQuery(q);
    }

    /**
     * return the kills made by ship with this DBID and optional under which faction the kills were made.
     * @param DBID
     * @param factionID
     * @return
     * @throws SQLException
     */
    public static ResultSet getKills(long DBID, @Nullable Integer factionID) throws SQLException {
        String q = "Select v.name, v.faction" +
                " from attacks as a, objects as v, objects as k" +
                " where a.attacker_snapshot = k.ship_snapshot and a.victim_snapshot = v.ship_snapshot" +
                " and k.dbid = ?";
        if (factionID != null)
            q += "and k.faction = ?";
        q+= ";";
        PreparedStatement p = connection.prepareStatement(q);
        p.setLong(1,DBID);
        if (factionID != null)
            p.setInt(2,factionID);
        return p.executeQuery();
    }

}
