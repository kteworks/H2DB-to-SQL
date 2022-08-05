# ◇H2DB-to-SQL
  
H2 DatabaseからPostgreSQLにデータを移行するプログラムです。  
自分で利用するために急ぎ制作しました。今後の更新、他のデータベースへの対応、例外処理等を行うかは 未定です。  
  
利用する場合は以下のドライバをライブラリに追加する必要があります。  
[H2 JDBC Driver (https://www.h2database.com/html/download.html)](https://www.h2database.com/html/download.html)  
[PostgreSQL JDBC Driver (https://jdbc.postgresql.org/download.html)](https://jdbc.postgresql.org/download.html)  
  
![image](https://user-images.githubusercontent.com/110329418/182751288-5e3e190d-9a43-4912-a94e-bc81108432d9.png)  
  
## ◇フローチャート  

```mermaid
flowchart TD
subgraph main
    mainrun([実行]) --> main1[/移行前のH2の情報を入力<br>移行先のPostgreSQLの情報を入力/] --> main2[[テーブル名<br>カラム名データ型取得]] --> main3[[移行先テーブル<br>カラム作成]] --> main4[[レコード取得,移行]] --> mainfin([プログラム終了])
end
```

```mermaid
flowchart TD
subgraph テーブル名<br>カラム名データ型取得
    gtcrun([実行]) --> gtc1[[移行前のSQLからデータを取得]] -->  gtc2(フィールドに<br>テーブル名カラム名を代入) --> gtcfin([終了])
end
subgraph レコード取得,移行
    agrrun([実行]) --> agr1[[移行前のSQLからデータを取得]] --> agr2(レコードを代入) --> agr3[[移行先SQLにデータを追加]] --> agrfin([終了])
    end
subgraph 移行先テーブルカラム作成
    tccrun([実行]) --> tcc1(取得したテーブル名から<br>テーブル作成コマンドを生成) --> tcc2[[移行先SQLにデータを追加]] --> tcc3(取得したテーブル名から<br>カラム作成コマンドを生成) --> tccfin[[移行先SQLにデータを追加]]
end
```

```mermaid
flowchart TD
db[(H2 Database)]
todb[(移行先データベース)]
subgraph 移行先SQLにデータを追加
        rc1([実行]) --> todb --> rc2([引数で渡されたコマンドを実行])--> rc3([終了])
end
subgraph 移行前のSQLからデータを取得
    rcrs1([実行]) --> db --> rcrs2([引数で渡されたコマンドを実行]) --> 結果を返す --> rcrs4([終了])
end
```

## ◇更新履歴  

```changelog
============================================
v1.0
・ソース公開

============================================
```
