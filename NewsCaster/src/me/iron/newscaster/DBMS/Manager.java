package me.iron.newscaster.DBMS;

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
    public static String printQuery ="SELECT a.* FROM attacks as a;";
    public static ResultSet getAttacksPretty() throws SQLException {
        String query = "Select event.millis, agg.name as \"agg\", vic.name as \"vic\",event.x,event.y,event.z\n" +
                "From attacks as event, objects as agg, objects as vic\n" +
                "WHERE event.victim_snapshot = vic.ship_snapshot and event.attacker_snapshot = agg.ship_snapshot order by vic.name;";
        //query = "Select * from attacks;";
        return getConnection().createStatement().executeQuery(query);
    }

    public static ResultSet getObjectsSimple() throws SQLException {
        String query = "Select o.*\n" +
                "From objects as o\n";
        return getConnection().createStatement().executeQuery(query);
    }



    public static void main(String[] args) {
        try {
            connection = getConnection();
            deleteTables();
            createTable();
            addEntries();
            System.out.println(resultToString(getObjectsSimple()));
            System.out.println("attacks pretty");
            System.out.println(resultToString(getAttacksPretty()));
            System.out.println("done");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public static void initTable() throws SQLException {
        createTable();
        System.out.print(resultToString(getAttacksPretty()));
    }

    private static String maxNIOSize = "";
    private static String getDbPath() {
        return "."+File.separator+"server-database"+File.separator;
    }
    public static Connection connection;
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:file:" + getDbPath() + ";shutdown=true" + maxNIOSize, "SA", "");
    }

    public static String resultToString(ResultSet r) throws SQLException {

        StringBuilder b = new StringBuilder();
        for (int i = 1; i < r.getMetaData().getColumnCount()+1; i++) {
            if (i > 1)
                b.append(" ");
            b.append(r.getMetaData().getColumnName(i));

        }
        b.append("\n\n-----------------------------------");
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

    public static void addAttack(long victimID, long victimFactionID, String victimName,
                                 boolean killed,
                                 long attackerID, long attackerFactionId, String attackerName,
                                 Vector3i sector, long millis) {
        try {
            Statement s = connection.createStatement();

            //add victim
            long v_snap = addOrUpdateObject(victimID,victimFactionID,victimName);
            //add attacker
            long a_snap = addOrUpdateObject(attackerID,attackerFactionId,attackerName);

            //get snapshot IDs of victim and attacker
            String a = String.format("Insert into attacks(victim_snapshot, victim_killed, attacker_snapshot, millis, x,y,z) values (%s,%s,%s,%s,%s,%s,%s)",
                    v_snap,true,a_snap,millis,sector.x,sector.y,sector.z);
            System.out.println(a);
            s.executeUpdate(a);
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
    private static long addOrUpdateObject(long DBID, long factionID, String name) throws SQLException {
        String q = String.format("select o.* from objects as o where o.dbid = %s and o.faction = %s;",DBID,factionID);
        ResultSet r = connection.createStatement().executeQuery(q);
        q = "";
        if (r.next()) { //victim exists
            q += String.format("Update objects as o SET name = %s where o.dbid = %s and o.faction = %s; \n",name,DBID, factionID);
        }else { //make new victim
            q += String.format("Insert into objects(dbid, faction, name) values (%s,%s,'%s'); \n",DBID, factionID, name);
        }
        System.out.println(q);
        connection.createStatement().executeUpdate(q);
        q = String.format("Select o.ship_snapshot from objects as o where o.DBID = %s and o.faction = %s",DBID, factionID);
        r = connection.createStatement().executeQuery(q);
        if (r.next())
            return r.getLong(1);
        else {
            new NullPointerException("ship snapshot wasnt found in objects DB").printStackTrace();
            return -1;

        }
    }

    private static void addEntries() throws SQLException {
        Statement s = getConnection().createStatement();
        Random r = new Random(420);
        for (int i = 0; i < 10; i++) {
            int a_id = r.nextInt(10);
            int v_id = r.nextInt(10);
            addAttack(v_id,
                    10000+r.nextInt(100),
                    "ship-"+v_id,

                    r.nextBoolean(),
                    a_id,
                    10000+r.nextInt(100),
                    "ship-"+ a_id,
                    new Vector3i(r.nextInt()%100,r.nextInt()%100,r.nextInt()%100),
                    i
            );
        }
    }
}
