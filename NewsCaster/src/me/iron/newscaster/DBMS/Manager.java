package me.iron.newscaster.DBMS;

import api.ModPlayground;
import com.sun.istack.internal.Nullable;
import me.iron.newscaster.commandUI.CommandLeaderboard;
import me.iron.newscaster.notification.broadcasting.Broadcaster;
import org.lwjgl.Sys;
import org.schema.common.util.linAlg.Vector3i;

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
        //   deleteTables();
        //   createTable();
        //   addEntries();

        //   System.out.println(resultToString(getObjectsSimple()));
        //   System.out.println("attacks pretty");
        //   System.out.println(resultToString(getAttacksPretty()));
        //   System.out.println("killers(UID,name,kills)");
        //   System.out.println(resultToString(getKillers()));

           String uid = "ENTITY_SHIP_Summer 2";
           System.out.println(Broadcaster.prettyKillers(getKillers()));

            System.out.println("get kills of UID = "+uid);
            System.out.println(resultToString(           getKills(uid,null)));

            System.out.println(Broadcaster.prettyVictims(getKills(uid,null)));
           System.out.println("done");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void initTable() throws SQLException {
        connection = getConnection();

        //TODO remove
    //    deleteTables();
        createTable();
        System.out.println(resultToString(connection.createStatement().executeQuery("select * from attacks;")));
        System.out.println(resultToString(connection.createStatement().executeQuery("select * from objects;")));

    }

    private static void createTable() throws SQLException {
        Statement s = connection.createStatement();

        //create table
        s.executeUpdate("CREATE TABLE IF NOT EXISTS objects (\n" +
                "\tship_snapshot bigint primary key identity,"+
                "\tUID varchar(128),\n" +
                "\tfaction bigint,\n" +
                "\tname varchar(64)\n" +
                "\t);\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS attacks (\n" +
                "\tvictim_snapshot bigint,\n"+
                "\tvictim_reactor int,\n"+
                "\tvictim_killed boolean,\n" +
                "\tattacker_snapshot bigint,\n" +
                "\tattacker_reactor int,\n"+
                "\tmillis bigint,\n" +
                "\tx int,\n"+
                "\ty int,\n"+
                "\tz int,\n"+
                "\tCONSTRAINT pk_attacks PRIMARY KEY(victim_snapshot,attacker_snapshot,millis,x,y,z)\n" +
                ")");

        s.close();
    }

    public static void deleteTables() throws SQLException {
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
     * @param victimUID
     * @param victimFactionID
     * @param victimName
     * @param killed
     * @param attackerUID
     * @param attackerFactionId
     * @param attackerName
     * @param sector
     * @param millis
     */
    public static void addAttack(String victimUID, int victimFactionID, String victimName, int victimReactor,
                                 boolean killed,
                                 String attackerUID, int attackerFactionId, String attackerName, int attackerReactor,
                                 Vector3i sector, long millis) {
        //ModPlayground.broadcastMessage("ADD ATTACK ON "+victimName + " BY " + attackerName);
        try {
            Statement s = connection.createStatement();

            //add victim
            long v_snap = addOrUpdateObject(victimUID,victimFactionID,victimName);
            //add attacker
            long a_snap = addOrUpdateObject(attackerUID,attackerFactionId,attackerName);

            //get snapshot IDs of victim and attacker
            PreparedStatement p = connection.prepareStatement("Insert into attacks(victim_snapshot, victim_killed, victim_reactor, attacker_snapshot, attacker_reactor, millis, x,y,z) values (?,?,?,?,?,?,?,?,?);");
            p.setLong(1,v_snap);
            p.setBoolean(2,killed);
            p.setInt(3,victimReactor);
            p.setLong(4,a_snap);
            p.setInt(5,attackerReactor);
            p.setLong(6,millis); //millis
            p.setInt(7,sector.x);//x
            p.setInt(8,sector.y);//y
            p.setInt(9,sector.z);//z
            p.executeUpdate();
            s.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * will create a ship snapshot in the DB, or update if one matches DBID and factionID
     * @param UID vanilla UID of the segmentcontroller
     * @param factionID .
     * @param name ships name
     * @return intern snapshot id used for identifiying snapshot in the attack entry, -1 if not existing
     * @throws SQLException
     */
    private static long addOrUpdateObject(String UID, int factionID, String name) throws SQLException {
        //ModPlayground.broadcastMessage("add object for '" + UID + "' (" + UID.length()+")");
        PreparedStatement p = connection.prepareStatement("select o.* from objects as o where o.UID = ? and o.faction = ?;");
        p.setString(1,UID);
        p.setInt(2,factionID);

        ResultSet r = p.executeQuery();

        if (r.next()) { //object exists
            p = connection.prepareStatement("Update objects as o SET name = ? where o.UID = ? and o.faction = ?;");
        }else { //make new object
            p = connection.prepareStatement("Insert into objects(name, UID, faction) values (?,?,?);");
        }
        p.setString(1,name);
        p.setString(2,UID);
        p.setInt(3,factionID);
        p.executeUpdate();

        p = connection.prepareStatement("Select o.ship_snapshot from objects as o where o.UID = ? and o.faction = ?;");
        p.setString(1,UID);
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
            String a_id = "ship_"+r.nextInt(10);
            String v_id = "ship_"+r.nextInt(10);
            addAttack(v_id,
                    10000+r.nextInt(5),
                    "ship-"+v_id,
                    r.nextInt()%1000,

                    r.nextBoolean(),
                    a_id,
                    10000+r.nextInt(5),
                    "ship_"+ a_id,
                    r.nextInt()%1000,
                    new Vector3i(r.nextInt()%100,r.nextInt()%100,r.nextInt()%100),
                    i
            );
        }
    }

    public static ResultSet getAttacksPretty() throws SQLException {
        String query = "Select event.millis, concat(agg.name,'(',agg.UID,')',' [',agg.faction,'] ','r',event.attacker_reactor) as Attacker, vic.name as Victim,concat(event.x,',',event.y,',',event.z) as sector, event.victim_killed \n" +
                "From attacks as event, objects as agg, objects as vic\n" +
                "WHERE event.victim_snapshot = vic.ship_snapshot and event.attacker_snapshot = agg.ship_snapshot and event.victim_killed = FALSE" +
                " order by vic.name;";
        return getConnection().createStatement().executeQuery(query);
    }

    public static ResultSet getObjectsSimple() throws SQLException {
        String query = "Select o.*\n" +
                "From objects as o\n" +
                "order by o.faction;";
        return getConnection().createStatement().executeQuery(query);
    }

    /**
     * gets all ships that have killed an entity and the amount they killed.
     * @return
     */
    public static ResultSet getKillers() throws SQLException {
        String q="Select distinct o.name, o.faction, a.kills\n" +
                "from " +
                "(Select agg.UID, agg.faction, count(*) as kills " +
                "from attacks as att, " +
                "objects as agg " +
                "where att.attacker_snapshot = agg.ship_snapshot and att.victim_killed = FALSE " +
                "group by agg.UID, agg.faction) as a,\n" +
                "\tobjects as o\n" +
                "where a.UID = o.UID and a.faction = o.faction " +
                "order by a.kills desc;";
        return connection.createStatement().executeQuery(q);
    }

    /**
     * return the kills made by ship with this UID and optional under which faction the kills were made.
     * @param UID
     * @param factionID
     * @return
     * @throws SQLException
     */
    public static ResultSet getKills(String UID, @Nullable Integer factionID) throws SQLException {
        System.out.println("getting kills for UID "+UID +"["+factionID+"]");
        String q = "Select v.name, v.faction, a.victim_reactor, a.x, a.y, a.z, a.millis" +
                " from attacks as a, objects as v, objects as k" +
                " where a.attacker_snapshot = k.ship_snapshot and a.victim_snapshot = v.ship_snapshot" +
                " and k.UID = ?";
        if (factionID != null)
            q += " and k.faction = ?";
        q+= " order by a.victim_reactor, v.faction;";
        PreparedStatement p = connection.prepareStatement(q);
        p.setString(1,UID);
        if (factionID != null)
            p.setInt(2,factionID);
        return p.executeQuery();
    }


}
