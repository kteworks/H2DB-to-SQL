import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		try {
			Class.forName("org.h2.Driver");

			try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {

				// テーブル名取得
				String sql = "SELECT TBL.TABLE_NAME AS TABLE_NAME FROM INFORMATION_SCHEMA.TABLES AS TBL WHERE TBL.TABLE_SCHEMA = SCHEMA() ORDER BY TBL.TABLE_NAME";
				PreparedStatement pStmt = conn.prepareStatement(sql);
				ResultSet rs = pStmt.executeQuery();
				while (rs.next()) {
					tables.add(rs.getString("TABLE_NAME"));
				}

				// カラム名とデータ型情報取得
				for (String table : tables) {
					sql = "SELECT COL.COLUMN_NAME AS COLUMN_NAME, COL.DATA_TYPE AS DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS COL WHERE COL.TABLE_SCHEMA = SCHEMA() AND COL.TABLE_NAME = '"
							+ table + "' ORDER BY COL.TABLE_NAME, COL.ORDINAL_POSITION";
					pStmt = conn.prepareStatement(sql);
					rs = pStmt.executeQuery();
					//System.out.println(table);
					ArrayList<String[]> list = new ArrayList<String[]>();
					while (rs.next()) {
						String[] column = new String[2];
						column[0] = rs.getString("COLUMN_NAME");
						column[1] = rs.getString("DATA_TYPE");
						if (column[1].equals("CHARACTER VARYING"))
							column[1] = "VARCHAR";
						else if (column[1].equals("CHARACTER"))
							column[1] = "CHAR";
						list.add(column);
					}
					columns.add(list);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		String HOST = "ec2-34-193-44-192.compute-1.amazonaws.com";
		String toDB_NAME = "de81jcg0ffo4th";
		String PORT = "5432";
		String toDB_USER = "qmlxmlqaqlyauk";
		String toDB_PASS = "86de3290ea25ece6f7128b7b1b3ff277d284dbcb0403c20c51677b47063e1cc4";

		String toJDBC_URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + toDB_NAME + "?user=" + toDB_USER
				+ "&password=" + toDB_PASS;

		CreateTablesForPostgre(toJDBC_URL, tables, columns);

		try {
			Class.forName("org.h2.Driver");

			try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
				int cnt = 0;
				for (String table : tables) {
					String sql = "SELECT * FROM " + table;
					PreparedStatement pStmt = conn.prepareStatement(sql);
					ResultSet rs = pStmt.executeQuery();
					while (rs.next()) {
						ArrayList<String[]> column = columns.get(cnt);
						ArrayList<String> data = new ArrayList<String>();
						for (String[] str : column) {
							if (rs.getString(str[0]) == null)
								data.add("null");
							else
								data.add(rs.getString(str[0]));
						}
						RunCommandForPostgre(toJDBC_URL, OutputInsertCommand(table, data));
					}
					cnt++;
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("Success");
	}

	static void CreateTablesForPostgre(String url, List<String> tables, List<ArrayList<String[]>> columns) {

		StringBuilder sb = new StringBuilder();
		int cnt1 = 0, cnt2 = 0;

		for (String table : tables) {
			sb.append("CREATE TABLE " + table + " ( ");
			ArrayList<String[]> column = columns.get(cnt1);
			for (String[] cn : column) {
				cnt2++;
				sb.append(cn[0] + " " + cn[1]);
				if (cnt2 < column.size())
					sb.append(", ");
			}
			cnt2 = 0;
			sb.append(" );");
			RunCommandForPostgre(url, sb.toString());
			sb.delete(0, sb.length());
			cnt1++;
		}
	}

	static void RunCommandForPostgre(String url, String sql) {

		try {
			// コピー先のPostgreSQLサーバーにアクセス
			Class.forName("org.postgresql.Driver");

			try (Connection conn = DriverManager.getConnection(url)) {
				// 自動コミットOFF
				conn.setAutoCommit(false);
				Statement pStmt = conn.createStatement();
				System.out.println(sql);
				pStmt.execute(sql);
				conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	static String OutputInsertCommand(String table, ArrayList<String> datas) {
		int cnt = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO " + table + " VALUES (");
		for (String data : datas) {
			if (data.equals("null"))
				sb.append("null");
			else if (TryParseInt(data))
				sb.append(data);
			else
				sb.append("\'" + data + "\'");
			cnt++;
			if (cnt < datas.size())
				sb.append(", ");
		}
		sb.append(");");
		return sb.toString();
	}

	public static boolean TryParseInt(String val) {
		try {
			Integer.parseInt(val);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
