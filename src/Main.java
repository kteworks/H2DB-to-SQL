import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		// コピー元JDBC情報
		String JDBC_URL = "jdbc:h2:tcp://localhost/~/RPGDB";
		String DB_USER = "sa";
		String DB_PASS = "";

		// テーブル名格納
		List<String> tables = new ArrayList<String>();
		// カラム名とデータ型情報格納
		List<ArrayList<String[]>> columns = new ArrayList<ArrayList<String[]>>();
		
		// コピー元のH2 Databaseにアクセス
		try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
				
			// テーブル名取得
			String sql = "SELECT TBL.TABLE_NAME AS TABLE_NAME FROM INFORMATION_SCHEMA.TABLES AS TBL WHERE TBL.TABLE_SCHEMA = SCHEMA() ORDER BY TBL.TABLE_NAME";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			ResultSet rs = pStmt.executeQuery();
			while (rs.next()) {
				tables.add(rs.getString("TABLE_NAME"));
			}
			
			// カラム名とデータ型情報取得
			for(String table : tables) {
				sql = "SELECT COL.COLUMN_NAME AS COLUMN_NAME, COL.DATA_TYPE AS DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS COL WHERE COL.TABLE_SCHEMA = SCHEMA() AND COL.TABLE_NAME = '"+ table + "' ORDER BY COL.TABLE_NAME, COL.ORDINAL_POSITION";
				pStmt = conn.prepareStatement(sql);
				rs = pStmt.executeQuery();
				//System.out.println(table);
				ArrayList<String[]> list = new ArrayList<String[]>();
				while (rs.next()) {
					String[] column = new String[2];
					column[0] = rs.getString("COLUMN_NAME");
					column[1] = rs.getString("DATA_TYPE");
					list.add(column);
				}
				columns.add(list);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String HOST = "ec2-34-193-44-192.compute-1.amazonaws.com";
		String DB_NAME = "de81jcg0ffo4th";
		String PORT = "5432";
		DB_USER = "qmlxmlqaqlyauk";
		DB_PASS = "86de3290ea25ece6f7128b7b1b3ff277d284dbcb0403c20c51677b47063e1cc4";
		
		JDBC_URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME + "?user=" + DB_USER + "&password=" + DB_PASS;
		
		CreateTablesForPostgre(JDBC_URL, tables);
	}
	
	static void CreateTablesForPostgre(String url, List<String> tables) {
		try (Connection conn = DriverManager.getConnection(url)) {
			
			String sql = "";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			ResultSet rs = pStmt.executeQuery();
			while (rs.next()) {
				tables.add(rs.getString("TABLE_NAME"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
