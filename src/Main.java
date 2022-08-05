import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Main {
	// 各種データベースごとのJDBCドライバー
	static String driver;
	static String h2Driver = "org.h2.Driver";
	static String postgreDriver = "org.postgresql.Driver";

	// コピー元H2データベース JDBC情報
	static String DB_URL = "jdbc:h2:tcp://";	// サーバーアドレス
	static String DB_USER = ""; 				// ユーザー名
	static String DB_PASS = ""; 				// パスワード
	static String JDBC_URL =  DB_URL + "?user=" + DB_USER + "&password=" + DB_PASS;

	// 移行先 PostgreSQL データベース情報
	static String HOST = ""; 					// ホスト名
	static String toDB_NAME = ""; 				// データベース名
	static String PORT = ""; 					// ポート番号
	static String toDB_USER = ""; 				// ユーザー名
	static String toDB_PASS = ""; 				// パスワード
	static String toJDBC_URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + toDB_NAME + "?sslmode=require" + "?user="
			+ toDB_USER
			+ "&password=" + toDB_PASS;

	// テーブル名格納
	static List<String> tables = new ArrayList<String>();
	// カラム名とデータ型情報格納
	static List<ArrayList<String[]>> columns = new ArrayList<ArrayList<String[]>>();
	// レコード情報格納
	static ArrayList<String> data;

	public static void main(String[] args) {
		driver = postgreDriver;

		// コピー元のH2 Databaseからテーブル名、カラム名とデータ型情報取得
		GetH2TablesColumns();
		// 移行先のテーブル、カラム作成
		CreateTables();

		// 各テーブルのレコード取得、移行
		AddGetRecords();

		
		System.out.println("プログラムを終了します。");
	}

	// コピー元のH2 Databaseからテーブル名、カラム名とデータ型情報取得
	static void GetH2TablesColumns() {
		try {
			// テーブル名取得
			String sql = "SELECT TBL.TABLE_NAME AS TABLE_NAME FROM INFORMATION_SCHEMA.TABLES AS TBL WHERE TBL.TABLE_SCHEMA = SCHEMA() ORDER BY TBL.TABLE_NAME";
			ResultSet rs = RunCommandRS(h2Driver, JDBC_URL, sql);
			while (rs.next()) {
				tables.add(rs.getString("TABLE_NAME"));
			}

			// カラム名とデータ型情報取得
			for (String table : tables) {
				sql = "SELECT COL.COLUMN_NAME AS COLUMN_NAME, COL.DATA_TYPE AS DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS COL WHERE COL.TABLE_SCHEMA = SCHEMA() AND COL.TABLE_NAME = '"
						+ table + "' ORDER BY COL.TABLE_NAME, COL.ORDINAL_POSITION";
				System.out.println("実行中コマンド:" + sql);
				rs = RunCommandRS(h2Driver, JDBC_URL, sql);
				ArrayList<String[]> list = new ArrayList<String[]>();
				while (rs.next()) {
					String[] column = new String[2];
					column[0] = rs.getString("COLUMN_NAME").toUpperCase();
					column[1] = rs.getString("DATA_TYPE").toUpperCase();
					if (column[1].indexOf("CHARACTER VARYING") > -1)
						column[1].replace("CHARACTER VARYING", "VARCHAR");
					else if (column[1].equals("CHARACTER"))
						column[1] = "CHAR";
					list.add(column);
				}
				columns.add(list);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 各テーブルのレコード取得、移行
	static void AddGetRecords() {
		try {
			int cnt = 0;
			for (String table : tables) {
				String sql = "SELECT * FROM " + table;
				System.out.println("実行中コマンド:" + sql);
				ResultSet rs = RunCommandRS(h2Driver, JDBC_URL, sql);
				while (rs.next()) {
					ArrayList<String[]> column = columns.get(cnt);
					data = new ArrayList<String>();
					for (String[] str : column) {
						if (rs.getString(str[0]) == null)
							data.add("null");
						else
							data.add(rs.getString(str[0]));
					}
					RunCommand(driver, toJDBC_URL, OutputInsertCommand(table, data));
				}
				cnt++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 移行先のテーブル、カラム作成メソッド
	static void CreateTables() {

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
			RunCommand(driver, toJDBC_URL, sb.toString());
			sb.delete(0, sb.length());
			cnt1++;
		}
	}

	// PostgreSQLに戻り値のないコマンドを実行するメソッド
	static void RunCommand(String sqlDriver, String url, String sql) {

		try {
			// コピー先のPostgreSQLサーバーにアクセス
			Class.forName(sqlDriver);

			try (Connection conn = DriverManager.getConnection(url)) {
				// 自動コミットOFF
				conn.setAutoCommit(false);
				Statement pStmt = conn.createStatement();
				System.out.println("実行中コマンド:" + sql);
				pStmt.execute(sql);
				conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	static ResultSet RunCommandRS(String sqlDriver, String url, String sql) {
		try {
			// コピー先のPostgreSQLサーバーにアクセス
			Class.forName(sqlDriver);

			try (Connection conn = DriverManager.getConnection(url)) {
				// 自動コミットOFF
				conn.setAutoCommit(false);
				Statement Stmt = conn.createStatement();
				System.out.println("実行中コマンド:" + sql);
				ResultSet rs = Stmt.executeQuery(sql);
				conn.commit();
				return rs;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	// レコード追加コマンド文字列出力
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

	// 文字列をint型数値に変換可能か確認
	public static boolean TryParseInt(String val) {
		try {
			Integer.parseInt(val);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
