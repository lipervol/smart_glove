package com.example.hello;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBLib {
    private Connection connection=null;
    private String url = "jdbc:mysql://114.116.247.153:3306/smartglove?useUnicode=true&characterEncoding=utf8";
    private String dbname = "lpbnb";
    private String dbpasswd = "nicai";

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void connToDB(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url,dbname,dbpasswd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConn(){
        if(connection != null){
            try{
                connection.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public int checkUser(User user){
        connToDB();
        if(connection==null) return 0;
        String sql="select * from usersinfo where name = ? and passwd = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,user.getName());
            preparedStatement.setString(2,user.getPasswd());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) return 1;
            else return 2;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 0;
        }
        finally {
            closeConn();
        }
    }

    public boolean adduser(User user){
        connToDB();
        if(connection==null) return false;
        String sql="insert into usersinfo(name,passwd,phone) values(?,?,?)";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,user.getName());
            preparedStatement.setString(2,user.getPasswd());
            preparedStatement.setString(3,user.getPhone());
            preparedStatement.executeUpdate();
            return preparedStatement.getUpdateCount() != 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        finally {
            closeConn();
        }
    }

    public int checkrep(User user){
        connToDB();
        if(connection==null) return 0;
        String sql="select * from usersinfo where name = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1,user.getName());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) return 1;
            else return 2;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 0;
        }
        finally {
            closeConn();
        }
    }

}
