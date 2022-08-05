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
db[(H2 Database)]
todb[(移行先データベース)]
    subgraph Main
        subgraph main
            main1([実行]) --> main2[/移行前のH2の情報を入力<br>移行先のPostgreSQLの情報を入力/]  -->　db --> main3(テーブル名<br>カラム名データ型取得) --> main4[[移行先テーブル<br>カラム 作成]] --> 移行先テーブルカラム作成 --> db --> main5([各テーブルのレコード取得]) --> main6(取得したレコードから<br>SQLコマンドを生成) --> 戻り値なしのSQLコマンドを実行 --> mainfin([プログラム終了])
        end
        subgraph 移行先テーブルカラム作成
            tcc1([実行]) --> tcc2(取得したテーブル名から<br>テーブル作成コマンドを生成) --> db --> SQLコマンドを実行 --> tcc3(取得したテーブル名から<br>カラム作成コマンドを生成) --> SQLコマンドを実行
        end
        subgraph 戻り値なしのSQLコマンドを実行
            rc4s1([実行]) --> todb --> rc4s2([引数で渡されたコマンドを実行]) --> rc4s3([終了])
        end
    end
```

## ◇更新履歴  

```changelog
============================================
v1.0
・ソース公開

============================================
```
