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
    public static String printQuery ="SELECT v.name, v.size, a.name, a.size, att.sector_x, att.sector_y, attack.sector_z, att.millis \n" +
            "from\n" +
            "\tdestroyed as att,\n" +
            "\tobjects as v,\n" +
            "\tobjects as a\n" +
            "WHERE att.victim_id = v.DBID and att.attacker_id = a.DBID;";

    public static Connection connection;
    private static String maxNIOSize = "";

    public static void main(String[] args) {
        try {
            connection = getConnection();
            deleteTables();
            createTable();

            addEntries();
            Statement s = connection.createStatement();
            System.out.print(tableToString(s.executeQuery("SELECT o.* FROM objects as o;")));
            System.out.print(tableToString(s.executeQuery("SELECT o.* FROM destroyed as o;")));
            System.out.println(tableToString(s.executeQuery(printQuery)));
            connection.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }



    private static String getDbPath() {
        return "."+File.separator+"server-database"+File.separator;
    }



    public static void addDestroyed(long victimID, long victimFactionID, String victimName, int victim_size, long attackerID, long attackerFactionId, String attackerName, int attacker_size, long millis, Vector3i sector) {
        try {
            Statement s = connection.createStatement();
            victimName = "'"+victimName.substring(0, Math.min(victimName.length(),50))+"'";
            attackerName ="'"+attackerName.substring(0, Math.min(attackerName.length(),50))+"'";

            addOrUpdateObject(victimID,victimFactionID,victimName,victim_size);
            addOrUpdateObject(attackerID,attackerFactionId,attackerName,attacker_size);

            //was ship destroyed earlier already
        //   ResultSet r = s.executeQuery("SELECT 1 from destroyed as a where a.victim_ID = "+victimID +";");
        //   if (r.next()) {
        //       s.close();
        //       System.out.println("NOT LOGGING KILLED SHIP TWICE: " + victimName);
        //       return;
        //   }

            StringBuilder q = new StringBuilder();

            q.append("INSERT INTO destroyed(victim_id,attacker_id,millis,sector_x,sector_y,sector_z) VALUES (");
            q.append(victimID).append(",");
            q.append(attackerID).append(",");
            q.append(millis).append(",");
            q.append(sector.x).append(",").append(sector.y).append(",").append(sector.z).append(");");
            System.out.println(q.toString());
            System.out.println("");
            s.executeUpdate( q.toString());


            s.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    private static void addEntries() throws SQLException {
        Statement s = connection.createStatement();
        Random r = new Random(420);
        for (int i = 5; i < 10; i++) {
            long shipID1 = r.nextInt(10);
            long shipID2 = r.nextInt(10);
            addDestroyed(
                    shipID1,
                    r.nextInt(2),
                    "ship"+shipID1,
                    500,

                    shipID2,
                    r.nextInt(2),
                    "ship_"+shipID2,
                    1000,

                    i,
                    new Vector3i(r.nextInt()%1000,r.nextInt()%1000,r.nextInt()%1000)
                    );
        }
        s.close();
    }

    private static void addOrUpdateObject(long DBID, long factionID, String name, int size) throws SQLException {
        Statement s = connection.createStatement();
        ResultSet existsQuery = s.executeQuery("SELECT 1 from objects as o WHERE o.DBID ="+DBID+" and o.faction = "+factionID+";");
        String updateVictim;
        if (existsQuery.next()) { //update existing
            updateVictim= "UPDATE objects as o \n" + "SET (faction,name,size)=(" + factionID + ","+ name + ","+size+ ") WHERE o.DBID = "+ DBID +"and o.faction = "+factionID+";";
        } else { //create new
            updateVictim = "INSERT INTO objects(DBID,faction,name,size)\n" +
                    "VALUES" +
                    "(" + DBID + "," + factionID + "," + name +","+size+ ");";

        }
        s.executeUpdate(updateVictim);
        s.close();
    }

    public static String tableToString(ResultSet r) throws SQLException {
        StringBuilder b = new StringBuilder();
        for (int i = 1; i < r.getMetaData().getColumnCount()+1; i++) {
            b.append(String.format("%1$" + 20 + "s", r.getMetaData().getColumnName(i)));


        }
        b.append("\n");
        for (int i = 0; i < r.getMetaData().getColumnCount(); i++) {
            b.append("--------------------");
        }
        b.append("\n");
        while (r.next()) {
            for (int i = 1; i <= r.getMetaData().getColumnCount(); i++) {
                b.append( String.format("%1$" + 20 + "s", r.getString(i)));//+ " " + r.getMetaData().getColumnName(i));
            }
            b.append("\n");
        }
        r.close();
        return b.toString();

    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:file:" + getDbPath() + ";shutdown=true" + maxNIOSize, "SA", "");
    }

    private static void createTable() throws SQLException {
        Statement s = connection.createStatement();

        //create table
        s.executeUpdate("CREATE TABLE IF NOT EXISTS objects (\n" +
                "\tDBID bigint,\n" +
                "\tfaction bigint not null,\n" +
                "\tname varchar(50) not null,\n" +
                "\tsize int not null,\n" +
                "\tCONSTRAINT ship_key PRIMARY KEY (DBID, faction)\n" +
                ");\n" +
                "\t\n" +
                "CREATE TABLE IF NOT EXISTS destroyed (\n" +
                "\tvictim_id bigint,\n" +
                "\tattacker_id bigint,\n" +
                "\tmillis bigint,\n" +
                "\tsector_x int,\n" +
                "\tsector_y int,\n" +
                "\tsector_z int,\n" +
                "\tCONSTRAINT pk_attack PRIMARY KEY(victim_id,attacker_id,millis)\n" +
                ");");

        s.close();
    }

    private static void deleteTables() throws SQLException {
        Statement s = connection.createStatement();
        s.executeUpdate("DROP TABLE IF EXISTS objects;\n" +
                "DROP TABLE IF EXISTS attacks;"+
                "DROP TABLE IF EXISTS destroyed;");
        s.close();
    }

    public static void initTable() throws SQLException {
        connection = getConnection();
        deleteTables();
        createTable();
    }
}
