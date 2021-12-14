package me.iron.newscaster.DBMS;

import org.lwjgl.Sys;
import org.schema.game.server.data.GameServerState;

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
    public static void main(String[] args) {
        try {
            initTable();
            connection = getConnection();
            printTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    public static void initTable() throws SQLException {
        connection = getConnection();
        createTable();
        System.out.print(printTable());
    //    connection.close();
    }

    private static String maxNIOSize = "";
    private static String getDbPath() {
        return "."+File.separator+"server-database"+File.separator;
    }
    public static Connection connection;
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:hsqldb:file:" + getDbPath() + ";shutdown=true" + maxNIOSize, "SA", "");
    }

    public static String printTable() throws SQLException {
        Statement s = connection.createStatement();
        ResultSet r = s.executeQuery(printQuery);

        StringBuilder b = new StringBuilder("Printing query: \n"+printQuery+"\n\n");
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
        r.close();
        s.close();
        return b.toString();
    }

    private static void createTable() throws SQLException {
        Statement s = connection.createStatement();

        //create table
        s.executeUpdate("CREATE TABLE IF NOT EXISTS objects (\n" +
                "\tDBID bigint PRIMARY KEY,\n" +
                "\tfaction bigint,\n" +
                "\tname varchar(50)\n" +
                "\t);\n" +
                "\t\n" +
                "CREATE TABLE IF NOT EXISTS attacks (\n" +
                "\tvictim_id bigint,\n" +
                "\tvictim_faction bigint,\n" +
                "\tvictim_received_dmg bigint, --mass lost\n" +
                "\tvictim_killed boolean,\n" +
                "\tattacker_id bigint,\n" +
                "\tattacker_faction bigint,\n" +
                "\tmillis bigint,\n" +
                "\tCONSTRAINT pk_attack PRIMARY KEY(victim_id,attacker_id,millis)\n" +
                ")");

        s.close();
    //    addEntries(s);

    }

    private static void deleteTables() throws SQLException {
        connection.createStatement().executeUpdate("DROP TABLE IF EXISTS objects;\n" +
                "DROP TABLE IF EXISTS attacks;");
    }

    public static void addAttack(long victimID, long victimFactionID, int lostMass, boolean killed, long attackerID, long attackerFactionId) {
        try {
            Statement s = connection.createStatement();
            StringBuilder q = new StringBuilder("INSERT INTO attacks VALUES (");
            q.append(victimID).append(",").append(victimFactionID).append(",").append(lostMass);
            q.append(",").append(killed).append(",").append(attackerID).append(",").append(attackerFactionId);
            q.append(",").append(System.currentTimeMillis()).append(");");
            System.out.println(q.toString());
            s.executeUpdate( q.toString());
            s.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    private static void addEntries(Statement s) throws SQLException {
        Random r = new Random(420);
        for (int i = 0; i < 1000; i++) {
            addAttack(r.nextInt(10000), 10000+r.nextInt(100), r.nextInt(50000), r.nextBoolean(), r.nextInt(10000), 10000+r.nextInt(100));
        }

    }


}
