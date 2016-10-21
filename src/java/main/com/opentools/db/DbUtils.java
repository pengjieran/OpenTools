package com.opentools.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 *
 * @author aaron
 *
 * @version v3.1
 *
 * Created on 2016年10月19日 上午9:37:02
 *
 * Description:操作数据库相关工具类
 */
public class DbUtils {

    public static final String DBTYPE_MYSQL = "mysql";

    public static final String DBTYPE_ORACLE = "oracle";

    public static final String DBTYPE_POSTGRESQL = "postgresql";

    public static final String DBTYPE_SQLSERVER = "sqlserver";

    /**
     *
     * @auther aaron
     *
     * @param dbType
     * @param dbName
     * @param userName
     * @param password
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    public static Connection getConnection(String dbType,String dbIp, String dbPort, String dbName, String userName, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

        String driver = getDriver(dbType);
        String url = getURL(dbType, dbIp, dbPort, dbName);

        Class.forName(driver).newInstance();
        return DriverManager.getConnection(url, userName, password);
    }

    /**
     * 获取数据库驱动名称
     * @auther aaron
     *
     * @param dbType
     * @return
     */
    public static String getDriver(String dbType) {

        String driverName = "";
        switch (dbType) {
            case DBTYPE_MYSQL:

                driverName = "com.mysql.jdbc.Driver";
                break;
            case DBTYPE_ORACLE:

                driverName = "oracle.jdbc.driver.OracleDriver";
                break;
            case DBTYPE_POSTGRESQL:

                driverName = "org.postgresql.Driver";
                break;

            case DBTYPE_SQLSERVER:

                driverName = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
                break;
            default:

                driverName = "unknow dbType";
                break;
        }

        return driverName;
    }

    /**
     * 拼接数据库访问地址
     * @auther aaron
     *
     * @param dbType
     * @param dbIp
     * @param dbPort
     * @param dbName
     * @return
     */
    public static final String getURL(String dbType, String dbIp, String dbPort, String dbName) {

        String url = null;

        switch (dbType) {

            case DBTYPE_MYSQL:

                url = "jdbc:mysql://" + dbIp + ":" + dbPort + "/" + dbName + "";
                break;
            case DBTYPE_ORACLE:

                url = "jdbc:oracle:thin:@" + dbIp + ":" + dbPort + ":" + dbName;
                break;
            default:
                break;
        }

        return url;
    }
}